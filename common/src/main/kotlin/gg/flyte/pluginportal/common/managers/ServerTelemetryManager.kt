package gg.flyte.pluginportal.common.managers

import gg.flyte.pluginportal.common.API
import gg.flyte.pluginportal.common.Config
import gg.flyte.pluginportal.common.PluginPortalBase
import org.bukkit.Bukkit
import java.security.MessageDigest
import java.util.UUID

object ServerTelemetryManager {
    private const val SERVER_ID_PATH = "PluginPortalUpdates.ServerId"
    private var pluginPortalUpdateSuccessCount = 0
    private var pluginPortalUpdateFailureCount = 0
    private var managedPluginUpdateFailureCount = 0

    fun serverId(): String {
        val config = PluginPortalBase.plugin.config
        val existing = config.getString(SERVER_ID_PATH)
        if (!existing.isNullOrBlank()) return existing

        val generated = UUID.randomUUID().toString()
        config.set(SERVER_ID_PATH, generated)
        PluginPortalBase.plugin.saveConfig()
        return generated
    }

    private fun licenseKeyTelemetry(): Pair<String?, String?> {
        val key = Config.getApiKey()?.trim()
        if (key.isNullOrEmpty()) return null to null

        val hash = MessageDigest.getInstance("SHA-256")
            .digest(key.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
        return hash to maskLicenseKey(key)
    }

    private fun maskLicenseKey(key: String): String {
        if (key.length <= 8) return "****"
        return "${key.take(4)}...${key.takeLast(4)}"
    }

    fun recordStartup() {
        if (!Config.isTelemetryEnabled()) return

        val (licenseKeyHash, licenseKeyMasked) = licenseKeyTelemetry()
        API.recordPluginPortalStartup(
            serverId = serverId(),
            licenseKeyHash = licenseKeyHash,
            licenseKeyMasked = licenseKeyMasked,
            version = PluginPortalBase.plugin.description.version,
            serverVersion = Bukkit.getServer().version,
            minecraftVersion = Bukkit.getServer().bukkitVersion,
            managedPluginCount = LocalPluginCache.size,
            pluginPortalUpdateSuccessCount = pluginPortalUpdateSuccessCount,
            pluginPortalUpdateFailureCount = pluginPortalUpdateFailureCount,
            managedPluginUpdateFailureCount = managedPluginUpdateFailureCount,
        )
    }

    fun recordPluginPortalUpdateSucceeded() {
        pluginPortalUpdateSuccessCount += 1
    }

    fun recordPluginPortalUpdateFailed() {
        pluginPortalUpdateFailureCount += 1
    }

    fun recordManagedPluginUpdateFailed() {
        managedPluginUpdateFailureCount += 1
    }

    fun recordUpdateQueued(targetVersion: String) {
        if (!Config.isTelemetryEnabled()) return

        val (licenseKeyHash, licenseKeyMasked) = licenseKeyTelemetry()
        API.recordPluginPortalUpdateQueued(
            serverId = serverId(),
            licenseKeyHash = licenseKeyHash,
            licenseKeyMasked = licenseKeyMasked,
            currentVersion = PluginPortalBase.plugin.description.version,
            targetVersion = targetVersion,
            serverVersion = Bukkit.getServer().version,
            minecraftVersion = Bukkit.getServer().bukkitVersion,
            managedPluginCount = LocalPluginCache.size,
            pluginPortalUpdateSuccessCount = pluginPortalUpdateSuccessCount,
            pluginPortalUpdateFailureCount = pluginPortalUpdateFailureCount,
            managedPluginUpdateFailureCount = managedPluginUpdateFailureCount,
        )
    }
}
