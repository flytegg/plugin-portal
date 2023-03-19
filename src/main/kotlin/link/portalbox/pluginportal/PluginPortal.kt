package link.portalbox.pluginportal

import link.portalbox.pluginportal.command.PPCommand
import link.portalbox.pluginportal.file.Config
import link.portalbox.pluginportal.file.Data
import link.portalbox.pluginportal.listener.ConnectionListener
import org.bukkit.plugin.java.JavaPlugin

class PluginPortal : JavaPlugin() {

    var LATEST_VERSION = true

    override fun onEnable() {
        Config.init(this)
        Data.init(this)

        server.pluginManager.registerEvents(ConnectionListener(this), this)

        val command = PPCommand()
        getCommand("pluginportal")!!.setExecutor(command)
        getCommand("pluginportal")!!.tabCompleter = command

        val latestVersionId = "3.0.0" // GET FROM API
        LATEST_VERSION = latestVersionId == description.version
        if (!LATEST_VERSION) {
            logger.severe("You are running an outdated version of Plugin Portal! Please update to the latest version.")
            logger.severe("Download Link: https://www.spigotmc.org/resources/plugin-portal.108700/")
            logger.severe("Current Version: ${description.version}")
            logger.severe("Latest Version: $latestVersionId")
        } else {
            logger.fine("Having problems? Join our support Discord @ discord.gg/portalbox.")
        }
    }

    override fun onDisable() {
        if (!LATEST_VERSION) {
            logger.severe("Please remember to update your Plugin Portal as it is outdated.")
            logger.severe("Download Link: https://www.spigotmc.org/resources/plugin-portal.108700/")
        }
    }

}