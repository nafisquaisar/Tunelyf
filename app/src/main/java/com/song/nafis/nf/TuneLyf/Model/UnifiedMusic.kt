package com.song.nafis.nf.TuneLyf.Model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UnifiedMusic(
    val musicId: String = "",              // For both
    val musicTitle: String = "",           // For both
    val musicAlbum: String = "",           // For both
    val musicArtist: String = "",          // For both
    var musicPath: String = "",            // Local: file path, Online: stream URL
    val duration: Long = 0L,               // For both
    val imgUri: String = "",               // Online: image URL, Local: content URI or embedded image as Base64
    val isLocal: Boolean = true            // True = Local, False = Online
) : Parcelable
