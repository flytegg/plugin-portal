# Plugin Portal commands

Use `/pp`, `/pluginportal`, or `/ppm`. In the server console, omit `/`.
Use `/pp help <command>` for instructions, for example `/pp help update`.
Quote names, filenames, or version labels that contain spaces.

Plugin Portal uses one JAR. All local management is free. Network management
requires an active account entitlement checked by the hosted API. A permission
does not grant network access. On Velocity, replace `/pp` with `/ppv`.
`/ppnetwork` is an alias for `/ppv network`.
Help, hover hints, and command buttons keep the alias you entered. For example,
`/ppnetwork help` suggests `/ppnetwork list`, while `/ppv network help` suggests
`/ppv network list`.
Missing or invalid arguments show help for that command. For example,
`/ppv platform` shows platform usage and an example.

Command discovery and help require `pluginportal.view`. Grant it alongside the
specific action permissions for limited roles. Players without it cannot see
or complete the command root. On Bukkit, operators and `pluginportal.admin`
receive local command access by default. Velocity uses its permission provider;
give proxy administrators `pluginportal.admin`. Network commands still require
`pluginportal.network` and a hosted entitlement.

## Start here

1. Put `PluginPortal-<version>.jar` in `plugins/`.
2. Start the server. Run `/pp info` to check the version, platform, and network connection.
3. Run `/pp search ViaVersion` to find a plugin.
4. Run `/pp install ViaVersion MODRINTH` to download it.
5. Restart the server to load the new JAR.

Plugin Portal supports marketplace plugins from Modrinth, Hangar, SpigotMC,
and free Polymart listings. A marketplace entry must provide a downloadable
JAR. Paid Polymart downloads are not supported.

Plugin Portal manages Bukkit-compatible plugins and Velocity proxy plugins.
BungeeCord, datapacks, and paid Polymart downloads are outside this release.

## Find and list plugins

| Command | What it does |
| --- | --- |
| `/pp help [page or command]` | Show help pages 1 to 3, or help for a command. |
| `/pp search <query> [platform] [--page <number>] [--full]` | Search the marketplace catalog. |
| `/pp view <name> [platform] [--byId] [--exact]` | Show marketplace details and available actions. |
| `/pp list [--all] [--untracked] [--outdated] [--external] [--detailed] [--page <number>] [--full]` | List local plugins and configured external plugins. |
| `/pp info` | Show the exact version, platform, free local tools, and network connection. Alias: `/pp version`. |

These commands require `pluginportal.view`.

Lists and search results show eight entries per page in chat. Use the page
buttons or `--page 2` to continue. Use `--full` to show every entry. The console
shows every entry unless you request a page. Page numbers start at 1. Do not
combine `--page` and `--full`.

List options:

- `--untracked` shows only JARs that Plugin Portal does not manage. It uses local files and does not check remote sources. Do not combine it with `--all`, `--outdated`, or `--external`.
- `--all` includes unrecognized JARs. Do not combine it with `--outdated` or `--external`.
- `--outdated` shows available marketplace and external updates. Failed checks appear separately.
- `--external` shows only configured external plugins.
- `--detailed` includes versions, IDs, and source details. Console output includes these details by default.

Use `MODRINTH`, `HANGAR`, `SPIGOTMC`, or `POLYMART` for `[platform]`.
For `/pp view` and `/pp install`, `--byId` requires a platform. On Modrinth,
use the project ID, such as `P1OZGk5p`, rather than the URL slug.
`--exact` or `-e` matches the full plugin name without case sensitivity.

## Install and update

| Command | Permission | What it does |
| --- | --- | --- |
| `/pp install <name> [platform] [channel] [--byId] [--exact] [--version <version>]` | `pluginportal.manage.install` | Download a compatible plugin JAR. |
| `/pp update <name> [--byId] [--ignoreOutdated] [--refresh] [--channel <name>] [--version <version>]` | `pluginportal.maintain.update` | Update one tracked plugin. |
| `/pp updateAll [--ignoreOutdated]` | `pluginportal.maintain.update` | Update tracked marketplace plugins. |
| `/pp blacklist [name] [--byId]` | `pluginportal.maintain.update` | Toggle exclusion from `updateAll`. With no name, list exclusions. |
| `/pp platform <name> <platform> [--byId]` | `pluginportal.maintain.update` | Download a compatible version from another linked marketplace. |
| `/pp uninstall <name> [--byId]` | `pluginportal.manage.uninstall` | Remove a tracked plugin JAR. Alias: `/pp delete`. |
| `/pp install-url <url>` | `pluginportal.manage.install-url` | Download a JAR from a direct URL. Console only. |
| `/pp upgrade [--yes] [--channel <name>]` | `pluginportal.admin` | Check for a Plugin Portal update. Add `--yes` to download it. |

Restart after an install, update, uninstall, platform change, or self-upgrade.
Plugin Portal stages updates in `plugins/update/`. A staged version can appear
in `/pp list` before the server loads it. `/pp reload` does not load JARs.

Plugin Portal selects versions for the server software and Minecraft version.
When the marketplace has release or stable builds, an install without a channel
prefers those builds. The channel on `/pp install` is a positional argument.
The channel on `/pp update` is a flag.

```text
/pp install P1OZGk5p MODRINTH --byId
/pp install "Plugin Name" MODRINTH beta
/pp update "Plugin Name" --channel beta --refresh
/pp update "Plugin Name" --version "1.2.3"
```

A selected channel is saved for future updates. An exact version must still be
compatible. If a version label exists on more than one channel, specify the
channel too. Exact command-selected versions are excluded from `updateAll`.
Use `/pp blacklist <name>` to remove that exclusion when you want bulk updates again.

`--refresh` bypasses the local two-hour marketplace cache. It does not force the
API scanner to fetch new upstream data. `--ignoreOutdated` permits a reinstall
even when the selected compatible version is current. It does not bypass compatibility.

`updateAll` respects exclusions and each plugin's saved channel. It reports
excluded plugins, unavailable compatible versions, lookup failures, and download
failures. One failed check does not stop the other plugins. It does not update
external plugins. Use `/pp external updateAll` for those.

A blacklist entry only affects marketplace bulk updates. A manual single-plugin
update still works. Plugin Portal refuses to uninstall itself.

## Recognize existing JARs

These commands require `pluginportal.manage.recognize`.

```text
/pp recognize "Example.jar" [--channel <name>]
/pp recognizeAll [--channel <name>]
```

Recognition identifies files in `plugins/` by their hashes or supported Polymart
metadata. A partial filename must match one JAR. Recognized files become tracked
plugins and may be renamed to Plugin Portal's managed filename format.
Recognition skips Plugin Portal, tracked files, and configured external files.
`--channel` sets the future update channel for hash-recognized files.

An unrecognized file is not proof of a broken plugin. The marketplace or API may
not have its hash. Install the correct listing through Plugin Portal if needed.
Back up your server before replacing manually installed files.

## External plugins

External sources require `pluginportal.manage.external`.
They use `external-plugins.yml`, separately from marketplace tracking.
GitHub Releases and GeyserMC are supported. Jenkins is not supported.
See [external source setup](docs/adapters.md) for complete examples.

| Command | What it does |
| --- | --- |
| `/pp external add github <id> <owner> <repo> <asset> [--prereleases]` | Add a GitHub source. The asset selector can be a name fragment or a regular expression. |
| `/pp external add geysermc <id> <project> <artifact>` | Add a GeyserMC source. Use a server-compatible artifact, such as `spigot`. |
| `/pp external import github <id> <owner> <repo> <asset> <file> [--prereleases]` | Track an existing JAR from GitHub. |
| `/pp external import geysermc <id> <project> <artifact> <file>` | Track an existing JAR from GeyserMC. |
| `/pp external check <id>` | Check the matching artifact without downloading it. |
| `/pp external install <id>` | Install a configured entry. |
| `/pp external update <id>` | Stage an installed entry's update. |
| `/pp external uninstall <id>` | Remove the JAR and staged update. Keep the configuration. |
| `/pp external invalidate <id>` | Make the next eligible update download the artifact again. |
| `/pp external updateAll` | Update installed entries with `manual` or `auto` policies. |
| `/pp external reload` | Reload the external configuration. |

The `manual` policy requires an explicit update command. The `auto` policy also
checks and updates at startup. The `disabled` policy prevents updates.
Use unique filenames. Plugin Portal rejects ambiguous asset matches and checks
provider-supplied SHA-256 digests. GitHub assets without a digest remain supported.
Do not add new entries to the old `adapters.yml` file.

## Legacy API key support

Use `/pp key set <key>` to validate and save a key. Access refreshes in the same
session. A restart is not normally required. Use `/pp info` to check the result.
Key commands require `pluginportal.manage`.

| Command | What it does |
| --- | --- |
| `/pp key` | Show key instructions. |
| `/pp key set <key>` | Validate and save a key. |
| `/pp key get` | Show masked key status. In chat, the copy action contains the full key. |
| `/pp key clear` | Clear configured authentication and refresh access. |

At startup, Plugin Portal checks `Authentication.ApiKey` in `config.yml`, then
`pluginportal.txt`, then `mclicense.txt`, then supported embedded or marketplace
license data. These files are in `plugins/PluginPortal/`. A saved key takes
priority over marketplace delivery. See [marketplace license import](docs/marketplace-license-import.md).

Never post a key, editor link, or token in a public issue. Support only needs the
license state from `/pp info`, not the key itself.

## Temporary web editor

Editor commands require `pluginportal.manage.editor`.

| Command | What it does |
| --- | --- |
| `/pp editor [--isConsole]` | Create a temporary editor session and show its link. |
| `/pp editor status` | Show connection and session status. |
| `/pp editor url [--isConsole]` | Show the active session link. |
| `/pp editor reconnect` | Reconnect the previous session. |
| `/pp editor stop` | Stop the session. |

`--isConsole` prints a URL for copying. Treat the link as a credential. If the
session expires or the editor reports no connected plugin, run `/pp editor`
again. `/pp connect` is not supported.

## Import, export, and diagnostics

| Command | Permission | What it does |
| --- | --- | --- |
| `/pp export` | `pluginportal.manage.export` | Upload tracked marketplace IDs to MCLogs. Free local tool. |
| `/pp import <mclogs-url>` | `pluginportal.manage.import` | Install the exported marketplace plugins. Free local tool. |
| `/pp scan <file>` | `pluginportal.manage.scan` | Run the bundled Hangar JAR scanner locally. Free local tool. |
| `/pp dump` | `pluginportal.dump` | Upload a support dump to MCLogs. |
| `/pp support <code>` | `pluginportal.admin` | Upload a diagnostic bundle with the 8-digit code supplied by support. The bundle expires in 24 hours. |
| `/pp reload` | `pluginportal.manage.config` | Reload configuration and local tracking files. |

`/pp config refresh` and `/pp config reload` are aliases for `/pp reload`.

Export is a list of marketplace IDs. It is not a backup of exact versions,
configuration, plugin data, or external sources. Back up those files separately.
Import accepts an MCLogs URL and skips already installed plugins.

Scanner findings need review. A warning does not prove that a JAR is malicious,
and a clean result does not prove that a JAR is safe.

For a bug report, include the Plugin Portal version, server software and Minecraft
version, exact command, exact error, and steps to reproduce it. Review a support
dump before sharing its link.

## Configuration and files

| Path in `plugins/PluginPortal/` | Purpose |
| --- | --- |
| `config.yml` | Authentication, feature flags, disabled platforms, telemetry, and webhook settings. |
| `plugins.json` | Tracked marketplace plugins, versions, channels, and bulk-update exclusions. |
| `external-plugins.yml` | External source definitions and update policies. |
| `external-plugins-state.json` | Installed/staged external versions, hashes, and check results. |

Do not edit tracking files while an operation is in progress. Back up files before
manual changes. Prefer commands for normal maintenance.

`EnabledFeatures` contains `INSTALL`, `UPDATE`, `DELETE`, `LIST`, `RECOGNISE`,
`IMPORT`, `EXPORT`, and `AUTOMATICALLY_UPDATE_PPP`. A disabled feature blocks its
associated commands. `AUTOMATICALLY_UPDATE_PPP` concerns Plugin Portal itself.
It is not a scheduler for all installed marketplace plugins.

- `DownloadPlatforms.Disabled` lists marketplace names to exclude from downloads.
- `Telemetry.Enabled` controls telemetry.
- `DiscordWebhook.Url` sets the notification destination. Keep it private.
- `Authentication.ApiKey` stores the configured key.

Run `/pp reload` after changing supported configuration values. Use the key
commands to change authentication. Restart the server to apply JAR changes.

## Troubleshooting

| Problem | Action |
| --- | --- |
| Plugin is not tracked | Run recognition, or install it through Plugin Portal. Check `/pp list --all`. |
| New upstream release is missing | Try `/pp update <name> --refresh`. If it is still missing, report the marketplace URL and version. API catalog scans can lag. |
| No compatible version | Check the Minecraft version, server type, and selected channel. Do not force a client mod or datapack into the plugin folder. |
| Download succeeded but old code still runs | Restart the server to apply the staged update. |
| Network access is denied | Check the account purchase, node role, and enrollment. Never share the node credential or enrollment code. |
| Editor session is missing | Run `/pp editor` again. |
| External asset is ambiguous | Change the asset selector to match one JAR. |
| Command is disabled | Check `EnabledFeatures` and run `/pp reload`. |

## Paid network management

Sign in to the dashboard. Link your MC License key under Purchases, or use your
existing verified purchase. Choose **Add server**, enter its name and platform,
and run the generated `/pp link <code>` command in its console. Groups are optional;
the first is created automatically. API keys do not enroll servers.
Choose `node` for a backend and `controller` for a proxy that will initiate operations.
Enrollment and leaving are console-only. All network commands require
`pluginportal.network` (or the platform admin permission). Controllers require a
current paid owner entitlement for listing and operating on the network.

Use the shorter commands for normal work. Commands without a target flag remain
local. Remote commands require the action permission, `pluginportal.network`, and
a controller enrolled in the same group. Use exact server names or UUIDs; duplicate
names require UUIDs. Never infer an all-server target.

| Command | Purpose |
| --- | --- |
| `/pp link <code>` | Link this server from its console. |
| `/pp unlink` | Remove the local credential and clear dashboard approval. Console only. |
| `/pp servers [--page <number>] [--full]` | List connected servers. Controller only. |
| `/pp history [operationId] [--page <number>] [--full]` | Inspect operations and per-server outcomes. Controller only. |
| `/pp list --server lobby` | View a remote inventory. |
| `/pp install LuckPerms MODRINTH --servers lobby,proxy` | Install on explicit targets. |
| `/pp update LuckPerms --server lobby` | Update one tracked remote plugin. |
| `/pp uninstall LuckPerms --server lobby` | Remove a tracked remote plugin; retain its data. |
| `/pp dashboard [enable\|disable\|status]` | Approve or disable dashboard writes on this server. Admin and console only. |

On Velocity use `/ppv`; backend `/pp` still works. Remote `list` supports `--page`,
`--full`, `--untracked`, `--all`, and `--detailed`. Remote `--outdated`, `--external`,
`update --refresh`, and `update --ignoreOutdated` are unsupported. Exact versions
and release channels use the same install/update arguments as local commands.

The dashboard starts read-only, showing every linked server's status and plugin
inventory. Each server independently approves dashboard plugin changes through
`Dashboard.AllowWrites` (default `false`). Run `pp dashboard enable` in a backend
console or `ppv dashboard enable` in the proxy console. The browser cannot enable
this setting. The API and JAR both enforce it. Use `disable` to revoke approval.
Proxy command permissions are separate from dashboard approval. Each dashboard
change requires explicit targets and a review before submission.

### Compatibility commands

Existing `/pp network`, `/ppv network`, and `/ppnetwork` commands remain available:

| Command | Purpose |
| --- | --- |
| `/pp network [help]` | Show network actions and usage hints. |
| `/pp network enroll <code>` | Consume a one-use code and save a private node credential. |
| `/pp network leave` | Delete the local credential; revoke the old node in the dashboard too. |
| `/pp network status` | Show local connection state, node ID, and role. |
| `/pp network list [--page <number>] [--full]` | Show nodes and online state with copyable IDs. Controller only. |
| `/pp network operations [--page <number>] [--full]` | Show recent operation summaries and clickable results. Controller only. |
| `/pp network operation <operationId> [--page <number>] [--full]` | Show outcomes for each target. Controller only. |
| `/pp network refresh <nodeIds>` | Refresh inventories on explicit targets. |
| `/pp network install <nodeIds> <platform> <pluginId> [version]` | Install compatible catalog artifacts. |
| `/pp network update <nodeIds> <platform> <pluginId> [version]` | Stage compatible updates; respect each node's blacklist. |
| `/pp network uninstall <nodeIds> <platform> <pluginId>` | Remove managed JARs; retain data folders. |

Network lists show eight entries per chat page. Hover for full IDs and result
messages; click **Copy ID** to copy a full UUID. Console output includes full IDs
and messages. Use `--page` or `--full` as with local lists.

Use comma-separated UUIDs without spaces for `nodeIds`. On Velocity use
`/ppv network` or `/ppnetwork`. Example:

```text
ppnetwork install <backend-uuid>,<proxy-uuid> MODRINTH Vebnzrzj
ppnetwork operations
```

The dashboard also supports release-channel selection, enrollment, node revocation,
inventory, operation history, and reviewing retry targets. Each retry creates a new
operation. Inspect unknown outcomes on disk before retrying. Offline nodes are
skipped and work is never queued for a future connection.

Network operations support catalog inventory, install, update, and uninstall.
Arbitrary URLs, console commands, restarts, and configuration changes are not remote
operations. Local feature switches and blacklists still apply. Restart each node
to apply JAR changes. Velocity applies pending replacements at graceful shutdown;
a crash leaves them staged until the next successful graceful shutdown.

See [network architecture and operating guide](docs/network-mvp.md).
