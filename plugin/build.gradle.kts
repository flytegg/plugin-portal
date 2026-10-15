plugins { base }
// Compatibility entrypoints for existing build scripts.
tasks.register("compileKotlin") { dependsOn(":core:compileKotlin", ":platforms:bukkit:compileKotlin", ":platforms:velocity:compileKotlin") }
tasks.register("shadowJar") { dependsOn(":distribution:shadowJar") }
tasks.register("runServer") { dependsOn(":distribution:runServer") }
tasks.register("paperSmoke") { dependsOn(":platforms:bukkit:paperSmoke") }
tasks.named("build") { dependsOn(":distribution:build") }
