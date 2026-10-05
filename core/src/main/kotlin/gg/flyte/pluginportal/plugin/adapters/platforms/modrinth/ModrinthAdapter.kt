package gg.flyte.pluginportal.plugin.adapters.platforms.modrinth

import gg.flyte.pluginportal.plugin.PortalApplication
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
            PortalApplication.runtime.logger.warning("No Modrinth versions found for ${adaptation.modrinthSlug}")
            return
        }

        if (adaptation.modrinthPrimary) {
            version.files.firstOrNull { it.isPrimary } ?: return
            PortalApplication.runtime.logger.info("Downloading Modrinth adapter ${adaptation.modrinthSlug} from ${version.name}")
        } else {
            version.files.firstOrNull() ?: return
            PortalApplication.runtime.logger.info("Downloading Modrinth adapter ${adaptation.modrinthSlug} from ${version.name}")
        }
    }

}
