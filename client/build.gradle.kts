plugins {
    alias(libs.plugins.ktorfit)
    alias(libs.plugins.ksp)
}

dependencies {
    compileOnlyApi(projects.pluginportalApi)

    implementation(libs.logback)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.serialization)
    implementation(libs.ktor.client.serialization.gson)
    implementation(libs.ktor.client.logging)

    ksp(libs.ktorfit.ksp)
    implementation(libs.ktorfit.lib)
}

publishShadowJar()
