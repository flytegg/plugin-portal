package link.portalbox.pluginportal.command

import java.util.*

enum class SubCommandType(
        val command: String,
        val usage: String,
        val alias: String,
        val permission: String,
        val isAsync: Boolean = false
) {
    HELP("Help", "/pp help", "h", "pluginportal.help"),
    PREVIEW("Preview", "/pp preview (Plugin Name)", "p", "pluginportal.preview", true),
    INSTALL("Install", "/pp install (Plugin Name)", "i", "pluginportal.install", true),
    LIST("List", "/pp list", "l", "pluginportal.list"),
    UPDATE("Update", "/pp update (Plugin Name)", "u", "pluginportal.update", true),
    DELETE("Delete", "/pp delete (File Name)", "d", "pluginportal.delete", true),
    UPDATEALL("UpdateAll", "/pp updateall", "ua", "pluginportal.updateall", true),
    REQUEST("Request", "/pp request (Plugin Name)", "r", "pluginportal.request", true);

    companion object {
        /** Cache to improve performance */
        val values = values()
        val commandNames = values.map { it.command.lowercase(Locale.getDefault()) }

        fun byName(name: String) = values.find { name.equals(it.name, true) || name.equals(it.alias, true) }
    }

}