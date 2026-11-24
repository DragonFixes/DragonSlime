plugins {
    java
    id("com.gradleup.shadow")
    id("io.github.intisy.github-gradle")
}

group = "io.github.sefiraat"
description = "Networks is a Slimefun addon that brings item storage and transportation networks."

apply(from = rootProject.file("addons/addon.gradle"))

repositories {
    maven("https://nexus.neetgames.com/repository/maven-public/")
    maven("https://repo.bg-software.com/repository/api/")
}

dependencies {
    compileOnly("org.jetbrains:annotations:23.0.0")
    // Compiled against the in-tree InfinityExpansion jar (its InfinityLib is relocated there).
    compileOnly(project(path = ":addons:InfinityExpansion", configuration = "shadowRuntimeElements")) { isTransitive = false }
    compileOnly("com.bgsoftware:WildChestsAPI:2024.1")
}
