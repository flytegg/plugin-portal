package gg.flyte.pluginportal.common.runtime

import gg.flyte.pluginportal.common.types.enums.ServerType
import gg.flyte.pluginportal.common.util.requireCompatibleJar
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import kotlin.test.*

class ArtifactSafetyTest {
    @TempDir lateinit var root: File
    private fun jar(file: File, velocity: Boolean, version: String = "1", id: String = "fixture"): File {
        file.parentFile.mkdirs()
        JarOutputStream(file.outputStream()).use {
            it.putNextEntry(JarEntry(if (velocity) "velocity-plugin.json" else "plugin.yml"))
            it.write((if (velocity) """{"id":"$id","name":"Fixture","version":"$version","main":"test.Plugin"}""" else "name: Fixture\nversion: '$version'\nmain: test.Plugin\n").toByteArray())
            it.closeEntry()
        }
        return file
    }
    @Test fun `proxy rejects backend artifacts and backend rejects proxy artifacts`() {
        val backend = jar(File(root, "backend.jar"), false)
        val proxy = jar(File(root, "proxy.jar"), true)
        assertFailsWith<IllegalArgumentException> { backend.requireCompatibleJar(listOf(ServerType.VELOCITY)) }
        assertFailsWith<IllegalArgumentException> { proxy.requireCompatibleJar(listOf(ServerType.PAPER)) }
        backend.requireCompatibleJar(listOf(ServerType.PAPER))
        proxy.requireCompatibleJar(listOf(ServerType.VELOCITY))
    }
    @Test fun `graceful proxy stop replaces by identity with a recoverable backup`() {
        val installed = jar(File(root, "plugins/old-name.jar"), true)
        val old = installed.readBytes()
        val staged = jar(File(root, "staging/new-name.jar"), true, "2")
        val updated = staged.readBytes()
        assertEquals(emptyList(), StagedUpdates.apply(File(root, "staging"), File(root, "plugins"), File(root, "backup")))
        assertContentEquals(updated, installed.readBytes())
        assertContentEquals(old, File(root, "backup/old-name.jar.previous").readBytes())
        assertFalse(staged.exists())
        assertEquals(listOf("old-name.jar"), File(root, "plugins").list()!!.toList())
    }
    @Test fun `ambiguous identity and filename collision retain staged files`() {
        jar(File(root, "plugins/a.jar"), true)
        jar(File(root, "plugins/b.jar"), true)
        val staged = jar(File(root, "staging/update.jar"), true, "2")
        assertEquals(1, StagedUpdates.apply(File(root, "staging"), File(root, "plugins"), File(root, "backup")).size)
        assertTrue(staged.exists())
        File(root, "plugins/b.jar").delete()
        jar(File(root, "staging/a.jar"), true, "3", "other")
        assertEquals(1, StagedUpdates.apply(File(root, "staging"), File(root, "plugins"), File(root, "backup")).size)
        assertTrue(File(root, "staging/a.jar").exists())
    }
}
