package gg.flyte.common.type.papermc

import com.google.gson.annotations.SerializedName

data class ProjectVersions(

	@field:SerializedName("project_id")
	val projectId: String,

	@field:SerializedName("project_name")
	val projectName: String,

	@field:SerializedName("version")
	val version: String,

	@field:SerializedName("builds")
	val builds: List<Int>,
)
