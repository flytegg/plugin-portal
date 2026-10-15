# External plugin adapters

External adapters let Plugin Portal manage plugins from sources outside its marketplace catalog.
The providers are GitHub Releases and the GeyserMC download API.

External plugin commands are Premium. They require a valid key and the `pluginportal.manage.external` permission.

## Configuration

At first startup, Plugin Portal creates this file:

```text
plugins/PluginPortal/external-plugins.yml
```

If the generated file contains `plugins: {}`, replace that line with `plugins:`.
Add entries beneath the `plugins` section.
Run `/pp external reload`.

```yaml
plugins:
  viaversion:
    source: github:ViaVersion/ViaVersion
    asset: "^ViaVersion-[0-9].*\\.jar$"
    file: "[PP] viaversion [GITHUB].jar"
    prereleases: false
    updates: manual

  geyser:
    source: geysermc:geyser
    artifact: spigot
    file: "[PP] geyser [GEYSERMC].jar"
    updates: manual
```

Each plugin ID can contain letters, numbers, underscores, and hyphens.
The available fields are:

| Field | Required | Purpose |
| --- | --- | --- |
| `source` | Yes | Identifies the provider and project with `:` between them. Use `github:owner/repository` or `geysermc:project`. |
| `asset` | GitHub only | Matches the complete release asset filename with a regular expression. |
| `artifact` | GeyserMC only | Selects a server-compatible artifact from the GeyserMC API, such as `spigot`. Do not use a proxy artifact on a Bukkit server. |
| `file` | No | Defines a stable filename in the server's `plugins` directory. |
| `prereleases` | No | Controls access to GitHub prereleases. Defaults to `false`. |
| `updates` | No | Selects `manual`, `auto`, or `disabled`. Defaults to `manual`. |

## Filenames

`file` must be one `.jar` filename, not a path.
Filenames must be unique across external entries.
The check treats uppercase and lowercase letters as equal.

If you omit `file`, Plugin Portal generates a stable managed name:

```text
[PP] <id> [GITHUB].jar
[PP] <id> [GEYSERMC].jar
```

A stable filename lets Bukkit replace the same plugin on restart.
Old versioned JARs do not remain beside the new one.
To use a JAR with a different filename, run an `import` command with its exact filename.

## GitHub Releases

This example selects ViaVersion's Bukkit-compatible release asset:

```yaml
plugins:
  viaversion:
    source: github:ViaVersion/ViaVersion
    asset: "^ViaVersion-[0-9].*\\.jar$"
    file: "[PP] viaversion [GITHUB].jar"
    prereleases: false
    updates: auto
```

The `asset` value is a regular expression. It must match the complete asset filename.
Plugin Portal checks releases from newest to oldest. It skips drafts.
Unless `prereleases` is `true`, it skips prereleases.
If no asset matches, it skips the release.

If multiple assets in one release match, the check fails.
Plugin Portal does not guess which JAR to install.

You can also add ViaVersion in the game.
These commands create an entry with manual updates.
The filename pattern matches names that contain `ViaVersion`:

```text
/pp external add github viaversion ViaVersion ViaVersion ViaVersion
/pp external install viaversion
```

For the exact asset pattern above, edit `asset` in `external-plugins.yml`.
For automatic updates, edit `updates` in the same file.
Run `/pp external reload`.

To use an installed `ViaVersion.jar`, run this command:

```text
/pp external import github viaversion ViaVersion ViaVersion ViaVersion ViaVersion.jar
```

If the entry requires prereleases, add `--prereleases` to either GitHub command.

## GeyserMC downloads

GeyserMC adapters use a project and an artifact name from the GeyserMC download
API. These entries manage the Spigot builds of Geyser and Floodgate:

```yaml
plugins:
  geyser:
    source: geysermc:geyser
    artifact: spigot
    file: "[PP] geyser [GEYSERMC].jar"
    updates: manual

  floodgate:
    source: geysermc:floodgate
    artifact: spigot
    file: "[PP] floodgate [GEYSERMC].jar"
    updates: manual
```

The equivalent commands are:

```text
/pp external add geysermc geyser geyser spigot
/pp external install geyser
```

To use an installed `Geyser-Spigot.jar`, run this command:

```text
/pp external import geysermc geyser geyser spigot Geyser-Spigot.jar
```

GeyserMC supplies a SHA-256 digest for each artifact.
Before Plugin Portal installs or stages the download, it verifies the digest.

## Updates

The `updates` policy controls when Plugin Portal can update an installed entry:

- `manual` permits updates only through explicit `/pp external update` and `updateAll` commands.
- `auto` permits explicit commands and the startup update check.
- `disabled` keeps the entry tracked but prevents updates.

Useful commands:

```text
/pp external check <id>
/pp external install <id>
/pp external update <id>
/pp external updateAll
/pp external invalidate <id>
/pp external uninstall <id>
/pp external reload
```

Plugin Portal stages Bukkit updates in Bukkit's update directory.
The updates apply after a server restart.
`invalidate` forces the next eligible update to download the selected artifact again.
Uninstall removes installed and staged JARs but keeps the configuration entry.

## Recognition exclusions and state

Plugin Portal excludes configured external plugins from normal marketplace recognition.
It excludes both of these:

- The configured external filename.
- The installed or staged SHA-256 hashes recorded for that entry.

This applies to `/pp recognize`, `/pp recognizeAll`, and the unrecognized JARs that `/pp list --all` shows.
Plugin Portal does not also track that external JAR as a marketplace plugin.

Plugin Portal stores runtime state separately in this file:

```text
plugins/PluginPortal/external-plugins-state.json
```

Do not use that JSON file for configuration.
Plugin Portal owns it and records installed and staged versions, hashes, check times, and errors there.
Keep user configuration in `external-plugins.yml`.
After you edit it, use `/pp external reload`.
