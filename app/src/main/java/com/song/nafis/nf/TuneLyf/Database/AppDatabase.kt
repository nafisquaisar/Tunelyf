package com.song.nafis.nf.TuneLyf.Database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.song.nafis.nf.TuneLyf.DAO.CachedTrackDao
import com.song.nafis.nf.TuneLyf.DAO.FavoriteDao
import com.song.nafis.nf.TuneLyf.Entity.CachedTrackEntity

import com.song.nafis.nf.TuneLyf.Entity.PlaylistEntity
import com.song.nafis.nf.TuneLyf.DAO.PlaylistDao
import com.song.nafis.nf.TuneLyf.DAO.PlaylistSongDao
import com.song.nafis.nf.TuneLyf.DAO.RecentlyPlayedDao
import com.song.nafis.nf.TuneLyf.Entity.Converters
import com.song.nafis.nf.TuneLyf.Entity.FavoriteEntity
import com.song.nafis.nf.TuneLyf.Entity.PlaylistSongEntity
import com.song.nafis.nf.TuneLyf.Entity.RecentlyPlayedEntity

@Database(
    entities = [
        CachedTrackEntity::class,
        PlaylistEntity::class,
        FavoriteEntity::class,
        PlaylistSongEntity::class,
        RecentlyPlayedEntity::class
    ],
    version = 12,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cachedTrackDao(): CachedTrackDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun favroiteDao(): FavoriteDao
    abstract fun playlistSongDao(): PlaylistSongDao
    abstract fun recentlyPlayedDao(): RecentlyPlayedDao
}
