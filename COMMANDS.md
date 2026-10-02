# Plugin Portal commands

Use `/pp`, `/pluginportal`, or `/ppm`. In the server console, omit `/`.
Use `/pp help <command>` for instructions, for example `/pp help update`.
Quote names, filenames, or version labels that contain spaces.

Plugin Portal uses one JAR. Premium commands unlock when the server validates
an eligible key. A command permission does not grant Premium access.

## Start here

1. Put `PluginPortal-<version>.jar` in `plugins/`.
2. Start the server. Run `/pp info` to check the version and license state.
3. Run `/pp search ViaVersion` to find a plugin.
4. Run `/pp install ViaVersion MODRINTH` to download it.
5. Restart the server to load the new JAR.

Plugin Portal supports marketplace plugins from Modrinth, Hangar, SpigotMC,
and free Polymart listings. A marketplace entry must provide a downloadable
JAR. Paid Polymart downloads are not supported.

Plugin Portal manages Bukkit-compatible server plugins. It does not manage
datapacks or install plugins on proxy servers.

## Find and list plugins

| Command | What it does |
| --- | --- |
| `/pp help [page or command]` | Show help page 1 or 2, or help for a command. |
| `/pp search <query> [platform] [--page <number>] [--full]` | Search the marketplace catalog. |
| `/pp view <name> [platform] [--byId] [--exact]` | Show marketplace details and available actions. |
| `/pp list [--all] [--untracked] [--outdated] [--external] [--detailed] [--page <number>] [--full]` | List local plugins and configured external plugins. |
| `/pp info` | Show the Plugin Portal version, license state, and update information. Alias: `/pp version`. |

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
| `/pp updateAll [--ignoreOutdated]` | `pluginportal.maintain.update` | Update tracked marketplace plugins. Requires Premium. |
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

These commands require Premium and `pluginportal.manage.recognize`.

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

External sources require Premium and `pluginportal.manage.external`.
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

## Premium access

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

Editor commands require Premium and `pluginportal.manage.editor`.

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
| `/pp export` | `pluginportal.manage.export` | Upload tracked marketplace IDs to MCLogs. Premium. |
| `/pp import <mclogs-url>` | `pluginportal.manage.import` | Install the exported marketplace plugins. Premium. |
| `/pp scan <file>` | `pluginportal.manage.scan` | Run the bundled Hangar JAR scanner locally. Premium. |
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
| Premium command is locked | Check `/pp info`. Set a valid key. If validation fails, include the error in a support request without sharing the key. |
| Editor session is missing | Run `/pp editor` again. |
| External asset is ambiguous | Change the asset selector to match one JAR. |
| Command is disabled | Check `EnabledFeatures` and run `/pp reload`. |
