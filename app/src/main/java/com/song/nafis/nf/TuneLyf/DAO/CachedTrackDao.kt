package com.song.nafis.nf.TuneLyf.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.song.nafis.nf.TuneLyf.Entity.CachedTrackEntity

@Dao
interface CachedTrackDao {

    @Query("SELECT * FROM cached_tracks WHERE searchKey = :searchKey")
    suspend fun getTracksBySearchKey(searchKey: String): List<CachedTrackEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<CachedTrackEntity>)

    @Query("DELETE FROM cached_tracks WHERE searchKey = :searchKey")
    suspend fun deleteTracksForSearch(searchKey: String)


    @Query("UPDATE cached_tracks SET streamUrl = :url WHERE id = :trackId")
    suspend fun updateStreamUrl(trackId: String, url: String)



}
