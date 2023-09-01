package gg.flyte.pluginPortal.type.server

import com.github.ajalt.mordant.table.table
import gg.flyte.common.api.dataClasses.MarketplacePlugin
import gg.flyte.common.type.service.PlatformType
import gg.flyte.pluginPortal.type.config.Config
import gg.flyte.common.type.plugin.InstalledPlugin
import gg.flyte.common.util.*
import gg.flyte.pluginPortal.util.isWindows
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
        generateOpsFile(server)
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
            if (isWindows) command("cmd.exe", "/c", "start.bat")
            else command("sh", "-c", "start.bat")

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

    private fun generateOpsFile(server: ServerConfig) {
        server.getDirectory().let { serverDirectory ->
            File(serverDirectory, "ops.json").let { opsFile ->
                if (!opsFile.exists()) {
                    opsFile.createNewFile()
                    opsFile.writeText(Config.userConfig.defaultOperators.toJson())
                }
            }
        }
    }

    fun loadPreset(preset: ServerConfig) {
        if (getServerNameList().contains(preset.name)) {
            Config.terminal.println(table {
                header { row("Server already exists!") }
                body { row("Delete it with /ppcli server delete ${preset.name}") }
            })
            return
        }

        createServer(preset)
    }

    fun installPluginToServer(
        plugin: MarketplacePlugin,
        url: String,
        pluginFolder: File,
        version: String,
        platformType: PlatformType
    ) {

        if (!isJARFileDownload(url)) {
            Config.terminal.println(table {
                header { row("${plugin.displayInfo.name} is not a valid download!") }
                body { row("Plugin has been requested to be updated.") }
            })
            return
        }

        if (getActiveServer()!!.installedPlugins.any { it.id == plugin.id }) {
            Config.terminal.println(table {
                header { row("Plugin already installed!") }
                body { row("Update it with /ppcli plugins update") }
            })
            return
        }


        getActiveServer()!!.let { server ->
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

            Config.terminal.println(table {
                header { row("Installed plugin: ${plugin.displayInfo.name}") }
                footer { row("Total Plugins: ${getActiveServer()!!.installedPlugins.size} | Server Name: ${getActiveServer()!!.name}") }
            })

            server.save()
        }
    }

    fun noServerFoundCheck(): Boolean {
        getServerList().let {servers ->
            if (servers.isEmpty()) {
                Config.terminal.println(table {
                    header { row("No Servers found") }
                    body { row("Create one with /ppcli server create") }
                })
                return true
            }
        }

        if (getActiveServer() == null) {
            Config.terminal.println(table {
                header { row("No active server found! Please use /ppcli server select") }
                body { row("Total Server Count: ${getServerList().size}") }
            })
            return true
        }
        return false
    }

    fun getServerList() = getServerFolderDirectory()
        .listFiles()!!
        .filter { it.isDirectory }
        .map { GSON.fromJson(File(it, "config.ppm").readText(), ServerConfig::class.java) }

    fun getServerNameList() = getServerFolderDirectory()
        .listFiles()!!
        .filter { it.isDirectory }
        .map { it.name }

    fun getPresetList() = getPresetsFolderDirectory()
        .listFiles()!!
        .filter { it.isFile }
        .map { GSON.fromJson(it.readText(), ServerConfig::class.java) }

    fun getHomeFolderDirectory() = File(System.getProperty("java.class.path")).parentFile.parentFile
    fun getServerFolderDirectory() = File(getHomeFolderDirectory(), "servers")
    fun getPresetsFolderDirectory() = File(getHomeFolderDirectory(), "presets")

}