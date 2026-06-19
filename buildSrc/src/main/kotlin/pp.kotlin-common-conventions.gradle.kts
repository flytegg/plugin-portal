plugins {
    id("org.jetbrains.kotlin.jvm")
}

repositories {
    mavenCentral()
}

java {
    javaTarget(17)
}

kotlin {
    jvmToolchain(17)
}