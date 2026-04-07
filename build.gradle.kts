import io.gitlab.arturbosch.detekt.Detekt
import org.edx.builder.ConfigHelper
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.regex.Pattern

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(libs.snakeyaml)
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.detekt) apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}

// Android SDK versions (accessible from subprojects via rootProject.extra)
val compileSdkVersion by extra(36)
val targetSdkVersion by extra(36)
val minSdkVersion by extra(24)
val javaVersion by extra(JavaVersion.VERSION_17)
val jvmTargetVersion by extra(JvmTarget.JVM_17)

val configHelper by extra(ConfigHelper(projectDir, getCurrentFlavor()))

fun getCurrentFlavor(): String {
    val tskReqStr = gradle.startParameter.taskRequests.toString()
    val pattern = when {
        tskReqStr.contains("assemble") -> Pattern.compile("assemble(\\w+)(Release|Debug)")
        tskReqStr.contains("bundle") -> Pattern.compile("bundle(\\w+)(Release|Debug)")
        else -> Pattern.compile("generate(\\w+)(Release|Debug)")
    }
    val matcher = pattern.matcher(tskReqStr)
    return if (matcher.find()) {
        matcher.group(1).lowercase()
    } else {
        ""
    }
}

tasks.register("generateMockedRawFile") {
    doLast { configHelper.generateMicrosoftConfig() }
}

val projectSource = file(projectDir)
val configFile = files("$rootDir/config/detekt.yml")
val basePathFile = rootProject.projectDir.absolutePath
val kotlinFiles = "**/*.kt"
val resourceFiles = "**/resources/**"
val buildFiles = "**/build/**"

apply(plugin = "io.gitlab.arturbosch.detekt")

tasks.register<Detekt>("detektAll") {
    val autoFix = project.hasProperty("detektAutoFix")

    description = "Custom DETEKT build for all modules"
    parallel = true
    ignoreFailures = false
    autoCorrect = autoFix
    buildUponDefaultConfig = true
    setSource(projectSource)
    config.setFrom(configFile)
    include(kotlinFiles)
    basePath = basePathFile
    exclude(resourceFiles, buildFiles)
    reports {
        html.required.set(true)
        xml.required.set(true)
        txt.required.set(false)
        sarif.required.set(true)
    }
}

dependencies {
    "detektPlugins"(libs.detekt.formatting)
}
