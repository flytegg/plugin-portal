package gg.flyte.pluginportal.common

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PluginBlacklistTest {

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
