plugins {
    java
    id("com.gradleup.shadow")
    id("io.github.intisy.github-gradle")
}

group = "me.waleks"
description = "SimpleMaterialGenerators is a Slimefun addon that adds simple material generators."

apply(from = rootProject.file("addons/addon.gradle"))
