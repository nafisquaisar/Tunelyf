package com.song.nafis.nf.TuneLyf.Cache

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@UnstableApi
@Singleton
class AudioCache @Inject constructor(
    @ApplicationContext context: Context
) {

    val simpleCache: SimpleCache by lazy {
        val cacheDir = File(context.cacheDir, "audio_cache")

        SimpleCache(
            cacheDir,
            LeastRecentlyUsedCacheEvictor(100L * 1024 * 1024), // 🔥 ~20 songs
            StandaloneDatabaseProvider(context)
        )
    }
}
