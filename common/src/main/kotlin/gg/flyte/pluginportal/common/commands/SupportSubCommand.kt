package gg.flyte.pluginportal.common.commands

import gg.flyte.pluginportal.common.chat.Status
import gg.flyte.pluginportal.common.chat.boxed
import gg.flyte.pluginportal.common.chat.sendFailure
import gg.flyte.pluginportal.common.chat.status
import gg.flyte.pluginportal.common.support.SupportDiagnosticsBundle
import gg.flyte.pluginportal.common.support.SupportDiagnosticsUploader
import gg.flyte.pluginportal.common.util.async
import net.kyori.adventure.audience.Audience
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Named
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("pp", "pluginportal", "ppm")
class SupportSubCommand {
    @Subcommand("support")
    @CommandPermission("pluginportal.admin")
    fun supportCommand(audience: Audience, @Named("code") code: String) {
        if (!Regex("""^\d{8}$""").matches(code)) {
            audience.sendFailure("Support code must be 8 digits.")
            return
        }

        audience.sendMessage(status(Status.INFO, "Preparing secure support bundle...").boxed())

        async {
            try {
                val payload = SupportDiagnosticsBundle.build()
                val result = SupportDiagnosticsUploader.upload(code, payload)

                if (result.success) {
                    audience.sendMessage(
                        status(
                            Status.SUCCESS,
                            "Support bundle uploaded (${result.bundleId ?: "received"}). It expires in 24 hours."
                        ).boxed()
                    )
                } else {
                    audience.sendFailure(result.message ?: "Support bundle upload failed.")
                }
            } catch (e: Exception) {
                audience.sendFailure("Support bundle upload failed: ${e.message ?: e::class.simpleName}")
            }
        }
    }
}
