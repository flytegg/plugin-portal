#!/usr/bin/env bun
import { createHash } from "node:crypto";
import { existsSync } from "node:fs";
import { copyFile, mkdir, mkdtemp, readFile, rm, writeFile } from "node:fs/promises";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { spawn } from "node:child_process";

type CommandProof = { command: string; status: "passed" | "failed" };

type Args = {
  version: string;
  api: string;
  dryRun: boolean;
  force: boolean;
  forceReason?: string;
  skipSmoke: boolean;
};

const args = parseArgs(Bun.argv.slice(2));
const commands: CommandProof[] = [];
const smokeLogExcerpts: string[] = [];

if (!args.version.match(/^\d+\.\d+\.\d+$/)) {
  fail("Only stable x.y.z versions can be released.");
}

const root = process.cwd();
const pluginJar = join(root, "out", `PluginPortal-${args.version}.jar`);
const premiumAliasJar = join(root, "out", `PluginPortalPremium-${args.version}.jar`);

await ensureVersion(args.version);
await runChecked(["./gradlew", "clean", "test", "build"], "build and tests");
await assertFile(pluginJar);
await assertJarVersion(pluginJar, args.version);
await copyFile(pluginJar, premiumAliasJar);
await assertJarVersion(premiumAliasJar, args.version);

let smokeStartedAt = new Date().toISOString();
let smokeCompletedAt = smokeStartedAt;
let installSucceeded = false;

if (!args.skipSmoke) {
  const smoke = await runSmoke(args.version);
  smokeStartedAt = smoke.startedAt;
  smokeCompletedAt = smoke.completedAt;
  installSucceeded = smoke.installSucceeded;
} else {
  commands.push({ command: "smoke skipped", status: "failed" });
  fail("Strict release requires smoke test; remove --skip-smoke.");
}

const [branch, commit] = await Promise.all([
  capture(["git", "rev-parse", "--abbrev-ref", "HEAD"]),
  capture(["git", "rev-parse", "HEAD"]),
]);
const jarProof = await Promise.all([fileProof(pluginJar), fileProof(premiumAliasJar)]);

const proof = {
  version: args.version,
  commit,
  branch,
  jars: jarProof,
  commands,
  smoke: {
    startedAt: smokeStartedAt,
    completedAt: smokeCompletedAt,
    logExcerpts: smokeLogExcerpts.slice(-40),
    installCommand: "pp install ViaVersion MODRINTH --exact",
    installSucceeded,
  },
};

if (args.dryRun) {
  console.log(JSON.stringify({ dryRun: true, proof }, null, 2));
  process.exit(0);
}

const adminKey = process.env.ADMIN_API_KEY;
if (!adminKey) fail("ADMIN_API_KEY is required for upload.");

const form = new FormData();
form.set("version", args.version);
form.set("proof", JSON.stringify(proof));
form.set("force", String(args.force));
if (args.forceReason) form.set("forceReason", args.forceReason);
form.set("pluginJar", new File([await Bun.file(pluginJar).arrayBuffer()], `PluginPortal-${args.version}.jar`, { type: "application/java-archive" }));

const response = await fetch(`${args.api.replace(/\/$/, "")}/admin/releases/plugin-portal/upload`, {
  method: "POST",
  headers: { Authorization: `Bearer ${adminKey}` },
  body: form,
});

const body = await response.text();
if (!response.ok) {
  fail(`Upload failed: ${response.status} ${body}`);
}

console.log(body);

function parseArgs(values: string[]): Args {
  if (values.includes("--help") || values.includes("-h")) {
    console.log(`Usage: ADMIN_API_KEY=... bun scripts/release-plugin-portal.ts --version <x.y.z> [--api <url>] [--dry-run] [--force --force-reason <reason>]`);
    process.exit(0);
  }

  const parsed: Args = {
    version: "",
    api: "https://v3.pluginportal.link",
    dryRun: false,
    force: false,
    skipSmoke: false,
  };

  for (let i = 0; i < values.length; i++) {
    const value = values[i];
    if (value === "--version") parsed.version = values[++i] ?? "";
    else if (value === "--api") parsed.api = values[++i] ?? parsed.api;
    else if (value === "--dry-run") parsed.dryRun = true;
    else if (value === "--force") parsed.force = true;
    else if (value === "--force-reason") parsed.forceReason = values[++i];
    else if (value === "--skip-smoke") parsed.skipSmoke = true;
    else fail(`Unknown argument: ${value}`);
  }

  if (!parsed.version) fail("--version is required.");
  return parsed;
}

async function ensureVersion(version: string) {
  const path = join(root, "gradle.properties");
  const current = await readFile(path, "utf8");
  const next = current.replace(/^projectVersion=.*$/m, `projectVersion=${version}`);
  if (current !== next) await writeFile(path, next);
}

async function runChecked(command: string[], label: string) {
  const commandString = command.join(" ");
  const result = await run(command, { inherit: true });
  commands.push({ command: commandString, status: result === 0 ? "passed" : "failed" });
  if (result !== 0) fail(`${label} failed.`);
}

async function runSmoke(version: string) {
  const runDir = join(root, "plugin", "run", "release-smoke");
  await rm(runDir, { recursive: true, force: true });
  await mkdir(runDir, { recursive: true });
  await writeFile(join(runDir, "eula.txt"), "eula=true\n");
  await writeFile(join(runDir, "server.properties"), "online-mode=false\nenable-command-block=false\nserver-port=0\n");

  const startedAt = new Date().toISOString();
  const child = spawn("./gradlew", [":plugin:runServer", "-PrunDir=run/release-smoke"], {
    cwd: root,
    stdio: ["pipe", "pipe", "pipe"],
  });

  let output = "";
  let installed = false;
  const append = (chunk: Buffer) => {
    const text = chunk.toString();
    output += text;
    for (const line of text.split(/\r?\n/).filter(Boolean)) {
      if (line.includes("PluginPortal") || line.includes("Done (") || line.includes("ViaVersion") || line.includes("ERROR") || line.includes("Exception")) {
        smokeLogExcerpts.push(line);
      }
    }
    if (text.includes("Done (") && !installed) {
      installed = true;
      child.stdin.write("pp install ViaVersion MODRINTH --exact\n");
      setTimeout(() => child.stdin.write("stop\n"), 18_000);
    }
  };

  child.stdout.on("data", append);
  child.stderr.on("data", append);

  const timeout = setTimeout(() => {
    child.stdin.write("stop\n");
    child.kill("SIGTERM");
  }, 120_000);

  const code = await new Promise<number>((resolve) => {
    child.on("exit", (exitCode) => resolve(exitCode ?? 1));
  });
  clearTimeout(timeout);

  const completedAt = new Date().toISOString();
  const logPath = join(runDir, "logs", "latest.log");
  const logOutput = existsSync(logPath) ? await readFile(logPath, "utf8") : "";
  const proofOutput = `${output}\n${logOutput}`;
  const started = proofOutput.includes("Done (");
  const noCrash = !logOutput.match(/(Exception|ERROR|Plugin Portal API may be down)/i);
  const installSucceeded =
    proofOutput.includes("Successfully installed ViaVersion") ||
    proofOutput.includes("ViaVersion (MODRINTH)") ||
    proofOutput.includes("Downloaded ViaVersion from MODRINTH") ||
    existsSync(join(runDir, "plugins", "[PP] ViaVersion (MODRINTH).jar"));

  const serverPassed = started && noCrash;
  commands.push({ command: "./gradlew :plugin:runServer -PrunDir=run/release-smoke", status: serverPassed ? "passed" : "failed" });
  commands.push({ command: "pp install ViaVersion MODRINTH --exact", status: installSucceeded ? "passed" : "failed" });

  if (!serverPassed || !installSucceeded) {
    fail(`Smoke test failed. started=${started} noCrash=${noCrash} installSucceeded=${installSucceeded}`);
  }

  return { startedAt, completedAt, installSucceeded };
}

async function assertFile(path: string) {
  if (!existsSync(path)) fail(`Missing artifact: ${path}`);
}

async function assertJarVersion(path: string, version: string) {
  const tempDir = await mkdtemp(join(tmpdir(), "pp-release-jar-"));
  try {
    await capture(["jar", "xf", path, "plugin.yml"], { cwd: tempDir });
    const pluginYml = await readFile(join(tempDir, "plugin.yml"), "utf8");
    if (!pluginYml.includes(`version: ${version}`)) fail(`${path} has wrong plugin.yml version.`);
  } finally {
    await rm(tempDir, { recursive: true, force: true });
  }
}

async function fileProof(path: string) {
  const file = Bun.file(path);
  const bytes = Buffer.from(await file.arrayBuffer());
  return {
    filename: path.split("/").at(-1) ?? path,
    sha256: createHash("sha256").update(bytes).digest("hex"),
    size: bytes.byteLength,
  };
}

async function capture(command: string[], options: { cwd?: string } = {}) {
  const proc = spawn(command[0], command.slice(1), { cwd: options.cwd ?? root });
  let output = "";
  proc.stdout.on("data", (chunk) => (output += chunk.toString()));
  proc.stderr.on("data", (chunk) => (output += chunk.toString()));
  const code = await new Promise<number>((resolve) => proc.on("exit", (exitCode) => resolve(exitCode ?? 1)));
  if (code !== 0) fail(`${command.join(" ")} failed: ${output}`);
  return output.trim();
}

async function run(command: string[], options: { inherit?: boolean } = {}) {
  const proc = spawn(command[0], command.slice(1), {
    cwd: root,
    stdio: options.inherit ? "inherit" : "pipe",
  });
  return new Promise<number>((resolve) => proc.on("exit", (code) => resolve(code ?? 1)));
}

function fail(message: string): never {
  console.error(message);
  process.exit(1);
}
