import java.util.jar.JarFile

plugins {
    id("pp.plugin-conventions")
    id("io.papermc.hangar-publish-plugin")
    id("com.modrinth.minotaur")
}

dependencies {
    implementation(project(":platforms:bukkit"))
    implementation(project(":platforms:velocity"))
}

val supportedMinecraftVersions = (property("mcVersions") as String)
    .split(",")
    .map { it.trim() }
    .filter { it.isNotEmpty() }

val marketplaceChangelog = (findProperty("marketplaceChangelog") as? String) ?: "Release ${project.version}"
val prereleaseChannel = when {
    project.version.toString().contains("-beta.") -> "beta"
    project.version.toString().contains("-alpha.") -> "alpha"
    else -> "release"
}
val modrinthVersionType = (findProperty("modrinthVersionType") as? String) ?: prereleaseChannel
val hangarReleaseChannel = (findProperty("hangarChannel") as? String)
    ?: if (prereleaseChannel == "release") "Release" else "Snapshot"
check(prereleaseChannel == "release" || (modrinthVersionType != "release" && !hangarReleaseChannel.equals("Release", ignoreCase = true))) {
    "Prerelease versions cannot be published to a stable channel"
}

modrinth {
    token.set(System.getenv("MODRINTH_TOKEN"))
    projectId.set((findProperty("modrinthProjectId") as? String) ?: "pluginportal")
    versionName.set("PluginPortal ${project.version}")
    versionNumber.set(project.version as String)
    versionType.set(modrinthVersionType)
    uploadFile.set(tasks.shadowJar)
    gameVersions.addAll(supportedMinecraftVersions)
    loaders.addAll("bukkit", "folia", "paper", "purpur", "spigot", "velocity")
    changelog.set(marketplaceChangelog)
    debugMode.set((findProperty("modrinthDebugMode") as? String)?.toBoolean() ?: false)
}

hangarPublish {
    publications.register("plugin") {
        version = project.version as String
        id = (findProperty("hangarProjectId") as? String) ?: "PluginPortal"
        channel = hangarReleaseChannel
        changelog = marketplaceChangelog
        apiKey = System.getenv("HANGAR_API_TOKEN")

        platforms {
            velocity {
                jar = tasks.shadowJar.flatMap { it.archiveFile }
                platformVersions = listOf("3.4")
            }
            paper {
                jar = tasks.shadowJar.flatMap { it.archiveFile }
                platformVersions = supportedMinecraftVersions
            }
        }
    }
}

val verifyUniversalJar by tasks.registering {
    group = "verification"
    dependsOn(tasks.shadowJar)
    val artifact = tasks.shadowJar.flatMap { it.archiveFile }
    inputs.file(artifact)
    doLast {
        JarFile(artifact.get().asFile).use { jar ->
            listOf(
                "plugin.yml", "velocity-plugin.json",
                "gg/flyte/pluginportal/plugin/PluginPortal.class",
                "gg/flyte/pluginportal/velocity/VelocityPluginPortal.class",
            ).forEach { path -> check(jar.getJarEntry(path) != null) { "Missing universal JAR entry: $path" } }
            val bukkit = jar.getInputStream(jar.getJarEntry("plugin.yml")).bufferedReader().use { it.readText() }
            val velocity = jar.getInputStream(jar.getJarEntry("velocity-plugin.json")).bufferedReader().use { it.readText() }
            check(Regex("(?m)^version: ['\"]?${Regex.escape(project.version.toString())}['\"]?\\s*$").containsMatchIn(bukkit)) { "Bukkit descriptor version mismatch" }
            check(Regex("\"version\"\\s*:\\s*\"${Regex.escape(project.version.toString())}\"").containsMatchIn(velocity)) { "Velocity descriptor version mismatch" }
            check(artifact.get().asFile.name == "PluginPortal-${project.version}.jar") { "Universal filename version mismatch" }
            listOf("mclicense.txt", "pluginportal.txt", "network-node.json", "network-operations.json").forEach { path ->
                check(jar.getJarEntry(path) == null) { "Runtime identity or buyer key present in universal JAR: $path" }
            }
        }
    }
}
tasks.check { dependsOn(verifyUniversalJar) }
