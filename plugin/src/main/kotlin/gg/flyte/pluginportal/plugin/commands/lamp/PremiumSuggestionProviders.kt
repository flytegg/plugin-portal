package gg.flyte.pluginportal.plugin.commands.lamp

import gg.flyte.pluginportal.common.commands.lamp.CustomSuggestionProvider
import gg.flyte.pluginportal.common.managers.LocalPluginCache
import gg.flyte.pluginportal.common.util.HashType
import gg.flyte.pluginportal.common.util.isJarFile
import gg.flyte.pluginportal.common.util.isPluginPortal
import java.io.File

class PluginJarFilesUnrecognisedSP: CustomSuggestionProvider({
    File("plugins").listFiles()
        .orEmpty()
        .filter { it.isJarFile() && !LocalPluginCache.hasPluginByHash(HashType.SHA256.hash(it)) && !it.isPluginPortal }
        .map { it.name }
})

class PluginJarFilesSuggestionProvider: CustomSuggestionProvider({
    File("plugins").listFiles().orEmpty().filter(File::isJarFile).map(File::getName)
})
