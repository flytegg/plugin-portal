package gg.flyte.pluginPortal.type.server

import gg.flyte.common.type.service.SoftwareType
import gg.flyte.common.util.GSON
import gg.flyte.common.util.downloadFileSync
import gg.flyte.pluginPortal.type.config.Config
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

object ServerManager {

    fun createServer(server: ServerConfig) {
        val serverFolder = File(getServerFolderDirectory(), server.name).apply { mkdir() }
        File(serverFolder, "config.ppm").let {
            it.createNewFile()
            it.writeText(GSON.toJson(server))
        }
    }

    fun getActiveServer(): ServerConfig? {
        if (Config.userConfig.activeServerName.isNullOrEmpty()) return null

        val folder = Config.userConfig.activeServerName?.let { File(getServerFolderDirectory(), it) }
        val file = folder?.let { File(it, "config.ppm") }

        return if (!file?.exists()!!) null
        else GSON.fromJson(file.readText(), ServerConfig::class.java)
    }

    fun setActiveServer(server: ServerConfig) {
        Config.userConfig.activeServerName = server.name
        Config.saveConfig()

        downloadServer()
    }

    fun startServer() {
        val server = getActiveServer()
        if (server == null) {
            println("No server found!")
            return
        }

        val process = ProcessBuilder("java -jar server.jar").start()
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        val writer = process.outputStream.bufferedWriter()

        while (true) {
            val line = reader.readLine() ?: break
            println(line)
        }
    }

    fun downloadServer() {
        when (getActiveServer()?.softwareType) {
            SoftwareType.VANILLA -> {
                //
                downloadFileSync("https://cdn.mcutils.com/jars/vanilla-1.20.1.jar", File(getActiveServer()?.getDirectory(), "server.jar"))


            }

            SoftwareType.SPIGOT -> {
                // https://cdn.mcutils.com/jars/bukkit-1.20.1.jar

            }

            SoftwareType.PAPER -> {
                // https://cdn.mcutils.com/jars/paper-1.20.1.jar
                downloadFileSync("https://cdn.mcutils.com/jars/paper-1.20.1.jar", File(getActiveServer()?.getDirectory(), "server.jar"))

            }

            SoftwareType.PURPUR -> {
                // https://cdn.mcutils.com/jars/bungeecord-1.20.1.jar

            }

            SoftwareType.FOLIA -> {

            }

            SoftwareType.WATERFALL -> {
                // https://cdn.mcutils.com/jars/waterfall-1.20.1.jar

            }

            SoftwareType.VELOCITY -> {
                // https://cdn.mcutils.com/jars/velocity-1.20.1.jar

            }

            else -> {
                println("Unsupported server type!")
            }
        }
    }

    fun getServerFromName(serverName: String): ServerConfig {
        val folder = File(getServerFolderDirectory(), serverName)
        val file = File(folder, "config.ppm")

        return GSON.fromJson(file.readText(), ServerConfig::class.java)
    }

    fun getHomeFolderDirectory() = File(System.getProperty("java.class.path")).parentFile.parentFile
    fun getServerFolderDirectory() = File(getHomeFolderDirectory(), "servers")
}