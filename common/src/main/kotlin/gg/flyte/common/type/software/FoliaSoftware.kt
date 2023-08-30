package gg.flyte.common.type.software

import gg.flyte.common.api.PaperMCAPI

class FoliaSoftware : SoftwareInterface {
    override fun getVersions(): List<String> {
        return PaperMCAPI.getProject("folia").body()?.versions?.map { it!! }!!
    }

    override fun getDownloadUrl(version: String): String {
        PaperMCAPI.getProjectVersionBuilds("folia", version).body()?.builds?.last()?.build!!.let { build ->
            return PaperMCAPI.getBuildDownloadUrl(
                "folia",
                version,
                build,
                PaperMCAPI.getProjectVersionBuild("folia", version, build).body()?.downloads?.application?.name!!
            )
        }
    }
}