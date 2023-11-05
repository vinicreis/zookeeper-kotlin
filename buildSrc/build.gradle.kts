plugins {
    `kotlin-dsl`
    id("io.gitlab.arturbosch.detekt") version "1.23.3"
}

repositories {
    gradlePluginPortal() // so that external plugins can be resolved in dependencies section
}