import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
    alias(libs.plugins.kotlin.compose)
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
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
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
    // Compose
    api(platform("androidx.compose:compose-bom:2025.05.01"))
    api("androidx.compose.material3:material3")
    api("androidx.compose.ui:ui")
    api("androidx.compose.runtime:runtime")
    api("androidx.compose.foundation:foundation")

    // AndroidX
    api("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.0")
    api("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.0")
    api("androidx.lifecycle:lifecycle-runtime-compose:2.9.0")
    api("androidx.lifecycle:lifecycle-common:2.9.0")
    api("androidx.fragment:fragment-ktx:1.8.6")
    api("androidx.appcompat:appcompat:1.7.0")

    // Gson (for objectToString/stringToObject compatibility)
    api("com.google.code.gson:gson:2.13.1")

    // Coroutines
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")

    // Transitive dependencies (previously from external foundation library)
    api("io.insert-koin:koin-core:4.1.0")
    api("io.insert-koin:koin-android:4.1.0")
    api("io.insert-koin:koin-androidx-compose:4.1.0")
    api("androidx.room:room-runtime:2.7.2")
    api("androidx.room:room-ktx:2.7.2")
    api("com.squareup.retrofit2:retrofit:3.0.0")
    api("com.squareup.retrofit2:converter-gson:3.0.0")
    api("com.squareup.okhttp3:okhttp:5.1.0")
    api("com.squareup.okhttp3:logging-interceptor:5.1.0")
    api("io.coil-kt:coil-compose:2.7.0")
    api("io.coil-kt:coil-gif:2.7.0")
    api("io.coil-kt:coil:2.7.0")
    api("androidx.viewpager2:viewpager2:1.1.0")
    api("androidx.work:work-runtime-ktx:2.10.0")
    api("androidx.window:window:1.3.0")
    api("androidx.constraintlayout:constraintlayout-compose:1.1.0")
    api("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    api("androidx.webkit:webkit:1.12.1")
    api("com.google.android.material:material:1.12.0")
    api("androidx.compose.material:material-icons-extended")
    api("androidx.compose.material:material")
    api("androidx.compose.runtime:runtime-livedata")

    testImplementation(libs.junit)
}
