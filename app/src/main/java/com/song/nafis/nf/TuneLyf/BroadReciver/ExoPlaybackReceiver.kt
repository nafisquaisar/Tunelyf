package com.song.nafis.nf.TuneLyf.BroadReciver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.KeyEvent
import androidx.core.content.ContextCompat
import com.song.nafis.nf.TuneLyf.Service.MusicServiceOnline

class ExoPlaybackReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action ?: return
        Log.d("ExoPlaybackReceiver", "Received action: $action")

        // For Media Button events
        if (Intent.ACTION_MEDIA_BUTTON == action) {
            val keyEvent = intent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
            Log.d("ExoPlaybackReceiver", "Media button: ${keyEvent?.keyCode}")
        }

        val serviceIntent = Intent(context, MusicServiceOnline::class.java).apply {
            this.action = action
        }
        ContextCompat.startForegroundService(context!!, serviceIntent)

    }

}
