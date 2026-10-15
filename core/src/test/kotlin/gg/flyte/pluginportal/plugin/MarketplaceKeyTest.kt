package gg.flyte.pluginportal.plugin

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class MarketplaceKeyTest {
    @Test
    fun `unrewritten delivery never supplies a key`() {
        assertNull(MarketplaceKey.embedded())
        assertNull(MarketplaceKey.resolve("  %%__LICENSE__%%  ", ""))
        assertNull(MarketplaceKey.resolve(" ", " "))
    }

    @Test
    fun `license only delivery uses the MCLicense Polymart prefix`() {
        assertEquals("pm_test-license", MarketplaceKey.resolve(" test-license ", "%%__BBB_LICENSE__%%"))
    }

    @Test
    fun `BBB delivery retains its key and takes precedence`() {
        assertEquals("bbb-test", MarketplaceKey.resolve("test-license", " bbb-test "))
        assertEquals("bbb-test", MarketplaceKey.resolve("%%__LICENSE__%%", "bbb-test"))
    }

    @Test
    fun `rewriting compiled license strings works without a marker branch`() {
        // Equal-length fake replacements simulate the marketplace's class constant rewrite.
        // Loading the rewritten class catches compiler-folded branches that source-only tests miss.
        val type = MarketplaceKey::class.java
        val original = type.getResourceAsStream("MarketplaceKey.class")!!.use { it.readBytes() }
        for (token in listOf("%%__LICENSE__%%", "%%__BBB_LICENSE__%%")) {
            val replacement = "x".repeat(token.length)
            val bytes = original.copyOf()
            val needle = token.toByteArray()
            var replaced = 0
            for (index in 0..bytes.size - needle.size) {
                if (needle.indices.all { bytes[index + it] == needle[it] }) {
                    replacement.toByteArray().copyInto(bytes, index)
                    replaced++
                }
            }
            check(replaced > 0) { "Marketplace placeholder missing from compiled class" }
            val loaded = object : ClassLoader(type.classLoader) {
                fun rewritten() = defineClass(type.name, bytes, 0, bytes.size)
            }.rewritten()
            val instance = loaded.getField("INSTANCE").get(null)
            val expected = if (token.contains("BBB")) replacement else "pm_$replacement"
            assertEquals(expected, loaded.getMethod("embedded").invoke(instance))
        }
    }
}
