@file:Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")

package gg.flyte.pluginportal.common.commands.lamp

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.MessageType
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.pointer.Pointers
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CommandSenderAudience(
    private val sender: CommandSender,
    audiences: BukkitAudiences,
) : Audience {
    private val fallback = audiences.sender(sender)
    private val nativeSendMessage = sender.javaClass.methods.firstOrNull { method ->
        method.name == "sendMessage" &&
            method.parameterTypes.size == 1 &&
            method.parameterTypes[0].name == Component::class.java.name
    }
    private val pointers = Pointers.builder()
        .withStatic(Identity.NAME, if (sender is Player) sender.name else "CONSOLE")
        .apply {
            if (sender is Player) withStatic(Identity.UUID, sender.uniqueId)
        }
        .build()

    override fun pointers(): Pointers = pointers

    override fun sendMessage(message: Component) {
        sendNative(message) || sendFallback(message)
    }

    override fun sendMessage(message: Component, type: MessageType) {
        sendNative(message) || runCatching {
            fallback.sendMessage(message, type)
            true
        }.getOrDefault(false) || sendPlain(message)
    }

    override fun sendMessage(source: Identity, message: Component, type: MessageType) {
        sendNative(message) || runCatching {
            fallback.sendMessage(source, message, type)
            true
        }.getOrDefault(false) || sendPlain(message)
    }

    private fun sendNative(message: Component): Boolean = runCatching {
        val method = nativeSendMessage ?: return false
        method.invoke(sender, message)
        true
    }.getOrDefault(false)

    private fun sendFallback(message: Component): Boolean = runCatching {
        fallback.sendMessage(message)
        true
    }.getOrDefault(false) || sendPlain(message)

    private fun sendPlain(message: Component): Boolean = runCatching {
        sender.sendMessage(PlainTextComponentSerializer.plainText().serialize(message))
        true
    }.getOrDefault(false)
}
