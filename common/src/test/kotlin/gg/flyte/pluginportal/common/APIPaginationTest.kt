package gg.flyte.pluginportal.common

import gg.flyte.pluginportal.common.types.Pagination
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class APIPaginationTest {
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
