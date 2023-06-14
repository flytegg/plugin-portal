package link.portalbox.pluginportal.type.language

import link.portalbox.pluginportal.PluginPortal
import link.portalbox.pluginportal.type.Config
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

object Message {
    private lateinit var config: FileConfiguration
    lateinit var audiences: BukkitAudiences

    private val prefix get() = config.getBetterString("prefix")

    val noPermission get() = parseString(config.getBetterString("no-permission"))
    val illegalArguments get() = parseString(config.getBetterString("illegal-arguments"))
    val noPluginSpecified get() = parseString(config.getBetterString("no-plugin-specified"))
    val pluginNotFound get() = parseString(config.getBetterString("plugin-not-found"))
    val downloadNotFound get() = parseString(config.getBetterString("no-download-found"))

    val consoleOutdatedPluginPortal get() = parseString(config.getBetterString("console-plugin-portal-outdated"))
    val playerOutdatedPluginPortal get() = parseString(config.getBetterString("player-plugin-portal-outdated"))
    val playerManuallyRemovedPlugins get() = parseString(config.getBetterString("player-manually-removed-plugins"))

    val pluginNotInstalled get() = parseString(config.getBetterString("plugin-not-installed"))
    val pluginNotDeleted get() = parseString(config.getBetterString("plugin-not-deleted"))
    val pluginDeleted get() = parseString(config.getBetterString("plugin-deleted"))

    val blankStrikeThroughWithWatermark get() = parseString(config.getBetterString("blank-strike-through-with-watermark"))
    val blankStrikeThrough get() = parseString(config.getBetterString("blank-strike-through"))
    val helpCommandDisplay get() = parseString(config.getBetterString("help-command-display"))

    val pluginAlreadyInstalled get() = parseString(config.getBetterString("plugin-already-installed"))
    val pluginIsPremium get() = parseString(config.getBetterString("plugin-is-premium"))
    val pluginIsBeingInstalled get() = parseString(config.getBetterString("plugin-is-being-installed"))
    val pluginHasBeenInstalled get() = parseString(config.getBetterString("plugin-has-been-installed"))
    val pluginAttemptedEnabling get() = parseString(config.getBetterString("plugin-attempted-enabling"))
    val restartServerToEnablePlugin get() = parseString(config.getBetterString("restart-server-to-enable-plugin"))
    val serviceNotSupported get() = parseString(config.getBetterString("service-not-supported"))

    val noPluginsInstalled get() = parseString(config.getBetterString("no-plugins-installed"))
    val listingAllPlugins get() = parseString(config.getBetterString("listing-all-plugins"))
    val installedPlugin get() = parseString(config.getBetterString("installed-plugin"))

    val pluginRequested get() = parseString(config.getBetterString("plugin-requested"))
    val pluginIsSupported get() = parseString(config.getBetterString("plugin-is-supported"))

    val noPluginRequireAnUpdate get() = parseString(config.getBetterString("no-plugin-requires-an-update"))
    val updatingPlugins get() = parseString(config.getBetterString("updating-plugins"))
    val pluginUpdated get() = parseString(config.getBetterString("plugin-updated"))
    val pluginNotUpdated get() = parseString(config.getBetterString("plugin-not-updated"))

    val pluginIsUpToDate get() = parseString(config.getBetterString("plugin-is-up-to-date"))
    val listingAllOutdatedPlugins get() = parseString(config.getBetterString("listing-all-outdated-plugins"))

    val openUrlPreviewFormatButton get() = parseString(config.getBetterString("open-url-preview-format-button"))
    val runCommandPreviewFormatButton get() = parseString(config.getBetterString("run-command-preview-format-button"))

    val noPluginsFound get() = config.getBetterString("no-plugins-found")
    val keepTyping get() = config.getBetterString("keep-typing")

    fun init(pluginPortal: PluginPortal) {
        if (Config.language == null) {
            pluginPortal.getLogger().warning("No language set in config.yml. Defaulting to EN_US")
        }

        var language = Language.EN_US

        try {
            language = Language.valueOf(Config.language?.uppercase() ?: "EN_US")
        } catch (e: IllegalArgumentException) {
            println("Language ${Config.language} is not supported. Defaulting to EN_US")
        }

        if (!language.supported) {
            println("Language ${Config.language} is not supported. Defaulting to EN_US")
            language = Language.EN_US
        }

        val file = File("${pluginPortal.dataFolder}${File.separator}languages", "$language.yml")
        pluginPortal.saveResource("languages${File.separator}$language.yml", true)
        config = YamlConfiguration.loadConfiguration(file)

        audiences = BukkitAudiences.create(pluginPortal)
    }

    private fun parseString(string: String): Component {
        return MiniMessage.miniMessage().deserialize(
            string,
            Placeholder.component(
                "prefix", MiniMessage.miniMessage().deserialize(prefix)
            )
        )
    }

    fun Component.fillInVariables(args: Array<String>): Component {
        var serialized = MiniMessage.miniMessage().serialize(this)

        for ((i, arg) in args.withIndex()) {
            serialized = serialized.replace("**{$i}**", arg)
        }

        return MiniMessage.miniMessage().deserialize(serialized)
    }

    fun String.deserialize(): Component = MiniMessage.miniMessage().deserialize(this)

    fun Component.serialize(): String = MiniMessage.miniMessage().serialize(this)

    private fun FileConfiguration.getBetterString(path: String): String {
        val configString = this.getString(path)
        if (configString.isNullOrEmpty()) {
            throw LanguageLoadingException("Failed to load string at $path, defaulting to English")
        }

        return configString
    }
}