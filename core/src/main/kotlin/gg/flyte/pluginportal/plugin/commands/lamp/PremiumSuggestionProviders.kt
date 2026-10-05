package gg.flyte.pluginportal.plugin.commands.lamp

import gg.flyte.pluginportal.common.commands.lamp.CustomSuggestionProvider
import gg.flyte.pluginportal.common.managers.ExternalPluginManager
import gg.flyte.pluginportal.common.managers.LocalPluginCache
import gg.flyte.pluginportal.common.util.HashType
import gg.flyte.pluginportal.common.util.isJarFile
import gg.flyte.pluginportal.common.util.isPluginPortal
import java.io.File

class PluginJarFilesUnrecognisedSP: CustomSuggestionProvider({
    val externalHashes = ExternalPluginManager.managedHashes()
    val externalFileNames = ExternalPluginManager.managedFileNames()
    File("plugins").listFiles()
        .orEmpty()
        .filter { file ->
            if (!file.isJarFile()) return@filter false
            val hash = runCatching { HashType.SHA256.hash(file) }.getOrNull() ?: return@filter false
            !LocalPluginCache.hasPluginByHash(hash) &&
                hash.lowercase() !in externalHashes &&
                file.name.lowercase() !in externalFileNames &&
                !LocalPluginCache.hasManagedDownloadedFile(file) &&
                !file.isPluginPortal
        }
        .map { it.name }
})

class PluginJarFilesSuggestionProvider: CustomSuggestionProvider({
    File("plugins").listFiles().orEmpty().filter(File::isJarFile).map(File::getName)
})
