import org.edx.builder.ConfigHelper
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(libs.snakeyaml)
    }
}

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.androidx.room)
}

room {
    schemaDirectory("$projectDir/schemas")
}

val configHelper: ConfigHelper by rootProject.extra

val currentFlavour = getCurrentFlavor()
val config = configHelper.fetchConfig()
val themeDirectory = config.getOrDefault("THEME_DIRECTORY", "openedx") as String

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.set(listOf("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode"))
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            api(project(":foundation"))
        }
        androidMain {
            kotlin.srcDir("src/main/java")
        }
        androidMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)

            // jsoup
            api(libs.jsoup)

            // Firebase
            api(libs.firebase.common.ktx)
            api(libs.firebase.crashlytics.ktx)

            // Play In-App Review
            api(libs.google.inAppReview)

            // Branch SDK Integration
            api(libs.branch.sdk)
            api(libs.google.playServices.adsIdentifier)
            api(libs.installReferrer)

            // Zip
            api(libs.zip4j)
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "org.openedx.core"
    generateResClass = always
}

android {
    namespace = "org.openedx.core"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    flavorDimensions += "env"
    productFlavors {
        create("prod") {
            dimension = "env"
            insertBuildConfigFields(currentFlavour, this)
        }
        create("develop") {
            dimension = "env"
            insertBuildConfigFields(currentFlavour, this)
        }
        create("stage") {
            dimension = "env"
            insertBuildConfigFields(currentFlavour, this)
        }
    }

    sourceSets {
        getByName("main") {
            java.srcDirs(emptyList<String>())
            assets.srcDirs("assets")
        }
        getByName("prod") {
            java.srcDirs("src/$themeDirectory")
            res.srcDirs("src/$themeDirectory/res")
        }
        getByName("develop") {
            java.srcDirs("src/$themeDirectory")
            res.srcDirs("src/$themeDirectory/res")
        }
        getByName("stage") {
            java.srcDirs("src/$themeDirectory")
            res.srcDirs("src/$themeDirectory/res")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = true
        compose = true
        buildConfig = true
    }
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosX64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)

    // These don't work in KMP sourceSets DSL
    api(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    api(platform(libs.firebase.bom))
    debugApi(libs.compose.ui.tooling)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso)
}

fun getCurrentFlavor(): String {
    val tskReqStr = gradle.startParameter.taskRequests.toString()
    val pattern = when {
        tskReqStr.contains("assemble") -> Regex("assemble(\\w+)(Release|Debug)")
        tskReqStr.contains("bundle") -> Regex("bundle(\\w+)(Release|Debug)")
        else -> Regex("generate(\\w+)(Release|Debug)")
    }
    val match = pattern.find(tskReqStr)
    return match?.groupValues?.get(1)?.lowercase() ?: ""
}

fun insertBuildConfigFields(currentFlavour: String, buildType: com.android.build.api.dsl.ProductFlavor) {
    if (currentFlavour == buildType.name) {
        configHelper.generateConfigJson()
        configHelper.generateMicrosoftConfig()
    }
    val config = configHelper.fetchConfig()
    val platformName = config.getOrDefault("PLATFORM_NAME", "") as String
    val platformFullName = config.getOrDefault("PLATFORM_FULL_NAME", "") as String

    buildType.resValue("string", "platform_name", platformName)
    buildType.resValue("string", "platform_full_name", platformFullName)
    insertFacebookBuildConfigFields(config, buildType)
    insertMicrosoftManifestPlaceholder(config, buildType)
}

fun insertFacebookBuildConfigFields(config: Map<String, Any>, buildType: com.android.build.api.dsl.ProductFlavor) {
    @Suppress("UNCHECKED_CAST")
    val facebook = config["FACEBOOK"] as? Map<String, Any>
    var facebookAppId = ""
    var facebookClientToken = ""
    if (facebook != null) {
        facebookAppId = facebook.getOrDefault("FACEBOOK_APP_ID", "") as String
        facebookClientToken = facebook.getOrDefault("CLIENT_TOKEN", "") as String
    }
    buildType.resValue("string", "facebook_app_id", facebookAppId)
    buildType.resValue("string", "fb_login_protocol_scheme", "fb$facebookAppId")
    buildType.resValue("string", "facebook_client_token", facebookClientToken)
}

fun insertMicrosoftManifestPlaceholder(config: Map<String, Any>, buildType: com.android.build.api.dsl.ProductFlavor) {
    @Suppress("UNCHECKED_CAST")
    val microsoft = config["MICROSOFT"] as? Map<String, Any>
    var microsoftPackageSignature = ""
    if (microsoft != null) {
        microsoftPackageSignature = microsoft.getOrDefault("PACKAGE_SIGNATURE", "") as String
    }
    buildType.manifestPlaceholders["microsoftSignature"] = microsoftPackageSignature
}
