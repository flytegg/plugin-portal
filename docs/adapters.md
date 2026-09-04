# External plugin adapters

External adapters let Plugin Portal manage plugins that are distributed outside
its marketplace catalog. The built-in providers are GitHub Releases and the
GeyserMC download API.

External plugin commands require a linked Plugin Portal server and the
`pluginportal.manage.external` permission.

## Configuration

Plugin Portal creates this file on first startup:

```text
plugins/PluginPortal/external-plugins.yml
```

Add entries beneath the `plugins` section, then run `/pp external reload`. If the
generated file still contains `plugins: {}`, replace that line with `plugins:`
before adding entries.

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

Each plugin ID may contain letters, numbers, underscores, and hyphens. The
available fields are:

| Field | Required | Meaning |
| --- | --- | --- |
| `source` | Yes | Provider and project separated by `:`. Use `github:owner/repository` or `geysermc:project`. |
| `asset` | GitHub only | Regular expression matched against the entire release asset filename. |
| `artifact` | GeyserMC only | Artifact returned by the GeyserMC API, such as `spigot`, `bungee`, or `velocity`. |
| `file` | No | Stable filename used in the server's `plugins` directory. |
| `prereleases` | No | Whether a GitHub adapter may use prereleases. Defaults to `false`. |
| `updates` | No | `manual`, `auto`, or `disabled`. Defaults to `manual`. |

## Filenames

`file` must be one `.jar` filename, not a path. Filenames must also be unique
across external entries, ignoring capitalization.

When `file` is omitted, Plugin Portal generates a stable managed name:

```text
[PP] <id> [GITHUB].jar
[PP] <id> [GEYSERMC].jar
```

A stable filename lets Bukkit replace the same plugin on restart instead of
leaving old versioned JARs beside the new one. To adopt a JAR that already has a
different filename, use an `import` command and pass its exact filename.

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

The `asset` value is a regular expression matched against the complete asset
filename. Plugin Portal checks releases from newest to oldest, skips drafts,
and skips prereleases unless `prereleases` is `true`. A release with no matching
asset is skipped. If more than one asset in the same release matches, the check
fails so that Plugin Portal does not guess which JAR to install.

You can create the same entry in game. A plain asset value such as `ViaVersion`
is converted to a JAR filename pattern by the command:

```text
/pp external add github viaversion ViaVersion ViaVersion ViaVersion
/pp external install viaversion
```

To adopt an existing `ViaVersion.jar` instead:

```text
/pp external import github viaversion ViaVersion ViaVersion ViaVersion ViaVersion.jar
```

Add `--prereleases` to either GitHub command if that entry should follow
prereleases.

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

To adopt an existing `Geyser-Spigot.jar`:

```text
/pp external import geysermc geyser geyser spigot Geyser-Spigot.jar
```

GeyserMC supplies a SHA-256 digest for each artifact. Plugin Portal verifies the
download before installing or staging it.

## Updates

The `updates` policy controls when an installed entry is eligible for an update:

- `manual`: only explicit `/pp external update` and `updateAll` commands.
- `auto`: explicit commands plus the startup update check.
- `disabled`: tracked, but never updated.

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

Updates are staged in Bukkit's update directory and take effect after a server
restart. `invalidate` forces the next eligible update to download the resolved
artifact again. Uninstalling removes installed and staged JARs but keeps the
configuration entry.

## Recognition exclusions and state

Configured external plugins are deliberately excluded from normal marketplace
recognition. Plugin Portal excludes both:

- the configured external filename; and
- the installed or staged SHA-256 hashes recorded for that entry.

This applies to `/pp recognize`, `/pp recognizeAll`, and the unrecognized JARs
shown by `/pp list --all`. It prevents an externally managed JAR from also being
adopted as a marketplace plugin.

Runtime state is stored separately in:

```text
plugins/PluginPortal/external-plugins-state.json
```

Do not use that JSON file for configuration. Plugin Portal owns it and records
installed and staged versions, hashes, check times, and errors there. Keep user
configuration in `external-plugins.yml`, and use `/pp external reload` after
editing it.
