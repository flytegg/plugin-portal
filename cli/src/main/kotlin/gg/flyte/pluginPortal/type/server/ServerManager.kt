package gg.flyte.pluginPortal.type.server

import gg.flyte.common.api.dataClasses.MarketplacePlugin
import gg.flyte.common.type.service.PlatformType
import gg.flyte.common.util.GSON
import gg.flyte.common.util.downloadFileSync
import gg.flyte.common.util.installPlugin
import gg.flyte.pluginPortal.type.config.Config
import gg.flyte.pluginPortal.type.plugin.InstalledPlugin
import gg.flyte.pluginPortal.util.isWindows
import okhttp3.internal.notify
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit


object ServerManager {

    fun createServer(server: ServerConfig) {
        val serverFolder = File(getServerFolderDirectory(), server.name).apply { mkdir() }
        File(serverFolder, "config.ppm").let {
            it.createNewFile()
            it.writeText(GSON.toJson(server))
        }

        downloadServer(server)
        generateEula(server)
        generateFlags(server)
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
    }

    fun startServer(server: ServerConfig) {
        val executorService = Executors.newFixedThreadPool(2)

        ProcessBuilder("start.bat").apply {
            if (isWindows) command("cmd.exe", "/c", "start.bat");
            else command("sh", "-c", "start.bat");

            redirectInput(ProcessBuilder.Redirect.INHERIT)
            redirectOutput(ProcessBuilder.Redirect.INHERIT)
            directory(server.getDirectory())
        }.start()
            .let { process ->
                val streamGobbler = StreamGobbler(process.inputStream, System.out::println)

                val future: Future<*> = executorService.submit(streamGobbler)

                val exitCode = process.waitFor()

                try {
                    future.get(10, TimeUnit.SECONDS)
                } catch (e: Exception) {
                    println("Exception occurred while waiting for the future: ${e.message}")
                }

                if (exitCode == 0) {
                    println("Process completed successfully.")
                } else {
                    println("Process exited with a non-zero code: $exitCode")
                }

                executorService.shutdown()

            }
    }

    private fun downloadServer(server: ServerConfig) {
        server.softwareType.softwareInterface?.getDownloadUrl(server.version)
            ?.let { downloadUrl -> downloadFileSync(downloadUrl, File(server.getDirectory(), "server.jar")) }
    }

    fun getServerFromName(serverName: String): ServerConfig {
        val folder = File(getServerFolderDirectory(), serverName)
        val file = File(folder, "config.ppm")

        return GSON.fromJson(file.readText(), ServerConfig::class.java)
    }

    private fun generateEula(server: ServerConfig) {
        server.getDirectory().let { serverDirectory ->
            File(serverDirectory, "eula.txt").let { eulaFile ->
                if (!eulaFile.exists()) {
                    eulaFile.createNewFile()
                    if (Config.userConfig.autoAcceptEula) eulaFile.writeText("eula=true")
                    else eulaFile.writeText("eula=false")
                }
            }
        }
    }

    private fun generateFlags(server: ServerConfig) {
        server.getDirectory().let { serverDirectory ->
            File(serverDirectory, "start.bat").let { startFile ->
                if (!startFile.exists()) {
                    startFile.createNewFile()
                    startFile.writeText("java -Xmx2G --add-modules=jdk.incubator.vector -XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200 -XX:+UnlockExperimentalVMOptions -XX:+DisableExplicitGC -XX:+AlwaysPreTouch -XX:G1HeapWastePercent=5 -XX:G1MixedGCCountTarget=4 -XX:InitiatingHeapOccupancyPercent=15 -XX:G1MixedGCLiveThresholdPercent=90 -XX:G1RSetUpdatingPauseTimePercent=5 -XX:SurvivorRatio=32 -XX:+PerfDisableSharedMem -XX:MaxTenuringThreshold=1 -Dusing.aikars.flags=https://mcutils.com -Daikars.new.flags=true -XX:G1NewSizePercent=30 -XX:G1MaxNewSizePercent=40 -XX:G1HeapRegionSize=8M -XX:G1ReservePercent=20 -jar server.jar --nogui")
                }
            }
        }
    }

    fun installPluginToServer(
        plugin: MarketplacePlugin,
        url: String,
        pluginFolder: File,
        version: String,
        platformType: PlatformType
    ) {


        getActiveServer()!!.let { server ->
            println("adding plugin to server: ${plugin.displayInfo.name}")
            server.installedPlugins.add(
                InstalledPlugin(
                    plugin.id,
                    plugin.displayInfo.name,
                    version,
                    platformType,
                    plugin.primaryServiceType,
                    installPlugin(
                        plugin,
                        url,
                        pluginFolder,
                        false
                    ),
                )
            )

            server.save()
        }
    }

    fun getHomeFolderDirectory() = File(System.getProperty("java.class.path")).parentFile.parentFile
    fun getServerFolderDirectory() = File(getHomeFolderDirectory(), "servers")
}