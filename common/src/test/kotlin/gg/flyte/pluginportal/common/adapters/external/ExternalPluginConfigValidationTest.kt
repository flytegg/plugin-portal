package gg.flyte.pluginportal.common.adapters.external

import gg.flyte.pluginportal.common.managers.duplicateExternalPluginIds
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExternalPluginConfigValidationTest {
    @Test
    fun `detects duplicate plugin ids without confusing nested keys`() {
        val yaml = """
            plugins:
              viaversion:
                source: github:ViaVersion/ViaVersion
                file: ViaVersion.jar
              floodgate:
                source: geysermc:floodgate
                file: floodgate.jar
              viaversion:
                source: github:ViaVersion/ViaVersion
                file: ViaVersion-new.jar
        """.trimIndent()

        assertEquals(setOf("viaversion"), duplicateExternalPluginIds(yaml))
    }

    @Test
    fun `accepts unique plugin ids`() {
        val yaml = """
            plugins:
              first:
                file: plugin.jar
              second:
                file: plugin.jar
        """.trimIndent()

        assertTrue(duplicateExternalPluginIds(yaml).isEmpty())
    }

    @Test
    fun `detects quoted duplicate ids`() {
        val yaml = """
            plugins:
              "viaversion":
                file: first.jar
              viaversion:
                file: second.jar
        """.trimIndent()

        assertEquals(setOf("viaversion"), duplicateExternalPluginIds(yaml))
    }
}
