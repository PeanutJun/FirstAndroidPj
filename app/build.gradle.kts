import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.yuguri.me.mypersonalapp"

    compileSdk = 36

    defaultConfig {
        applicationId = "com.yuguri.me.mypersonalapp"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            val localProperties = Properties()
            localProperties.load(localPropertiesFile.inputStream())
            
            buildConfigField("String", "MOOD_FEED", "\"${localProperties.getProperty("MOOD_FEED_KEY", "")}\"")
            buildConfigField("String", "TIANJU", "\"${localProperties.getProperty("TIANJU_KEY", "")}\"")
            buildConfigField("String", "CAR", "\"${localProperties.getProperty("CAR_KEY", "")}\"")
            buildConfigField("String", "NEWS", "\"${localProperties.getProperty("NEWS_KEY", "")}\"")
            buildConfigField("String", "BAIDU_APP_ID", "\"${localProperties.getProperty("BAIDU_APP_ID", "")}\"")
            buildConfigField("String", "BAIDU_AUTH_TOKEN", "\"${localProperties.getProperty("BAIDU_AUTH_TOKEN", "")}\"")
            buildConfigField("String", "AMAP_KEY", "\"${localProperties.getProperty("AMAP_KEY", "")}\"")
            buildConfigField("String", "USER_BASE_URL", "\"${localProperties.getProperty("USER_BASE_URL", "")}\"")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.txt")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.coil.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.gson)

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
