package gg.flyte.pluginportal.common.commands.lamp

import gg.flyte.pluginportal.common.chat.*
import gg.flyte.pluginportal.common.types.enums.MarketplacePlatform
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import revxrsal.commands.bukkit.actor.BukkitCommandActor
import revxrsal.commands.bukkit.exception.BukkitExceptionHandler
import revxrsal.commands.exception.CommandErrorException
import revxrsal.commands.exception.EnumNotFoundException
import revxrsal.commands.exception.MissingArgumentException
import revxrsal.commands.node.ParameterNode

class LampExceptionHandler(private val audiences: BukkitAudiences): BukkitExceptionHandler() {

    class PortalCommandException(val msg: String): CommandErrorException()

    private val BukkitCommandActor.audience get() = CommandSenderAudience(sender(), audiences)

    override fun onMissingArgument(ex: MissingArgumentException, actor: BukkitCommandActor, parameter: ParameterNode<BukkitCommandActor, *>) {
        actor.audience.sendFailure("No value provided for ${parameter.name()}") // Doesn't actually run in v4 with @CommandPlaceholder
    }

    @HandleException
    fun handleGenericPortalException(ex: PortalCommandException, actor: BukkitCommandActor) {
        actor.audience.sendFailure(ex.msg)
    }

    @HandleException
    fun handleMarketplaceException(ex: InvalidMarketplaceException, actor: BukkitCommandActor) {
        var comp = status(Status.FAILURE, "Invalid Marketplace Platform: ${ex.input()}")
            .appendSecondary("\n\n- Acceptable values are: ${MarketplacePlatform.entries.joinToString()}")

        actor.audience.sendMessage(comp.boxed())
    }

    @HandleException
    fun handleDisabledCommandException(ex: DisabledCommandException, actor: BukkitCommandActor) {
        actor.audience.sendFailure("${ex.feature} plugins is disabled in config.")
    }

    class UnauthenticatedException: CommandErrorException()

    @HandleException
    fun handleAuthRequiredCommandException(ex: UnauthenticatedException, actor: BukkitCommandActor) {
        val message = runCatching {
            val pluginClass = Class.forName("gg.flyte.pluginportal.plugin.PluginPortal")
            val instance = pluginClass.getField("instance").get(null)
            pluginClass.getMethod("lockedPremiumMessage").invoke(instance) as? String
        }.getOrNull()

        if (message != null) actor.audience.sendFailure(message)
        else actor.audience.sendUnAuthed()
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
