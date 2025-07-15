package com.song.nafis.nf.TuneLyf.Api

import com.song.nafis.nf.TuneLyf.ApiModel.JamendoResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface JamendoApiService {
    @GET("tracks/")
    suspend fun getTracks(
        @Query("client_id") clientId: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 20,
        @Query("search") search: String? = null  // search parameter
    ): Response<JamendoResponse>

}
