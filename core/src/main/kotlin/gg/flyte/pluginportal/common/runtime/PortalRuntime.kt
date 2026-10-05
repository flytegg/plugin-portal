package gg.flyte.pluginportal.common.runtime

import gg.flyte.pluginportal.common.types.enums.ServerType
import java.io.File
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.logging.Logger

data class PluginDescription(val name: String, val version: String, val authors: List<String> = emptyList())
data class InstalledPlugin(val name: String, val description: PluginDescription, val isEnabled: Boolean)
data class ServerDescription(
    val name: String,
    val version: String,
    val minecraftVersion: String,
    val onlineMode: Boolean,
    val plugins: List<InstalledPlugin>,
)

/** Platform APIs never cross this boundary into shared services. */
abstract class PortalRuntime : AutoCloseable {
    abstract val dataFolder: File
    abstract val logger: Logger
    abstract val description: PluginDescription
    abstract val server: ServerDescription
    abstract val serverTypes: List<ServerType>
    abstract val jarFile: File
    open val installDirectory: File get() = dataFolder.parentFile
    abstract val updateDirectory: File
    open val commandName: String = "pp"

    val executor = ScheduledThreadPoolExecutor(4) { work ->
        Thread(work, "PluginPortal-worker").apply { isDaemon = true }
    }.apply { removeOnCancelPolicy = true }

    val config: YamlConfiguration by lazy { YamlConfiguration.loadConfiguration(File(dataFolder, "config.yml")) }
    fun saveDefaultConfig() { saveResource("config.yml", false) }
    fun saveConfig() = config.save(File(dataFolder, "config.yml"))
    fun reloadConfig() = config.loadFromString(File(dataFolder, "config.yml").readText())
    fun saveResource(name: String, replace: Boolean) {
        val destination = File(dataFolder, name)
        if (destination.exists() && !replace) return
        destination.parentFile.mkdirs()
        requireNotNull(PortalRuntime::class.java.classLoader.getResourceAsStream(name)) { "Missing resource $name" }
            .use { input -> destination.outputStream().use(input::copyTo) }
    }

    override fun close() {
        executor.shutdown()
        if (!executor.awaitTermination(15, java.util.concurrent.TimeUnit.SECONDS)) executor.shutdownNow()
    }
}
