import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    id("com.github.johnrengelman.shadow")
}

tasks {
    val shadowJar = named<ShadowJar>("shadowJar") {
        archiveClassifier.set("")
        println(project.version)
        // print the location
        println(project.buildDir)
        archiveFileName.set("PluginPortal-${project.version}.jar")

        minimize()
        exclude("com/google/common/")
        relocate("org.bstats", "gg.flyte.pluginportal.libs.bstats")
    }
    named("build") {
        dependsOn(shadowJar)
    }
}