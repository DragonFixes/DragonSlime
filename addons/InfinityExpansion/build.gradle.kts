plugins {
    java
    id("com.gradleup.shadow")
    id("io.github.intisy.github-gradle")
}

group = "io.github.mooy1"
description = "InfinityExpansion is a Slimefun addon that adds machines, generators, and more endgame content."

apply(from = rootProject.file("addons/addon.gradle"))

dependencies {
    githubImplementation("Slimefun5:InfinityLib:v1.3.14.7")
}

tasks {
    shadowJar {
        relocate("io.github.mooy1.infinitylib", "io.github.mooy1.infinityexpansion.infinitylib")
        minimize()
        exclude("io/github/thebusybiscuit/slimefun5/**")
        exclude("me/mrCookieSlime/**")
    }
}
