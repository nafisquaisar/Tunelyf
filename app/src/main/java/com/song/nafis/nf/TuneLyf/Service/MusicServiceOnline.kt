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
import com.song.nafis.nf.TuneLyf.BroadReciver.PlaybackControlHolder
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
        // Notification channel ID (Android 8+ ke liye mandatory)
        const val CHANNEL_ID = "MusicPlaybackChannel"

        // Notification ka fixed ID (same ID use hota hai update ke liye)
        const val NOTIFICATION_ID = 101

        // Flag to know service manually stop hui ya nahi
        var isServiceStopped: Boolean = false

        // Notification action constants (button clicks ke liye)
        const val ACTION_PLAY_PAUSE = "com.song.nafis.nf.TuneLyf.ACTION_PLAY_PAUSE"
        const val ACTION_NEXT = "com.song.nafis.nf.TuneLyf.ACTION_NEXT"
        const val ACTION_PREV = "com.song.nafis.nf.TuneLyf.ACTION_PREV"
        const val ACTION_EXIT = "com.song.nafis.nf.TuneLyf.ACTION_EXIT"
    }

    // ExoPlayer + playback state ka single source
    @Inject lateinit var playerRepository: PlayerRepository

    // MediaSession = system (lockscreen, BT, headset) se controls handle karta hai
    private lateinit var mediaSession: MediaSessionCompat

    // Audio focus ke liye (call aane pe pause, etc.)
    private var audioManager: AudioManager? = null
    private var focusRequest: AudioFocusRequest? = null

    // Notification image loading async ke liye
    private val notificationScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Audio focus loss ke baad resume karne ke liye
    private var wasPlayingBeforeLoss = false

    // Important flag → foreground already start hua ya nahi
    private var isForegroundStarted = false

    /**
     * Service lifecycle ka entry point
     * Yahin sab one-time setup hota hai
     */
    override fun onCreate() {
        super.onCreate()

        // Android 8+ ke liye notification channel
        createNotificationChannel()

        // MediaSession initialize (lockscreen + headset controls)
        initializeMediaSession()

        // PlayerRepository ke state ko observe karna
        observePlayback()

        // Audio focus request (call / other apps handling)
        requestAudioFocus()
    }

    /**
     * MediaSession setup
     * System-level play/pause/next/prev yahin aata hai
     */
    private fun initializeMediaSession() {
        mediaSession = MediaSessionCompat(this, "TuneLyfSession")

        // Media button broadcast ko disable (we handle manually)
        mediaSession.setMediaButtonReceiver(null)

        // System se aane wale playback commands
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

        // MediaSession active karna zaroori hai
        mediaSession.isActive = true
    }

    /**
     * Service ko start / command receive hota hai yahin
     * Notification actions bhi yahin handle hote hain
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // Media button intents (BT / headset) handle
        MediaButtonReceiver.handleIntent(mediaSession, intent)

        isServiceStopped = false

        /**
         * 🔥 MOST IMPORTANT PART
         * Service start hote hi foreground lena
         * isPlaying ka wait nahi karte
         */
        if (!isForegroundStarted) {
            startForeground(
                NOTIFICATION_ID,
                buildInitialNotification()
            )
            isForegroundStarted = true
        }

        // Notification buttons ke actions
        when (intent?.action) {
            ACTION_PLAY_PAUSE -> playerRepository.playPause()
            ACTION_NEXT -> playerRepository.nextSong()
            ACTION_PREV -> playerRepository.previousSong()
            ACTION_EXIT -> stopAndExit()
        }

        return START_NOT_STICKY
    }

    /**
     * PlayerRepository ke state changes observe karna
     * Yahin se notification update hoti hai
     */
    private fun observePlayback() {

        // Play / Pause change hua → notification update
        playerRepository.isPlaying.observeForever {
            if (isForegroundStarted) showNotification()
        }

        // Song change hua → notification update
        playerRepository.currentSong.observeForever {
            if (isForegroundStarted) showNotification()
        }
    }

    /**
     * Initial dummy notification
     * Sirf foreground rule satisfy karne ke liye
     */
    private fun buildInitialNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Playing music")
            .setContentText("TuneLyf")
            .setSmallIcon(R.drawable.music_img)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .build()
    }

    /**
     * MAIN notification builder
     * Song info, buttons, progress bar – sab yahin
     */
    private fun showNotification() {

        // Current playing song
        val song = playerRepository.getCurrentSong()

        val title = song?.musicTitle ?: "Unknown Title"
        val artist = song?.musicArtist ?: "Unknown Artist"
        val artworkUrl = song?.imgUri

        val isPlaying = playerRepository.isPlaying.value == true

        // Play / Pause icon dynamically change
        val playPauseIcon =
            if (isPlaying) R.drawable.pause_notification
            else R.drawable.play_notificaiton

        // PendingIntents → notification button clicks
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

        // Playlist & current index (notification click pe activity open ke liye)
        val playlist = playerRepository.getPlaylist()
        val currentIndex = playlist.indexOfFirst { it.musicId == song?.musicId }

        val contentIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, PlayMusicStreamActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("SONG_TITLE", song?.musicTitle)
                putExtra("SONG_TRACK", song?.imgUri)
                putParcelableArrayListExtra("SONG_LIST", ArrayList(playlist))
                putExtra("SONG_INDEX", currentIndex)
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        /**
         * MediaSession playback state update
         * Lock screen & BT devices ke liye
         */
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
                    if (isPlaying)
                        PlaybackStateCompat.STATE_PLAYING
                    else
                        PlaybackStateCompat.STATE_PAUSED,
                    playerRepository.currentPositionMillis.value ?: 0,
                    1.0f
                )
                .build()
        )

        // Metadata (song info)
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

        /**
         * Artwork loading background thread me
         */
        notificationScope.launch {

            val largeIcon = withContext(Dispatchers.IO) {
                try {
                    artworkUrl?.let {
                        when {
                            it.startsWith("content://") -> {
                                val inputStream =
                                    contentResolver.openInputStream(Uri.parse(it))
                                BitmapFactory.decodeStream(inputStream)
                            }
                            it.startsWith("http") -> {
                                BitmapFactory.decodeStream(URL(it).openStream())
                            }
                            else -> BitmapFactory.decodeResource(
                                resources,
                                R.drawable.notification_music_img
                            )
                        }
                    } ?: BitmapFactory.decodeResource(
                        resources,
                        R.drawable.notification_music_img
                    )
                } catch (e: Exception) {
                    BitmapFactory.decodeResource(
                        resources,
                        R.drawable.notification_music_img
                    )
                }
            }

            val currentPosition =
                playerRepository.currentPositionMillis.value?.toInt() ?: 0
            val totalDuration =
                playerRepository.currentDurationMillis.value?.toInt() ?: 100

            val notification =
                NotificationCompat.Builder(this@MusicServiceOnline, CHANNEL_ID)
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
                            .setShowActionsInCompactView(0, 1, 2)
                    )
                    .setProgress(totalDuration, currentPosition, false)
                    .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setOnlyAlertOnce(true)
                    .setOngoing(isPlaying)
                    .setContentIntent(contentIntent)
                    .build()

            // Foreground notification update
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    /**
     * Exit button pressed
     * Playback + service dono band
     */
    private fun stopAndExit() {
        stopForeground(true)
        playerRepository.stopCurrentSong()
        isServiceStopped = true
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * Audio focus handling (calls, other apps)
     */
    private fun requestAudioFocus() {
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes =
                android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()

            focusRequest =
                AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
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

    /**
     * Audio focus callbacks
     */
    private val audioFocusChangeListener =
        AudioManager.OnAudioFocusChangeListener { focusChange ->

            when (focusChange) {

                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    if (playerRepository.isPlaying.value == true) {
                        wasPlayingBeforeLoss = true
                        CoroutineScope(Dispatchers.Main).launch {
                            playerRepository.playPause()
                        }
                    }
                }

                AudioManager.AUDIOFOCUS_LOSS -> {
                    CoroutineScope(Dispatchers.Main).launch {
                        playerRepository.playPause()
                    }
                    wasPlayingBeforeLoss = false
                }

                AudioManager.AUDIOFOCUS_GAIN -> {
                    if (wasPlayingBeforeLoss) {
                        CoroutineScope(Dispatchers.Main).launch {
                            playerRepository.playPause()
                        }
                        wasPlayingBeforeLoss = false
                    }
                }
            }
        }

    /**
     * Notification channel (Android 8+)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "TuneLyf Playback",
                NotificationManager.IMPORTANCE_HIGH
            )
            getSystemService(NotificationManager::class.java)
                ?.createNotificationChannel(channel)
        }
    }

    /**
     * Service destroy
     */
    override fun onDestroy() {
        mediaSession.release()
        playerRepository.stopCurrentSong()
        notificationScope.cancel()

        focusRequest?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioManager?.abandonAudioFocusRequest(it)
            }
        }

        PlaybackControlHolder.listener = null
        super.onDestroy()
    }
}
