package gg.flyte.pluginportal.common.adapters

import gg.flyte.pluginportal.common.types.LocalPlugin
import gg.flyte.pluginportal.common.types.Plugin
import net.kyori.adventure.audience.Audience
import java.io.File

interface DownloadAdapter {
    fun canHandle(request: DownloadRequest): Boolean
    fun download(request: DownloadRequest): DownloadResult
    fun getPriority(): Int = 0
}

data class DownloadRequest(
    val plugin: Plugin? = null,
    val url: String? = null,
    val targetDirectory: File,
    val versionFilter: String? = null,
    val audience: Audience? = null
)

data class DownloadResult(
    val success: Boolean,
    val file: File? = null,
    val error: String? = null,
    val localPlugin: LocalPlugin? = null
)
