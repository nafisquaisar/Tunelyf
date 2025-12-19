package com.song.nafis.nf.TuneLyf

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.work.*
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.song.nafis.nf.TuneLyf.WorkManager.HomePreloadWorker
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class ApplicationClass : Application(), Configuration.Provider {

    companion object {
        var isColdStart = true
    }

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())


        FirebaseApp.initializeApp(this)

        FirebaseAppCheck.getInstance()
            .installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance()
            )

        val request = OneTimeWorkRequestBuilder<HomePreloadWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(this).enqueueUniqueWork(
            "home_preload",
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    // 🔥 THIS IS THE FIX
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
