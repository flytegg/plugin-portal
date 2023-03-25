import link.portalbox.pluginportal.util.coloredComponent
import link.portalbox.pplib.type.SpigetPlugin
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
import java.util.*
import javax.imageio.ImageIO


object PreviewUtil {
  /* This awful class still needs converted. */
  fun sendPreview(player: CommandSender, spigetPlugin: SpigetPlugin, containDownloadPrompt: Boolean) {
    player.sendMessage(
      ChatColor.translateAlternateColorCodes(
        '&', "&8&m                                                       "
      )
    )
    val marketplacePlugin = spigetPlugin.marketplacePlugin
    val downloadUrl =
      if (spigetPlugin.externalUrl == null) "https://api.spiget.org/v2/resources/" + spigetPlugin.id + "/download" else spigetPlugin.externalUrl
    val informationAsComponents = ArrayList<TextComponent?>()
    try {
      informationAsComponents.addAll(
        listOf(
          " &7Name: &b${spigetPlugin.name}".coloredComponent(),
          " &7Description: &b&l[Hover Here]".coloredComponent().apply {
            hoverEvent = HoverEvent(
              HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("${ChatColor.AQUA} ${spigetPlugin.description}")
            )
          },
          " &7Downloads: &b${String.format("%,d", spigetPlugin.downloads)}".coloredComponent(),
          " &7Rating: &b${spigetPlugin.rating}&e ⭐".coloredComponent(),
          " &7Premium: &b${if (spigetPlugin.premium) "Yes (${spigetPlugin.price})" else "No"}".coloredComponent(),
          " &7Last Updated: &b${formatDate(spigetPlugin.updateDate * 1000L)}".coloredComponent(),
        )
      )
      if (!downloadUrl.contains("api.spiget.org")) {
        val label = TextComponent(ChatColor.translateAlternateColorCodes('&', " &7External Link: &b"))
        val link = TextComponent(ChatColor.translateAlternateColorCodes('&', "&b&l[Click Here]")).apply {
          clickEvent = ClickEvent(ClickEvent.Action.OPEN_URL, downloadUrl)
          hoverEvent = HoverEvent(
            HoverEvent.Action.SHOW_TEXT,
            TextComponent.fromLegacyText(ChatColor.AQUA.toString() + "Click to open the external download link")
          )
        }
        label.addExtra(link)
        informationAsComponents.add(label)
      }
      informationAsComponents.add(TextComponent(" "))
      informationAsComponents.add(" &b&l[Click to Download]".coloredComponent().apply {
        clickEvent = ClickEvent(ClickEvent.Action.OPEN_URL, downloadUrl)
        hoverEvent = HoverEvent(
          HoverEvent.Action.SHOW_TEXT,
          TextComponent.fromLegacyText(ChatColor.AQUA.toString() + "Click to download the plugin")
        )
      })

    } catch (exception: Exception) {
      exception.printStackTrace()
      informationAsComponents.add(TextComponent(ChatColor.RED.toString() + " Error"))
    }

    val image = runCatching {
      val url =
        spigetPlugin.iconUrl.ifEmpty { "https://cdn.discordapp.com/emojis/1065698008815112302.webp?size=128&quality=lossless" }
      val imageUrl = URL(url)
      val connection = imageUrl.openConnection() as HttpURLConnection
      connection.setRequestProperty(
        "User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:37.0) Gecko/20100101 Firefox/37.0"
      )

      ImageIO.read(connection.inputStream).also { connection.inputStream.close() }
    }.getOrNull()

    val SQUARE_SIZE = 12
    val imageGrid = image?.let { createImageArray(image, SQUARE_SIZE, SQUARE_SIZE) } ?: emptyArray()

    val pre116 = Bukkit.getVersion().split("\\.".toRegex()).dropLastWhile { it.isEmpty() }[1].toInt() < 16
    for ((rowIndex, row) in imageGrid.withIndex()) {

      val rowComponent = row.fold(TextComponent()) { acc, gridSquare ->
        acc.addExtra(TextComponent("▉").apply {
          color = ChatColor.of(getAverageColor(gridSquare))
        })
        acc
      }

      informationAsComponents.getOrNull(rowIndex)?.let { rowComponent.addExtra(it) }

      if (pre116) {
        player.spigot().sendMessage(rowComponent)
      } else {
        player.sendMessage(rowComponent.toLegacyText())
      }

    }


    player.sendMessage(
      ChatColor.translateAlternateColorCodes(
        '&', "&8&m                                                       "
      )
    )
  }

  fun createImageArray(image: BufferedImage, rows: Int, cols: Int): Array<Array<BufferedImage?>> {
    val chunks = Array(rows) { arrayOfNulls<BufferedImage>(cols) }
    val chunkWidth = image.width / cols
    val chunkHeight = image.height / rows
    for (row in 0 until rows) {
      for (col in 0 until cols) {
        chunks[row][col] = image.getSubimage(
          col * chunkWidth, row * chunkHeight, chunkWidth, chunkHeight
        )
      }
    }
    return chunks
  }

  fun formatDate(millis: Long): String {
    val date: LocalDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()

    val dayOfMonth = date.dayOfMonth
    val dayOfMonthStr = when {
      dayOfMonth % 10 == 1 && dayOfMonth != 11 -> "st"
      dayOfMonth % 10 == 2 && dayOfMonth != 12 -> "nd"
      dayOfMonth % 10 == 3 && dayOfMonth != 13 -> "rd"
      else -> "th"
    }
    val monthStr = date.month.toString().toLowerCase().capitalize()
    val yearStr = date.year.toString()
    return "$dayOfMonth$dayOfMonthStr $monthStr $yearStr"
  }

  fun getVersionRange(versions: List<String>): String {
    val majorVersions: MutableList<Int> = ArrayList()
    val minorVersions: MutableList<Int> = ArrayList()
    for (version in versions) {
      val parts = version.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
      majorVersions.add(parts[0].toInt())
      minorVersions.add(parts[1].toInt())
    }
    val minMajor = Collections.min(majorVersions)
    val maxMajor = Collections.max(majorVersions)
    val minMinor = Collections.min(minorVersions)
    val maxMinor = Collections.max(minorVersions)
    val minVersion = String.format("%d.%d", minMajor, minMinor)
    val maxVersion = String.format("%d.%d", maxMajor, maxMinor)
    return String.format("%s-%s", minVersion, maxVersion)
  }

  private fun formatVersion(versionValue: Double): String {
    return String.format("%.1f", versionValue)
  }

  private fun getAverageColor(bi: BufferedImage?): Color {
    val step = 5
    var sampled = 0
    var sumr = 0L
    var sumg = 0L
    var sumb = 0L
    for (x in 0 until bi!!.width) {
      for (y in 0 until bi.height) {
        if (x % step != 0 || y % step != 0) continue
        val pixel = Color(bi.getRGB(x, y))
        sumr += pixel.red.toLong()
        sumg += pixel.green.toLong()
        sumb += pixel.blue.toLong()
        ++sampled
      }
    }
    return Color(
      Math.round((sumr / sampled.toLong()).toFloat()),
      Math.round((sumg / sampled.toLong()).toFloat()),
      Math.round((sumb / sampled.toLong()).toFloat())
    )
  }
}