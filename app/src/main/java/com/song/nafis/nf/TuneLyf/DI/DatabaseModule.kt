package com.song.nafis.nf.TuneLyf.DI

import android.content.Context
import androidx.room.Room
import com.song.nafis.nf.TuneLyf.DAO.CachedTrackDao
import com.song.nafis.nf.TuneLyf.DAO.FavoriteDao
import com.song.nafis.nf.TuneLyf.DAO.PlaylistDao
import com.song.nafis.nf.TuneLyf.DAO.PlaylistSongDao
import com.song.nafis.nf.TuneLyf.DAO.RecentlyPlayedDao
import com.song.nafis.nf.TuneLyf.Database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "music_track"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideCachedTrackDao(database: AppDatabase): CachedTrackDao {
        return database.cachedTrackDao()
    }

    @Provides
    fun providePlaylistDao(database: AppDatabase): PlaylistDao {
        return database.playlistDao()
    }

    @Provides
    fun provideFavoriteDao(database: AppDatabase): FavoriteDao {
        return database.favroiteDao()
    }
    @Provides
    fun providePlaylistSongDao(database: AppDatabase): PlaylistSongDao {
        return database.playlistSongDao()
    }

    @Provides
    fun provideRecentlyPlayedDao(db: AppDatabase): RecentlyPlayedDao = db.recentlyPlayedDao()
}
