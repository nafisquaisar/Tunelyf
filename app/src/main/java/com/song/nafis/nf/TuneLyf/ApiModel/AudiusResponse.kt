package com.song.nafis.nf.TuneLyf.ApiModel

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


@Parcelize
data class AudiusResponse(
    val data: List<AudiusTrack>
): Parcelable

@Parcelize
data class AudiusTrack(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("user") val user: ArtistData,
    @SerializedName("duration") val duration: Int,
    @SerializedName("artwork") val artwork: Artwork? = null,
    var streamUrl: String? = null,
    @SerializedName("album")
    val album: String? = null
) : Parcelable


@Parcelize
data class Artwork(
    @SerializedName("150x150") val url: String?
): Parcelable

@Parcelize
data class AudiusArtistResponse(
    val data: List<ArtistData>
): Parcelable

@Parcelize
data class ArtistData(
    @SerializedName("id") val id: String,
    @SerializedName("handle") val handle: String,
    @SerializedName("name") val name: String? = null
) : Parcelable

@Parcelize
data class StreamUrlResponse(
    val streamUrl: String
): Parcelable
