# Plugin Portal

Plugin Portal installs, updates, and manages plugins on Bukkit-compatible Minecraft
servers. Search Modrinth, Hangar, SpigotMC, and free Polymart listings from chat
or the server console.

## Get started

1. Download the Plugin Portal JAR from the [releases page](https://github.com/flytegg/plugin-portal/releases).
2. Put it in `plugins/` and start the server.
3. Run `/pp search ViaVersion`.
4. Run `/pp install ViaVersion MODRINTH`.
5. Restart the server to load the plugin.

Use `/pp help <command>` for command details. Use `/pp list --outdated` to check
for updates. Chat results have page controls. Console lists show all entries by
default.

See the [command reference](COMMANDS.md) for every command, permission, configuration
setting, and troubleshooting procedure. See [external sources](docs/adapters.md)
for GitHub Releases and GeyserMC setup.

## Version selection

Plugin Portal selects JARs for the server software and Minecraft version.
You can select a release channel or an exact compatible version. Exact versions
are excluded from bulk updates until you remove the exclusion with `/pp blacklist`.
Updates take effect after a server restart.

The plugin targets Java 17 or later. Use the Java version required by your server.
The runtime checks pass on Paper 1.21.11 and Paper 1.8.8 with Java 21.
Java 8 hosts are not supported. See [contributing](CONTRIBUTING.md).

Datapack management, proxy-server plugins, and paid Polymart downloads are not
supported. A marketplace listing must supply a downloadable compatible JAR.

## Premium

The same JAR contains the free and Premium commands. A valid Plugin Portal key
unlocks recognition, bulk marketplace updates, external sources, import/export,
JAR scanning, and the temporary web editor.

Run `/pp key set <key>`, then `/pp info` to check access. A valid key refreshes
access without a restart. Marketplace-delivered licenses can import automatically.
See [license import](docs/marketplace-license-import.md).

Single-plugin install/update, version and channel selection, and Plugin Portal
self-upgrade do not require Premium.

[Get Plugin Portal Premium](https://polymart.org/product/6974/plugin-portal-premium)

## Support and contributions

Report reproducible bugs in [GitHub issues](https://github.com/flytegg/plugin-portal/issues)
or ask for help in [Discord](https://flyte.gg/discord). Include `/pp info`, the
server version, and the exact command and error. Do not share API keys or editor links.

Read [CONTRIBUTING.md](CONTRIBUTING.md) for build and test commands.
Report security problems as described in [SECURITY.md](SECURITY.md).

This repository contains the Minecraft plugin. The hosted API, dashboard, release
storage, and entitlement services are separate closed-source infrastructure.

The code uses the [Plugin Portal Source Available License](LICENSE.md). It is not
an OSI-approved open-source license. The license includes the merged free and
Premium code and historical names. [Trademark terms](TRADEMARKS.md) apply separately.
