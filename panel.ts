#!/usr/bin/env bun
import { createWriteStream, existsSync } from "node:fs";
import { copyFile, mkdir, readFile, rm, writeFile } from "node:fs/promises";
import { createInterface } from "node:readline/promises";
import { stdin as input, stdout as output } from "node:process";
import { basename, join } from "node:path";
import { spawn } from "node:child_process";
import { pipeline } from "node:stream/promises";

type Command =
  | "setup"
  | "up"
  | "down"
  | "restart"
  | "logs"
  | "status"
  | "seed"
  | "provision"
  | "start"
  | "stop"
  | "server-logs"
  | "open"
  | "clean";
type ProfileId =
  | "paper-1.21.11"
  | "leaf-26.2"
  | "purpur-1.21.11"
  | "folia-1.21.11"
  | "spigot-1.21.11";

type Args = {
  command?: Command;
  craftyDir: string;
  profiles: ProfileId[];
  all: boolean;
  skipBuild: boolean;
  pull: boolean;
  createServers: boolean;
  yes: boolean;
  server?: string;
  lines: number;
  webPort: number;
  mcPortStart: number;
  version: string;
};

type ServerProfile = {
  id: ProfileId;
  label: string;
  type: string;
  version: string;
  build?: number;
  portOffset: number;
};

type CraftyCredentials = {
  username: string;
  password: string;
};

type CraftyServer = {
  server_id?: string;
  server_uuid?: string;
  server_name?: string;
  path?: string;
};

type CraftyServerTarget = {
  id: string;
  name: string;
  path?: string;
};

type RunResult = {
  code: number;
  stdout: string;
  stderr: string;
};

const root = process.cwd();
const defaultCraftyDir = ".crafty";
const composeContainerName = "plugin-portal-crafty";
const craftyImage = "registry.gitlab.com/crafty-controller/crafty-4:latest";
const profiles: ServerProfile[] = [
  { id: "paper-1.21.11", label: "Paper 1.21.11", type: "paper", version: "1.21.11", portOffset: 0 },
  { id: "leaf-26.2", label: "Leaf 26.2 build 2", type: "leaf", version: "26.2", build: 2, portOffset: 1 },
  { id: "purpur-1.21.11", label: "Purpur 1.21.11", type: "purpur", version: "1.21.11", portOffset: 2 },
  { id: "folia-1.21.11", label: "Folia 1.21.11", type: "folia", version: "1.21.11", portOffset: 3 },
  { id: "spigot-1.21.11", label: "Spigot 1.21.11", type: "spigot", version: "1.21.11", portOffset: 4 },
];

const args = await parseArgs(Bun.argv.slice(2));

if (!args.command) args.command = await promptForCommand(args);

if (args.command === "setup") {
  await writeCompose(args);
  await seedProfiles(args);
  if (args.pull) await compose(args, ["pull"], { inherit: true });
  await printNextSteps(args);
} else if (args.command === "up") {
  await writeCompose(args);
  if (args.profiles.length > 0 || args.all) await seedProfiles(args);
  if (args.pull) await compose(args, ["pull"], { inherit: true });
  await compose(args, ["up", "-d"], { inherit: true });
  if (args.createServers) await provisionServers(args);
  await printPanelInfo(args);
} else if (args.command === "down") {
  await compose(args, ["down"], { inherit: true });
} else if (args.command === "restart") {
  await writeCompose(args);
  await compose(args, ["up", "-d", "--force-recreate"], { inherit: true });
  await printPanelInfo(args);
} else if (args.command === "logs") {
  await compose(args, ["logs", "-f"], { inherit: true });
} else if (args.command === "status") {
  await compose(args, ["ps"], { inherit: true });
  await printPanelInfo(args);
} else if (args.command === "seed") {
  await seedProfiles(args);
  await printSeedInfo(args);
} else if (args.command === "provision") {
  await seedProfiles(args);
  await provisionServers(args);
} else if (args.command === "start") {
  await startServers(args);
} else if (args.command === "stop") {
  await stopServers(args);
} else if (args.command === "server-logs") {
  await printServerLogs(args);
} else if (args.command === "open") {
  await openUrl(`https://localhost:${args.webPort}`);
} else if (args.command === "clean") {
  await cleanCraftyDir(args);
}

async function parseArgs(values: string[]): Promise<Args> {
  if (values.includes("--help") || values.includes("-h")) {
    printHelp();
    process.exit(0);
  }

  const parsed: Args = {
    craftyDir: defaultCraftyDir,
    profiles: [],
    all: false,
    skipBuild: false,
    pull: false,
    createServers: true,
    yes: false,
    lines: 120,
    webPort: 8443,
    mcPortStart: 25500,
    version: await currentProjectVersion(),
  };

  for (let index = 0; index < values.length; index++) {
    const value = values[index];
    if (isCommand(value)) parsed.command = value;
    else if (value === "--crafty-dir") parsed.craftyDir = values[++index] ?? parsed.craftyDir;
    else if (value === "--profile") parsed.profiles.push(parseProfile(values[++index] ?? ""));
    else if (value === "--default-profiles") parsed.profiles.push("paper-1.21.11", "leaf-26.2");
    else if (value === "--all") parsed.all = true;
    else if (value === "--skip-build") parsed.skipBuild = true;
    else if (value === "--pull") parsed.pull = true;
    else if (value === "--no-create") parsed.createServers = false;
    else if (value === "--yes" || value === "-y") parsed.yes = true;
    else if (value === "--server") parsed.server = values[++index] ?? "";
    else if (value === "--lines") parsed.lines = parsePositiveInt(values[++index] ?? "", "log lines");
    else if (value === "--web-port") parsed.webPort = parsePort(values[++index] ?? "", "web port");
    else if (value === "--mc-port-start") parsed.mcPortStart = parsePort(values[++index] ?? "", "Minecraft port start");
    else if (value === "--version") parsed.version = values[++index] ?? parsed.version;
    else fail(`Unknown argument: ${value}`);
  }

  if (parsed.profiles.length === 0 && !parsed.all) parsed.profiles = ["paper-1.21.11", "leaf-26.2"];
  parsed.profiles = [...new Set(parsed.all ? profiles.map((profile) => profile.id) : parsed.profiles)];
  return parsed;
}

async function promptForCommand(args: Args): Promise<Command> {
  const rl = createInterface({ input, output });
  try {
    console.log("Plugin Portal Crafty panel");
    console.log("1. Setup files only");
    console.log("2. Start panel and create default Paper/Leaf servers");
    console.log("3. Seed import folders only");
    console.log("4. Provision Crafty server records from seeded imports");
    console.log("5. Start default Paper/Leaf servers");
    console.log("6. Show server logs");
    console.log("7. Show status");
    console.log("8. Follow Crafty container logs");
    const answer = (await rl.question("Choose an action [2]: ")).trim() || "2";
    if (answer === "1") return "setup";
    if (answer === "3") return "seed";
    if (answer === "4") return "provision";
    if (answer === "5") return "start";
    if (answer === "6") return "server-logs";
    if (answer === "7") return "status";
    if (answer === "8") return "logs";
    args.pull = await yesNo(rl, "Pull the latest Crafty image first?", false);
    args.all = await yesNo(rl, "Seed all server profiles instead of just Paper/Leaf?", false);
    if (args.all) args.profiles = profiles.map((profile) => profile.id);
    return "up";
  } finally {
    rl.close();
  }
}

async function yesNo(rl: ReturnType<typeof createInterface>, question: string, fallback: boolean): Promise<boolean> {
  const suffix = fallback ? "Y/n" : "y/N";
  const answer = (await rl.question(`${question} [${suffix}]: `)).trim().toLowerCase();
  if (!answer) return fallback;
  return answer === "y" || answer === "yes";
}

async function writeCompose(args: Args) {
  await ensureCraftyDirs(args);
  const composePath = join(args.craftyDir, "docker-compose.yml");
  const timezone = Intl.DateTimeFormat().resolvedOptions().timeZone || "Etc/UTC";
  const compose = `services:
  crafty:
    container_name: ${composeContainerName}
    image: ${craftyImage}
    restart: unless-stopped
    environment:
      - TZ=${timezone}
    ports:
      - "${args.webPort}:8443"
      - "8123:8123"
      - "19132:19132/udp"
      - "${args.mcPortStart}-${args.mcPortStart + 100}:${args.mcPortStart}-${args.mcPortStart + 100}"
    volumes:
      - ./backups:/crafty/backups
      - ./logs:/crafty/logs
      - ./servers:/crafty/servers
      - ./config:/crafty/app/config
      - ./import:/crafty/import
`;

  await writeFile(composePath, compose);
  console.log(`Wrote ${composePath}`);
}

async function seedProfiles(args: Args) {
  await ensureCraftyDirs(args);
  if (!args.skipBuild) await runChecked(["./gradlew", ":plugin:build"], "Plugin Portal build");
  const pluginJar = join(root, "out", `PluginPortal-${args.version}.jar`);
  if (!existsSync(pluginJar)) fail(`Missing ${pluginJar}. Build first or pass --version for an existing jar.`);

  for (const profileId of args.profiles) {
    const profile = profiles.find((candidate) => candidate.id === profileId);
    if (!profile) fail(`Unknown profile ${profileId}`);
    await seedProfile(args, profile, pluginJar);
  }
}

async function seedProfile(args: Args, profile: ServerProfile, pluginJar: string) {
  const importRoot = join(args.craftyDir, "import", profile.id);
  const pluginsDir = join(importRoot, "plugins");
  const serverPort = args.mcPortStart + profile.portOffset;
  await mkdir(pluginsDir, { recursive: true });

  const serverJar = await resolveServerJar(profile);
  await copyFile(serverJar, join(importRoot, "server.jar"));
  await copyFile(pluginJar, join(pluginsDir, basename(pluginJar)));
  await writeFile(join(importRoot, "eula.txt"), "eula=true\n");
  await writeFile(join(importRoot, "server.properties"), [
    "online-mode=false",
    "enable-command-block=false",
    `server-port=${serverPort}`,
    `motd=Plugin Portal ${profile.label}`,
    "",
  ].join("\n"));
  await writeFile(join(importRoot, "CRAFTY_IMPORT.md"), [
    `# ${profile.label}`,
    "",
    "Use this folder as a local import in Crafty.",
    "",
    `Server path/root: /crafty/import/${profile.id}`,
    "Executable: server.jar",
    "Execution command: java -Xms1G -Xmx2G -jar server.jar nogui",
    `Server port: ${serverPort}`,
    `Plugin Portal jar: plugins/${basename(pluginJar)}`,
    "",
  ].join("\n"));

  console.log(`Seeded ${profile.label}: ${importRoot}`);
}

async function provisionServers(args: Args) {
  await waitForCrafty(args);
  const token = await loginToCrafty(args);
  const existingServers = await getCraftyServers(args, token);
  const existingNames = new Set(existingServers.map((server) => server.server_name).filter(Boolean));

  for (const profileId of args.profiles) {
    const profile = profiles.find((candidate) => candidate.id === profileId);
    if (!profile) fail(`Unknown profile ${profileId}`);
    const serverName = `Plugin Portal ${profile.label}`;
    if (existingNames.has(serverName)) {
      console.log(`Crafty server already exists: ${serverName}`);
      continue;
    }
    await createImportArchive(args, profile);
    const response = await createCraftyServer(args, token, profile, serverName);
    const newId = response?.data?.new_server_id ?? response?.data?.new_server_uuid;
    if (newId) await ensureCraftyServerWritable(newId);
    console.log(`Created Crafty server ${serverName}${newId ? ` (${newId})` : ""}`);
    existingNames.add(serverName);
  }
}

async function startServers(args: Args) {
  await waitForCrafty(args);
  const token = await loginToCrafty(args);
  const targets = await resolveServerTargets(args, token);
  for (const target of targets) {
    await ensureEulaAccepted(args, target);
    await ensureCraftyServerWritable(target.id);
    await sendCraftyServerAction(args, token, target.id, "start_server");
    console.log(`Start requested: ${target.name}`);
  }
}

async function stopServers(args: Args) {
  await waitForCrafty(args);
  const token = await loginToCrafty(args);
  const targets = await resolveServerTargets(args, token);
  for (const target of targets) {
    await sendCraftyServerAction(args, token, target.id, "stop_server");
    console.log(`Stop requested: ${target.name}`);
  }
}

async function printServerLogs(args: Args) {
  await waitForCrafty(args);
  const token = await loginToCrafty(args);
  const targets = await resolveServerTargets(args, token);
  for (const target of targets) {
    const response = await craftyRequest(args, {
      method: "GET",
      path: `/api/v2/servers/${target.id}/logs?file=true&raw=true`,
      token,
    });
    if (response?.status !== "ok" || !Array.isArray(response.data)) {
      fail(`Could not read logs for ${target.name}: ${JSON.stringify(response)}`);
    }
    console.log(`\n== ${target.name} ==`);
    for (const line of response.data.slice(-args.lines)) {
      console.log(line);
    }
  }
}

async function resolveServerTargets(args: Args, token: string): Promise<CraftyServerTarget[]> {
  const existingServers = await getCraftyServers(args, token);
  const targets = existingServers
    .map((server) => ({
      id: server.server_id ?? server.server_uuid ?? "",
      name: server.server_name ?? "",
      path: server.path,
    }))
    .filter((server) => server.id && server.name);

  if (args.server) {
    const needle = args.server.toLowerCase();
    const matched = targets.filter((server) => (
      server.id === args.server
      || server.name.toLowerCase() === needle
      || server.name.toLowerCase().includes(needle)
    ));
    if (matched.length === 0) fail(`No Crafty server matched: ${args.server}`);
    return matched;
  }

  const names = new Set(args.profiles.map((profileId) => {
    const profile = profiles.find((candidate) => candidate.id === profileId);
    if (!profile) fail(`Unknown profile ${profileId}`);
    return `Plugin Portal ${profile.label}`;
  }));
  const matched = targets.filter((server) => names.has(server.name));
  if (matched.length === 0) {
    fail("No matching Crafty servers found. Run: ./panel.ts provision --skip-build");
  }
  return matched;
}

async function ensureEulaAccepted(args: Args, target: CraftyServerTarget) {
  const localPath = target.path?.startsWith("/crafty/servers/")
    ? join(args.craftyDir, "servers", target.path.slice("/crafty/servers/".length))
    : join(args.craftyDir, "servers", target.id);
  await mkdir(localPath, { recursive: true });
  await writeFile(join(localPath, "eula.txt"), "eula=true\n");
}

async function ensureCraftyServerWritable(serverId: string) {
  await runChecked(
    ["docker", "exec", composeContainerName, "chmod", "-R", "g+rwX", `/crafty/servers/${serverId}`],
    `chmod Crafty server ${serverId}`,
  );
}

async function sendCraftyServerAction(args: Args, token: string, serverId: string, action: string) {
  const response = await craftyRequest(args, {
    method: "POST",
    path: `/api/v2/servers/${serverId}/action/${action}`,
    token,
  });
  if (response?.status !== "ok") {
    fail(`Crafty action ${action} failed for ${serverId}: ${JSON.stringify(response)}`);
  }
}

async function createImportArchive(args: Args, profile: ServerProfile) {
  const importRoot = join(root, args.craftyDir, "import", profile.id);
  const uploadDir = join(root, args.craftyDir, "import", "upload");
  const zipPath = join(uploadDir, `${profile.id}.zip`);
  await mkdir(uploadDir, { recursive: true });
  await rm(zipPath, { force: true });
  await runChecked(["chmod", "-R", "g+rwX", "."], `chmod import ${profile.id}`, { cwd: importRoot });
  await runChecked(["zip", "-qr", zipPath, "."], `zip ${profile.id}`, { cwd: importRoot });
  console.log(`Prepared Crafty upload archive: ${join(args.craftyDir, "import", "upload", `${profile.id}.zip`)}`);
}

async function waitForCrafty(args: Args) {
  const url = `https://localhost:${args.webPort}/api/v2/servers`;
  const started = Date.now();
  while (Date.now() - started < 60_000) {
    const result = await runCapture(["curl", "-k", "-sS", "-o", "/dev/null", "-w", "%{http_code}", url]);
    if (result.stdout.trim() !== "000") return;
    await Bun.sleep(1000);
  }
  fail(`Crafty did not become reachable at https://localhost:${args.webPort}`);
}

async function loginToCrafty(args: Args): Promise<string> {
  const creds = await readCraftyCredentials(args);
  const response = await craftyRequest(args, {
    method: "POST",
    path: "/api/v2/auth/login",
    body: {
      username: creds.username,
      password: creds.password,
    },
  });
  const token = response?.data?.token;
  if (!token) fail(`Crafty login did not return a token: ${JSON.stringify(response)}`);
  return token;
}

async function readCraftyCredentials(args: Args): Promise<CraftyCredentials> {
  const path = join(args.craftyDir, "config", "default-creds.txt");
  if (!existsSync(path)) {
    fail(`Missing ${path}. Start Crafty once with: ./panel.ts up --no-create`);
  }
  return JSON.parse(await readFile(path, "utf8")) as CraftyCredentials;
}

async function getCraftyServers(args: Args, token: string): Promise<CraftyServer[]> {
  const response = await craftyRequest(args, {
    method: "GET",
    path: "/api/v2/servers",
    token,
  });
  if (response?.status !== "ok" || !Array.isArray(response.data)) {
    fail(`Could not list Crafty servers: ${JSON.stringify(response)}`);
  }
  return response.data as CraftyServer[];
}

async function createCraftyServer(args: Args, token: string, profile: ServerProfile, name: string) {
  const serverPort = args.mcPortStart + profile.portOffset;
  return await craftyRequest(args, {
    method: "POST",
    path: "/api/v2/servers",
    token,
    body: {
      name,
      monitoring_type: "minecraft_java",
      minecraft_java_monitoring_data: {
        host: "127.0.0.1",
        port: serverPort,
      },
      create_type: "minecraft_java",
      minecraft_java_create_data: {
        create_type: "import_server",
        import_server_create_data: {
          archive_name: `${profile.id}.zip`,
          archive_internal_path: "",
          jarfile: "server.jar",
          mem_min: 1,
          mem_max: 2,
          server_properties_port: serverPort,
          agree_to_eula: true,
        },
      },
    },
  });
}

async function craftyRequest(
  args: Args,
  request: { method: "GET" | "POST"; path: string; token?: string; body?: unknown },
) {
  const command = [
    "curl",
    "-k",
    "-sS",
    "-X",
    request.method,
    "-H",
    "Content-Type: application/json",
  ];
  if (request.token) command.push("-H", `Authorization: Bearer ${request.token}`);
  if (request.body !== undefined) command.push("--data", JSON.stringify(request.body));
  command.push(`https://localhost:${args.webPort}${request.path}`);

  const result = await runCapture(command);
  if (result.code !== 0) fail(`Crafty API request failed: ${result.stderr || result.stdout}`);
  try {
    return JSON.parse(result.stdout);
  } catch {
    fail(`Crafty API returned non-JSON response: ${result.stdout}`);
  }
}

async function resolveServerJar(profile: ServerProfile): Promise<string> {
  const cacheDir = join(root, ".cache", "mcjars");
  await mkdir(cacheDir, { recursive: true });
  const cachePath = join(cacheDir, `${profile.type}-${profile.version}-${profile.build ?? "latest"}.jar`);
  if (existsSync(cachePath)) return cachePath;

  const metadataUrl = `https://mcjars.app/api/v1/builds/${profile.type}/${profile.version}`;
  const response = await fetch(metadataUrl, { headers: { "User-Agent": "PluginPortalCraftySetup/1.0" } });
  if (!response.ok) fail(`Failed to fetch MCJars metadata for ${profile.id}: HTTP ${response.status}`);
  const metadata = await response.json() as { success: boolean; builds?: Array<{ buildNumber: number; jarUrl: string }> };
  const build = profile.build
    ? metadata.builds?.find((candidate) => candidate.buildNumber === profile.build)
    : metadata.builds?.[0];
  if (!metadata.success || !build?.jarUrl) fail(`No MCJars build found for ${profile.id}`);

  await download(build.jarUrl, cachePath);
  return cachePath;
}

async function download(url: string, to: string) {
  console.log(`Downloading ${url}`);
  const response = await fetch(url, { headers: { "User-Agent": "PluginPortalCraftySetup/1.0" } });
  if (!response.ok || !response.body) fail(`Download failed: ${url} HTTP ${response.status}`);
  await pipeline(response.body, createWriteStream(to));
}

async function ensureCraftyDirs(args: Args) {
  await mkdir(args.craftyDir, { recursive: true });
  for (const dir of ["backups", "logs", "servers", "config", "import"]) {
    await mkdir(join(args.craftyDir, dir), { recursive: true });
  }
}

async function cleanCraftyDir(args: Args) {
  if (!args.yes) {
    const rl = createInterface({ input, output });
    try {
      const answer = await rl.question(`Delete ${args.craftyDir}? This removes Crafty config, imports, logs, and server data. Type DELETE: `);
      if (answer !== "DELETE") {
        console.log("Clean cancelled.");
        return;
      }
    } finally {
      rl.close();
    }
  }

  if (existsSync(join(args.craftyDir, "docker-compose.yml"))) {
    await compose(args, ["down"], { inherit: true });
  }
  await rm(args.craftyDir, { recursive: true, force: true });
  console.log(`Removed ${args.craftyDir}`);
}

async function compose(args: Args, composeArgs: string[], options: { inherit?: boolean } = {}) {
  const composePath = join(args.craftyDir, "docker-compose.yml");
  if (!existsSync(composePath) && !["setup", "up", "restart"].includes(args.command ?? "")) {
    fail(`Missing ${composePath}. Run: ./panel.ts setup`);
  }
  await runChecked(["docker", "compose", "-f", composePath, ...composeArgs], `docker compose ${composeArgs.join(" ")}`, options);
}

async function runChecked(command: string[], label: string, options: { inherit?: boolean; cwd?: string } = {}) {
  const code = await run(command, options);
  if (code !== 0) fail(`${label} failed with exit code ${code}`);
}

async function run(command: string[], options: { inherit?: boolean; cwd?: string } = {}): Promise<number> {
  const proc = spawn(command[0], command.slice(1), {
    cwd: options.cwd ?? root,
    stdio: options.inherit ? "inherit" : "pipe",
  });

  if (!options.inherit) {
    proc.stdout?.on("data", (chunk) => process.stdout.write(chunk));
    proc.stderr?.on("data", (chunk) => process.stderr.write(chunk));
  }

  return await new Promise((resolve) => proc.on("close", resolve));
}

async function runCapture(command: string[], options: { cwd?: string } = {}): Promise<RunResult> {
  const proc = spawn(command[0], command.slice(1), {
    cwd: options.cwd ?? root,
    stdio: ["ignore", "pipe", "pipe"],
  });
  let stdout = "";
  let stderr = "";
  proc.stdout.on("data", (chunk) => stdout += chunk.toString());
  proc.stderr.on("data", (chunk) => stderr += chunk.toString());
  const code = await new Promise<number>((resolve) => proc.on("close", resolve));
  return { code, stdout, stderr };
}

async function currentProjectVersion(): Promise<string> {
  const gradleProperties = await readFile(join(root, "gradle.properties"), "utf8");
  return gradleProperties.match(/^projectVersion=(.+)$/m)?.[1]?.trim() ?? "3.8.2";
}

async function printNextSteps(args: Args) {
  await printSeedInfo(args);
  console.log("");
  console.log("Start Crafty and create server records:");
  console.log("  ./panel.ts up --skip-build");
}

async function printPanelInfo(args: Args) {
  console.log("");
  console.log(`Crafty panel: https://localhost:${args.webPort}`);
  console.log(`Initial login is written to ${join(args.craftyDir, "config", "default-creds.txt")} on first boot.`);
  console.log("It is also printed in the Crafty container logs:");
  console.log("  ./panel.ts logs");
}

async function printSeedInfo(args: Args) {
  console.log("");
  console.log("Crafty import folders:");
  for (const profileId of args.profiles) {
    console.log(`  /crafty/import/${profileId}`);
  }
  console.log("");
  console.log("Create Crafty server records from these folders:");
  console.log("  ./panel.ts provision --skip-build");
}

async function openUrl(url: string) {
  await runChecked(["open", url], "open Crafty panel");
}

function isCommand(value: string): value is Command {
  return [
    "setup",
    "up",
    "down",
    "restart",
    "logs",
    "status",
    "seed",
    "provision",
    "start",
    "stop",
    "server-logs",
    "open",
    "clean",
  ].includes(value);
}

function parseProfile(value: string): ProfileId {
  if (profiles.some((profile) => profile.id === value)) return value as ProfileId;
  fail(`Unknown profile: ${value}`);
}

function parsePort(value: string, label: string): number {
  const port = Number.parseInt(value, 10);
  if (!Number.isInteger(port) || port < 1 || port > 65535) fail(`Invalid ${label}: ${value}`);
  return port;
}

function parsePositiveInt(value: string, label: string): number {
  const parsed = Number.parseInt(value, 10);
  if (!Number.isInteger(parsed) || parsed < 1) fail(`Invalid ${label}: ${value}`);
  return parsed;
}

function printHelp() {
  console.log(`Usage: ./panel.ts [command] [options]

Commands:
  setup       Write .crafty/docker-compose.yml and seed import folders.
  up          Start Crafty and create default Paper/Leaf server records.
  down        Stop Crafty.
  restart     Recreate the Crafty container.
  logs        Follow Crafty logs.
  status      Show docker compose status.
  seed        Create/update Crafty import folders only.
  provision   Create Crafty server records from seeded imports.
  start       Accept EULA and start matching Crafty servers.
  stop        Stop matching Crafty servers.
  server-logs Print recent Minecraft server logs.
  open        Open https://localhost:8443.
  clean       Stop Crafty and delete .crafty data.

Options:
  --profile <id>        Server import profile. Repeatable.
  --default-profiles    Seed Paper 1.21.11 and Leaf 26.2.
  --all                 Seed all profiles.
  --version <x.y.z>     Plugin Portal jar version. Default: gradle.properties.
  --skip-build          Do not run ./gradlew :plugin:build before seeding.
  --pull                Pull latest Crafty image.
  --no-create           Start Crafty without creating server records.
  --server <name|id>    Select a Crafty server by exact/partial name or ID.
  --lines <n>           Lines to print for server-logs. Default: 120.
  --web-port <port>     Host HTTPS port. Default: 8443.
  --mc-port-start <n>   First mapped Minecraft server port. Default: 25500.
  --crafty-dir <path>   Local data directory. Default: .crafty.
  --yes, -y             Skip destructive confirmation for clean.

Profiles:
  ${profiles.map((profile) => `${profile.id.padEnd(18)} ${profile.label}`).join("\n  ")}
`);
}

function fail(message: string): never {
  console.error(message);
  process.exit(1);
}
