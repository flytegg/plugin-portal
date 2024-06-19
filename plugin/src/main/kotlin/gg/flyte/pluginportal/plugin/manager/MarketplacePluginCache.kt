package gg.flyte.pluginportal.plugin.manager

import gg.flyte.pluginportal.common.API
import gg.flyte.pluginportal.common.types.MarketplacePlatform
import gg.flyte.pluginportal.common.types.Plugin
import gg.flyte.pluginportal.plugin.logging.PortalLogger
import gg.flyte.pluginportal.plugin.util.*
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor

object MarketplacePluginCache : PluginCache<Plugin>() {

    fun getFilteredPlugins(
        prefix: String? = null,
        platform: MarketplacePlatform? = null,
        id: String? = null
    ): List<Plugin> {
        val plugins = API.getPlugins(prefix)

        if (id != null) return plugins.filter { plugin -> plugin.id == id }
        if (platform != null) return plugins.filter { plugin -> plugin.platforms.containsKey(platform) }

        return plugins
    }

    fun installPlugin(
        audience: Audience,
        plugin: Plugin,
        platform: MarketplacePlatform,
        targetDirectory: String,
    ) {
        val platformPlugin =
            plugin.platforms[platform] ?: return sendFailureMessage(audience, "No platform plugin found")

        val downloadURL = platformPlugin.download?.url
            ?: return audience.sendMessage(
                text("No download URL found for platform ", NamedTextColor.RED)
                    .append(text(platform.name, NamedTextColor.AQUA))
                    .append(endLine())
            )

        text("Invalid download URL for platform ", NamedTextColor.RED)
            .append(text(platform.name, NamedTextColor.AQUA))
            .append(endLine())

        if (!isValidDownload(downloadURL)) {
            return audience.sendMessage(
                status(Status.FAILURE, "Invalid download URL. See more information below")
                    .appendNewline()
                    .append(
                        textSecondary(
                            """
                        - The URL must be a direct download link
                        - The URL must download a JAR file
                        - Please contact us in our discord for more information
                            """.trimIndent()
                        )
                    )
            )
        }

        audience.sendMessage(
            textSecondary("Found download URL, starting installation from: ")
                .appendPrimary(platform.name).appendSecondary("...")
        )


        val targetMessage = "${plugin.name} from ${platform.name} with ID ${plugin.id}"
        PortalLogger.log(audience, PortalLogger.Action.INITIATED_INSTALL, targetMessage)
        plugin.download(platform, targetDirectory)
        PortalLogger.log(audience, PortalLogger.Action.INSTALL, targetMessage)

        audience.sendMessage(
            newline()
                .appendStatus(Status.SUCCESS, "Downloaded ${plugin.name} from ${platform.name}")
                .append(endLine())
        )
    }

}