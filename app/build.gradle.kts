plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.dwarrow.calendar"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.dwarrow.calendar"
        minSdk = 26
        targetSdk = 35
        versionCode = 10
        versionName = "0.4.6-dev"
    }
    signingConfigs {
        create("persistent") {
            val storePath = System.getenv("CALENDAR_KEYSTORE_PATH")
            val password = System.getenv("CALENDAR_KEYSTORE_PASSWORD")
            if (!storePath.isNullOrBlank() && !password.isNullOrBlank()) {
                storeFile = file(storePath)
                storePassword = password
                keyAlias = "calendario-anao"
                keyPassword = password
            }
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("persistent")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}
