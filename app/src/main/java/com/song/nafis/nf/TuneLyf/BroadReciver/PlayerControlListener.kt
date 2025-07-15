// PlaybackControlHolder.kt
package com.song.nafis.nf.TuneLyf.BroadReciver

object PlaybackControlHolder {
    var listener: PlayerControlListener? = null

    interface PlayerControlListener {
        fun onPlayPause()
        fun onNext()
        fun onPrev()
        fun onExit()
        fun isPlaying(): Boolean
    }
}