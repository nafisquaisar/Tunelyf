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
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.song.nafis.nf.TuneLyf.Model.UnifiedMusic
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerRepository @Inject constructor(
    private val exoPlayer: ExoPlayer,
    @ApplicationContext private val context: Context,
    private val audiusRepository: AudiusRepository
) {

    // ===== UI STATE =====
    val currentSong = MutableLiveData<UnifiedMusic?>()
    val isPlaying = MutableLiveData<Boolean>()
    val currentTitle = MutableLiveData<String>()
    val currentArtwork = MutableLiveData<String>()
    val durationFormatted = MutableLiveData<String>()
    val positionFormatted = MutableLiveData<String>()
    val currentPositionMillis = MutableLiveData<Long>()
    val currentDurationMillis = MutableLiveData<Long>()
    val isBufferingLiveData = MutableLiveData(false)

    // ===== PLAYLIST STATE =====
    private val playlist = mutableListOf<UnifiedMusic>()
    private val _playlistLiveData = MutableLiveData<List<UnifiedMusic>>()
    val playlistLiveData: LiveData<List<UnifiedMusic>> get() = _playlistLiveData
    private var currentIndex = 0

    // ===== INTERNAL CALLBACKS =====
    private var progressListener: ((Long, Long, Boolean) -> Unit)? = null
    private var onSongCompleted: (() -> Unit)? = null
    private var fetchJob: Job? = null

    // ===== PLAYER LISTENER =====
    private val playerListener = object : Player.Listener {

        override fun onPlaybackStateChanged(state: Int) {
            when (state) {
                Player.STATE_BUFFERING -> isBufferingLiveData.postValue(true)
                Player.STATE_READY -> {
                    isBufferingLiveData.postValue(false)
                    isPlaying.postValue(exoPlayer.isPlaying)
                    refreshNowPlaying()
                }
                Player.STATE_ENDED -> {
                    onSongCompleted?.invoke()
                }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            isBufferingLiveData.postValue(false)
            onSongCompleted?.invoke()
        }
    }

    init {
        exoPlayer.addListener(playerListener)
    }

    // ===== PLAYLIST SETUP =====
    fun setPlaylist(songs: List<UnifiedMusic>) {
        if (isSamePlaylist(songs)) return
        playlist.clear()
        playlist.addAll(songs)
        _playlistLiveData.postValue(playlist)
    }

    private fun isSamePlaylist(newList: List<UnifiedMusic>): Boolean {
        if (playlist.size != newList.size) return false
        return playlist.map { it.musicId } == newList.map { it.musicId }
    }

    // ===== INITIAL PLAY =====
    fun setInitialIndex(index: Int) {
        if (playlist.isEmpty() || index !in playlist.indices) return
        currentIndex = index
        playCurrent()
    }

    // ===== CORE PLAY =====
    @OptIn(UnstableApi::class)
    private fun playCurrent() {
        if (playlist.isEmpty() || currentIndex !in playlist.indices) return

        fetchJob?.cancel()

        val song = playlist[currentIndex]

        isBufferingLiveData.postValue(true) // ✅ RIGHT PLACE

        if (song.musicPath.isNullOrBlank()) {
            fetchJob = CoroutineScope(Dispatchers.IO).launch {
                val url = audiusRepository.getStreamUrl(song)
                if (!url.isNullOrBlank()) {
                    song.musicPath = url
                    withContext(Dispatchers.Main) {
                        startPlayback(song)
                    }
                } else {
                    isBufferingLiveData.postValue(false)
                }
            }
        } else {
            startPlayback(song)
        }
    }

    // ===== ACTUAL PLAYER START =====
    private fun startPlayback(song: UnifiedMusic) {
        exoPlayer.stop()
        exoPlayer.clearMediaItems()

        exoPlayer.setMediaItem(
            MediaItem.Builder()
                .setMediaId(song.musicId)
                .setUri(Uri.parse(song.musicPath))
                .build()
        )

        exoPlayer.prepare()
        exoPlayer.play()

        currentSong.postValue(song)
        currentTitle.postValue(song.musicTitle)
        currentArtwork.postValue(song.imgUri)
    }

    // ===== NEXT / PREVIOUS =====
    fun nextSong() {
        if (playlist.isEmpty()) return
        currentIndex = (currentIndex + 1) % playlist.size
        // ✅ UI turant update
        playlist.getOrNull(currentIndex)?.let { song ->
            currentSong.postValue(song)
            currentTitle.postValue(song.musicTitle)
            currentArtwork.postValue(song.imgUri)
        }
        playCurrent()
    }
    fun previousSong() {
        if (playlist.isEmpty()) return

        currentIndex =
            if (currentIndex - 1 < 0) playlist.size - 1
            else currentIndex - 1

        // ✅ UI turant update (optimistic UI)
        playlist.getOrNull(currentIndex)?.let { song ->
            currentSong.postValue(song)
            currentTitle.postValue(song.musicTitle)
            currentArtwork.postValue(song.imgUri)
        }

        playCurrent()
    }


    // ===== UI REFRESH (EXISTING FUNCTION – NOT REMOVED) =====
    fun refreshNowPlaying() {
        playlist.getOrNull(currentIndex)?.let {
            currentTitle.postValue(it.musicTitle)
            currentArtwork.postValue(it.imgUri)
            isPlaying.postValue(exoPlayer.isPlaying)
        }
    }

    // ===== PLAY / PAUSE =====
    fun playPause() {
        if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
        isPlaying.postValue(exoPlayer.isPlaying)
    }

    fun resumePlayback() {
        if (!exoPlayer.isPlaying) exoPlayer.play()
    }

    fun pausePlayback() {
        if (exoPlayer.isPlaying) exoPlayer.pause()
    }

    // ===== SEEK & STOP =====
    fun seekTo(position: Long) = exoPlayer.seekTo(position)

    fun seekBy(ms: Long) {
        val current = exoPlayer.currentPosition
        val duration = exoPlayer.duration

        val target = (current + ms).coerceIn(0, duration)
        exoPlayer.seekTo(target)
    }


    fun stopCurrentSong() {
        exoPlayer.stop()
    }


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

    // ===== CALLBACK =====
    fun setOnSongCompletedListener(callback: () -> Unit) {
        onSongCompleted = callback
    }

    // ===== RESTORE =====
    fun restorePlayerIfNeeded(): Boolean {
        if (exoPlayer.mediaItemCount == 0 && playlist.isNotEmpty()) {
            val song = playlist.getOrNull(currentIndex) ?: return false
            if (song.musicPath.isNotBlank()) {
                exoPlayer.setMediaItem(MediaItem.fromUri(song.musicPath))
                exoPlayer.prepare()
                return true
            }
        }
        return false
    }

    // ===== HELPERS =====
    private fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        return String.format("%02d:%02d", totalSeconds / 60, totalSeconds % 60)
    }

    fun getCurrentSong(): UnifiedMusic? = currentSong.value
    fun getCurrentIndex(): Int = currentIndex
    fun getPlaylist(): List<UnifiedMusic> = playlist

    @OptIn(UnstableApi::class)
    fun getAudioSessionId(): Int {
        return exoPlayer.audioSessionId
    }
}
