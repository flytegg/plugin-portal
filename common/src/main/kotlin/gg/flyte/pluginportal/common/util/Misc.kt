package gg.flyte.pluginportal.common.util

import gg.flyte.pluginportal.common.Config
import gg.flyte.pluginportal.common.PluginPortalBase
import gg.flyte.pluginportal.common.types.LocalPlugin
import gg.flyte.pluginportal.common.types.enums.MarketplacePlatform
import gg.flyte.pluginportal.common.types.Plugin
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.text.DecimalFormat
import java.util.jar.JarEntry
import java.util.jar.JarFile

fun Int.format(): String = DecimalFormat.getIntegerInstance().format(this)

fun File.appendLine(text: String) = appendText(text + "\n")
fun File.createIfNotExists() = apply {
    parentFile?.mkdirs()
    if (!exists()) createNewFile()
}
fun File.getPluginYML() = runCatching {
    JarFile(this).use { jar ->
        val ymlEntry: JarEntry = jar.getJarEntry("plugin.yml") ?: return null
        jar.getInputStream(ymlEntry).use { stream ->
            Yaml().load<Map<String, Any>>(stream.reader())
        }
    }
}.getOrNull()
/** @return true if the plugin.yml name is a Plugin Portal artifact. */
val File.isPluginPortal: Boolean get() = (getPluginYML()?.get("name") as? String)?.contains("PluginPortal") == true

private fun File.readJarYml(entryName: String): Map<String, Any>? = runCatching {
    JarFile(this).use { jar ->
        val entry: JarEntry = jar.getJarEntry(entryName) ?: return@runCatching null
        jar.getInputStream(entry).use { stream ->
            Yaml().load<Map<String, Any>>(stream.reader())
        }
    }
}.getOrNull()

/** @return the name declared in plugin.yml or paper-plugin.yml, which is what users see in /pp list. */
val File.declaredPluginName: String?
    get() = ((readJarYml("plugin.yml") ?: readJarYml("paper-plugin.yml"))?.get("name") as? String)
        ?.trim()
        ?.takeIf(String::isNotEmpty)

/**
 * Matches against the file name first (cheap), then the declared plugin name. Plugin Portal renames installed jars to
 * "[PP] Name (PLATFORM).jar", so the declared name is the reliable fallback when file names don't line up.
 */
val File.isBlacklistedPlugin: Boolean
    get() = Config.isPluginNameBlacklisted(nameWithoutExtension) ||
        declaredPluginName?.let(Config::isPluginNameBlacklisted) == true


fun String.capitaliseFirst() = lowercase().replaceFirstChar(Char::uppercaseChar)


internal val PP_MODRINTH_ID = "5qkQnnWO"
internal val PP_PLUGIN_ID = "6881375644543c82da481311"
private val PP_PLATFORM_IDS = setOf(
    MarketplacePlatform.MODRINTH to PP_MODRINTH_ID,
    MarketplacePlatform.HANGAR to "PluginPortal",
    MarketplacePlatform.SPIGOTMC to "108700",
)
internal val Plugin.isPluginPortalMarketplaceEntry: Boolean get() =
    id == PP_PLUGIN_ID || PP_PLATFORM_IDS.any { (platform, platformId) -> platform(platform)?.platformId == platformId }
internal val LocalPlugin.isPluginPortal: Boolean get() = PP_PLATFORM_IDS.contains(platform to platformId)

internal val logger get() = PluginPortalBase.plugin.logger
