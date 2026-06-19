package gg.flyte.pluginportal.plugin.adapters

import gg.flyte.pluginportal.common.API
import gg.flyte.pluginportal.common.commands.lamp.Features
import gg.flyte.pluginportal.plugin.MCLInfo
import gg.flyte.pluginportal.plugin.PluginPortal
import gg.flyte.pluginportal.plugin.adapters.platforms.github.GitHubAdapter
import gg.flyte.pluginportal.plugin.adapters.platforms.modrinth.ModrinthAdapter
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

object AdapterManager {
    private val platformAdapters = mapOf(
        AdapterPlatform.GITHUB to GitHubAdapter,
        AdapterPlatform.MODRINTH to ModrinthAdapter,
    )

    private val dataFolder = PluginPortal.instance.dataFolder
    private var adaptersFile = File(dataFolder, "adapters.yml").apply { createNewFile() }

    private val adaptations = mutableListOf<Adaptation>()

    init {
        val config = YamlConfiguration.loadConfiguration(adaptersFile)
        if (!config.contains("gg/flyte/pluginportal/plugin/adapters/pluginportal/plugin/adapters")) {
            config.set("gg/flyte/pluginportal/plugin/adapters/pluginportal/plugin/adapters", null)
            config.save(adaptersFile)
        }

        config.getConfigurationSection("gg/flyte/pluginportal/plugin/adapters/pluginportal/plugin/adapters")?.getKeys(false)?.forEach { key ->
            val section = config.getConfigurationSection("adapters.$key")!!
            val platform = AdapterPlatform.valueOf(section.getString("platform")!!.uppercase())
            val trigger = AdapterTrigger.valueOf(section.getString("trigger")!!.uppercase())

            val githubRepo = section.getString("githubRepo", null)
            val githubPreRelease = section.getBoolean("githubPre-release", false)
            val githubNameFilter = section.getString("githubNameFilter", "\\.jar\$")?.toRegex()

            val modrinthSlug = section.getString("modrinthSlug", null)
            val modrinthFeatured = section.getBoolean("modrinthFeatured", true)
            val modrinthPrimary = section.getBoolean("modrinthPrimary", true)
            val modrinthChannels = section.getStringList("modrinthChannels")
            val modrinthLoaders = section.getStringList("modrinthLoaders")

            adaptations.add(
                Adaptation(
                    key,
                    platform,
                    trigger,
                    githubRepo,
                    githubPreRelease,
                    githubNameFilter,
                    modrinthSlug,
                    modrinthFeatured,
                    modrinthPrimary,
                    modrinthChannels,
                    modrinthLoaders
                )
            )
        }

        if (PluginPortal.instance.isAuthed()) {
            downloadAdapters()
        }
    }

    private fun downloadAdapters() {
        adaptations.forEach { adaptation ->
            val adapter = platformAdapters[adaptation.platform] ?: return@forEach

            if (adaptation.trigger == AdapterTrigger.AUTO) {
                runCatching {
                    adapter.download(adaptation)
                }.onFailure { ex ->
                    PluginPortal.instance.logger.warning(
                        "Unable to update ${adaptation.name}: ${ex.message ?: ex::class.simpleName}"
                    )
                }
            }
        }
    }
}
