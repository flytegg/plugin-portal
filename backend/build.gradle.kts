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

//    implementation(libs.logback)
//    implementation(libs.logging)

    implementation("ch.qos.logback:logback-core:1.4.14")
    implementation("org.slf4j:slf4j-api:2.0.11")
    implementation("ch.qos.logback:logback-classic:1.4.14")

    implementation(libs.kmongo)
    implementation(libs.dotenv.kotlin)

    implementation(libs.kotlinx.coroutines.core)
    implementation("io.ktor:ktor-server-call-logging-jvm:2.3.7")
    implementation("io.ktor:ktor-server-caching-headers-jvm:2.3.7")
}

application {
    mainClass.set("gg.flyte.pluginportal.backend.ApplicationKt")
}


//publishShadowJar()
