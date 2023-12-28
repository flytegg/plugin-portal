plugins {
    alias(libs.plugins.run.paper)
}

dependencies {
    compileOnlyApi(projects.pluginportalCommon)
    compileOnly(libs.paper) {
        exclude("junit", "junit")
        exclude("com.google.code.gson", "gson")
        exclude("javax.persistence", "persistence-api")
    }

    implementation(libs.bstats)
    implementation(libs.paperlib)
    implementation(libs.twilight)
    implementation(libs.lamp.common)
    implementation(libs.lamp.bukkit)
    implementation(libs.adventure.text.minimessage)
    implementation(libs.adventure.platform.bukkit)

    implementation(libs.mccoroutine.api)
    implementation(libs.mccoroutine.core)
    implementation(libs.kotlinx.coroutines.core)
}

publishShadowJar()
