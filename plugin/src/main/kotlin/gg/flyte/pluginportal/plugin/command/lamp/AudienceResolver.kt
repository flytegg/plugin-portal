package gg.flyte.pluginportal.plugin.command.lamp

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import revxrsal.commands.bukkit.actor.BukkitCommandActor
import revxrsal.commands.command.CommandParameter
import revxrsal.commands.command.ExecutableCommand
import revxrsal.commands.process.SenderResolver

class AudienceResolver(val audiences: BukkitAudiences): SenderResolver<BukkitCommandActor> {
    override fun isSenderType(parameter: CommandParameter) = Audience::class.java.isAssignableFrom(parameter.type())

    override fun getSender(
        customSenderType: Class<*>,
        actor: BukkitCommandActor,
        command: ExecutableCommand<BukkitCommandActor>
    ) = audiences.sender(actor.sender())
}