# Contributing

Thanks for working on Plugin Portal.

## Development

- Use the Gradle wrapper included in this repository.
- Keep plugin changes scoped to `plugin/` and shared runtime code in `common/`.
- Do not commit built JARs, server run folders, API keys, or local config files.
- Public plugin builds should use production Plugin Portal endpoints by default.
- Localhost API/socket URLs should require the development flag, for example `-Dpluginportal.dev=true`.
- The hosted Plugin Portal API, dashboard, release storage, and entitlement services are closed-source infrastructure and are not part of this repository.

## Endpoint Configuration

The API endpoint configuration is located in:

```text
common/src/main/kotlin/gg/flyte/pluginportal/common/util/HttpInfo.kt
```

Release builds use:

- API: `https://v3.pluginportal.link`
- WebSocket: `wss://v3.pluginportal.link`
- Dashboard/editor links: `https://pluginportal.link`

Local development uses localhost only when the server starts with:

```bash
-Dpluginportal.dev=true
```

## Useful Commands

Compile the plugin:

```bash
./gradlew :plugin:compileKotlin
```

Run tests:

```bash
./gradlew test
```

Build the release JAR:

```bash
./gradlew :plugin:shadowJar
```

Run a local Paper test server:

```bash
./gradlew :plugin:runServer
```

The built plugin JAR is written to `out/PluginPortal-<version>.jar`.

## Version Information

The current plugin version is defined in `gradle.properties`.

The build process automatically updates the version in `plugin.yml`.

Plugin Portal now builds one public JAR:

```text
PluginPortal-<version>.jar
```

Premium features are controlled by runtime entitlement and server-side API enforcement, not by a separate premium artifact.

## Local Minecraft Panel

For a local Minecraft-hosting-style UI with console, file manager, and server controls, use the Crafty Controller helper:

```bash
./panel.ts up
```

It starts Crafty in Docker, prepares Paper/Leaf server imports with the current Plugin Portal jar, and creates the Crafty server records through Crafty's v2 API.

See `docs/CRAFTY_PANEL.md`.

## Release Upload

Release operators publish stable releases through the closed-source admin API after local gates pass:

```bash
ADMIN_API_KEY=... bun scripts/release-plugin-portal.ts --version <version> --api https://v3.pluginportal.link
```

The release script updates `gradle.properties`, runs `./gradlew clean test build`, verifies the JAR, starts a local Paper smoke server with `:plugin:runServer`, runs a Plugin Portal install smoke command, then uploads the JAR to the API.

Use `--dry-run` to run the gates without uploading.

## Pull Requests

Please include:

- The behavior changed.
- Any commands or features affected.
- Validation commands you ran.
- Known follow-up work.

Security issues should be reported privately as described in `SECURITY.md`.
