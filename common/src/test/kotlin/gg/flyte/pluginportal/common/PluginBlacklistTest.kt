package gg.flyte.pluginportal.common

import gg.flyte.pluginportal.common.util.declaredPluginName
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PluginBlacklistTest {

    private fun jarWith(fileName: String, entry: String?, contents: String?): File {
        val jar = File.createTempFile(fileName, ".jar")
        jar.deleteOnExit()
        ZipOutputStream(jar.outputStream()).use { out ->
            if (entry != null && contents != null) {
                out.putNextEntry(ZipEntry(entry))
                out.write(contents.toByteArray())
                out.closeEntry()
            }
            out.putNextEntry(ZipEntry("dummy.class"))
            out.closeEntry()
        }
        return jar
    }

    @Test
    fun `reads the declared name from plugin yml`() {
        val jar = jarWith("[PP] Multiverse-Core (MODRINTH)", "plugin.yml", "name: Multiverse-Core\nversion: 5.7.3\n")

        assertEquals("Multiverse-Core", jar.declaredPluginName)
    }

    @Test
    fun `falls back to paper-plugin yml`() {
        val jar = jarWith("renamed", "paper-plugin.yml", "name: PrivateCore\nversion: 1.0\n")

        assertEquals("PrivateCore", jar.declaredPluginName)
    }

    @Test
    fun `returns null when the jar declares no name`() {
        val jar = jarWith("nameless", null, null)

        assertNull(jar.declaredPluginName)
    }

    @Test
    fun `matches exact name case-insensitively`() {
        assertTrue(Config.matchesBlacklistEntry("PrivateCore", "privatecore"))
        assertFalse(Config.matchesBlacklistEntry("PrivateCoreExtra", "PrivateCore"))
    }

    @Test
    fun `matches prefix wildcard`() {
        assertTrue(Config.matchesBlacklistEntry("MyServerEssentials", "MyServer*"))
        assertTrue(Config.matchesBlacklistEntry("myserver-core-1.2.0", "MyServer*"))
        assertFalse(Config.matchesBlacklistEntry("EssentialsMyServer", "MyServer*"))
    }

    @Test
    fun `matches wildcard anywhere in entry`() {
        assertTrue(Config.matchesBlacklistEntry("SkyBlockAddon", "*Addon"))
        assertTrue(Config.matchesBlacklistEntry("CoreLibPatch", "Core*Patch"))
        assertFalse(Config.matchesBlacklistEntry("CorePatcher", "Core*Patch"))
    }

    @Test
    fun `escapes regex metacharacters in entries`() {
        assertFalse(Config.matchesBlacklistEntry("PluginX", "Plugin."))
        assertTrue(Config.matchesBlacklistEntry("Plugin.", "Plugin."))
        assertTrue(Config.matchesBlacklistEntry("Lib(v2)-final", "Lib(v2)*"))
    }
}
