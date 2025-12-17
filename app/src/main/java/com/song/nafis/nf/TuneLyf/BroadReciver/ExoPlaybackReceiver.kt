//package com.song.nafis.nf.TuneLyf.BroadReciver
//
//import android.content.BroadcastReceiver
//import android.content.Context
//import android.content.Intent
//import android.view.KeyEvent
//import androidx.core.content.ContextCompat
//import com.song.nafis.nf.TuneLyf.Service.MusicServiceOnline
//
//class ExoPlaybackReceiver : BroadcastReceiver() {
//    override fun onReceive(context: Context?, intent: Intent?) {
//        if (intent == null || context == null) return
//
//        when (intent.action) {
//            Intent.ACTION_MEDIA_BUTTON -> {
//                val keyEvent: KeyEvent? = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT)
//                if (keyEvent?.action == KeyEvent.ACTION_DOWN) {
//                    val action = when (keyEvent.keyCode) {
//                        KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> MusicServiceOnline.ACTION_PLAY_PAUSE
//                        KeyEvent.KEYCODE_MEDIA_NEXT -> MusicServiceOnline.ACTION_NEXT
//                        KeyEvent.KEYCODE_MEDIA_PREVIOUS -> MusicServiceOnline.ACTION_PREV
//                        else -> null
//                    }
//
//                    action?.let {
//                        val serviceIntent = Intent(context, MusicServiceOnline::class.java).apply {
//                            this.action = it
//                        }
//                        ContextCompat.startForegroundService(context, serviceIntent)
//                    }
//                }
//            }
//
//            else -> {
//                // Handle your custom notification actions
//                val serviceIntent = Intent(context, MusicServiceOnline::class.java).apply {
//                    action = intent.action
//                }
//                ContextCompat.startForegroundService(context, serviceIntent)
//            }
//        }
//    }
//}
