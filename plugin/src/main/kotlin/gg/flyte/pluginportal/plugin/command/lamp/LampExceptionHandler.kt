package gg.flyte.pluginportal.plugin.command.lamp

import gg.flyte.pluginportal.plugin.chat.sendFailureMessage
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import revxrsal.commands.bukkit.actor.BukkitCommandActor
import revxrsal.commands.bukkit.exception.BukkitExceptionHandler
import revxrsal.commands.exception.EnumNotFoundException
import revxrsal.commands.exception.MissingArgumentException
import revxrsal.commands.node.ParameterNode

class LampExceptionHandler(private val audiences: BukkitAudiences): BukkitExceptionHandler() {

    private val BukkitCommandActor.audience get() = audiences.sender(sender())

    override fun onMissingArgument(ex: MissingArgumentException, actor: BukkitCommandActor, parameter: ParameterNode<BukkitCommandActor, *>) {
        // TODO: Test
        sendFailureMessage(actor.audience, "No value provided for ${parameter.name()}")
    }

    override fun onEnumNotFound(ex: EnumNotFoundException, actor: BukkitCommandActor) {
        // TODO: Parameter name not available in v4
//        var comp = status(Status.FAILURE, "Invalid ${ex.}: ${ex.input()}")
//
//        if (MarketplacePlatform::class.java == exception.parameter.type)
//            comp = comp.appendSecondary("\n\n- Acceptable values are: ${MarketplacePlatform.entries.joinToString()}")
//
//        audiences.sender(actor.sender).sendMessage(comp.boxed())
    }

/*    override fun invalidEnumValue(actor: CommandActor, exception: EnumNotFoundException) {
        var comp = status(Status.FAILURE, "Invalid ${exception.parameter.name}: ${exception.input}")

        if (MarketplacePlatform::class.java == exception.parameter.type)
            comp = comp.appendSecondary("\n\n- Acceptable values are: ${MarketplacePlatform.entries.joinToString()}")

        audiences.sender(actor.sender).sendMessage(comp.boxed())
    }*/
}