package com.song.nafis.nf.TuneLyf.Service

import android.app.*
import android.content.*
import android.graphics.BitmapFactory
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.song.nafis.nf.TuneLyf.ApplicationClass
import com.song.nafis.nf.TuneLyf.R
import kotlinx.coroutines.*
import java.net.URL

class MusicServiceOnline : Service() {

    companion object {
        const val CHANNEL_ID = "MusicPlaybackChannel"
        const val NOTIFICATION_ID = 101
        var isServiceStopped = false

        const val ACTION_PLAY_PAUSE = "com.song.nafis.nf.blissfulvibes.ACTION_PLAY_PAUSE"
        const val ACTION_NEXT = "com.song.nafis.nf.blissfulvibes.EXOPLAYER_ACTION_NEXT"
        const val ACTION_PREV = "com.song.nafis.nf.blissfulvibes.EXOPLAYER_ACTION_PREV"
        const val ACTION_EXIT = "com.song.nafis.nf.blissfulvibes.EXOPLAYER_ACTION_EXIT"
    }

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var controlReceiver: BroadcastReceiver

    private lateinit var audioManager: AudioManager
    private var focusRequest: AudioFocusRequest? = null


    val exoPlayer: ExoPlayer
        get() = (applicationContext as ApplicationClass).exoPlayer

    private var isPlaying = false
    private var songTitle = "Unknown"
    private var songArtist = "Unknown"
    private var songImage: String? = null
    private var progressUpdateJob: Job? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        mediaSession = MediaSessionCompat(this, "BlissfulVibesSession").apply {
            isActive = true
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    togglePlayPause()
                }

                override fun onPause() {
                    togglePlayPause()
                }

                override fun onSkipToNext() {
                    handleNext()
                }

                override fun onSkipToPrevious() {
                    handlePrev()
                }

                override fun onStop() {
                    stopAndExit()
                }

                override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
                    togglePlayPause()
                    return super.onMediaButtonEvent(mediaButtonEvent)
                }

                override fun onSeekTo(pos: Long) {
                    exoPlayer.seekTo(pos)
                }
            })
        }

        createNotificationChannel()
        registerControlReceiver()



        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isActuallyPlaying: Boolean) {
                isPlaying = isActuallyPlaying
                showMediaNotification()
                sendBroadcast(Intent("MUSIC_PLAYBACK_STATE_CHANGED").putExtra("isPlaying", isPlaying))
            }
        })


        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = android.media.AudioAttributes.Builder()
                .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(audioAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener { focusChange ->
                    when (focusChange) {
                        AudioManager.AUDIOFOCUS_LOSS -> {
                            exoPlayer.pause() // lost focus, pause playback
                        }
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                            exoPlayer.pause() // temp pause
                        }
                        AudioManager.AUDIOFOCUS_GAIN -> {
                            exoPlayer.play() // resume
                        }
                    }
                }
                .build()

            val result = audioManager.requestAudioFocus(focusRequest!!)
            if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                Log.w("MusicService", "Audio focus not granted!")
            }
        } else {
            @Suppress("DEPRECATION")
            val result = audioManager.requestAudioFocus(
                { focusChange ->
                    when (focusChange) {
                        AudioManager.AUDIOFOCUS_LOSS -> exoPlayer.pause()
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> exoPlayer.pause()
                        AudioManager.AUDIOFOCUS_GAIN -> exoPlayer.play()
                    }
                },
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
            if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                Log.w("MusicService", "Audio focus not granted (pre-O)!")
            }
        }

    }

    private fun registerControlReceiver() {
        controlReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d("MusicService", "Received broadcast: ${intent?.action}")

                when (intent?.action) {
                    ACTION_NEXT -> handleNext()
                    ACTION_PREV -> handlePrev()
                    ACTION_EXIT -> stopAndExit()
                    ACTION_PLAY_PAUSE -> togglePlayPause()
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(ACTION_NEXT)
            addAction(ACTION_PREV)
            addAction(ACTION_PLAY_PAUSE)
            addAction(ACTION_EXIT)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(controlReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(controlReceiver, filter)
        }
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isServiceStopped = false // ✅ Reset when service restarts
        intent?.getStringExtra("title")?.let { songTitle = it }
        intent?.getStringExtra("artist")?.let { songArtist = it }
        intent?.getStringExtra("image")?.let { songImage = it }

        showMediaNotification()

        intent?.getStringExtra("url")?.let { url ->
            exoPlayer.stop()
            exoPlayer.clearMediaItems()

            val mediaItem = androidx.media3.common.MediaItem.fromUri(url)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.play()
        }


        when (intent?.action) {
            ACTION_PLAY_PAUSE -> togglePlayPause()
            ACTION_NEXT -> handleNext()
            ACTION_PREV -> handlePrev()
            ACTION_EXIT -> stopAndExit()
        }

        return START_STICKY
    }

    private fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
            stopProgressUpdater()
        } else {
            exoPlayer.play()
            startProgressUpdater()
        }
        isPlaying = exoPlayer.isPlaying
        sendBroadcast(Intent(ACTION_PLAY_PAUSE).putExtra("isPlaying", isPlaying))
        showMediaNotification()
    }

    private fun handleNext() {
        exoPlayer.seekToNextMediaItem()
        // Add this to update artwork/title in notification:
        val newIntent = Intent(this, MusicServiceOnline::class.java).apply {
            putExtra("title", songTitle) // update to next song's title
            putExtra("artist", songArtist)
            putExtra("image", songImage)
        }
        onStartCommand(newIntent, 0, 0)
    }


    private fun handlePrev() {
        exoPlayer.seekToPreviousMediaItem()
        sendBroadcast(Intent("MUSIC_PLAYBACK_STATE_CHANGED").putExtra("isPlaying", true))
        showMediaNotification()
    }

    private fun stopAndExit() {
        isServiceStopped = true  // ✅ Set flag

        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        stopForeground(true)
        stopSelf()
//        sendBroadcast(Intent(ACTION_EXIT).setPackage(packageName))
        sendBroadcast(Intent("MUSIC_PLAYBACK_STATE_CHANGED").putExtra("isPlaying", false))
    }

    private fun getNotificationProgress(): Pair<Int, Int> {
        val duration = exoPlayer.duration
        val position = exoPlayer.currentPosition
        return if (duration > 0) Pair(position.toInt(), duration.toInt()) else Pair(0, 0)
    }

    private fun startProgressUpdater() {
        progressUpdateJob?.cancel()
        progressUpdateJob = CoroutineScope(Dispatchers.Main).launch {
            while (isPlaying) {
                sendBroadcast(Intent("MUSIC_PROGRESS_UPDATED").apply {
                    putExtra("progress", exoPlayer.currentPosition.toInt())
                    putExtra("duration", exoPlayer.duration.toInt())
                })
                delay(1000)
            }
        }
    }

    private fun stopProgressUpdater() {
        progressUpdateJob?.cancel()
        progressUpdateJob = null
    }

    private fun showMediaNotification() {
        val playPauseIcon = if (isPlaying) R.drawable.pause_notification else R.drawable.play_notificaiton

        // Use ContextCompat for better compatibility
        val playPauseIntent = PendingIntent.getBroadcast(
            this,
            1,
            Intent(ACTION_PLAY_PAUSE).setPackage(packageName),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val nextIntent = PendingIntent.getBroadcast(
            this,
            2,
            Intent(ACTION_NEXT).setPackage(packageName),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val prevIntent = PendingIntent.getBroadcast(
            this,
            3,
            Intent(ACTION_PREV).setPackage(packageName),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val exitIntent = PendingIntent.getBroadcast(
            this,
            4,
            Intent(ACTION_EXIT).setPackage(packageName),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )


        mediaSession.setMetadata(
            MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, songTitle)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, songArtist)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, exoPlayer.duration)
                .build()
        )


        mediaSession.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or
                            PlaybackStateCompat.ACTION_PAUSE or
                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                            PlaybackStateCompat.ACTION_STOP or
                            PlaybackStateCompat.ACTION_SEEK_TO
                )
                .setState(
                    if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
                    exoPlayer.currentPosition, 1.0f
                )
                .build()
        )


        CoroutineScope(Dispatchers.Main).launch {
            val largeIcon = withContext(Dispatchers.IO) {
                try {
                    songImage?.let {
                        if (it.startsWith("content://")) {
                            // Local image via content URI
                            val inputStream = contentResolver.openInputStream(android.net.Uri.parse(it))
                            BitmapFactory.decodeStream(inputStream)
                        } else if (it.startsWith("http")) {
                            // Online URL image
                            BitmapFactory.decodeStream(URL(it).openStream())
                        } else {
                            BitmapFactory.decodeResource(resources, R.drawable.notification_music_img)
                        }
                    } ?: BitmapFactory.decodeResource(resources, R.drawable.notification_music_img)
                } catch (e: Exception) {
                    BitmapFactory.decodeResource(resources, R.drawable.notification_music_img)
                }
            }


            val (progress, max) = getNotificationProgress()

            val notification = NotificationCompat.Builder(this@MusicServiceOnline, CHANNEL_ID)
                .setContentTitle(songTitle)
                .setContentText(songArtist)
                .setSmallIcon(R.drawable.music_img)
                .setLargeIcon(largeIcon)
                .addAction(R.drawable.previous_notification, "Previous", prevIntent)
                .addAction(playPauseIcon, "Play/Pause", playPauseIntent)
                .addAction(R.drawable.nextplay_notification, "Next", nextIntent)
                .addAction(R.drawable.baseline_exit_to_app_24, "Exit", exitIntent)
                .setStyle(
                    MediaStyle()
                        .setMediaSession(mediaSession.sessionToken)
                        .setShowActionsInCompactView(0, 1, 2) // Show Previous(0), Play/Pause(1), Next(2)
                        .setShowCancelButton(true) // Optional: Adds a cancel button
                )
                .setOnlyAlertOnce(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setProgress(max, progress, false)
                .build()

            startForeground(NOTIFICATION_ID, notification)
        }
    }

    override fun onDestroy() {
        unregisterReceiver(controlReceiver)
        // exoPlayer.release() ❌ hata do
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        mediaSession.release()
        stopForeground(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(null)
        }

        super.onDestroy()
    }


    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Music Playback Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }
}
