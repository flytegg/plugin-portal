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

  child.stdin.write("pp install ViaVersion MODRINTH --exact\n");
  await expectOutput(
    /Downloaded ViaVersion from MODRINTH|Successfully installed ViaVersion/i,
    "a ViaVersion install from the public API",
    75_000,
  );
  await assertInstalled("ViaVersion");

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
