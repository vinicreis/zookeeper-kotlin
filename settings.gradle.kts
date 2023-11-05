pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "zookeeper-kotlin"
include("model")
include("client")
include("client")
include("controller")
include("node")
