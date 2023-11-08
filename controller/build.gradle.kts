plugins {
    id("zookeeper.kotlin-conventions")
}

dependencies {
    api(project(":model"))

    implementation(libs.coroutines.core)
}

tasks.withType<Jar> {
    manifest {
        attributes(
            "Main-Class" to "io.github.vinicreis.controller.Controller"
        )
    }
}
