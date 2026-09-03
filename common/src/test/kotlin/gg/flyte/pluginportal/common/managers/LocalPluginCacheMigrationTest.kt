package gg.flyte.pluginportal.common.managers

import gg.flyte.pluginportal.common.PlatformId
import gg.flyte.pluginportal.common.types.enums.MarketplacePlatform.HANGAR
import gg.flyte.pluginportal.common.types.enums.MarketplacePlatform.MODRINTH
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LocalPluginCacheMigrationTest {
    @Test
    fun `legacy cache entries survive startup and gain stable entry ids`() {
        val legacyCache = """
            [
              {
                "platformId": "3wmN97b8",
                "name": "Multiverse-Core",
                "version": "5.6.2",
                "platform": "MODRINTH",
                "sha256": "multiverse-sha256",
                "sha512": "multiverse-sha512",
                "installedAt": 1779562612528
              },
              {
                "platformId": "ViaVersion",
                "name": "ViaVersion",
                "version": "5.9.2-SNAPSHOT+996",
                "platform": "HANGAR",
                "sha256": "viaversion-sha256",
                "sha512": "viaversion-sha512",
                "installedAt": 1779563017721
              }
            ]
        """.trimIndent()

        val result = migrateLocalPluginCache(legacyCache) { identities ->
            assertEquals(listOf(PlatformId("3wmN97b8", MODRINTH), PlatformId("ViaVersion", HANGAR)), identities)
            mapOf(PlatformId("3wmN97b8", MODRINTH) to "resolved-multiverse-entry")
        }

        assertEquals(2, result.migratedCount)
        assertEquals(1, result.unresolvedCount)
        assertEquals("resolved-multiverse-entry", result.plugins[0].entryId)
        assertEquals("legacy:hangar:ViaVersion", result.plugins[1].entryId)
        assertEquals(2, result.plugins.toMutableSet().size)
        assertTrue(result.plugins[0].hasResolvedEntryId())
        assertTrue(!result.plugins[1].hasResolvedEntryId())
    }
}
