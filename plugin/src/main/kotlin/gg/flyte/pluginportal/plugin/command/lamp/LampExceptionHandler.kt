package gg.flyte.pluginportal.plugin.command.lamp

import gg.flyte.pluginportal.plugin.chat.sendFailureMessage
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import revxrsal.commands.bukkit.exception.BukkitExceptionAdapter
import revxrsal.commands.bukkit.sender
import revxrsal.commands.command.CommandActor
import revxrsal.commands.exception.MissingArgumentException

class LampExceptionHandler(private val audiences: BukkitAudiences): BukkitExceptionAdapter() {

    override fun missingArgument(actor: CommandActor, exception: MissingArgumentException) {
        sendFailureMessage(audiences.sender(actor.sender), "No value provided for ${exception.parameter.name}")
    }

}