package gg.flyte.pluginportal.plugin.command.lamp

import gg.flyte.pluginportal.common.types.MarketplacePlatform
import gg.flyte.pluginportal.plugin.chat.*
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import revxrsal.commands.bukkit.exception.BukkitExceptionAdapter
import revxrsal.commands.bukkit.sender
import revxrsal.commands.command.CommandActor
import revxrsal.commands.exception.EnumNotFoundException
import revxrsal.commands.exception.MissingArgumentException

class LampExceptionHandler(private val audiences: BukkitAudiences): BukkitExceptionAdapter() {

    override fun missingArgument(actor: CommandActor, exception: MissingArgumentException) {
        sendFailureMessage(audiences.sender(actor.sender), "No value provided for ${exception.parameter.name}")
    }

    override fun invalidEnumValue(actor: CommandActor, exception: EnumNotFoundException) {
        var comp = status(Status.FAILURE, "Invalid ${exception.parameter.name}: ${exception.input}")

        if (MarketplacePlatform::class.java == exception.parameter.type)
            comp = comp.appendSecondary("\n\n- Acceptable values are: ${MarketplacePlatform.entries.joinToString()}")

        audiences.sender(actor.sender).sendMessage(comp.boxed())
    }
}