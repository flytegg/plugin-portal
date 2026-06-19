package gg.flyte.pluginportal.common.types

import gg.flyte.pluginportal.common.types.enums.ServerType
import java.time.Instant
import java.util.Date
import kotlin.test.Test
import kotlin.test.assertEquals

class VersionSelectionTest {
    @Test
    fun `chooses newest compatible version behind newer incompatible builds`() {
        val versions = listOf(
            version("v5.5.54-fabric", "2026-05-29T21:16:18Z", ServerType.FABRIC),
            version("v5.5.53-velocity", "2026-05-27T07:14:53Z", ServerType.VELOCITY),
            version("v5.5.53-bungee", "2026-05-27T07:14:45Z", ServerType.BUNGEECORD),
            version("v5.5.53-bukkit", "2026-05-27T07:14:37Z", ServerType.BUKKIT, ServerType.SPIGOT, ServerType.PAPER),
            version("v5.5.52-bukkit", "2026-05-20T07:14:37Z", ServerType.BUKKIT, ServerType.SPIGOT, ServerType.PAPER),
        )

        val selected = versions.newestCompatibleVersion("release", listOf(ServerType.PAPER, ServerType.SPIGOT, ServerType.BUKKIT))

        assertEquals("v5.5.53-bukkit", selected?.versionNumber)
    }

    @Test
    fun `keeps release channel separate from beta channel`() {
        val versions = listOf(
            version("2.0.0-beta", "2026-06-02T00:00:00Z", ServerType.PAPER, channel = "beta"),
            version("1.9.0", "2026-06-01T00:00:00Z", ServerType.PAPER),
        )

        val selected = versions.newestCompatibleVersion("release", listOf(ServerType.PAPER, ServerType.SPIGOT, ServerType.BUKKIT))

        assertEquals("1.9.0", selected?.versionNumber)
    }

    @Test
    fun `prefers a more specific server type over a newer fallback type`() {
        val versions = listOf(
            version("1.1.0-bukkit", "2026-06-02T00:00:00Z", ServerType.BUKKIT),
            version("1.0.0-paper", "2026-06-01T00:00:00Z", ServerType.PAPER),
        )

        val selected = versions.newestCompatibleVersion("release", listOf(ServerType.PAPER, ServerType.SPIGOT, ServerType.BUKKIT))

        assertEquals("1.0.0-paper", selected?.versionNumber)
    }

    @Test
    fun `does not let older spigot builds beat newer bukkit builds on paper`() {
        val versions = listOf(
            version("5.4.0", "2025-08-09T00:30:00Z", ServerType.BUKKIT),
            version("5.3.0", "2024-10-20T23:20:00Z", ServerType.SPIGOT),
        )

        val selected = versions.newestCompatibleVersion("release", listOf(ServerType.PAPER, ServerType.SPIGOT, ServerType.BUKKIT))

        assertEquals("5.4.0", selected?.versionNumber)
    }

    @Test
    fun `keeps paper fallback ahead of bukkit fallback on folia`() {
        val versions = listOf(
            version("1.1.0-bukkit", "2026-06-02T00:00:00Z", ServerType.BUKKIT),
            version("1.0.0-paper", "2026-06-01T00:00:00Z", ServerType.PAPER),
        )

        val selected = versions.newestCompatibleVersion("release", listOf(ServerType.FOLIA, ServerType.PAPER, ServerType.SPIGOT, ServerType.BUKKIT))

        assertEquals("1.0.0-paper", selected?.versionNumber)
    }

    private fun version(
        versionNumber: String,
        releasedAt: String,
        vararg serverTypes: ServerType,
        channel: String = "release",
    ) = Version(
        versionNumber = versionNumber,
        releasedAt = Date.from(Instant.parse(releasedAt)),
        releaseChannel = channel,
        downloadURL = "https://example.com/$versionNumber.jar",
        supportedVersions = null,
        serverTypes = arrayOf(*serverTypes),
        sha256 = null,
    )
}
