plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services") // Required for Firebase
    id ("kotlin-parcelize")
}

android {
    namespace = "com.song.nafis.nf.TuneLyf"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.song.nafis.nf.TuneLyf"
        minSdk = 24
        targetSdk = 35
        versionCode = 8
        versionName = "1.8"

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
        viewBinding = true
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")

    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.9.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.9.0")

    // Activity/Fragment
    implementation("androidx.activity:activity-ktx:1.10.1")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.0")

    // Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))

    // Firebase Auth, Firestore, Storage
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation ("com.google.firebase:firebase-appcheck-playintegrity:18.0.0")
    implementation("com.google.firebase:firebase-database-ktx")


    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:21.3.0")

    // Android Credentials API
    implementation("androidx.credentials:credentials:1.5.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Dagger - Hilt

    implementation("androidx.activity:activity:1.10.1")
    implementation("com.google.dagger:hilt-android:2.56.2")
    implementation("androidx.navigation:navigation-fragment:2.9.0")
    implementation("androidx.navigation:navigation-ui:2.9.0")
    implementation("androidx.hilt:hilt-common:1.3.0")
    implementation("androidx.work:work-runtime-ktx:2.11.0")
    ksp("com.google.dagger:hilt-android-compiler:2.56.2")

    // Replace old lifecycle-viewmodel
    implementation("androidx.hilt:hilt-navigation-fragment:1.2.0")
    ksp("androidx.hilt:hilt-compiler:1.2.0")  // Changed from kapt to ksp
    implementation("androidx.hilt:hilt-work:1.3.0")


    // Glide (image loading)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    ksp("com.github.bumptech.glide:compiler:4.16.0")  // Changed from kapt to ksp

    // UI Enhancements
    implementation("com.intuit.sdp:sdp-android:1.1.1")
    implementation("com.intuit.ssp:ssp-android:1.1.1")
    implementation("com.airbnb.android:lottie:3.4.0")
    implementation("nl.psdcompany:duo-navigation-drawer:3.0.0")

    // Media and JSON
    implementation("androidx.media:media:1.7.0")
    implementation("com.google.code.gson:gson:2.11.0")

    // Logging
    implementation("com.jakewharton.timber:timber:5.0.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    // Circular Image
    implementation ("de.hdodenhof:circleimageview:3.1.0")

    // Shimmer for Skeleton
    implementation ("com.facebook.shimmer:shimmer:0.5.0")

    // Retrofit
    implementation ("com.squareup.retrofit2:retrofit:2.11.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.11.0")

    // ExoPlayer
    implementation ("androidx.media3:media3-exoplayer:1.7.1")
    implementation ("androidx.media3:media3-ui:1.7.1")

    // Logging Interceptor
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Room
    implementation("androidx.room:room-runtime:2.7.1")
    ksp ("androidx.room:room-compiler:2.7.1")  // Changed from kapt to ksp

    // lotti animation
    implementation("com.airbnb.android:lottie:6.0.0")


    // Add this line if missing
    implementation ("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")



// Hilt + WorkManager integration

}
