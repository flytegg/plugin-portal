import xyz.jpenilla.runpaper.task.RunServer

plugins {
    alias(libs.plugins.run.paper)
}

dependencies {
    implementation(projects.pluginportalCommon)

    compileOnly(libs.paper) {
        exclude("junit", "junit")
        exclude("com.google.code.gson", "gson")
        exclude("javax.persistence", "persistence-api")
    }

    implementation(libs.gson)
    implementation(libs.bstats)
    implementation(libs.paperlib)
    implementation(libs.twilight)

    implementation(libs.lamp.common)
    implementation(libs.lamp.bukkit)

    implementation(libs.cloud.paper)
    implementation(libs.cloud.extras)
    implementation(libs.cloud.extensions)
    implementation(libs.cloud.coroutines)
    implementation(libs.cloud.annotations)


    implementation(libs.adventure.text.minimessage)
    implementation(libs.adventure.platform.bukkit)
    implementation(libs.triumph.guis)

    implementation(libs.kotlinx.coroutines.core)
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }

    runServer {
        minecraftVersion("1.20.4")
        runDirectory(file("run/latest"))
        javaLauncher.set(
            project.javaToolchains.launcherFor {
                languageVersion.set(JavaLanguageVersion.of(17))
            }
        )
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



