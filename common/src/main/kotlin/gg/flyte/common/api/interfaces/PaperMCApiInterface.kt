package gg.flyte.common.api.interfaces

import gg.flyte.common.type.papermc.*
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import java.io.File

interface PaperMCApiInterface {

    @GET("projects")
    fun getProjects(): Call<Projects>

    @GET("projects/{project}")
    fun getProject(@Path("project") project: String): Call<Project>

    @GET("projects/{project}/versions")
    fun getProjectVersions(@Path("project") project: String): Call<ProjectVersions>

    @GET("projects/{project}/versions/{version}")
    fun getProjectVersion(@Path("project") project: String, @Path("version") version: String): Call<ProjectVersion>

    @GET("projects/{project}/versions/{version}/builds")
    fun getProjectVersionBuilds(@Path("project") project: String, @Path("version") version: String): Call<ProjectBuilds>

    @GET("projects/{project}/versions/{version}/builds/{build}")
    fun getProjectVersionBuild(@Path("project") project: String, @Path("version") version: String, @Path("build") build: Int): Call<ProjectBuild>
//
//    @GET("projects/{project}/versions/{version}/builds/{build}/downloads/{download}")
//    fun getProjectVersionBuildDownload(@Path("project") project: String, @Path("version") version: String, @Path("build") build: Int, @Path("download") download: String): Call<File>




}