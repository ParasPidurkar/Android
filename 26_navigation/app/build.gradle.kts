plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // Needed for type-safe routes: every route is a @Serializable object or class
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.paras.navigation"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.paras.navigation"
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
    implementation("androidx.compose.material:material-icons-core")

    // NavHost, NavController, composable<Route>() ... (2.8+ has type-safe routes)
    implementation("androidx.navigation:navigation-compose:2.8.0")
    // Runtime for @Serializable, used by the route classes
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.0")
}
