plugins {
    id("pp.kotlin-common-conventions")
    id("pp.shadow-convention")
    id("xyz.jpenilla.run-paper")
}


val libs = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")

dependencies {
    implementation(project(":core"))

    compileOnly(libs.findLibrary("spigot-api").get())
}

tasks {
    runServer {
        minecraftVersion("1.21.11")
        runDirectory(file((project.findProperty("runDir") as? String) ?: "run/latest"))
        if ((project.findProperty("pluginPortalDev") as? String)?.toBoolean() == true) {
            jvmArgs("-Dpluginportal.dev=true")
        }
        javaLauncher.set(
            project.javaToolchains.launcherFor {
                languageVersion.set(JavaLanguageVersion.of(21))
            }
        )
    }

}
