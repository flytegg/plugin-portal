package link.portalbox.pluginportal.command

enum class SubCommandType(
    val command: String,
    val usage: String,
    val alias: String,
    val permission: String,
    val flags: List<Flag>?) {

    DELETE("Delete", "/pp delete (File Name)", "d", "pluginportal.delete", null),
    HELP("Help", "/pp help", "h", "pluginportal.help", null),
    INSTALL("Install", "/pp install (Plugin Name) <Optional Flags>", "i", "pluginportal.install", listOf(Flag.FORCE)),
    LIST("List", "/pp list", "l", "pluginportal.list", null),
    PREVIEW("Preview", "/pp preview (Plugin Name)", "p","pluginportal.preview", null),
    UPDATE("Update", "/pp update (Plugin Name) <Optional Flags>", "u", "pluginportal.update", listOf(Flag.FORCE))

}