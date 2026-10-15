package gg.flyte.pluginportal.common

import gg.flyte.pluginportal.common.types.Platforms
import gg.flyte.pluginportal.common.types.Plugin
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Date

class SearchPluginsTest {
    @Test
    fun `failed searches can be retried`() {
        val plugin = Plugin(
            id = "viaversion-test",
            name = "ViaVersion",
            totalDownloads = 0,
            platforms = Platforms(null, null, null, null),
            createdAt = Date(0),
            updatedAt = Date(0)
        )
        var attempts = 0

        val failed = SearchPlugins.search("retryable-search") {
            attempts++
            null
        }
        val retried = SearchPlugins.search("retryable-search") {
            attempts++
            arrayOf(plugin)
        }

        assertEquals(emptyList<Plugin>(), failed)
        assertEquals(listOf(plugin), retried)
        assertEquals(2, attempts)
    }
}
