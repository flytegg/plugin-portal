package gg.flyte.common.api

import gg.flyte.common.type.papermc.Project
import gg.flyte.common.type.papermc.Projects
import gg.flyte.common.util.paperMCApiInterface

object PaperMCAPI {

    fun getProjects() = paperMCApiInterface.getProjects().execute()

    fun getProject(project: String) = paperMCApiInterface.getProject(project).execute()

    fun getProjectVersions(project: String) = paperMCApiInterface.getProjectVersions(project).execute()

    fun getProjectVersion(project: String, version: String) = paperMCApiInterface.getProjectVersion(project, version).execute()

    fun getProjectVersionBuilds(project: String, version: String) = paperMCApiInterface.getProjectVersionBuilds(project, version).execute()

    fun getProjectVersionBuild(project: String, version: String, build: Int) = paperMCApiInterface.getProjectVersionBuild(project, version, build).execute()

    fun getBuildDownloadUrl(project: String, version: String, build: Int, download: String): String {
        return "https://api.papermc.io/v2/projects/$project/versions/$version/builds/$build/downloads/$download"

//        paperMCApiInterface.getProjectVersionBuildDownload(project, version, build, download).execute()
    }

}