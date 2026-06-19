package gg.flyte.pluginportal.plugin.adapters.platforms.modrinth

import gg.flyte.pluginportal.plugin.PluginPortal
import gg.flyte.pluginportal.plugin.adapters.Adaptation
import gg.flyte.pluginportal.plugin.adapters.PlatformAdapter
import gg.flyte.pluginportal.plugin.modrinthClient
import masecla.modrinth4j.endpoints.version.GetProjectVersions.GetProjectVersionsRequest

object ModrinthAdapter : PlatformAdapter {
    override fun download(adaptation: Adaptation) {
        val versions = modrinthClient.versions().getProjectVersions(
            adaptation.modrinthSlug,
            GetProjectVersionsRequest.builder()
                .featured(adaptation.modrinthFeatured)
                .build()
        ).get()

        val version = versions.filter { version -> version.loaders.any { adaptation.modrinthLoaders.contains(it) } }
            .firstOrNull { version ->
                adaptation.modrinthChannels.contains(version.versionType.name)
            }

        if (version == null) {
            PluginPortal.instance.logger.warning("No Modrinth versions found for ${adaptation.modrinthSlug}")
            return
        }

        if (adaptation.modrinthPrimary) {
            version.files.firstOrNull { it.isPrimary } ?: return
            PluginPortal.instance.logger.info("Downloading Modrinth adapter ${adaptation.modrinthSlug} from ${version.name}")
        } else {
            version.files.firstOrNull() ?: return
            PluginPortal.instance.logger.info("Downloading Modrinth adapter ${adaptation.modrinthSlug} from ${version.name}")
        }
    }

}
