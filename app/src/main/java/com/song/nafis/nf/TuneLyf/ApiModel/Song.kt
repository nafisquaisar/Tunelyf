package com.song.nafis.nf.TuneLyf.ApiModel


data class Song(
    val id: String?,
    val title: String?,
    val album: String?,
    val artist: String?,
    val duration: String?,  // API may give it as String or Int
    val url: String?,
    val image: String?
)

