plugins {
    alias(libs.plugins.run.paper)
}

tasks.runServer {
    minecraftVersion("1.20.4")
}

dependencies {
    implementation(projects.pluginportalCommon)

    compileOnly(libs.paper) {
        exclude("junit", "junit")
        exclude("com.google.code.gson", "gson")
        exclude("javax.persistence", "persistence-api")
    }

    implementation(libs.gson)
    implementation(libs.bstats)
    implementation(libs.paperlib)
    implementation(libs.twilight)

    implementation(libs.lamp.common)
    implementation(libs.lamp.bukkit)

    implementation(libs.cloud.paper)
    implementation(libs.cloud.extras)
    implementation(libs.cloud.extensions)
    implementation(libs.cloud.coroutines)
    implementation(libs.cloud.annotations)
    annotationProcessor(libs.cloud.annotations)


    implementation(libs.adventure.text.minimessage)
    implementation(libs.adventure.platform.bukkit)
    implementation(libs.triumph.guis)

    implementation(libs.kotlinx.coroutines.core)
}


