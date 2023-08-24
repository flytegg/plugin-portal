repositories {
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
    }
}

dependencies {
    compileOnly(project(":common"))

    compileOnly("net.md-5:bungeecord-api:1.19-R0.1-SNAPSHOT")
    compileOnly("net.kyori:adventure-platform-bungeecord:4.3.0")
}