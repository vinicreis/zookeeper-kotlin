plugins {
    id("zookeeper.kotlin-conventions")
}

dependencies {
    api(project(":model"))

    implementation(libs.coroutines.core)
}
