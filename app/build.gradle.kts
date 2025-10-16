plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.example.tidenotify"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.tidenotify"
        minSdk = 26          // uses java.time
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release { isMinifyEnabled = false }
        debug {  isMinifyEnabled = false }  // we'll ship debug for easy install
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
}
