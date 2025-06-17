import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp") version "1.8.20-1.0.10"
}
fun getSigningProperties(): Properties {
    return Properties().apply {
        val propertiesFile = rootProject.file("sign.properties")
        if (propertiesFile.exists()) {
            load(FileInputStream(propertiesFile))
        }
    }
}


android {
    signingConfigs {
        getByName("debug") {
            storeFile = rootProject.file("keystore/testkey.p12")
            storePassword = "android"
            keyAlias = "androidtestkey"
            keyPassword = "android"
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
        }
        create("release") {
            val props = getSigningProperties()
            val keystorePath = props.getProperty("signing.storeFile")
            val keystoreFile = keystorePath?.let { rootProject.file(it) }
            if (keystoreFile?.exists() == true) {
                storeFile = keystoreFile
                storePassword = props.getProperty("signing.storePassword", "")
                keyAlias = props.getProperty("signing.keyAlias", "")
                keyPassword = props.getProperty("signing.keyPassword", "")
                enableV1Signing = false
                enableV2Signing = false
                enableV3Signing = true
            } else {
                initWith(getByName("debug"))
                println("\u001B[33m[Warning]: Release signing config not found, falling back to debug signing\u001B[0m")
            }
        }
    }

    compileSdk = 33

    defaultConfig {
        val gitCount: String = providers.exec {
            commandLine("git", "rev-list", "HEAD", "--count")
        }.standardOutput.asText.get().trim()

        val gitHash: String = providers.exec {
            commandLine("git", "rev-list", "HEAD", "--abbrev-commit", "--max-count=1")
        }.standardOutput.asText.get().trim()

        applicationId = "cn.wankkoree.xp.webviewpp"
        minSdk = 27
        targetSdk = 33
        versionCode = gitCount.toInt()
        versionName = "v3.1.0.$gitHash"
    }

    buildTypes {
        release {
            isDebuggable = false
            isJniDebuggable = false
            isRenderscriptDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        create("dev") {
            isDebuggable = false
            isJniDebuggable = false
            isRenderscriptDebuggable = false
            isMinifyEnabled = false
            isShrinkResources = false
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            isDebuggable = true
            isJniDebuggable = true
            isRenderscriptDebuggable = false
            isMinifyEnabled = false
            isShrinkResources = false
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
    }

    namespace = "cn.wankkoree.xp.webviewpp"

//    applicationVariants.configureEach { variant ->
//        variant.outputs.configureEach {
//            outputFileName =
//                "${defaultConfig.applicationId}-${defaultConfig.versionName}_${defaultConfig.versionCode}-${variant.buildType.name}.apk"
//        }
//    }
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    val yukiHookApiVersion = "1.1.11"
    val appCenterSdkVersion = "4.4.5"

    // Xposed Hook
    compileOnly("de.robv.android.xposed:api:82")                                                     // Xposed
    implementation("com.highcapable.yukihookapi:api:$yukiHookApiVersion")                            // Yuki Hook API
    ksp("com.highcapable.yukihookapi:ksp-xposed:$yukiHookApiVersion")                                // Yuki Hook API - KSP

    // 语言特性支持
    implementation("androidx.core:core-ktx:1.9.0")                                                   // Jetpack
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")                                 // Jetpack - Lifecycle
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")                         // Kotlin - Coroutines Android

    // 界面支持
    implementation("androidx.appcompat:appcompat:1.6.0")                                             // Jetpack - AppCompat
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")                               // Jetpack - Constraint Layout
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")                           // Jetpack - Swipe Refresh Layout
    implementation("androidx.gridlayout:gridlayout:1.0.0")                                           // Jetpack - Grid Layout
    implementation("com.google.android.material:material:1.7.0")                                     // Material Design
    implementation("com.google.android.flexbox:flexbox:3.0.0")                                       // Flexbox
    implementation("io.noties.markwon:core:4.6.2")                                                   // Markdown

    // 功能支持
    implementation("androidx.palette:palette-ktx:1.0.0")                                             // Jetpack - Palette
    implementation("com.github.kittinunf.fuel:fuel:2.3.1")                                           // Fuel
    implementation("com.github.kittinunf.fuel:fuel-android:2.3.1")                                   // Fuel - Android
    implementation("com.github.kittinunf.fuel:fuel-gson:2.3.1")                                      // Fuel - Gson
    implementation("com.google.code.gson:gson:2.9.0")                                                // Gson
    implementation("com.microsoft.appcenter:appcenter-analytics:${appCenterSdkVersion}")             // App Center
    implementation("com.microsoft.appcenter:appcenter-crashes:${appCenterSdkVersion}")               // App Center
}