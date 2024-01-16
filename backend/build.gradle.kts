plugins {
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.ktor)
}

dependencies {
    implementation(projects.pluginportalCommon)

    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.serialization.gson)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.resources)

    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.rate.limit)

    implementation(libs.logback)
    implementation(libs.logging)

    implementation(libs.kmongo)
    implementation(libs.dotenv.kotlin)

    implementation(libs.kotlinx.coroutines.core)
    implementation("io.ktor:ktor-server-call-logging-jvm:2.3.7")
    implementation("io.ktor:ktor-server-caching-headers-jvm:2.3.7")
}

application {
    mainClass.set("gg.flyte.pluginportal.backend.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}


publishShadowJar()
