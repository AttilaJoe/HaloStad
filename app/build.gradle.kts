plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)

    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.halostad"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.halostad"
        minSdk = 36
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation(libs.androidx.compose.foundation)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Import Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:34.6.0"))

    // Tambahkan library Firebase yang dibutuhkan (tanpa versi)
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth") // Untuk Login/Register
    implementation("com.google.firebase:firebase-firestore") // Untuk Database

    // Dependensi Google Sign In (Wajib untuk fitur Login Google)
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    // 1. Library Perhitungan Waktu Sholat (Adhan)
    implementation("com.batoulapps.adhan:adhan2:0.0.6")

    // Library Date-Time versi terbaru
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")

    // 2. Library untuk mengambil Lokasi GPS (Google Play Services)
    implementation("com.google.android.gms:play-services-location:21.2.0")

    // 3. Accompanist Permission (Agar minta izin lokasi di Compose lebih mudah)
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")

    // 2. Coil (Untuk menampilkan gambar dari URL)
    implementation("io.coil-kt:coil-compose:2.6.0")

    implementation("androidx.compose.material:material-icons-extended-android:1.7.8") // Cek versi terbaru jika perlu

    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // CameraX
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.extensions)

}