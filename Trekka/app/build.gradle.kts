plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "pt.ipt.dama2026.trekka"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "pt.ipt.dama2026.trekka"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    // Room
    implementation(libs.androidx.room.common.jvm)
    implementation(libs.androidx.room.ktx)

    // Lifecycle
//    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
//    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
//    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // Google Location Services
//    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation(libs.play.services.location)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.constraintlayout)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}