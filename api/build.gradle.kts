plugins {
    id("net.kyori.blossom")
    id("org.jetbrains.gradle.plugin.idea-ext")
}

sourceSets {
    main {
        blossom {
            javaSources {
                property("version", project.version.toString())
                property("impl_version", "git-PluginPortal-${project.version}:${rootProject.latestCommitHash()}")
            }
        }
    }
}

dependencies {
    compileOnlyApi(libs.snakeYaml)
    compileOnlyApi(libs.guava)
}

java {
    withJavadocJar()
}

publishShadowJar()
