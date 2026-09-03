#!/usr/bin/env bun
import { mkdtemp, readFile, readdir, rm, writeFile } from "node:fs/promises";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { spawn } from "node:child_process";

const root = process.cwd();
const runDirectory = await mkdtemp(join(tmpdir(), "plugin-portal-run-paper-"));
await writeFile(join(runDirectory, "eula.txt"), "eula=true\n");
await writeFile(join(runDirectory, "server.properties"), "online-mode=false\nserver-port=0\n");
const child = spawn(
  "./gradlew",
  [":plugin:runServer", `-PrunDir=${runDirectory}`],
  { cwd: root, stdio: ["pipe", "pipe", "pipe"] },
);

let output = "";
const append = (chunk: Buffer) => {
  const text = chunk.toString();
  output += text;
  process.stdout.write(text);
};
child.stdout.on("data", append);
child.stderr.on("data", append);

try {
  await expectOutput(/Done \(/, "Paper startup", 180_000);
  assertHealthy();

  child.stdin.write("pp\n");
  await expectOutput(/\/pp install/, "the /pp help command");

  child.stdin.write("pp key get\n");
  await expectOutput(/No API key configured/, "the /pp key get command");

  child.stdin.write("pluginportal\n");
  await expectOccurrences(/\/pp install/g, 2, "the pluginportal command alias");

  child.stdin.write("pp install ViaVersion MODRINTH\n");
  await expectOutput(
    /Downloaded ViaVersion from MODRINTH|Successfully installed ViaVersion/i,
    "an exact-name ViaVersion install from the public API",
    75_000,
  );
  await assertInstalled("ViaVersion");

  child.stdin.write("pp install DKY9btbd MODRINTH --byId\n");
  await expectOutput(/Downloaded WorldGuard from MODRINTH/i, "a Minecraft-compatible WorldGuard install", 75_000);
  await assertTrackedVersionSupports("WorldGuard", "1.21.11");

  child.stdin.write("pp search ViaVersion\n");
  await expectOutput(/ViaVersionStatus/i, "related marketplace search results", 30_000);

  child.stdin.write("pp list --outdated\n");
  await expectOutput(/All managed plugins are up to date/i, "the combined outdated list", 30_000);

  child.stdin.write("pp update ViaVersion --refresh\n");
  await expectOutput(/Plugin is already up to date/i, "a fresh single-plugin update check", 30_000);

  child.stdin.write("stop\n");
  const exitCode = await waitForExit(45_000);
  if (exitCode !== 0) throw new Error(`Paper exited with code ${exitCode}.`);

  await includeLatestLog();
  assertHealthy();
  console.log("\nPaper smoke passed: PluginPortal enabled, commands ran, and Paper stopped cleanly.");
} catch (error) {
  if (child.exitCode === null) {
    child.stdin.write("stop\n");
    await waitForExit(15_000).catch(() => child.kill("SIGTERM"));
  }
  throw error;
} finally {
  await rm(runDirectory, { recursive: true, force: true });
}

async function expectOutput(pattern: RegExp, label: string, timeoutMs = 30_000) {
  const started = Date.now();
  while (Date.now() - started < timeoutMs) {
    if (pattern.test(clean(output))) return;
    if (child.exitCode !== null) throw new Error(`Paper exited before ${label}.\n${tail(output)}`);
    await Bun.sleep(200);
  }
  throw new Error(`Timed out waiting for ${label}.\n${tail(output)}`);
}

async function expectOccurrences(pattern: RegExp, count: number, label: string) {
  const started = Date.now();
  while (Date.now() - started < 30_000) {
    const matches = clean(output).match(pattern)?.length ?? 0;
    if (matches >= count) return;
    await Bun.sleep(200);
  }
  throw new Error(`Timed out waiting for ${label}.\n${tail(output)}`);
}

function assertHealthy() {
  const text = clean(output);
  const failures = [
    /Error occurred while enabling PluginPortal/i,
    /Could not load ['"]?plugins[\\/]PluginPortal/i,
    /PluginPortal[^\n]*(?:NullPointerException|NoClassDefFoundError)/i,
  ].filter((pattern) => pattern.test(text));
  if (failures.length > 0) throw new Error(`PluginPortal failed during Paper smoke.\n${tail(text)}`);
}

async function includeLatestLog() {
  const path = join(runDirectory, "logs", "latest.log");
  const log = await readFile(path, "utf8").catch(() => "");
  output += `\n${log}`;
}

async function assertInstalled(pluginName: string) {
  const pluginsDirectory = join(runDirectory, "plugins");
  const files = await readdir(pluginsDirectory);
  const installed = files.some((file) => file.endsWith(".jar") && file.toLowerCase().includes(pluginName.toLowerCase()));
  if (!installed) {
    throw new Error(`The install command succeeded but no ${pluginName} JAR exists in ${pluginsDirectory}.`);
  }
}

async function assertTrackedVersionSupports(pluginName: string, minecraftVersion: string) {
  const pluginsFile = join(runDirectory, "plugins", "PluginPortal", "plugins.json");
  let tracked: { name: string; version: string; platform: string; platformId: string } | undefined;

  for (let attempt = 0; attempt < 25 && !tracked; attempt++) {
    const contents = await readFile(pluginsFile, "utf8").catch(() => "[]");
    tracked = (JSON.parse(contents) as Array<typeof tracked>).find((plugin) => plugin?.name === pluginName);
    if (!tracked) await Bun.sleep(200);
  }
  if (!tracked) throw new Error(`${pluginName} was downloaded but not written to plugins.json.`);

  const response = await fetch(
    `https://v3.pluginportal.link/versions/platform/${tracked.platform.toLowerCase()}/${tracked.platformId}?limit=500&offset=0`,
  );
  if (!response.ok) throw new Error(`Could not verify ${pluginName} compatibility: API returned ${response.status}.`);

  const payload = await response.json() as { versions?: Array<{ versionNumber: string; mcVersions?: string[] }> };
  const version = payload.versions?.find((candidate) => candidate.versionNumber === tracked.version);
  if (!version?.mcVersions?.includes(minecraftVersion)) {
    throw new Error(`${pluginName} ${tracked.version} does not explicitly support Minecraft ${minecraftVersion}.`);
  }
}

function waitForExit(timeoutMs: number) {
  if (child.exitCode !== null) return Promise.resolve(child.exitCode);
  return new Promise<number>((resolve, reject) => {
    const timeout = setTimeout(() => reject(new Error("Timed out waiting for Paper to stop.")), timeoutMs);
    child.once("exit", (code) => {
      clearTimeout(timeout);
      resolve(code ?? 1);
    });
  });
}

function clean(value: string) {
  return value.replace(/\x1B\[[0-?]*[ -/]*[@-~]/g, "");
}

function tail(value: string, lines = 80) {
  return clean(value).split(/\r?\n/).slice(-lines).join("\n");
}
