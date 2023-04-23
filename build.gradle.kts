plugins {
    application
    kotlin("jvm") version "1.7.21"
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "link.portalbox"
version = "1.2.4"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://jitpack.io")
}

dependencies {
    implementation("org.spigotmc:spigot-api:1.16.4-R0.1-SNAPSHOT")
    implementation ("org.bstats:bstats-bukkit:3.0.0")
    implementation("com.github.portal-box:pp-lib:1.2.7")
    compileOnly("commons-io:commons-io:2.11.0")
    compileOnly("com.google.code.gson:gson:2.10.1")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    application {
        mainClassName = "MainKt"
    }

    shadowJar {
        relocate("org.bstats", "link.portalbox.bstats")
        relocate("kotlin", "link.portalbox.kotlin")
        relocate("org.jetbrains.annotations", "link.portalbox.jetbrains.annotations")
        relocate("org.intellij.lang.annotations", "link.portalbox.intellij.lang.annotations")
    }
}