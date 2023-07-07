package link.portalbox.pluginportal.command

enum class SubCommandType(
        val command: String,
        val usage: String,
        val alias: String,
        val permission: String,
        val isAsync: Boolean = false) {

    HELP("Help", "/pp help", "h", "pluginportal.help"),
    PREVIEW("Preview", "/pp preview (Plugin Name)", "p", "pluginportal.preview", true),
    INSTALL("Install", "/pp install (Plugin Name)", "i", "pluginportal.install", true),
    LIST("List", "/pp list", "l", "pluginportal.list"),
    UPDATE("Update", "/pp update (Plugin Name)", "u", "pluginportal.update", true),
    DELETE("Delete", "/pp delete (File Name)", "d", "pluginportal.delete", true),
    UPDATEALL("UpdateAll", "/pp updateall", "ua", "pluginportal.updateall", true),
    REQUEST("Request", "/pp request (Plugin Name)", "r", "pluginportal.request", true),

}