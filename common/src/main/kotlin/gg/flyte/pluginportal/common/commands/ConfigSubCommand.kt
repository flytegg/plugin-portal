package gg.flyte.pluginportal.common.commands

import gg.flyte.pluginportal.common.Config
import gg.flyte.pluginportal.common.chat.sendFailure
import gg.flyte.pluginportal.common.chat.sendSuccess
import gg.flyte.pluginportal.common.managers.ExternalPluginManager
import gg.flyte.pluginportal.common.managers.LocalPluginCache
import net.kyori.adventure.audience.Audience
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("pp", "pluginportal", "ppm")
class ConfigSubCommand {

    @Subcommand("config refresh", "config reload", "reload")
    @CommandPermission("pluginportal.manage.config")
    fun refreshConfig(audience: Audience) {
        Config.reload()
        LocalPluginCache.reloadCache()
        val externalErrors = ExternalPluginManager.reload()
        if (externalErrors.isEmpty()) {
            audience.sendSuccess("Reloaded config.yml, plugins.json, and external-plugins.yml")
        } else {
            audience.sendFailure(
                "Reloaded config.yml and plugins.json, but external-plugins.yml has errors: ${externalErrors.joinToString("; ")}"
            )
        }
    }

}
