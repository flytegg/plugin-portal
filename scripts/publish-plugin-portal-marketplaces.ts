#!/usr/bin/env bun
import { existsSync } from "node:fs";
import { readFile } from "node:fs/promises";
import { join } from "node:path";
import { spawn } from "node:child_process";

type Args = {
  version: string;
  changelog: string;
  changelogFile?: string;
  dryRun: boolean;
  modrinth: boolean;
  hangar: boolean;
  skipBuild: boolean;
  channel: "release" | "beta" | "alpha";
};

const root = process.cwd();
const args = await parseArgs(Bun.argv.slice(2));
const pluginJar = join(root, "out", `PluginPortal-${args.version}.jar`);

if (!args.version.match(/^\d+\.\d+\.\d+$/)) {
  fail("Only stable x.y.z versions can be published.");
}

if (!args.skipBuild) {
  await runChecked(["./gradlew", ":plugin:build"], "plugin jar build");
}

if (!existsSync(pluginJar)) fail(`Missing artifact: ${pluginJar}`);

if (!args.modrinth && !args.hangar) {
  fail("Nothing selected. Use --modrinth, --hangar, or --all.");
}

const publishTasks: string[] = [];
if (args.modrinth) publishTasks.push(":plugin:modrinth");
if (args.hangar) publishTasks.push(":plugin:publishPluginPublicationToHangar");

await publishMarketplaces(args, publishTasks, pluginJar);

async function parseArgs(values: string[]): Promise<Args> {
  if (values.includes("--help") || values.includes("-h")) {
    console.log(`Usage: bun scripts/publish-plugin-portal-marketplaces.ts --version <x.y.z> [--all|--modrinth|--hangar] [--channel release|beta|alpha] [--changelog <text>|--changelog-file <path>] [--dry-run] [--skip-build]

Environment:
  MODRINTH_TOKEN      Required for --modrinth
  HANGAR_API_TOKEN    Required for --hangar
  HANGAR_PROJECT_ID   Optional, defaults to PluginPortal

Examples:
  bun scripts/publish-plugin-portal-marketplaces.ts --version 3.8.0 --all --dry-run
  bun scripts/publish-plugin-portal-marketplaces.ts --version 3.8.2 --modrinth --channel beta --dry-run --skip-build
  MODRINTH_TOKEN=... HANGAR_API_TOKEN=... bun scripts/publish-plugin-portal-marketplaces.ts --version 3.8.0 --all --changelog-file CHANGELOG.md`);
    process.exit(0);
  }

  const parsed: Args = {
    version: "",
    changelog: "",
    dryRun: false,
    modrinth: false,
    hangar: false,
    skipBuild: false,
    channel: "release",
  };

  for (let i = 0; i < values.length; i++) {
    const value = values[i];
    if (value === "--version") parsed.version = values[++i] ?? "";
    else if (value === "--changelog") parsed.changelog = values[++i] ?? "";
    else if (value === "--changelog-file") parsed.changelogFile = values[++i] ?? "";
    else if (value === "--channel") parsed.channel = parseChannel(values[++i] ?? "");
    else if (value === "--dry-run") parsed.dryRun = true;
    else if (value === "--modrinth") parsed.modrinth = true;
    else if (value === "--hangar") parsed.hangar = true;
    else if (value === "--all") {
      parsed.modrinth = true;
      parsed.hangar = true;
    } else if (value === "--skip-build") parsed.skipBuild = true;
    else fail(`Unknown argument: ${value}`);
  }

  if (!parsed.version) fail("--version is required.");
  if (parsed.changelogFile) parsed.changelog = await readFile(join(root, parsed.changelogFile), "utf8");
  parsed.changelog = normalizeChangelog(parsed.changelog);
  if (!parsed.changelog.trim()) parsed.changelog = `PluginPortal ${parsed.version}`;
  return parsed;
}

async function publishMarketplaces(args: Args, tasks: string[], jarPath: string) {
  if (args.modrinth && !args.dryRun && !process.env.MODRINTH_TOKEN) fail("MODRINTH_TOKEN is required for Modrinth upload.");
  if (args.hangar && !args.dryRun && !process.env.HANGAR_API_TOKEN) fail("HANGAR_API_TOKEN is required for Hangar upload.");
  
  const command = [
    "./gradlew",
    ...tasks,
    `-PmarketplaceChangelog=${args.changelog}`,
    `-PmodrinthVersionType=${args.channel}`,
    `-PhangarChannel=${toHangarChannel(args.channel)}`,
  ];

  if (process.env.HANGAR_PROJECT_ID) command.push(`-PhangarProjectId=${process.env.HANGAR_PROJECT_ID}`);
  if (args.dryRun && args.modrinth) command.push("-PmodrinthDebugMode=true");

  if (args.dryRun) {
    console.log(JSON.stringify({
      platforms: {
        modrinth: args.modrinth,
        hangar: args.hangar,
      },
      dryRun: true,
      channel: args.channel,
      file: jarPath,
      command: redactCommand(command),
      note: "Hangar does not expose a dry-run upload mode; this wrapper skips the Hangar upload task unless --dry-run is removed.",
    }, null, 2));
    if (args.hangar && !args.modrinth) return;
    if (args.modrinth) {
      await runChecked(
        command.filter((part) => part !== ":plugin:publishPluginPublicationToHangar"),
        "Modrinth dry run",
        { MODRINTH_TOKEN: process.env.MODRINTH_TOKEN ?? "dry-run-token" },
      );
    }
    return;
  }

  await runChecked(command, "marketplace upload");
}

function parseChannel(value: string): Args["channel"] {
  if (value === "stable") return "release";
  if (value === "release" || value === "beta" || value === "alpha") return value;
  fail("--channel must be release, beta, or alpha.");
}

function toHangarChannel(channel: Args["channel"]) {
  if (channel === "release") return "Release";
  if (channel === "beta") return "Beta";
  return "Alpha";
}

function normalizeChangelog(changelog: string) {
  return changelog
    .replace(/\\r\\n/g, "\n")
    .replace(/\\n/g, "\n")
    .replace(/\\t/g, "\t");
}

async function runChecked(command: string[], label: string, env: Record<string, string> = {}) {
  const code = await run(command, env);
  if (code !== 0) fail(`${label} failed.`);
}

async function run(command: string[], env: Record<string, string> = {}) {
  const proc = spawn(command[0], command.slice(1), {
    cwd: root,
    stdio: "inherit",
    env: { ...process.env, ...env },
  });
  return new Promise<number>((resolve) => proc.on("exit", (code) => resolve(code ?? 1)));
}

function redactCommand(command: string[]) {
  return command.map((part) => part.replace(/(token|key)=([^ ]+)/gi, "$1=<redacted>"));
}

function fail(message: string): never {
  console.error(message);
  process.exit(1);
}
