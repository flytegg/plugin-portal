package gg.flyte.pluginportal.common.commands.lamp

import gg.flyte.pluginportal.common.PluginPortalBase
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import revxrsal.commands.command.CommandActor
import java.util.UUID

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class CommandPermission(val value: String)

class PortalCommandActor(
    val audience: Audience,
    private val actorName: String,
    private val id: UUID,
    val isConsole: Boolean,
    val hasPermission: (String) -> Boolean,
) : CommandActor {
    override fun name() = actorName
    override fun uniqueId() = id
    override fun lamp() = PluginPortalBase.lamp
    override fun sendRawMessage(message: String) = audience.sendMessage(Component.text(message))
    override fun sendRawError(message: String) = sendRawMessage(message)
}
