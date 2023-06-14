package link.portalbox.pluginportal.util

import gg.flyte.pplib.type.plugin.MarketplacePlugin
import link.portalbox.pluginportal.type.language.Message
import link.portalbox.pluginportal.type.language.Message.deserialize
import link.portalbox.pluginportal.type.language.Message.fillInVariables
import gg.flyte.pplib.util.isDirectDownload
import gg.flyte.pplib.util.requestPlugin
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.command.CommandSender
import java.awt.Color
import java.awt.image.BufferedImage
import java.net.URL
import javax.imageio.ImageIO
import kotlin.math.roundToInt

fun sendPreview(plugin: MarketplacePlugin, audience: Audience, commandSender: CommandSender) {
    val price = if (plugin.isPremium) "$${plugin.price}" else "Free"
    val descriptionComponents = createDescriptionLines(plugin.description)
    val text = mutableListOf(
        displayInformation("<gray>┌ <aqua><bold>${plugin.name}"),
        displayInformation(
            "<gray>├─ <aqua>${plugin.downloads} <u><bold>⬇</bold></u> <gray>| <aqua>${plugin.ratingAverage}<gold> ⭐ <gray>| <aqua>${price}"
            , 100
        ),
        *descriptionComponents
    )

    text.add(Component.text(" "))
    text.addAll(createButton(plugin, commandSender))

    val imageGrid = fetchImageAsBuffer(plugin.iconURL)?.let { createImageGrid(it, 11, 13) } ?: emptyArray()

    audience.sendMessage(Message.blankStrikeThrough)
    imageGrid.forEach { row ->
        val rowComponent = Component.text()

        row.forEach { gridSquare ->
            rowComponent.append(
                Component.text()
                    .color(TextColor.color(getAverageColor(gridSquare).rgb))
                    .content("▉")
            )
        }

        text.getOrNull(imageGrid.indexOf(row))?.let { rowComponent.append(it) }
        audience.sendMessage(rowComponent)
    }
    audience.sendMessage(Message.blankStrikeThrough)
}

fun createButton(plugin: MarketplacePlugin, commandSender: CommandSender): List<Component> {
    val hoverText = when (plugin.isPremium) {
        false -> "<aqua>Click to Download"
        true -> when (isDirectDownload(plugin.downloadURL)) {
            false -> "<dark_red>This plugin is external. Click to view the plugin online."
            true -> "<dark_red>We are unable to download paid plugins. Click to view the plugin online."
        }
    }

    val downloadable = !(plugin.downloadURL.isEmpty() || plugin.isPremium)

    val clickString = when (downloadable) {
        false -> "https://www.spigotmc.org/resources/${plugin.id}"
        true -> "/pp install ${plugin.name.replace(" ", "")}"
    }

    if (!downloadable && !plugin.isPremium) {
        requestPlugin(plugin.toRequestPlugin("External Download URL", commandSender.name))
    }

    val button = when (plugin.isPremium) {
        false -> listOf(
            "<aqua><bold>┌──────┐", "<aqua><bold>│ Download │", "<aqua><bold>└──────┘"
        )

        true -> listOf(
            "<gold><bold>┌─────┐", "<gold><bold>│   Buy   │", "<gold><bold>└─────┘"
        )
    }

    return button.map { line ->
        if (downloadable) {
            Message.runCommandPreviewFormatButton.fillInVariables(arrayOf(clickString, hoverText, line))
        } else {
            Message.openUrlPreviewFormatButton.fillInVariables(arrayOf(clickString, hoverText, line))
        }
    }
}

fun createDescriptionLines(description: String): Array<Component> {
    val descriptionLines = description.chunked(35)
    if (descriptionLines.size > 3) {
        return arrayOf("<gray>$description".deserialize())
    }

    return descriptionLines.map { "<gray>│ $it".deserialize() }.toTypedArray()
}

/**
 * Creates a grid of images from the given image.
 *
 * @param image The BufferImage to create a grid from.
 * @param rows The number of rows to create in the grid.
 * @param cols The number of columns to create in the grid.
 * @return A 2D array of BufferedImages.
 */
fun createImageGrid(image: BufferedImage, rows: Int, cols: Int): Array<Array<BufferedImage>> {
    val blackedImage = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_RGB)
    val graphics = blackedImage.createGraphics()
    graphics.drawImage(image, 0, 0, null)
    graphics.dispose()

    val chunkWidth = image.width / cols
    val chunkHeight = image.height / rows
    val chunks = Array(rows) { Array(cols) { BufferedImage(chunkWidth, chunkHeight, BufferedImage.TYPE_INT_RGB) } }

    for (row in 0 until rows) {
        for (col in 0 until cols) {
            chunks[row][col] = blackedImage.getSubimage(
                col * chunkWidth, row * chunkHeight, chunkWidth, chunkHeight
            )
        }
    }
    return chunks
}

/**
 * Gets the average color of the given image.
 *
 * @param image The BufferedImage to get the average color of.
 * @return The average color of the image.
 */
fun getAverageColor(image: BufferedImage): Color {
    val step = 5
    var sampled = 0
    var sumR: Long = 0
    var sumG: Long = 0
    var sumB: Long = 0
    for (x in 0 until image.width) {
        for (y in 0 until image.height) {
            if (x % step == 0 && y % step == 0) {
                val pixel = Color(image.getRGB(x, y))
                sumR += pixel.red.toLong()
                sumG += pixel.green.toLong()
                sumB += pixel.blue.toLong()
                sampled++
            }
        }
    }
    return Color(
        (sumR / sampled).toFloat().roundToInt(),
        (sumG / sampled).toFloat().roundToInt(),
        (sumB / sampled).toFloat().roundToInt()
    )
}

/**
 * Creates a TextComponent with the given string and colors it and truncates it if it is too long with a hover event.
 *
 * @param text The string to create the TextComponent from.
 * @return The TextComponent
 */
fun displayInformation(text: String, length: Int = 45): Component {
    if (text.length < length+5) {
        return "<gray>$text</gray>".deserialize()
    }

    return "<hover:show_text:'${text}'>${text.substring(0, length)} <dark_gray>[...]</hover>".deserialize()
}

/**
 * Fetches an image from the given URL and returns it as a BufferedImage.
 *
 * @param imageUrl The URL of the image to fetch.
 * @return The image as a BufferedImage, or null if the image could not be fetched.
 */
fun fetchImageAsBuffer(imageUrl: String): BufferedImage? {
    return ImageIO.read(URL(imageUrl))
}