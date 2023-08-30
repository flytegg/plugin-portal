package gg.flyte.common.type.papermc

import com.google.gson.annotations.SerializedName

data class ProjectBuilds(

    @SerializedName("project_id") var projectId: String? = null,
    @SerializedName("project_name") var projectName: String? = null,
    @SerializedName("version") var version: String? = null,
    @SerializedName("builds") var builds: ArrayList<Builds> = arrayListOf()
)

data class Builds(

    @SerializedName("build") var build: Int? = null,
    @SerializedName("time") var time: String? = null,
    @SerializedName("channel") var channel: String? = null,
    @SerializedName("promoted") var promoted: Boolean? = null,
    @SerializedName("changes") var changes: ArrayList<ChangesItem> = arrayListOf(),
    @SerializedName("downloads") var downloads: Downloads? = Downloads()

)

data class Downloads(

    @field:SerializedName("application")
    val application: Application? = null,

    @field:SerializedName("mojang-mappings")
    val mojangMappings: MojangMappings? = null
)

data class ChangesItem(

    @field:SerializedName("summary")
    val summary: String? = null,

    @field:SerializedName("commit")
    val commit: String? = null,

    @field:SerializedName("message")
    val message: String? = null
)

data class Application(

    @field:SerializedName("sha256")
    val sha256: String? = null,

    @field:SerializedName("name")
    val name: String? = null
)

data class MojangMappings(

    @field:SerializedName("sha256")
    val sha256: String? = null,

    @field:SerializedName("name")
    val name: String? = null
)
