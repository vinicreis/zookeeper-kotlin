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
    buildUponDefaultConfig = true // preconfigure defaults
    allRules = false // activate all available (even unstable) rules.
    source.setFrom(projectSource)
    config.setFrom(configFile) // point to your custom config defining rules to run, overwriting default behavior
    baseline = baselineFile
}

tasks.withType<Detekt>().configureEach {
    gradle.startParameter.isContinueOnFailure = true

    include(kotlinFiles)
    exclude(resourceFiles, buildFiles)

    reports {
        html.required.set(true) // observe findings in your browser with structure and code snippets
        html.outputLocation.set(file("build/reports/detekt.html"))
        md.required.set(true) // simple Markdown format
        md.outputLocation.set(file("build/reports/detekt.md"))
    }
}

tasks.withType<Detekt>().configureEach {
    jvmTarget = "17"
}

tasks.withType<DetektCreateBaselineTask>().configureEach {
    jvmTarget = "17"
}
