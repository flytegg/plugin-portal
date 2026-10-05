package gg.flyte.pluginportal.common.commands.lamp

import gg.flyte.pluginportal.common.chat.*
import gg.flyte.pluginportal.common.types.enums.MarketplacePlatform
import gg.flyte.pluginportal.common.commands.lamp.PortalCommandActor
import revxrsal.commands.exception.DefaultExceptionHandler
import revxrsal.commands.exception.CommandErrorException
import revxrsal.commands.exception.EnumNotFoundException
import revxrsal.commands.exception.MissingArgumentException
import revxrsal.commands.node.ParameterNode

class LampExceptionHandler: DefaultExceptionHandler<PortalCommandActor>() {

    class PortalCommandException(val msg: String): CommandErrorException()


    override fun onMissingArgument(ex: MissingArgumentException, actor: PortalCommandActor, parameter: ParameterNode<PortalCommandActor, *>) {
        actor.audience.sendFailure("No value provided for ${parameter.name()}")
    }

    @HandleException
    fun handleGenericPortalException(ex: PortalCommandException, actor: PortalCommandActor) {
        actor.audience.sendFailure(ex.msg)
    }

    @HandleException
    fun handleMarketplaceException(ex: InvalidMarketplaceException, actor: PortalCommandActor) {
        var comp = status(Status.FAILURE, "Invalid Marketplace Platform: ${ex.input()}")
            .appendSecondary("\n\n- Acceptable values are: ${MarketplacePlatform.entries.joinToString()}")

        actor.audience.sendMessage(comp.boxed())
    }

    @HandleException
    fun handleDisabledCommandException(ex: DisabledCommandException, actor: PortalCommandActor) {
        actor.audience.sendFailure("${ex.feature} plugins is disabled in config.")
    }

    class UnauthenticatedException: CommandErrorException()

    @HandleException
    fun handleAuthRequiredCommandException(ex: UnauthenticatedException, actor: PortalCommandActor) {
        actor.audience.sendFailure(gg.flyte.pluginportal.plugin.PortalApplication.lockedPremiumMessage())
    }

    override fun onEnumNotFound(ex: EnumNotFoundException, actor: PortalCommandActor) {
        actor.audience.sendFailure("${ex.input()} is not recognised.")
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
