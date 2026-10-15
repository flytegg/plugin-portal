# Plugin Portal

Plugin Portal installs, updates, and manages plugins on Bukkit-compatible Minecraft servers and Velocity proxies.
One JAR contains every platform and feature.
Search Modrinth, Hangar, SpigotMC, and free Polymart entries from chat or the server console.

## Start

1. Download the Plugin Portal JAR from the [releases page](https://github.com/flytegg/plugin-portal/releases).
2. Put it in `plugins/`.
3. Start the server.
4. Run `/pp search ViaVersion`.
5. Run `/pp install ViaVersion MODRINTH`.
6. Restart the server to load the plugin.

On Velocity, use `/ppv` instead of `/pp` for the same local and network commands.
The proxy leaves the server's `/pp` commands available.

Use `/pp help <command>` for command details.
Use `/pp list --outdated` to check for updates.
Chat results have page controls. Console lists show all entries by default.

See the [command reference](COMMANDS.md) for every command, permission, configuration option, and procedure to correct problems.
See [external sources](docs/adapters.md) for GitHub Releases and GeyserMC configuration.

## Version selection

Plugin Portal selects JARs for the server software and Minecraft version.
You can select a release channel or an exact compatible version.
Bulk updates exclude exact versions until you remove the exclusion with `/pp blacklist`.
Updates apply after a server restart.

The shared plugin code targets Java 17 or later.
Use the Java version that your server requires.
Local checks cover the universal MVP on Paper 1.21.11 and Velocity 3.4.0 with Java 21.
Other Bukkit-family adapters compile. Their runtime checks are separate.

This release does not support Java 8, BungeeCord, datapacks, or paid Polymart downloads.
A marketplace entry must supply a compatible JAR that you can download.

## Free, Premium, and paid networks

Search, view, list, install, update, and uninstall are free.
Premium commands require a valid key: `updateAll`, recognition, external sources, import/export, JAR scans, and the web editor.
A JAR from BuiltByBit or Polymart contains your key. Plugin Portal saves it at the first start.
For other purchases, run `/pp key set <key>`. `/pp info` shows `Premium: Active` or `Premium: Locked`.
Command permissions still apply.

Paid network management uses your account on the hosted Plugin Portal API.
The dashboard lists your proxy and servers across networks.

1. Open [the dashboard](https://pluginportal.link/dashboard) with your account.
2. Link a purchase.
3. Choose Add server.
4. Run its single-use `/pp link` command in the console.
5. Review the account and network.
6. Run `/pp link confirm`.

Use `/ppv` on Velocity.
Enroll each server separately. A proxy never enrolls servers automatically.
The platform determines the role. A Velocity proxy can send plugin changes to servers that allow them.
A Paper server reports its plugins.

Remote plugin changes start disabled.
On each server, run `pp control enable` in the console to approve plugin changes from the dashboard and proxy.
On a proxy, use `ppv control enable`.

Use `/pp servers`, `/pp history`, and the plugin commands with `--server` or `--servers`.
Use `/ppv servers` to see names and UUIDs on Velocity.
Select targets with `--server` or `--servers`.
Use `/ppv history` or the dashboard to inspect results.
See [commands](COMMANDS.md).

The network skips offline nodes. It does not automatically retry plugin changes.
Restart each affected server or proxy to apply file changes.
See [network configuration and security](docs/network-mvp.md).

Network access depends on the hosted API and a current entitlement. A modified
client cannot grant itself access to the hosted service. Source is available under
[LICENSE.md](LICENSE.md), which restricts entitlement bypass and unauthorized
network service access. It does not make Java bytecode impossible to modify.

## Support and contributions

Report reproducible bugs in [GitHub issues](https://github.com/flytegg/plugin-portal/issues) or ask for help in [Discord](https://flyte.gg/discord).
Include `/pp info`, the server version, and the exact command and error.
Do not share API keys or editor links.

Read [CONTRIBUTING.md](CONTRIBUTING.md) for build and test commands.
For a persistent local proxy and server, see the companion API repository's `docs/network-playground.md`.
The universal candidate is `4.0.0-beta.2`. It is not yet a published release.
Report security problems as described in [SECURITY.md](SECURITY.md).

This repository contains the Minecraft plugin.
The hosted API, dashboard, release storage, and entitlement services are separate closed-source infrastructure.

The code uses the [Plugin Portal Source Available License](LICENSE.md). It is not
an OSI-approved open-source license. The license includes the merged free and
Premium code and historical names. [Trademark terms](TRADEMARKS.md) apply separately.

Dashboard installs of exact versions report absent releases, absent platform JARs, and Minecraft compatibility mismatches separately.
After you select an exact release, **Install anyway** bypasses only its Minecraft-version metadata check.
The selected channel, Paper/Velocity artifact validation, marketplace controls, and console approval on each server still apply.
This requires the updated dashboard, shared API contract, and universal JAR.
