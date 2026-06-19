plugins {
    id("pp.kotlin-common-conventions")
    id("pp.shadow-convention")
    id("xyz.jpenilla.run-paper")
}

repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

val libs = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")

dependencies {
    implementation(project(":common"))

    compileOnly(libs.findLibrary("spigot-api").get())
}

tasks {
    runServer {
        minecraftVersion("1.21.11")
        runDirectory(file((project.findProperty("runDir") as? String) ?: "run/latest"))
        javaLauncher.set(
            project.javaToolchains.launcherFor {
                languageVersion.set(JavaLanguageVersion.of(21))
            }
        )
    }

    processResources {
        val channel: String? = project.findProperty("channel") as? String
        
        val version = pluginVersion(project.version as String)
        
        // Add channel suffix if specified
        val fullVersion = if (channel != null && channel != "stable") {
            "$version-$channel"
        } else {
            version
        }

        filesMatching(listOf("plugin.yml")) {
            expand("version" to fullVersion)
            println("Set plugin.yml version to $fullVersion")
        }

        outputs.upToDateWhen { false } // It caches the version number in the bulit resource, stopping the dynamic version replacement from working
    }
}
