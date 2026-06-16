# 🌐 Plugin Portal

**Plugin Portal** is the ultimate in-game plugin downloader and updater for Minecraft servers, integrating directly with the **Polymart**, **SpigotMC**, **Modrinth**, and **Hangar** marketplaces. Browse, preview, install, and update plugins — all from the comfort of your Minecraft chatbar.

> **Premium version available:** Unlock auto-updates, malware scanning, external source support, and more!
> [🔗 View Plugin Portal Premium](https://polymart.org/product/6974/plugin-portal-premium)

---

## ✨ Features

* 🚀 **Direct Install**: Install plugins directly from **Polymart**, **Spigot**, **Modrinth**, and **Hangar** with a single command.
* 📚 **Massive Plugin Directory**: Instantly search and install from **100,000+ plugins** — no browser required.
* 🔍 **Plugin Previewing**: View icons, descriptions, stats, and ratings in-game with `/pp view`.
* 🔄 **Self-Updating**: Plugin Portal keeps itself up-to-date, automatically.
* 🧭 **Cross-Version Compatible**: Works with **1.8+** and supports all major server jars — including **Folia**.
* 🧹 **No Junk Plugins**: Automatically filters out inactive, deprecated, and abandoned plugins.

> Want even more? Scroll down to see what **Plugin Portal Premium** has to offer!

---

## 🖼️ Screenshots

**Installing from the chatbar:**

![Installing Plugin](https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExMjNmMDkyOWFlZWZjYjk1ODIwMzY2ZmQ5YmM0ZWI5ODQyM2U3NzEwNSZjdD1n/ibAI3FM0CPySWIbdJU/giphy.gif)

**Previewing a plugin in-game:**

![Plugin View](https://i.imgur.com/hTUkP2n.png)

**Updating your plugins effortlessly:**

![Plugin Update](https://i.imgur.com/SzQFXqj.png)

---

## 💻 Commands & Permissions

| Command                                            | Permission                      | Description                                                               |
| -------------------------------------------------- | ------------------------------- | ------------------------------------------------------------------------- |
| `/pp view <name\|id> [platform] [-byId]`           | `pluginportal.view`             | View plugin preview including image, description, stats, and ratings.     |
| `/pp install <name\|id> [platform] [-byId]`        | `pluginportal.manage.install`   | Install a plugin from a marketplace using its name or ID.                 |
| `/pp update <name\|id> [-byId]`                    | `pluginportal.maintain.update`  | Update a plugin to its latest version from the original source.           |
| `/pp uninstall` or `/pp delete <name\|id> [-byId]` | `pluginportal.manage.uninstall` | Remove a plugin installed by Plugin Portal.                               |
| `/pp list`                                         | `pluginportal.view`             | List all plugins installed through Plugin Portal.                         |
| `/pp help`                                         | `pluginportal.view`             | Display the help menu with all commands.                                  |
| `/pp dump`                                         | `pluginportal.dump`             | Dump Plugin Portal's internal logs to [https://mclo.gs](https://mclo.gs). |

---

## 💎 Plugin Portal Premium

Upgrade to **Premium** for powerful features designed for serious server owners:

* 🔍 **Plugin Recognition**: Auto-detect and manage existing plugins not installed via Plugin Portal.
* 🛡️ **Malware Detection**: Each download is scanned for safety using a Minecraft-specific scanner.
* 🔄 **Automatic Updates**: Keep every plugin up to date automatically — no commands required.
* 🌐 **Custom Adapters**: Install from GitHub, Jenkins, or other external sources.
* 🕰️ **Install Specific Versions**: Need a particular version of a plugin? No problem.
* 🧯 **Plugin Backups**: Roll back to earlier plugin versions if something breaks.
* 🚀 **Priority Releases & Support**: Get updates and help faster than everyone else.

🔗 [**Get Plugin Portal Premium** on Polymart](https://polymart.org/product/6974/plugin-portal-premium)

---

## 🤝 Support

Need help or want to share feedback?

🧠 Join our Discord community: [Discord](https://flyte.gg/discord)

We have an active, friendly community of developers ready to assist you. ❤️

---

## 🛠️ Source Availability

Plugin Portal started as an open source project, and the public GitHub repository remains available here:
[https://github.com/flytegg/plugin-portal](https://github.com/flytegg/plugin-portal)

The current release line is temporarily developed in a private repository while Premium support is being finished. The code in this public repository is still close to how the plugin works today, but it may not include the newest updates yet.

The plan is to reopen the plugin codebase, including the free and Premium plugin code, once the remaining cleanup work is complete. The hosted API that powers marketplace/search features will remain closed source.

### Ways to Help:

* 🐛 Report issues you encounter
* 💡 Suggest features in our [Discord server](https://flyte.gg/discord) or via GitHub Issues
* 🧑‍💻 Watch this repository for updates when source contributions reopen

Thanks for using Plugin Portal! We hope it makes your server management easier than ever. ❤️
