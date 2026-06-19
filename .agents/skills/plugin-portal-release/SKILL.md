---
name: plugin-portal-release
description: Build, test, publish, and verify Plugin Portal releases to GitHub, Modrinth, and Hangar.
---

# Plugin Portal Release

Use this skill when publishing Plugin Portal from this repository.

## Release Channels

Plugin Portal uses normal `x.y.z` versions in `gradle.properties`, `plugin.yml`,
Git tags, GitHub releases, and marketplace version numbers. Channel is release
metadata, not part of the JAR filename.

- `beta`: GitHub prerelease, Modrinth `beta`, Hangar `Beta` when that channel exists, no admin release API upload.
- `release`: GitHub full release, Modrinth `release`, Hangar `Release`, admin release API upload only when stable auto-updates should offer the version.
- `alpha`: GitHub prerelease, Modrinth `alpha`, Hangar `Alpha`, no admin release API upload unless explicitly requested.

For a beta such as `3.8.3 beta`, build `out/PluginPortal-3.8.3.jar` and publish
that version as a beta channel.

## Guardrails

- Inspect `git status --short --branch` before changing files.
- Treat unrelated dirty files as user-owned.
- Never print, commit, or paste `MODRINTH_TOKEN`, `HANGAR_API_TOKEN`, `ADMIN_API_KEY`, R2 credentials, license keys, or device tokens.
- Use `committer "message" <files...>` for normal commits.
- Do not use the admin release API for beta releases; that endpoint controls Plugin Portal's stable automatic updater behavior.
- Use real Markdown release notes files. Do not pass literal `\n` strings to release commands.

## Preflight

Confirm the target version does not already exist:

```bash
gh release view "v<x.y.z>"
curl -fsSL https://api.modrinth.com/v2/project/pluginportal/version | jq 'map(select(.version_number == "<x.y.z>"))'
curl -fsSL 'https://hangar.papermc.io/api/v1/projects/Flyte/PluginPortal/versions?limit=50' | jq '.result | map(select(.name == "<x.y.z>"))'
```

If the version already exists anywhere, stop and ask whether to replace it or
use a new version.

## Build and Test

Run the strict dry-run release gate:

```bash
bun scripts/release-plugin-portal.ts --version <x.y.z> --dry-run
```

This updates `gradle.properties`, runs `./gradlew clean test build`, verifies
the built JAR metadata, starts a local Paper smoke server, and runs a Plugin
Portal install smoke command.

Run marketplace publish dry-run. If Hangar does not have a configured `Beta`
channel, use its existing non-release `Snapshot` channel explicitly:

```bash
bun scripts/publish-plugin-portal-marketplaces.ts --version <x.y.z> --all --channel <release|beta|alpha> --changelog-file <notes.md> --dry-run --skip-build
bun scripts/publish-plugin-portal-marketplaces.ts --version <x.y.z> --all --channel beta --hangar-channel Snapshot --changelog-file <notes.md> --dry-run --skip-build
```

## GitHub Release

Create beta/alpha releases as prereleases:

```bash
gh release create "v<x.y.z>" \
  "out/PluginPortal-<x.y.z>.jar" \
  --title "PluginPortal <x.y.z> Beta" \
  --notes-file <notes.md> \
  --prerelease \
  --target "$(git rev-parse HEAD)"
```

Create stable releases without `--prerelease`.

Verify:

```bash
gh release view "v<x.y.z>" --json tagName,isPrerelease,name,assets,url
```

## Marketplace Publishing

Live publish after dry-run and GitHub release:

```bash
set -a; source <env-file-containing-marketplace-tokens>; set +a
bun scripts/publish-plugin-portal-marketplaces.ts --version <x.y.z> --all --channel <release|beta|alpha> --changelog-file <notes.md> --skip-build
```

The script publishes through Gradle:

- Modrinth task: `:plugin:modrinth`, token `MODRINTH_TOKEN`.
- Hangar task: `:plugin:publishPluginPublicationToHangar`, token `HANGAR_API_TOKEN`.
- Hangar has no dry-run upload mode, so the wrapper skips Hangar during `--dry-run`.
- Use `--hangar-channel Snapshot` when publishing a beta before a true Hangar `Beta` channel has been configured with a color on the project.

Verify public API results:

```bash
curl -fsSL https://api.modrinth.com/v2/project/pluginportal/version | jq 'map(select(.version_number == "<x.y.z>"))'
curl -fsSL 'https://hangar.papermc.io/api/v1/projects/Flyte/PluginPortal/versions?limit=50' | jq '.result | map(select(.name == "<x.y.z>"))'
```

Download the published JAR and inspect metadata:

```bash
curl -fsSL -o /tmp/PluginPortal-<x.y.z>.jar '<download-url-from-public-api>'
tmp="$(mktemp -d)" && (cd "$tmp" && jar xf /tmp/PluginPortal-<x.y.z>.jar plugin.yml && cat plugin.yml)
```

## Post-Publish Smoke

After a beta is visible on Modrinth/Hangar, test marketplace upgrade behavior:

```bash
bun scripts/smoke-plugin-portal-matrix.ts --version <x.y.z> --previous-version 3.8.1 --scenario beta-upgrade
```

For production releases, also test release-channel upgrade behavior before
uploading to the admin release API.
