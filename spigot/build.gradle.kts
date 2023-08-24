repositories {
    mavenCentral()

    maven {
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }

    maven {
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }
}

dependencies {
    compileOnly(project(":common"))
    compileOnly("org.spigotmc:spigot-api:1.18.2-R0.1-SNAPSHOT")
    implementation("net.kyori:adventure-platform-bukkit:4.3.0")
    compileOnly("me.clip:placeholderapi:2.11.1")
}