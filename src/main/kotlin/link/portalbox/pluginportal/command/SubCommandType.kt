package link.portalbox.pluginportal.command

enum class SubCommandType(
        val command: String,
        val usage: String,
        val alias: String,
        val permission: String) {

    HELP("Help", "/pp help", "h", "pluginportal.help"),
    PREVIEW("Preview", "/pp preview (Plugin Name)", "p", "pluginportal.preview"),
    INSTALL("Install", "/pp install (Plugin Name)", "i", "pluginportal.install"),
    LIST("List", "/pp list", "l", "pluginportal.list"),
    UPDATE("Update", "/pp update (Plugin Name)", "u", "pluginportal.update"),
    DELETE("Delete", "/pp delete (File Name)", "d", "pluginportal.delete"),
    UPDATEALL("UpdateAll", "/pp updateall", "ua", "pluginportal.updateall"),
    REQUEST("Request", "/pp request (Plugin Name)", "r", "pluginportal.request"),

}