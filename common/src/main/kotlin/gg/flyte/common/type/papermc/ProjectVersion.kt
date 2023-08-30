package gg.flyte.common.type.papermc

import com.google.gson.annotations.SerializedName

data class ProjectVersion(

	@field:SerializedName("project_id")
	val projectId: String? = null,

	@field:SerializedName("builds")
	val builds: List<Int?>? = null,

	@field:SerializedName("project_name")
	val projectName: String? = null,

	@field:SerializedName("version")
	val version: String? = null
)
