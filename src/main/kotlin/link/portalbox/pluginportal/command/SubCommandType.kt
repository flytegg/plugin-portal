package link.portalbox.pluginportal.command

enum class SubCommandType(
    val command: String,
    val usage: String,
    val permission: String,
    val flags: List<Flag>?) {

    DELETE("Delete", "/pp delete (File Name)", "pluginportal.delete", null),
    HELP("Help", "/pp help", "pluginportal.help", null),
    INSTALL("Install", "/pp install (Plugin Name) <Optional Flags>", "pluginportal.install", listOf(Flag.FORCE)),
    LIST("List", "/pp list", "pluginportal.list", null),
    PREVIEW("Preview", "/pp preview (Plugin Name)", "pluginportal.preview", null),
    UPDATE("Update", "/pp update (Plugin Name) <Optional Flags>", "pluginportal.update", listOf(Flag.FORCE))

}