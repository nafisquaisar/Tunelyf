package com.song.nafis.nf.TuneLyf.UI

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
//import com.song.nafis.nf.TuneLyf.BroadReciver.ExoPlaybackReceiver
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

//    private var exoPlaybackReceiver: ExoPlaybackReceiver? = null

    // App states
    val isRepeatMode = MutableLiveData(false)
    val timerRunning = MutableLiveData(false)

    val isBuffering: LiveData<Boolean> = playerRepository.isBufferingLiveData



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
        viewModelScope.launch {
            playerRepository.setInitialIndex(index)

            // Get current song to send in service
            val song = playerRepository.getCurrentSong() ?: return@launch
            val finalSong = getResolvedSong(song) ?: return@launch

            // ✅ Always start service to ensure notification shows up
//            startMusicService(context, finalSong)
        }
    }

    fun playPauseToggle(context: Context) {
        val song = currentUnifiedSong.value ?: return

        viewModelScope.launch {
            val finalSong = getResolvedSong(song) ?: return@launch

            // ✅ Check if service was fully stopped
            if (MusicServiceOnline.isServiceStopped) {
                Timber.d("🚀 Service was stopped — resetting player with current song")

                // Re-prepare player with current song again
                val playlist = playlistLiveData.value ?: return@launch
                playerRepository.setPlaylist(playlist) // 🔁 reset all media items
                playerRepository.setInitialIndex(playerRepository.getCurrentIndex())

                // Reset flag after restarting
                MusicServiceOnline.isServiceStopped = false
            }

            // Ensure service is started

            // Safely restore player if empty
            val restored = playerRepository.restorePlayerIfNeeded()

            // Always toggle now
            playerRepository.playPause()
//            startMusicService(context, finalSong)
        }
    }

    private suspend fun getResolvedSong(song: UnifiedMusic): UnifiedMusic? {
        if (!song.musicPath.isNullOrBlank()) return song

        val url = playerRepository.getStreamUrl(song)
        return if (url.isNullOrBlank()) {
            Timber.e("❌ Failed to fetch stream URL for ${song.musicTitle}")
            null
        } else {
            song.copy(musicPath = url)
        }
    }


    fun seekTo(position: Long) {
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
                playerRepository.seekTo(0)
                playerRepository.resumePlayback()
            } else {
                nextSong()
            }
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
    }

    fun getAudioSessionId(): Int {
        return playerRepository.getAudioSessionId()
    }

}
