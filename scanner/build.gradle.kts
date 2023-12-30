plugins {

}

dependencies {
    implementation(projects.pluginportalCommon)

    implementation(libs.gson)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.hangar.wrapper)
    implementation(libs.kmongo)
    implementation(libs.dotenv.kotlin)
}

publishShadowJar()
