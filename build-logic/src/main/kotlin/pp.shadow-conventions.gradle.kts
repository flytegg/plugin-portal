import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.jvm.tasks.Jar

plugins {
    id("pp.base-conventions")
    id("com.github.johnrengelman.shadow")
}

tasks {
    named<Jar>("jar") {
        archiveClassifier.set("unshaded")
        from(project.rootProject.file("LICENSE"))
    }
    val shadowJar = named<ShadowJar>("shadowJar") {
        archiveClassifier.set("")
        configureRelocations()
        minimize()
    }
    named("build") {
        dependsOn(shadowJar)
    }
}

fun ShadowJar.configureRelocations() {
    relocate("com.google.gson", "gg.flyte.pluginportal.libs.gson")
    relocate("org.bstats", "gg.flyte.pluginportal.libs.bstats")
    relocate("io.papermc.lib", "gg.flyte.pluginportal.libs.paperlib")
    relocate("com.mongodb", "gg.flyte.pluginportal.libs.mongodb")
    relocate("gg.flyte.twilight", "gg.flyte.pluginportal.libs.twilight")
    relocate("de.jensklingenberg.ktorfit", "gg.flyte.pluginportal.libs.ktorfit")
    relocate("kotlinx", "gg.flyte.pluginportal.libs.kotlinx")
    relocate("kotlin", "gg.flyte.pluginportal.libs.kotlin")
//    relocate("net.kyori.adventure", "gg.flyte.pluginportal.libs.adventure")

//    relocate("com.github.steveice10.opennbt", "gg.flyte.pluginportal.libs.opennbt")
//    relocate("it.unimi.dsi.fastutil", "gg.flyte.pluginportal.libs.fastutil")
//    relocate("space.vectrix.flare", "gg.flyte.pluginportal.libs.flare")
//    relocate("net.lenni0451.mcstructs", "gg.flyte.pluginportal.libs.mcstructs")
}