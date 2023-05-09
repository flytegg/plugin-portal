package link.portalbox.pluginportal.type.language

import link.portalbox.pluginportal.PluginPortal
import link.portalbox.pluginportal.type.Config
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

object Message {
    private lateinit var config: FileConfiguration

    private val prefix get() = config.getString("prefix") ?: ""

    val noPermission get() = parseString(config.getString("no-permission"))
    val illegalArguments get() = parseString(config.getString("illegal-arguments"))
    val noPluginSpecified get() = parseString(config.getString("no-plugin-specified"))
    val pluginNotFound get() = parseString(config.getString("plugin-not-found"))
    val downloadNotFound get() = parseString(config.getString("no-download-found"))

    val consoleOutdatedPluginPortal get() = parseString(config.getString("console-plugin-portal-outdated"))
    val playerOutdatedPluginPortal get() = parseString(config.getString("player-plugin-portal-outdated"))
    val playerManuallyRemovedPlugins get () = parseString(config.getString("player-manually-removed-plugins"))

    val pluginNotInstalled get() = parseString(config.getString("plugin-not-installed"))
    val pluginNotDeleted get() = parseString(config.getString("plugin-not-deleted"))
    val pluginDeleted get() = parseString(config.getString("plugin-deleted"))

    val blankStrikeThroughWithWatermark get() = parseString(config.getString("blank-strike-through-with-watermark"))
    val blankStrikeThrough get() = parseString(config.getString("blank-strike-through"))
    val helpCommandDisplay get() = parseString(config.getString("help-command-display"))

    val pluginAlreadyInstalled get() = parseString(config.getString("plugin-already-installed"))
    val pluginIsPremium get() = parseString(config.getString("plugin-is-premium"))
    val pluginIsBeingInstalled get() = parseString(config.getString("plugin-is-being-installed"))
    val pluginHasBeenInstalled get() = parseString(config.getString("plugin-has-been-installed"))
    val pluginAttemptedEnabling get() = parseString(config.getString("plugin-attempted-enabling"))
    val restartServerToEnablePlugin get() = parseString(config.getString("restart-server-to-enable-plugin"))
    val serviceNotSupported get() = parseString(config.getString("service-not-supported"))

    val noPluginsInstalled get() = parseString(config.getString("no-plugins-installed"))
    val listingAllPlugins get() = parseString(config.getString("listing-all-plugins"))
    val installedPlugin get() = parseString(config.getString("installed-plugin"))

    val pluginRequested get() = parseString(config.getString("plugin-requested"))
    val pluginIsSupported get() = parseString(config.getString("plugin-is-supported"))

    val noPluginRequireAnUpdate get() = parseString(config.getString("no-plugin-require-an-update"))
    val updatingPlugins get() = parseString(config.getString("updating-plugins"))
    val pluginUpdated get() = parseString(config.getString("plugin-updated"))
    val pluginNotUpdated get() = parseString(config.getString("plugin-not-updated"))

    val pluginIsUpToDate get() = parseString(config.getString("plugin-is-up-to-date"))
    val listingAllOutdatedPlugins get() = parseString(config.getString("listing-all-outdated-plugins"))

    val previewFormatButton get() = parseString(config.getString("preview-format-button"))

    fun init(pluginPortal: PluginPortal) {
        if (Config.language == null) {
            pluginPortal.getLogger().warning("No language set in config.yml. Defaulting to EN_US")
        }

        val language = Language.valueOf(Config.language?.uppercase() ?: "EN_US")
        if (!language.supported) {
            pluginPortal.getLogger().warning("Language $language is not supported. Defaulting to EN_US")
        }

        val file = File("${pluginPortal.dataFolder}${File.separator}languages", "$language.yml")
        if (!file.exists()) {
            println("Creating language file for $language")
            pluginPortal.saveResource("languages${File.separator}$language.yml", true)
        }

        config = YamlConfiguration.loadConfiguration(file)
    }

    private fun parseString(string: String?): Component {
        return MiniMessage.miniMessage().deserialize(string ?: "prefix <red>Language Error, Please report this to our discord @ discord.gg/pluginportal</red>", Placeholder.component("prefix", MiniMessage.miniMessage().deserialize(
            prefix
        )))
    }

    fun Component.fillInVariables(args: Array<String>): Component {
        var serialized = MiniMessage.miniMessage().serialize(this)

        for ((i, arg) in args.withIndex()) {
            serialized = serialized.replace("**{$i}**", arg)
            println("Replacing **{$i}** with $arg")
        }

        return MiniMessage.miniMessage().deserialize(serialized)
    }

    fun String.deserialize(): Component = MiniMessage.miniMessage().deserialize(this)
}