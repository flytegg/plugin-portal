package gg.flyte.pluginportal.common.chat

import gg.flyte.pluginportal.common.Config
import gg.flyte.pluginportal.common.types.LocalPlugin
import gg.flyte.pluginportal.common.types.Plugin
import gg.flyte.pluginportal.common.util.format
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.*
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import java.util.concurrent.atomic.AtomicInteger
import kotlin.jvm.optionals.getOrDefault

fun solidLine(prefix: String = "", suffix: String = "\n") = text(
    "$prefix                                                                                $suffix",
    DARK_GRAY,
    TextDecoration.STRIKETHROUGH
)

fun startLine() = solidLine()
fun endLine() = solidLine(prefix = "\n", suffix = "")

fun TextComponent.Builder.appendStartLine() = append(startLine())
fun TextComponent.Builder.appendEndLine() = append(endLine())

fun Component.boxed(): Component = Component.empty()
    .append(startLine())
    .append(this)
    .append(endLine())


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

fun TextComponent.hyperlink(colour: TextColor = AQUA) = color(colour).decoration(TextDecoration.UNDERLINED, true)

enum class Status(val color: NamedTextColor) {
    SUCCESS(GREEN),
    FAILURE(RED),
    WARNING(YELLOW),
    INFO(GRAY),
}

fun Audience.sendFailure(msg: String) = send(Status.FAILURE, msg)
fun Audience.sendInfo(msg: String) = send(Status.INFO, msg)
fun Audience.sendSuccess(msg: String) = send(Status.SUCCESS, msg)

fun progress(action: String, target: String): Component =
    textSecondary("$action ").appendPrimary(target).appendSecondary("...")

fun Audience.sendUnAuthed() = sendMessage(
    status(Status.FAILURE, "You are not authenticated. ")
        .appendSecondary("Join our ")
        .append(
            text("Discord", AQUA, TextDecoration.UNDERLINED)
                .clickEvent(ClickEvent.openUrl("https://flyte.gg/discord"))
                .hoverEvent(HoverEvent.showText(text("Click to join our Discord")))
        )
        .appendSecondary(" for support")
        .boxed()
)

private fun Audience.send(status: Status, msg: String) = sendMessage(status(status, msg).boxed())


fun Audience.isConsole() = get(Identity.UUID).isEmpty && "CONSOLE" == get(Identity.NAME).getOrDefault("")
fun sendPluginListMessage(audience: Audience, message: String, plugins: List<Plugin>, command: String, commandSuffix: String = "") {
    audience.sendMessage(startLine().appendSecondary(message).appendNewline())
    if (audience.isConsole()) {
        plugins.forEach { plugin ->
            audience.sendMessage(textPrimary(plugin.name))
            plugin.platforms.asList().filter { Config.isDownloadPlatformEnabled(it.platform) }.forEach { entry ->
                audience.sendMessage(textSecondary("  /pp $command \"${entry.platformId}\" ${entry.platform}$commandSuffix --byId"))
            }
        }
        return
    }
    if (plugins.size > 16) audience.sendInfo("Showing 16 of ${plugins.size} matches. Use /pp search <name> to page through results, or use a more specific name.")
    plugins.take(16).forEach { plugin ->
        var platformSuffix = textDark(" (")

        val downloadablePlatforms = plugin.platforms.asList()
            .filter { Config.isDownloadPlatformEnabled(it.platform) }

        downloadablePlatforms.forEachIndexed { index, platformPlugin ->
            val platform = platformPlugin.platform
            platformSuffix = platformSuffix.append(
                textDark(platform.name)
                    .hoverEvent(text("Click to $command with ${platform.name}"))
                    .suggestCommand(
                        "/pp $command \"${platformPlugin.platformId}\" $platform$commandSuffix --byId"
                    )
            )

            if (audience.isConsole()) platformSuffix = platformSuffix.appendDark(":${platformPlugin.platformId}")

            if (index != downloadablePlatforms.size - 1) {
                platformSuffix = platformSuffix.appendDark(", ")
            }
        }

        val platform = plugin.bestPlatform ?: return@forEach
        val name = plugin.name.shortenToLine(
            23 + plugin.totalDownloads.format().pixelLength() + plugin.platformString.pixelLength()
        )
        audience.sendMessage(
            textSecondary(" - ").appendPrimary("$name - ${plugin.totalDownloads.format()}")
                .append(text("⬇", AQUA, TextDecoration.UNDERLINED))
                .hoverEvent(
                    text("Click to $command ${plugin.name}", AQUA).appendNewline()
                        .append(text(plugin.sanitisedDescription ?: "", GRAY))
                ).suggestCommand("/pp $command \"${plugin.platform(platform)?.platformId}\" $platform$commandSuffix --byId")
                .append(platformSuffix.appendDark(")"))
        )
    }
    audience.sendMessage(solidLine("", ""))
}

fun sendPluginSearchResultsMessage(audience: Audience, query: String, plugins: List<Plugin>, page: Int? = null, full: Boolean = false, command: String = "/pp search") {
    val duplicateNames = plugins
        .groupingBy { it.name.lowercase() }
        .eachCount()
        .filterValues { it > 1 }
        .keys

    val rows = plugins.mapNotNull { plugin ->
        val viewPlatform = plugin.platforms.best ?: return@mapNotNull null
        val installPlatform = plugin.platforms.bestDownloadable
        val platformSummary = plugin.platforms.available.joinToString(", ") { it.name }
        val hover = text(plugin.name, AQUA).appendNewline()
            .append(text("${plugin.totalDownloads.format()} downloads · $platformSummary", GRAY))
            .appendNewline()
            .append(text(plugin.sanitisedDescription ?: "No description available", DARK_GRAY))

        var row = textSecondary(" - ")
            .append(textPrimary(plugin.name).hoverEvent(HoverEvent.showText(hover)))

        if (plugin.name.lowercase() in duplicateNames) {
            row = row.appendDark(" (${viewPlatform.platform.name.lowercase()}:${viewPlatform.platformId})")
        }

        row = row.appendSecondary("  ").append(
            textPrimary("View")
                .hyperlink()
                .showOnHover("View ${plugin.name}")
                .suggestCommand("/pp view \"${viewPlatform.platformId}\" ${viewPlatform.platform} --byId")
        )

        if (installPlatform != null) {
            row = row.appendSecondary(" | ").append(
                textPrimary("Install")
                    .hyperlink()
                    .showOnHover("Install ${plugin.name} from ${installPlatform.platform.name}")
                    .suggestCommand("/pp install \"${installPlatform.platformId}\" ${installPlatform.platform} --byId")
            )
        }

        if (audience.isConsole()) {
            row = textPrimary(plugin.name).appendSecondary(" ($platformSummary)")
                .appendNewline().appendSecondary("  /pp view \"${viewPlatform.platformId}\" ${viewPlatform.platform} --byId")
            if (installPlatform != null) row = row.appendNewline()
                .appendSecondary("  /pp install \"${installPlatform.platformId}\" ${installPlatform.platform} --byId")
        }
        "Search results for \"$query\"" to { row }
    }
    sendPagedRows(audience, rows, page, full, command)
}

fun sendLocalPluginListMessage(audience: Audience, message: String, plugins: List<LocalPlugin>, command: String, commandSuffix: String = "") {
    audience.sendMessage(startLine().appendSecondary(message).appendNewline())
    plugins.forEach { plugin ->
        val platformSuffix = textDark(" (${plugin.platform.name})")
        if (audience.isConsole()) {
            audience.sendMessage(textPrimary(plugin.name).append(platformSuffix)
                .appendNewline().appendSecondary("  /pp $command \"${plugin.platformId}\"$commandSuffix --byId"))
            return@forEach
        }

        audience.sendMessage(
            textSecondary(" - ").appendPrimary(plugin.name)
                .hoverEvent(text("Click to $command ${plugin.name} from ${plugin.platform.name}"))
                .suggestCommand("/pp $command \"${plugin.platformId}\"$commandSuffix --byId")
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

    val totalPadding = (maxLength - messageWidth).coerceAtLeast(0)
    val leftPadding = totalPadding / 2
    val spaceWidth = DefaultFontInfo.SPACE.length
    val padding = " ".repeat(leftPadding / spaceWidth)

    return MiniMessage.miniMessage().deserialize("$padding$message")
}

// TODO: Matches all abc in '<bold> a </bold> b <bold> c </bold>'
private val BOLD_TAG_TEXT_REGEX = "(?<=<bold>)(.+)(?=</bold>)".toRegex()

fun getBoldCharacters(input: String) = miniMessage.stripTags(
    BOLD_TAG_TEXT_REGEX.findAll(input)
        .map { it.value }
        .joinToString(""))
    .replace(" ", "")

private val String.withoutTags get() = miniMessage.stripTags(this)

fun String.shortenToLine(pixelsAlreadyInLine: Int, bold: Boolean = false) =
    if (pixelsAlreadyInLine + pixelLength(bold) <= MAX_LINE_LENGTH) this
    else AtomicInteger(0).let { i -> takeWhile { i.addAndGet(it.pixelLength(bold)) <= MAX_LINE_LENGTH - pixelsAlreadyInLine - 3 } } + "..."

fun Char.pixelLength(bold: Boolean = false) =
    DefaultFontInfo.getDefaultFontInfo(this).let { if (bold) it.getBoldLength() else it.length }

fun String.pixelLength(bold: Boolean = false) = sumOf { it.pixelLength(bold) }

val Plugin.platformString: String get() = platforms.available.joinToString(", ", "(", ")")

/** Paginate chat output; console output stays complete unless a page is requested. */
fun sendPagedRows(
    audience: Audience,
    rows: List<Pair<String, () -> Component>>,
    page: Int?,
    full: Boolean,
    command: String,
) {
    val pageSize = 8
    val pages = maxOf(1, (rows.size + pageSize - 1) / pageSize)
    val selectedPage = page ?: 1
    if (selectedPage !in 1..pages) return audience.sendFailure("Choose a page from 1 to $pages")
    val paginated = !full && (!audience.isConsole() || page != null)
    val visible = if (paginated) rows.drop((selectedPage - 1) * pageSize).take(pageSize) else rows
    var message = Component.empty()
    var section: String? = null
    for ((title, render) in visible) {
        if (title != section) {
            if (section != null) message = message.appendNewline().appendNewline()
            message = message.append(textPrimary(title).bold())
            section = title
        }
        message = message.appendNewline().append(render())
    }
    if (paginated && pages > 1) {
        message = message.appendNewline().appendNewline().append(textSecondary("Page $selectedPage of $pages"))
        if (selectedPage > 1) message = message.append(textPrimary("  [Previous]")
            .clickEvent(ClickEvent.runCommand("$command --page ${selectedPage - 1}")))
        if (selectedPage < pages) message = message.append(textPrimary("  [Next]")
            .clickEvent(ClickEvent.runCommand("$command --page ${selectedPage + 1}")))
        message = message.appendNewline().append(textDark("$command --page <number> | --full"))
    }
    audience.sendMessage(message.boxed())
}
