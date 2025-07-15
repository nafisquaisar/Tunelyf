package com.song.nafis.nf.TuneLyf.ApiModel

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

data class JamendoResponse(
    val results: List<JamendoTrack>
)

@Parcelize
data class JamendoTrack(
    val id: String,
    val name: String,
    @SerializedName("artist_name") val artistName: String,
    val audio: String,
    @SerializedName("album_name") val albumName: String,
    val image: String,
    val duration: Int
) : Parcelable
