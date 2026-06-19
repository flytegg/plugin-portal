package gg.flyte.pluginportal.plugin.commands.recognize

import gg.flyte.pluginportal.common.API
import gg.flyte.pluginportal.common.Constants
import gg.flyte.pluginportal.common.Hash
import gg.flyte.pluginportal.common.PlatformId
import gg.flyte.pluginportal.common.chat.*
import gg.flyte.pluginportal.common.commands.lamp.EnabledCommand
import gg.flyte.pluginportal.common.commands.lamp.Features
import gg.flyte.pluginportal.common.managers.LocalPluginCache
import gg.flyte.pluginportal.common.managers.MarketplacePluginCache
import gg.flyte.pluginportal.common.types.LocalPlugin
import gg.flyte.pluginportal.common.types.enums.MarketplacePlatform
import gg.flyte.pluginportal.common.util.HashType
import gg.flyte.pluginportal.common.util.async
import gg.flyte.pluginportal.common.util.isJarFile
import gg.flyte.pluginportal.common.util.isPluginPortal
import gg.flyte.pluginportal.plugin.PluginPortal
import gg.flyte.pluginportal.plugin.commands.lamp.RequiresAuth
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.annotation.CommandPermission
import java.io.File

@Command("pp", "pluginportal", "ppm")
class RecognizeAllSubCommand {

    private val logger get() = PluginPortal.instance.logger
    private fun log(msg: String) = logger.info("[RECOGNIZE] $msg")
    private fun Audience.infoAndLog(msg: String) = sendInfo(msg).also { log(msg) }


    private class RecognitionInfo(val file: File, val polymartData: PolymartFileData?)

    private val File.hash: Hash get() = Hash(HashType.SHA256.hash(this), HashType.SHA512.hash(this))

    @RequiresAuth
    @EnabledCommand(Features.RECOGNISE)
    @Subcommand("recognizeAll")
    @CommandPermission("pluginportal.manage.recognize")
    fun recognizeAllCommand(audience: Audience) {
        async {
            val jars = Constants.INSTALL_DIRECTORY.listFiles()
                ?.filter(File::isJarFile)
                ?.associateBy(HashType.SHA256.hash)
                ?.filter { !LocalPluginCache.hasPluginByHash(it.key) && !it.value.isPluginPortal && !LocalPluginCache.hasManagedDownloadedFile(it.value) }
                ?.map { RecognitionInfo(it.value, Recognize.getPolymartData(it.value)) }
                ?.ifEmpty { return@async audience.sendSuccess("No plugins are unrecognized") }
                ?: return@async audience.sendFailure("Could not determine a plugin directory")

            audience.infoAndLog("Attempting to recognize ${jars.size} plugins...")

            val polymartPluginDatas = jars.filter { it.polymartData != null }
            val polymartPlugins = if (polymartPluginDatas.isNotEmpty()) {
                log("Fetching polymart plugin data...")
                polymartPluginDatas.map { PlatformId(it.polymartData!!.id, MarketplacePlatform.POLYMART) }.let { API.getAllPluginsByPlatformIds(it) }?.get(MarketplacePlatform.POLYMART)
            } else mapOf()

            val unrecognizedPlugins: MutableList<File> = mutableListOf()
            val recognizedPlugins: MutableList<LocalPlugin> = if (polymartPlugins == null || polymartPlugins.isEmpty()) mutableListOf() else polymartPluginDatas.map { info ->
                val data = info.polymartData!!
                val plugin = polymartPlugins[data.id]?.platform(MarketplacePlatform.POLYMART)
                    if (plugin == null) {
                        log("Could not detect a database entry for recognised polymart plugin ${data.title} (${data.id})")
                        unrecognizedPlugins.add(info.file)
                        return@map null
                    }
                log("Found Polymart plugin - ID: ${data.id}, Title: ${data.title}, Version: ${data.version}")
                data.getNewLocalPlugin(plugin.entryId, HashType.SHA256.hash(info.file), HashType.SHA512.hash(info.file)).also(LocalPluginCache::add)
            }.filterNotNull().toMutableList()

            log("Found ${recognizedPlugins.size} plugins from Polymart")

            val nonPolymart = jars.filter { it.polymartData == null }.map { it.file }.associateBy { it.hash }
            val nonPolymart256 = nonPolymart.mapKeys { it.key.sha256 }

            fun noRecognize(file: File) { unrecognizedPlugins.add(file) }
            API.recognizePlugins(nonPolymart.keys.toList())
                ?.map { (sha256, plugin) -> nonPolymart256[sha256]!! to plugin }
                ?.forEach { (file, recognitionResponse) ->
                    val (sha256, sha512) = file.hash
                    val plugin = recognitionResponse?.plugin
                    val ppl = plugin?.platforms?.best ?: return@forEach noRecognize(file)
                    val version = recognitionResponse.version ?: ppl.newestBukkitPaperVersion(null)?.versionNumber
                    if (version == null) {
                        log("Could not find a compatible Bukkit/Paper version for ${plugin.name} from ${file.name}")
                        return@forEach noRecognize(file)
                    }
                    val local = LocalPlugin(ppl.entryId, ppl.platformId, plugin.name, version, ppl.platform, sha256, sha512, System.currentTimeMillis())
                    log("Found plugin ${ppl.platform} ${plugin.name} $version from ${file.name}")
                    LocalPluginCache.add(local)
                    recognizedPlugins.add(local)
                    file.renameTo(File(file.parentFile, plugin.getFullDownloadedName(ppl.platform)))
                }

            LocalPluginCache.save()

            async {
                // Load into the marketplace cache.
                MarketplacePluginCache.addRecognizeAllPluginsToCache(recognizedPlugins)
            }

            log("Finished recognizing plugins. Found ${recognizedPlugins.size}/${jars.size}")

            if (recognizedPlugins.isEmpty()) {
                audience.sendInfo("No new plugins were recognised")
            } else {
                var component = textPrimary("Recognition Results:\n")
                recognizedPlugins.forEach { plugin ->
                    component = component.appendDark("  - ").append(text("${plugin.name} ${plugin.version} (${plugin.platform})\n", NamedTextColor.GREEN))
                }
                component = component.appendPrimary("\nUnable to Recognize:\n")
                unrecognizedPlugins.forEach { file ->
                    component = component.appendDark("  - ").append(text("${file.name}\n", NamedTextColor.RED))
                }
                audience.sendMessage(component.boxed())
            }
        }
    }
}
