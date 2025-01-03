package gg.flyte.pluginportal.plugin

import gg.flyte.pluginportal.common.API
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
//import org.mockbukkit.mockbukkit.MockBukkit
//import org.mockbukkit.mockbukkit.ServerMock
import kotlin.test.assertNotNull

class PluginPortalTest {

//    private lateinit var server: ServerMock
//    private lateinit var plugin: PluginPortal

    @BeforeEach
    fun setUp() {
//        server = MockBukkit.mock()
//        plugin = MockBukkit.load(PluginPortal::class.java)
    }

    @AfterEach
    fun tearDown() {
//        MockBukkit.unmock()
    }

    @Test
    fun testPluginStartsOnLoad() {
//        assertTrue(plugin.isEnabled)
    }

    @Test
    fun testCanFetchPlugin() {
        val plugin = API.getPlugins("Via")
        assertNotNull(plugin)
        assertTrue(plugin.isNotEmpty())
    }

    @Test
    fun testCanFetchPluginPortal() {
        val plugin = API.getPlugins("PluginPortal")
        assertNotNull(plugin.find { it.id == "667c45a7e4fff17899284030" })
    }
}