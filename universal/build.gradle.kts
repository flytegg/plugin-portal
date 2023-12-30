import io.papermc.hangarpublishplugin.model.Platforms

plugins {
    id("io.papermc.hangar-publish-plugin") version "0.1.1"
//    id("com.modrinth.minotaur") version "2.+"
}

dependencies {
    api(projects.pluginportalCommon)
    api(projects.pluginportalBukkit)
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        archiveFileName.set("PluginPortal-${project.version}.jar")
        destinationDirectory.set(rootProject.projectDir.resolve("build/libs"))
    }
    sourcesJar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        rootProject.subprojects.forEach { subproject ->
            if (subproject == project) return@forEach
            val platformSourcesJarTask = subproject.tasks.findByName("sourcesJar") as? Jar ?: return@forEach
            dependsOn(platformSourcesJarTask)
            from(zipTree(platformSourcesJarTask.archiveFile))
        }
    }
}

publishShadowJar()

val branch = rootProject.branchName()
val baseVersion = project.version as String
val isRelease = !baseVersion.contains('-')
val isMainBranch = branch == "2.x"
if (!isRelease || isMainBranch) { // Only publish releases from the main branch
    val suffixedVersion = if (isRelease) baseVersion else baseVersion + "+" + System.getenv("GITHUB_RUN_NUMBER")
    val changelogContent = if (isRelease) {
        "See [GitHub](https://github.com/flytegg/plugin-portal) for release notes."
    } else {
        val commitHash = rootProject.latestCommitHash()
        "[$commitHash](https://github.com/flytegg/plugin-portal/commit/$commitHash) ${rootProject.latestCommitMessage()}"
    }

//    modrinth {
//        val mcVersions: List<String> = (property("mcVersions") as String)
//            .split(",")
//            .map { it.trim() }
//        token.set(System.getenv("MODRINTH_TOKEN"))
//        projectId.set("pluginportal")
//        versionType.set(if (isRelease) "release" else if (isMainBranch) "beta" else "alpha")
//        versionNumber.set(suffixedVersion)
//        versionName.set(suffixedVersion)
//        changelog.set(changelogContent)
//        uploadFile.set(tasks.shadowJar.flatMap { it.archiveFile })
//        gameVersions.set(mcVersions)
//        loaders.add("paper")
//        autoAddDependsOn.set(false)
//        detectLoaders.set(false)
//        dependencies {
//
//        }
//    }

    hangarPublish {
        publications.register("plugin") {
            version.set(suffixedVersion)
            id.set("PluginPortal")
            channel.set(if (isRelease) "Release" else if (isMainBranch) "Snapshot" else "Alpha")
            changelog.set(changelogContent)
            apiKey.set(System.getenv("HANGAR_API_KEY"))
            platforms {
                register(Platforms.PAPER) {
                    jar.set(tasks.shadowJar.flatMap { it.archiveFile })
                    platformVersions.set(listOf(property("mcVersionRange") as String))
                }
            }
        }
    }
}