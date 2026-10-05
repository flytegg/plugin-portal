plugins {
    id("pp.kotlin-common-conventions")
    id("com.gradleup.shadow")
}

tasks {
    shadowJar {
        val channel: String? = project.findProperty("channel") as? String

        val version = pluginVersion(project.version as String)
        val name = getJarName()
        
        // Add channel suffix if specified
        val fullVersion = if (channel != null && channel != "stable") {
            "$version-$channel"
        } else {
            version
        }

        archiveClassifier.set("")
        archiveFileName.set("$name-${fullVersion}.jar")
        destinationDirectory.set(file("$rootDir/out"))

        mergeServiceFiles()
        relocate("org.yaml.snakeyaml", "gg.flyte.pluginportal.libs.yaml")
        // Legacy servers bundle Gson versions that cannot parse the current API responses.
        relocate("com.google.gson", "gg.flyte.pluginportal.libs.gson")
        relocate("org.bstats", "gg.flyte.pluginportal.libs.bstats")
    }

    build {
        dependsOn(shadowJar)
    }

    jar {
        enabled = false
    }
}
