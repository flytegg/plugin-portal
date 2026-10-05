# Plugin Portal

Plugin Portal installs, updates, and manages plugins on Bukkit-compatible Minecraft
servers and Velocity proxies. One JAR contains every platform and feature. Search Modrinth, Hangar, SpigotMC, and free Polymart listings from chat
or the server console.

## Get started

1. Download the Plugin Portal JAR from the [releases page](https://github.com/flytegg/plugin-portal/releases).
2. Put it in `plugins/` and start the server.
3. Run `/pp search ViaVersion`.
4. Run `/pp install ViaVersion MODRINTH`.
5. Restart the server to load the plugin.

On Velocity, use `/ppv` instead of `/pp`. The proxy also exposes `/ppnetwork` for
network management and leaves backend `/pp` commands available.

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

The shared plugin code targets Java 17 or later. Use the Java version required by
your server. The universal MVP is checked with Paper 1.21.11 and Velocity 3.4.0
on Java 21. Other Bukkit-family adapters compile; their runtime checks are separate.
Java 8, BungeeCord, datapacks, and paid Polymart downloads are outside this release.
A marketplace listing must supply a compatible downloadable JAR.

## Free local tools and paid networks

The dashboard lists your proxy and backends across groups. Link a purchase, use
Add server, and run its one-use `/pp link` command in each console. Dashboard
plugin changes default to read-only and require per-server console approval.
Use `/pp servers`, `/pp history`, and existing commands with `--server` or
`--servers`; use `/ppv` on Velocity. See [commands](COMMANDS.md).

All local commands are free: recognition, bulk updates, external sources,
import/export, JAR scanning, and the temporary web editor are included.
A local command permission still applies.

Paid network management uses your account on the hosted Plugin Portal API. Open
[the dashboard](https://pluginportal.link/dashboard), choose Add server, and run
the generated link command in each console. Give your trusted proxy controller
access to manage servers in its group.

Run `/ppv servers` to see names and UUIDs. Target existing commands with `--server`
or `--servers`, then use `/ppv history` or the dashboard to inspect results.
Offline nodes are skipped. File changes require a restart of each affected node.
See [network setup and security](docs/network-mvp.md).

Network access depends on the hosted API and a current entitlement. A modified
client cannot grant itself access to the hosted service. Source is available under
[LICENSE.md](LICENSE.md), which restricts entitlement bypass and unauthorized
network service access. It does not make Java bytecode impossible to modify.

## Support and contributions

Report reproducible bugs in [GitHub issues](https://github.com/flytegg/plugin-portal/issues)
or ask for help in [Discord](https://flyte.gg/discord). Include `/pp info`, the
server version, and the exact command and error. Do not share API keys or editor links.

Read [CONTRIBUTING.md](CONTRIBUTING.md) for build and test commands.
For a persistent local proxy and backend, see the sibling API repository's
`docs/network-playground.md`. The universal candidate is `4.0.0-beta.2` and is
not published yet.
Report security problems as described in [SECURITY.md](SECURITY.md).

This repository contains the Minecraft plugin. The hosted API, dashboard, release
storage, and entitlement services are separate closed-source infrastructure.

The code uses the [Plugin Portal Source Available License](LICENSE.md). It is not
an OSI-approved open-source license. The license includes the merged free and
Premium code and historical names. [Trademark terms](TRADEMARKS.md) apply separately.
