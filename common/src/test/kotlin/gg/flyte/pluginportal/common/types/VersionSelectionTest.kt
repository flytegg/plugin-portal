package gg.flyte.pluginportal.common.types

import gg.flyte.pluginportal.common.types.enums.ServerType
import java.time.Instant
import java.util.Date
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class VersionSelectionTest {
    private fun cachedAlpha() = version("3.0-alpha", "2026-06-03T00:00:00Z", ServerType.PAPER,
        channel = "alpha", minecraftVersions = listOf("1.21.4"))

    private fun installedPlugin() = LocalPlugin("entry", "project", "Example", "1.0",
        gg.flyte.pluginportal.common.types.enums.MarketplacePlatform.MODRINTH, "installed-sha256", "installed-sha512", 0L)

    @Test
    fun `update selects later stable instead of cached newer alpha`() {
        val alpha = cachedAlpha()
        val selected = installedPlugin().targetUpdateVersion(platformEntry(listOf(alpha)), listOf(ServerType.PAPER), "1.21.4") {
            listOf(alpha, version("2.0", "2026-06-02T00:00:00Z", ServerType.PAPER, minecraftVersions = listOf("1.21.4")))
        }
        assertEquals("2.0", selected?.versionNumber)
    }

    @Test
    fun `update does not resurrect cached alpha when full stable is already installed`() {
        val alpha = cachedAlpha()
        val selected = installedPlugin().targetUpdateVersion(platformEntry(listOf(alpha)), listOf(ServerType.PAPER), "1.21.4") {
            listOf(alpha, version("1.0", "2026-06-02T00:00:00Z", ServerType.PAPER, minecraftVersions = listOf("1.21.4")))
        }
        assertNull(selected)
    }

    @Test
    fun `update with failed lookup does not fall back to implicit alpha`() {
        assertNull(installedPlugin().targetUpdateVersion(platformEntry(listOf(cachedAlpha())), listOf(ServerType.PAPER), "1.21.4") { null })
    }

    @Test
    fun `cached compatible alpha does not hide stable on later pages`() {
        val alpha = cachedAlpha()
        var fetched = false
        val selected = platformEntry(listOf(alpha)).newestCompatibleVersionWithFallback(null, listOf(ServerType.PAPER), "1.21.4") {
            fetched = true
            listOf(alpha, version("2.0", "2026-06-02T00:00:00Z", ServerType.PAPER, minecraftVersions = listOf("1.21.4")))
        }
        assertEquals(true, fetched)
        assertEquals("2.0", selected?.versionNumber)
    }

    @Test
    fun `full lookup with incompatible stable does not resurrect cached alpha`() {
        val alpha = cachedAlpha()
        val selected = platformEntry(listOf(alpha)).newestCompatibleVersionWithFallback(null, listOf(ServerType.PAPER), "1.21.4") {
            listOf(alpha, version("2.0", "2026-06-02T00:00:00Z", ServerType.PAPER, minecraftVersions = listOf("1.20.6")))
        }
        assertNull(selected)
    }

    @Test
    fun `failed full lookup cannot establish that a project only has prereleases`() {
        val selected = platformEntry(listOf(cachedAlpha())).newestCompatibleVersionWithFallback(null, listOf(ServerType.PAPER), "1.21.4") { null }
        assertNull(selected)
    }

    @Test
    fun `complete prerelease only project remains installable`() {
        val alpha = cachedAlpha()
        val selected = platformEntry(listOf(alpha)).newestCompatibleVersionWithFallback(null, listOf(ServerType.PAPER), "1.21.4") { listOf(alpha) }
        assertEquals(alpha, selected)
    }

    @Test
    fun `explicit alpha channel can use a compatible cached alpha`() {
        val alpha = cachedAlpha()
        val selected = platformEntry(listOf(alpha)).newestCompatibleVersionWithFallback("ALPHA", listOf(ServerType.PAPER), "1.21.4") {
            error("An exact cached channel match should not need a full lookup")
        }
        assertEquals(alpha, selected)
    }

    @Test
    fun `unordered versions and duplicate entries do not select oldest release`() {
        val newest = version("2.0", "2026-06-02T00:00:00Z", ServerType.PAPER)
        val old = version("1.0", "2026-06-01T00:00:00Z", ServerType.PAPER)
        assertEquals(newest, listOf(old, newest, old).newestCompatibleVersion(null, listOf(ServerType.PAPER)))
    }

    @Test
    fun `structured patch version does not imply compatibility with base version`() {
        val versions = listOf(version("1.0", "2026-06-01T00:00:00Z", ServerType.PAPER, minecraftVersions = listOf("1.21.4")))
        assertNull(versions.newestCompatibleVersion(null, listOf(ServerType.PAPER), "1.21"))
    }

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
    fun `defaults to a stable channel instead of a newer prerelease`() {
        val versions = listOf(
            version("2.0.0-alpha", "2026-06-02T00:00:00Z", ServerType.PAPER, channel = "alpha"),
            version("1.9.0", "2026-06-01T00:00:00Z", ServerType.PAPER),
        )

        val selected = versions.newestCompatibleVersion(null, listOf(ServerType.PAPER))

        assertEquals("1.9.0", selected?.versionNumber)
    }

    @Test
    fun `honors an explicitly requested prerelease channel`() {
        val versions = listOf(
            version("2.0.0-beta", "2026-06-02T00:00:00Z", ServerType.PAPER, channel = "beta"),
            version("1.9.0", "2026-06-01T00:00:00Z", ServerType.PAPER),
        )

        val selected = versions.newestCompatibleVersion("beta", listOf(ServerType.PAPER))

        assertEquals("2.0.0-beta", selected?.versionNumber)
    }

    @Test
    fun `does not silently cross to alpha when stable versions are incompatible`() {
        val versions = listOf(
            version("2.0.0-alpha", "2026-06-02T00:00:00Z", ServerType.PAPER, channel = "alpha", minecraftVersions = listOf("1.21.4")),
            version("1.9.0", "2026-06-01T00:00:00Z", ServerType.PAPER, minecraftVersions = listOf("1.20.6")),
        )

        val selected = versions.newestCompatibleVersion(null, listOf(ServerType.PAPER), "1.21.4")

        assertNull(selected)
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
    fun `does not install a version that explicitly targets another minecraft version`() {
        val versions = listOf(
            version("2.0.0", "2026-06-02T00:00:00Z", ServerType.PAPER, minecraftVersions = listOf("26.1")),
            version("1.9.0", "2026-06-01T00:00:00Z", ServerType.PAPER, minecraftVersions = listOf("1.20.6")),
        )

        val selected = versions.newestCompatibleVersion("release", listOf(ServerType.PAPER), "1.21.4")

        assertNull(selected)
    }

    @Test
    fun `finds Advanced Portals Paper build beyond the cached first page`() {
        val cachedVersions = listOf(
            version("2.8.0-folia", "2026-05-27T21:30:47Z", ServerType.FOLIA, channel = "alpha", minecraftVersions = listOf("26.1.1")),
            version("2.8.0-legacy-spigot", "2026-05-27T21:30:43Z", ServerType.BUKKIT, ServerType.SPIGOT, ServerType.PAPER, minecraftVersions = listOf("1.8.8", "1.12.2")),
        )
        val platform = platformEntry(cachedVersions)

        val selected = platform.newestCompatibleVersionWithFallback(
            null,
            listOf(ServerType.PAPER, ServerType.SPIGOT, ServerType.BUKKIT),
            "1.21.4",
        ) {
            cachedVersions + listOf(
                version("2.8.0-spigot", "2026-05-27T21:30:37Z", ServerType.BUKKIT, ServerType.SPIGOT, ServerType.PAPER, minecraftVersions = listOf("1.21", "1.21.4")),
                version("0.0.41", "2023-04-20T02:20:14Z", ServerType.PAPER, minecraftVersions = listOf("1.8.8", "1.12.2")),
            )
        }

        assertEquals("2.8.0-spigot", selected?.versionNumber)
    }

    @Test
    fun `structured minor minecraft version does not imply later patch compatibility`() {
        val versions = listOf(
            version("1.0.0", "2026-06-01T00:00:00Z", ServerType.PAPER, minecraftVersions = listOf("1.21")),
        )

        val selected = versions.newestCompatibleVersion("release", listOf(ServerType.PAPER), "1.21.4")

        assertNull(selected)
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
