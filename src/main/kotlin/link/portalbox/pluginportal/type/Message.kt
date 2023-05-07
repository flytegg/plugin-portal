package link.portalbox.pluginportal.type

import link.portalbox.pluginportal.PluginPortal
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags
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

    val consoleOutdatedPluginPortal get() = parseString(config.getString("plugin-portal-outdated-console"))
    val playerOutdatedPluginPortal get() = parseString(config.getString("plugin-portal-outdated-player"))
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
    val pluginAttemptedEnabling = parseString(config.getString("plugin-attempted-enabling"))
    val restartServerToEnablePlugin = parseString(config.getString("restart-server-to-enable-plugin"))

    val noPluginsInstalled get() = parseString(config.getString("no-plugins-installed"))
    val listingAllPlugins get() = parseString(config.getString("listing-all-plugins"))
    val installedPlugin get() = parseString(config.getString("installed-plugin"))

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
        return MiniMessage.miniMessage().deserialize(string ?: "<prefix> <red>Language Error, Please report this to our discord @ discord.gg/pluginportal</red>", Placeholder.component("<prefix>", Component.text(prefix)))
    }
}