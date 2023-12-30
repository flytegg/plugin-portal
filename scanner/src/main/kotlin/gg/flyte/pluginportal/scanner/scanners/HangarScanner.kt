package gg.flyte.pluginportal.scanner.scanners

import gg.flyte.hangarwrapper.HangarClient
import gg.flyte.hangarwrapper.model.Platform
import gg.flyte.hangarwrapper.model.RequestPagination
import gg.flyte.hangarwrapper.model.project.Project
import gg.flyte.pluginportal.api.type.*

object HangarScanner : Scanner() {

    override suspend fun scan() {
        val plugins = hashSetOf<MarketplacePlugin>()

        val totalProjectCount = HangarClient.getProjects(RequestPagination(1, 0)).pagination.count

        for (i in 1..totalProjectCount step 25) {
            for (project in HangarClient.getProjects(RequestPagination(25, i)).result) {
                runCatching {
                    project.toMarketplacePlugin().addPluginToDatabase()
                }.onFailure {
                    println("Failed to scan ${project.name}")
                    it.printStackTrace()
                }
            }
        }
    }

    private suspend fun Project.toMarketplacePlugin(): MarketplacePlugin {

        val projectVersions = HangarClient.getVersions(
            name,
            RequestPagination(25, 0),
            Platform.PAPER,
            "Release"
        )

        val versions = HashMap<String, PluginVersion>().apply {
            projectVersions.result.forEach { version ->
                val download = version.downloads["PAPER"]
                put(
                    version.name,
                    PluginVersion(
                        version.name,
                        download?.downloadUrl,
                        hashMapOf<String, String>().apply {
                            download?.fileInfo?.sha256Hash?.let { put("SHA256", it) }
                        },
                        version.createdAt,
                        version.platformDependenciesFormatted[Platform.PAPER],
                        hashSetOf<Dependency>().apply {
                            version.pluginDependencies[Platform.PAPER]?.forEach { dependency ->
                                add(
                                    Dependency(
                                        dependency.name,
                                        dependency.name,
                                        dependency.required
                                    )
                                )
                            }
                        },
                        null
                    )
                )
            }
        }



        return MarketplacePlugin(
            name,
            DisplayInfo(
                namespace.slug,
                description,
                avatarUrl,
                null
            ),
            Statistics(
                stats.downloads,
                0L,
                0,
                false,
                0,
            ),
            ReleaseData(
                createdAt,
                lastUpdated,
                HangarClient.getLatestVersion(name, "Release")
            ),
            versions,
            null,
            )
    }
}