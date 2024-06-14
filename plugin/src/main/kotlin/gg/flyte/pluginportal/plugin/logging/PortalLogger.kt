package gg.flyte.pluginportal.plugin.logging

import gg.flyte.pluginportal.plugin.PluginPortal
import gg.flyte.pluginportal.plugin.appendLine
import gg.flyte.pluginportal.plugin.createIfNotExists
import gg.flyte.pluginportal.plugin.util.async
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.Audiences
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.pointer.Pointer
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import java.io.File

// TODO: After 1k entries move this to a zipped archive folder and create a new file
// TODO: Load & Read & make queryable
object PortalLogger {
    private val file = File(PluginPortal.instance.dataFolder, "history.log").createIfNotExists()

    fun log(initiator: Audience, action: Action, target: String) {
        val name: String = initiator.getOrDefault(Identity.NAME, "[UNKNOWN]")!!
        log(Record(System.currentTimeMillis(), name, action, target))
    }

    fun log(initiator: CommandSender, action: Action, target: String) =
        log(Record(System.currentTimeMillis(), initiator.name, action, target))

    fun log(record: Record) = writeToFile(record)

    private fun writeToFile(record: Record) = async { file.appendLine(record.description) }

    enum class Action() {
        // Actions are queried linearly thus AUTO_UPDATE must precede UPDATE and etc.
        INSTALL, DELETE, AUTO_UPDATE, UPDATE;

        val pastTense = toString() + if (toString().endsWith("E")) "D" else "ED"
    }

}