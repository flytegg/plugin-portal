package gg.flyte.common.type.software.`interface`

import gg.flyte.common.api.PaperMCAPI

interface PaperMCSoftwareInterface : SoftwareInterface {

    abstract fun getPlatformName(): String

    override fun getVersions(): List<String> {
        return PaperMCAPI.getProject(getPlatformName()).body()?.versions?.map { it!! }!!
    }

    override fun getDownloadUrl(version: String): String {
        PaperMCAPI.getProjectVersionBuilds(getPlatformName(), version).body()?.builds?.last()?.build!!.let { build ->
            return PaperMCAPI.getBuildDownloadUrl(
                getPlatformName(),
                version,
                build,
                PaperMCAPI.getProjectVersionBuild(getPlatformName(), version, build).body()?.downloads?.application?.name!!
            )
        }
    }

}