package com.song.nafis.nf.TuneLyf.WorkManager

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.song.nafis.nf.TuneLyf.Cache.PreloadPrefs
import com.song.nafis.nf.TuneLyf.Repository.AudiusRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

@HiltWorker
class HomePreloadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: AudiusRepository,
    private val preloadPrefs: PreloadPrefs
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {

        if (preloadPrefs.isPreloadDone()) {
            return Result.success()
        }

        return try {
            repository.preloadHomeSections()
            preloadPrefs.markPreloadDone()
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "❌ Preload failed")
            Result.retry()
        }
    }
}
