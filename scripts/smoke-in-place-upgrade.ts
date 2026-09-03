#!/usr/bin/env bun
import { createWriteStream, existsSync } from "node:fs";
import { copyFile, mkdir, mkdtemp, readFile, readdir, rm, unlink, writeFile } from "node:fs/promises";
import { tmpdir } from "node:os";
import { basename, join } from "node:path";
import { spawn, type ChildProcessWithoutNullStreams } from "node:child_process";
import { pipeline } from "node:stream/promises";

const root = process.cwd();
const previousVersion = process.argv[2] ?? "3.8.5";
const currentVersion = process.argv[3] ?? "3.8.6";
const currentJar = join(root, "out", `PluginPortal-${currentVersion}.jar`);
const cacheDir = join(root, ".cache", "upgrade-smoke");
const previousJar = join(cacheDir, `PluginPortal-${previousVersion}.jar`);
const paperJar = join(cacheDir, "paper-1.21.11.jar");
const runDirectory = await mkdtemp(join(tmpdir(), "plugin-portal-upgrade-"));

if (!existsSync(currentJar)) throw new Error(`Missing ${currentJar}; build the plugin first.`);
await mkdir(cacheDir, { recursive: true });
await downloadPreviousRelease();
await downloadPaper();
await mkdir(join(runDirectory, "plugins"), { recursive: true });
await copyFile(paperJar, join(runDirectory, "server.jar"));
await copyFile(previousJar, join(runDirectory, "plugins", basename(previousJar)));
await writeFile(join(runDirectory, "eula.txt"), "eula=true\n");
await writeFile(join(runDirectory, "server.properties"), "online-mode=false\nserver-port=0\n");

try {
  const previous = await startServer();
  try {
    await expect(previous, new RegExp(`Enabling PluginPortal v${escape(previousVersion)}`), "released plugin startup");
    previous.write("pp install P1OZGk5p MODRINTH --byId");
    await expect(previous, /Downloaded ViaVersion from MODRINTH|Successfully installed ViaVersion/i, "released plugin install", 75_000);
  } finally {
    await previous.stop();
    assertHealthy(previous.output());
  }

  const dataFile = join(runDirectory, "plugins", "PluginPortal", "plugins.json");
  const before = JSON.parse(await readFile(dataFile, "utf8")) as Array<{ name?: string; platformId?: string }>;
  const tracked = before.find((plugin) => plugin.name === "ViaVersion");
  if (!tracked?.platformId) throw new Error("Released plugin did not persist ViaVersion before upgrade.");

  for (const file of await readdir(join(runDirectory, "plugins"))) {
    if (/^PluginPortal(?:Premium)?-.*\.jar$/i.test(file)) await unlink(join(runDirectory, "plugins", file));
  }
  await rm(join(runDirectory, "plugins", "update"), { recursive: true, force: true });
  await copyFile(currentJar, join(runDirectory, "plugins", basename(currentJar)));

  const current = await startServer();
  try {
    await expect(current, new RegExp(`Enabling PluginPortal v${escape(currentVersion)}`), "upgraded plugin startup");
    current.write("pp list");
    await expect(current, /Marketplace[\s\S]*ViaVersion/i, "preserved managed plugin list");
    current.write("pp update ViaVersion --refresh");
    await expect(current, /Plugin is already up to date|Successfully updated|Downloaded ViaVersion/i, "upgraded plugin refresh", 75_000);
  } finally {
    await current.stop();
    assertHealthy(current.output());
  }

  const after = JSON.parse(await readFile(dataFile, "utf8")) as Array<{ name?: string; platformId?: string }>;
  if (!after.some((plugin) => plugin.name === "ViaVersion" && plugin.platformId === tracked.platformId)) {
    throw new Error("ViaVersion tracking state changed or disappeared after upgrade.");
  }

  console.log(`In-place upgrade smoke passed: released ${previousVersion} state survived the ${currentVersion} JAR replacement.`);
} finally {
  await rm(runDirectory, { recursive: true, force: true });
}

async function startServer() {
  const child = spawn("docker", [
    "run", "--rm", "-i", "-v", `${runDirectory}:/server`, "-w", "/server",
    "eclipse-temurin:21-jre", "java", "-Xms1G", "-Xmx2G", "-jar", "server.jar", "--nogui",
  ], { stdio: ["pipe", "pipe", "pipe"] });
  let output = "";
  child.stdout.on("data", (chunk) => output += chunk.toString());
  child.stderr.on("data", (chunk) => output += chunk.toString());
  const server = {
    output: () => output,
    write: (command: string) => child.stdin.write(`${command}\n`),
    stop: async () => {
      if (child.exitCode !== null) return;
      child.stdin.write("stop\n");
      await waitForExit(child, 45_000);
    },
  };
  await expect(server, /Done \(/, "Paper startup", 180_000);
  assertHealthy(output);
  return server;
}

async function expect(server: { output: () => string }, pattern: RegExp, label: string, timeoutMs = 30_000) {
  const started = Date.now();
  while (Date.now() - started < timeoutMs) {
    if (pattern.test(clean(server.output()))) return;
    await Bun.sleep(200);
  }
  throw new Error(`Timed out waiting for ${label}.\n${tail(server.output())}`);
}

function assertHealthy(output: string) {
  const value = clean(output);
  if (/Error occurred while enabling PluginPortal|Could not load ['"]?plugins[\\/]PluginPortal|PluginPortal[^\n]*(?:NullPointerException|NoClassDefFoundError)/i.test(value)) {
    throw new Error(`PluginPortal failed during upgrade smoke.\n${tail(value)}`);
  }
}

async function downloadPreviousRelease() {
  if (existsSync(previousJar)) return;
  const versions = await fetch("https://api.modrinth.com/v2/project/5qkQnnWO/version").then((response) => response.json()) as Array<{
    version_number: string;
    files: Array<{ url: string; primary: boolean }>;
  }>;
  const release = versions.find((candidate) => candidate.version_number === previousVersion);
  const url = release?.files.find((file) => file.primary)?.url ?? release?.files[0]?.url;
  if (!url) throw new Error(`Could not find released PluginPortal ${previousVersion}.`);
  await download(url, previousJar);
}

async function downloadPaper() {
  if (existsSync(paperJar)) return;
  const metadata = await fetch("https://mcjars.app/api/v1/builds/paper/1.21.11").then((response) => response.json()) as {
    builds: Array<{ jarUrl: string }>;
  };
  const url = metadata.builds[0]?.jarUrl;
  if (!url) throw new Error("Could not resolve Paper 1.21.11.");
  await download(url, paperJar);
}

async function download(url: string, destination: string) {
  const response = await fetch(url);
  if (!response.ok || !response.body) throw new Error(`Download failed with HTTP ${response.status}.`);
  await pipeline(response.body, createWriteStream(destination));
}

function waitForExit(child: ChildProcessWithoutNullStreams, timeoutMs: number) {
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

function escape(value: string) {
  return value.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
}
