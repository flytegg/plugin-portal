package link.portalbox.pluginportal.file

import link.portalbox.pluginportal.PluginPortal
import org.bukkit.configuration.file.FileConfiguration

object Config {
    private lateinit var config: FileConfiguration

    val enable_plugins_on_install_very_experimental_dont_recommend_enabling_will_cause_corruption get() = config.getBoolean("enable-plugins-on-install-very-experimental-dont-recommend-enabling-will-cause-corruption")

    fun init(pluginPortal: PluginPortal) {
        pluginPortal.saveDefaultConfig()
        config = pluginPortal.config
    }
}