import io.papermc.hangarpublishplugin.model.Platforms
import java.io.ByteArrayOutputStream

plugins {
    kotlin("jvm") version "1.7.21"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("io.papermc.hangar-publish-plugin") version "0.0.5"
    id("xyz.jpenilla.run-paper") version "2.0.1"
}

group = "link.portalbox"
version = "1.4.4"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://jitpack.io")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.19.3-R0.1-SNAPSHOT")
    compileOnly("net.kyori:adventure-platform-bukkit:4.1.0") {
        exclude("adventure-bom")
        exclude("adventure-api")
        exclude("adventure-nbt")
    }

    implementation ("org.bstats:bstats-bukkit:3.0.2")
    implementation("com.github.portal-box:pp-lib:1.3.6")
}

hangarPublish {
    val owner = "nucker"
    val slug = "plugin-portal-test"
    val versions: List<String> = listOf("1.8-1.19.4")

    // To be run every github release
    publications.register("Release") {
        if(project.properties["hangar-publish-plugin.use-dev-endpoint"] as String == "true") {
            apiEndpoint.set("https://hangar.papermc.dev/api/v1/")
        }

        namespace(owner, slug)
        version.set(project.version as String)
        channel.set("release") // placeholder, see code below

        channel.set(version.map { if(it.endsWith("-SNAPSHOT")) "Pre-Release" else "Release" })
        changelog.set(provider {
            val commitLog = getCommitHistory(project.properties["release-start-commit"] as String)

            "# Release ${project.version}\n ${commitLog.joinToString(separator = "") { formatCommitLog(it) }} \n"
        })

        platforms {
            register(Platforms.PAPER) {
                jar.set(tasks.jar.flatMap { it.archiveFile })
                platformVersions.set(versions)
            }
        }
    }

    // To be run every commit
    publications.register("Nightly") {
        if(project.properties["hangar-publish-plugin.use-dev-endpoint"] as String == "true") {
            apiEndpoint.set("https://hangar.papermc.dev/api/v1/")
        }
        namespace(owner, slug)

        val commitLog = getLatestCommit()
        version.set(getCommitHashFromLog(commitLog))
        channel.set("Nightly")

        changelog.set("# Nightly Release " +
                "[${version.get()}](https://github.com/Nuckerr/plugin-portal/commit/${version.get()})" +
                "\n ${getCommitMessageFromLog(commitLog)}  " +
                "\n*Remember this build is unstable (is the bleeding edge)*")

        platforms {
            register(Platforms.PAPER) {
                jar.set(tasks.jar.flatMap { it.archiveFile })
                platformVersions.set(versions)
            }
        }
    }
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    shadowJar {
        relocate("org.bstats", "link.portalbox.bstats")
        relocate("kotlin", "link.portalbox.kotlin")
        relocate("org.jetbrains.annotations", "link.portalbox.jetbrains.annotations")
        relocate("org.intellij.lang.annotations", "link.portalbox.intellij.lang.annotations")
    }

    runServer {
        minecraftVersion("1.19.4")
    }
}

fun getCommitHistory(startHash: String, endHash: String = "HEAD"): List<String> {
    val output: String = ByteArrayOutputStream().use { outputStream ->
        project.exec {
            executable("git")
            args("log",  "$startHash..$endHash", "--format=format:%h %s")
            standardOutput = outputStream
        }
        outputStream.toString()
    }
    return output.split("\n")
}

fun getLatestCommit(): String {
    val output: String = ByteArrayOutputStream().use { outputStream ->
        project.exec {
            executable("git")
            args("log",  "-n", "1", "--format=format:%h %s")
            standardOutput = outputStream
        }
        outputStream.toString()
    }
    return output
}

// Assuming log is in the format: 2059265 Commit message here
//println("fixing $commitLog")
fun getCommitHashFromLog(commitLog: String) = commitLog.take(7)

fun getCommitMessageFromLog(commitLog: String) = commitLog.substring(8) // Get message after commit hash + space between

fun formatCommitLog(commitLog: String): String {
    val hash = getCommitHashFromLog(commitLog)
    val message = getCommitMessageFromLog(commitLog)
    return "* [$hash](https://github.com/Nuckerr/plugin-portal/commit/$hash) $message\n"
}
