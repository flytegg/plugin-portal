import xyz.jpenilla.runpaper.task.RunServer

/*
 * This file was generated by the Gradle("init") task.
 */

plugins {
    id("pp.kotlin-library-conventions")
    id("pp.shadow-convention")

    id("xyz.jpenilla.run-paper") version "2.3.0"
}

repositories {
    maven("https://oss.sonatype.org/content/repositories/central")
    maven("https://jitpack.io")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation(project(":common"))

    compileOnly("org.spigotmc:spigot-api:1.20.6-R0.1-SNAPSHOT")

    implementation("net.kyori:adventure-api:4.17.0")
    implementation("net.kyori:adventure-text-minimessage:4.17.0")
    implementation("net.kyori:adventure-platform-bukkit:4.3.3")

    implementation("com.github.Revxrsal.Lamp:common:3.2.1")
    implementation("com.github.Revxrsal.Lamp:bukkit:3.2.1")
    implementation("com.github.Revxrsal.Lamp:brigadier:3.2.1")

    implementation("com.google.guava:guava:33.2.1-jre")
    implementation("dev.masecla:Modrinth4J:2.0.0")

    api("io.papermc:paperlib:1.0.7")
}

tasks {
    runServer {
        minecraftVersion("1.20.4")
        runDirectory(file("run/latest"))
        javaLauncher.set(
            project.javaToolchains.launcherFor {
                languageVersion.set(JavaLanguageVersion.of(17))
            }
        )
    }

    shadowJar {
        minimize()
        exclude("com/google/common/")
    }


    mapOf(
        8 to setOf("1.8.8"),
        11 to setOf("1.9.4", "1.10.2", "1.11.2"),
        17 to setOf("1.12.2", "1.13.2", "1.14.4", "1.15.2", "1.16.5", "1.17.1", "1.18.2", "1.19.4", "1.20.4")
    ).forEach { (javaVersion, minecraftVersions) ->
        for (version in minecraftVersions) {
            createVersionedRun(version, javaVersion)
        }
    }
}

fun TaskContainerScope.createVersionedRun(
    version: String,
    javaVersion: Int
) = register<RunServer>("runServer-${version.replace(".", "_")}") {
    group = "minecraft"
    pluginJars.from(shadowJar.flatMap { it.archiveFile })
    minecraftVersion(version)
    runDirectory(file("run/$version"))
    systemProperty("Paper.IgnoreJavaVersion", true)
    javaLauncher.set(
        project.javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(javaVersion))
        }
    )
}