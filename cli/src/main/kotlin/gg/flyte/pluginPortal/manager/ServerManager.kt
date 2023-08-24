package gg.flyte.pluginPortal.manager

import gg.flyte.common.type.service.SoftwareType
import gg.flyte.common.util.GSON
import gg.flyte.pluginPortal.`object`.serializer.SerializedServer
import java.awt.Desktop
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

object ServerManager {

    var activeServerFile: File? = null

    fun createServer(server: SerializedServer) {
        println(getHomeFolderDirectory().absolutePath)

        val serverFolder = File(getServerFolderDirectory(), server.name).apply { mkdir() }
        File(serverFolder, "PluginPortal_Server.json").let {
            it.createNewFile()
            it.writeText(GSON.toJson(server))
            println(serverFolder.absolutePath)
        }
    }

    fun getActiveServer(): SerializedServer? = if (activeServerFile?.exists() == true)
        GSON.fromJson(activeServerFile!!.readText(), SerializedServer::class.java) else null

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
                // https://cdn.mcutils.com/jars/vanilla-1.20.1.jar


            }

            SoftwareType.SPIGOT -> {
                // https://cdn.mcutils.com/jars/bukkit-1.20.1.jar

            }

            SoftwareType.PAPER -> {
                // https://cdn.mcutils.com/jars/paper-1.20.1.jar

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

    fun getHomeFolderDirectory() = File(System.getProperty("java.class.path")).parentFile.parentFile
    fun getServerFolderDirectory() = File(getHomeFolderDirectory(), "servers")
}