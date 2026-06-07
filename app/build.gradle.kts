plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.cattasticpos"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.cattasticpos"
        minSdk = 26
        targetSdk = 34
        versionCode = 10100
        versionName = "1.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
}

dependencies {
    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    debugImplementation(libs.compose.ui.tooling)

    // Lifecycle
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Coroutines
    implementation(libs.coroutines.android)

    // WorkManager
    implementation(libs.work.runtime.ktx)

    // Haze liquid glass blur
    implementation(libs.haze)

    // Lucide thin-stroke icons
    implementation(libs.icons.lucide.android)

    // Compose Unstyled (requires Kotlin 2.3+ KSP — wrappers in ui/components/unstyled until toolchain catches up)
    // implementation(libs.compose.unstyled.theming)
    // implementation(libs.compose.unstyled.button)
    // implementation(libs.compose.unstyled.text.field)
    // Compose Cupertino (KMP artifacts — local adaptive layer in ui/adaptive mirrors API on AndroidX Compose)
    // implementation(libs.compose.cupertino)
    // implementation(libs.compose.cupertino.adaptive)
    // implementation(libs.compose.cupertino.native)
}
