pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "zookeeper-kotlin"
include(
    "model",
    "client",
    "client",
    "controller",
    "node"
)
