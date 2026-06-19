package gg.flyte.pluginportal.common

import gg.flyte.pluginportal.common.chat.appendPrimary
import gg.flyte.pluginportal.common.chat.textPrimary
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class UpdateNotificationListener : Listener {

    val updatePluginPortalComponent = textPrimary("An update is available for Plugin Portal! ").append(
        Component.text("[Click Here]", NamedTextColor.GRAY).decorate(TextDecoration.UNDERLINED).clickEvent(ClickEvent.suggestCommand("/pp upgrade"))
    ).appendPrimary(" to update.")

    @EventHandler
    fun sendUpdateAvailableMessage(event: PlayerJoinEvent) {
        if (!event.player.hasPermission("pluginportal.admin")) return;
        if (!PluginPortalBase.updateIsAvailable) return;

        val audience = PluginPortalBase.audiences.player(event.player)
        audience.sendMessage(updatePluginPortalComponent)

        // TODO: Can notify about other plugins' status as well.
    }
}