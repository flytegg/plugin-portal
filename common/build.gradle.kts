dependencies {
    api(projects.pluginportalApi)
    api(projects.pluginportalClient)

    implementation(libs.kotlinx.coroutines.core)

    // Note: If manually starting tests doesn't work for you in IJ, change 'Gradle -> Run Tests Using' to 'IntelliJ IDEA'
    testImplementation(rootProject.libs.netty)
    testImplementation(rootProject.libs.guava)
    testImplementation(rootProject.libs.snakeYaml2)
    testImplementation(rootProject.libs.bundles.junit)
}

java {
    withJavadocJar()
}

tasks.named<Jar>("sourcesJar") {
    from(project(":pluginportal-api").sourceSets.main.get().allSource)
}

publishShadowJar()
