package com.song.nafis.nf.TuneLyf.BroadReciver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.song.nafis.nf.TuneLyf.Service.MusicServiceOnline

class ExoPlaybackReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action ?: return
        Log.d("ExoPlaybackReceiver", "Received action: $action")

        val listener = PlaybackControlHolder.listener

        when (action) {
            MusicServiceOnline.ACTION_PLAY_PAUSE -> {
                listener?.onPlayPause()
            }
            MusicServiceOnline.ACTION_NEXT -> {
                listener?.onNext()
            }
            MusicServiceOnline.ACTION_PREV -> {
                listener?.onPrev()
            }
            MusicServiceOnline.ACTION_EXIT -> {
                // ❌ Important: don't restart service after exit
                listener?.onExit()
            }
            "MUSIC_PLAYBACK_STATE_CHANGED" -> {
                listener?.onPlayPause()
            }
        }
    }
}
