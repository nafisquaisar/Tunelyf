package com.song.nafis.nf.TuneLyf.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.song.nafis.nf.TuneLyf.Entity.RecentlyPlayedEntity

@Dao
interface RecentlyPlayedDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(song: RecentlyPlayedEntity)

    @Query("SELECT * FROM recently_played ORDER BY playedAt DESC LIMIT :limit")
    suspend fun getRecentlyPlayed(limit: Int = 100): List<RecentlyPlayedEntity>

    @Query("SELECT COUNT(*) FROM recently_played")
    suspend fun getCount(): Int

    // Delete oldest entries beyond 100
    @Query("""
        DELETE FROM recently_played
        WHERE songId IN (
            SELECT songId FROM recently_played
            ORDER BY playedAt ASC
            LIMIT (
                SELECT COUNT(*) - 100 FROM recently_played
            )
        )
    """)
    suspend fun deleteOldEntriesBeyondLimit()
}
