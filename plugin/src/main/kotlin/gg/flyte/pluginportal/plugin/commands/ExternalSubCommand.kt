package gg.flyte.pluginportal.plugin.commands

import gg.flyte.pluginportal.common.chat.sendFailure
import gg.flyte.pluginportal.common.chat.sendInfo
import gg.flyte.pluginportal.common.chat.sendSuccess
import gg.flyte.pluginportal.common.commands.lamp.ExternalPluginSuggestionProvider
import gg.flyte.pluginportal.common.managers.ExternalPluginManager
import gg.flyte.pluginportal.common.managers.ExternalPluginResult
import gg.flyte.pluginportal.plugin.commands.lamp.RequiresAuth
import net.kyori.adventure.audience.Audience
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Named
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.annotation.SuggestWith
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("pp", "pluginportal", "ppm")
@CommandPermission("pluginportal.manage.external")
class ExternalSubCommand {
    @RequiresAuth
    @Subcommand("external check")
    fun check(
        audience: Audience,
        @Named("id") @SuggestWith(ExternalPluginSuggestionProvider::class) id: String
    ) = runAsync(audience) { ExternalPluginManager.check(id) }

    @RequiresAuth
    @Subcommand("external install")
    fun install(
        audience: Audience,
        @Named("id") @SuggestWith(ExternalPluginSuggestionProvider::class) id: String
    ) = runAsync(audience) { ExternalPluginManager.install(id) }

    @RequiresAuth
    @Subcommand("external update")
    fun update(
        audience: Audience,
        @Named("id") @SuggestWith(ExternalPluginSuggestionProvider::class) id: String
    ) = runAsync(audience) { ExternalPluginManager.update(id) }

    @RequiresAuth
    @Subcommand("external invalidate")
    fun invalidate(
        audience: Audience,
        @Named("id") @SuggestWith(ExternalPluginSuggestionProvider::class) id: String
    ) = runAsync(audience) { ExternalPluginManager.invalidate(id) }

    @RequiresAuth
    @Subcommand("external updateAll")
    fun updateAll(audience: Audience) {
        executeAsync(audience) {
            val results = ExternalPluginManager.updateAll(includeManual = true)
            if (results.isEmpty()) return@executeAsync audience.sendInfo("No installed external plugins are eligible for updates")

            results.forEach { result ->
                if (result.success) audience.sendSuccess(result.message) else audience.sendFailure(result.message)
            }
            val changed = results.count { it.changed }
            val failed = results.count { !it.success }
            audience.sendInfo("External update complete: $changed updated, $failed failed")
        }
    }

    @RequiresAuth
    @Subcommand("external reload")
    fun reload(audience: Audience) {
        executeAsync(audience) {
            val errors = ExternalPluginManager.reload()
            if (errors.isEmpty()) {
                audience.sendSuccess("Reloaded external-plugins.yml")
            } else {
                audience.sendFailure("Reloaded external-plugins.yml with errors: ${errors.joinToString("; ")}")
            }
        }
    }

    private fun runAsync(
        audience: Audience,
        operation: () -> ExternalPluginResult
    ) {
        executeAsync(audience) {
            val result = operation()
            if (result.success) audience.sendSuccess(result.message) else audience.sendFailure(result.message)
        }
    }

    private fun executeAsync(audience: Audience, operation: () -> Unit) {
        ExternalPluginManager.executeAsync {
            runCatching(operation).onFailure { throwable ->
                val message = throwable.message ?: throwable::class.simpleName ?: "Unknown error"
                audience.sendFailure("External plugin operation failed: $message")
            }
        }
    }
}
