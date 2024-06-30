package gg.flyte.pluginportal.plugin.command

import gg.flyte.pluginportal.plugin.chat.*
import gg.flyte.pluginportal.plugin.logging.Paste
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("pp", "pluginportal", "ppm")
class DumpSubCommand {

    @Subcommand("dump")
    @CommandPermission("pluginportal.dump")
    fun dumpCommand(audience: Audience) {
        Paste.dump().let { url ->
            audience.sendMessage(
                status(Status.SUCCESS, "Dumped log to")

                    .append(textSecondary(" $url .").clickEvent(ClickEvent.openUrl(url)))
                    .append(
                        textSecondary("Join our ")
                            .append(
                                text("Discord")
                                    .clickEvent(ClickEvent.openUrl("https://discord.gg/flytegg"))
                                    .color(TextColor.fromHexString("#5865F2"))
                                    .bold()
                            )
                            .append(textSecondary(" for support"))
                    )
                    .boxed()
            )
        }
    }

}