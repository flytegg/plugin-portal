package gg.flyte.pluginportal.common.commands.lamp

import revxrsal.commands.bukkit.actor.BukkitCommandActor
import revxrsal.commands.exception.CommandErrorException
import revxrsal.commands.node.ExecutionContext
import revxrsal.commands.process.CommandCondition

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class EnabledCommand(val feature: Features)

class DisabledCommandException(val feature: Features): CommandErrorException()

class CommandEnabledConditionValidator : CommandCondition<BukkitCommandActor> {
    override fun test(context: ExecutionContext<BukkitCommandActor>) {
        val toggle = context.command().annotations().get(EnabledCommand::class.java) ?: return

        if (!toggle.feature.isEnabled()) {
            throw DisabledCommandException(toggle.feature)
        }
    }
}
