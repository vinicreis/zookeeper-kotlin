import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    `java-library`
    kotlin("jvm")

    id("zookeeper.detekt")
}

val libs = the<LibrariesForLibs>()
val major = 1
val minor = 0
val patch = 0

group = "io.github.vinicreis"
version = "$major.$minor.$patch" +
        if(properties.contains("snapshot")) "-SNAPSHOT" else ""

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation(platform(libs.junit.bom))
}

kotlin {
    jvmToolchain(17)
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.test {
    useJUnitPlatform()
}
