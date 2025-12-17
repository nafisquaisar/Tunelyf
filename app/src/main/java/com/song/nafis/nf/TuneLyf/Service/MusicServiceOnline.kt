package com.song.nafis.nf.TuneLyf.Service

import android.app.*
import android.content.*
import android.graphics.BitmapFactory
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.media.session.MediaButtonReceiver
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media3.common.util.UnstableApi
import com.song.nafis.nf.TuneLyf.Activity.PlayMusicStreamActivity
import com.song.nafis.nf.TuneLyf.Player.PlayerManager
import com.song.nafis.nf.TuneLyf.R
import com.song.nafis.nf.TuneLyf.Repository.PlayerRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import timber.log.Timber
import java.net.URL
import javax.inject.Inject


@AndroidEntryPoint
class MusicServiceOnline : Service() {

    companion object {
        const val CHANNEL_ID = "MusicPlaybackChannel"
        const val NOTIFICATION_ID = 101
        var isServiceStopped: Boolean = false

        const val ACTION_PLAY_PAUSE = "com.song.nafis.nf.TuneLyf.ACTION_PLAY_PAUSE"
        const val ACTION_NEXT = "com.song.nafis.nf.TuneLyf.ACTION_NEXT"
        const val ACTION_PREV = "com.song.nafis.nf.TuneLyf.ACTION_PREV"
        const val ACTION_EXIT = "com.song.nafis.nf.TuneLyf.ACTION_EXIT"
    }

    @Inject lateinit var playerRepository: PlayerRepository

    private lateinit var mediaSession: MediaSessionCompat
    private var audioManager: AudioManager? = null
    private var focusRequest: AudioFocusRequest? = null
    private val notificationScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var wasPlayingBeforeLoss = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        initializeMediaSession()
        observePlayback()
        requestAudioFocus()
    }

    private fun initializeMediaSession() {
        mediaSession = MediaSessionCompat(this, "TuneLyfSession")
        mediaSession.setMediaButtonReceiver(null)

        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onPlay() {
                playerRepository.playPause()
                showNotification()
            }

            override fun onPause() {
                playerRepository.playPause()
                showNotification()
            }

            override fun onSkipToNext() {
                playerRepository.nextSong()
                showNotification()
            }

            override fun onSkipToPrevious() {
                playerRepository.previousSong()
                showNotification()
            }

            override fun onSeekTo(pos: Long) {
                playerRepository.seekTo(pos)
                showNotification()
            }
        })

        mediaSession.isActive = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        isServiceStopped = false

        when (intent?.action) {
            ACTION_PLAY_PAUSE -> playerRepository.playPause()
            ACTION_NEXT -> playerRepository.nextSong()
            ACTION_PREV -> playerRepository.previousSong()
            ACTION_EXIT -> stopAndExit()
        }

        showNotification()
        return START_NOT_STICKY
    }

    private fun observePlayback() {
        playerRepository.isPlaying.observeForever { showNotification() }
        playerRepository.currentSong.observeForever { showNotification() }
        playerRepository.currentPositionMillis.observeForever { showNotification() }
        playerRepository.currentDurationMillis.observeForever { showNotification() }
    }

    private fun showNotification() {
        val song = playerRepository.getCurrentSong()
        val title = song?.musicTitle ?: "Unknown Title"
        val artist = song?.musicArtist ?: "Unknown Artist"
        val artworkUrl = song?.imgUri
        val isPlaying = playerRepository.isPlaying.value == true

        val playPauseIcon = if (isPlaying) R.drawable.pause_notification else R.drawable.play_notificaiton

        val playPauseIntent = PendingIntent.getService(
            this, 1,
            Intent(this, this::class.java).apply { action = ACTION_PLAY_PAUSE },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextIntent = PendingIntent.getService(
            this, 2,
            Intent(this, this::class.java).apply { action = ACTION_NEXT },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val prevIntent = PendingIntent.getService(
            this, 3,
            Intent(this, this::class.java).apply { action = ACTION_PREV },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val exitIntent = PendingIntent.getService(
            this, 4,
            Intent(this, this::class.java).apply { action = ACTION_EXIT },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playlist = playerRepository.getPlaylist() // ⚠️ create a getter if it's private
        val currentIndex = playlist.indexOfFirst { it.musicId == song?.musicId }

        val contentIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, PlayMusicStreamActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("SONG_TITLE", song?.musicTitle)
                putExtra("SONG_TRACK", song?.imgUri)
                putParcelableArrayListExtra("SONG_LIST", ArrayList(playlist))  // needs to be Parcelable
                putExtra("SONG_INDEX", currentIndex)
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )



        mediaSession.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or
                            PlaybackStateCompat.ACTION_PAUSE or
                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                            PlaybackStateCompat.ACTION_STOP
                )
                .setState(
                    if (isPlaying) PlaybackStateCompat.STATE_PLAYING
                    else PlaybackStateCompat.STATE_PAUSED,
                    playerRepository.currentPositionMillis.value ?: 0,
                    1.0f
                )
                .build()
        )

        mediaSession.setMetadata(
            MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                .putLong(
                    MediaMetadataCompat.METADATA_KEY_DURATION,
                    playerRepository.currentDurationMillis.value ?: 0L
                )
                .build()
        )

        notificationScope.launch {
            val largeIcon = withContext(Dispatchers.IO) {
                try {
                    artworkUrl?.let {
                        if (it.startsWith("content://")) {
                            val inputStream = contentResolver.openInputStream(Uri.parse(it))
                            BitmapFactory.decodeStream(inputStream)
                        } else if (it.startsWith("http")) {
                            BitmapFactory.decodeStream(URL(it).openStream())
                        } else {
                            BitmapFactory.decodeResource(resources, R.drawable.notification_music_img)
                        }
                    } ?: BitmapFactory.decodeResource(resources, R.drawable.notification_music_img)
                } catch (e: Exception) {
                    BitmapFactory.decodeResource(resources, R.drawable.notification_music_img)
                }
            }

            val currentPosition = playerRepository.currentPositionMillis.value?.toInt() ?: 0
            val totalDuration = playerRepository.currentDurationMillis.value?.toInt() ?: 100


            val notification = NotificationCompat.Builder(this@MusicServiceOnline, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(artist)
                .setSmallIcon(R.drawable.music_img)
                .setLargeIcon(largeIcon)
                .addAction(R.drawable.previous_notification, "Prev", prevIntent)
                .addAction(playPauseIcon, "PlayPause", playPauseIntent)
                .addAction(R.drawable.nextplay_notification, "Next", nextIntent)
                .addAction(R.drawable.baseline_exit_to_app_24, "Exit", exitIntent)
                .setStyle(
                    MediaStyle()
                        .setMediaSession(mediaSession.sessionToken)
                        .setShowActionsInCompactView(0, 1, 2)  // Collapsed view
                )
                .setProgress(totalDuration, currentPosition, false) // ✅ PROGRESS BAR 😎
                .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .setOngoing(isPlaying)
                .setContentIntent(contentIntent) // ✅ This handles opening the app when notification is clicked
                .build()

            startForeground(NOTIFICATION_ID, notification)

        }
    }

    private fun stopAndExit() {
//        playerRepository.stopCurrentSong()
        stopForeground(true)
        playerRepository.stopCurrentSong()
        isServiceStopped = true
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun requestAudioFocus() {
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = android.media.AudioAttributes.Builder()
                .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(audioAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(audioFocusChangeListener)
                .build()

            audioManager?.requestAudioFocus(focusRequest!!)
        } else {
            @Suppress("DEPRECATION")
            audioManager?.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
    }

    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                // Call ya temporary interruption
                if (playerRepository.isPlaying()) {
                    wasPlayingBeforeLoss = true
                    playerRepository.playPause()  // ✅ Pause music
                }
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // ⚠️ Optional duck — Hume nahi chahiye
                if (playerRepository.isPlaying()) {
                    wasPlayingBeforeLoss = true
                    playerRepository.playPause()  // ✅ Ducking nahi, direct pause
                }
            }

            AudioManager.AUDIOFOCUS_LOSS -> {
                // Permanent loss → pause
                playerRepository.playPause()
                wasPlayingBeforeLoss = false
            }

            AudioManager.AUDIOFOCUS_GAIN -> {
                // Regain audio focus
                if (wasPlayingBeforeLoss) {
                    playerRepository.playPause()  // ✅ Resume music
                    wasPlayingBeforeLoss = false
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "TuneLyf Playback",
                NotificationManager.IMPORTANCE_HIGH
            )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        // Don't release player here unless app is being fully killed
        // You can release mediaSession (good practice):
        mediaSession.release()
        playerRepository.stopCurrentSong()
        notificationScope.cancel()
        focusRequest?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioManager?.abandonAudioFocusRequest(it)
            }
        }

        super.onDestroy()
    }

}

