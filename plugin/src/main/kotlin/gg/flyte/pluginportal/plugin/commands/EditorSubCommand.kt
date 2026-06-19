package gg.flyte.pluginportal.plugin.commands

import gg.flyte.pluginportal.common.API
import gg.flyte.pluginportal.common.Config
import gg.flyte.pluginportal.common.MCServerMetadata
import gg.flyte.pluginportal.common.chat.*
import gg.flyte.pluginportal.common.util.delay
import gg.flyte.pluginportal.plugin.PluginPortal
import gg.flyte.pluginportal.plugin.commands.lamp.RequiresAuth
import gg.flyte.pluginportal.plugin.websocket.TypedSocketManager
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.annotation.Switch
import revxrsal.commands.bukkit.annotation.CommandPermission
import org.bukkit.Bukkit

@Command("pp", "pluginportal", "ppm")
class EditorSubCommand {

//    @Subcommand("connect")
//    @CommandPermission("pluginportal.manage.editor")
//    fun connectCommand(audience: Audience, @Switch("isConsole") isConsole: Boolean = false) {
//        val sendRawLink = isConsole || audience.isConsole()
//        val metadata = serverMetadata()
//
//        delay(1, true) {
//            val code = API.createDeviceCode() ?: return@delay audience.sendFailure("Failed to create device login code.")
//            val link = code.verification_uri_complete
//
//            val linkComponent = text(if (sendRawLink) link else "[Approve Server]")
//                .colorPrimary()
//                .bold()
//                .clickEvent(ClickEvent.openUrl(link))
//
//            audience.sendMessage(
//                status(Status.INFO, "Approve this server with Discord: ")
//                    .append(linkComponent)
//                    .append(Component.newline())
//                    .appendSecondary("Code: ${code.user_code}")
//                    .boxed()
//            )
//
//            val deadline = System.currentTimeMillis() + (code.expires_in * 1000L)
//            while (System.currentTimeMillis() < deadline) {
//                Thread.sleep((code.interval.coerceAtLeast(2)) * 1000L)
//                val token = API.pollDeviceToken(code.device_code) ?: continue
//                val registered = API.registerServer(metadata, token.access_token)
//                    ?: return@delay audience.sendFailure("Server approval succeeded, but registration failed.")
//
//                Config.setApiKey(registered.apiKey)
//                API.enableAuthenticatedClient(registered.apiKey)
//                PluginPortal.instance.markAuthenticated()
//                TypedSocketManager.stop()
//                TypedSocketManager.start(registered.apiKey, "server", "https://pluginportal.link/rooms/${registered.serverId}")
//                audience.sendSuccess("Server connected permanently.")
//                return@delay
//            }
//
//            audience.sendFailure("Device login code expired. Run /pp connect again.")
//        }
//    }

//    @RequiresAuth
//    @Subcommand("connect temp")
//    @CommandPermission("pluginportal.manage.editor")
//    fun temporaryConnectCommand(audience: Audience, @Switch("isConsole") isConsole: Boolean = false) {
//        startTemporaryEditor(audience, isConsole)
//    }

    @RequiresAuth
    @Subcommand("editor")
    @CommandPermission("pluginportal.manage.editor")
    fun editorCommand(
        audience: Audience, @Switch("isConsole") isConsole: Boolean = false
    ) {
        startTemporaryEditor(audience, isConsole)
    }

    private fun startTemporaryEditor(audience: Audience, isConsole: Boolean) {
        val sendRawLink = isConsole || audience.isConsole()

        val link = API.createTemporaryServerLink(serverMetadata()) ?: return audience.sendFailure("Failed to create temporary dashboard link.")

        val editorURL = link.url

        TypedSocketManager.stop()

        if (!TypedSocketManager.isConnected()) {
            try {
                TypedSocketManager.start(link.pluginToken, link.code, editorURL)
            } catch (e: Exception) {
                audience.sendFailure("Failed to start editor websocket: ${e.message ?: e::class.simpleName}")
                return
            }
        }

        if (sendRawLink) {
            audience.sendMessage(
                status(Status.SUCCESS, "Plugin Portal Editor ready! URL: ").append(
                    text(editorURL).colorPrimary().clickEvent(ClickEvent.openUrl(editorURL))
                ).boxed()
            )

            return
        }

        audience.sendMessage(
            status(Status.SUCCESS, "Plugin Portal Editor ready! ").append(
                text("[Open Editor]").colorPrimary().bold().clickEvent(ClickEvent.openUrl(editorURL))
            ).boxed()
        )
    }

    private fun serverMetadata(): MCServerMetadata {
        return MCServerMetadata(
            name = Bukkit.getServer().name,
            serverVersion = Bukkit.getServer().version,
            minecraftVersion = Bukkit.getServer().bukkitVersion,
            pluginVersion = Bukkit.getPluginManager().getPlugin("PluginPortal")?.description?.version
        )
    }

    @RequiresAuth
    @Subcommand("editor status")
    @CommandPermission("pluginportal.manage.editor")
    fun editorStatusCommand(audience: Audience) {
        if (!TypedSocketManager.hasConnectedBefore()) {
            return audience.sendInfo("No editor session has been started yet. Use /pp editor to create one.")
        }

        val isConnected = TypedSocketManager.isConnected()
        val connectionStatus = if (isConnected) "Connected" else "Disconnected"
        val statusColor = if (isConnected) Status.SUCCESS else Status.FAILURE

        var message = status(statusColor, "Editor Status: $connectionStatus")
        
        if (isConnected) {
            val roomCode = TypedSocketManager.getRoomCode()
            if (roomCode != null) {
                message = message.append(textSecondary(" (Room: $roomCode)"))
            }
            
            val duration = TypedSocketManager.getConnectionDuration()
            val minutes = duration / 60000
            val seconds = (duration % 60000) / 1000
            message = message.append(Component.newline())
                .append(textSecondary("- Connected for: $minutes minutes, $seconds seconds"))
            
            val timeSinceActivity = TypedSocketManager.getTimeSinceLastActivity()
            val minutesSinceActivity = timeSinceActivity / 60000
            message = message.append(Component.newline())
                .append(textSecondary("- Last activity: $minutesSinceActivity minutes ago"))
            
            val timeUntilTimeout = 30 - minutesSinceActivity
            if (timeUntilTimeout <= 10 && timeUntilTimeout > 0) {
                message = message.append(Component.newline())
                    .append(text("- WARNING: Connection will timeout in $timeUntilTimeout minutes", NamedTextColor.YELLOW))
            }
            
            val connectedUsers = TypedSocketManager.getConnectedUsers()
            if (connectedUsers.isNotEmpty()) {
                message = message.append(Component.newline())
                    .append(textSecondary("- Connected users: ${connectedUsers.joinToString(", ")}"))
            }
        }

        audience.sendMessage(message.boxed())
    }

    @RequiresAuth
    @Subcommand("editor stop")
    @CommandPermission("pluginportal.manage.editor")
    fun editorStopCommand(audience: Audience) {
        if (!TypedSocketManager.hasConnectedBefore()) {
            return audience.sendInfo("No editor session is currently active.")
        }

        TypedSocketManager.stop()
        audience.sendSuccess("Editor session stopped successfully.")
    }

    @RequiresAuth
    @Subcommand("editor reconnect")
    @CommandPermission("pluginportal.manage.editor")
    fun editorReconnectCommand(audience: Audience) {
        if (!TypedSocketManager.hasConnectedBefore()) {
            return audience.sendFailure("No previous editor session found. Use /pp editor to create a new one.")
        }

        if (TypedSocketManager.isConnected()) {
            return audience.sendInfo("Editor is already connected.")
        }

        try {
            TypedSocketManager.reconnect()
            audience.sendSuccess("Reconnecting to editor session...")
        } catch (e: Exception) {
            audience.sendFailure("Failed to reconnect: ${e.message}")
        }
    }

    @RequiresAuth
    @Subcommand("editor url")
    @CommandPermission("pluginportal.manage.editor")
    fun editorURLCommand(audience: Audience, @Switch("isConsole") isConsole: Boolean = false) {
        val currentURL = TypedSocketManager.getCurrentURL() ?: return audience.sendFailure("No active editor session found. Use /pp editor to create one.")
        val sendRawLink = isConsole || audience.isConsole() // TODO: Dupe from setup command

        if (sendRawLink) {
            audience.sendMessage(
                status(Status.SUCCESS, "Current Editor URL: ").append(
                    text(currentURL).colorPrimary().clickEvent(ClickEvent.openUrl(currentURL))
                ).boxed()
            )
        } else {
            audience.sendMessage(
                status(Status.SUCCESS, "Current Editor URL: ").append(
                    text("[Open Editor]").colorPrimary().bold().clickEvent(ClickEvent.openUrl(currentURL))
                ).boxed()
            )
        }
    }
}
