package com.song.nafis.nf.TuneLyf.Repository

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.song.nafis.nf.TuneLyf.Api.AudiusApi
import com.song.nafis.nf.TuneLyf.ApplicationClass
import com.song.nafis.nf.TuneLyf.Model.UnifiedMusic
import com.song.nafis.nf.TuneLyf.Service.MusicServiceOnline
import timber.log.Timber
import javax.inject.Singleton
import kotlin.getValue

@Singleton
class PlayerRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audiusApi: AudiusApi
) {

    val currentSong = MutableLiveData<UnifiedMusic?>()
    val isPlaying = MutableLiveData<Boolean>()
    val currentTitle = MutableLiveData<String>()
    val currentArtwork = MutableLiveData<String>()
    val durationFormatted = MutableLiveData<String>()
    val positionFormatted = MutableLiveData<String>()
    val currentPositionMillis = MutableLiveData<Long>()
    val currentDurationMillis = MutableLiveData<Long>()

    // ✅ Shared playlist and index
    private val playlist = mutableListOf<UnifiedMusic>()
    private val _playlistLiveData = MutableLiveData<List<UnifiedMusic>>()
    val playlistLiveData: LiveData<List<UnifiedMusic>> get() = _playlistLiveData
    private var currentIndex = 0

    val exoPlayer: ExoPlayer
        get() = (context.applicationContext as ApplicationClass).exoPlayer

    private var listener: ((position: Long, duration: Long, isPlaying: Boolean) -> Unit)? = null
    private var prepareListener: ((Boolean) -> Unit)? = null
    private var onSongCompleted: (() -> Unit)? = null
    private var bufferingListener: ((Boolean) -> Unit)? = null

    private val audiusRepository: AudiusRepository by lazy {
        AudiusRepository(api = audiusApi)
    }

    // ✅ Shared playlist setter
    fun setPlaylist(songs: List<UnifiedMusic>) {
        playlist.clear()
        playlist.addAll(songs)
        _playlistLiveData.postValue(playlist)

        Timber.d("✅ Global Playlist updated with ${songs.size} songs")

        // Clear ExoPlayer items
        exoPlayer.stop()
        exoPlayer.clearMediaItems()

        // Set all media items
        val mediaItems = playlist.map { MediaItem.fromUri(it.musicPath) }
        exoPlayer.setMediaItems(mediaItems)
        exoPlayer.prepare()
    }


    fun setInitialIndex(index: Int) {
        if (playlist.isEmpty()) {
            Timber.e("⛔ Playlist is empty. Cannot set index.")
            return
        }
        if (index in playlist.indices) {
            currentIndex = index
            exoPlayer.seekTo(index, 0)
            playCurrent()
        } else {
            Timber.e("⛔ Invalid index: $index")
        }
    }


    private fun playCurrent() {
        val song = playlist[currentIndex]

        preparePlayer(song.musicPath, song.musicTitle, song.imgUri, song) { success ->
            if (success) {
                refreshNowPlaying()
            } else {
                Timber.e("❌ Failed to prepare player")
            }
        }
    }

    fun nextSong() {
        if (playlist.isNotEmpty()) {
            currentIndex = (currentIndex + 1) % playlist.size
            playCurrent()
        }
    }

    fun previousSong() {
        if (playlist.isNotEmpty()) {
            currentIndex = if (currentIndex - 1 < 0) playlist.size - 1 else currentIndex - 1
            playCurrent()
        }
    }

    fun refreshNowPlaying() {
        Timber.d("🔁 Current Playlist Size: ${playlist.size}")
        playlist.getOrNull(currentIndex)?.let {
            Timber.d("🎵 Refreshing Now Playing: ${it.musicTitle}")
            currentTitle.postValue(it.musicTitle)
            currentArtwork.postValue(it.imgUri)
            isPlaying.postValue(exoPlayer.isPlaying)
        }
    }

    fun preparePlayer(
        songUrl: String,
        title: String,
        image: String,
        song: UnifiedMusic,
        onReady: (Boolean) -> Unit
    ) {
        prepareListener = onReady

        val mediaItem = MediaItem.fromUri(Uri.parse(songUrl))
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        exoPlayer.setMediaItem(mediaItem, true)

        // 🔥 Shared live data updates
        currentTitle.value = title
        currentArtwork.value = image
        currentSong.value = song

        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_READY -> {
                        prepareListener?.invoke(true)
                        bufferingListener?.invoke(false)
                        prepareListener = null
                    }


                    Player.STATE_BUFFERING -> bufferingListener?.invoke(true)
                    Player.STATE_ENDED -> onSongCompleted?.invoke()
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                prepareListener?.invoke(false)
                prepareListener = null
            }

        })

        exoPlayer.prepare()
        exoPlayer.play()
        startUpdating()
    }

    fun playPause() {
        if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
    }

    fun stopCurrentSong() = exoPlayer.stop()

    fun seekTo(position: Long) = exoPlayer.seekTo(position)

    fun isPlaying(): Boolean = exoPlayer.isPlaying

    fun setPlayerListener(listener: (Long, Long, Boolean) -> Unit) {
        this.listener = listener
        startUpdating()
    }

    private fun startUpdating() {
        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                val pos = exoPlayer.currentPosition
                val dur = exoPlayer.duration
                val playing = exoPlayer.isPlaying

                listener?.invoke(pos, dur, playing)

                isPlaying.postValue(playing)
                currentPositionMillis.postValue(pos)
                currentDurationMillis.postValue(dur)
                positionFormatted.postValue(formatTime(pos))
                durationFormatted.postValue(formatTime(dur))

                handler.postDelayed(this, 1000)
            }
        })
    }

    suspend fun getStreamUrl(trackId: String): String? {
        return audiusRepository.getStreamUrl(trackId)
    }

    fun setOnSongCompletedListener(callback: () -> Unit) {
        onSongCompleted = callback
    }

    fun setBufferingListener(listener: (Boolean) -> Unit) {
        bufferingListener = listener
    }

    @OptIn(UnstableApi::class)
    fun getAudioSessionId(): Int = exoPlayer.audioSessionId

    private fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    fun getCurrentSong(): UnifiedMusic? {
        return currentSong.value
    }

    fun shouldStartNewSession(): Boolean {
        return exoPlayer.mediaItemCount == 0 || MusicServiceOnline.isServiceStopped
    }


}

