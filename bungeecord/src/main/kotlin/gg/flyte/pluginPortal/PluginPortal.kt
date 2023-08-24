package gg.flyte.pluginPortal

import net.md_5.bungee.api.plugin.Plugin

class PluginPortal : Plugin() {
    override fun onEnable() {
        logger.info("PluginPortal has been enabled!")
    }

    override fun onDisable() {
        logger.info("PluginPortal has been disabled!")
    }
}