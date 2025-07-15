package com.song.nafis.nf.TuneLyf.Cache

import com.song.nafis.nf.TuneLyf.DAO.CachedTrackDao
import com.song.nafis.nf.TuneLyf.Model.UnifiedMusic
import com.song.nafis.nf.TuneLyf.Model.toCachedEntity
import com.song.nafis.nf.TuneLyf.Model.toUnifiedMusic
import javax.inject.Inject

class TrackRoomCache @Inject constructor(
    private val dao: CachedTrackDao
) {

    suspend fun getTracksForQueryById(trackId: String, query: String): UnifiedMusic? {
        return dao.getTracksBySearchKey(query)
            .map { it.toUnifiedMusic() }
            .firstOrNull { it.musicId == trackId }
    }

    suspend fun getTracksForQuery(query: String): List<UnifiedMusic> {
        return dao.getTracksBySearchKey(query)
            .map { it.toUnifiedMusic() }
    }

    suspend fun saveTracks(query: String, tracks: List<UnifiedMusic>) {
        val cached = tracks.map { it.toCachedEntity(query) }

        cached.forEach {
            println("💾 saveTracks: saving ${it.id} with streamUrl = ${it.streamUrl}")
        }

        dao.deleteTracksForSearch(query)
        dao.insertTracks(cached)
    }
}
