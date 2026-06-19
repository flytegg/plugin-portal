plugins {
    id("pp.plugin-conventions")
    id("io.papermc.hangar-publish-plugin")
    id("com.modrinth.minotaur")
}

repositories {
    maven("https://repo.flyte.gg/releases")
    maven("https://jitpack.io")
}

dependencies {
    // MCLicense library removed - using unified API authentication instead
    
    implementation("com.github.HangarMC:HangarJarScanner:906710dc36")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("dev.masecla:Modrinth4J:2.0.0")
    
    implementation("org.java-websocket:Java-WebSocket:1.5.7")
}

val supportedMinecraftVersions = (property("mcVersions") as String)
    .split(",")
    .map { it.trim() }
    .filter { it.isNotEmpty() }

val marketplaceChangelog = (findProperty("marketplaceChangelog") as? String) ?: "Release ${project.version}"
val modrinthVersionType = (findProperty("modrinthVersionType") as? String) ?: "release"

modrinth {
    token.set(System.getenv("MODRINTH_TOKEN"))
    projectId.set((findProperty("modrinthProjectId") as? String) ?: "pluginportal")
    versionName.set("PluginPortal ${project.version}")
    versionNumber.set(project.version as String)
    versionType.set(modrinthVersionType)
    uploadFile.set(tasks.shadowJar)
    gameVersions.addAll(supportedMinecraftVersions)
    loaders.addAll("bukkit", "folia", "paper", "purpur", "spigot")
    changelog.set(marketplaceChangelog)
    debugMode.set((findProperty("modrinthDebugMode") as? String)?.toBoolean() ?: false)
}

hangarPublish {
    publications.register("plugin") {
        version = project.version as String
        id = (findProperty("hangarProjectId") as? String) ?: "PluginPortal"
        channel = (findProperty("hangarChannel") as? String) ?: "Release"
        changelog = marketplaceChangelog
        apiKey = System.getenv("HANGAR_API_TOKEN")

        platforms {
            paper {
                jar = tasks.shadowJar.flatMap { it.archiveFile }
                platformVersions = supportedMinecraftVersions
            }
        }
    }
}
