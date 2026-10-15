package gg.flyte.pluginportal.common.managers

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PluginPortalSelfUpdateVersionTest {
    @Test
    fun `numbered betas advance numerically`() {
        assertTrue(comparePluginPortalVersions("4.0.0-beta.10", "4.0.0-beta.2") > 0)
        assertEquals(0, comparePluginPortalVersions("4.0.0-beta.1", "4.0.0-beta.1"))
    }

    @Test
    fun `stable promotes a beta but a beta cannot replace the same stable version`() {
        assertTrue(comparePluginPortalVersions("4.0.0", "4.0.0-beta.1") > 0)
        assertTrue(comparePluginPortalVersions("4.0.0-beta.2", "4.0.0") < 0)
        assertTrue(comparePluginPortalVersions("4.0.0-rc.1", "4.0.0-beta.10") > 0)
    }

    @Test
    fun `base versions and legacy patch formatting remain comparable`() {
        assertTrue(comparePluginPortalVersions("4.0.1-alpha.1", "4.0.0") > 0)
        assertTrue(comparePluginPortalVersions("4.0.0-beta.1", "3.8.9") > 0)
        assertEquals(0, comparePluginPortalVersions("3.7.04", "3.7.4"))
        assertEquals(0, comparePluginPortalVersions("4.0.0+build.2", "4.0.0+build.1"))
    }
}
