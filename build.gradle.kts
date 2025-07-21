buildscript {
    dependencies {
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.50") // No change here
        classpath("com.google.gms:google-services:4.4.1") // For Firebase
    }
}

plugins {
        id("com.android.application") version "8.10.0" apply false
         id("com.google.gms.google-services") version "4.4.2" apply false
        id("org.jetbrains.kotlin.android") version "2.0.0" apply false
        id("com.google.devtools.ksp") version "2.0.0-1.0.21" apply false
    id("com.google.dagger.hilt.android") version "2.56.2" apply false
}
