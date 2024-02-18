plugins {
    base
    id("pp.build-logic")
}

allprojects {
    group = "gg.flyte"
    version = property("projectVersion") as String // from gradle.properties
    description = "Minecraft plugin package manager."
}

val main = setOf(
    projects.pluginportal,
    projects.pluginportalApi,
    projects.pluginportalClient,
    projects.pluginportalScanner,
    projects.pluginportalBackend,
    projects.pluginportalCommon,
    projects.pluginportalBukkit,
).map { it.dependencyProject }

// val special = setOf().map { it.dependencyProject }

subprojects {
    when (this) {
        in main -> plugins.apply("pp.shadow-conventions")
        // in special -> plugins.apply("pp.base-conventions")
        else -> plugins.apply("pp.standard-conventions")
    }
}