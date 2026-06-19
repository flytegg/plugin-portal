import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    id("pp.kotlin-library-conventions")
    id("pp.test-conventions")
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
    compileOnly(libs.spigot.api)

    api(libs.bundles.lamp)
    
    // HTTP client for ORPC calls (already present as implementation, promote to api)
    api("com.squareup.okhttp3:okhttp:4.12.0")
    api("com.google.code.gson:gson:2.11.0")
    
    // Adventure API and platform dependencies
    api(libs.bundles.adventure)

    implementation("org.bstats:bstats-bukkit:3.1.0")
    implementation("gs.mclo:api:4.0.3")

    implementation("com.google.guava:guava:33.2.1-jre")
}
