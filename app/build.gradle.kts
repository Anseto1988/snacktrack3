plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
}

android {
    namespace = "com.example.snacktrack"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.snacktrack"
        minSdk = 27
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        getByName("release") {
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

// Konfiguration für Abhängigkeitsauflösung
configurations.all {
    resolutionStrategy {
        force("com.squareup.okhttp3:okhttp:4.12.0")
        force("com.squareup.okhttp3:okhttp-bom:4.12.0")
    }
}

dependencies {
    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    
    // Edge-to-Edge Support
    implementation("androidx.core:core-ktx:1.16.0")
    
    // Jetpack Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    
    // Material Icons Extended
    implementation("androidx.compose.material:material-icons-extended")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")
    
    // OkHttp BOM - explizit hinzugefügt um Konflikte zu vermeiden
    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.12.0"))
    
    // Appwrite SDK
    implementation("io.appwrite:sdk-for-android:4.0.1") {
        exclude(group = "com.squareup.okhttp3", module = "okhttp-bom")
    }
    
    // OkHttp-Abhängigkeiten explizit hinzufügen
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    // ML Kit für Barcode-Scanning
    val cameraxVersion = "1.3.1"
    implementation("com.google.mlkit:barcode-scanning:17.2.0")
    implementation("androidx.camera:camera-core:${cameraxVersion}")
    implementation("androidx.camera:camera-camera2:${cameraxVersion}")
    implementation("androidx.camera:camera-lifecycle:${cameraxVersion}")
    implementation("androidx.camera:camera-view:${cameraxVersion}")
    
    // Room für Offline-Cache
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    
    // WorkManager für Hintergrundaufgaben
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    
    // Diagramme und Visualisierungen - Alternative zu MPAndroidChart
    implementation("co.yml:ycharts:2.1.0")
    
    // Coil für Bildladung
    implementation("io.coil-kt:coil-compose:2.5.0")
    
    // Datastore für Einstellungen
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}