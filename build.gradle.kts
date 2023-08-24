import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.9.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "gg.flyte"
version = "2.0.0"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(project(":common", "shadow"))
    implementation(project(":spigot", "shadow"))
    implementation(project(":bungeecord", "shadow"))
    implementation(project(":velocity", "shadow"))
    implementation(project(":cli", "shadow"))
}

tasks {
    withType<ProcessResources> {
        expand("version" to project.version)
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "idea")
    apply(plugin = "kotlin")
    apply(plugin = "com.github.johnrengelman.shadow")

    group = project.group
    version = project.version

    repositories {
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://repo.flyte.gg/releases")

    }

    dependencies {
        implementation(kotlin("stdlib-jdk8"))

        compileOnly("net.kyori:adventure-api:4.14.0")
        compileOnly("net.kyori:adventure-text-minimessage:4.14.0")
        implementation("com.github.Revxrsal.Lamp:common:3.1.5")
        implementation("com.google.code.gson:gson:2.10.1")
    }

    sourceSets {
        main {
            java.srcDir("src/main/kotlin")
            resources.srcDir("src/main/resources")
        }
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    tasks {
        withType<KotlinCompile> {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    relocate("net.kyori.adventure", "gg.flyte.lib.adventure")
    relocate("kotlin", "gg.flyte.lib.kotlin")
    relocate("com.google.gson", "gg.flyte.lib.gson")
    relocate("com.github.Revxrsal.Lamp", "gg.flyte.lib.lamp")

}

task<Copy>("copyJars") {
    outputTasks().forEach { from(it) }
    rename("(.*)-all.jar", "PluginPortal-$1-$version.jar")
    into("jars")
}

fun outputTasks(): List<Task?> {
    return arrayOf(":common:shadowJar", ":spigot:shadowJar", ":bungeecord:shadowJar", ":velocity:shadowJar", ":cli:shadowJar").map {
        tasks.findByPath(it)
    }
}

task("cleanJars") {
    delete("jars")
}

tasks.named("clean") {
    dependsOn("cleanJars")
}

tasks.named("build") {
    dependsOn("shadowJar")
    dependsOn("copyJars")
}