plugins {
    // Support convention plugins written in Kotlin. Convention plugins are build scripts in 'src/main' that automatically become available as plugins in the main build.
    `kotlin-dsl`
}

repositories {
    // Use the plugin portal to apply community plugins in convention plugins.
    gradlePluginPortal()
}

dependencies {
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.shadow.gradle.plugin)
    implementation("xyz.jpenilla.run-paper:xyz.jpenilla.run-paper.gradle.plugin:2.3.1")
}
