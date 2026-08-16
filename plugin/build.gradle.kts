plugins {
    id("pp.plugin-conventions")
    id("pp.test-conventions")
    id("io.papermc.hangar-publish-plugin")
    id("com.modrinth.minotaur")
}

val mockBukkitJavaVersion = 21

configurations.matching { it.name in setOf("testCompileClasspath", "testRuntimeClasspath") }.configureEach {
    attributes.attribute(
        org.gradle.api.attributes.java.TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE,
        mockBukkitJavaVersion,
    )
}

tasks.named<org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile>("compileTestKotlin") {
    compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
}

tasks.named<JavaCompile>("compileTestJava") {
    options.release.set(mockBukkitJavaVersion)
}

tasks.named<Test>("test") {
    javaLauncher.set(
        javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(mockBukkitJavaVersion))
        }
    )
}

tasks.register<Exec>("paperSmoke") {
    group = "verification"
    description = "Starts a disposable Paper server and verifies Plugin Portal startup and commands."
    workingDir(rootProject.projectDir)
    commandLine("bun", "scripts/smoke-run-paper.ts")
}

repositories {
    maven("https://repo.flyte.gg/releases")
    maven("https://jitpack.io")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation("org.mclicense:library:1.5.1")

    implementation("com.github.HangarMC:HangarJarScanner:906710dc36")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("dev.masecla:Modrinth4J:2.0.0")
    
    implementation("org.java-websocket:Java-WebSocket:1.5.7")

    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v1.21:4.110.0")
    testImplementation("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
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
