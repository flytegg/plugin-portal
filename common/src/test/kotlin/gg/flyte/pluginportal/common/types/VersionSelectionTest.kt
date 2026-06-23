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

    @Test
    fun `falls back to full version list when cached marketplace slice has no compatible version`() {
        val platform = platformEntry(
            versions = listOf(
                version("v5.5.57-neoforge", "2026-06-18T21:16:30Z", ServerType.NEOFORGE),
                version("v5.5.57-fabric", "2026-06-18T21:16:14Z", ServerType.FABRIC),
                version("v5.5.57-forge", "2026-06-18T21:15:58Z", ServerType.FORGE),
                version("v5.5.53-velocity", "2026-05-27T07:14:53Z", ServerType.VELOCITY),
            )
        )

        val selected = platform.newestCompatibleVersionWithFallback("release", listOf(ServerType.PAPER, ServerType.SPIGOT, ServerType.BUKKIT)) {
            listOf(
                version("v5.5.53-bukkit", "2026-05-27T07:14:37Z", ServerType.BUKKIT, ServerType.SPIGOT, ServerType.PAPER)
            )
        }

        assertEquals("v5.5.53-bukkit", selected?.versionNumber)
    }

    @Test
    fun `falls back to full version list when cached marketplace slice is empty`() {
        val platform = platformEntry(versions = emptyList())

        val selected = platform.newestCompatibleVersionWithFallback("release", listOf(ServerType.PAPER, ServerType.SPIGOT, ServerType.BUKKIT)) {
            listOf(
                version("1.0.0-paper", "2026-06-01T00:00:00Z", ServerType.PAPER)
            )
        }

        assertEquals("1.0.0-paper", selected?.versionNumber)
    }

    @Test
    fun `prefers current minecraft version over newer wrong-version builds`() {
        val versions = listOf(
            version("2.0.0", "2026-06-02T00:00:00Z", ServerType.PAPER, minecraftVersions = listOf("1.20.6")),
            version("1.9.0", "2026-06-01T00:00:00Z", ServerType.PAPER, minecraftVersions = listOf("1.21.4")),
        )

        val selected = versions.newestCompatibleVersion("release", listOf(ServerType.PAPER, ServerType.SPIGOT, ServerType.BUKKIT), "1.21.4")

        assertEquals("1.9.0", selected?.versionNumber)
    }

    @Test
    fun `treats minor minecraft versions as family compatibility`() {
        val versions = listOf(
            version("1.0.0", "2026-06-01T00:00:00Z", ServerType.PAPER, minecraftVersions = listOf("1.21")),
        )

        val selected = versions.newestCompatibleVersion("release", listOf(ServerType.PAPER), "1.21.4")

        assertEquals("1.0.0", selected?.versionNumber)
    }

    @Test
    fun `falls back to full version list when cached slice only has a server type fallback`() {
        val platform = platformEntry(
            versions = listOf(
                version("1.1.0-bukkit", "2026-06-02T00:00:00Z", ServerType.BUKKIT, minecraftVersions = listOf("1.21.4")),
            )
        )

        val selected = platform.newestCompatibleVersionWithFallback("release", listOf(ServerType.PAPER, ServerType.SPIGOT, ServerType.BUKKIT), "1.21.4") {
            listOf(
                version("1.0.0-paper", "2026-06-01T00:00:00Z", ServerType.PAPER, minecraftVersions = listOf("1.21.4"))
            )
        }

        assertEquals("1.0.0-paper", selected?.versionNumber)
    }

    @Test
    fun `exact version fallback prefers exact server type from full version list`() {
        val platform = platformEntry(
            versions = listOf(
                version("1.0.0", "2026-06-02T00:00:00Z", ServerType.BUKKIT, minecraftVersions = listOf("1.21.4")),
            )
        )

        val selection = platform.exactCompatibleVersionWithFallback("1.0.0", "release", listOf(ServerType.PAPER, ServerType.SPIGOT, ServerType.BUKKIT), "1.21.4") {
            listOf(
                version("1.0.0", "2026-06-01T00:00:00Z", ServerType.PAPER, minecraftVersions = listOf("1.21.4"))
            )
        }

        assertEquals("1.0.0", (selection as ExactVersionSelection.Found).version.versionNumber)
        assertEquals(0, selection.version.bestServerTypeRank(listOf(ServerType.PAPER, ServerType.SPIGOT, ServerType.BUKKIT)))
    }

    private fun version(
        versionNumber: String,
        releasedAt: String,
        vararg serverTypes: ServerType,
        channel: String = "release",
        minecraftVersions: List<String>? = null,
    ) = Version(
        versionNumber = versionNumber,
        releasedAt = Date.from(Instant.parse(releasedAt)),
        releaseChannel = channel,
        downloadURL = "https://example.com/$versionNumber.jar",
        supportedVersions = null,
        mcVersions = minecraftVersions,
        serverTypes = arrayOf(*serverTypes),
        sha256 = null,
    )

    private fun platformEntry(versions: List<Version>) = ModrinthPlatformEntry(
        entryId = "entry",
        platform = gg.flyte.pluginportal.common.types.enums.MarketplacePlatform.MODRINTH,
        platformId = "luckperms",
        author = "lucko",
        description = null,
        iconURL = null,
        downloads = 0,
        lastModified = null,
        versions = versions,
        followers = 0,
        lastSynced = Date.from(Instant.parse("2026-06-18T21:16:30Z")),
    )
}
