package gg.flyte.pluginportal.plugin.commands.recognize

import gg.flyte.pluginportal.plugin.PluginPortal
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.util.jar.JarEntry
import java.util.jar.JarFile

object Recognize {

    private val logger get() = PluginPortal.instance.logger

    /** @return the processed content of the polymart.yml inside a jar, if it exists or null */
    fun getPolymartData(file: File): PolymartFileData? = runCatching {
        JarFile(file).use { jar ->
            val polymartEntry: JarEntry = jar.getJarEntry("polymart.yml") ?: return null
            jar.getInputStream(polymartEntry).use { stream ->
                Yaml().load<Map<String, Any>>(stream.reader())
            }
        }
    }.onFailure {
        logger.warning("Failed to read potential Polymart data from ${file.name}: ${it.message ?: it::class.simpleName}")
    }.getOrNull()?.let { raw ->
        val parsed = parsePolymartData(raw)
        if (parsed == null) logger.severe("Found polymart.yml inside of the jar (${file.name}) but could not read it, please report this as a bug")
        return@let parsed
    }


    private fun parsePolymartData(raw: Map<String, Any>): PolymartFileData? = runCatching {
        val polymartSection = raw["polymart"] as? Map<String, Any>

        val product = polymartSection?.get("product") as? Map<String, Any> ?: return null
        val upload = polymartSection["upload"] as? Map<String, Any>

        val productId = product["id"]?.toString() ?: return null
        val title = product["title"]?.toString() ?: return null
        val version = upload?.get("version")?.toString() ?: "Unknown"

        return@runCatching PolymartFileData(productId, title, version)
    }.onFailure {
        logger.warning("Failed to parse polymart.yml: ${it.message ?: it::class.simpleName}")
    }.getOrNull()

}
