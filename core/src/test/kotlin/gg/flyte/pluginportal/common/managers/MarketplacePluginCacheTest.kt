package gg.flyte.pluginportal.common.managers

import gg.flyte.pluginportal.common.types.Platforms
import gg.flyte.pluginportal.common.types.Plugin
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Date

class MarketplacePluginCacheTest {
    @Test
    fun `installing a unique exact name selects it over related search results`() {
        val exact = plugin("ViaVersion", downloads = 1)
        val related = plugin("ViaVersionStatus", downloads = 10_000)

        val resolution = MarketplacePluginCache.resolvePluginSearch("viaversion", listOf(related, exact))

        assertEquals(MarketplacePluginCache.SearchResolution.Exact(exact), resolution)
    }

    @Test
    fun `duplicate exact names exclude merely related plugins from choices`() {
        val first = plugin("ViaVersion", downloads = 20)
        val second = plugin("viaversion", downloads = 10)
        val related = plugin("ViaVersionStatus", downloads = 1_000)

        val resolution = MarketplacePluginCache.resolvePluginSearch("ViaVersion", listOf(related, second, first))

        assertEquals(MarketplacePluginCache.SearchResolution.AmbiguousExact(listOf(first, second)), resolution)
    }

    @Test
    fun `a fuzzy result is presented for confirmation instead of auto-selected`() {
        val related = plugin("ViaVersionStatus")

        val resolution = MarketplacePluginCache.resolvePluginSearch("ViaVersionSta", listOf(related))

        assertEquals(MarketplacePluginCache.SearchResolution.Results(listOf(related)), resolution)
    }

    @Test
    fun `exact mode rejects search results without an exact name`() {
        val related = plugin("ViaVersionStatus")

        val resolution = MarketplacePluginCache.resolvePluginSearch("ViaVersion", listOf(related), exactOnly = true)

        assertEquals(MarketplacePluginCache.SearchResolution.NotFound, resolution)
    }

    private fun plugin(name: String, downloads: Int = 0) = Plugin(
        id = name,
        name = name,
        totalDownloads = downloads,
        platforms = Platforms(null, null, null, null),
        createdAt = Date(0),
        updatedAt = Date(0)
    )
}
