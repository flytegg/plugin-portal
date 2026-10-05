package gg.flyte.pluginportal.common.commands.lamp

import gg.flyte.pluginportal.common.commands.HelpSubCommand
import gg.flyte.pluginportal.plugin.network.NetworkCommands
import revxrsal.commands.exception.*
import revxrsal.commands.exception.context.ErrorContext

/** Parser failures keep their explanation and add help for the matched command. */
class CommandUsageExceptionHandler(
    private val delegate: CommandExceptionHandler<PortalCommandActor>,
) : CommandExceptionHandler<PortalCommandActor> {
    override fun handleException(exception: Throwable, context: ErrorContext<PortalCommandActor>) {
        delegate.handleException(exception, context)
        val syntaxError = exception is InvalidValueException || exception is MissingArgumentException ||
            exception is UnknownParameterException || exception is InputParseException ||
            exception is NumberNotInRangeException || exception is InvalidStringSizeException ||
            exception is InvalidListSizeException
        if (!syntaxError || !context.hasExecutionContext()) return
        val actor = context.actor()
        val command = context.context().command()
        if (!command.isVisibleTo(actor)) return
        val path = command.path().substringAfter(' ', "")
        if (path.startsWith("network ")) {
            NetworkCommands().showCommandHelp(actor, path.substringAfter(' ').substringBefore(' '))
        } else {
            HelpSubCommand().showCommandHelp(actor.audience, path.substringBefore(' '))
        }
    }
}
