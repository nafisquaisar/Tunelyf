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
            when (intent?.action) {
                Intent.ACTION_MEDIA_BUTTON -> {
                    val event = intent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
                    event?.let {
                        if (it.action == KeyEvent.ACTION_DOWN) {
                            when (it.keyCode) {
                                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                                    context?.startService(
                                        Intent(context, MusicServiceOnline::class.java)
                                            .setAction(MusicServiceOnline.ACTION_PLAY_PAUSE)
                                    )
                                }
                                // Handle other media keys
                            }
                        }
                    }
                }
                else -> {
                    // Handle your custom actions
                    context?.startService(
                        Intent(context, MusicServiceOnline::class.java)
                            .setAction(intent?.action)
                    )
                }
            }
        }
    }