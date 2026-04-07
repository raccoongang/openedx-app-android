import org.edx.builder.ConfigHelper
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val configHelper: ConfigHelper by rootProject.extra
val appConfig = configHelper.fetchConfig()
val appId = appConfig.getOrDefault("APPLICATION_ID", "org.openedx.app") as String
val themeDirectory = appConfig.getOrDefault("THEME_DIRECTORY", "openedx") as String
@Suppress("UNCHECKED_CAST")
val firebaseConfig = appConfig["FIREBASE"] as? Map<String, Any>
val firebaseEnabled = firebaseConfig?.getOrDefault("ENABLED", false) == true

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.compose)
}

if (firebaseEnabled) {
    apply(plugin = "com.google.gms.google-services")
    apply(plugin = "com.google.firebase.crashlytics")

    tasks.register("generateGoogleServicesJson") {
        doLast { configHelper.generateGoogleServicesJson(appId) }
    }
    tasks.named("preBuild") { dependsOn("generateGoogleServicesJson") }
} else {
    tasks.register("removeGoogleServicesJson") {
        doLast { configHelper.removeGoogleServicesJson() }
    }
    tasks.named("preBuild") { dependsOn("removeGoogleServicesJson") }
}

android {
    namespace = "org.openedx.app"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = appId
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0.0"

        androidResources.localeFilters += listOf("en", "uk")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    flavorDimensions += "env"
    productFlavors {
        create("prod") {
            dimension = "env"
            setupBranchConfigFields(this)
            setupFirebaseConfigFields(this)
        }
        create("develop") {
            dimension = "env"
            setupBranchConfigFields(this)
            setupFirebaseConfigFields(this)
        }
        create("stage") {
            dimension = "env"
            setupBranchConfigFields(this)
            setupFirebaseConfigFields(this)
        }
    }

    sourceSets {
        getByName("prod") {
            res.srcDirs("src/$themeDirectory/res")
        }
        getByName("develop") {
            res.srcDirs("src/$themeDirectory/res")
        }
        getByName("stage") {
            res.srcDirs("src/$themeDirectory/res")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

            if (firebaseEnabled) {
                configure<com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension> {
                    mappingFileUploadEnabled = false
                }
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.set(listOf("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode"))
        }
    }
    buildFeatures {
        viewBinding = true
        compose = true
        buildConfig = true
    }
    bundle {
        language {
            enableSplit = false
        }
        density {
            enableSplit = true
        }
        abi {
            enableSplit = true
        }
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":auth"))
    implementation(project(":course"))
    implementation(project(":dashboard"))
    implementation(project(":discovery"))
    implementation(project(":profile"))
    implementation(project(":discussion"))
    implementation(project(":whatsnew"))
    implementation(project(":dates"))
    implementation(project(":downloads"))

    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.datastore.preferences)

    api(platform(libs.firebase.bom))
    api(libs.firebase.messaging)

    // Braze SDK Integration
    implementation(libs.braze.sdk)

    // Plugins
    implementation(libs.openedx.firebase.analytics)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.mockk.android)
    testImplementation(libs.androidx.arch.core.testing)
}

fun setupBranchConfigFields(buildType: com.android.build.api.dsl.ProductFlavor) {
    @Suppress("UNCHECKED_CAST")
    val branchConfig = configHelper.fetchConfig()["BRANCH"] as? Map<String, Any>
    var branchKey = ""
    var branchUriScheme = ""
    var branchHost = ""
    var branchAlternateHost = ""

    if (branchConfig != null && branchConfig["ENABLED"] == true) {
        branchKey = branchConfig.getOrDefault("KEY", "") as String
        branchUriScheme = branchConfig.getOrDefault("URI_SCHEME", "") as String
        branchHost = branchConfig.getOrDefault("HOST", "") as String
        branchAlternateHost = branchConfig.getOrDefault("ALTERNATE_HOST", "") as String

        if (branchKey.isEmpty() || branchUriScheme.isEmpty() || branchHost.isEmpty() || branchAlternateHost.isEmpty()) {
            throw IllegalStateException("One or more Branch configuration fields are empty.")
        }
    }

    buildType.resValue("string", "branch_key", branchKey)
    buildType.resValue("string", "branch_uri_scheme", branchUriScheme)
    buildType.resValue("string", "branch_host", branchHost)
    buildType.resValue("string", "branch_alternate_host", branchAlternateHost)
}

fun setupFirebaseConfigFields(buildType: com.android.build.api.dsl.ProductFlavor) {
    @Suppress("UNCHECKED_CAST")
    val fbConfig = configHelper.fetchConfig()["FIREBASE"] as? Map<String, Any>
    val fbEnabled = fbConfig?.getOrDefault("ENABLED", false) == true
    val cloudMessagingEnabled = fbConfig?.getOrDefault("CLOUD_MESSAGING_ENABLED", false) == true

    buildType.manifestPlaceholders["fcmEnabled"] = fbEnabled && cloudMessagingEnabled
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
