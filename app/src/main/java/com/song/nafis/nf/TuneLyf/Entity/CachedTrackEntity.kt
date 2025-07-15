package com.song.nafis.nf.TuneLyf.Entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Entity(tableName = "cached_tracks")
@Parcelize
data class CachedTrackEntity(
    @PrimaryKey val id: String,
    val searchKey: String,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val streamUrl: String,
    val imageUrl: String,
    val isLocal: Boolean
) : Parcelable
