plugins {
    id("pp.kotlin-library-conventions")
    id("pp.test-conventions")
}
dependencies {
    api(project(":core"))
    compileOnly(libs.spigot.api)
    implementation(libs.adventure.bukkit)
    implementation("org.bstats:bstats-bukkit:3.1.0")
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v1.21:4.110.0")
    testImplementation("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
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


tasks.processResources {
    val releaseVersion = project.version.toString() + ((findProperty("channel") as? String)?.takeUnless { it == "stable" }?.let { "-$it" } ?: "")
    inputs.property("releaseVersion", releaseVersion)
    filesMatching("plugin.yml") { expand("version" to releaseVersion) }
}
