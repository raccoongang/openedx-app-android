pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    buildscript {
        repositories {
            mavenCentral()
            maven {
                url = uri("https://storage.googleapis.com/r8-releases/raw")
            }
        }
        dependencies {
            classpath("com.android.tools:r8:8.12.14")
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("http://appboy.github.io/appboy-android-sdk/sdk")
            isAllowInsecureProtocol = true
        }
        maven { url = uri("https://pkgs.dev.azure.com/MicrosoftDeviceSDK/DuoSDK-Public/_packaging/Duo-SDK-Feed/maven/v1") }
        maven {
            url = uri("https://jitpack.io")
        }
    }
}
// Workaround for AS Iguana https://github.com/gradle/gradle/issues/28407
gradle.startParameter.excludedTaskNames.addAll(listOf(":buildSrc:testClasses"))

rootProject.name = "OpenEdX"
include(":app")
include(":core")
include(":auth")
include(":course")
include(":dashboard")
include(":discovery")
include(":profile")
include(":discussion")
include(":whatsnew")
include(":dates")
include(":downloads")
include(":foundation")
