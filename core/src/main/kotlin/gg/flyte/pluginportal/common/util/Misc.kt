package gg.flyte.pluginportal.common.util

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
fun File.getPluginYML(): Map<String, Any>? = runCatching {
    JarFile(this).use { jar ->
        val descriptor = listOf("plugin.yml", "paper-plugin.yml", "velocity-plugin.json")
            .firstNotNullOfOrNull { jar.getJarEntry(it) } ?: return null
        jar.getInputStream(descriptor).use { stream ->
            val text = stream.readNBytes(256 * 1024 + 1)
            require(text.size <= 256 * 1024) { "Plugin descriptor is too large" }
            if (descriptor.name.endsWith(".json")) {
                @Suppress("UNCHECKED_CAST")
                GSON.fromJson(String(text, Charsets.UTF_8), Map::class.java) as Map<String, Any>
            } else {
                Yaml(org.yaml.snakeyaml.constructor.SafeConstructor(org.yaml.snakeyaml.LoaderOptions()))
                    .load<Map<String, Any>>(String(text, Charsets.UTF_8))
            }
        }
    }
}.getOrNull()

fun File.requireCompatibleJar(serverTypes: List<gg.flyte.pluginportal.common.types.enums.ServerType>) {
    JarFile(this).use { jar ->
        val velocity = gg.flyte.pluginportal.common.types.enums.ServerType.VELOCITY in serverTypes
        val compatible = if (velocity) jar.getJarEntry("velocity-plugin.json") != null
        else jar.getJarEntry("plugin.yml") != null ||
            (serverTypes.any { it.platform == gg.flyte.pluginportal.common.types.enums.ServerPlatform.PAPER } && jar.getJarEntry("paper-plugin.yml") != null)
        require(compatible) { "Downloaded JAR is incompatible with ${serverTypes.first()}" }
        require(getPluginYML()?.get("version") != null) { "Downloaded JAR has no valid plugin descriptor" }
    }
}
/** @return true if the plugin.yml name is a Plugin Portal artifact. */
val File.isPluginPortal: Boolean get() = (getPluginYML()?.get("name") as? String)?.contains("PluginPortal") == true


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
