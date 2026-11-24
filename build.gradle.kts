// Root build: collects the core plugin jar and every bundled addon jar into deploy/.

@Suppress("UNCHECKED_CAST")
val bundledAddons = gradle.extra["bundledAddons"] as List<String>

// Resolves each project's shaded plugin jar (what goes in a server's plugins folder).
val deployJars: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
    isTransitive = false
}

dependencies {
    (listOf(":core") + bundledAddons.map { ":addons:$it" }).forEach { path ->
        deployJars(project(path = path, configuration = "shadowRuntimeElements"))
    }
}

// Sync, not Copy: deploy/ holds exactly this build's jars, so an older version of a plugin never sits
// beside the new one (Bukkit refuses to load either, "Ambiguous plugin name").
val deploy by tasks.registering(Sync::class) {
    group = "slimefun"
    description = "Builds core and the bundled addons and puts their plugin jars in deploy/."
    from(deployJars)
    into(layout.projectDirectory.dir("deploy"))
}

// `gradlew build` at the root also refreshes deploy/.
tasks.register("build") {
    group = "build"
    dependsOn(deploy)
}
