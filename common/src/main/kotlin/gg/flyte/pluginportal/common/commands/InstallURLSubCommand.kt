package gg.flyte.pluginportal.common.commands

import gg.flyte.pluginportal.common.PluginPortalBase.audiences
import gg.flyte.pluginportal.common.adapters.DownloadManager
import gg.flyte.pluginportal.common.adapters.DownloadRequest
import gg.flyte.pluginportal.common.chat.*
import gg.flyte.pluginportal.common.commands.lamp.EnabledCommand
import gg.flyte.pluginportal.common.commands.lamp.Features
import gg.flyte.pluginportal.common.util.async
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bukkit.command.ConsoleCommandSender
import revxrsal.commands.annotation.*
import revxrsal.commands.bukkit.annotation.CommandPermission
import java.io.File

@Command("pp", "pluginportal", "ppm")
class InstallURLSubCommand {

    @EnabledCommand(Features.INSTALL)
    @Subcommand("install-url")
    @CommandPermission("pluginportal.manage.install-url")
    fun installURLCommand(
        sender: ConsoleCommandSender,
        @Named("url") url: String
    ) {

        val audience = audiences.sender(sender)

        async {
            audience.sendMessage(
                startLine()
                    .appendSecondary("Downloading plugin from URL: ")
                    .appendPrimary(url)
                    .appendNewline()
            )
            
            val request = DownloadRequest(
                url = url,
                targetDirectory = File("plugins"),
                audience = audience
            )
            
            val result = DownloadManager.download(request)
            
            if (result.success) {
                audience.sendMessage(
                    status(Status.SUCCESS, "Successfully downloaded ${result.file?.name}")
                        .appendSecondary(" - Please restart your server to enable this plugin")
                        .append(endLine())
                )
            } else {
                audience.sendFailure(result.error ?: "Download failed")
            }
        }
    }
}