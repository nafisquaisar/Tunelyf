package com.song.nafis.nf.TuneLyf.Repository

import com.song.nafis.nf.TuneLyf.Api.AudiusApi
import com.song.nafis.nf.TuneLyf.ApiModel.AudiusTrack
import timber.log.Timber
import javax.inject.Inject

class AudiusRepository @Inject constructor(
    private val api: AudiusApi
) {
    suspend fun searchTracks(query: String, limit: Int = 10, offset: Int = 0): List<AudiusTrack>? {
        return try {
            val safeLimit = if (limit <= 0) 10 else if(limit>20) 20 else limit
            val response = api.searchTracks(artist = query, offset = offset, limit = safeLimit)
            if (response.isSuccessful) {
                Timber.d("🎯 API success: ${response.body()?.data?.size} tracks found.")
                response.body()?.data

            } else {
                Timber.e("❌ API error: ${response.errorBody()?.string()}")
                null
            }


        } catch (e: Exception) {
            Timber.e(e, "API call failed")
            null
        }
    }


    suspend fun getStreamUrl(trackId: String): String? {
        return try {
            val response = api.getStreamRedirect(trackId)
            if (response.isSuccessful) {
                val url = response.body()?.streamUrl
                Timber.d("🎧 Stream URL for $trackId: $url")
                url
            } else {
                Timber.e("❌ Failed to fetch stream URL: ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Exception in getStreamUrl")
            null
        }
    }




}
