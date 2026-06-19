package gg.flyte.pluginportal.common.commands

import gg.flyte.pluginportal.common.chat.*
import gg.flyte.pluginportal.common.logging.Paste
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.TextColor
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
                    .appendSecondary(" $url.").clickEvent(ClickEvent.openUrl(url))
                    .appendSecondary(" Join our ")
                    .append(
                        Component.text("Discord")
                            .clickEvent(ClickEvent.openUrl("https://flyte.gg/discord"))
                            .color(TextColor.fromHexString("#5865F2"))
                            .bold()
                    )
                    .appendSecondary(" for support")
                    .boxed()
            )
        }
    }

}