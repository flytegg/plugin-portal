package gg.flyte.pluginportal.plugin

import gg.flyte.pluginportal.common.AuthCreds
import gg.flyte.pluginportal.common.Config
import java.io.File
import java.util.*

object MCLInfo {

    val sessionId by lazy { UUID.randomUUID().toString() }

    fun getRawLicenseKey(): String {
        // Use unified Config system instead of direct file access
        val apiKey = Config.getApiKey()
        if (!apiKey.isNullOrEmpty()) {
            return apiKey
        }
        
        // Fallback to legacy file for backwards compatibility
        val licenseFile = File(PluginPortal.instance.dataFolder, "mclicense.txt")

        if (!licenseFile.exists()) {
            licenseFile.writeText("")
        }

        val licenseKey = licenseFile.readText().trim()
        return licenseKey
    }

    val authCreds: AuthCreds
        get() = AuthCreds(
            getRawLicenseKey(),
            PluginPortal.instance.server.ip
        )

}