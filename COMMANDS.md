# Plugin Portal commands

Use `/pp`, `/pluginportal`, or `/ppm`. In the server console, omit `/`.
Use `/pp help <command>` for instructions, for example `/pp help update`.
Quote names, filenames, or version labels that contain spaces.

Plugin Portal uses one JAR. All local management is free.
Network management requires an active account entitlement. The hosted API checks this entitlement.
A permission does not grant network access. On Velocity, replace `/pp` with `/ppv`.

Help, hover hints, and command buttons use the alias that you entered.
For example, `/ppm help` suggests `/ppm list`, and `/ppv help` suggests `/ppv list`.
If arguments are absent or invalid, Plugin Portal shows help for that command.
For example, `/ppv platform` shows the command syntax and an example.

Command discovery and help require `pluginportal.view`.
For limited roles, grant this permission with the permissions for each action.
Players without it cannot see or complete the command root.
On Bukkit, operators and `pluginportal.admin` have local command access by default.
Velocity uses its permission provider. Give proxy administrators `pluginportal.admin`.

Network commands still require `pluginportal.network` and a hosted entitlement.

## Start here

1. Put `PluginPortal-<version>.jar` in `plugins/`.
2. Start the server.
3. Run `/pp info` to check the version, platform, and network connection.
4. Run `/pp search ViaVersion` to find a plugin.
5. Run `/pp install ViaVersion MODRINTH` to download it.
6. Restart the server to load the new JAR.

Plugin Portal supports marketplace plugins from Modrinth, Hangar, SpigotMC, and free Polymart entries.
A marketplace entry must supply a JAR that you can download.
Plugin Portal does not support paid Polymart downloads.

Plugin Portal manages Bukkit-compatible plugins and Velocity proxy plugins.
This release does not support BungeeCord or datapacks.

## Find and list plugins

| Command | Purpose |
| --- | --- |
| `/pp help [page or command]` | Shows help pages 1 to 3, or help for a command. |
| `/pp search <query> [platform] [--page <number>] [--full]` | Searches the marketplace catalog. |
| `/pp view <name> [platform] [--byId] [--exact]` | Shows marketplace details and available actions. |
| `/pp list [--all] [--untracked] [--outdated] [--external] [--detailed] [--page <number>] [--full]` | Lists local plugins and configured external plugins. |
| `/pp info` | Shows the exact version, platform, free local tools, network connection, and number of changes that require a restart. Alias: `/pp version`. |

These commands require `pluginportal.view`.

Lists and search results show eight entries per page in chat.
Use the page buttons or `--page 2` to continue.
Use `--full` to show every entry.
Unless you request a page, the console shows every entry.
Page numbers start at 1. Do not combine `--page` and `--full`.

List options:

- `--untracked` shows only JARs that Plugin Portal does not manage. It uses local files and does not check remote sources. Do not combine it with `--all`, `--outdated`, or `--external`.
- `--all` includes unrecognized JARs. Do not combine it with `--outdated` or `--external`.
- `--outdated` shows available marketplace and external updates. It shows failed checks separately.
- `--external` shows only configured external plugins.
- `--detailed` includes versions, IDs, and source details. Console output includes these details by default.

Use `MODRINTH`, `HANGAR`, `SPIGOTMC`, or `POLYMART` for `[platform]`.
For `/pp view` and `/pp install`, `--byId` requires a platform.
On Modrinth, use the project ID, such as `P1OZGk5p`, instead of the URL slug.
`--exact` or `-e` matches the full plugin name without case sensitivity.

## Install and update

| Command | Permission | Purpose |
| --- | --- | --- |
| `/pp install <name> [platform] [channel] [--byId] [--exact] [--version <version>]` | `pluginportal.manage.install` | Downloads a compatible plugin JAR. |
| `/pp update <name> [--byId] [--ignoreOutdated] [--refresh] [--channel <name>] [--version <version>]` | `pluginportal.maintain.update` | Updates one tracked plugin. |
| `/pp updateAll [--ignoreOutdated]` | `pluginportal.maintain.update` | Updates tracked marketplace plugins. |
| `/pp blacklist [name] [--byId]` | `pluginportal.maintain.update` | Changes the exclusion from `updateAll`. Lists exclusions if you omit the name. |
| `/pp platform <name> <platform> [--byId]` | `pluginportal.maintain.update` | Downloads a compatible version from another linked marketplace. |
| `/pp uninstall <name> [--byId]` | `pluginportal.manage.uninstall` | Removes a tracked plugin JAR. Alias: `/pp delete`. |
| `/pp install-url <url>` | `pluginportal.manage.install-url` | Downloads a JAR from a direct URL. Requires the console. |
| `/pp upgrade [--yes] [--channel <name>]` | `pluginportal.admin` | Checks for a Plugin Portal update. Add `--yes` to download it. |

Restart after an install, update, uninstall, platform change, or self-upgrade.
Plugin Portal stages Paper updates in its update folder, normally `plugins/update/`.
It stages Velocity updates in `plugins/pluginportal/pending-updates/`.

`/pp list` marks installs, updates, and removals that require a restart with `Restart to apply`.
For updates, it also shows the staged version.
A removed plugin stays in the list while the server or proxy still has it loaded.
`/pp info` reports how many changes apply after a restart.
`/pp reload` does not load JARs.

Plugin Portal selects versions for the server software and Minecraft version.
If the marketplace has release or stable builds, an install without a channel prefers those builds.
The channel on `/pp install` is a positional argument.
The channel on `/pp update` is a flag.

```text
/pp install P1OZGk5p MODRINTH --byId
/pp install "Plugin Name" MODRINTH beta
/pp update "Plugin Name" --channel beta --refresh
/pp update "Plugin Name" --version "1.2.3"
```

Plugin Portal saves the selected channel for future updates.
An exact version must still be compatible.
If a version label exists on more than one channel, specify the channel too.
`updateAll` excludes exact versions that you select through a command.
The dashboard also excludes exact installs from automatic updates.
To permit bulk updates again, use `/pp blacklist <name>` to remove that exclusion.

The dashboard's **Install anyway** option bypasses Minecraft-version metadata for an exact release.
It still requires a JAR for the correct server platform.
The review step records this choice. The choice does not change future compatibility checks.

`--refresh` bypasses the local two-hour marketplace cache.
It does not force the API scanner to get new upstream data.
`--ignoreOutdated` permits a reinstall even when the selected compatible version is current.
It does not bypass compatibility.

`updateAll` uses exclusions and each plugin's saved channel.
It reports excluded plugins, unavailable compatible versions, lookup failures, and download failures.
One failed check does not stop checks for the other plugins.
It does not update external plugins. Use `/pp external updateAll` for those plugins.

A blacklist entry only affects marketplace bulk updates.
A manual update for one plugin still works. Plugin Portal refuses to uninstall itself.

## Recognize installed JARs

These commands require `pluginportal.manage.recognize`.

```text
/pp recognize "Example.jar" [--channel <name>]
/pp recognizeAll [--channel <name>]
```

Recognition identifies files in `plugins/` by their hashes or supported Polymart metadata.
A partial filename must match one JAR.
Plugin Portal tracks recognized files and can rename them to its managed filename format.
Recognition skips Plugin Portal, tracked files, and configured external files.
`--channel` sets the future update channel for files that Plugin Portal recognizes by hash.

An unrecognized file does not prove that a plugin is defective.
The marketplace or API possibly does not have its hash.
If necessary, install the correct marketplace entry through Plugin Portal.
Before you replace manually installed files, make a backup of your server.

## External plugins

External sources require `pluginportal.manage.external`.
They use `external-plugins.yml`, separate from the marketplace records.
Plugin Portal supports GitHub Releases and GeyserMC. It does not support Jenkins.
See [external source configuration](docs/adapters.md) for complete examples.

| Command | Purpose |
| --- | --- |
| `/pp external add github <id> <owner> <repo> <asset> [--prereleases]` | Adds a GitHub source. The asset selector can be a name fragment or a regular expression. |
| `/pp external add geysermc <id> <project> <artifact>` | Adds a GeyserMC source. Use a server-compatible artifact, such as `spigot`. |
| `/pp external import github <id> <owner> <repo> <asset> <file> [--prereleases]` | Tracks an installed JAR from GitHub. |
| `/pp external import geysermc <id> <project> <artifact> <file>` | Tracks an installed JAR from GeyserMC. |
| `/pp external check <id>` | Checks the selected artifact without a download. |
| `/pp external install <id>` | Installs a configured entry. |
| `/pp external update <id>` | Stages an installed entry's update. |
| `/pp external uninstall <id>` | Removes the JAR and staged update. Keeps the configuration. |
| `/pp external invalidate <id>` | Makes the next eligible update download the artifact again. |
| `/pp external updateAll` | Updates installed entries with `manual` or `auto` policies. |
| `/pp external reload` | Reloads the external configuration. |

The `manual` policy requires an explicit update command.
The `auto` policy also checks and updates at startup.
The `disabled` policy prevents updates.

Use unique filenames. Plugin Portal rejects ambiguous asset matches and checks SHA-256 digests from the provider.
It also supports GitHub assets without a digest.
Do not add new entries to the old `adapters.yml` file.

## Legacy API key support

Use `/pp key set <key>` to validate and save a key.
Access refreshes in the same session. A restart is not normally necessary.
Use `/pp info` to check the result.
Key commands require `pluginportal.manage`.

| Command | Purpose |
| --- | --- |
| `/pp key` | Shows key instructions. |
| `/pp key set <key>` | Validates and saves a key. |
| `/pp key get` | Shows masked key status. In chat, the copy action contains the full key. |
| `/pp key clear` | Clears configured authentication and refreshes access. |

At startup, Plugin Portal checks `Authentication.ApiKey` in `config.yml`, then `pluginportal.txt`, then `mclicense.txt` on disk.
These files are in `plugins/PluginPortal/`.
In v4, Plugin Portal ignores embedded JAR credentials and marketplace placeholders.
Keys never enroll a server or grant remote control.
See [marketplace license import](docs/marketplace-license-import.md).

Do not post a key, editor link, or token in a public issue.
Support only needs the license state from `/pp info`.

## Temporary web editor

Editor commands require `pluginportal.manage.editor`.

| Command | Purpose |
| --- | --- |
| `/pp editor [--isConsole]` | Creates a temporary editor session and shows its link. |
| `/pp editor status` | Shows the connection and session status. |
| `/pp editor url [--isConsole]` | Shows the active session link. |
| `/pp editor reconnect` | Reconnects the previous session. |
| `/pp editor stop` | Stops the session. |

`--isConsole` prints a URL that you can copy.
Protect the link. It is a credential.
If the session expires or the editor reports no connected plugin, run `/pp editor` again.
Plugin Portal does not support `/pp connect`.

## Import, export, and diagnostics

| Command | Permission | Purpose |
| --- | --- | --- |
| `/pp export` | `pluginportal.manage.export` | Uploads tracked marketplace IDs to MCLogs. Is a free local tool. |
| `/pp import <mclogs-url>` | `pluginportal.manage.import` | Installs the exported marketplace plugins. Is a free local tool. |
| `/pp scan <file>` | `pluginportal.manage.scan` | Runs the bundled Hangar JAR scanner locally. Is a free local tool. |
| `/pp dump` | `pluginportal.dump` | Uploads a support dump to MCLogs. |
| `/pp support <code>` | `pluginportal.admin` | Uploads a diagnostic bundle with the 8-digit code from support. The bundle expires in 24 hours. |
| `/pp reload` | `pluginportal.manage.config` | Reloads the configuration and local plugin records. |

`/pp config refresh` and `/pp config reload` are aliases for `/pp reload`.

Export is a list of marketplace IDs.
It is not a backup of exact versions, configuration, plugin data, or external sources.
Make a separate backup of those files.
Import accepts an MCLogs URL and skips plugins that are already installed.

Review the scanner results. An alert does not prove that a JAR is malicious.
A result without an alert does not prove that a JAR is safe.

For a bug report, include the Plugin Portal version, server software, and Minecraft version.
Include the exact command, exact error, and steps to reproduce it.
Before you share a support dump link, review the dump.

## Configuration and files

| Path in `plugins/PluginPortal/` | Purpose |
| --- | --- |
| `config.yml` | Stores authentication, feature flags, disabled platforms, telemetry, and webhook options. |
| `plugins.json` | Records tracked marketplace plugins, versions, channels, and exclusions from bulk updates. |
| `external-plugins.yml` | Defines external sources and update policies. |
| `external-plugins-state.json` | Records installed and staged external versions, hashes, and check results. |

While an operation is in progress, do not edit the plugin records.
Before manual changes, make a backup of the files.
Use commands for normal maintenance.

`EnabledFeatures` contains `INSTALL`, `UPDATE`, `DELETE`, `LIST`, `RECOGNISE`, `IMPORT`, `EXPORT`, and `AUTOMATICALLY_UPDATE_PPP`.
A disabled feature blocks its related commands.
`AUTOMATICALLY_UPDATE_PPP` controls updates for Plugin Portal itself.
It does not schedule updates for all installed marketplace plugins.

- `DownloadPlatforms.Disabled` lists marketplace names to exclude from downloads.
- `Telemetry.Enabled` controls telemetry.
- `DiscordWebhook.Url` sets the notification destination. Keep it private.
- `Authentication.ApiKey` stores the configured key.

After you change supported configuration values, run `/pp reload`.
Use the key commands to change authentication.
Restart the server to apply JAR changes.

## Problems and solutions

| Problem | Action |
| --- | --- |
| Plugin Portal does not track a plugin | Run recognition, or install the plugin through Plugin Portal. Check `/pp list --all`. |
| A new upstream release is absent | Try `/pp update <name> --refresh`. If the release is still absent, report the marketplace URL and version. API catalog scans can have delays. |
| No compatible version is available | Check the Minecraft version, server type, and selected channel. Do not put a client mod or datapack in the plugin folder. |
| The download succeeded but the old code still runs | Restart the server to apply the staged update. |
| The API denies network access | Check the account purchase, node role, and enrollment. Do not share the node credential or enrollment code. |
| The editor session is absent | Run `/pp editor` again. |
| The external asset is ambiguous | Change the asset selector to match one JAR. |
| A command is disabled | Check `EnabledFeatures`. Run `/pp reload`. |

## Paid network management

1. Open the dashboard with your account.
2. Link your MC License key under Purchases, or use your verified purchase.
3. Choose **Add server**.
4. Enter its name and platform.
5. Run the generated `/pp link <code>` command in its console.
6. Review the account, network, and role.
7. Run `/pp link confirm` to approve the link, or `/pp link cancel` to cancel it.

Networks are optional. The dashboard creates the first network automatically.
API keys do not enroll servers.
The platform determines the role. A Velocity proxy uses `controller` and can send plugin changes to servers that allow them.
A Paper server uses `node` and reports its plugins.

Enrollment and unlink require the console.
Network commands require `pluginportal.network` or the platform admin permission.
The proxy requires a current paid owner entitlement to list the network and send plugin changes.

Commands without a target flag remain local.
Remote commands require the action permission, `pluginportal.network`, and a proxy enrolled in the same network.
Use exact server names or UUIDs.
If names are duplicates, use UUIDs. Always specify the targets.

| Command | Purpose |
| --- | --- |
| `/pp link` | Shows the account, network, node ID, role, connection, last error, approval for plugin changes, and restart count. |
| `/pp link <code>` | Shows the account, network, and role for review in this console. |
| `/pp link confirm` | Uses the reviewed code and saves the node credential. |
| `/pp link cancel` | Cancels the link review without enrollment. |
| `/pp unlink` | Removes the local credential and clears approval for plugin changes. Requires the console. |
| `/pp servers [--page <number>] [--full]` | Lists enrolled servers and proxies with their connection status. Requires a proxy. |
| `/pp refresh --servers lobby,proxy` | Refreshes inventories on explicit targets. Requires a proxy. Accepts `--server` as an alias. |
| `/pp history [operationId] [--page <number>] [--full]` | Shows operations and results for each target. Requires a proxy. |
| `/pp list --server lobby` | Shows a remote inventory. |
| `/pp install LuckPerms MODRINTH --servers lobby,proxy` | Installs a plugin on explicit targets. |
| `/pp update LuckPerms --server lobby` | Updates one tracked remote plugin. |
| `/pp uninstall LuckPerms --server lobby` | Removes a tracked remote plugin. Keeps its data. |
| `/pp control [enable\|disable\|status]` | Approves or disables plugin changes from the dashboard and proxy on this target. Requires admin permission and the console. |

On Velocity, use `/ppv`. The server's `/pp` command remains available.
Remote `list` supports `--page`, `--full`, `--untracked`, `--all`, and `--detailed`.
Remote commands do not support `--outdated`, `--external`, `update --refresh`, or `update --ignoreOutdated`.
Exact versions and release channels use the same install/update arguments as local commands.

The dashboard starts in read-only mode. It shows each linked server's status and plugin inventory.
Each target separately approves plugin changes from the dashboard and proxy through `RemoteControl.AllowWrites`.
The default is `false`. One approval covers both sources of plugin changes.

On a server, run `pp control enable` in the console.
On a proxy, run `ppv control enable` in the console.
The browser and editor cannot enable this option.
The API and JAR both enforce it. Use `disable` to remove approval.
Proxy command permissions still apply.

The old `dashboard` command is a compatibility alias for `control`.
Normal restarts retain approval. Unlink and re-enrollment clear approval.
Plugin Portal removes the old `Dashboard` section and clears its approval.

Exclude `network-node.json` from server templates and shared backups. It is a credential, not configuration.
Plugin Portal keeps unconfirmed enrollment reviews in memory only.

Network lists show eight entries per chat page.
Hover for full IDs and result messages.
Click **Copy ID** to copy a full UUID.
Console output includes full IDs and messages.
Use `--page` or `--full` as with local lists.

For `--servers`, use server names or UUIDs with commas between them.
Quote names with spaces. `refresh` always requires explicit targets.
For example, on Velocity:

```text
ppv refresh --servers lobby,proxy
ppv install Vebnzrzj MODRINTH --byId --servers lobby,proxy
ppv history
```

Run `pp link` or `ppv link` to check the local link status without an API request.
If an older identity has no saved network name, the command shows the network UUID.
If no link exists, the command directs you to Add server in the dashboard for a code.

The dashboard also supports release channels, enrollment, node revocation, inventory, operation history, and review of retry targets.
Each retry creates a new operation. Before a retry, inspect unknown results on disk.
The network skips offline nodes. It never queues work for a future connection or automatically retries plugin changes.

Network operations support catalog inventory, install, update, and uninstall.
Manage Plugin Portal itself with the local upgrade command.
Remote operations do not support arbitrary URLs, console commands, restarts, or configuration changes.
Local feature switches and blacklists still apply.

Restart each affected server or proxy to apply JAR changes.
Velocity applies staged replacements at graceful shutdown.
After a crash, the replacements stay staged until the next successful graceful shutdown.

See the [network architecture and operation guide](docs/network-mvp.md).
