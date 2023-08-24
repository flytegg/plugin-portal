package gg.flyte.pluginPortal

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.proxy.ProxyServer
import org.slf4j.Logger


@Plugin(
    id = "pluginportal",
    name = "PluginPortal",
    version = "0.0.1",
    description = "In-game Minecraft plugin package manager.",
    url = "https://flyte.gg",
    authors = ["Flyte"]
)
class PluginPortal {

    private lateinit var server: ProxyServer
    private lateinit var logger: Logger

    @Inject
    fun velocity(server: ProxyServer, logger: Logger) {
        this.server = server
        this.logger = logger

        logger.info("PluginPortal has been enabled!")
    }

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent?) {
        // OnEnable Alternative
    }
}