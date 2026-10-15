package gg.flyte.pluginportal.common.commands.lamp

import net.kyori.adventure.audience.Audience
import gg.flyte.pluginportal.common.commands.lamp.PortalCommandActor
import revxrsal.commands.command.CommandParameter
import revxrsal.commands.command.ExecutableCommand
import revxrsal.commands.process.SenderResolver

class AudienceResolver(): SenderResolver<PortalCommandActor> {
    override fun isSenderType(parameter: CommandParameter) = Audience::class.java.isAssignableFrom(parameter.type())

    override fun getSender(
        customSenderType: Class<*>,
        actor: PortalCommandActor,
        command: ExecutableCommand<PortalCommandActor>
    ) = actor.audience
}
