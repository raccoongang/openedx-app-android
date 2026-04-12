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

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            // Compose Multiplatform
            api(compose.runtime)
            api(compose.foundation)
            api(compose.material3)
            api(compose.ui)
            api(compose.materialIconsExtended)

            // Coroutines
            api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

            // Koin core + compose
            api("io.insert-koin:koin-core:4.1.0")
            api("io.insert-koin:koin-compose:4.1.0")
            api("io.insert-koin:koin-compose-viewmodel:4.1.0")

            // kotlinx.serialization
            api(libs.kotlinx.serialization.json)

            // kotlinx.datetime
            api(libs.kotlinx.datetime)

            // Compose Multiplatform Resources
            api(compose.components.resources)
            api(compose.components.uiToolingPreview)

            // Lifecycle (KMP since 2.8.0)
            api("androidx.lifecycle:lifecycle-viewmodel:2.9.0")
            api("androidx.lifecycle:lifecycle-common:2.9.0")

            // Ktor
            api(libs.ktor.client.core)
            api(libs.ktor.client.content.negotiation)
            api(libs.ktor.client.logging)
            api(libs.ktor.client.auth)
            api(libs.ktor.serialization.kotlinx.json)

            // Coil (Compose Multiplatform)
            api(libs.coil.compose)
            api(libs.coil.network.ktor)

            // Room (KMP)
            api(libs.androidx.room.runtime)
            api(libs.androidx.sqlite.bundled)

            // Navigation Compose (KMP — JetBrains multiplatform wrapper)
            api(libs.jetbrains.navigation.compose)

        }

        androidMain.dependencies {
            // Compose extras
            api("androidx.compose.material:material")
            api("androidx.compose.runtime:runtime-livedata")

            // AndroidX Lifecycle (Android-specific versions)
            api("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.0")
            api("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.0")
            api("androidx.lifecycle:lifecycle-runtime-compose:2.9.0")
            api("androidx.lifecycle:lifecycle-common:2.9.0")

            // AndroidX
            api("androidx.fragment:fragment-ktx:1.8.6")
            api("androidx.appcompat:appcompat:1.7.0")
            api("androidx.viewpager2:viewpager2:1.1.0")
            api(libs.androidx.room.ktx)
            api("androidx.work:work-runtime-ktx:2.10.0")
            api("androidx.window:window:1.3.0")
            api("androidx.constraintlayout:constraintlayout-compose:1.1.0")
            api("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
            api("androidx.webkit:webkit:1.12.1")
            api("com.google.android.material:material:1.12.0")

            // Koin Android
            api("io.insert-koin:koin-android:4.1.0")
            api("io.insert-koin:koin-androidx-compose:4.1.0")

            // OkHttp
            api("com.squareup.okhttp3:okhttp:5.1.0")
            api("com.squareup.okhttp3:logging-interceptor:5.1.0")

            // Ktor OkHttp engine
            api(libs.ktor.client.okhttp)

            // Coroutines test
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")

            // Testing
            implementation(libs.junit)
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "org.openedx.foundation"
    generateResClass = always
}

android {
    namespace = "org.openedx.foundation"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    flavorDimensions += "env"
    productFlavors {
        create("prod") { dimension = "env" }
        create("develop") { dimension = "env" }
        create("stage") { dimension = "env" }
    }
}

dependencies {
    api(platform("androidx.compose:compose-bom:2025.05.01"))
}
