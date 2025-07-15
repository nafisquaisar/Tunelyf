package com.song.nafis.nf.TuneLyf.Repository

import com.song.nafis.nf.TuneLyf.Api.JamendoApiService
import com.song.nafis.nf.TuneLyf.ApiModel.JamendoTrack
import javax.inject.Inject

class JamendoRepository @Inject constructor(
    private val apiService: JamendoApiService
) {
    suspend fun searchTracks(query: String): List<JamendoTrack>? {
        val response = apiService.getTracks(
            clientId = "23d43879",
            format = "json",
            limit = 20,
            query
        )
        return if (response.isSuccessful) {
            response.body()?.results
        } else null
    }
}

