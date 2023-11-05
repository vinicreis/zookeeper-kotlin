plugins {
    id("zookeeper.kotlin-conventions")
}

dependencies {
    api(project(":model"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}
