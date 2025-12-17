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



    suspend fun getTracksForQuery(query: String): List<UnifiedMusic> =
        dao.getTracksBySearchKey(query).map { it.toUnifiedMusic() }

    suspend fun saveTracks(query: String, tracks: List<UnifiedMusic>) {
        dao.insertTracks(tracks.map { it.toCachedEntity(query) })
    }

    suspend fun updateSingleTrack(query: String, track: UnifiedMusic) {
        val existing = getTracksForQuery(query)
        val updated = (existing.filterNot { it.musicId == track.musicId } + track)
            .distinctBy { it.musicId }
        saveTracks(query, updated)
    }
}
