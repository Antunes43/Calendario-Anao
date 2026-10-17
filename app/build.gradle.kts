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
        versionCode = 2
        versionName = "0.2"
    }
}
