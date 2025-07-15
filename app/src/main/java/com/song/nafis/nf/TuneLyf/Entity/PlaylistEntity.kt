package com.song.nafis.nf.TuneLyf.Entity
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.song.nafis.nf.TuneLyf.R
import kotlinx.parcelize.Parcelize

@Entity(tableName = "playlists")
@Parcelize
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val createdAt: Long,
    val playlistId: String? = null,
    val userId: String? = null,             // Add this line
    val imageRes: Int = R.drawable.musicicon,
    val songCount: Int = 0,
) : Parcelable
