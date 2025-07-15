package com.song.nafis.nf.TuneLyf.ApiModel

data class ApiResponse<T>(
    val success: Boolean,
    val data: T
)
