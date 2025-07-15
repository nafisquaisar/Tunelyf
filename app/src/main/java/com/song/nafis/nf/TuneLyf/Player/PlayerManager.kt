package com.song.nafis.nf.TuneLyf.Player

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.song.nafis.nf.TuneLyf.ApiModel.AudiusTrack
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerManager @Inject constructor(@ApplicationContext context: Context) {

    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build()

    private val _isPlaying = MutableLiveData(false)
    val isPlaying: LiveData<Boolean> = _isPlaying

    private val _currentSong = MutableLiveData<AudiusTrack?>()
    val currentSong: LiveData<AudiusTrack?> = _currentSong

    fun playStream(song: AudiusTrack, streamUrl: String) {
        val mediaItem = MediaItem.fromUri(streamUrl)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
        _currentSong.postValue(song)
        _isPlaying.postValue(true)
    }


    fun pause() {
        exoPlayer.pause()
        _isPlaying.postValue(false)
    }

    fun resume() {
        exoPlayer.play()
        _isPlaying.postValue(true)
    }

    fun release() {
        exoPlayer.release()
    }

    fun isPlaying() = exoPlayer.isPlaying
}
