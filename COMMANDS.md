# Plugin Portal Support Memory

Concise support-agent memory for Plugin Portal commands, dashboard behavior, update flow, auth, and common troubleshooting.

## Basics

- Main aliases: `/pp`, `/pluginportal`, `/ppm`
- Help aliases: `/pp`, `/pp help`, `/pluginportal`, `/ppm`, `/ppm help`
- Plugin folder: `plugins/`
- Plugin Portal data folder: `plugins/PluginPortal/`
- Tracked local plugin cache: `plugins/PluginPortal/plugins.json`
- Bukkit/Paper update folder: `plugins/update/`
- Plugin Portal API used by plugin code: `https://v3.pluginportal.link`
- Web dashboard/editor URL: `https://pluginportal.link/rooms/<room-or-server-id>`
- Most install/update/delete/list commands can be disabled in `config.yml` under `EnabledFeatures`.
- Premium-only commands require a valid configured Plugin Portal key. The plugin now ships as one JAR, and premium access is controlled by runtime entitlement. If auth fails, users see an unauthenticated/support message.
- After installing, updating, deleting, or upgrading plugin jars, tell users to restart the server when prompted.

## What Plugin Portal Does

Plugin Portal is a Minecraft plugin manager for installing, tracking, updating, deleting, recognizing, importing/exporting, and remotely managing plugins through a web dashboard.

Supported marketplace/platform concepts in code:

- Modrinth
- Hangar
- SpigotMC
- Polymart
- Custom direct JAR URLs for `/pp install-url`

Important distinction:

- Plugin Portal only fully manages plugins it has in `plugins.json`.
- Manually installed JARs are not tracked until recognized with `/pp recognize <file>` or `/pp recognizeAll`.
- The dashboard's installed/update views are based on the local tracked cache plus marketplace API enrichment.

## Authentication And Keys

Premium auth key lookup order on startup:

1. `plugins/PluginPortal/config.yml` -> `Authentication.ApiKey`
2. `plugins/PluginPortal/pluginportal.txt`
3. `plugins/PluginPortal/mclicense.txt` legacy
4. Embedded `mclicense.txt` inside the premium JAR

Set/manage key:

| Command | Permission | Purpose |
| --- | --- | --- |
| `/pp key` | `pluginportal.manage` | Show key-management help. |
| `/pp key set <key>` | `pluginportal.manage` | Validate and save an API/license key. |
| `/pp key get` | `pluginportal.manage` | Show current configured key as a clickable copy component. |
| `/pp key clear` | `pluginportal.manage` | Remove saved authentication key. |

Support notes:

- There is no `/pp set-key <key>` command in code. Use `/pp key set <key>`.
- Current code validates and saves `/pp key set <key>`, but may not refresh premium command auth until restart. If premium commands still say unauthenticated after setting a valid key, restart the server/plugin.
- Valid key types can include Plugin Portal API keys beginning with `pp_` or `pps_`, legacy MCLicense keys, and Polymart license keys.

## Dashboard And Editor

The dashboard/editor is a web UI connected to the running Minecraft plugin over websocket.

How users connect:

- Permanent dashboard connection: planned for `/pp connect`, but not supported in the current build.
- Temporary editor session: `/pp editor`
- If the dashboard says "No Plugin Connected", run `/pp editor` again from the Minecraft server.

Dashboard tabs/features:

- `Browse Plugins`: search marketplace plugins. Search starts after at least 2 characters.
- `Installed`: shows tracked local plugins from `plugins.json`, enriched with marketplace data.
- `Updates Available`: shows tracked plugins where local version differs from latest marketplace version.
- Settings modal: toggles Plugin Portal feature flags stored in `config.yml`.

Dashboard plugin actions:

- Install plugin from marketplace.
- Uninstall tracked plugin.
- Update tracked plugin.
- Choose platform when a plugin exists on multiple marketplaces.
- Choose release channel and version in the install dialog UI.
- View compatibility/version metadata when available.

Dashboard connection details:

- The web app connects to `/ws/temp/<room>?token=<otp>&role=dashboard` for temporary rooms, or `/ws/servers/<serverId>` for permanent rooms.
- It fetches local plugins via websocket message `query.localPlugins`.
- It fetches settings via `query.getSettings`.
- It mutates plugins via `mutation.installPlugin`, `mutation.uninstallPlugin`, and `mutation.updatePlugin`.
- It mutates settings via `mutation.updateSetting`.

Dashboard limitations/gotchas:

- The install dialog can pass selected `version` and `channel` values through the websocket mutation to the plugin backend.
- Premium Polymart plugin installs are currently not supported by the plugin download adapter. Free Polymart downloads may work.
- If the room is missing/expired, the web UI tells the user to create a new session.

## How Installing Works

Install from command:

1. User runs `/pp install <name> [platform] [channel] [--byId] [--exact] [--version <version>]`.
2. Plugin searches marketplace cache/API.
3. If `--byId` is used, `platform` is required and `<name>` is treated as the marketplace/platform ID.
4. If `--exact` or `-e` is used, name search is filtered to exact case-insensitive plugin-name matches.
5. If `--version <version>` is used, Plugin Portal selects that exact compatible version. If the version label exists on multiple channels, rerun with a channel.
6. If one match exists, it downloads the plugin JAR into `plugins/`.
7. It writes a tracked `LocalPlugin` entry to `plugins/PluginPortal/plugins.json`.
8. User usually needs to restart the server for Bukkit/Paper to load the newly installed plugin.

Install command arguments:

| Argument/flag | Required | Meaning | Notes |
| --- | --- | --- | --- |
| `<name>` | Yes | Marketplace plugin search term. | Quote names with spaces. With `--byId`, this is the marketplace/platform ID. |
| `[platform]` | No | Marketplace enum: `MODRINTH`, `HANGAR`, `SPIGOTMC`, or `POLYMART`. | Required with `--byId`; otherwise narrows search/download to one platform. |
| `[channel]` | No | Marketplace release-channel name. | Positional argument, not a flag. Examples include `release`, `stable`, `beta`, `alpha`, or any custom channel returned by the marketplace. Saved for future updates unless an exact version is selected. |
| `--byId` | No | Treat `<name>` as a marketplace/platform ID. | Requires `[platform]` so Plugin Portal knows which marketplace ID namespace to use. For Modrinth, use the base62 project ID/project_id, not the slug from the URL. |
| `--exact`, `-e` | No | Require exact case-insensitive plugin-name matches. | This is still name search; it does not turn `<name>` into an ID or slug lookup. |
| `--version <version>` | No | Install an exact compatible version. | Exact command-selected installs are marked excluded from `updateAll`. Add `[channel]` when the same version label exists on multiple channels. |

Install from dashboard:

1. Dashboard sends `mutation.installPlugin` with `{ platform, id }` plus optional `{ version, channel }`.
2. Plugin fetches marketplace plugin by platform/id.
3. Plugin downloads through the download adapter system.
4. Local cache is updated and dashboard refetches local plugins.

Download adapters:

- Polymart adapter has highest priority.
- Standard marketplace adapter handles non-Polymart marketplace plugins.
- Custom URL adapter handles direct JAR URLs.

Install failure patterns:

- Already installed: use update instead.
- No download URL: marketplace entry likely uses an external/download page Plugin Portal cannot resolve.
- Premium Polymart plugin: currently unsupported.
- Network/API failure: retry and collect `/pp dump` if persistent.

## How Updating Works

Single plugin update:

1. `/pp update <name> [--byId] [--ignoreOutdated] [--channel <name>] [--version <version>]` finds a tracked local plugin in `plugins.json`.
2. If `--channel <name>` is used, Plugin Portal saves that channel before updating.
3. If `--version <version>` is used, Plugin Portal selects that exact compatible version and marks the plugin excluded from `updateAll`.
4. Without `--version`, it compares local version to marketplace latest version.
5. If already current, it exits unless `--ignoreOutdated` is used.
6. It downloads the new JAR into `plugins/update/`.
7. It removes the old local cache entry, adds the new one, and saves `plugins.json`.
8. Restart is needed for Bukkit/Paper to apply the update from `plugins/update/`.

Bulk update:

1. `/pp updateAll` fetches marketplace data for all tracked platform IDs.
2. It builds a list where local version differs from latest marketplace version.
3. It skips plugins excluded from auto-updates.
4. It updates each plugin one by one with a short delay.
5. It reports success/failure counts.

Dashboard update:

1. Dashboard computes updates by comparing tracked local version to marketplace latest version.
2. User clicks Update.
3. Dashboard sends `mutation.updatePlugin`.
4. Plugin runs the same local update flow for that tracked plugin.

Plugin Portal itself:

- `/pp info` checks whether Plugin Portal has an update.
- `/pp upgrade` checks/downloads a Plugin Portal update.
- `/pp upgrade --yes` performs the upgrade.
- Downloaded Plugin Portal updates go through internal adapter logic and require restart.
- `EnabledFeatures.AUTOMATICALLY_UPDATE_PPP` controls automatic Plugin Portal self-update behavior where used.

Update failure patterns:

- "Plugin not installed": plugin is not tracked in `plugins.json`; use `/pp recognize` or reinstall through Plugin Portal.
- "Could not find plugin in marketplace": cached platform id may be stale, plugin removed, API unavailable, or marketplace data missing.
- "Plugin is already up to date": expected unless forcing with `--ignoreOutdated`.
- Download succeeds but running plugin is still old: user has not restarted server.

## How Uninstalling Works

Uninstall/delete:

1. `/pp delete <name>` or dashboard uninstall finds a tracked local plugin.
2. Plugin Portal refuses to delete Plugin Portal itself.
3. It finds the plugin JAR by SHA-256 in `plugins/` or `plugins/update/`.
4. It removes the tracked entry from `plugins.json`.
5. It deletes the JAR if found.
6. Restart is needed for Bukkit/Paper to unload the plugin.

If the JAR is already gone, Plugin Portal removes the cache entry but reports it could not find the JAR to delete.

## Recognition, Import, And Export

Recognition is for manually installed plugins.

`/pp recognize <file>`:

- Looks for the JAR in `plugins/`.
- Allows a single partial filename match.
- Skips Plugin Portal itself.
- Hashes SHA-256 and SHA-512.
- Checks whether the hash is already tracked.
- Reads `polymart.yml` from the JAR if present and handles Polymart metadata locally.
- Otherwise asks the API to recognize by hash.
- Adds a `LocalPlugin` entry to `plugins.json`.
- Renames the JAR to Plugin Portal's downloaded filename format when recognized.

`/pp recognizeAll`:

- Scans all JARs in `plugins/`.
- Skips Plugin Portal itself and already tracked hashes.
- Handles Polymart metadata first.
- Then batch-recognizes remaining JARs by hash.
- Shows recognized and unrecognized results.

Export/import:

- `/pp export` serializes tracked plugin platform IDs and uploads them to MCLogs.
- `/pp import <mclogs-url>` reads that MCLogs export, fetches marketplace details, skips already installed plugins, and installs the rest.
- Import URL must contain `mclo.gs`.

## Common Commands

| Command | Permission | Purpose | Notes |
| --- | --- | --- | --- |
| `/pp help` | `pluginportal.view` | Show command help. | Also works by running `/pp`. |
| `/pp info` | `pluginportal.view` | Show Plugin Portal version, edition, license state, and update info. | Alias: `/pp version`. |
| `/pp version` | `pluginportal.view` | Same as `/pp info`. | Shows Licensed: Yes/No. |
| `/pp list` | `pluginportal.view` | List plugins installed/tracked by Plugin Portal. | Includes update/uninstall buttons in player chat. |
| `/pp dump` | `pluginportal.dump` | Upload logs/support dump to MCLogs and return a support URL. | Use for diagnostics. |
| `/pluginportal config refresh` | `pluginportal.manage.config` | Reload local plugin cache/config state. | Command root is `pluginportal config`, not `/pp config`. |

## Marketplace Plugin Commands

| Command | Permission | Purpose | Notes |
| --- | --- | --- | --- |
| `/pp view <name> [platform] [--byId] [--exact]` | `pluginportal.view` | View marketplace plugin details. | `--exact` also has shorthand `-e`. |
| `/pp install <name> [platform] [channel] [--byId] [--exact] [--version <version>]` | `pluginportal.manage.install` | Install a plugin from marketplace search/API. | `channel` is positional and saved for future updates. `--exact` also has shorthand `-e`. `--version` installs an exact compatible version and excludes it from `updateAll`. |
| `/pp update <name> [--byId] [--ignoreOutdated] [--channel <name>] [--version <version>]` | `pluginportal.maintain.update` | Update one tracked local plugin. | `--channel` changes the saved channel before updating. `--version` installs an exact compatible version and excludes it from `updateAll`. Without `--ignoreOutdated`, skips if already up to date. |
| `/pp blacklist [name] [--byId]` | `pluginportal.maintain.update` | Toggle whether a plugin is skipped by `/pp updateAll`. | With no name, lists blacklisted plugins. Manual `/pp update` still works. |
| `/pp platform <name> <platform> [--byId]` | `pluginportal.maintain.update` | Switch a tracked plugin to another available marketplace platform. | Downloads the latest compatible jar from the target platform. Restart required. |
| `/pp delete <name> [--byId]` | `pluginportal.manage.uninstall` | Delete/uninstall a tracked local plugin. | Alias: `/pp uninstall`. Restart required. |
| `/pp uninstall <name> [--byId]` | `pluginportal.manage.uninstall` | Same as `/pp delete`. | Restart required. |
| `/pp install-url <url>` | `pluginportal.manage.install-url` | Download a plugin JAR directly from a URL. | Console-only command parameter type is used in code. Restart required. |

Argument notes:

- `platform` is a marketplace platform enum, such as Modrinth, Hangar, SpigotMC, or Polymart.
- `channel` on `/pp install` is a positional marketplace release-channel name, such as release, stable, beta, alpha, or any custom channel a marketplace returns.
- `--channel <name>` on `/pp update` is the flag form for changing the saved update channel.
- `--byId` treats the name argument as a marketplace/platform ID. For marketplace search commands, specify `platform` too. For Modrinth, Plugin Portal stores the base62 project ID/project_id, not the vanity slug.
- `--exact` or `-e` forces exact case-insensitive name matching. It is not an ID or slug lookup.
- `--version <version>` selects an exact compatible marketplace version. Exact command-selected install/update versions are skipped by `/pp updateAll`.

## Premium Commands

These require valid Plugin Portal entitlement unless noted. The same JAR contains the command implementations; locked commands stay unavailable until auth succeeds.

| Command | Permission | Purpose | Notes |
| --- | --- | --- | --- |
| `/pp updateAll [--ignoreOutdated]` | `pluginportal.maintain.update` | Bulk update all tracked plugins. | Skips blacklisted plugins and uses each plugin's saved channel. |
| `/pp recognize <file>` | `pluginportal.manage.recognize` | Recognize one untracked plugin JAR in the plugins folder by hash/metadata. | May rename the file to Plugin Portal format. |
| `/pp recognizeAll` | `pluginportal.manage.recognize` | Recognize all untracked plugin JARs in the plugins folder. | Skips Plugin Portal itself and already tracked hashes. |
| `/pp export` | `pluginportal.manage.export` | Export tracked plugin platform IDs to an MCLogs URL. | Used for migration/import. |
| `/pp import <mclogs-url>` | `pluginportal.manage.import` | Import and install plugins from an export URL. | URL must contain `mclo.gs`. |
| `/pp scan <file>` | `pluginportal.manage.scan` | Scan a plugin JAR for Hangar scanner alerts. | Findings are warnings, not automatic proof of malware. |
| `/pp upgrade [--yes]` | `pluginportal.admin` | Check/download Plugin Portal update. | Without `--yes`, shows available updates. Restart required after upgrade. |

## Editor And Dashboard Commands

| Command | Permission | Auth Required | Purpose | Notes |
| --- | --- | --- | --- | --- |
| `/pp connect [--isConsole]` | `pluginportal.manage.editor` | No | Planned permanent dashboard connection. | Not supported in the current build. |
| `/pp connect temp [--isConsole]` | `pluginportal.manage.editor` | Yes | Planned temporary editor/dashboard alias. | Not supported in the current build; use `/pp editor`. |
| `/pp editor [--isConsole]` | `pluginportal.manage.editor` | Yes | Create temporary editor/dashboard link. | Opens `pluginportal.link` room URL. |
| `/pp editor status` | `pluginportal.manage.editor` | Yes | Show current editor websocket status, room, duration, activity, connected users. | Warns near timeout. |
| `/pp editor stop` | `pluginportal.manage.editor` | Yes | Stop active editor session. | |
| `/pp editor reconnect` | `pluginportal.manage.editor` | Yes | Reconnect previous editor session. | Fails if no previous session exists. |
| `/pp editor url [--isConsole]` | `pluginportal.manage.editor` | Yes | Show current editor URL. | Use after an editor session exists. |

`--isConsole` forces raw URL output instead of clickable text, useful for console/support copy-paste.

## Feature Flags

Generated config feature keys:

- `EnabledFeatures.INSTALL`
- `EnabledFeatures.UPDATE`
- `EnabledFeatures.DELETE`
- `EnabledFeatures.LIST`
- `EnabledFeatures.RECOGNISE`
- `EnabledFeatures.IMPORT`
- `EnabledFeatures.EXPORT`
- `EnabledFeatures.AUTOMATICALLY_UPDATE_PPP`

If a feature-backed command says it is disabled, check `plugins/PluginPortal/config.yml` and run `/pluginportal config refresh` or restart if needed.

Dashboard settings can toggle:

- Install
- Update
- Delete
- List
- Recognise
- Import
- Export
- Auto-update Plugin Portal

Polymart settings:

- Dashboard can show whether a Polymart token is configured.
- Dashboard can revoke `Polymart.token`.
- If no token exists, UI says Polymart purchase linking is managed from the Premium dashboard.

## Permissions Summary

- View/help/list/version/view marketplace: `pluginportal.view`
- Install marketplace plugin: `pluginportal.manage.install`
- Install URL: `pluginportal.manage.install-url`
- Update one/all plugins: `pluginportal.maintain.update`
- Delete/uninstall: `pluginportal.manage.uninstall`
- Recognize: `pluginportal.manage.recognize`
- Import: `pluginportal.manage.import`
- Export: `pluginportal.manage.export`
- Scan: `pluginportal.manage.scan`
- Editor/dashboard: `pluginportal.manage.editor`
- Key management: `pluginportal.manage`
- Config refresh: `pluginportal.manage.config`
- Dump support logs: `pluginportal.dump`
- Plugin Portal upgrade: `pluginportal.admin`

## Support Diagnostics

Ask for these first:

- Plugin Portal edition and license state: `/pp info`
- Installed/tracked list: `/pp list`
- Support dump: `/pp dump`
- Current key state: `/pp key get`
- Dashboard/editor status: `/pp editor status`
- Exact command run and exact error message.

Good questions:

- Did they install the plugin manually or through Plugin Portal?
- Did they restart after install/update/delete?
- Is the plugin tracked in `/pp list`?
- Is this free or premium Polymart?
- Is the dashboard temporary room expired?
- Are feature flags disabled in dashboard/settings?

Known command mismatch:

- If logs mention `/pp set-key <key>`, tell the user to use `/pp key set <key>`.

## Common Support Flows

Premium JAR installed but commands say unauthenticated:

1. Ask user to run `/pp info` and check `Edition` plus `Licensed`.
2. If `Licensed: No`, run `/pp key get` to confirm whether a key is configured.
3. If missing/invalid, run `/pp key set <key>`.
4. Restart the server if premium commands still fail after key is saved.

User asks how to open dashboard/editor:

1. Use `/pp editor`.
2. Open the generated editor link.
3. Do not direct users to `/pp connect` yet; it is planned but not supported in the current build.

User has plugins installed manually and wants Plugin Portal to track them:

1. Use `/pp recognize <file>` for one JAR.
2. Use `/pp recognizeAll` for all untracked JARs.
3. Use `/pp list` to confirm tracked plugins.

User wants to migrate tracked plugins to another server:

1. On old server, run `/pp export`.
2. Copy the returned MCLogs URL.
3. On new server, run `/pp import <mclogs-url>`.
