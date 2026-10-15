package gg.flyte.pluginportal.common.runtime

import gg.flyte.pluginportal.common.types.LocalPlugin
import gg.flyte.pluginportal.common.types.enums.MarketplacePlatform
import gg.flyte.pluginportal.common.types.enums.ServerType
import gg.flyte.pluginportal.common.util.HashType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import java.util.logging.Logger
import kotlin.test.*

class PluginInventoryTest {
    @TempDir lateinit var root: File
    private var loaded = emptyList<InstalledPlugin>()
    private fun runtime(velocity: Boolean = false) = object : PortalRuntime() {
        override val dataFolder = File(root, "plugins/pluginportal")
        override val logger = Logger.getAnonymousLogger()
        override val description = PluginDescription("PluginPortal", "4")
        override val jarFile = File(root, "plugins/PluginPortal.jar")
        override val serverTypes = listOf(if (velocity) ServerType.VELOCITY else ServerType.PAPER)
        override val updateDirectory = if (velocity) File(dataFolder, "pending-updates") else File(root, "plugins/update")
        override val server get() = ServerDescription("Test", "1", "1", true, loaded)
    }
    private fun jar(file: File, version: String = "1", velocity: Boolean = false): File {
        file.parentFile.mkdirs()
        JarOutputStream(file.outputStream()).use {
            it.putNextEntry(JarEntry(if (velocity) "velocity-plugin.json" else "plugin.yml"))
            it.write((if (velocity) """{"id":"fixture","name":"Fixture","version":"$version"}"""
                else "name: Fixture\nversion: '$version'\nmain: test.Plugin\n").toByteArray())
            it.closeEntry()
        }
        return file
    }
    private fun managed(file: File, version: String = "1") = LocalPlugin("entry", "id", "Catalog title", version,
        MarketplacePlatform.MODRINTH, HashType.SHA256.hash(file), "", 0)
    private fun load(version: String = "1") { loaded = listOf(InstalledPlugin("Fixture", PluginDescription("Fixture", version), true)) }

    @Test fun `installed unloaded plugin needs restart but stale cache does not`() {
        runtime().use { runtime ->
            val file = jar(File(runtime.installDirectory, "fixture.jar"))
            val plugin = managed(file)
            val snapshot = runtime.inventory.snapshot(listOf(plugin))
            assertEquals("staged-install", snapshot.plugins.single().state)
            assertEquals(1, snapshot.restartCount)
            assertTrue(snapshot.restartRequired)
            assertFalse(snapshot.plugins.single().json().has("stagedVersion"))
            file.delete()
            assertFalse(runtime.inventory.snapshot(listOf(plugin)).restartRequired)
            assertTrue(runtime.inventory.snapshot(listOf(plugin)).plugins.isEmpty())
        }
    }

    @Test fun `Paper and Velocity replacements match descriptor identities and avoid double counting`() {
        for (velocity in listOf(false, true)) runtime(velocity).use { runtime ->
            val installed = jar(File(runtime.installDirectory, "old.jar"), velocity = velocity)
            val plugin = managed(installed)
            load()
            assertEquals("running", runtime.inventory.snapshot(listOf(plugin)).plugins.single().state)
            val staged = jar(File(runtime.updateDirectory, "new.jar"), "2", velocity)
            val snapshot = runtime.inventory.snapshot(listOf(plugin))
            val entry = snapshot.plugins.single()
            assertEquals("staged-update", entry.state)
            assertEquals("2", entry.stagedVersion)
            assertEquals("1", entry.runningVersion)
            assertEquals(1, snapshot.restartCount)
            assertEquals("staged-update", entry.json().get("state").asString)
            assertEquals("2", entry.json().get("stagedVersion").asString)
            staged.delete()
            installed.delete()
        }
    }

    @Test fun `cache or installed version differences report staged updates`() {
        runtime().use { runtime ->
            val installed = jar(File(runtime.installDirectory, "fixture.jar"), "2")
            load()
            val snapshot = runtime.inventory.snapshot(listOf(managed(installed, "1")))
            assertEquals("staged-update", snapshot.plugins.single().state)
            assertEquals("2", snapshot.plugins.single().stagedVersion)
            jar(installed, "1")
            val cached = runtime.inventory.snapshot(listOf(managed(installed, "2")))
            assertEquals("2", cached.plugins.single().stagedVersion)
        }
    }

    @Test fun `uninstall retains managed identity while loaded and reinstall cancels removal`() {
        runtime().use { runtime ->
            val installed = jar(File(runtime.installDirectory, "fixture.jar"))
            val plugin = managed(installed)
            load()
            runtime.inventory.recordRemoval(plugin, listOf(installed))
            // Recording an attempted uninstall is insufficient until the JAR is gone.
            assertFalse(runtime.inventory.snapshot(listOf(plugin)).restartRequired)
            installed.delete()
            val entry = runtime.inventory.snapshot(emptyList()).plugins.single()
            assertEquals("staged-removal", entry.state)
            assertEquals("id", entry.json().get("id").asString)
            assertFalse(entry.json().has("stagedVersion"))
            jar(installed)
            assertEquals("running", runtime.inventory.snapshot(listOf(plugin)).plugins.single().state)
            installed.delete()
            loaded = emptyList()
            assertFalse(runtime.inventory.snapshot(emptyList()).restartRequired)
        }
    }

    @Test fun `unknown pending files require restart and snapshot counts beyond wire cap`() {
        runtime().use { runtime ->
            runtime.updateDirectory.mkdirs()
            File(runtime.updateDirectory, "broken.jar").writeText("broken")
            loaded = (1..500).map { InstalledPlugin("Other$it", PluginDescription("Other$it", "1"), true) }
            val installed = jar(File(runtime.installDirectory, "fixture.jar"))
            load()
            runtime.inventory.recordRemoval(managed(installed), listOf(installed))
            installed.delete()
            loaded = (1..500).map { InstalledPlugin("Other$it", PluginDescription("Other$it", "1"), true) } + loaded
            val snapshot = runtime.inventory.snapshot(emptyList())
            assertEquals(501, snapshot.plugins.size)
            assertEquals(2, snapshot.restartCount)
            assertTrue(snapshot.restartRequired)
        }
    }
}
