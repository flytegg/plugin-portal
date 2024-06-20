package gg.flyte.pluginportal.plugin.command

import gg.flyte.pluginportal.common.types.LocalPlugin
import gg.flyte.pluginportal.common.types.MarketplacePlatform
import gg.flyte.pluginportal.plugin.chat.Status
import gg.flyte.pluginportal.plugin.chat.boxed
import gg.flyte.pluginportal.plugin.chat.sendFailureMessage
import gg.flyte.pluginportal.plugin.chat.status
import gg.flyte.pluginportal.plugin.manager.LocalPluginCache
import gg.flyte.pluginportal.plugin.util.*
import masecla.modrinth4j.client.agent.UserAgent
import masecla.modrinth4j.main.ModrinthAPI
import masecla.modrinth4j.model.version.FileHash
import net.kyori.adventure.audience.Audience
import revxrsal.commands.annotation.AutoComplete
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Optional
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.annotation.CommandPermission
import java.io.File

@Command("pp", "pluginportal", "ppm")
class RecognizeSubCommand {

    private val userAgent = UserAgent.builder()
        .projectName("PluginPortal (A SERVER NOT DEVELOPER)")
        .projectVersion("2.0.0")
        .contact("Unknown")
        .authorUsername("Flyte")
        .build()

    private val modrinth = ModrinthAPI.unlimited(userAgent, "")

    @Subcommand("recognize")
    @AutoComplete("@pluginFileSearch *")
    @CommandPermission("pluginportal.manage.recognize")
    fun recognizeCommand(audience: Audience, @Optional pluginFileName: String? = null) {
        if (pluginFileName == null)
            return sendFailureMessage(audience, "No plugin file name provided")

        val pluginFile = File("plugins", pluginFileName)

        if (!pluginFile.exists() || pluginFile.extension != "jar")
            return sendFailureMessage(audience, "Could not find a plugin file at $pluginFile")

        val sha512 = calculateSHA512(pluginFile)

        if (LocalPluginCache.any { it.sha512 == sha512 })
            return sendFailureMessage(audience, "PluginPortal already recognizes $pluginFileName")

        val version = modrinth.versions().files().getVersionByHash(FileHash.SHA512, sha512).get()
            ?: return sendFailureMessage(audience, "Could not recognize plugin")

        val project = modrinth.projects().get(version.projectId).get()

        val localPlugin = LocalPlugin(
            id = project.id,
            name = project.title,
            platform = MarketplacePlatform.MODRINTH,
            sha256 = calculateSHA256(pluginFile),
            sha512 = sha512,
            installedAt = System.currentTimeMillis(),
        )

        LocalPluginCache.add(localPlugin)

        audience.sendMessage(
            status(
                Status.SUCCESS,
                "Recognized plugin ${project.title} from Modrinth. This has been added to your local plugin cache."
            ).boxed()
        )
    }
}