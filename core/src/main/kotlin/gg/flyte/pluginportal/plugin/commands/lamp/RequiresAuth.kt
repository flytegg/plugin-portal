package gg.flyte.pluginportal.plugin.commands.lamp

import gg.flyte.pluginportal.common.commands.lamp.LampExceptionHandler
import gg.flyte.pluginportal.plugin.PortalApplication
import gg.flyte.pluginportal.common.commands.lamp.PortalCommandActor
import revxrsal.commands.node.ExecutionContext
import revxrsal.commands.process.CommandCondition

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class RequiresAuth

class RequiresAuthValidator : CommandCondition<PortalCommandActor> {
    override fun test(context: ExecutionContext<PortalCommandActor>) {
        // Will `return` if it does not have a @RequiresAuth annotation
        context.command().annotations().get(RequiresAuth::class.java) ?: return

        if (!PortalApplication.isAuthed() && !PortalApplication.refreshEntitlement()) {
            throw LampExceptionHandler.UnauthenticatedException()
        }

    }
}
