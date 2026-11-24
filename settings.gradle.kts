pluginManagement {
    val shadowVersion: String by settings
    val githubGradleVersion: String by settings
    val runPaperVersion: String by settings
    plugins {
        id("com.gradleup.shadow") version shadowVersion
        id("io.github.intisy.github-gradle") version githubGradleVersion
        id("xyz.jpenilla.run-paper") version runPaperVersion
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "Slimefun"

include("core")
include("stubs")

// Addons kept in this repository (addons/<Name>), each built against this build's core jar.
val bundledAddons = listOf("InfinityExpansion", "LiteXpansion", "Networks", "SlimefunAdvancements", "SMG", "Supreme")
bundledAddons.forEach { include("addons:$it") }
gradle.extra["bundledAddons"] = bundledAddons
