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
import android.view.KeyEvent
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.song.nafis.nf.TuneLyf.BroadReciver.ExoPlaybackReceiver
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

    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                // 📞 Temporary loss (e.g., phone call)
                if (playerRepository.isPlaying()) {
                    wasPlayingBeforeLoss = true
                    playerRepository.playPause() // pause
                    Timber.d("🎧 Audio focus lost temporarily, pausing")
                }
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                // ✅ Regained focus
                if (wasPlayingBeforeLoss) {
                    playerRepository.playPause() // resume
                    wasPlayingBeforeLoss = false
                    Timber.d("🎧 Audio focus regained, resuming")
                }
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                // ❌ Permanent loss (e.g., YouTube or another app)
                playerRepository.playPause()
                wasPlayingBeforeLoss = false
                Timber.d("🎧 Audio focus lost permanently, pausing")
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // 🔉 Optionally duck volume
                Timber.d("🎧 Audio focus lost temporarily (can duck), lowering volume")
            }
        }
    }


    override fun onCreate() {
        super.onCreate()

        media()
        createNotificationChannel()
        observePlayback()
        requestAudioFocus()
    }

    private fun media() {
        mediaSession = MediaSessionCompat(this, "TuneLyfSession")
        // ⬆️ This line is crucial for collapsed button behavior
        mediaSession.setMediaButtonReceiver(
            PendingIntent.getBroadcast(
                this,
                0,
                Intent(Intent.ACTION_MEDIA_BUTTON).setClass(this, MediaButtonReceiver::class.java),
                PendingIntent.FLAG_IMMUTABLE
            )
        )

        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onPlay() {
                Timber.e("onPlay Triggered")
                playerRepository.playPause()
            }

            override fun onPause() {
                Timber.e("onPause Triggered")
                playerRepository.playPause()
            }

            override fun onSkipToNext() {
                Timber.e("onSkipToNext Triggered")
                playerRepository.nextSong()
            }

            override fun onSkipToPrevious() {
                Timber.e("onSkipToPrevious Triggered")
                playerRepository.previousSong()
            }
        })

        mediaSession.isActive = true
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.e("Service Started")

        MediaButtonReceiver.handleIntent(mediaSession, intent)

        when (intent?.action) {
            ACTION_PLAY_PAUSE -> playerRepository.playPause()
            ACTION_NEXT -> playerRepository.nextSong()
            ACTION_PREV -> playerRepository.previousSong()
            ACTION_EXIT -> stopAndExit()
        }
        showNotification()
        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)

        Timber.e("🚫 App removed from recent — stopping music")

        playerRepository.stopCurrentSong()

        stopForeground(true)
        stopSelf()
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
        val flag = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

        val playPauseIntent = PendingIntent.getBroadcast(
            this, 1, Intent(ACTION_PLAY_PAUSE).apply {
                component = ComponentName(this@MusicServiceOnline, ExoPlaybackReceiver::class.java)
            }, flag
        )

        val nextIntent = PendingIntent.getBroadcast(
            this, 2, Intent(ACTION_NEXT).apply {
                component = ComponentName(this@MusicServiceOnline, ExoPlaybackReceiver::class.java)
            }, flag
        )

        val prevIntent = PendingIntent.getBroadcast(
            this, 3, Intent(ACTION_PREV).apply {
                component = ComponentName(this@MusicServiceOnline, ExoPlaybackReceiver::class.java)
            }, flag
        )

        val exitIntent = PendingIntent.getBroadcast(
            this, 4, Intent(ACTION_EXIT).apply {
                component = ComponentName(this@MusicServiceOnline, ExoPlaybackReceiver::class.java)
            }, flag
        )

        mediaSession.setMetadata(
            MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                .putLong(
                    MediaMetadataCompat.METADATA_KEY_DURATION,
                    playerRepository.currentDurationMillis.value ?: 0
                )
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
                    playerRepository.currentPositionMillis.value ?: 0,
                    1.0f
                )
                .build()
        )

        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onSeekTo(pos: Long) {
                playerRepository.seekTo(pos)
            }
        })



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

            val progress = playerRepository.currentPositionMillis.value?.toInt() ?: 0
            val duration = playerRepository.currentDurationMillis.value?.toInt() ?: 100

            val notification = NotificationCompat.Builder(this@MusicServiceOnline, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(artist)
                .setSmallIcon(R.drawable.music_img)
                .setLargeIcon(largeIcon)
                .addAction(R.drawable.previous_notification, "Prev", prevIntent)
                .addAction(playPauseIcon, "PlayPause", playPauseIntent)
                .addAction(R.drawable.nextplay_notification, "Next", nextIntent)
                .addAction(R.drawable.baseline_exit_to_app_24, "Exit", exitIntent)
                .setStyle(MediaStyle().setMediaSession(mediaSession.sessionToken).setShowActionsInCompactView(0, 1, 2))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setProgress(duration, progress, false)
                .build()

            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun stopAndExit() {
        playerRepository.stopCurrentSong()
        stopForeground(true)
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "TuneLyf Playback", NotificationManager.IMPORTANCE_HIGH)
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

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

    override fun onDestroy() {

        playerRepository.releasePlayer()
        mediaSession.release()
        notificationScope.cancel()
        focusRequest?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioManager?.abandonAudioFocusRequest(it)
            }
        }
        super.onDestroy()
    }
}