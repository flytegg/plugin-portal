plugins {
    id("pp.kotlin-library-conventions")
    id("pp.shadow-convention")

    id("xyz.jpenilla.run-paper") version "2.3.0"
    id("com.modrinth.minotaur") version "2.+"
    id("io.papermc.hangar-publish-plugin") version "0.1.2"
}

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/central")
    maven("https://repo.flyte.gg/releases")
    maven("https://jitpack.io")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation(project(":common"))

    compileOnly("org.spigotmc:spigot-api:1.20.6-R0.1-SNAPSHOT")

    implementation("net.kyori:adventure-api:4.17.0")
    implementation("net.kyori:adventure-text-minimessage:4.17.0")
    implementation("net.kyori:adventure-platform-bukkit:4.3.3")

    implementation("io.github.revxrsal:lamp.common:4.0.0-beta.20")
    implementation("io.github.revxrsal:lamp.bukkit:4.0.0-beta.20")
    implementation("io.github.revxrsal:lamp.brigadier:4.0.0-beta.20")

    implementation("dev.masecla:Modrinth4J:2.0.0")

    api("io.papermc:paperlib:1.0.7")

    implementation("gs.mclo:api:4.0.3")
    implementation("org.bstats:bstats-bukkit:3.0.2")

    implementation("com.github.HangarMC:HangarJarScanner:906710dc36")
}

tasks {
    runServer {
        minecraftVersion("1.20.6")
        runDirectory(file("run/latest"))
        javaLauncher.set(
            project.javaToolchains.launcherFor {
                languageVersion.set(JavaLanguageVersion.of(21))
            }
        )
    }

}

// Source: https://github.com/ViaVersion/ViaVersion/blob/dc503cd613f5cf00a6f11b78e52b1a76a42acf91/universal/build.gradle.kts
val branch = rootProject.branchName()
val baseVersion = project.version as String
val isRelease = !baseVersion.contains('-')
//val isMainBranch = branch == "master"
//if (!isRelease || isMainBranch) { // Only publish releases from the main branch
val suffixedVersion = if (isRelease) baseVersion else baseVersion + "+" + System.getenv("GITHUB_RUN_NUMBER")
val changelogContent = if (isRelease) {
    "See [GitHub](https://github.com/flytegg/plugin-portal) for release notes."
} else {
    val commitHash = rootProject.latestCommitHash()
    "[$commitHash](https://github.com/flytegg/plugin-portal/commit/$commitHash) ${rootProject.latestCommitMessage()}"
}

modrinth {
    val mcVersions: List<String> = (property("mcVersions") as String)
        .split(",")
        .map { it.trim() }
    token.set(System.getenv("MODRINTH_TOKEN"))
    projectId.set("pluginportal")
    versionType.set(if (isRelease) "release" else "beta")
    versionNumber.set(suffixedVersion)
    versionName.set(suffixedVersion)
    changelog.set(changelogContent)
    uploadFile.set(tasks.shadowJar.flatMap { it.archiveFile })
    gameVersions.set(mcVersions)
    loaders.add("bukkit")
    loaders.add("spigot")
    loaders.add("paper")
    loaders.add("folia")
    loaders.add("purpur")
    autoAddDependsOn.set(false)
    detectLoaders.set(false)
}

hangarPublish {
    publications.register("plugin") {
        version.set(suffixedVersion)
        id.set("PluginPortal")
        channel.set(if (isRelease) "Release" else "Snapshot")
        changelog.set(changelogContent)
        apiKey.set(System.getenv("HANGAR_API_KEY"))
        platforms {
            paper {
                jar.set(tasks.shadowJar.flatMap { it.archiveFile })
                platformVersions.set(listOf(property("mcVersionRange") as String))
            }
        }
    }
}