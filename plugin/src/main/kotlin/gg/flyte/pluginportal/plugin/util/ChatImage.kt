package gg.flyte.pluginportal.plugin.util

import gg.flyte.pluginportal.common.types.Plugin
import gg.flyte.pluginportal.plugin.chat.bold
import gg.flyte.pluginportal.plugin.chat.showOnHover
import gg.flyte.pluginportal.plugin.chat.textPrimary
import gg.flyte.pluginportal.plugin.chat.textSecondary
import gg.flyte.pluginportal.plugin.manager.LocalPluginCache
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.minimessage.MiniMessage
import java.awt.Color
import java.awt.image.BufferedImage
import java.net.URI
import javax.imageio.ImageIO

object ChatImage {

    private fun getImage(imageURL: String): BufferedImage =
        ImageIO.read(URI(imageURL).toURL())

    private fun imageToChat(image: BufferedImage): String {
        val sb = StringBuilder()

        val grid = createImageGrid(image, 12, 12)

        for (row in grid) {
            for (cell in row) {
                val color = getAverageColor(cell)
                val chatColor = String.format("<#%02x%02x%02x>", color.red, color.green, color.blue)
                sb.append(chatColor).append("█")
            }
            sb.append("\n")
        }

        return sb.toString().removeSuffix("\n")
    }

    fun getImageLines(imageURL: String) = imageToChat(
        getImage(imageURL)
    )
        .split("\n")
        .toMutableList()

    class ImageTextBuilder(imageURL: String) {

        private val originalLines = getImageLines(imageURL)
            .map { translate(it) }

        private val lines = originalLines
            .toMutableList()

        fun setLine(line: Int, text: Component): ImageTextBuilder {
            lines[line] = originalLines[line].append(Component.space()).append(text)
            return this
        }

        fun appendLine(line: Int, text: Component): ImageTextBuilder {
            lines[line].append(text)
            return this
        }

        fun prependLine(line: Int, text: Component): ImageTextBuilder {
            lines[line] = text.append(lines[line])
            return this
        }

        fun getFullLine(index: Int) = lines[index]
        fun getImageLine(index: Int) = originalLines[index]

        fun build(): Component {
            var component = Component.empty()
            lines.forEachIndexed { index, line ->
                component = component.append(line)
                if (index != lines.size - 1) component = component.append(Component.newline())
            }
            return component
        }

    }

}

/**
 * Gets the average color of the given image.
 *
 * @param image The BufferedImage to get the average color of.
 * @return The average color of the image.
 */
fun getAverageColor(image: BufferedImage): Color {
    var sampled = 0
    var sumR: Long = 0
    var sumG: Long = 0
    var sumB: Long = 0
    for (x in 0 until image.width) {
        for (y in 0 until image.height) {
            val pixel = Color(image.getRGB(x, y))
            sumR += pixel.red.toLong()
            sumG += pixel.green.toLong()
            sumB += pixel.blue.toLong()
            sampled++
        }
    }
    return Color(
        (sumR / sampled).toInt(),
        (sumG / sampled).toInt(),
        (sumB / sampled).toInt()
    )
}


/**
 * Creates a grid of images from the given image.
 *
 * @param image The BufferedImage to create a grid from.
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

private const val MAX_DESCRIPTION_LINES = 5

fun Plugin.getImageComponent(): Component {
    val description = splitDescriptionIntoLines(getDescription() ?: "", 35, MAX_DESCRIPTION_LINES)
    val installed = LocalPluginCache.hasPlugin(id)
    val image = ChatImage.ImageTextBuilder(getImageURL() ?: "")
        .setLine(0, textPrimary(name).bold())
        .apply {
            description.forEachIndexed { index, line -> setLine(index + 2, textSecondary(line).showOnHover(getDescription() ?: "")) }
        }
        .setLine(description.size + 3, textSecondary("Downloads: ${getDownloads().format()}"))
        .setLine(description.size + 4, textSecondary("Platforms: ${platforms.keys.joinToString()}"))
        .setLine(11, text("      ").let {
            if (installed) it.append(SharedComponents.getUpdateButton(name)).append(text( " "))
            else it.append(text("    "))
        }.append(SharedComponents.getInstallButton(name, installed)))
        .build()
    return image
}



private fun splitDescriptionIntoLines(description: String, maxLineLength: Int, maxLines: Int): List<String> {
    val words = description.split(" ")
    val lines = mutableListOf<String>()
    var currentLine = StringBuilder()

    for (word in words) {
        if (currentLine.length + word.length + 1 > maxLineLength) {
            lines.add(currentLine.toString())

            if (lines.size == maxLines) { // Enforce max line count.
                lines[maxLines-1] = currentLine.append("...").toString()
                currentLine.clear()
                break
            }

            currentLine = StringBuilder()
        }
        if (currentLine.isNotEmpty()) {
            currentLine.append(" ")
        }
        currentLine.append(word)
    }
    if (currentLine.isNotEmpty()) {
        lines.add(currentLine.toString())
    }
    return lines
}

fun translate(text: String) = MiniMessage.miniMessage().deserialize(text)