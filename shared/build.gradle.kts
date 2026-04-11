plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Feature modules
            api(project(":core"))
            implementation(project(":auth"))
            implementation(project(":course"))
            implementation(project(":dashboard"))
            implementation(project(":dates"))
            implementation(project(":discovery"))
            implementation(project(":discussion"))
            implementation(project(":downloads"))
            implementation(project(":profile"))
            implementation(project(":whatsnew"))

            // Compose Multiplatform
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)

            // Coroutines
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

            // Ktor
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)

            // kotlinx.serialization
            implementation(libs.kotlinx.serialization.json)

            // Koin
            implementation("io.insert-koin:koin-core:4.1.0")

            // Lifecycle ViewModel (KMP)
            implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.9.0-alpha07")
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation("androidx.security:security-crypto:1.1.0-alpha06")
            implementation(libs.media3.exoplayer)
            implementation(libs.media3.ui)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

android {
    namespace = "org.openedx.shared"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        missingDimensionStrategy("env", "develop")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
