plugins {
    alias(libs.plugins.ktorfit)
    alias(libs.plugins.ksp)
}

dependencies {
    compileOnlyApi(projects.pluginportalApi)

    implementation(libs.logback)

    implementation(libs.ktor.core)
    implementation(libs.ktor.cio)
    implementation(libs.ktor.content.negotiation)
    implementation(libs.ktor.serialization)
    implementation(libs.ktor.serialization.gson)
    implementation(libs.ktor.logging)

    ksp(libs.ktorfit.ksp)
    implementation(libs.ktorfit.lib)
}

publishShadowJar()
