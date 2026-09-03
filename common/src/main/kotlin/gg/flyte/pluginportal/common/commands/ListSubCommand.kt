package gg.flyte.pluginportal.common.commands

import com.google.gson.JsonParser
import gg.flyte.pluginportal.common.API
import gg.flyte.pluginportal.common.PluginPortalBase
import gg.flyte.pluginportal.common.Constants
import gg.flyte.pluginportal.common.adapters.external.ExternalPluginConfig
import gg.flyte.pluginportal.common.adapters.external.ExternalPluginState
import gg.flyte.pluginportal.common.chat.*
import gg.flyte.pluginportal.common.commands.lamp.EnabledCommand
import gg.flyte.pluginportal.common.commands.lamp.Features
import gg.flyte.pluginportal.common.managers.ExternalPluginManager
import gg.flyte.pluginportal.common.managers.ExternalPluginResult
import gg.flyte.pluginportal.common.managers.LocalPluginCache
import gg.flyte.pluginportal.common.managers.MarketplacePluginCache
import gg.flyte.pluginportal.common.types.LocalPlugin
import gg.flyte.pluginportal.common.types.Version
import gg.flyte.pluginportal.common.util.HashType
import gg.flyte.pluginportal.common.util.SharedComponents
import gg.flyte.pluginportal.common.util.async
import gg.flyte.pluginportal.common.util.isJarFile
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import java.io.File
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.annotation.Switch
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("pp", "pluginportal", "ppm")
class ListSubCommand {

    private val BUTTON_PIXEL_LENGTH = "[Update] [Uninstall]".pixelLength()
    private val UPTODATE_BUTTON_PIXEL_LENGTH = "[Up to date] [Uninstall]".pixelLength()

    @EnabledCommand(Features.LIST)
    @Subcommand("list")
    @CommandPermission("pluginportal.view")
    fun listCommand(
        audience: Audience,
        @Switch("detailed") detailed: Boolean = false,
        @Switch("all") all: Boolean = false,
        @Switch("outdated") outdated: Boolean = false,
        @Switch("external") externalOnly: Boolean = false,
    ) {
        async {
            if (all && outdated) return@async audience.sendFailure("Use either --all or --outdated, not both")
            if (all && externalOnly) return@async audience.sendFailure("Use either --all or --external, not both")

            val plugins = if (externalOnly) emptyList() else LocalPluginCache
                    .sortedBy { plugin -> plugin.name }
                    .filter { plugin -> plugin.name != PluginPortalBase.plugin.name }
            val marketplaceUpdates = if (outdated && plugins.isNotEmpty()) {
                val remotes = API.getAllPluginsByPlatformIds(plugins.map(LocalPlugin::platformWithId))
                    ?: return@async audience.sendFailure("Could not check marketplace plugins for updates")
                plugins.mapNotNull { local ->
                    val remote = remotes[local.platform]?.get(local.platformId) ?: return@mapNotNull null
                    local.targetUpdateVersion(remote)?.let { version -> MarketplaceUpdate(local, version) }
                }
            } else emptyList()
            val checkedExternalPlugins = ExternalPluginManager.configuredPlugins().map { (config, state) ->
                ExternalListEntry(config, state, ExternalPluginManager.check(config.id))
            }
            val externalPlugins = if (outdated) {
                checkedExternalPlugins.filter { entry -> entry.state?.installed != null && entry.check.updateAvailable }
            } else checkedExternalPlugins
            val failedExternalChecks = if (outdated) {
                checkedExternalPlugins.filter { entry -> entry.state?.installed != null && !entry.check.success }
            } else emptyList()
            val unrecognizedJars = if (all) findUnrecognizedJars(plugins, externalPlugins) else emptyList()

            if (outdated && marketplaceUpdates.isEmpty() && externalPlugins.isEmpty() && failedExternalChecks.isEmpty()) {
                return@async audience.sendSuccess(
                    if (externalOnly) "All external plugins are up to date" else "All plugins are up to date"
                )
            }
            if (!outdated && plugins.isEmpty() && externalPlugins.isEmpty() && unrecognizedJars.isEmpty()) {
                val emptyMessage = if (externalOnly) "No external plugins configured" else "No plugins found"
                return@async audience.sendMessage(Component.text(emptyMessage, NamedTextColor.GRAY).boxed())
            }

            var message = Component.empty()
            var hasSection = false
            val console = audience.isConsole()
            val showDetails = detailed || console

            fun startSection(title: String) {
                if (hasSection) message = message.append(Component.text("\n\n"))
                message = message.append(textPrimary(title).bold())
                hasSection = true
            }

            val marketplaceEntries = if (outdated) marketplaceUpdates else plugins
            if (marketplaceEntries.isNotEmpty()) {
                startSection("Marketplace")
                if (outdated) {
                    marketplaceUpdates.forEach { update ->
                        message = message.append(Component.text("\n"))
                            .append(getOutdatedMarketplaceLine(update, showDetails, console))
                    }
                } else {
                    plugins.forEach { plugin ->
                        message = message.append(Component.text("\n"))
                            .append(if (showDetails) getDetailedPluginLine(plugin, console) else getCompactPluginLine(plugin, console))
                    }
                }
            }

            if (externalPlugins.isNotEmpty()) {
                startSection("External")
                externalPlugins.forEach { plugin ->
                    message = message.append(Component.text("\n"))
                        .append(
                            if (outdated) getOutdatedExternalPluginLine(plugin, showDetails, console)
                            else if (showDetails) getDetailedExternalPluginLine(plugin, console)
                            else getCompactExternalPluginLine(plugin, console)
                        )
                }
            }

            if (failedExternalChecks.isNotEmpty()) {
                startSection("External check failures")
                failedExternalChecks.forEach { entry ->
                    message = message.append(Component.text("\n"))
                        .append(textDark(" - "))
                        .appendPrimary(entry.config.id)
                        .appendSecondary(": ${entry.check.message}")
                }
            }

            if (unrecognizedJars.isNotEmpty()) {
                startSection("Unrecognized JARs")

                unrecognizedJars.forEach { jar ->
                    message = message.append(Component.text("\n"))
                        .append(if (showDetails) getDetailedUnrecognizedJarLine(jar) else getCompactUnrecognizedJarLine(jar))
                }
            }

            audience.sendMessage(message.boxed())
        }
    }

    private fun getOutdatedMarketplaceLine(update: MarketplaceUpdate, detailed: Boolean, console: Boolean): Component {
        val plugin = update.plugin
        var line = textDark(" - ")
            .append(
                textPrimary(plugin.name)
                    .showOnHover("View ${plugin.name}")
                    .suggestCommand("/pp view \"${plugin.platformId}\" ${plugin.platform} --byId")
            )
            .appendDark(" (${plugin.platform.name}) ")

        if (detailed) {
            line = line.appendSecondary("id=${plugin.platformId} ")
        }

        line = line
            .append(Component.text(plugin.version, NamedTextColor.RED))
            .appendDark(" → ")
            .append(Component.text(update.version.versionNumber, NamedTextColor.GREEN))

        if (plugin.excludedFromUpdates) line = line.appendDark(" (skipped by updateAll)")
        if (!console) {
            line = line.appendSecondary("  ").append(
                textPrimary("Update")
                    .hyperlink()
                    .showOnHover("Update ${plugin.name}")
                    .suggestCommand("/pp update \"${plugin.platformId}\" --byId")
            )
        }

        return line
    }

    private fun getCompactExternalPluginLine(entry: ExternalListEntry, console: Boolean): Component {
        var line = Component.text(" - ", NamedTextColor.DARK_GRAY)
            .append(
                textPrimary(entry.config.id)
                    .hoverEvent(HoverEvent.showText(textSecondary("External plugin: ${entry.config.sourceId}")))
                    .suggestCommand("/pp external check ${entry.config.id}")
            )
            .append(textDark(" (${entry.providerName()}) "))
            .append(externalStatusButton(entry))

        if (!console && entry.state?.installed != null) {
            line = line.append(Component.text(" ")).append(externalUninstallButton(entry))
        }
        return line
    }

    private fun getOutdatedExternalPluginLine(entry: ExternalListEntry, detailed: Boolean, console: Boolean): Component {
        val installed = entry.state?.installed ?: return getCompactExternalPluginLine(entry, console)
        val available = entry.check.artifact ?: return getCompactExternalPluginLine(entry, console)
        var line = textDark(" - ")
            .append(
                textPrimary(entry.config.id)
                    .showOnHover("External plugin: ${entry.config.sourceId}")
                    .suggestCommand("/pp external check ${entry.config.id}")
            )
            .appendDark(" (${entry.providerName()}) ")

        if (detailed) line = line.appendSecondary("source=${entry.config.sourceId} ")

        line = line
            .append(Component.text(installed.version, NamedTextColor.RED))
            .appendDark(" → ")
            .append(Component.text(available.version, NamedTextColor.GREEN))

        if (!console) {
            line = line.append(Component.text(" "))
                .append(externalActionButton("Update", "/pp external update ${entry.config.id}", NamedTextColor.AQUA))
                .append(Component.text(" "))
                .append(externalUninstallButton(entry))
        }
        return line
    }

    private fun getDetailedExternalPluginLine(entry: ExternalListEntry, console: Boolean): Component {
        val installed = entry.state?.installed?.version ?: "not installed"
        val latest = entry.check.artifact?.version
        var line = Component.text(" - ", NamedTextColor.DARK_GRAY)
            .append(textPrimary(entry.config.id).suggestCommand("/pp external check ${entry.config.id}"))
            .append(textDark(" (${entry.providerName()}) "))
            .append(textSecondary("source="))
            .append(textPrimary(entry.config.sourceId))
            .append(textDark(" installed="))
            .append(Component.text(installed, entry.installedColor()))

        if (latest != null && latest != installed) {
            line = line.append(textDark(" latest=")).append(Component.text(latest, NamedTextColor.GREEN))
        }

        line = entry.status()?.let { line.append(textDark(" status=")).append(textSecondary(it)) } ?: line
        if (!console) {
            line = line.append(Component.text(" ")).append(externalStatusButton(entry))
            if (entry.state?.installed != null) {
                line = line.append(Component.text(" ")).append(externalUninstallButton(entry))
            }
        }
        return line
    }

    private fun ExternalListEntry.providerName(): String = when (config.provider.lowercase()) {
        "github" -> "GitHub"
        "geysermc" -> "GeyserMC"
        else -> config.provider
    }

    private fun externalStatusButton(entry: ExternalListEntry): Component = when {
        !entry.check.success -> externalActionButton("Check failed", "/pp external check ${entry.config.id}", NamedTextColor.RED)
        entry.state?.staged != null -> externalActionButton("Staged", "/pp external check ${entry.config.id}", NamedTextColor.GRAY)
        entry.state?.installed == null -> externalActionButton("Install", "/pp external install ${entry.config.id}", NamedTextColor.AQUA)
        entry.check.updateAvailable -> externalActionButton("Update", "/pp external update ${entry.config.id}", NamedTextColor.AQUA)
        else -> externalActionButton("Up to date", "/pp external check ${entry.config.id}", NamedTextColor.GRAY)
    }

    private fun externalUninstallButton(entry: ExternalListEntry): Component = externalActionButton(
        "Uninstall",
        "/pp external uninstall ${entry.config.id}",
        NamedTextColor.RED
    )

    private fun externalActionButton(label: String, command: String, color: NamedTextColor): Component = textDark("[")
        .append(Component.text(label, color))
        .appendDark("]")
        .showOnHover("Click to run $command", color)
        .suggestCommand(command)

    private fun ExternalListEntry.status(): String? = when {
        !check.success -> "check failed"
        state?.staged != null -> "${state.staged.version} staged"
        check.updateAvailable -> "update available"
        state?.installed != null -> "up to date"
        else -> null
    }

    private fun ExternalListEntry.installedColor(): NamedTextColor = when {
        state?.installed == null -> NamedTextColor.GRAY
        check.updateAvailable -> NamedTextColor.RED
        else -> NamedTextColor.GREEN
    }

    private data class ExternalListEntry(
        val config: ExternalPluginConfig,
        val state: ExternalPluginState?,
        val check: ExternalPluginResult
    )

    private data class MarketplaceUpdate(
        val plugin: LocalPlugin,
        val version: Version,
    )

    private fun getCompactPluginLine(plugin: LocalPlugin, console: Boolean): Component {
        val buttonLength = if (plugin.isUpToDate) UPTODATE_BUTTON_PIXEL_LENGTH else BUTTON_PIXEL_LENGTH
        val name = plugin.name.shortenToLine(plugin.platform.toString().pixelLength() + 12 + buttonLength)

        var line = Component.text(" - ", NamedTextColor.DARK_GRAY)
            .append(
                textPrimary(name)
                    .hoverEvent(
                        HoverEvent.showText(
                            Component.text("Click to view ", NamedTextColor.GRAY).appendPrimary(plugin.name)))
                    .suggestCommand("/pp view \"${plugin.platformId}\" ${plugin.platform} --byId")
            )
            .append(textDark(" (${plugin.platform.name}) "))
            .append(SharedComponents.getUpdateButton(plugin))

        if (!console) line = line
            .append(Component.text(" "))
            .append(SharedComponents.getInstallButton(plugin, true))

        return line
    }

    private fun getDetailedPluginLine(plugin: LocalPlugin, console: Boolean): Component {
        val marketplace = MarketplacePluginCache.getCachedPluginById(plugin.platform, plugin.platformId)
        val targetVersion = marketplace?.let(plugin::targetUpdateVersion)?.versionNumber
        val status = if (plugin.isUpToDate) "Up to date" else "Update available"

        var line = Component.text(" - ", NamedTextColor.DARK_GRAY)
            .append(
                textPrimary(plugin.name)
                    .hoverEvent(
                        HoverEvent.showText(
                            Component.text("Click to view ", NamedTextColor.GRAY).appendPrimary(plugin.name)))
                    .suggestCommand("/pp view \"${plugin.platformId}\" ${plugin.platform} --byId")
            )
            .append(textDark(" (${plugin.platform.name}) "))
            .append(textSecondary("id="))
            .append(textPrimary(plugin.platformId))
            .append(textDark(" version="))
            .append(Component.text(plugin.version, if (plugin.isUpToDate) NamedTextColor.GREEN else NamedTextColor.RED))

        if (targetVersion != null && targetVersion != plugin.version) {
            line = line
                .append(textDark(" latest="))
                .append(Component.text(targetVersion, NamedTextColor.GREEN))
        }

        line = line
            .append(textDark(" status="))
            .append(Component.text(status, if (plugin.isUpToDate) NamedTextColor.GRAY else NamedTextColor.AQUA))

        if (!console) {
            line = line.append(Component.text(" ")).append(SharedComponents.getUpdateButton(plugin))
                .append(Component.text(" ")).append(SharedComponents.getInstallButton(plugin, true))
        }

        return line
    }

    private fun findUnrecognizedJars(
        plugins: List<LocalPlugin>,
        externalPlugins: List<ExternalListEntry>
    ): List<UnrecognizedJar> {
        val managedHashes = buildSet {
            addAll(plugins.map { it.sha256.lowercase() })
            addAll(ExternalPluginManager.managedHashes())
            addAll(externalPlugins.flatMap { entry ->
                listOfNotNull(entry.state?.installed?.sha256, entry.state?.staged?.sha256)
            }.map(String::lowercase))
            addAll(adapterManagedHashes())
            add(HashType.SHA256.hash(PluginPortalBase.info.pluginJarFile).lowercase())
        }
        val externalFileNames = ExternalPluginManager.managedFileNames()

        return Constants.INSTALL_DIRECTORY
            .listFiles()
            .orEmpty()
            .filter(File::isJarFile)
            .filterNot(LocalPluginCache::hasManagedDownloadedFile)
            .filterNot { it.name.lowercase() in externalFileNames }
            .mapNotNull { file ->
                val hash = runCatching { HashType.SHA256.hash(file) }.getOrNull() ?: return@mapNotNull null
                file.takeUnless { hash.lowercase() in managedHashes }?.let { UnrecognizedJar(it, hash) }
            }
            .sortedBy { it.file.name.lowercase() }
    }

    private fun adapterManagedHashes(): Set<String> {
        val file = File(PluginPortalBase.plugin.dataFolder, "adapter-plugins.json")
        if (!file.isFile) return emptySet()

        return runCatching {
            JsonParser.parseString(file.readText()).asJsonArray
                .mapNotNull { element ->
                    element.asJsonObject.get("sha256")?.takeUnless { it.isJsonNull }?.asString
                }
                .map(String::lowercase)
                .toSet()
        }.getOrDefault(emptySet())
    }

    private fun getCompactUnrecognizedJarLine(jar: UnrecognizedJar): Component =
        Component.text(" - ", NamedTextColor.DARK_GRAY)
            .append(textPrimary(jar.file.name).suggestCommand("/pp recognize \"${jar.file.name}\""))
            .append(Component.text(" "))
            .append(recognizeButton(jar.file))

    private fun getDetailedUnrecognizedJarLine(jar: UnrecognizedJar): Component =
        Component.text(" - ", NamedTextColor.DARK_GRAY)
            .append(textPrimary(jar.file.name).suggestCommand("/pp recognize \"${jar.file.name}\""))
            .append(Component.text(" "))
            .append(textSecondary("sha256="))
            .append(textPrimary(jar.sha256.take(12)))
            .append(Component.text(" "))
            .append(recognizeButton(jar.file))

    private fun recognizeButton(file: File): Component {
        val command = "/pp recognize \"${file.name}\""
        return textDark("[")
            .append(Component.text("RECOGNIZE", NamedTextColor.AQUA))
            .append(textDark("]"))
            .hoverEvent(HoverEvent.showText(textSecondary("Click to run $command")))
            .clickEvent(ClickEvent.runCommand(command))
    }

    private data class UnrecognizedJar(
        val file: File,
        val sha256: String
    )

}
