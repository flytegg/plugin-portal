package gg.flyte.pluginportal.paper.config.language

import gg.flyte.pluginportal.paper.PluginPortal
import gg.flyte.pluginportal.paper.config.Config
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

object Message {
    private lateinit var config: FileConfiguration
//    lateinit var audiences: BukkitAudiences

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
    val notStarredOnHangar get() = parseString(config.getBetterString("not-starred-on-hangar"))

    val pluginIsUpToDate get() = parseString(config.getBetterString("plugin-is-up-to-date"))
    val listingAllOutdatedPlugins get() = parseString(config.getBetterString("listing-all-outdated-plugins"))

    val openUrlPreviewFormatButton get() = parseString(config.getBetterString("open-url-preview-format-button"))
    val runCommandPreviewFormatButton get() = parseString(config.getBetterString("run-command-preview-format-button"))

    val noPluginsFound get() = config.getBetterString("no-plugins-found")
    val keepTyping get() = config.getBetterString("keep-typing")

    init {
        PluginPortal.instance.apply {
            var language = Language.EN_US

            try {
                language = Language.valueOf(Config.language.uppercase()) ?: Language.EN_US
            } catch (e: IllegalArgumentException) {
                println("Language ${Config.language} is not supported. Defaulting to EN_US")
            }

            if (!language.supported) {
                println("Language ${Config.language} is not supported. Defaulting to EN_US")
                language = Language.EN_US
            }

            val file = File("${dataFolder}${File.separator}languages", "$language.yml")
            saveResource("languages${File.separator}$language.yml", true)
            this@Message.config = YamlConfiguration.loadConfiguration(file)

//        audiences = Audiences.create(pluginPortal)
        }
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

    fun String.toComponent(): Component = MiniMessage.miniMessage().deserialize(this)
    fun Component.serialize() = MiniMessage.miniMessage().serialize(this)

    fun Component.solidLine(): TextComponent {
        return Component.text("                                                                               ")
            .decorate(
                TextDecoration.STRIKETHROUGH
            )
    }

    private fun FileConfiguration.getBetterString(path: String): String {
        val configString = this.getString(path)
        if (configString.isNullOrEmpty()) {
            throw NullPointerException("Failed to load string at $path, defaulting to English")
        }

        return configString
    }
}

object Messages {

    val GREETING = LocaleMessages(
        ResponseTypes.INFO,
        hashSetOf(
            Language.EN_US to "Hello",
            Language.ES_ES to "Hola"
        )
    )

}

data class LocaleMessages(
    val responseType: ResponseTypes.Response,
    val messages: HashSet<Pair<Language, String>>,
)

object ResponseTypes {

    object INFO: Response {
        override val color: NamedTextColor = NamedTextColor.GRAY
    }

    object SUCCESS: Response {
        override val color: NamedTextColor = NamedTextColor.GREEN
    }

    object ERROR: Response {
        override val color: NamedTextColor = NamedTextColor.RED
    }

    interface Response {
        val color: NamedTextColor
    }

}