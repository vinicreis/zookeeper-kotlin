import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    `java-library`
    kotlin("jvm")

    id("zookeeper.detekt")
}

val libs = the<LibrariesForLibs>()

group = "io.github.vinicreis"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation(platform(libs.junit.bom))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}
