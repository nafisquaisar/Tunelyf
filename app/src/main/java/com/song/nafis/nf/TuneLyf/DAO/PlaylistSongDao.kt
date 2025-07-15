package com.song.nafis.nf.TuneLyf.DAO

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.song.nafis.nf.TuneLyf.Entity.PlaylistEntity
import com.song.nafis.nf.TuneLyf.Entity.PlaylistSongEntity

@Dao
interface PlaylistSongDao {
    @Query("SELECT * FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun getSongs(playlistId: String): List<PlaylistSongEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSongs(songs: List<PlaylistSongEntity>)

    @Query("SELECT * FROM playlists WHERE playlistId = :playlistId")
    suspend fun getPlaylistById(playlistId: String): PlaylistEntity?


    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songId IN (:songIds)")
    suspend fun removeSongs(songIds: List<String>, playlistId: String)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun removeAll(playlistId: String)


    @Query("SELECT * FROM playlist_songs WHERE playlistId = :playlistId")
    fun getLiveSongs(playlistId: String): LiveData<List<PlaylistSongEntity>>


}
