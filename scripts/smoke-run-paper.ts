#!/usr/bin/env bun
import { cp, mkdir, mkdtemp, readFile, readdir, rm, writeFile } from "node:fs/promises";
import { tmpdir } from "node:os";
import { dirname, join, resolve } from "node:path";
import { spawn } from "node:child_process";

const root = process.cwd();
const runDirectory = await mkdtemp(join(tmpdir(), "plugin-portal-run-paper-"));
await writeFile(join(runDirectory, "eula.txt"), "eula=true\n");
await writeFile(join(runDirectory, "server.properties"), "online-mode=false\nserver-port=0\n");
const jarFlag = process.argv.indexOf("--server-jar");
const serverJar = jarFlag >= 0 ? process.argv[jarFlag + 1] : undefined;
const versionFlag = process.argv.indexOf("--minecraft-version");
const minecraftVersion = versionFlag >= 0 ? process.argv[versionFlag + 1] : (serverJar ? "1.8.8" : "1.21.11");
if (!minecraftVersion) throw new Error("--minecraft-version requires a version");
const legacy = minecraftVersion === "1.8.8";
const expectIncompatible = process.argv.includes("--expect-incompatible");
if (jarFlag >= 0 && !serverJar) throw new Error("--server-jar requires a path to a Paper server JAR");
if (serverJar) {
  const build = Bun.spawn(["./gradlew", ":plugin:shadowJar"], { cwd: root, stdout: "inherit", stderr: "inherit" });
  if (await build.exited !== 0) throw new Error("Plugin build failed");
  const version = (await readFile(join(root, "gradle.properties"), "utf8")).match(/^projectVersion=(.+)$/m)?.[1];
  await mkdir(join(runDirectory, "plugins"));
  await cp(join(root, "out", `PluginPortal-${version}.jar`), join(runDirectory, "plugins", "PluginPortal.jar"));
  await cp(resolve(serverJar), join(runDirectory, "server.jar"));
  // Reuse Paperclip's downloaded server cache when testing an old release offline.
  await cp(join(dirname(resolve(serverJar)), "cache"), join(runDirectory, "cache"), { recursive: true }).catch(() => {});
}
const serverJavaHome = process.env.PAPER_JAVA_HOME ?? process.env.JAVA_HOME;
const child = serverJar
  ? spawn(serverJavaHome ? join(serverJavaHome, "bin", "java") : "java", ["-Xmx1G", "-jar", "server.jar", "nogui"], { cwd: runDirectory, stdio: ["pipe", "pipe", "pipe"] })
  : spawn("./gradlew", [":plugin:runServer", `-PrunDir=${runDirectory}`], { cwd: root, stdio: ["pipe", "pipe", "pipe"] });

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

  if (expectIncompatible) {
    await runCommand("pp install ViaVersion MODRINTH", /No compatible version found/, "incompatible install rejection");
    const files = await readdir(join(runDirectory, "plugins"));
    if (files.some((file) => /viaversion.*\.jar$/i.test(file))) throw new Error("Rejected install wrote a JAR");
    const tracked = JSON.parse((await readFile(join(runDirectory, "plugins", "PluginPortal", "plugins.json"), "utf8").catch(() => "")).trim() || "[]");
    if (tracked.length !== 0) throw new Error("Rejected install created tracking state");
  } else {
    child.stdin.write(`pp install ViaVersion ${legacy ? "HANGAR" : "MODRINTH"}\n`);
    await expectOutput(
      /Downloaded ViaVersion from (?:MODRINTH|HANGAR)|Successfully installed ViaVersion/i,
      "an exact-name ViaVersion install from the public API",
      75_000,
    );
    await assertInstalled("ViaVersion");

    if (!legacy) {
      await runCommand("pp install DKY9btbd MODRINTH --byId", /Downloaded WorldGuard from MODRINTH/i, "WorldGuard install", 75_000);
      await assertTrackedVersionSupports("WorldGuard", minecraftVersion);
      await runCommand(`pp install FfpCagQb MODRINTH ${minecraftVersion === "26.2" ? "release" : "beta"} --byId`, /Downloaded Enchanted Timber from MODRINTH/i, "Enchanted Timber plugin artifact", 75_000);
      await assertTrackedVersionSupports("Enchanted Timber", minecraftVersion);
    }

  }

  await runCommand("pp view ViaVersion MODRINTH --exact", /https:\/\/modrinth.com\//, "plain console marketplace details");
  await runCommand("pp help update", /--refresh/, "command-specific update help");
  await runCommand("pluginportal help install", /The channel is positional/, "long help alias");
  if (!expectIncompatible) await runCommand("pp list --page 99", /Choose a page from/, "out-of-range list page");
  await runCommand("pp list --page 0", /Page must be at least 1/, "invalid list page");

  child.stdin.write("pp search ViaVersion\n");
  await expectOutput(/ViaVersionStatus/i, "related marketplace search results", 30_000);

  child.stdin.write("pp list --outdated\n");
  await expectOutput(/All (?:managed )?plugins are up to date/i, "the combined outdated list", 30_000);

  if (!expectIncompatible) {
    child.stdin.write("pp update ViaVersion --refresh\n");
    await expectOutput(/Plugin is already up to date/i, "a fresh single-plugin update check", 30_000);

    await runCommand("pp update ViaVersion --ignoreOutdated --refresh", /Updated ViaVersion:|Successfully updated ViaVersion|Downloaded ViaVersion from (?:MODRINTH|HANGAR)/i, "explicit reinstall", 75_000);
    await runCommand("pp update ViaVersion --refresh --ignoreOutdated", /Updated ViaVersion:/i, "reordered update switches", 75_000);
    await runCommand("pp list --page 1 --detailed", /Marketplace/, "reordered list options");
    await runCommand("pp blacklist ViaVersion", /blacklisted from/, "exclude a plugin from bulk updates");
    await runCommand("pp blacklist", /ViaVersion/, "persisted exclusion list");

  }

  await runCommand("pp list --untracked", /No untracked JARs found/, "managed JARs excluded from untracked list");
  // Add a JAR after startup so Paper does not try to load this file-only fixture.
  const fixtureDir = join(runDirectory, "untracked-fixture");
  await mkdir(fixtureDir);
  await writeFile(join(fixtureDir, "plugin.yml"), "name: ManualSmoke\nversion: 1.0\nmain: test.ManualSmoke\n");
  const jarTool = process.env.JAVA_HOME ? join(process.env.JAVA_HOME, "bin", "jar") : "jar";
  const fixture = Bun.spawn([jarTool, "cf", join(runDirectory, "plugins", "manual-smoke.jar"), "-C", fixtureDir, "plugin.yml"], { stdout: "pipe", stderr: "pipe" });
  if (await fixture.exited !== 0) throw new Error("Could not create untracked JAR fixture");
  const untrackedStart = output.length;
  await runCommand("pp list --full --untracked", /manual-smoke.jar/, "untracked-only list");
  if (/Marketplace|ViaVersion/.test(clean(output.slice(untrackedStart)))) throw new Error("Untracked list included managed plugins");
  const allStart = output.length;
  await runCommand("pp list --all --full", /manual-smoke.jar/, "combined managed and untracked list");
  if (!expectIncompatible && !/ViaVersion/.test(clean(output.slice(allStart)))) throw new Error("All list omitted managed plugins");
  await runCommand("pp list --untracked --outdated", /Use --untracked without/, "conflicting list filters");

  child.stdin.write("stop\n");
  const exitCode = await waitForExit(45_000);
  if (exitCode !== 0) throw new Error(`Paper exited with code ${exitCode}.`);

  await includeLatestLog();
  assertHealthy();
  if (expectIncompatible) console.log("Install coverage: incompatible fixture rejected; successful install/update paths were not exercised.");
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

async function runCommand(command: string, pattern: RegExp, label: string, timeoutMs = 30_000) {
  const start = output.length;
  child.stdin.write(`${command}\n`);
  await expectOutput(pattern, label, timeoutMs, start);
}

async function expectOutput(pattern: RegExp, label: string, timeoutMs = 30_000, start = 0) {
  const started = Date.now();
  while (Date.now() - started < timeoutMs) {
    if (pattern.test(clean(output.slice(start)))) return;
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
    /NoSuchMethodError|Exception in.*PluginPortal/i,
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
