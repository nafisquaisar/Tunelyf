//package com.song.nafis.nf.blissfulvibes.BroadReciver
//
//import android.content.BroadcastReceiver
//import android.content.Context
//import android.content.Intent
//import com.bumptech.glide.Glide
//import com.bumptech.glide.request.RequestOptions
//import com.song.nafis.nf.blissfulvibes.NowPlaying
//import com.song.nafis.nf.blissfulvibes.PlaymusicList
//import com.song.nafis.nf.blissfulvibes.R
//import com.song.nafis.nf.blissfulvibes.ApplicationClass
////import com.song.nafis.nf.blissfulvibes.Model.favoriteCheker
//import com.song.nafis.nf.blissfulvibes.Model.setSongPosition
//import com.song.nafis.nf.blissfulvibes.Model.stopApplication
//
//class NotificationReciver: BroadcastReceiver() {
//    override fun onReceive(context: Context?, intent: Intent?) {
//        if (context != null && intent != null) {
//            when (intent.action) {
////                ApplicationClass.PREVIOUS -> prenextprocss(false, context)
////                ApplicationClass.PLAY -> {
////                    if (PlaymusicList.isPlay) pausefun() else playfun()
////                }
////                ApplicationClass.NEXT -> prenextprocss(true, context)
////                ApplicationClass.EXIT -> stopApplication()
//            }
//        }
//    }
//
//
//    private fun playfun() {
//        PlaymusicList.isPlay = true
//        PlaymusicList.musicService!!.mediaPlayer!!.start()
//        PlaymusicList.musicService!!.showNotification(R.drawable.pause_notification)
//        PlaymusicList.binding.playMusicplaypausebtn.setImageResource(R.drawable.pause_button)
//        NowPlaying.binding.playPauseBtn.setIconResource(R.drawable.pause_notification)
//    }
//
//    private fun pausefun() {
//        PlaymusicList.isPlay = false
//        PlaymusicList.musicService!!.mediaPlayer!!.pause()
//        PlaymusicList.musicService!!.showNotification(R.drawable.play_notificaiton)
//        PlaymusicList.binding.playMusicplaypausebtn.setImageResource(R.drawable.playbtn)
//        NowPlaying.binding.playPauseBtn.setIconResource(R.drawable.play_notificaiton)
//    }
//
//    private fun prenextprocss(increment: Boolean, context: Context) {
//        setSongPosition(increment)
//        PlaymusicList.musicService!!.createMediaFun()
//        Glide.with(context)
//            .load(PlaymusicList.musicPlayList[PlaymusicList.songPosition].imgUri)
//            .apply(RequestOptions().placeholder(R.mipmap.music_icon).centerCrop())
//            .into(PlaymusicList.binding.playmusicImg)
//        PlaymusicList.binding.playMusicTitle.text = PlaymusicList.musicPlayList[PlaymusicList.songPosition].musicTitle
//
//        Glide.with(context)
//            .load(PlaymusicList.musicPlayList[PlaymusicList.songPosition].imgUri)
//            .apply(RequestOptions().placeholder(R.mipmap.music_icon).centerCrop())
//            .into(NowPlaying.binding.musicImage)
//        NowPlaying.binding.musicTitle.text = PlaymusicList.musicPlayList[PlaymusicList.songPosition].musicTitle
//        playfun()
////        PlaymusicList.fabId = favoriteCheker(PlaymusicList.musicPlayList[PlaymusicList.songPosition].musicId)
//        if (PlaymusicList.isFav) PlaymusicList.binding.playMusicfavoritebtn.setImageResource(R.drawable.favoritefull)
//        else PlaymusicList.binding.playMusicfavoritebtn.setImageResource(R.drawable.favorite_empty)
//    }
//}
