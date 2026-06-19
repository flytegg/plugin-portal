plugins {
    base
    id("io.papermc.hangar-publish-plugin") version "0.1.2" apply false
    id("com.modrinth.minotaur") version "2.9.0" apply false
}

allprojects {
    group = "gg.flyte"
    version = property("projectVersion") as String
}
