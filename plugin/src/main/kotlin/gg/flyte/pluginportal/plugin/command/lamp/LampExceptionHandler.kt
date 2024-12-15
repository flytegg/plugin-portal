package gg.flyte.pluginportal.plugin.command.lamp

import gg.flyte.pluginportal.common.types.MarketplacePlatform
import gg.flyte.pluginportal.plugin.chat.*
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import revxrsal.commands.bukkit.actor.BukkitCommandActor
import revxrsal.commands.bukkit.exception.BukkitExceptionHandler
import revxrsal.commands.exception.EnumNotFoundException
import revxrsal.commands.exception.MissingArgumentException
import revxrsal.commands.node.ParameterNode

class LampExceptionHandler(private val audiences: BukkitAudiences): BukkitExceptionHandler() {

    private val BukkitCommandActor.audience get() = audiences.sender(sender())

    override fun onMissingArgument(ex: MissingArgumentException, actor: BukkitCommandActor, parameter: ParameterNode<BukkitCommandActor, *>) {
        actor.audience.sendFailure("No value provided for ${parameter.name()}") // Doesn't actually run in v4 with @CommandPlaceholder
    }

    @HandleException
    fun handleMarketplaceException(ex: InvalidMarketplaceException, actor: BukkitCommandActor) {
        var comp = status(Status.FAILURE, "Invalid Marketplace Platform: ${ex.input()}")
            .appendSecondary("\n\n- Acceptable values are: ${MarketplacePlatform.entries.joinToString()}")

        actor.audience.sendMessage(comp.boxed())
    }

    override fun onEnumNotFound(ex: EnumNotFoundException, actor: BukkitCommandActor) {
        actor.audience.sendFailure("${ex.input()} is not recognised}")  // Generic because they removed parameters
    }

//    override fun invalidEnumValue(actor: CommandActor, exception: EnumNotFoundException) {
//        var comp = status(Status.FAILURE, "Invalid ${exception.parameter.name}: ${exception.input}")
//        TODO: Parameter name not available in v4
//
//        if (MarketplacePlatform::class.java == exception.parameter.type)
//            comp = comp.appendSecondary("\n\n- Acceptable values are: ${MarketplacePlatform.entries.joinToString()}")
//
//        audiences.sender(actor.sender).sendMessage(comp.boxed())
//    }
}