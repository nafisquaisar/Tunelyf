package com.song.nafis.nf.TuneLyf.Repository

import com.song.nafis.nf.TuneLyf.DAO.RecentlyPlayedDao
import com.song.nafis.nf.TuneLyf.Model.UnifiedMusic
import com.song.nafis.nf.TuneLyf.Model.toRecentlyPlayedEntity
import com.song.nafis.nf.TuneLyf.Model.toUnifiedMusic
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


class RecentlyPlayedRepository @Inject constructor(
    private val dao: RecentlyPlayedDao
) {

    suspend fun addToRecentlyPlayed(song: UnifiedMusic) {
        dao.insertOrUpdate(song.toRecentlyPlayedEntity())
        val count = dao.getCount()
        if (count > 100) {
            dao.deleteOldEntriesBeyondLimit()
        }
    }

    suspend fun getRecentlyPlayed(limit: Int = 100): List<UnifiedMusic> {
        return dao.getRecentlyPlayed(limit).map { it.toUnifiedMusic() }
    }
}
