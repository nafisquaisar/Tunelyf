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
import androidx.media3.common.Player
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.song.nafis.nf.TuneLyf.Api.AudiusApi
import com.song.nafis.nf.TuneLyf.ApplicationClass
import com.song.nafis.nf.TuneLyf.Model.UnifiedMusic
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audiusApi: AudiusApi,
    private val audiusRepository: AudiusRepository   // ✅ inject
) {

    // 🔒 Single ExoPlayer instance owned ONLY by PlayerRepository
    private val exoPlayer: ExoPlayer
        get() = (context.applicationContext as ApplicationClass).exoPlayer

    // ===== UI STATE =====
    val currentSong = MutableLiveData<UnifiedMusic?>()        // current playing song
    val isPlaying = MutableLiveData<Boolean>()               // play / pause state
    val currentTitle = MutableLiveData<String>()             // song title
    val currentArtwork = MutableLiveData<String>()           // artwork url
    val durationFormatted = MutableLiveData<String>()        // total duration (mm:ss)
    val positionFormatted = MutableLiveData<String>()        // current position (mm:ss)
    val currentPositionMillis = MutableLiveData<Long>()      // current position in ms
    val currentDurationMillis = MutableLiveData<Long>()      // duration in ms
    val isBufferingLiveData = MutableLiveData(false)         // buffering state

    // ===== PLAYLIST STATE =====
    private val playlist = mutableListOf<UnifiedMusic>()     // shared playlist
    private val _playlistLiveData = MutableLiveData<List<UnifiedMusic>>()
    val playlistLiveData: LiveData<List<UnifiedMusic>> get() = _playlistLiveData
    private var currentIndex = 0                              // current song index

    // ===== INTERNAL CALLBACKS =====
    private var progressListener: ((Long, Long, Boolean) -> Unit)? = null
    private var prepareListener: ((Boolean) -> Unit)? = null
    private var onSongCompleted: (() -> Unit)? = null

    // ===== STREAM REPO =====


    // ===== PLAYLIST SETUP =====
    fun setPlaylist(songs: List<UnifiedMusic>) {
        playlist.clear()
        playlist.addAll(songs)
        _playlistLiveData.postValue(playlist)

        exoPlayer.stop()                                      // stop previous playback
        exoPlayer.clearMediaItems()                           // clear old media items

        val items = playlist.map { MediaItem.fromUri(it.musicPath) }
        exoPlayer.setMediaItems(items)                        // set full playlist
        exoPlayer.prepare()
    }

    // ===== INITIAL PLAY =====
    fun setInitialIndex(index: Int) {
        if (playlist.isEmpty() || index !in playlist.indices) return
        currentIndex = index

        val song = playlist[index]

        // fetch stream url if missing
        if (song.musicPath.isNullOrBlank()) {
            CoroutineScope(Dispatchers.IO).launch {
                val url = getStreamUrl(song)
                if (!url.isNullOrBlank()) {
                    song.musicPath = url
                    withContext(Dispatchers.Main) { playCurrent() }
                }
            }
        } else {
            playCurrent()
        }
    }

    // ===== CORE PLAY FUNCTION =====
    @OptIn(UnstableApi::class)
    private fun playCurrent() {
        if (playlist.isEmpty() || currentIndex !in playlist.indices) return

        val song = playlist[currentIndex]
        preparePlayer(song.musicPath, song.musicTitle, song.imgUri, song) {
            if (it) refreshNowPlaying()
        }
    }

    // ===== NEXT / PREVIOUS =====
    fun nextSong() {
        if (playlist.isEmpty()) return
        currentIndex = (currentIndex + 1) % playlist.size
        playCurrent()
    }

    fun previousSong() {
        if (playlist.isEmpty()) return
        currentIndex = if (currentIndex - 1 < 0) playlist.size - 1 else currentIndex - 1
        playCurrent()
    }

    // ===== UI REFRESH =====
    fun refreshNowPlaying() {
        playlist.getOrNull(currentIndex)?.let {
            currentTitle.postValue(it.musicTitle)
            currentArtwork.postValue(it.imgUri)
            isPlaying.postValue(exoPlayer.isPlaying)
        }
    }

    // ===== PLAYER PREPARE =====
    fun preparePlayer(
        songUrl: String,
        title: String,
        image: String,
        song: UnifiedMusic,
        onReady: (Boolean) -> Unit
    ) {
        prepareListener = onReady

        if (songUrl.isBlank()) {
            onReady(false)
            return
        }

        val uri = Uri.parse(songUrl)

        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        exoPlayer.setMediaItem(MediaItem.fromUri(uri), true)

        currentSong.postValue(song)
        currentTitle.postValue(title)
        currentArtwork.postValue(image)

        exoPlayer.addListener(object : Player.Listener {

            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_READY -> {
                        isBufferingLiveData.postValue(false)
                        prepareListener?.invoke(true)
                        prepareListener = null
                    }
                    Player.STATE_BUFFERING -> {
                        isBufferingLiveData.postValue(true)
                    }
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
        startProgressUpdates()
    }

    // ===== PLAY / PAUSE (SAFE API) =====
    fun playPause() {
        if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
        isPlaying.postValue(exoPlayer.isPlaying)
    }

    fun resumePlayback() {                                  // used by ViewModel / Service
        if (!exoPlayer.isPlaying) exoPlayer.play()
    }

    fun pausePlayback() {                                   // used by ViewModel / Service
        if (exoPlayer.isPlaying) exoPlayer.pause()
    }

    // ===== SEEK & STOP =====
    fun seekTo(position: Long) = exoPlayer.seekTo(position)

    fun stopCurrentSong() = exoPlayer.stop()

    // ===== PROGRESS UPDATES =====
    fun setPlayerListener(listener: (Long, Long, Boolean) -> Unit) {
        progressListener = listener
        startProgressUpdates()
    }

    private fun startProgressUpdates() {
        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                val pos = exoPlayer.currentPosition
                val dur = exoPlayer.duration
                val playing = exoPlayer.isPlaying

                progressListener?.invoke(pos, dur, playing)
                currentPositionMillis.postValue(pos)
                currentDurationMillis.postValue(dur)
                positionFormatted.postValue(formatTime(pos))
                durationFormatted.postValue(formatTime(dur))
                isPlaying.postValue(playing)

                handler.postDelayed(this, 1000)
            }
        })
    }

    // ===== STREAM URL =====
    suspend fun getStreamUrl(song: UnifiedMusic): String? {
        return audiusRepository.getStreamUrl(song)
    }



    // ===== CALLBACKS =====
    fun setOnSongCompletedListener(callback: () -> Unit) {
        onSongCompleted = callback
    }

    // ===== HELPERS =====
    private fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        return String.format("%02d:%02d", totalSeconds / 60, totalSeconds % 60)
    }

    fun getCurrentSong(): UnifiedMusic? = currentSong.value

    fun getCurrentIndex(): Int = currentIndex

    fun getPlaylist(): List<UnifiedMusic> = playlist

    fun restorePlayerIfNeeded(): Boolean {
        if (exoPlayer.mediaItemCount == 0 && playlist.isNotEmpty()) {
            val mediaItems = playlist.map { MediaItem.fromUri(it.musicPath) }
            exoPlayer.setMediaItems(mediaItems)
            exoPlayer.prepare()
            return true
        }
        return false
    }


    @OptIn(UnstableApi::class)
    fun getAudioSessionId(): Int {
        return exoPlayer.audioSessionId
    }

}
