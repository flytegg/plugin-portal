# 🌐 Plugin Portal

**Plugin Portal** is an in-game plugin downloader, updater, and manager for
Bukkit-compatible Minecraft servers. It integrates with **Modrinth**,
**Hangar**, **SpigotMC**, and **Polymart** so you can browse, preview, install,
recognize, update, and remove plugins — all from the comfort of your Minecraft
chatbar.

> **Premium features available:** automatic updates, plugin recognition,
> version/channel selection, web editor workflows, custom sources, Discord
> webhooks, and more.
> [🔗 View Plugin Portal Premium](https://polymart.org/product/6974/plugin-portal-premium)

This repository contains the Minecraft plugin only. The hosted Plugin Portal
API, dashboard, release storage, and entitlement services are separate
closed-source infrastructure.

Plugin Portal now builds one public JAR:

```text
PluginPortal-<version>.jar
```

Premium features are controlled by runtime entitlement and server-side API
enforcement, not by a separate premium artifact.

## ✨ Features

- 🚀 **Direct Install**: Install plugins from Modrinth, Hangar, SpigotMC, Polymart,
  and supported custom sources.
- 📚 **Marketplace Search**: Search and preview plugin metadata in game with
  `/pp view`.
- 🕰️ **Version Selection**: Install or update to specific compatible versions and
  marketplace release channels when available.
- 🧰 **Plugin Management**: Update, remove, recognize, import, export, and scan
  managed plugins.
- 🔄 **Self-Updating**: Plugin Portal can check for and install Plugin Portal
  updates.
- 🧭 **Cross-Version Support**: Supports Bukkit-compatible servers from 1.8 through
  current Paper/Folia-style runtimes, including Leaf 26.x.
- 💎 **Premium Workflows**: Use premium commands from the same JAR after configuring
  a valid Plugin Portal key.

> Want even more? Scroll down to see what **Plugin Portal Premium** has to offer!

## 🖼️ Screenshots

**Installing from the chatbar:**

![Installing Plugin](https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExMjNmMDkyOWFlZWZjYjk1ODIwMzY2ZmQ5YmM0ZWI5ODQyM2U3NzEwNSZjdD1n/ibAI3FM0CPySWIbdJU/giphy.gif)

**Previewing a plugin in-game:**

![Plugin View](https://i.imgur.com/hTUkP2n.png)

**Updating your plugins effortlessly:**

![Plugin Update](https://i.imgur.com/SzQFXqj.png)

## 💻 Commands

| Command | Permission | Description |
| --- | --- | --- |
| `/pp view <name\|id> [platform] [--byId] [--exact]` | `pluginportal.view` | View marketplace plugin details. |
| `/pp install <name\|id> [platform] [channel] [--byId] [--exact] [--version <version>]` | `pluginportal.manage.install` | Install a plugin from a marketplace. |
| `/pp update <name\|id> [--byId] [--channel <name>] [--version <version>]` | `pluginportal.maintain.update` | Update a tracked plugin. |
| `/pp updateAll` | `pluginportal.maintain.update` | Update all tracked plugins with available updates. |
| `/pp delete <name>` or `/pp uninstall <name>` | `pluginportal.manage.uninstall` | Remove a tracked plugin. |
| `/pp recognize <file>` / `/pp recognizeAll` | `pluginportal.manage.recognize` | Track manually installed plugin JARs. |
| `/pp upgrade [--yes]` | `pluginportal.admin` | Check for and install Plugin Portal updates. |
| `/pp dump` | `pluginportal.dump` | Upload sanitized diagnostics to MCLogs. |
| `/pp help` | `pluginportal.view` | Show command help. |

See `COMMANDS.md` for the detailed command and troubleshooting reference.

## 💎 Plugin Portal Premium

Upgrade to **Premium** for powerful features designed for serious server owners.
Premium features are included in the same JAR and unlock after a valid Plugin
Portal key is configured.

- 🔍 **Plugin Recognition**: Auto-detect and manage existing plugins not installed
  through Plugin Portal.
- 🔄 **Bulk Updates**: Keep tracked plugins up to date with `/pp updateAll`.
- 🌐 **Custom Sources**: Install from supported external adapters such as GitHub
  releases and Modrinth direct flows.
- 🕰️ **Version and Channel Selection**: Pin exact versions or follow marketplace
  beta/release channels.
- 🧑‍💻 **Web Editor Workflows**: Connect the running server to hosted Plugin Portal
  tooling.
- 📣 **Discord Webhooks**: Send install, update, platform-switch, and self-update
  notifications.

🔗 [**Get Plugin Portal Premium** on Polymart](https://polymart.org/product/6974/plugin-portal-premium)

## 🤝 Support

Need help or want to share feedback?

🧠 Join our Discord community: [Discord](https://flyte.gg/discord)

We have an active community of server owners and developers ready to help. ❤️

## 📝 Notes
- Premium features are controlled by runtime entitlement, not by a separate artifact.
- "Plugin Portal" in this repository and license includes the merged free and premium plugin code. Historical names like "Plugin Portal Premium" and `PluginPortalPremium` are covered by the same license and trademark terms.
- Developers and contributors should read `CONTRIBUTING.md` for build, test, local server, endpoint, and release workflow notes.
- See `SECURITY.md` for security reporting.
- This repository is source-available under `LICENSE.md`. It is not an OSI-approved open-source license.
- Plugin Portal branding is covered separately in `TRADEMARKS.md`.

## 🛠️ Source Code

This repository contains the current Plugin Portal plugin source, including the
merged free and premium command code.

The hosted Plugin Portal API, dashboard, release storage, entitlement checks,
and related infrastructure remain closed source.

The plugin is licensed under the Plugin Portal Source Available License in
`LICENSE.md`. It is available for viewing, private use, forks, and contributions
under that license, but it is not an OSI-approved open-source license.

Ways to help:

- 🐛 Report issues you encounter.
- 💡 Suggest features in our [Discord server](https://flyte.gg/discord) or via GitHub Issues.
- 🧑‍💻 Open pull requests for focused plugin fixes or docs improvements.

Thanks for using Plugin Portal! We hope it makes your server management easier than ever. ❤️
