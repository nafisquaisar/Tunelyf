package com.song.nafis.nf.TuneLyf.DAO

import androidx.lifecycle.LiveData
import androidx.room.*
import com.song.nafis.nf.TuneLyf.Entity.FavoriteEntity

@Dao
interface FavoriteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(song: FavoriteEntity)

    @Delete
    suspend fun deleteFavorite(song: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE id = :songId")
    suspend fun deleteById(songId: String)

    @Query("SELECT * FROM favorites")
    fun getAllFavorites(): LiveData<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT * FROM favorites WHERE id = :songId)")
    fun isFavorite(songId: String): LiveData<Boolean>
}
