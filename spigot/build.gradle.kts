import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "1.5.5"
    id("xyz.jpenilla.run-paper") version "2.1.0"
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.flyte.gg/releases")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/repositories/central")

}

dependencies {
    implementation(project(":common"))

    paperweight.paperDevBundle("1.20.1-R0.1-SNAPSHOT")
    implementation("io.papermc:paperlib:1.0.7")
    implementation("net.kyori:adventure-platform-bukkit:4.3.0")
    implementation("com.github.SuperGlueLib:SuperFoundations:1.2.0")

    implementation("org.bstats:bstats-bukkit:3.0.2")
    implementation("com.github.Revxrsal.Lamp:bukkit:3.1.5")
    implementation("com.github.Revxrsal.Lamp:brigadier:3.1.5")
}

tasks {
    assemble {
        dependsOn(reobfJar)
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything

//        options.release.set(17)
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
    }

    processResources {
//        filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything
        val props = mapOf(
            "name" to "PluginPortal",
            "version" to project.version,
            "main" to "gg.flyte.pluginPortal.PluginPortal",
            "description" to "A Minecraft Modification Package Manager.",
            "author" to "Flyte",
            "website" to "https://flyte.gg",
            "apiVersion" to "1.13"

        )
        inputs.properties(props)
        filesMatching("plugin.yml") {
            expand(props)
        }

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    named<ShadowJar>("shadowJar") {
        relocate("org.bstats", "gg.flyte.lib.bstats")
    }

    withType<Jar>() {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        manifest {
            attributes["Main-Class"] = "gg.flyte.pluginPortal.PluginPortal"
        }
    }

    runServer {
        minecraftVersion("1.20.1")
    }
}
//
//java {
//    // Configure the java toolchain. This allows gradle to auto-provision JDK 17 on systems that only have JDK 8 installed for example.
//    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
//}