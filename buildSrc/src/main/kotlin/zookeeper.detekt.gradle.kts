import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask

plugins {
    id("io.gitlab.arturbosch.detekt")
}

val projectSource = file(projectDir)
val configFile = files("$rootDir/config/detekt.yml")
val baselineFile = file("$rootDir/config/baseline.xml")
val kotlinFiles = "**/*.kt"
val resourceFiles = "**/resources/**"
val buildFiles = "**/build/**"

detekt {
    buildUponDefaultConfig = true
    allRules = false
    source.setFrom(projectSource)
    config.setFrom(configFile)
    baseline = baselineFile
}

tasks.withType<Detekt>().configureEach {
    gradle.startParameter.isContinueOnFailure = true

    include(kotlinFiles)
    exclude(resourceFiles, buildFiles)

    reports {
        html.required.set(true)
        html.outputLocation.set(file("build/reports/detekt.html"))
        md.required.set(true)
        md.outputLocation.set(file("build/reports/detekt.md"))
    }
}

tasks.withType<Detekt>().configureEach {
    jvmTarget = "17"
}

tasks.withType<DetektCreateBaselineTask>().configureEach {
    jvmTarget = "17"
}
