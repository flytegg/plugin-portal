plugins {
    id("pp.kotlin-library-conventions")
    id("pp.test-conventions")
}
dependencies {
    api(libs.lamp.common)
    api(libs.adventure.api)
    api(libs.adventure.minimessage)
    api(libs.adventure.plain)
    api("com.squareup.okhttp3:okhttp:4.12.0")
    api("com.google.code.gson:gson:2.11.0")
    implementation("org.yaml:snakeyaml:2.2")
    implementation("gs.mclo:api:4.0.3")
    implementation("com.google.guava:guava:33.2.1-jre")
    implementation("com.github.HangarMC:HangarJarScanner:906710dc36")
    implementation("dev.masecla:Modrinth4J:2.0.0")
    implementation("org.java-websocket:Java-WebSocket:1.5.7")
}
