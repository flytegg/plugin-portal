package gg.flyte.pluginportal.paper.command.info.display

import gg.flyte.pluginportal.api.type.MarketplacePlugin
import net.kyori.adventure.text.Component

interface InfoInterface {
    fun getDisplayInfo(plugin: MarketplacePlugin): Component
}