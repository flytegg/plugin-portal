package gg.flyte.pluginportal.common

import gg.flyte.pluginportal.common.types.Pagination
import gg.flyte.pluginportal.common.types.Version
import gg.flyte.pluginportal.common.types.newestCompatibleVersion
import gg.flyte.pluginportal.common.types.enums.ServerType
import java.util.Date
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class APIPaginationTest {
    private fun page(offset: Int, more: Boolean, channel: String = "release") = ORPCPlatformVersionsResponse(
        arrayOf(Version("version-$offset", Date(1000L - offset), channel, "https://example.com/$offset.jar",
            null, listOf("1.21.4"), arrayOf(ServerType.PAPER), null)),
        Pagination(total = 2, limit = 1, offset = offset, hasMore = more),
    )

    @Test
    fun `selection sees stable release on later page after compatible alpha`() {
        val offsets = mutableListOf<Int>()
        val versions = collectPlatformVersionPages { offset ->
            offsets.add(offset)
            if (offset == 0) page(0, true, "alpha") else page(1, false)
        }
        assertEquals(listOf(0, 1), offsets)
        assertEquals("version-1", versions?.toList()?.newestCompatibleVersion(null, listOf(ServerType.PAPER), "1.21.4")?.versionNumber)
    }

    @Test
    fun `later page failure discards incomplete history`() {
        val versions = collectPlatformVersionPages { offset -> if (offset == 0) page(0, true, "alpha") else null }
        assertNull(versions)
    }

    @Test
    fun `non advancing page discards incomplete history`() {
        assertNull(collectPlatformVersionPages { page(0, true) })
    }

    @Test
    fun `endless pagination is bounded and not returned as complete`() {
        var requests = 0
        assertNull(collectPlatformVersionPages { offset -> requests++; page(offset, true) })
        assertEquals(20, requests)
    }

    @Test
    fun `advances to the next platform version page while more results exist`() {
        val pagination = Pagination(total = 750, limit = 500, offset = 0, hasMore = true)

        assertEquals(500, nextPlatformVersionsOffset(pagination, currentOffset = 0, receivedCount = 500))
    }

    @Test
    fun `stops platform version paging when there are no more results`() {
        val pagination = Pagination(total = 500, limit = 500, offset = 0, hasMore = false)

        assertNull(nextPlatformVersionsOffset(pagination, currentOffset = 0, receivedCount = 500))
    }

    @Test
    fun `stops platform version paging on empty pages`() {
        val pagination = Pagination(total = 750, limit = 500, offset = 500, hasMore = true)

        assertNull(nextPlatformVersionsOffset(pagination, currentOffset = 500, receivedCount = 0))
    }

    @Test
    fun `stops platform version paging when pagination does not advance`() {
        val pagination = Pagination(total = 750, limit = 0, offset = 500, hasMore = true)

        assertNull(nextPlatformVersionsOffset(pagination, currentOffset = 500, receivedCount = 100))
    }
}
