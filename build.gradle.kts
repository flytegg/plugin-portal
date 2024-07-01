plugins {
    base
}

allprojects {
    group = "gg.flyte"
    version = property("projectVersion") as String
}