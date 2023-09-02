plugins {
    kotlin("jvm") version "1.9.0"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.github.ajalt.mordant:mordant:2.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    implementation(files("libs/hangarWrapper-1.1.7-all.jar"))
}

application {
    mainClass.set("gg.flyte.pluginPortal.PluginPortal")
}

tasks {
    compileKotlin {
//        kotlinOptions.jvmTarget = "1.8"
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