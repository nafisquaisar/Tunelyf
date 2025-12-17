//package com.song.nafis.nf.TuneLyf.UI
//
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.ViewModel
//import com.song.nafis.nf.TuneLyf.ApiModel.AudiusTrack
//import com.song.nafis.nf.TuneLyf.Player.PlayerManager
//import dagger.hilt.android.lifecycle.HiltViewModel
//import javax.inject.Inject
//
//@HiltViewModel
//class PlayerViewModel @Inject constructor(
//    private val playerManager: PlayerManager
//) : ViewModel() {
//
//    val isPlaying: LiveData<Boolean> = playerManager.isPlaying
//    val currentSong: LiveData<AudiusTrack?> = playerManager.currentSong
//
//    fun playSong(song: AudiusTrack,streamUrl: String) {
//        playerManager.playStream(song,streamUrl)
//    }
//
//    fun pause() {
//        playerManager.pause()
//    }
//
//    fun resume() {
//        playerManager.resume()
//    }
//}
