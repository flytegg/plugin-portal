package gg.flyte.pluginportal.plugin

import gg.flyte.pluginportal.common.PluginPortalBase
import gg.flyte.pluginportal.common.UpdateNotificationListener
import gg.flyte.pluginportal.common.commands.lamp.CommandAliasAudience
import gg.flyte.pluginportal.common.commands.lamp.CommandSenderAudience
import gg.flyte.pluginportal.common.commands.lamp.PortalCommandActor
import gg.flyte.pluginportal.common.runtime.*
import gg.flyte.pluginportal.common.types.enums.ServerType
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bstats.bukkit.Metrics
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.UUID

open class PluginPortal : JavaPlugin() {
    companion object {
        lateinit var instance: PluginPortal
        lateinit var pluginPortalJarFile: File
    }
    lateinit var audiences: BukkitAudiences
        private set

    override fun onEnable() {
        instance = this
        pluginPortalJarFile = resolveLoadedJar(file)
        audiences = BukkitAudiences.create(this)
        PortalApplication.start(BukkitRuntime(this, pluginPortalJarFile))
        getCommand("pp")!!.apply {
            setExecutor { sender, _, label, args ->
                PluginPortalBase.lamp.dispatch(actor(sender, label), "pp " + args.joinToString(" "))
                true
            }
            setTabCompleter { sender, _, label, args ->
                gg.flyte.pluginportal.plugin.network.ServerTargets.complete(actor(sender, label), "pp " + args.joinToString(" "))
            }
        }
        server.pluginManager.registerEvents(UpdateNotificationListener(), this)
        Metrics(this, 18005)
    }

    private fun actor(sender: CommandSender, label: String) = PortalCommandActor(
        CommandAliasAudience(CommandSenderAudience(sender, audiences), label), sender.name,
        (sender as? Player)?.uniqueId ?: UUID(0, 0), sender is ConsoleCommandSender,
        { permission -> sender.hasPermission(permission) || sender.hasPermission("pluginportal.admin") },
    )
    fun refreshEntitlement() = PortalApplication.refreshEntitlement()
    fun isAuthed() = PortalApplication.isAuthed()
    fun lockedPremiumMessage() = PortalApplication.lockedPremiumMessage()

    override fun onDisable() {
        PortalApplication.stop()
        if (::audiences.isInitialized) audiences.close()
    }

    private fun resolveLoadedJar(loaded: File): File {
        if (loaded.parentFile?.name == ".paper-remapped") {
            val original = File(loaded.parentFile.parentFile, loaded.name)
            if (original.exists()) return original
        }
        return loaded
    }
}

private class BukkitRuntime(private val plugin: PluginPortal, override val jarFile: File) : PortalRuntime() {
    override val dataFolder get() = plugin.dataFolder
    override val logger get() = plugin.logger
    override val description get() = PluginDescription(plugin.name, plugin.description.version, plugin.description.authors)
    override val server get() = ServerDescription(
        Bukkit.getName(), Bukkit.getVersion(), Bukkit.getBukkitVersion(), Bukkit.getOnlineMode(),
        Bukkit.getPluginManager().plugins.map { InstalledPlugin(it.name, PluginDescription(it.name, it.description.version, it.description.authors), it.isEnabled) },
    )
    override val updateDirectory get() = Bukkit.getUpdateFolderFile()
    override val serverTypes: List<ServerType> by lazy {
        val descriptor = (Bukkit.getName() + " " + Bukkit.getVersion()).lowercase()
        val platform = when {
            runCatching { Class.forName("io.papermc.paper.threadedregions.RegionizedServer") }.isSuccess -> ServerType.FOLIA
            "purpur" in descriptor -> ServerType.PURPUR
            "pufferfish" in descriptor -> ServerType.PUFFERFISH
            "paper" in descriptor || runCatching { Class.forName("com.destroystokyo.paper.PaperConfig") }.isSuccess -> ServerType.PAPER
            "spigot" in descriptor -> ServerType.SPIGOT
            else -> ServerType.BUKKIT
        }
        when (platform) {
            ServerType.BUKKIT -> listOf(platform)
            ServerType.SPIGOT -> listOf(platform, ServerType.BUKKIT)
            else -> listOf(platform, ServerType.PAPER, ServerType.SPIGOT, ServerType.BUKKIT).distinct()
        }
    }
}
