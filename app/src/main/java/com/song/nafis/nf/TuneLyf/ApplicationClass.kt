package com.song.nafis.nf.TuneLyf

import android.app.Application
import androidx.media3.exoplayer.ExoPlayer
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class ApplicationClass : Application() {

    lateinit var exoPlayer: ExoPlayer
        private set   // 🔒 nobody can modify from outside

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())

        // ✅ SINGLE ExoPlayer for entire app
        exoPlayer = ExoPlayer.Builder(this).build()

        FirebaseApp.initializeApp(this)

        FirebaseAppCheck.getInstance()
            .installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance()
            )
    }
}
