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

The common development loop is available through `make`:

```bash
make test   # Fast MockBukkit checks
make check  # Tests plus a complete build
make smoke  # Disposable real-Paper install test
make paper  # Reusable Paper server connected to the local API
```

`make paper` passes the localhost development flag automatically. It expects the API at
`http://localhost:3001` and keeps its server files in `run/latest`.

The underlying Gradle commands remain available when a more specific task is needed.

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

Run the automated Paper startup and command smoke test:

```bash
./gradlew :plugin:paperSmoke
```

This starts a disposable Paper 1.21.11 server. It checks startup, command aliases,
command help, list options, flag order, compatible Modrinth downloads, explicit
reinstall, and bulk-update exclusions. It stops the server and removes the test
folder. It requires Bun, internet access, and a healthy public API.

To check an existing legacy Paper server JAR without changing its server folder:

```bash
JAVA_HOME=/path/to/compatible/jdk bun scripts/smoke-run-paper.ts --server-jar /path/to/server.jar
```

The script copies the server JAR and its adjacent Paperclip cache into a temporary
folder. It runs the same basic commands and a ViaVersion download. The modern-only
WorldGuard and Enchanted Timber checks do not run on this path.

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

## Documentation

Update `COMMANDS.md` when a command, permission, option, or configuration behavior
changes. Keep the hosted documentation consistent in the API/web repository.
Use short sentences, active voice, and one action per instruction. Use the same
term for the same thing. Keep literal command names and configuration keys exact.
These conventions follow the intent of ASD-STE100. Do not claim formal compliance
without checking its complete rules and dictionary.

Describe shipped behavior. Label planned or unsupported behavior explicitly.
Do not describe version selection or self-upgrade as Premium-only.
