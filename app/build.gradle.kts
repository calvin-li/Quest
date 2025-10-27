plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    namespace = "com.sandbox.calvin_li.quest"
    signingConfigs {
        create("release") {
            keyAlias = "key0"
            keyPassword = "123456"
            // Switch locally between Mac OS and windows
            // storeFile = file("C:/Users/Calvin-PC/Dropbox/Documents/Quest key.jks")
            storeFile = file("/Users/calvinli/Dropbox/Documents/Quest key.jks")
            storePassword = "123456"
        }
    }
    compileSdk = 36
    defaultConfig {
        applicationId = "com.sandbox.calvin_li.quest"
        minSdk = 35
        targetSdk = 36
        versionCode = 11
        versionName = "1.9"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    androidTestImplementation("androidx.test.espresso:espresso-core:3.1.0") {
        exclude(group = "com.android.support", module = "support-annotations")
    }
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit.v412)
    implementation(libs.commons.io)
    implementation(libs.klaxon)
    implementation(libs.staticlog)
    implementation(libs.androidx.fragment.ktx)
}
