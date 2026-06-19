package gg.flyte.pluginportal.plugin

import gg.flyte.pluginportal.common.Config
import gg.flyte.pluginportal.common.PluginPortalBase
import gg.flyte.pluginportal.plugin.adapters.AdapterPluginCache
import gg.flyte.pluginportal.plugin.commands.ImportSubCommand
import gg.flyte.pluginportal.plugin.commands.ExportSubCommand
import gg.flyte.pluginportal.plugin.commands.UpdateAllSubCommand
import gg.flyte.pluginportal.plugin.commands.ScanSubCommand
import gg.flyte.pluginportal.plugin.commands.recognize.RecognizeSubCommand
import gg.flyte.pluginportal.plugin.commands.recognize.RecognizeAllSubCommand
import gg.flyte.pluginportal.plugin.commands.EditorSubCommand
import gg.flyte.pluginportal.plugin.commands.lamp.RequiresAuthValidator
import gg.flyte.pluginportal.plugin.commands.lamp.SafeFileNameValidator
import gg.flyte.pluginportal.plugin.websocket.TypedSocketManager
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class PluginPortal : JavaPlugin() {

    companion object {
        lateinit var instance: PluginPortal
        lateinit var pluginPortalJarFile: File
    }

    private lateinit var entitlementManager: EntitlementManager

    override fun onEnable() {
        instance = this
        pluginPortalJarFile = this.file

        Config.init(this)
        entitlementManager = EntitlementManager(this)
        
        entitlementManager.loadConfiguredKey()
        entitlementManager.refresh()

        if (!isAuthed()) {
            logger.info("Premium features are locked until a valid Plugin Portal key is configured.")
        }

        val commands: Array<Any> = arrayOf(
            ImportSubCommand(),
            ExportSubCommand(),
            UpdateAllSubCommand(),
            ScanSubCommand(),
            RecognizeSubCommand(),
            RecognizeAllSubCommand(),
            EditorSubCommand(),
        )

        PluginPortalBase.load(
            this,
            PluginPortalBase.PluginPortalInfo(
                pluginJarFile = pluginPortalJarFile,
                hasPremiumEntitlement = { isAuthed() },
                refreshPremiumEntitlement = { refreshEntitlement() },
            ),
            commands
        ) {
            it  .commandCondition(RequiresAuthValidator())
                .parameterValidator(String::class.java, SafeFileNameValidator())
        }


        AdapterPluginCache.load()
    }

    fun refreshEntitlement() = entitlementManager.refresh() is EntitlementState.Valid

    fun isAuthed() = entitlementManager.hasPremiumAccess()

    fun lockedPremiumMessage() = entitlementManager.lockedMessage()

    override fun onDisable() {
        TypedSocketManager.stop()
        PluginPortalBase.onDisable()
    }

}
