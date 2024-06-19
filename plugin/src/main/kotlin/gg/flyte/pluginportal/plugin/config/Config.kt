package gg.flyte.pluginportal.plugin.config

import dev.dejvokep.boostedyaml.YamlDocument
import gg.flyte.pluginportal.plugin.PluginPortal
import java.io.File

object Config {

    private val configFile = File("config.yml")

    val config by lazy {
        YamlDocument.create(configFile, PluginPortal.instance.getResource("config.yml")!!)
    }



}