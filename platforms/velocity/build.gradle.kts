plugins { id("pp.kotlin-library-conventions"); id("pp.test-conventions") }
dependencies {
    api(project(":core"))
    compileOnly("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
}
tasks.processResources {
    val releaseVersion = project.version.toString() + ((findProperty("channel") as? String)?.takeUnless { it == "stable" }?.let { "-$it" } ?: "")
    inputs.property("releaseVersion", releaseVersion)
    filesMatching("velocity-plugin.json") { expand("version" to releaseVersion) }
}
