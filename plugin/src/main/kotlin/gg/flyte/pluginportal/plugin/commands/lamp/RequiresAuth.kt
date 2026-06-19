package gg.flyte.pluginportal.plugin.commands.lamp

import gg.flyte.pluginportal.common.commands.lamp.LampExceptionHandler
import gg.flyte.pluginportal.plugin.PluginPortal
import revxrsal.commands.bukkit.actor.BukkitCommandActor
import revxrsal.commands.node.ExecutionContext
import revxrsal.commands.process.CommandCondition

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class RequiresAuth

class RequiresAuthValidator : CommandCondition<BukkitCommandActor> {
    override fun test(context: ExecutionContext<BukkitCommandActor>) {
        // Will `return` if it does not have a @RequiresAuth annotation
        context.command().annotations().get(RequiresAuth::class.java) ?: return

        if (!PluginPortal.instance.isAuthed() && !PluginPortal.instance.refreshEntitlement()) {
            throw LampExceptionHandler.UnauthenticatedException()
        }

    }
}
