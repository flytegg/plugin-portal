package gg.flyte.pluginportal.plugin.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.*
import net.kyori.adventure.text.format.TextDecoration

fun solidLine(prefix: String = "", suffix: String = "\n") = text(
    "$prefix                                                                 $suffix",
    DARK_GRAY,
    TextDecoration.STRIKETHROUGH
)

fun startLine() = solidLine()
fun endLine() = solidLine(prefix = "\n", suffix = "")

fun TextComponent.Builder.appendStartLine() = append(startLine())
fun TextComponent.Builder.appendEndLine() = append(endLine())

fun Component.boxed() = text()
    .appendStartLine()
    .append(this)
    .appendEndLine()


fun status(status: Status, text: String): Component =
    text("[${status.name}]: ", status.color)
    .append(
        text(text, GRAY)
    )

fun textPrimary(text: String) = text(text).colorPrimary()
fun textSecondary(text: String) = text(text).colorSecondary()
fun textDark(text: String) = text(text).colorDark()

fun TextComponent.appendStatus(status: Status, text: String) = append(status(status, text))
fun TextComponent.appendPrimary(text: String) = append(text(text).colorPrimary().removeStrikethrough())
fun TextComponent.appendSecondary(text: String) = append(text(text).colorSecondary().removeStrikethrough())
fun TextComponent.appendDark(text: String) = append(text(text).colorDark().removeStrikethrough())

fun TextComponent.colorPrimary() = color(AQUA)
fun TextComponent.colorSecondary() = color(GRAY)
fun TextComponent.colorDark() = color(DARK_GRAY)

fun TextComponent.suggestCommand(command: String) = clickEvent(ClickEvent.suggestCommand(command))

fun TextComponent.removeStrikethrough() = decoration(TextDecoration.STRIKETHROUGH, false)

fun TextComponent.bold() = decoration(TextDecoration.BOLD, true)

enum class Status(val color: NamedTextColor) {
    SUCCESS(GREEN),
    FAILURE(RED),
    WARNING(YELLOW),
    INFO(GRAY),
}