package gg.flyte.pluginportal.plugin.chat

import gg.flyte.pluginportal.common.types.LocalPlugin
import gg.flyte.pluginportal.common.types.Plugin
import gg.flyte.pluginportal.plugin.util.format
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.*
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import java.util.concurrent.atomic.AtomicInteger
import kotlin.jvm.optionals.getOrDefault
import kotlin.jvm.optionals.getOrElse
import kotlin.jvm.optionals.getOrNull

fun solidLine(prefix: String = "", suffix: String = "\n") = text(
    "$prefix                                                                                $suffix",
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


fun status(status: Status, text: String): TextComponent = text("[${status.name}]: ", status.color)
    .append(text(text, GRAY))

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
fun TextComponent.showOnHover(text: String, color: NamedTextColor = WHITE) =
    hoverEvent(HoverEvent.showText(text(text, color)))

fun TextComponent.removeStrikethrough() = decoration(TextDecoration.STRIKETHROUGH, false)

fun TextComponent.bold() = decoration(TextDecoration.BOLD, true)

enum class Status(val color: NamedTextColor) {
    SUCCESS(GREEN),
    FAILURE(RED),
    WARNING(YELLOW),
    INFO(GRAY),
}

fun Audience.sendFailure(msg: String) = send(Status.FAILURE, msg)
fun Audience.sendInfo(msg: String) = send(Status.INFO, msg)
fun Audience.sendSuccess(msg: String) = send(Status.SUCCESS, msg)

private fun Audience.send(status: Status, msg: String) = sendMessage(status(status, msg).boxed())


private fun Audience.isConsole() = get(Identity.UUID).isEmpty && "CONSOLE" == get(Identity.NAME).getOrDefault("")
fun sendPluginListMessage(audience: Audience, message: String, plugins: List<Plugin>, command: String) {
    audience.sendMessage(startLine().appendSecondary(message).appendNewline())
    plugins.take(16).forEach { plugin ->
        var platformSuffix = textDark(" (")

        plugin.platforms.keys.forEachIndexed { index, platform ->
            platformSuffix = platformSuffix.append(
                textDark(platform.name)
                    .hoverEvent(text("Click to $command with ${platform.name}"))
                    .suggestCommand(
                        "/pp $command \"${plugin.platforms[platform]!!.id}\" $platform --byId"
                    )
            )

            if (audience.isConsole()) platformSuffix = platformSuffix.appendDark(":${plugin.platforms[platform]!!.id}")

            if (index != plugin.platforms.size - 1) {
                platformSuffix = platformSuffix.appendDark(", ")
            }
        }

        val platform = plugin.highestPriorityPlatform
        val name = plugin.name.shortenToLine(23 + plugin.totalDownloads.format().pixelLength() + plugin.platformString.pixelLength())
        audience.sendMessage(
            textSecondary(" - ").appendPrimary("$name - ${plugin.totalDownloads.format()}")
                .append(text("⬇", AQUA, TextDecoration.UNDERLINED))
                .hoverEvent(
                    text("Click to $command ${plugin.name}", AQUA).appendNewline()
                        .append(text(plugin.getDescription() ?: "", GRAY))
                ).suggestCommand("/pp $command \"${plugin.platforms[platform]!!.id}\" $platform --byId")
                .append(platformSuffix.appendDark(")"))
        )
    }
    audience.sendMessage(solidLine("", ""))
}

fun sendLocalPluginListMessage(audience: Audience, message: String, plugins: List<LocalPlugin>, command: String) {
    audience.sendMessage(startLine().appendSecondary(message).appendNewline())
    plugins.forEach { plugin ->
        val platformSuffix = textDark(" (${plugin.platform.name})")

        audience.sendMessage(
            textSecondary(" - ").appendPrimary(plugin.name)
                .hoverEvent(text("Click to $command ${plugin.name} from ${plugin.platform.name}"))
                .suggestCommand("/pp $command ${plugin.platformId} ${plugin.platform} --byId")
                .append(platformSuffix)
        )
    }
    audience.sendMessage(solidLine("", ""))
}

val miniMessage = MiniMessage.builder().strict(true).build()

fun centerComponentLine(component: Component, maxLength: Int = 240) =
    centerMessage(miniMessage.serialize(component), maxLength)

private const val MAX_LINE_LENGTH = 240

fun centerMessage(message: String, maxLength: Int = MAX_LINE_LENGTH): Component {
    val boldCharacters = getBoldCharacters(message)
    val boldCharactersLength = boldCharacters.sumOf { it.pixelLength(true) }
    val boldCharactersNonBoldLength = boldCharacters.pixelLength()

    val messageWidth = message.withoutTags.pixelLength() - boldCharactersNonBoldLength + boldCharactersLength

    if (messageWidth > maxLength) {
        // TODO - Should be handled prior to calling this method.
    }

    val paddingWidth = ((maxLength - messageWidth) / 2).coerceAtLeast(0)
    val spaceWidth = DefaultFontInfo.SPACE.length
    val padding = " ".repeat(paddingWidth / spaceWidth)

    return MiniMessage.miniMessage().deserialize("$padding$message")
}
// TODO: Matches all abc in '<bold> a </bold> b <bold> c </bold>'
private val BOLD_TAG_TEXT_REGEX = "(?<=<bold>)(.+)(?=</bold>)".toRegex()

fun getBoldCharacters(input: String) = miniMessage.stripTags(BOLD_TAG_TEXT_REGEX.findAll(input)
    .map { it.value }
    .joinToString(""))
    .replace(" ", "")

private val String.withoutTags get() = miniMessage.stripTags(this)

fun String.shortenToLine(pixelsAlreadyInLine: Int, bold: Boolean = false) = if (pixelsAlreadyInLine + pixelLength(bold) <= MAX_LINE_LENGTH) this
else AtomicInteger(0).let { i -> takeWhile { i.addAndGet(it.pixelLength(bold)) <= MAX_LINE_LENGTH - pixelsAlreadyInLine - 3 } } + "..."

fun Char.pixelLength(bold: Boolean = false) = DefaultFontInfo.getDefaultFontInfo(this).let { if (bold) it.getBoldLength() else it.length }
fun String.pixelLength(bold: Boolean = false) = sumOf { it.pixelLength(bold) }

val Plugin.platformString: String get() = platforms.keys.joinToString(", ", "(", ")")