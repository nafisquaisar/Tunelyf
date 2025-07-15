package com.song.nafis.nf.TuneLyf.Repository


import com.song.nafis.nf.TuneLyf.DAO.FavoriteDao
import com.song.nafis.nf.TuneLyf.Entity.FavoriteEntity
import javax.inject.Inject

class FavoriteRepository @Inject constructor(
    private val dao: FavoriteDao
) {
    fun getAllFavorites() = dao.getAllFavorites()

    fun isFavorite(id: String) = dao.isFavorite(id)

    suspend fun addFavorite(song: FavoriteEntity) {
        dao.insertFavorite(song)
    }

    suspend fun removeFavorite(song: FavoriteEntity) {
        dao.deleteFavorite(song)
    }

    suspend fun removeById(id: String) {
        dao.deleteById(id)
    }
}
