package gg.flyte.pluginportal.plugin

import gg.flyte.pluginportal.common.Config
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PluginPortalTest {
    private lateinit var server: ServerMock
    private lateinit var plugin: PluginPortal

    @BeforeAll
    fun setUp() {
        System.setProperty("bstats.relocatecheck", "false")
        System.setProperty("pluginportal.dev", "true")
        server = MockBukkit.mock()
        plugin = MockBukkit.load(PluginPortal::class.java)
    }

    @AfterAll
    fun tearDown() {
        MockBukkit.unmock()
        System.clearProperty("bstats.relocatecheck")
        System.clearProperty("pluginportal.dev")
    }

    @Test
    fun `plugin enables`() {
        assertTrue(plugin.isEnabled)
    }

    @Test
    fun `help command runs from console`() {
        assertTrue(server.dispatchCommand(server.consoleSender, "pp"))
        val message = PlainTextComponentSerializer.plainText().serialize(requireNotNull(server.consoleSender.nextComponentMessage()))
        assertTrue(message.contains("Plugin Portal"), message)
        assertTrue(message.contains("/pp install"), message)
    }

    @Test
    fun `command aliases are registered`() {
        assertTrue(server.dispatchCommand(server.consoleSender, "pluginportal"))
        val longAliasMessage = PlainTextComponentSerializer.plainText()
            .serialize(requireNotNull(server.consoleSender.nextComponentMessage()))
        assertTrue(longAliasMessage.contains("/pp install"), longAliasMessage)

        assertTrue(server.dispatchCommand(server.consoleSender, "ppm"))
        val shortAliasMessage = PlainTextComponentSerializer.plainText()
            .serialize(requireNotNull(server.consoleSender.nextComponentMessage()))
        assertTrue(shortAliasMessage.contains("/pp install"), shortAliasMessage)
    }

    @Test
    fun `key clear command removes persisted authentication`() {
        Config.setApiKey("pp_test_key")

        assertTrue(server.dispatchCommand(server.consoleSender, "pp key clear"))
        assertNull(Config.getApiKey())

        val message = PlainTextComponentSerializer.plainText().serialize(requireNotNull(server.consoleSender.nextComponentMessage()))
        assertTrue(message.contains("API key cleared"), message)
    }
}
