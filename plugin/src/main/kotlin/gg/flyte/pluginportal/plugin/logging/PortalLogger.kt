package gg.flyte.pluginportal.plugin.logging

import gg.flyte.pluginportal.plugin.PluginPortal
import gg.flyte.pluginportal.plugin.util.appendLine
import gg.flyte.pluginportal.plugin.util.async
import gg.flyte.pluginportal.plugin.util.createIfNotExists
import gs.mclo.api.MclogsClient
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.identity.Identity
import org.bukkit.command.CommandSender
import java.io.File
import java.util.logging.Level

// TODO: After 1k entries move this to a zipped archive folder and create a new file
// TODO: Load & Read & make queryable
object PortalLogger {
    private val file = File(PluginPortal.instance.dataFolder, "history.log").createIfNotExists()

    fun info(action: Action, message: String) = log(Record(System.currentTimeMillis(), "SYSTEM", action, message))

    fun log(initiator: Audience, action: Action, target: String) {
        val name: String = initiator.getOrDefault(Identity.NAME, "[UNKNOWN]")!!
        log(Record(System.currentTimeMillis(), name, action, target))
    }

    fun log(initiator: CommandSender, action: Action, target: String) =
        log(Record(System.currentTimeMillis(), initiator.name, action, target))

    fun log(record: Record) {
        PluginPortal.instance.logger.log(Level.INFO, record.timelessDescription)
        writeToFile(record)
    }

    private fun writeToFile(record: Record) = async { file.appendLine(record.description) }

    enum class Action {
        // Actions are queried linearly thus AUTO_UPDATE must precede UPDATE etc.
        INITIATED_INSTALL, FAILED_INSTALL, INSTALL,
        DELETE,
        AUTO_UPDATE, INITIATED_UPDATE, FAILED_UPDATE, UPDATE,
        LOAD_PLUGINS, SAVE_PLUGINS;
    }
}