package gg.flyte.pluginportal.plugin

import gg.flyte.pluginportal.common.Config
import gg.flyte.pluginportal.common.PluginPortalBase
import gg.flyte.pluginportal.common.runtime.PortalRuntime
import gg.flyte.pluginportal.plugin.adapters.AdapterPluginCache
import gg.flyte.pluginportal.plugin.commands.*
import gg.flyte.pluginportal.plugin.commands.lamp.SafeFileNameValidator
import gg.flyte.pluginportal.plugin.commands.recognize.RecognizeAllSubCommand
import gg.flyte.pluginportal.plugin.commands.recognize.RecognizeSubCommand
import gg.flyte.pluginportal.plugin.websocket.TypedSocketManager

object PortalApplication {
    lateinit var runtime: PortalRuntime
        private set
    private lateinit var entitlement: EntitlementManager

    fun start(runtime: PortalRuntime) {
        this.runtime = runtime
        Config.init(runtime)
        entitlement = EntitlementManager(runtime)
        entitlement.loadConfiguredKey()
        entitlement.refresh()
        PluginPortalBase.load(runtime, PluginPortalBase.PluginPortalInfo(
            runtime.jarFile, ::isAuthed, ::refreshEntitlement,
        ), arrayOf(
            ImportSubCommand(), ExportSubCommand(), UpdateAllSubCommand(), ScanSubCommand(),
            RecognizeSubCommand(), RecognizeAllSubCommand(), EditorSubCommand(), ExternalSubCommand(),
        )) { it.parameterValidator(String::class.java, SafeFileNameValidator()) }
        AdapterPluginCache.load()
    }

    fun isAuthed() = entitlement.hasPremiumAccess()
    fun refreshEntitlement() = entitlement.refresh() is EntitlementState.Valid
    fun lockedPremiumMessage() = entitlement.lockedMessage()
    fun stop() {
        TypedSocketManager.stop()
        PluginPortalBase.onDisable()
        runtime.close()
    }
}
