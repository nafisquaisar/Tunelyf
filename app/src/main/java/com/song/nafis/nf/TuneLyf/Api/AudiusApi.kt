package com.song.nafis.nf.TuneLyf.Api

import com.song.nafis.nf.TuneLyf.ApiModel.AudiusResponse
import com.song.nafis.nf.TuneLyf.ApiModel.StreamUrlResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AudiusApi {

    @GET("audius-search")
    suspend fun searchTracks(
        @Query("artist") artist: String,
        @Query("limit") limit: Int = 10 ,
        @Query("offset") offset: Int = 0
    ): Response<AudiusResponse>

    @GET("audius-stream")
    suspend fun getStreamRedirect(@Query("trackId") trackId: String): Response<StreamUrlResponse>


    // ✅ ADD THIS
    @GET("audius-trending")
    suspend fun getTrendingTracks(
        @Query("limit") limit: Int = 20
    ): Response<AudiusResponse>


    @GET("audius-new")
    suspend fun getNewUploads(
        @Query("limit") limit: Int = 20
    ): Response<AudiusResponse>



}
