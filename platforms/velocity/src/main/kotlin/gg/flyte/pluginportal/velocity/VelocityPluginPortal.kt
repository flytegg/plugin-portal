package gg.flyte.pluginportal.velocity

import com.google.inject.Inject
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import gg.flyte.pluginportal.common.PluginPortalBase
import gg.flyte.pluginportal.common.commands.lamp.CommandAliasAudience
import gg.flyte.pluginportal.common.commands.lamp.PortalCommandActor
import gg.flyte.pluginportal.common.runtime.*
import gg.flyte.pluginportal.common.types.enums.ServerType
import gg.flyte.pluginportal.plugin.PortalApplication
import gg.flyte.pluginportal.plugin.network.ServerTargets
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.MessageType
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.pointer.Pointers
import net.kyori.adventure.text.Component
import java.io.File
import java.nio.file.Path
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.logging.Logger

class VelocityPluginPortal @Inject constructor(
    private val proxy: ProxyServer,
    @DataDirectory private val dataDirectory: Path,
) {
    @Subscribe
    fun initialize(event: ProxyInitializeEvent) {
        PortalApplication.start(VelocityRuntime(proxy, dataDirectory.toFile()))
        proxy.commandManager.register(proxy.commandManager.metaBuilder("ppv").aliases("pluginportalvelocity").plugin(this).build(), object : SimpleCommand {
            override fun hasPermission(invocation: SimpleCommand.Invocation) = allows(invocation.source(), "pluginportal.view")
            override fun execute(invocation: SimpleCommand.Invocation) {
                PluginPortalBase.lamp.dispatch(actor(invocation), "pp " + invocation.arguments().joinToString(" "))
            }
            override fun suggest(invocation: SimpleCommand.Invocation): List<String> =
                ServerTargets.complete(actor(invocation), "pp " + invocation.arguments().joinToString(" "))
            override fun suggestAsync(invocation: SimpleCommand.Invocation): CompletableFuture<List<String>> =
                CompletableFuture.supplyAsync({ suggest(invocation) }, PortalApplication.runtime.executor)
        })
        proxy.commandManager.register(proxy.commandManager.metaBuilder("ppnetwork").plugin(this).build(), object : SimpleCommand {
            override fun hasPermission(invocation: SimpleCommand.Invocation) = allows(invocation.source(), "pluginportal.network")
            override fun execute(invocation: SimpleCommand.Invocation) {
                PluginPortalBase.lamp.dispatch(actor(invocation), "pp network " + invocation.arguments().joinToString(" "))
            }
            override fun suggest(invocation: SimpleCommand.Invocation): List<String> =
                PluginPortalBase.lamp.autoCompleter().complete(actor(invocation), "pp network " + invocation.arguments().joinToString(" "))
            override fun suggestAsync(invocation: SimpleCommand.Invocation): CompletableFuture<List<String>> =
                CompletableFuture.supplyAsync({ suggest(invocation) }, PortalApplication.runtime.executor)
        })
    }

    private fun allows(source: CommandSource, permission: String) =
        source !is Player || source.hasPermission(permission) || source.hasPermission("pluginportal.admin")

    private fun actor(invocation: SimpleCommand.Invocation): PortalCommandActor {
        val source = invocation.source()
        val player = source as? Player
        val name = player?.username ?: "CONSOLE"
        val id = player?.uniqueId ?: UUID(0, 0)
        val audience = object : Audience {
            override fun pointers() = Pointers.builder().withStatic(Identity.NAME, name).apply {
                if (player != null) withStatic(Identity.UUID, id)
            }.build()
            override fun sendMessage(message: Component) = source.sendMessage(message)
            override fun sendMessage(message: Component, type: MessageType) = source.sendMessage(message, type)
            override fun sendMessage(identity: Identity, message: Component) = source.sendMessage(identity, message)
            override fun sendMessage(identity: Identity, message: Component, type: MessageType) = source.sendMessage(identity, message, type)
        }
        val alias = invocation.alias()
        val localRoot = if (alias.equals("ppnetwork", ignoreCase = true)) "ppv" else alias
        val networkRoot = if (alias.equals("ppnetwork", ignoreCase = true)) alias else "$alias network"
        return PortalCommandActor(CommandAliasAudience(audience, localRoot, networkRoot), name, id, player == null) { permission ->
            allows(source, permission)
        }
    }

    @Subscribe fun shutdown(event: ProxyShutdownEvent) = PortalApplication.stop()
}

private class VelocityRuntime(private val proxy: ProxyServer, override val dataFolder: File) : PortalRuntime() {
    override val logger = Logger.getLogger("PluginPortal")
    override val jarFile = File(VelocityPluginPortal::class.java.protectionDomain.codeSource.location.toURI())
    override val description get() = PluginDescription("PluginPortal", proxy.pluginManager.getPlugin("pluginportal").get().description.version.orElse("unknown"), listOf("Flyte"))
    override val server get() = ServerDescription(
        "Velocity", proxy.version.version, "", proxy.configuration.isOnlineMode,
        proxy.pluginManager.plugins.map { InstalledPlugin(it.description.name.orElse(it.description.id), PluginDescription(it.description.name.orElse(it.description.id), it.description.version.orElse("unknown"), it.description.authors), true) },
    )
    override val serverTypes = listOf(ServerType.VELOCITY)
    override val updateDirectory get() = File(dataFolder, "pending-updates")
    override val commandName = "ppv"
    override fun close() {
        super.close()
        if (!executor.isTerminated || !PortalApplication.network.isStopped) {
            logger.warning("Pending updates retained because plugin tasks did not stop. Apply them while the proxy is stopped.")
            return
        }
        gg.flyte.pluginportal.common.runtime.StagedUpdates.apply(updateDirectory, installDirectory, File(dataFolder, "backups"))
            .forEach { logger.warning(it) }
    }
}
