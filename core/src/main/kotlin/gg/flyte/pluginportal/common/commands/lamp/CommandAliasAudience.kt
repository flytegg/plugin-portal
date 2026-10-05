package gg.flyte.pluginportal.common.commands.lamp

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.MessageType
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import java.util.regex.Pattern

/** Each invocation keeps its alias, including messages sent by background tasks. */
class CommandAliasAudience(
    private val delegate: Audience,
    commandRoot: String,
    networkRoot: String = "$commandRoot network",
) : Audience {
    private val localCommand = "/$commandRoot"
    private val networkCommand = "/$networkRoot"

    override fun pointers() = delegate.pointers()
    override fun sendMessage(message: Component) = delegate.sendMessage(render(message))
    override fun sendMessage(message: Component, type: MessageType) = delegate.sendMessage(render(message), type)
    override fun sendMessage(identity: Identity, message: Component) = delegate.sendMessage(identity, render(message))
    override fun sendMessage(identity: Identity, message: Component, type: MessageType) = delegate.sendMessage(identity, render(message), type)

    private fun command(reference: String) = if (reference == "pp") localCommand else networkCommand

    private fun render(message: Component): Component = actions(message.replaceText {
        it.match(COMMAND_REFERENCE).replacement { match, builder -> builder.content(command(match.group(1))) }
    })

    private fun actions(message: Component): Component {
        var result = message.children(message.children().map(::actions))
        message.clickEvent()?.let { click ->
            if (click.action() == ClickEvent.Action.RUN_COMMAND || click.action() == ClickEvent.Action.SUGGEST_COMMAND) {
                val value = COMMAND_ACTION.matcher(click.value()).let { match ->
                    if (match.find()) command(match.group(1)) + click.value().substring(match.end()) else click.value()
                }
                result = result.clickEvent(ClickEvent.clickEvent(click.action(), value))
            }
        }
        (message.hoverEvent()?.value() as? Component)?.let { hover ->
            result = result.hoverEvent(HoverEvent.showText(actions(hover)))
        }
        return result
    }

    private companion object {
        val COMMAND_REFERENCE: Pattern = Pattern.compile("(?<![\\w:/])/(ppnetwork|pp(?:\\s+network)?)(?=\\s|$|[.,!?;:)\\]])")
        val COMMAND_ACTION: Pattern = Pattern.compile("^/(ppnetwork|pp(?:\\s+network)?)(?=\\s|$)")
    }
}
