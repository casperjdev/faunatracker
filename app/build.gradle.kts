import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.21"
}

val props = gradleLocalProperties(rootDir, providers)
val supabaseUrl: String = props.getProperty("SUPABASE_URL")
val supabaseApiKey: String = props.getProperty("SUPABASE_API_KEY")
val movebankUrl: String = props.getProperty("MOVEBANK_URL")
val movebankToken: String = props.getProperty("MOVEBANK_TOKEN")

android {
    namespace = "com.example.faunatracker"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.faunatracker"
        minSdk = 34
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_API_KEY", "\"$supabaseApiKey\"")
        buildConfigField("String", "MOVEBANK_URL", "\"$movebankUrl\"")
        buildConfigField("String", "MOVEBANK_TOKEN", "\"$movebankToken\"")
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

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.okhttp)
    testImplementation(libs.mockwebserver)
    implementation(libs.logging.interceptor)
    implementation(libs.retrofit)
    implementation(libs.converter.moshi)
    implementation(libs.moshi.kotlin)
    implementation(libs.moshi.kotlin.v1150)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.jackson.dataformat.csv)
    implementation(libs.jackson.module.kotlin)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    testImplementation(kotlin("test"))
    implementation(libs.opencsv)
}