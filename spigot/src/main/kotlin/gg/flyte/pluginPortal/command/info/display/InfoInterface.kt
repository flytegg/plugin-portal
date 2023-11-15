package gg.flyte.pluginPortal.command.info.display

import gg.flyte.common.api.plugins.schemas.MarketplacePlugin
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent

interface InfoInterface {
    fun getDisplayInfo(plugin: MarketplacePlugin): Component
}