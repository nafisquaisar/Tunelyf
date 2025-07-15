package com.song.nafis.nf.TuneLyf.Entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlist_songs")
data class PlaylistSongEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val playlistId: String,
    val songId: String,
    val title: String,
    val artist: String = "Unknown",       // musicArtist / artistName
    val album: String = "Unknown",        // musicAlbum / albumName
    val image: String,
    val audioUrl: String,
    val duration: Long,
    val isLocal: Boolean = true,          // true: local music, false: online
    val addedAt: Long = System.currentTimeMillis()
)
