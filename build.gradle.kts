import io.papermc.hangarpublishplugin.model.Platforms
import java.io.ByteArrayOutputStream

plugins {
    kotlin("jvm") version "1.7.21"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("io.papermc.hangar-publish-plugin") version "0.0.5"
    id("xyz.jpenilla.run-paper") version "2.0.1"
}

group = "link.portalbox"
version = "1.3.3"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://jitpack.io")
}

dependencies {
    implementation("org.spigotmc:spigot-api:1.16.4-R0.1-SNAPSHOT") {
        // Use in-house version of gson
        exclude(group = "com.google.code.gson", module = "gson")
    }

    implementation ("org.bstats:bstats-bukkit:3.0.2")
    implementation("com.github.portal-box:pp-lib:1.3.6")
    compileOnly("com.google.code.gson:gson:2.10.1")
}

// SET THIS UP SOON
hangarPublish {
    val owner = "stephen"
    val slug = "plugin-portal"
    val versions: List<String> = listOf("1.8-1.19.4")

    // To be run every github release
    publications.register("Release") {
        if(project.properties["hangar-publish-plugin.use-dev-endpoint"] as String == "true") {
            apiEndpoint.set("https://hangar.papermc.dev/api/v1/")
        }

        namespace(owner, slug)
        version.set(project.version as String)

        provider {
            channel.set(if(version.get().endsWith("-SNAPSHOT")) "prerelease" else "release")
            val commitLog = getCommitHistory(project.properties["release-start-commit"] as String)

            changelog.set("# Release ${project.version}\n ${commitLog.joinToString(separator = "") { formatCommitLog(it) }} \n")
        }

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
        channel.set("nightly")

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
        relocate("com.google.gson", "link.portalbox.gson")
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