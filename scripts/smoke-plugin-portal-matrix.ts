#!/usr/bin/env bun
import { createHash } from "node:crypto";
import { createWriteStream, existsSync } from "node:fs";
import { copyFile, mkdir, mkdtemp, readFile, readdir, rm, writeFile } from "node:fs/promises";
import { tmpdir } from "node:os";
import { basename, join } from "node:path";
import { spawn } from "node:child_process";
import { pipeline } from "node:stream/promises";

type RuntimeId =
  | "paper-1.21.11-java21"
  | "paper-1.21.11-java25"
  | "leaf-26.2-java25"
  | "purpur-1.21.11-java21"
  | "folia-1.21.11-java21"
  | "spigot-1.21.11-java21";

type ScenarioId =
  | "fresh-noauth"
  | "fresh-auth"
  | "beta-upgrade"
  | "release-upgrade"
  | "legacy-free-upgrade"
  | "legacy-premium-upgrade";

type Args = {
  version: string;
  previousVersion: string;
  runtimes: RuntimeId[];
  scenarios: ScenarioId[];
  skipBuild: boolean;
  keepRuns: boolean;
  includeOptional: boolean;
  strictSha: boolean;
  apiBase: string;
};

type Runtime = {
  id: RuntimeId;
  type: string;
  version: string;
  java: 21 | 25;
  build?: number;
  required: boolean;
};

type ServerRun = {
  dir: string;
  output: () => string;
  write: (command: string) => void;
  stop: () => Promise<void>;
};

type Result = {
  runtime: RuntimeId;
  scenario: ScenarioId;
  status: "passed" | "failed" | "skipped";
  detail: string;
  runDir?: string;
};

const root = process.cwd();
const cacheDir = join(root, ".cache", "mcjars");
const runtimes: Runtime[] = [
  { id: "paper-1.21.11-java21", type: "paper", version: "1.21.11", java: 21, required: true },
  { id: "paper-1.21.11-java25", type: "paper", version: "1.21.11", java: 25, required: true },
  { id: "leaf-26.2-java25", type: "leaf", version: "26.2", java: 25, build: 2, required: true },
  { id: "purpur-1.21.11-java21", type: "purpur", version: "1.21.11", java: 21, required: true },
  { id: "folia-1.21.11-java21", type: "folia", version: "1.21.11", java: 21, required: true },
  { id: "spigot-1.21.11-java21", type: "spigot", version: "1.21.11", java: 21, required: false },
];
const scenarioIds: ScenarioId[] = [
  "fresh-noauth",
  "fresh-auth",
  "beta-upgrade",
  "release-upgrade",
  "legacy-free-upgrade",
  "legacy-premium-upgrade",
];

const args = parseArgs(Bun.argv.slice(2));
const results: Result[] = [];
const selectedRuntimes = runtimes.filter((runtime) => args.runtimes.includes(runtime.id));
const pluginJar = join(root, "out", `PluginPortal-${args.version}.jar`);
const premiumAliasJar = join(root, "out", `PluginPortalPremium-${args.version}.jar`);
const patchedPreviousJar = join(root, "out", `PluginPortal-${args.previousVersion}.jar`);
const publicPluginPortalId = "5qkQnnWO";

await ensureDocker();
if (!args.skipBuild) await runChecked(["./gradlew", ":plugin:build"], "plugin build");
await assertJarVersion(pluginJar, args.version);
if (existsSync(premiumAliasJar)) await assertJarVersion(premiumAliasJar, args.version);
if (args.scenarios.includes("beta-upgrade") || args.scenarios.includes("release-upgrade")) {
  await ensurePatchedPreviousJar();
}

const localSha = await sha256(pluginJar);
const public381Jar = await downloadPublicModrinthJar("3.8.1");

for (const runtime of selectedRuntimes) {
  const serverJar = await resolveServerJar(runtime);
  for (const scenario of args.scenarios) {
    if (!shouldRunScenario(runtime, scenario)) {
      results.push({ runtime: runtime.id, scenario, status: "skipped", detail: "Scenario not required for this runtime." });
      continue;
    }

    try {
      if (scenario === "fresh-noauth") await freshNoAuth(runtime, serverJar);
      else if (scenario === "fresh-auth") await freshAuth(runtime, serverJar);
      else if (scenario === "beta-upgrade") await marketplaceUpgrade(runtime, serverJar, "beta");
      else if (scenario === "release-upgrade") await marketplaceUpgrade(runtime, serverJar, "release");
      else if (scenario === "legacy-free-upgrade") await legacyUpgrade(runtime, serverJar, "free");
      else if (scenario === "legacy-premium-upgrade") await legacyUpgrade(runtime, serverJar, "premium");
    } catch (error) {
      results.push({
        runtime: runtime.id,
        scenario,
        status: runtime.required ? "failed" : "skipped",
        detail: error instanceof Error ? error.message : String(error),
      });
    }
  }
}

printSummary();
if (results.some((result) => result.status === "failed")) process.exit(1);

function parseArgs(values: string[]): Args {
  if (values.includes("--help") || values.includes("-h")) {
    console.log(`Usage: bun scripts/smoke-plugin-portal-matrix.ts --version <x.y.z> [options]

Options:
  --runtime <id>          Runtime to run. Repeatable. Default: required matrix.
  --scenario <id>         Scenario to run. Repeatable. Default: fresh-noauth,beta-upgrade.
  --previous-version <v>   Previous patched self-updater version. Default: 3.8.1.
  --full                  Run required runtimes and fresh/auth/beta/release scenarios.
  --include-optional      Include optional runtimes such as Spigot in default/full matrix.
  --include-legacy-api    Include legacy free/premium release API upgrade scenarios.
  --skip-build            Use existing out/PluginPortal-<version>.jar.
  --keep-runs             Keep temporary server directories.
  --strict-sha            Fail if staged update jar SHA differs from local out/ artifact.
  --api <url>             Plugin Portal API base. Default: https://v3.pluginportal.link.

Runtime ids:
  ${runtimes.map((runtime) => runtime.id).join("\n  ")}

Scenario ids:
  fresh-noauth, fresh-auth, beta-upgrade, release-upgrade, legacy-free-upgrade, legacy-premium-upgrade`);
    process.exit(0);
  }

  const parsed: Args = {
    version: "",
    previousVersion: "3.8.1",
    runtimes: [],
    scenarios: [],
    skipBuild: false,
    keepRuns: false,
    includeOptional: false,
    strictSha: false,
    apiBase: "https://v3.pluginportal.link",
  };

  for (let index = 0; index < values.length; index++) {
    const value = values[index];
    if (value === "--version") parsed.version = values[++index] ?? "";
    else if (value === "--previous-version") parsed.previousVersion = values[++index] ?? parsed.previousVersion;
    else if (value === "--runtime") parsed.runtimes.push(parseRuntime(values[++index] ?? ""));
    else if (value === "--scenario") parsed.scenarios.push(parseScenario(values[++index] ?? ""));
    else if (value === "--full") parsed.scenarios.push("fresh-noauth", "fresh-auth", "beta-upgrade", "release-upgrade");
    else if (value === "--include-legacy-api") parsed.scenarios.push("legacy-free-upgrade", "legacy-premium-upgrade");
    else if (value === "--include-optional") parsed.includeOptional = true;
    else if (value === "--skip-build") parsed.skipBuild = true;
    else if (value === "--keep-runs") parsed.keepRuns = true;
    else if (value === "--strict-sha") parsed.strictSha = true;
    else if (value === "--api") parsed.apiBase = values[++index] ?? parsed.apiBase;
    else fail(`Unknown argument: ${value}`);
  }

  if (!parsed.version.match(/^\d+\.\d+\.\d+$/)) fail("--version must be a stable x.y.z version.");
  if (!parsed.previousVersion.match(/^\d+\.\d+\.\d+$/)) fail("--previous-version must be a stable x.y.z version.");
  if (parsed.runtimes.length === 0) {
    parsed.runtimes = runtimes
      .filter((runtime) => runtime.required || parsed.includeOptional)
      .map((runtime) => runtime.id);
  }
  if (parsed.scenarios.length === 0) parsed.scenarios = ["fresh-noauth", "beta-upgrade"];
  parsed.scenarios = [...new Set(parsed.scenarios)];
  return parsed;
}

function parseRuntime(value: string): RuntimeId {
  if (runtimes.some((runtime) => runtime.id === value)) return value as RuntimeId;
  fail(`Unknown runtime: ${value}`);
}

function parseScenario(value: string): ScenarioId {
  if (scenarioIds.includes(value as ScenarioId)) return value as ScenarioId;
  fail(`Unknown scenario: ${value}`);
}

async function freshNoAuth(runtime: Runtime, serverJar: string) {
  const run = await startServer(runtime, serverJar, [pluginJar], {});
  try {
    await expect(run, /Premium:\s+Locked|Premium features are locked/i, "premium locked output");
    run.write("pp version");
    await expect(run, /Version:\s+3\.8\.2[\s\S]*Premium:\s+Locked/i, "locked /pp version");
    if (runtime.type === "spigot") {
      pass(runtime, "fresh-noauth", "3.8.2 starts locked and reports version; install fixture skipped for Spigot compatibility.", run.dir);
      return;
    }
    run.write("pp install ViaVersion MODRINTH --exact");
    await expect(run, /Downloaded ViaVersion from MODRINTH|Successfully installed ViaVersion/i, "ViaVersion install");
    await assertFileExists(join(run.dir, "plugins", "[PP] ViaVersion (MODRINTH).jar"));
    pass(runtime, "fresh-noauth", "3.8.2 starts locked and can install public Modrinth plugins.", run.dir);
  } finally {
    await cleanup(run);
  }
}

async function freshAuth(runtime: Runtime, serverJar: string) {
  const key = process.env.PLUGINPORTAL_TEST_API_KEY?.trim();
  if (!key) {
    results.push({ runtime: runtime.id, scenario: "fresh-auth", status: "skipped", detail: "PLUGINPORTAL_TEST_API_KEY is not set." });
    return;
  }

  const run = await startServer(runtime, serverJar, [pluginJar], { apiKey: key });
  try {
    await expect(run, /Premium entitlement verified|Enabled authenticated API requests/i, "authenticated startup");
    run.write("pp version");
    await expect(run, /Version:\s+3\.8\.2[\s\S]*Premium:\s+Unlocked/i, "unlocked /pp version");
    pass(runtime, "fresh-auth", "3.8.2 starts with existing key and premium unlocked.", run.dir);
  } finally {
    await cleanup(run);
  }
}

async function marketplaceUpgrade(runtime: Runtime, serverJar: string, channel: "beta" | "release") {
  const scenario = channel === "beta" ? "beta-upgrade" : "release-upgrade";
  if (channel === "release") await assertMarketplaceReleaseAvailable();

  const run = await startServer(runtime, serverJar, [patchedPreviousJar], {});
  try {
    run.write(channel === "release" ? "pp upgrade" : "pp upgrade --channel beta");
    await expect(run, new RegExp(`3\\.8\\.2[\\s\\S]*${channel.toUpperCase()}`, "i"), `${channel} update available`);
    run.write(channel === "release" ? "pp upgrade --yes" : "pp upgrade --yes --channel beta");
    await expect(run, /Successfully downloaded Plugin Portal v3\.8\.2|Plugin Portal has been upgraded to\s+3\.8\.2/i, "upgrade download");
    const updateJar = await findJar(join(run.dir, "plugins", "update"));
    await assertJarVersion(updateJar, args.version);
    const stagedSha = await sha256(updateJar);
    if (args.strictSha) await assertSha(updateJar, localSha);
    const shaDetail = stagedSha === localSha ? "SHA matched local artifact" : `staged SHA ${stagedSha}`;
    pass(runtime, scenario, `Patched ${args.previousVersion} updater upgrades through ${channel} channel and stages ${args.version} jar (${shaDetail}).`, run.dir);
  } finally {
    await cleanup(run);
  }
}

async function legacyUpgrade(runtime: Runtime, serverJar: string, type: "free" | "premium") {
  const scenario = type === "free" ? "legacy-free-upgrade" : "legacy-premium-upgrade";
  await assertLegacyUpdateAvailable(type);
  const key = process.env.PLUGINPORTAL_TEST_API_KEY?.trim();

  const run = await startServer(runtime, serverJar, [public381Jar], type === "premium" && key ? { apiKey: key } : {});
  try {
    run.write("pp upgrade");
    await expect(run, /3\.8\.2/i, "legacy update available");
    run.write("pp upgrade --yes");
    await expect(run, /Successfully downloaded Plugin Portal v3\.8\.2|Plugin Portal has been upgraded to\s+3\.8\.2/i, "legacy upgrade download");
    const updateJar = await findJar(join(run.dir, "plugins", "update"));
    await assertJarVersion(updateJar, args.version);
    pass(runtime, scenario, `Legacy ${type} update API stages a 3.8.2 jar.`, run.dir);
  } finally {
    await cleanup(run);
  }
}

async function startServer(runtime: Runtime, serverJar: string, plugins: string[], options: { apiKey?: string }): Promise<ServerRun> {
  const dir = await mkdtemp(join(tmpdir(), `pp-smoke-${runtime.id}-`));
  await mkdir(join(dir, "plugins"), { recursive: true });
  await copyFile(serverJar, join(dir, "server.jar"));
  for (const plugin of plugins) await copyFile(plugin, join(dir, "plugins", basename(plugin)));
  await writeFile(join(dir, "eula.txt"), "eula=true\n");
  await writeFile(join(dir, "server.properties"), "online-mode=false\nenable-command-block=false\nserver-port=0\n");

  if (options.apiKey) {
    await mkdir(join(dir, "plugins", "PluginPortal"), { recursive: true });
    await writeFile(join(dir, "plugins", "PluginPortal", "config.yml"), `Authentication:\n  ApiKey: "${options.apiKey}"\n`);
  }

  const image = runtime.java === 25 ? "eclipse-temurin:25-jre" : "eclipse-temurin:21-jre";
  const child = spawn("docker", [
    "run",
    "--rm",
    "-i",
    "-v",
    `${dir}:/server`,
    "-w",
    "/server",
    image,
    "java",
    "-Xms1G",
    "-Xmx2G",
    "-jar",
    "server.jar",
  ], { stdio: ["pipe", "pipe", "pipe"] });

  let output = "";
  const append = (chunk: Buffer) => {
    output += chunk.toString();
  };
  child.stdout.on("data", append);
  child.stderr.on("data", append);

  const run: ServerRun = {
    dir,
    output: () => output,
    write: (command: string) => child.stdin.write(`${command}\n`),
    stop: async () => {
      if (child.exitCode !== null) return;
      child.stdin.write("stop\n");
      await waitForExit(child, 45_000);
    },
  };

  await expect(run, /Done \(/, "server startup", 180_000);
  await assertNoPluginPortalCrash(run);
  return run;
}

async function cleanup(run: ServerRun) {
  await run.stop();
  await assertNoPluginPortalCrash(run);
  if (!args.keepRuns) await rm(run.dir, { recursive: true, force: true });
}

async function resolveServerJar(runtime: Runtime) {
  await mkdir(cacheDir, { recursive: true });
  const cachePath = join(cacheDir, `${runtime.type}-${runtime.version}-${runtime.build ?? "latest"}.jar`);
  if (existsSync(cachePath)) return cachePath;

  const metadataUrl = `https://mcjars.app/api/v1/builds/${runtime.type}/${runtime.version}`;
  const metadata = await fetchJson<{ success: boolean; builds: Array<{ buildNumber: number; jarUrl: string }> }>(metadataUrl);
  const build = runtime.build
    ? metadata.builds.find((candidate) => candidate.buildNumber === runtime.build)
    : metadata.builds[0];
  if (!metadata.success || !build?.jarUrl) throw new Error(`No MCJars build found for ${runtime.id}`);

  await download(build.jarUrl, cachePath);
  return cachePath;
}

async function downloadPublicModrinthJar(version: string) {
  await mkdir(join(root, ".cache", "plugins"), { recursive: true });
  const cachePath = join(root, ".cache", "plugins", `PluginPortal-${version}.jar`);
  if (existsSync(cachePath)) return cachePath;

  const versions = await fetchJson<Array<{ version_number: string; files: Array<{ url: string }> }>>(
    `https://api.modrinth.com/v2/project/${publicPluginPortalId}/version`,
  );
  const target = versions.find((candidate) => candidate.version_number === version);
  const url = target?.files?.[0]?.url;
  if (!url) throw new Error(`Could not find public Modrinth PluginPortal ${version}`);
  await download(url, cachePath);
  return cachePath;
}

async function assertMarketplaceReleaseAvailable() {
  const plugin = await fetchJson<any>(`${args.apiBase.replace(/\/$/, "")}/plugins/6881375644543c82da481311`);
  const versions = plugin?.platforms?.modrinth?.versions ?? [];
  const release = versions.find((version: any) => version.versionNumber === args.version && version.channel === "release" && version.downloadURL);
  if (!release) throw new Error(`Marketplace release ${args.version} is not available in the canonical PluginPortal entry yet.`);
}

async function assertLegacyUpdateAvailable(type: "free" | "premium") {
  const update = await fetchJson<any>(`${args.apiBase.replace(/\/$/, "")}/versions/check-update?current=3.8.1&type=${type}`);
  if (!update?.updateAvailable || update?.latest?.version !== args.version) {
    throw new Error(`Legacy ${type} release API does not offer ${args.version} yet.`);
  }
}

function shouldRunScenario(runtime: Runtime, scenario: ScenarioId) {
  if (runtime.type === "folia" && (scenario.includes("upgrade") || scenario === "fresh-auth")) return false;
  return true;
}

async function ensureDocker() {
  await capture(["docker", "version", "--format", "{{.Server.Version}}"]);
}

async function ensurePatchedPreviousJar() {
  if (existsSync(patchedPreviousJar)) {
    await assertJarVersion(patchedPreviousJar, args.previousVersion);
    return;
  }

  if (args.skipBuild) {
    throw new Error(`Missing ${patchedPreviousJar}; rerun without --skip-build to build the patched previous-version jar.`);
  }

  await runChecked(["./gradlew", ":plugin:build", `-PprojectVersion=${args.previousVersion}`], `patched ${args.previousVersion} build`);
  await assertJarVersion(patchedPreviousJar, args.previousVersion);
  await runChecked(["./gradlew", ":plugin:build", `-PprojectVersion=${args.version}`], `${args.version} build restore`);
  await assertJarVersion(pluginJar, args.version);
}

async function runChecked(command: string[], label: string) {
  const code = await run(command, { inherit: true });
  if (code !== 0) fail(`${label} failed.`);
}

async function expect(run: ServerRun, pattern: RegExp, label: string, timeoutMs = 75_000) {
  const started = Date.now();
  while (Date.now() - started < timeoutMs) {
    if (pattern.test(stripAnsi(run.output()))) return;
    await sleep(500);
  }
  throw new Error(`Timed out waiting for ${label}. Last output:\n${tail(stripAnsi(run.output()), 80)}`);
}

async function assertNoPluginPortalCrash(run: ServerRun) {
  const latestLog = join(run.dir, "logs", "latest.log");
  const log = existsSync(latestLog) ? await readFile(latestLog, "utf8") : "";
  const output = stripAnsi(`${run.output()}\n${log}`);
  const badLines = output
    .split(/\r?\n/)
    .filter((line) =>
      /PluginPortal|Plugin Portal|ExceptionInInitializerError|BukkitVersion|NumberFormatException/.test(line) &&
      /(ERROR|Exception|Caused by|SEVERE|NumberFormatException|ExceptionInInitializerError)/i.test(line),
    );
  if (badLines.length > 0) throw new Error(`PluginPortal crash/error detected:\n${badLines.slice(-20).join("\n")}`);
}

async function assertJarVersion(path: string, version: string) {
  const output = await capture(["unzip", "-p", path, "plugin.yml"]);
  if (!new RegExp(`^version:\\s*${escapeRegExp(version)}\\s*$`, "m").test(output)) {
    throw new Error(`${path} does not contain plugin.yml version ${version}`);
  }
}

async function assertFileExists(path: string) {
  if (!existsSync(path)) throw new Error(`Expected file does not exist: ${path}`);
}

async function findJar(dir: string) {
  const files = await readdir(dir).catch(() => []);
  const jar = files.find((file) => file.endsWith(".jar"));
  if (!jar) throw new Error(`No staged update jar found in ${dir}`);
  return join(dir, jar);
}

async function assertSha(path: string, expected: string) {
  const actual = await sha256(path);
  if (actual !== expected) throw new Error(`SHA mismatch for ${path}: expected ${expected}, got ${actual}`);
}

async function sha256(path: string) {
  const bytes = Buffer.from(await Bun.file(path).arrayBuffer());
  return createHash("sha256").update(bytes).digest("hex");
}

async function download(url: string, destination: string) {
  const response = await fetch(url);
  if (!response.ok || !response.body) throw new Error(`Download failed ${response.status}: ${url}`);
  await mkdir(join(destination, ".."), { recursive: true });
  await pipeline(response.body as any, createWriteStream(destination));
}

async function fetchJson<T>(url: string): Promise<T> {
  const response = await fetch(url, { headers: { "User-Agent": "PluginPortalSmoke/1.0" } });
  if (!response.ok) throw new Error(`Request failed ${response.status}: ${url}`);
  return response.json() as Promise<T>;
}

async function capture(command: string[]) {
  const proc = spawn(command[0], command.slice(1), { cwd: root, stdio: ["ignore", "pipe", "pipe"] });
  let output = "";
  proc.stdout.on("data", (chunk) => (output += chunk.toString()));
  proc.stderr.on("data", (chunk) => (output += chunk.toString()));
  const code = await new Promise<number>((resolve) => proc.on("exit", (exitCode) => resolve(exitCode ?? 1)));
  if (code !== 0) throw new Error(`${command.join(" ")} failed: ${output}`);
  return output.trim();
}

async function run(command: string[], options: { inherit?: boolean } = {}) {
  const proc = spawn(command[0], command.slice(1), { cwd: root, stdio: options.inherit ? "inherit" : "pipe" });
  return new Promise<number>((resolve) => proc.on("exit", (code) => resolve(code ?? 1)));
}

async function waitForExit(child: ReturnType<typeof spawn>, timeoutMs: number) {
  if (child.exitCode !== null) return;
  let timeout: Timer | undefined;
  await Promise.race([
    new Promise<void>((resolve) => child.on("exit", () => resolve())),
    new Promise<void>((resolve) => {
      timeout = setTimeout(() => {
        child.kill("SIGTERM");
        resolve();
      }, timeoutMs);
    }),
  ]);
  if (timeout) clearTimeout(timeout);
}

function pass(runtime: Runtime, scenario: ScenarioId, detail: string, runDir: string) {
  results.push({ runtime: runtime.id, scenario, status: "passed", detail, runDir: args.keepRuns ? runDir : undefined });
}

function printSummary() {
  console.log("\nPlugin Portal smoke matrix:");
  for (const result of results) {
    const suffix = result.runDir ? ` (${result.runDir})` : "";
    console.log(`- ${result.status.toUpperCase()} ${result.runtime} ${result.scenario}: ${result.detail}${suffix}`);
  }
}

function stripAnsi(value: string) {
  return value.replace(/\x1B\[[0-?]*[ -/]*[@-~]/g, "");
}

function tail(value: string, lines: number) {
  return value.split(/\r?\n/).slice(-lines).join("\n");
}

function escapeRegExp(value: string) {
  return value.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
}

function sleep(ms: number) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

function fail(message: string): never {
  console.error(message);
  process.exit(1);
}
