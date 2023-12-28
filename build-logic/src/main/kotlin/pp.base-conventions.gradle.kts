import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.jvm")
    `java-library`
    `maven-publish`
}

tasks {
    // Variable replacements
    processResources {
        filesMatching(listOf("plugin.yml", "bungee.yml")) {
            expand("version" to project.version, "description" to project.description)
        }
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name()
        (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
    }
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.compilerArgs.addAll(listOf("-nowarn", "-Xlint:-unchecked", "-Xlint:-deprecation"))
    }
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    test {
        useJUnitPlatform()
    }
}

java {
    javaTarget(8)
    withSourcesJar()
}

publishing {
    publications.create<MavenPublication>("mavenJava") {
        groupId = rootProject.group as String
        artifactId = project.name
        version = rootProject.version as String
    }
    repositories.maven {
        name = "flyte-repository"
        url = uri("https://repo.flyte.gg/releases/")
        credentials(PasswordCredentials::class)
        authentication {
            create<BasicAuthentication>("basic")
        }
    }
}