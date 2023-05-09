package link.portalbox.pluginportal.listener

import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class ChatListener : Listener {

    @EventHandler
    fun onChat(event: AsyncChatEvent) {
        event.isCancelled = true
        Bukkit.broadcast(MiniMessage.miniMessage().deserialize(event.message().toString()))
    }
}