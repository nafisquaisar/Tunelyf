package com.song.nafis.nf.TuneLyf.Cache

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreloadPrefs @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("preload_prefs", Context.MODE_PRIVATE)

    fun isPreloadDone(): Boolean =
        prefs.getBoolean("preload_done", false)

    fun markPreloadDone() {
        prefs.edit().putBoolean("preload_done", true).apply()
    }
}
