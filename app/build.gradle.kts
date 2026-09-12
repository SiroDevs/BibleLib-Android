import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.dagger.hilt)
    alias(libs.plugins.devtools.ksp)
    alias(libs.plugins.kotlin.serialization)
    id("kotlin-parcelize")
    alias(libs.plugins.io.sentry)
}

val keystoreProperties = Properties()
val keystorePropertiesFile = rootProject.file("keystore/key.properties")
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(keystorePropertiesFile.inputStream())
}

val localProperties = Properties()
localProperties.load(project.rootProject.file("local.properties").inputStream())

android {
    compileSdk = 37

    defaultConfig {
        applicationId = "com.biblelib"
        versionCode = 24
        versionName = "1.0.24"
        minSdk = 26
        targetSdk = 37

        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            keyAlias = keystoreProperties["KEY_ALIAS"] as String
            keyPassword = keystoreProperties["KEY_PASSWORD"] as String
            storePassword = keystoreProperties["KEYSTORE_PASSWORD"] as String
            storeFile = keystoreProperties["STORE_FILE"]?.let { file(it as String) }
        }
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            isDebuggable = true
        }
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    lint {
        disable += "NullSafeMutableLiveData"
    }

    namespace = "com.biblelib"
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    // Core modules
    implementation(project(":core:common"))
    implementation(project(":core:casting"))
    implementation(project(":core:data"))
    implementation(project(":core:database"))
    implementation(project(":core:design_system"))
    implementation(project(":core:network"))
    implementation(project(":core:ui"))

    // Feature modules
    implementation(project(":feature:selection"))
    implementation(project(":feature:casting"))
    implementation(project(":feature:how_it_works"))
    implementation(project(":feature:reader"))
    implementation(project(":feature:bookmark_notes"))
    implementation(project(":feature:search"))
    implementation(project(":feature:scripture_opener"))
    implementation(project(":feature:history"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:bibles"))
    implementation(project(":feature:help"))
    implementation(project(":feature:donation"))

    // Navigation
    implementation(libs.compose.navigation)
    implementation(libs.compose.hilt.navigation)

    // Activity & lifecycle
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // WorkManager + Hilt
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}


sentry {
    debug.set(true)
    org.set("futuristicken")
    projectName.set("biblelib-android")
    includeSourceContext.set(true)
    authToken.set(localProperties.getProperty("SENTRY_AUTH_TOKEN"))
}
