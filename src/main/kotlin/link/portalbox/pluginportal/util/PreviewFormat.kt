package link.portalbox.pluginportal.util

import link.portalbox.pluginportal.util.ChatColor.color
import link.portalbox.pluginportal.util.ChatColor.coloredComponent
import link.portalbox.pplib.type.MarketplacePlugin
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import java.awt.Color
import java.awt.image.BufferedImage
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.imageio.ImageIO
import kotlin.math.roundToInt

const val SEPARATOR = "&8&m                                                       "

fun sendPreview(player: CommandSender, plugin: MarketplacePlugin, containDownloadPrompt: Boolean) {
  val downloadUrl = plugin.downloadURL.toString() ?: "https://api.spiget.org/v2/resources/${plugin.id}/download"
  val above1_8 = getVersion(Bukkit.getVersion()) > GameVersion(1, 8, 8)

  val price = if (plugin.premium) "$${plugin.price}" else "Free"
  val descriptionComponents = createDescriptionLines(plugin.description)

  val information = mutableListOf(
    infoComp("┌ &b&l${plugin.name}"),
    infoComp(
      "├─ &b${
        String.format(
          "%,d", plugin.downloads
        )
      } &n&l⬇&r&7 | &b${plugin.rating}&e ⭐ &7| &b${price}"
    ),
    *descriptionComponents,
//    infoComp("Last Update: &b${formatDate(spigetPlugin.updateDate * 1000L)}"),
  )

  if (!downloadUrl.contains("api.spiget.org")) {
    val label = infoComp("&7External Link: &b")
    val link = infoComp("&b&l[Click Here]").apply {
      clickEvent = ClickEvent(ClickEvent.Action.OPEN_URL, downloadUrl)
      hoverEvent = HoverEvent(
        HoverEvent.Action.SHOW_TEXT,
        TextComponent.fromLegacyText(ChatColor.AQUA.toString() + "Click to open the external download link")
      )
    }
    label.addExtra(link)
    information.add(label)
  }
  information.add(TextComponent(" "))

  information.addAll(
    if (above1_8) createButton(plugin)
    else {
      listOf(
        infoComp("/pp install ${plugin.name.replace(" ", "-")}")
      )
    }
  )

  val image = fetchImageAsBuffer(plugin.iconUrl)

  val imageGrid = image?.let { createImageGrid(image, 11, 13) } ?: emptyArray()

  player.sendMessage(SEPARATOR.color())

  for ((rowIndex, row) in imageGrid.withIndex()) {
    val rowComponent = TextComponent()

    row.forEach { gridSquare ->
      rowComponent.addExtra(TextComponent("▉").apply {
        color = ChatColor.of(getAverageColor(gridSquare))
      })
    }

    information.getOrNull(rowIndex)?.let { rowComponent.addExtra(it) }

    val post1_16 = (getVersion(Bukkit.getVersion()).minor) >= 16
    if (post1_16) {
      player.spigot().sendMessage(rowComponent)
    } else {
      player.sendMessage(rowComponent.toLegacyText())
    }
  }

  player.sendMessage(SEPARATOR.color())
}

fun createDescriptionLines(description: String, showHover: Boolean = true): Array<TextComponent> {
  val descriptionLines = description.chunked(35)

  val descriptionComponents = if (descriptionLines.size > 3) {
    val hoverDesc = if (showHover) {
      HoverEvent(
        HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(
          "${ChatColor.AQUA}$description"
        )
      )
    } else null

    val comps = descriptionLines.subList(0, 2).map { " &7│ $it".coloredComponent().apply { hoverEvent = hoverDesc } }
      .toMutableList()
    comps.add(" &7│ ${descriptionLines[2]}...".coloredComponent().apply { hoverEvent = hoverDesc })
    comps
  } else {
    descriptionLines.map { " &7│ $it".coloredComponent() }
  }.toTypedArray()

  return descriptionComponents
}

fun createButton(plugin: MarketplacePlugin): List<TextComponent> {
  val hoverText = when (plugin.premium) {
    false -> "&bClick to Download"
    true -> "&4This plugin is paid so you cannot download it through Plugin Portal. Click to view the plugin on SpigotMC."
  }

  val onClick = when (plugin.premium) {
    false -> ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pp install ${plugin.name.replace("", "-")}")
    true -> ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/${plugin.id}")
  }

  val button = when (plugin.premium) {
    false -> listOf(
      "&b&l┌──────┐", "&b&l│ Download │", "&b&l└──────┘"
    )

    true -> listOf(
      "&e&l┌───&r&e──&l┐", "&e&l│   Buy   │", "&e&l└───&r&e──&l┘"
    )
  }

  return button.map { line ->
    " &b&l$line".coloredComponent().apply {
      clickEvent = onClick
      hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(hoverText.color()))
    }
  }
}

/**
 * Creates a TextComponent with the given string and colors it and truncates it if it is too long with a hover event.
 *
 * @param string The string to create the TextComponent from.
 * @return The TextComponent
 */
fun infoComp(string: String): TextComponent {
  val above1_8 = getVersion(Bukkit.getVersion()) > GameVersion(1, 8, 8)

  if (string.length < 50) return " &7$string".coloredComponent()

  return " &7${string.substring(0, 40)} &8[...]".coloredComponent().applyIf(above1_8) {
    hoverEvent = HoverEvent(
      HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(string.color())
    )
  }
}

/**
 * Fetches an image from the given URL and returns it as a BufferedImage.
 *
 * @param imageUrl The URL of the image to fetch.
 * @return The image as a BufferedImage, or null if the image could not be fetched.
 */
fun fetchImageAsBuffer(imageUrl: String): BufferedImage? {
  return runCatching {
    val url =
      URL(imageUrl.ifEmpty { "https://cdn.discordapp.com/emojis/1065698008815112302.webp?size=128&quality=lossless" })
    val connection = url.openConnection() as HttpURLConnection
    connection.setRequestProperty(
      "User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:37.0) Gecko/20100101 Firefox/37.0"
    )

    ImageIO.read(connection.inputStream).also { connection.inputStream.close() }
  }.getOrNull()
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

/**sg
 * Formats the given epoch millis into a date string.
 *
 * @param millis The epoch millis to format.
 * @return The formatted date string. *e.g. 1st January 2021*
 */
private fun formatDate(millis: Long): String {
  val date: LocalDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()

  val dayOfMonth = date.dayOfMonth
  val dayOfMonthStr = when {
    dayOfMonth % 10 == 1 && dayOfMonth != 11 -> "st"
    dayOfMonth % 10 == 2 && dayOfMonth != 12 -> "nd"
    dayOfMonth % 10 == 3 && dayOfMonth != 13 -> "rd"
    else -> "th"
  }
  val monthStr = date.month.toString().lowercase().replaceFirstChar { it.uppercase() }
  val yearStr = date.year.toString()
  return "$dayOfMonth$dayOfMonthStr $monthStr $yearStr"
}