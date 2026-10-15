plugins {
    id("pp.kotlin-common-conventions")
}

dependencies {
    // Mock Bukkit?
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks {
    test {
        useJUnitPlatform()
    }
}