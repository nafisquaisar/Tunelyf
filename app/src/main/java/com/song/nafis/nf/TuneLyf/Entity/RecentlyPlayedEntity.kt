package com.song.nafis.nf.TuneLyf.Entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recently_played")
data class RecentlyPlayedEntity(
    @PrimaryKey val songId: String,
    val title: String?,
    val artist: String?,
    val album: String?,
    val image: String?,
    val audioUrl: String?,
    val duration: Long,
    val isLocal: Boolean,
    val playedAt: Long
)

