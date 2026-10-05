package gg.flyte.pluginportal.common

import gg.flyte.pluginportal.common.commands.*
import gg.flyte.pluginportal.common.commands.lamp.AudienceResolver
import gg.flyte.pluginportal.common.commands.lamp.CommandEnabledConditionValidator
import gg.flyte.pluginportal.common.commands.lamp.Features
import gg.flyte.pluginportal.common.commands.lamp.LampExceptionHandler
import gg.flyte.pluginportal.common.commands.lamp.MarketplacePlatformType
import gg.flyte.pluginportal.common.managers.ExternalPluginManager
import gg.flyte.pluginportal.common.managers.LocalPluginCache
import gg.flyte.pluginportal.common.managers.MarketplacePluginCache
import gg.flyte.pluginportal.common.managers.PluginPortalSelfUpdateManager
import gg.flyte.pluginportal.common.managers.ServerTelemetryManager
import gg.flyte.pluginportal.common.notifications.DiscordWebhookNotifier
import gg.flyte.pluginportal.common.types.enums.MarketplacePlatform
import gg.flyte.pluginportal.common.util.async
import gg.flyte.pluginportal.common.runtime.PortalRuntime
import revxrsal.commands.Lamp
import gg.flyte.pluginportal.common.commands.lamp.PortalCommandActor
import java.io.File

object PluginPortalBase {
    lateinit var plugin: PortalRuntime private set
    lateinit var info: PluginPortalInfo private set
    class PluginPortalInfo(
        val pluginJarFile: File,
        val hasPremiumEntitlement: () -> Boolean,
        val refreshPremiumEntitlement: () -> Boolean = hasPremiumEntitlement,
    ) {
        fun getJarName(version: String) = "PluginPortal-$version.jar"
    }

    lateinit var lamp: Lamp<PortalCommandActor> private set

    // State
    var updateIsAvailable = false

    fun load(plugin: PortalRuntime, info: PluginPortalInfo, commands: Array<Any>, lampConfiguration: (Lamp.Builder<PortalCommandActor>) -> Unit) {
        this.plugin = plugin
        this.info = PluginPortalInfo(
            pluginJarFile = info.pluginJarFile,
            hasPremiumEntitlement = info.hasPremiumEntitlement,
            refreshPremiumEntitlement = info.refreshPremiumEntitlement,
        )

        // Load API and attach an auth key for endpoints that require it.
        API

        try {
            val apiKey = Config.getApiKey()
            if (apiKey != null) {
                API.enableAuthenticatedClient(apiKey)
                plugin.logger.info("Enabled authenticated API requests")
            } else {
                plugin.logger.info("No API key found in config - authenticated API requests not enabled")
                plugin.logger.info("Use '/pp key set <key>' to add authentication")
            }
        } catch (e: Exception) {
            plugin.logger.warning("Failed to enable authenticated API requests: ${e.message}")
        }
        
        LocalPluginCache.load()
        ExternalPluginManager.load().forEach { plugin.logger.warning("Invalid external plugin config: $it") }
        async { MarketplacePluginCache.startCacheLoader() }
        async { ServerTelemetryManager.recordStartup() }
        ExternalPluginManager.executeAsync {
            runCatching { ExternalPluginManager.verifyInstalledPluginStates() }.onFailure {
                plugin.logger.warning("Could not verify external plugin state: ${it.message ?: it::class.simpleName}")
            }
        }
        run {
            ExternalPluginManager.executeAsync {
                runCatching { ExternalPluginManager.updateAll(includeManual = false) }.onSuccess { results ->
                    results.forEach { result ->
                        if (result.success) plugin.logger.info(result.message)
                        else plugin.logger.warning(result.message)
                    }
                }.onFailure {
                    plugin.logger.warning("Could not update external plugins: ${it.message ?: it::class.simpleName}")
                }
            }
        }


        // Update plugin portal
        async {
            plugin.logger.info("Checking for updates...")
            val current = plugin.description.version
            val marketplaceUpdate = PluginPortalSelfUpdateManager.findMarketplaceUpdate(current)
            val latest = if (marketplaceUpdate == null && PluginPortalSelfUpdateManager.fetchCanonicalPlugin() == null) {
                API.checkForPPUpdate(current)
            } else {
                null
            }
            val latestVersionString = marketplaceUpdate?.versionString ?: latest?.latest?.version

            updateIsAvailable = marketplaceUpdate != null || (latest?.updateAvailable == true && latest.latest != null)

            if (updateIsAvailable) plugin.logger.warning("An update is available for plugin portal [$current -> $latestVersionString]. Run /pp upgrade to download the update!")
            else plugin.logger.info("Plugin Portal is up to date!")

            // Auto-update
            if (updateIsAvailable && Features.AUTOMATICALLY_UPDATE_PPP.isEnabled()) {
                if (latestVersionString == null) {
                    ServerTelemetryManager.recordPluginPortalUpdateFailed()
                    plugin.logger.warning("Plugin Portal update was reported without a version string.")
                    return@async
                }

                plugin.logger.info("Automatically downloading Plugin Portal update [$current -> $latestVersionString]...")
                ServerTelemetryManager.recordUpdateQueued(latestVersionString)
                val result = if (marketplaceUpdate != null) {
                    PluginPortalSelfUpdateManager.downloadMarketplaceUpdate(marketplaceUpdate)
                } else {
                    API.downloadPluginPortalUpdate(latestVersionString, latest?.latest?.channel ?: "stable")
                }
                if (result) {
                    ServerTelemetryManager.recordPluginPortalUpdateSucceeded()
                    plugin.logger.info("Successfully downloaded plugin portal v$latestVersionString. Restart your server for this change to take effect.")
                    DiscordWebhookNotifier.pluginPortalUpdated(current, latestVersionString, automatic = true)
                    updateIsAvailable = false
                } else {
                    ServerTelemetryManager.recordPluginPortalUpdateFailed()
                    plugin.logger.info("Plugin Portal Update failed.")
                }
            }
        }

        // Register Commands, etc.
        lamp = Lamp.builder<PortalCommandActor>()
            .senderResolver(AudienceResolver())
            .permissionForAnnotation(gg.flyte.pluginportal.common.commands.lamp.CommandPermission::class.java) { permission ->
                revxrsal.commands.command.CommandPermission { actor -> actor.hasPermission(permission.value) }
            }
            .commandCondition(CommandEnabledConditionValidator())
            .exceptionHandler(gg.flyte.pluginportal.common.commands.lamp.CommandUsageExceptionHandler(LampExceptionHandler()))
            .parameterTypes {
                it.addParameterType(MarketplacePlatform::class.java, MarketplacePlatformType())
            }
            .apply(lampConfiguration)
            .build()

        lamp.register(
            // Shared Commands
            InstallSubCommand(),
            InstallURLSubCommand(),
            UpdateSubCommand(),
            BlacklistSubCommand(),
            PlatformSubCommand(),
            DeleteSubCommand(),
            HelpSubCommand(),
            ViewSubCommand(),
            SearchSubCommand(),
            ListSubCommand(),
            DumpSubCommand(),
            SupportSubCommand(),
            ConfigSubCommand(),
            VersionSubCommand(),
            AuthSubCommand(),  // Authentication key management
            UpgradeSubCommand(), // Self-Upgrade
            // Additional Commands
            *commands
        )

    }

    fun onDisable() {
        ExternalPluginManager.close()
        MarketplacePluginCache.stopCacheLoader()
        DiscordWebhookNotifier.close()
        API.closeClient()
    }

}
