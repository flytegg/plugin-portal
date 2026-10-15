pluginManagement { includeBuild("build-logic") }
plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0" }
rootProject.name = "plugin-portal"
include("core", "platforms:bukkit", "platforms:velocity", "distribution", "plugin")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://jitpack.io")
    }
}
