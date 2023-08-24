package gg.flyte.pluginPortal

import org.bukkit.plugin.java.JavaPlugin

class PluginPortal : JavaPlugin() {
    override fun onEnable() {
        logger.info("PluginPortal has been enabled!")
    }

    override fun onDisable() {
        logger.info("PluginPortal has been disabled!")
    }
}