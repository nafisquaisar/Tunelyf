// PlaylistRepository.kt
package com.song.nafis.nf.TuneLyf.Repository

import com.song.nafis.nf.TuneLyf.DAO.PlaylistDao
import com.song.nafis.nf.TuneLyf.Entity.PlaylistEntity
import com.song.nafis.nf.TuneLyf.Model.FirebasePlaylist
import com.google.firebase.firestore.FirebaseFirestore
import com.song.nafis.nf.TuneLyf.DAO.PlaylistSongDao
import com.song.nafis.nf.TuneLyf.Entity.PlaylistSongEntity
import com.song.nafis.nf.TuneLyf.Model.UnifiedMusic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject


class PlaylistRepository @Inject constructor(
    private val playlistDao: PlaylistDao,
    private val playlistSongDao: PlaylistSongDao // ✅ Inject this too

) {
    suspend fun createHybridPlaylist(name: String, userId: String?) {
        val playlistEntity = PlaylistEntity(name = name, createdAt = System.currentTimeMillis(), playlistId = UUID.randomUUID().toString())
        playlistDao.insert(playlistEntity)

        userId?.let {
            syncToFirebase(name, it)
        }
    }

    private fun syncToFirebase(name: String, userId: String) {
        val playlistId = FirebaseFirestore.getInstance().collection("playlists").document().id
        val playlist = FirebasePlaylist(
            playlistId = playlistId,
            name = name,
            createdAt = System.currentTimeMillis(),
            userId = userId
        )
        FirebaseFirestore.getInstance()
            .collection("playlists")
            .document(playlistId)
            .set(playlist)
    }

    // 🔄 Optional: Firebase → Room Sync
    fun syncFirebaseToLocal(userId: String) {
        FirebaseFirestore.getInstance()
            .collection("playlists")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { snapshot ->
                snapshot.documents.forEach { doc ->
                    val playlist = doc.toObject(FirebasePlaylist::class.java)
                    playlist?.let {
                        kotlinx.coroutines.GlobalScope.launch {
                            val exists = playlistDao.getPlaylistByName(it.name, userId)
                            if (exists == null) {
                                playlistDao.insert(
                                    PlaylistEntity(name = it.name, createdAt = it.createdAt, userId = userId)
                                )
                            }
                        }
                    }
                }
            }
    }

    fun allPlaylists(): Flow<List<PlaylistEntity>> {
        return playlistDao.getAllPlaylists()
    }

    suspend fun getPlaylistByName(name: String, userId: String?): PlaylistEntity? {
        return playlistDao.getPlaylistByName(name, userId)
    }

    suspend fun deletePlaylist(playlist: PlaylistEntity) {
        playlistDao.delete(playlist)
        playlist.playlistId?.let { firestoreDocId ->
            FirebaseFirestore.getInstance()
                .collection("playlists")
                .document(firestoreDocId)
                .delete()
        }
    }


    suspend fun updatePlaylist(playlist: PlaylistEntity) {
        // Update locally in Room
        playlistDao.update(playlist)

        // Sync update to Firestore if playlistId is available
        playlist.playlistId?.let { firestoreDocId ->
            val firestorePlaylist = FirebasePlaylist(
                playlistId = firestoreDocId,
                name = playlist.name,
                createdAt = playlist.createdAt,
                userId = playlist.userId ?: ""
            )
            FirebaseFirestore.getInstance()
                .collection("playlists")
                .document(firestoreDocId)
                .set(firestorePlaylist)
                .addOnFailureListener { e ->
                    // Handle failure if needed, e.g. log or retry
                }
        }
    }


    suspend fun getSongsForPlaylist(playlistId: String): List<PlaylistSongEntity> {
        return playlistSongDao.getSongs(playlistId)
    }

    fun getLiveSongsForPlaylist(playlistId: String): androidx.lifecycle.LiveData<List<PlaylistSongEntity>> {
        return playlistSongDao.getLiveSongs(playlistId)
    }

    suspend fun addSongsToPlaylist(songs: List<PlaylistSongEntity>) {
        playlistSongDao.addSongs(songs)
        // 🔁 Update song count in PlaylistEntity
        val playlistId = songs.firstOrNull()?.playlistId ?: return
        val existingPlaylist = playlistDao.getPlaylistById(playlistId) ?: return
        val currentCount = playlistSongDao.getSongs(playlistId).size
        val updated = existingPlaylist.copy(songCount = currentCount)
        playlistDao.update(updated)
    }

    suspend fun removeSongsFromPlaylist(songs: List<UnifiedMusic>, playlistId: String) {
        val ids = songs.map { it.musicId }
        playlistSongDao.removeSongs(ids, playlistId)

        // 🔁 Update song count
        val playlist = playlistDao.getPlaylistById(playlistId) ?: return
        val currentCount = playlistSongDao.getSongs(playlistId).size
        val updated = playlist.copy(songCount = currentCount)
        playlistDao.update(updated)

    }

    suspend fun removeAllSongsByPlaylistId(playlistId: String) {
        playlistSongDao.removeAll(playlistId)
        // 🔁 Reset song count to 0
        val playlist = playlistDao.getPlaylistById(playlistId) ?: return
        val updated = playlist.copy(songCount = 0)
        playlistDao.update(updated)
    }
}
