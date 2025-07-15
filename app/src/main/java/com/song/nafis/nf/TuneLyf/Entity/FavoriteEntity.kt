// File: FavoriteEntity.kt

package com.song.nafis.nf.TuneLyf.Entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey
    val id: String,                        // Shared: musicId or JamendoTrack id
    val title: String,                    // musicTitle / name
    val artist: String = "Unknown",       // musicArtist / artistName
    val album: String = "Unknown",        // musicAlbum / albumName
    val artworkUrl: String = "",          // imgUri / image
    val durationMs: Long = 0L,            // duration in milliseconds (for UnifiedMusic)
    val audioUrl: String = "",            // musicPath / audio URL (empty for local)
    val isLocal: Boolean = true,          // true: local music, false: online
    val addedAt: Long = System.currentTimeMillis()
) : Parcelable
