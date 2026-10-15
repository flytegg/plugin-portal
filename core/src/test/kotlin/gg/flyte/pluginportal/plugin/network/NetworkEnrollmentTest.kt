package gg.flyte.pluginportal.plugin.network

import com.google.gson.JsonObject
import org.junit.jupiter.api.Test
import kotlin.test.*

class NetworkEnrollmentTest {
    @Test fun `confirmed identity retains the account and network reviewed in the console`() {
        val nodeId = "10000000-0000-0000-0000-000000000001"
        val networkId = "20000000-0000-0000-0000-000000000002"
        val calls = mutableListOf<String>()
        val enrollment = NetworkEnrollment({ "bukkit" }) { route, _ ->
            calls += route
            JsonObject().apply {
                addProperty("nodeId", nodeId)
                addProperty("networkId", networkId)
                addProperty("role", "controller")
                if (route == "enrollment") {
                    addProperty("ownerId", "test-owner")
                    addProperty("ownerName", "Example owner")
                    addProperty("networkName", "Example network")
                    addProperty("name", "Lobby")
                    addProperty("platform", "bukkit")
                    addProperty("expiresAt", System.currentTimeMillis() + 60_000)
                    addProperty("confirmation", "a".repeat(64))
                }
            }
        }
        enrollment.review("a".repeat(43))
        assertEquals(listOf("enrollment"), calls)
        val identity = enrollment.confirm()
        assertEquals("Example owner", identity.get("ownerName").asString)
        assertEquals("Example network", identity.get("networkName").asString)
        assertEquals("test-owner", identity.get("ownerId").asString)
        assertEquals(listOf("enrollment", "enroll"), calls)
        assertFailsWith<IllegalStateException> { enrollment.confirm() }
    }
}
