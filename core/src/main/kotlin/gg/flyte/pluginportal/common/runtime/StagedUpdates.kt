package gg.flyte.pluginportal.common.runtime

import gg.flyte.pluginportal.common.types.enums.ServerType
import gg.flyte.pluginportal.common.util.getPluginYML
import gg.flyte.pluginportal.common.util.requireCompatibleJar
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/** Velocity has no update folder. Publish staged replacements during graceful shutdown. */
object StagedUpdates {
    fun apply(staging: File, plugins: File, backups: File): List<String> {
        val failures = mutableListOf<String>()
        staging.listFiles { file -> file.isFile && file.extension == "jar" }.orEmpty().forEach { staged ->
            runCatching {
                require(!Files.isSymbolicLink(staged.toPath())) { "Symbolic-link update" }
                staged.requireCompatibleJar(listOf(ServerType.VELOCITY))
                val metadata = requireNotNull(staged.getPluginYML())
                val identity = metadata["id"] ?: metadata["name"]
                require(identity != null) { "Missing plugin identity" }
                val candidates = plugins.listFiles { file -> file.isFile && file.extension == "jar" }.orEmpty().filter {
                    val existing = it.getPluginYML()
                    existing != null && (existing["id"] ?: existing["name"]) == identity
                }
                require(candidates.size <= 1) { "Multiple installed JARs have this identity" }
                val target = candidates.singleOrNull() ?: File(plugins, staged.name)
                require(!Files.isSymbolicLink(target.toPath())) { "Symbolic-link destination" }
                if (target.exists()) {
                    val existing = requireNotNull(target.getPluginYML())
                    require((existing["id"] ?: existing["name"]) == identity) { "Another plugin uses the destination filename" }
                    backups.mkdirs()
                    Files.copy(target.toPath(), File(backups, target.name + ".previous").toPath(), StandardCopyOption.REPLACE_EXISTING)
                }
                Files.move(staged.toPath(), target.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
            }.onFailure { failures += "Retained ${staged.name} in pending-updates: ${it.message}" }
        }
        return failures
    }
}
