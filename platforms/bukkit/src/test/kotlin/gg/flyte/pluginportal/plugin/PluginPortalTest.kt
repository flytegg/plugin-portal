package gg.flyte.pluginportal.plugin

import gg.flyte.pluginportal.common.PluginPortalBase
import gg.flyte.pluginportal.common.commands.lamp.CommandAliasAudience
import gg.flyte.pluginportal.common.commands.lamp.PortalCommandActor
import gg.flyte.pluginportal.common.chat.sendPagedRows
import gg.flyte.pluginportal.common.commands.lamp.CommandSenderAudience
import net.kyori.adventure.text.Component
import gg.flyte.pluginportal.common.Config
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
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

    @Test
    fun `existing on-disk key remains compatible across reloads`() {
        val licenseFile = plugin.dataFolder.resolve("mclicense.txt")
        val legacyFile = plugin.dataFolder.resolve("pluginportal.txt")
        Config.clearAuthenticationKey()
        licenseFile.delete()
        legacyFile.delete()
        licenseFile.writeText("existing-fixture-license")
        try {
            val manager = EntitlementManager(PortalApplication.runtime)
            assertEquals("existing-fixture-license", manager.loadConfiguredKey())
            assertEquals("existing-fixture-license", Config.getApiKey())
            assertFalse(manager.hasPremiumAccess())
            Config.clearAuthenticationKey()
            assertEquals("existing-fixture-license", EntitlementManager(PortalApplication.runtime).loadConfiguredKey())
        } finally {
            Config.clearAuthenticationKey()
            licenseFile.delete()
        }
    }

    @Test
    fun `configured key takes precedence over legacy disk key`() {
        val licenseFile = plugin.dataFolder.resolve("mclicense.txt")
        licenseFile.writeText("legacy-fixture")
        Config.setApiKey("existing-fixture")
        try {
            assertEquals("existing-fixture", EntitlementManager(PortalApplication.runtime).loadConfiguredKey())
        } finally {
            Config.clearAuthenticationKey()
            licenseFile.delete()
        }
    }

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
        assertTrue(longAliasMessage.contains("/pluginportal install"), longAliasMessage)

        assertTrue(server.dispatchCommand(server.consoleSender, "ppm"))
        val shortAliasMessage = PlainTextComponentSerializer.plainText()
            .serialize(requireNotNull(server.consoleSender.nextComponentMessage()))
        assertTrue(shortAliasMessage.contains("/ppm install"), shortAliasMessage)
    }

    @Test
    fun `command help works through every alias without intercepting commands`() {
        for (alias in listOf("pp", "pluginportal", "ppm")) {
            assertTrue(server.dispatchCommand(server.consoleSender, "$alias help update"))
            val message = PlainTextComponentSerializer.plainText()
                .serialize(requireNotNull(server.consoleSender.nextComponentMessage()))
            assertTrue(message.contains("--refresh"), message)
            assertTrue(message.contains("--ignoreOutdated"), message)
        }
    }

    @Test
    fun `short server commands provide status and require explicit refresh targets`() {
        val plain = PlainTextComponentSerializer.plainText()
        for (alias in listOf("pp", "pluginportal", "ppm")) {
            assertTrue(server.dispatchCommand(server.consoleSender, "$alias link"))
            val status = plain.serialize(requireNotNull(server.consoleSender.nextComponentMessage()))
            assertTrue(status.contains("Not linked"), status)
            assertTrue(status.contains("Add server"), status)
            assertTrue(status.contains("/$alias link <code>"), status)
            assertTrue(server.dispatchCommand(server.consoleSender, "$alias refresh"))
            val refresh = plain.serialize(requireNotNull(server.consoleSender.nextComponentMessage()))
            assertTrue(refresh.contains("/$alias refresh --servers"), refresh)
            assertTrue(server.dispatchCommand(server.consoleSender, "$alias refresh --server a --servers b"))
            val conflict = plain.serialize(requireNotNull(server.consoleSender.nextComponentMessage()))
            assertTrue(conflict.contains("Use --server or --servers, not both"), conflict)
        }
    }

    @Test
    fun `legacy network commands disappear from registration completion and help`() {
        assertNull(server.getPluginCommand("ppnetwork"))
        val actor = PortalCommandActor(net.kyori.adventure.audience.Audience.empty(), "CONSOLE", java.util.UUID(0, 0), true) { true }
        val completions = PluginPortalBase.lamp.autoCompleter().complete(actor, "pp ")
        assertFalse(completions.contains("network"), completions.toString())
        assertTrue(completions.containsAll(listOf("link", "refresh", "servers", "history")), completions.toString())
        assertTrue(server.dispatchCommand(server.consoleSender, "pp network status"))
        val message = PlainTextComponentSerializer.plainText().serialize(requireNotNull(server.consoleSender.nextComponentMessage()))
        assertTrue(message.contains("Unknown command"), message)
    }

    @Test
    fun `server help preserves command aliases in text clicks and hover hints`() {
        for (alias in listOf("pp", "ppm", "pluginportal", "ppv", "pluginportalvelocity")) {
            val messages = mutableListOf<Component>()
            val audience = object : net.kyori.adventure.audience.Audience {
                override fun sendMessage(message: Component) { messages += message }
            }
            val actor = PortalCommandActor(CommandAliasAudience(audience, alias), "CONSOLE", java.util.UUID(0, 0), true) { true }
            PluginPortalBase.lamp.dispatch(actor, "pp help 3")
            val message = messages.single()
            val plain = PlainTextComponentSerializer.plainText()
            assertTrue(plain.serialize(message).contains("/$alias link [code]"))
            fun inspect(component: Component) {
                component.clickEvent()?.let {
                    if (it.action() == net.kyori.adventure.text.event.ClickEvent.Action.RUN_COMMAND ||
                        it.action() == net.kyori.adventure.text.event.ClickEvent.Action.SUGGEST_COMMAND) {
                        assertTrue(it.value().startsWith("/$alias "), it.value())
                    }
                }
                (component.hoverEvent()?.value() as? Component)?.let { hover ->
                    val text = plain.serialize(hover)
                    assertFalse(Regex("/pp(?: |$)").containsMatchIn(text) && alias != "pp", text)
                }
                component.children().forEach(::inspect)
            }
            inspect(message)
        }
    }

    @Test
    fun `chat pages keep filters and console shows the complete list`() {
        val player = server.addPlayer()
        val rows = (1..9).map { number -> "Installed" to { Component.text("Entry $number") } }
        val playerAudience = CommandSenderAudience(player, plugin.audiences)
        sendPagedRows(playerAudience, rows, null, false, "/pp list --outdated")
        val firstPage = requireNotNull(player.nextComponentMessage())
        val plain = PlainTextComponentSerializer.plainText()
        assertTrue(plain.serialize(firstPage).contains("Entry 8"))
        assertFalse(plain.serialize(firstPage).contains("Entry 9"))
        fun clicks(component: Component): List<String> =
            listOfNotNull(component.clickEvent()?.value()) + component.children().flatMap(::clicks)
        assertTrue(clicks(firstPage).contains("/pp list --outdated --page 2"))
        sendPagedRows(playerAudience, rows, 2, false, "/pp list --outdated")
        val secondPage = plain.serialize(requireNotNull(player.nextComponentMessage()))
        assertTrue(secondPage.contains("Entry 9"))
        assertFalse(secondPage.contains("Entry 8"))
        sendPagedRows(CommandSenderAudience(server.consoleSender, plugin.audiences), rows, null, false, "/pp list")
        val console = plain.serialize(requireNotNull(server.consoleSender.nextComponentMessage()))
        assertTrue(console.contains("Entry 1") && console.contains("Entry 9"))
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
