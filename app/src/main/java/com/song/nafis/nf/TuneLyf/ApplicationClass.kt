package com.song.nafis.nf.TuneLyf

import android.app.Application
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.song.nafis.nf.TuneLyf.Repository.PlayerRepository
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class ApplicationClass: Application() {

    lateinit var exoPlayer: ExoPlayer
    lateinit var playerRepository: PlayerRepository

    @Inject
    lateinit var injectedRepository: PlayerRepository

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        exoPlayer = ExoPlayer.Builder(this).build()

        // Assign injected repo
        playerRepository = injectedRepository

        FirebaseApp.initializeApp(this)
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )


//        val loadControl = DefaultLoadControl.Builder()
//            .setBufferDurationsMs(
//                5000,   // minBufferMs
//                15000,  // maxBufferMs
//                2500,   // bufferForPlaybackMs
//                5000    // bufferForPlaybackAfterRebufferMs
//            )
//            .build()
//
//        exoPlayer = ExoPlayer.Builder(this)
//            .setLoadControl(loadControl)
//            .build()

    }


}


//companion object {
//    const val CHANNEL_ID = "channel1"
//    const val PLAY = "play"
//    const val NEXT = "next"
//    const val PREVIOUS = "previous"
//    const val EXIT = "exit"
//}

//if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//    val notificationChannel =
//        NotificationChannel(CHANNEL_ID, "Now Playing", NotificationManager.IMPORTANCE_HIGH)
//    notificationChannel.description = "This is Song Notification !!"
//    val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
//    notificationManager.createNotificationChannel(notificationChannel)
//}