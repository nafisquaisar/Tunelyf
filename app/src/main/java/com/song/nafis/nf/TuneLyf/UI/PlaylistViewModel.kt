// PlaylistViewModel.kt
package com.song.nafis.nf.TuneLyf.UI

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.song.nafis.nf.TuneLyf.Entity.PlaylistEntity
import com.song.nafis.nf.TuneLyf.Entity.PlaylistSongEntity
import com.song.nafis.nf.TuneLyf.Model.UnifiedMusic
import com.song.nafis.nf.TuneLyf.Repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val repository: PlaylistRepository
) : ViewModel() {

    val allPlaylists = repository.allPlaylists().asLiveData()


    fun createPlaylist(name: String, userId: String?) {
        viewModelScope.launch {
            val existing = repository.getPlaylistByName(name, userId)
            if (existing == null) {
                repository.createHybridPlaylist(name, userId)
            }
        }
    }



    fun syncPlaylistsFromFirebase(userId: String) {
        repository.syncFirebaseToLocal(userId)
    }

    fun deletePlaylist(playlist: PlaylistEntity) {
        viewModelScope.launch {
            repository.deletePlaylist(playlist)
        }
    }

    fun updatePlaylist(updatedPlaylist: PlaylistEntity) {
        viewModelScope.launch {
            repository.updatePlaylist(updatedPlaylist)
        }
    }


    fun getSongsLive(playlistId: String): LiveData<List<PlaylistSongEntity>> {
        return repository.getLiveSongsForPlaylist(playlistId)
    }


    // ✅ Add songs to playlist
    fun addSongsToPlaylist(songs: List<PlaylistSongEntity>) {
        viewModelScope.launch {
            repository.addSongsToPlaylist(songs)
        }
    }

    // ✅ Remove selected songs
    fun removeSongsFromPlaylist(songs: List<UnifiedMusic>, playlistId: String) {
        viewModelScope.launch {
            repository.removeSongsFromPlaylist(songs, playlistId)
        }
    }

    // ✅ Remove all songs
    fun removeAllSongsFromPlaylist(playlistId: String) {
        viewModelScope.launch {
            repository.removeAllSongsByPlaylistId(playlistId)
        }
    }

    // ✅ Get all songs of a playlist (async callback)
    fun getSongsForPlaylist(playlistId: String, callback: (List<PlaylistSongEntity>) -> Unit) {
        viewModelScope.launch {
            val songs = repository.getSongsForPlaylist(playlistId)
            callback(songs)
        }
    }

}
