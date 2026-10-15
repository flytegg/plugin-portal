package gg.flyte.pluginportal.common.util

import gg.flyte.pluginportal.common.types.Version
import gg.flyte.pluginportal.common.types.enums.ServerType
import java.util.Date
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class VersionSupportLabelTest {
    @Test
    fun `ViaVersion card shows numeric endpoints across Minecraft numbering schemes`() {
        val versions = listOf(
            "1.8.9", "1.9.4", "1.10.2", "1.11.2", "1.12.2", "1.13.2", "1.14.4", "1.15.2",
            "1.16.5", "1.17.1", "1.18.2", "1.19", "1.19.1", "1.19.2", "1.19.3", "1.19.4",
            "1.20", "1.20.1", "1.20.2", "1.20.3", "1.20.4", "1.20.5", "1.20.6", "1.21",
            "1.21.1", "1.21.2", "1.21.3", "1.21.4", "1.21.5", "1.21.6", "1.21.7", "1.21.8",
            "1.21.9", "1.21.10", "1.21.11", "26.1.1", "26.2", "26.1", "26.1.2", "26.3",
        )
        val version = version(versions.reversed())

        assertEquals("1.8.9 - 26.3", version.supportLabel)
        assertEquals(versions.reversed().joinToString(", "), version.supportDetails)
        assertFalse(version.explicitlySupportsMinecraftVersion("1.9.3"))
    }

    @Test
    fun `patch versions sort numerically and a single version has no range`() {
        assertEquals("1.21.2 - 1.21.11", version(listOf("1.21.9", "1.21.11", "1.21.2")).supportLabel)
        assertEquals("1.21.4", version(listOf("1.21.4", "1.21.4")).supportLabel)
    }

    @Test
    fun `legacy range retains numeric endpoints`() {
        assertEquals("1.8 - 1.21.11", version(supportedVersions = "1.8 - 1.21.11").supportLabel)
    }

    @Test
    fun `proxy and unknown entries retain their fallback labels`() {
        assertEquals("velocity", version(serverTypes = arrayOf(ServerType.VELOCITY)).supportLabel)
        assertEquals("Unknown", version().supportLabel)
    }

    private fun version(
        mcVersions: List<String>? = null,
        supportedVersions: String? = null,
        serverTypes: Array<ServerType> = emptyArray(),
    ) = Version("1.0", Date(0), "release", null, supportedVersions, mcVersions, serverTypes, null)
}
