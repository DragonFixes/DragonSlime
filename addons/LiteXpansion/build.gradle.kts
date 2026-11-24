plugins {
    java
    id("com.gradleup.shadow")
    id("io.github.intisy.github-gradle")
}

group = "dev.j3fftw"
description = "LiteXpansion is a Slimefun addon inspired by Industrial Craft 2."

apply(from = rootProject.file("addons/addon.gradle"))

dependencies {
    implementation("org.bstats:bstats-bukkit:2.2.1")
}

tasks {
    shadowJar {
        relocate("org.bstats", "litexpansion.libs.bstats")
        relocate("dev.j3fftw.extrautils", "dev.j3fftw.litexpansion.extrautils")
    }
}
