package gg.flyte.common.type.papermc

import com.google.gson.annotations.SerializedName

data class ProjectBuild(

	@field:SerializedName("project_id")
	val projectId: String? = null,

	@field:SerializedName("build")
	val build: Int? = null,

	@field:SerializedName("downloads")
	val downloads: Downloads? = null,

	@field:SerializedName("channel")
	val channel: String? = null,

	@field:SerializedName("changes")
	val changes: List<ChangesItem?>? = null,

	@field:SerializedName("time")
	val time: String? = null,

	@field:SerializedName("project_name")
	val projectName: String? = null,

	@field:SerializedName("promoted")
	val promoted: Boolean? = null,

	@field:SerializedName("version")
	val version: String? = null
)
