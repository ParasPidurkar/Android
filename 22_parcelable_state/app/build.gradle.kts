plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // Generates the Parcelable code for classes marked @Parcelize
    id("org.jetbrains.kotlin.plugin.parcelize")
}

android {
    namespace = "com.paras.parcelstate"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.paras.parcelstate"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        // Must match the Kotlin version (1.9.24 -> 1.5.14)
        kotlinCompilerExtensionVersion = "1.5.14"
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.0")
}
