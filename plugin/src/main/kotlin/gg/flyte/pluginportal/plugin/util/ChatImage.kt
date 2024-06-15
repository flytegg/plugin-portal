package gg.flyte.pluginportal.plugin.util

import gg.flyte.pluginportal.common.types.Plugin
import net.kyori.adventure.text.Component
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
                val colour = getAverageColor(cell)
                val chatColour = String.format("<#%02x%02x%02x>", colour.red, colour.green, colour.blue)
                sb.append(chatColour).append("█")
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

fun translate(text: String) = MiniMessage.miniMessage().deserialize(text)