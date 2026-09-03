package gg.flyte.pluginportal.common.managers

import gg.flyte.pluginportal.common.types.Platforms
import gg.flyte.pluginportal.common.types.Plugin
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Date

class MarketplacePluginCacheTest {
    @Test
    fun `a unique exact name wins over related prefix matches`() {
        val exact = plugin("ViaVersion")
        val related = plugin("ViaVersionStatus")

        val selected = with(MarketplacePluginCache) {
            listOf(related, exact).preferUniqueExactName("viaversion")
        }

        assertEquals(listOf(exact), selected)
    }

    @Test
    fun `duplicate exact names still require disambiguation`() {
        val first = plugin("ViaVersion")
        val second = plugin("viaversion")
        val matches = listOf(first, second)

        val selected = with(MarketplacePluginCache) {
            matches.preferUniqueExactName("ViaVersion")
        }

        assertEquals(matches, selected)
    }

    private fun plugin(name: String) = Plugin(
        id = name,
        name = name,
        totalDownloads = 0,
        platforms = Platforms(null, null, null, null),
        createdAt = Date(0),
        updatedAt = Date(0)
    )
}
