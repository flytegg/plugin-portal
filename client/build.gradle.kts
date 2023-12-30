plugins {
    alias(libs.plugins.ktorfit)
    alias(libs.plugins.ksp)
}

dependencies {
    compileOnlyApi(projects.pluginportalApi)

    implementation(libs.logback)

    api(libs.ktor.client.core)
    api(libs.ktor.client.cio)
    api(libs.ktor.client.content.negotiation)
    api(libs.ktor.client.serialization)
    api(libs.ktor.client.serialization.gson)
    api(libs.ktor.client.logging)

    ksp(libs.ktorfit.ksp)
    implementation(libs.ktorfit.lib)
}

publishShadowJar()
