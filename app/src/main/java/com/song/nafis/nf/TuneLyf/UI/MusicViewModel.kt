package com.song.nafis.nf.TuneLyf.UI

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.song.nafis.nf.TuneLyf.BroadReciver.ExoPlaybackReceiver
import com.song.nafis.nf.TuneLyf.Model.UnifiedMusic
import com.song.nafis.nf.TuneLyf.Repository.PlayerRepository
import com.song.nafis.nf.TuneLyf.Service.MusicServiceOnline
import com.song.nafis.nf.TuneLyf.resource.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MusicViewModel @Inject constructor(
    application: Application,
    internal val playerRepository: PlayerRepository
) : AndroidViewModel(application) {

    private var exoPlaybackReceiver: ExoPlaybackReceiver? = null

    // App states
    val isRepeatMode = MutableLiveData(false)
    val timerRunning = MutableLiveData(false)
    val isBuffering = MutableLiveData(false)

    // Expose LiveData from repository
    val currentSongTitle = playerRepository.currentTitle
    val currentSongArtwork = playerRepository.currentArtwork
    val currentUnifiedSong = playerRepository.currentSong
    val isPlaying = playerRepository.isPlaying
    val currentPosition = playerRepository.positionFormatted
    val currentDuration = playerRepository.durationFormatted
    val currentPositionMillis = playerRepository.currentPositionMillis
    val currentDurationMillis = playerRepository.currentDurationMillis
    val playlistLiveData = playerRepository.playlistLiveData

    private val _playMusicStatus = MutableLiveData<Resource<Pair<String, String>>>()
    val playMusicStatus: LiveData<Resource<Pair<String, String>>> get() = _playMusicStatus

    private var stopJob: kotlinx.coroutines.Job? = null

    init {
        observePlayer()
    }

    // Playlist & playback control
    fun setPlaylist(list: List<UnifiedMusic>) = playerRepository.setPlaylist(list)

    fun nextSong() {
        playerRepository.nextSong()
        currentUnifiedSong.postValue(playerRepository.getCurrentSong())
    }

    fun previousSong() {
        playerRepository.previousSong()
        currentUnifiedSong.postValue(playerRepository.getCurrentSong())
    }

    fun refreshNowPlayingUI() = playerRepository.refreshNowPlaying()
    fun setInitialIndex(index: Int, context: Context) {
        playerRepository.setInitialIndex(index)
        val song = playerRepository.getCurrentSong()
        if (song != null) {
            viewModelScope.launch {
                val finalSong = getResolvedSong(song) ?: return@launch
                
//                MusicServiceOnline.isServiceStopped = false
                startMusicService(context, finalSong)
                isPlaying.postValue(true)
            }
        }
    }
    fun playPauseToggle(context: Context) {
        val song = currentUnifiedSong.value ?: run {
            Timber.e("⚠️ No current song selected to play")
            return
        }

        viewModelScope.launch {
            val finalSong = getResolvedSong(song) ?: return@launch

            if (playerRepository.shouldStartNewSession()) {
//                MusicServiceOnline.isServiceStopped = false
                startMusicService(context, finalSong)
            } else {
                playerRepository.playPause()
            }

            isPlaying.value = playerRepository.isPlaying()
        }
    }

    private suspend fun getResolvedSong(song: UnifiedMusic): UnifiedMusic? {
        if (!song.musicPath.isNullOrBlank()) return song

        val url = playerRepository.getStreamUrl(song.musicId)
        return if (url.isNullOrBlank()) {
            Timber.e("❌ Failed to fetch stream URL for ${song.musicTitle}")
            null
        } else {
            song.copy(musicPath = url)
        }
    }


    fun seekTo(position: Long) {
        isBuffering.value = true
        playerRepository.seekTo(position)
    }

    fun toggleRepeatMode() {
        isRepeatMode.value = !(isRepeatMode.value ?: false)
    }

    fun observePlayer() {
        playerRepository.setPlayerListener { position, duration, playing ->
            currentPositionMillis.postValue(position)
            currentDurationMillis.postValue(duration)
            currentPosition.postValue(formatTime(position))
            currentDuration.postValue(formatTime(duration))
            isPlaying.postValue(playing)
        }

        playerRepository.setOnSongCompletedListener {
            if (isRepeatMode.value == true) {
                playerRepository.refreshNowPlaying()
            } else {
                nextSong()
            }
        }

        playerRepository.setBufferingListener {
            isBuffering.postValue(it)
        }
    }

    private fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    fun startStopTimer(minutes: Int) {
        stopJob?.cancel()
        timerRunning.value = true
        stopJob = viewModelScope.launch {
            kotlinx.coroutines.delay(minutes * 60_000L)
            stopMusicPlayback()
            timerRunning.postValue(false)
        }
    }

    fun cancelStopTimer() {
        stopJob?.cancel()
        stopJob = null
        timerRunning.value = false
    }

    private fun stopMusicPlayback() {
        playerRepository.stopCurrentSong()
        isPlaying.postValue(false)
    }

    fun getAudioSessionId(): Int {
        return playerRepository.getAudioSessionId()
    }

    fun registerExoReceiver(context: Context) {
        if (exoPlaybackReceiver == null) {
            exoPlaybackReceiver = ExoPlaybackReceiver()
            val filter = IntentFilter("MUSIC_PLAYBACK_STATE_CHANGED")
            ContextCompat.registerReceiver(
                context,
                exoPlaybackReceiver!!,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
            Timber.d("ExoPlaybackReceiver registered")
        }
    }

    override fun onCleared() {
        exoPlaybackReceiver?.let {
            try {
                getApplication<Application>().unregisterReceiver(it)
                Timber.d("ExoPlaybackReceiver unregistered")
            } catch (e: Exception) {
                Timber.e("Error during unregister: ${e.message}")
            }
        }
        exoPlaybackReceiver = null
    }

    fun startMusicService(context: Context, song: UnifiedMusic) {
        if (song.musicPath.isNullOrBlank()) {
            Timber.e("❌ startMusicService aborted: musicPath is null or blank for ${song.musicTitle}")
            return
        }

        val intent = Intent(context, MusicServiceOnline::class.java).apply {
            putExtra("title", song.musicTitle)
            putExtra("artist", song.musicArtist ?: "Unknown Artist")
            putExtra("image", song.imgUri)
            putExtra("url", song.musicPath)
            putExtra("isLocal", song.isLocal)
        }

        Timber.d("🎧 Starting MusicServiceOnline with URL: ${song.musicPath}")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(context, intent)
        } else {
            context.startService(intent)
        }
    }




}
