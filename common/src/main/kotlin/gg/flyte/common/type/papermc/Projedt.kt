package gg.flyte.common.type.papermc

import com.google.gson.annotations.SerializedName

data class Project(
	@field:SerializedName("project_id") val projectId: String? = null,
	@field:SerializedName("project_name") val projectName: String? = null,
	@field:SerializedName("version_groups") val versionGroups: List<String?>? = null,
	@field:SerializedName("versions") val versions: List<String?>? = null,
)
