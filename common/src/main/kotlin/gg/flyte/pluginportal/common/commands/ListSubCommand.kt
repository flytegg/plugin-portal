package gg.flyte.pluginportal.common.commands

import com.google.gson.JsonParser
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
    ) {
        async {
            val plugins = LocalPluginCache
                .sortedBy { plugin -> plugin.name }
                .filter { plugin -> plugin.name != PluginPortalBase.plugin.name }
            val externalPlugins = ExternalPluginManager.configuredPlugins().map { (config, state) ->
                ExternalListEntry(config, state, ExternalPluginManager.check(config.id))
            }
            val unrecognizedJars = if (all) findUnrecognizedJars(plugins, externalPlugins) else emptyList()

            if (plugins.isEmpty() && externalPlugins.isEmpty() && unrecognizedJars.isEmpty()) return@async audience.sendMessage(
                Component.text("No plugins found", NamedTextColor.GRAY).boxed())

            var message = Component.text("Plugins managed by Plugin Portal", NamedTextColor.GRAY)
            val console = audience.isConsole()
            val showDetails = detailed || console

            plugins.forEach { plugin ->
                message = message.append(Component.text("\n"))
                    .append(if (showDetails) getDetailedPluginLine(plugin) else getCompactPluginLine(plugin, console))
            }

            externalPlugins.forEach { plugin ->
                message = message.append(Component.text("\n"))
                    .append(if (showDetails) getDetailedExternalPluginLine(plugin) else getCompactExternalPluginLine(plugin))
            }

            if (unrecognizedJars.isNotEmpty()) {
                message = message.append(Component.text("\n\n"))
                    .append(textSecondary("Unrecognized JARs"))

                unrecognizedJars.forEach { jar ->
                    message = message.append(Component.text("\n"))
                        .append(if (showDetails) getDetailedUnrecognizedJarLine(jar) else getCompactUnrecognizedJarLine(jar))
                }
            }

            audience.sendMessage(message.boxed())
        }
    }

    private fun getCompactExternalPluginLine(entry: ExternalListEntry): Component {
        val installed = entry.state?.installed?.version ?: "not installed"
        val summary = listOfNotNull(installed, entry.status()).joinToString(" — ")
        return Component.text(" - ", NamedTextColor.DARK_GRAY)
            .append(
                textPrimary(entry.config.id)
                    .hoverEvent(HoverEvent.showText(textSecondary("External plugin: ${entry.config.sourceId}")))
                    .suggestCommand("/pp external check ${entry.config.id}")
            )
            .append(textDark(" (EXTERNAL/${entry.config.provider.uppercase()}) "))
            .append(textSecondary(summary))
    }

    private fun getDetailedExternalPluginLine(entry: ExternalListEntry): Component {
        val installed = entry.state?.installed?.version ?: "not installed"
        val latest = entry.check.artifact?.version
        var line = Component.text(" - ", NamedTextColor.DARK_GRAY)
            .append(textPrimary(entry.config.id).suggestCommand("/pp external check ${entry.config.id}"))
            .append(textDark(" (EXTERNAL/${entry.config.provider.uppercase()}) "))
            .append(textSecondary("source="))
            .append(textPrimary(entry.config.sourceId))
            .append(textDark(" installed="))
            .append(Component.text(installed, entry.installedColor()))

        if (latest != null && latest != installed) {
            line = line.append(textDark(" latest=")).append(Component.text(latest, NamedTextColor.GREEN))
        }

        return entry.status()?.let { line.append(textDark(" status=")).append(textSecondary(it)) } ?: line
    }

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

    private fun getDetailedPluginLine(plugin: LocalPlugin): Component {
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

        if (!plugin.isUpToDate) {
            line = line
                .append(textDark(" command="))
                .append(textPrimary("/pp update \"${plugin.platformId}\" --byId"))
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
