plugins {
    kotlin("jvm") version "1.9.0"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.github.ajalt.mordant:mordant:2.1.0")
    implementation("gg.flyte:hangarWrapper:1.1.1")

    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation("com.google.code.gson:gson:2.10.1")
}

application {
    mainClass.set("gg.flyte.pluginPortal.PluginPortal")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    build {
        dependsOn(shadowJar)
    }

    jar {
        manifest {
            attributes["Main-Class"] = application.mainClass
        }
    }
}