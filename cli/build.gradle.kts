plugins {
    kotlin("jvm") version "1.9.0"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
    implementation("com.github.Revxrsal.Lamp:cli:3.1.5")
    implementation("com.github.ajalt.clikt:clikt:4.2.0")
    //implementation("com.github.ajalt:mordant:1.2.1") // Colors
    //implementation("com.jakewharton.picnic:picnic:0.5.0") // Border around Text
    implementation("com.github.kotlin-inquirer:kotlin-inquirer:0.1.0") // Arrow Key Navigation


    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.14.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
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