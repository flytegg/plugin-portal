package gg.flyte.pluginportal.plugin.command

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import gg.flyte.pluginportal.common.types.LocalPlugin
import gg.flyte.pluginportal.common.types.MarketplacePlatform
import gg.flyte.pluginportal.common.util.GSON
import gg.flyte.pluginportal.plugin.manager.LocalPluginCache
import gg.flyte.pluginportal.plugin.util.*
import masecla.modrinth4j.client.agent.UserAgent
import masecla.modrinth4j.main.ModrinthAPI
import masecla.modrinth4j.model.version.FileHash
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component.text
import revxrsal.commands.annotation.AutoComplete
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Subcommand
import java.awt.SystemColor.text
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

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
    fun recognizeCommand(audience: Audience, pluginFile: String) {
        val pluginFile = File("plugins", pluginFile)
        val sha512 = calculateSHA512(pluginFile)

        val version = modrinth.versions().files().getVersionByHash(FileHash.SHA512, sha512)
            .get() ?: return sendFailureMessage(audience, "Could not recognize plugin")

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