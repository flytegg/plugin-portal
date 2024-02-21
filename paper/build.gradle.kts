import xyz.jpenilla.runpaper.task.RunServer

plugins {
    alias(libs.plugins.run.paper)
}

group = "gg.flyte.pluginportal"
version = "2.0.0-SNAPSHOT"

dependencies {
    implementation(projects.pluginportalCommon)

    implementation(libs.paper) {
        exclude("junit", "junit")
        exclude("com.google.code.gson", "gson")
    }

    implementation(libs.lamp.common)
    implementation(libs.lamp.bukkit)

    implementation(libs.gson)
    implementation(libs.bstats)
    implementation(libs.paperlib)
    implementation(libs.twilight)

    implementation(libs.adventure.text.minimessage)
    implementation(libs.adventure.platform.bukkit)
    implementation(libs.triumph.guis)

    implementation(libs.kotlinx.coroutines.core)
    implementation("com.github.shynixn.mccoroutine:mccoroutine-folia-api:2.14.0")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-folia-core:2.14.0")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks {
    test {
        useJUnitPlatform()
    }

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
}