//package com.song.nafis.nf.blissfulvibes.Service
//
//import android.annotation.SuppressLint
//import android.app.PendingIntent
//import android.app.Service
//import android.content.Context
//import android.content.Intent
//import android.graphics.BitmapFactory
//import android.media.AudioManager
//import android.media.MediaPlayer
//import android.os.Binder
//import android.os.Build
//import android.os.Handler
//import android.os.IBinder
//import android.os.Looper
//import android.support.v4.media.MediaMetadataCompat
//import android.support.v4.media.session.MediaSessionCompat
//import android.support.v4.media.session.PlaybackStateCompat
//import androidx.core.app.NotificationCompat
//import androidx.media.app.NotificationCompat.MediaStyle
//import com.bumptech.glide.Glide
//import com.bumptech.glide.request.RequestOptions
//import com.song.nafis.nf.blissfulvibes.BroadReciver.NotificationReciver
//import com.song.nafis.nf.blissfulvibes.MainActivity
//import com.song.nafis.nf.blissfulvibes.NowPlaying
//import com.song.nafis.nf.blissfulvibes.PlaymusicList
//import com.song.nafis.nf.blissfulvibes.R
//import com.song.nafis.nf.blissfulvibes.ApplicationClass
////import com.song.nafis.nf.blissfulvibes.Model.favoriteCheker
//import com.song.nafis.nf.blissfulvibes.Model.formateDuration
//import com.song.nafis.nf.blissfulvibes.Model.getImgPath
//import com.song.nafis.nf.blissfulvibes.Model.setSongPosition
//
//class MusicService: Service(), AudioManager.OnAudioFocusChangeListener{
//    private var myBinder = MyBinder()
//    var mediaPlayer: MediaPlayer? = null
//    private lateinit var mediaSession: MediaSessionCompat
//    private lateinit var runnable: Runnable
//    lateinit var audioManager: AudioManager
//
//
//    override fun onBind(intent: Intent?): IBinder {
//        mediaSession = MediaSessionCompat(baseContext, "BlissFul Music")
//        return myBinder
//    }
//
//    inner class MyBinder: Binder() {
//        fun currentService(): MusicService {
//            return this@MusicService
//        }
//    }
//
//    @SuppressLint("UnspecifiedImmutableFlag")
//    fun showNotification(playPauseBtn: Int) {
//        val intent = Intent(baseContext, MainActivity::class.java)
//
//        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            PendingIntent.FLAG_IMMUTABLE
//        } else {
//            PendingIntent.FLAG_UPDATE_CURRENT
//        }
//
//        val contentIntent = PendingIntent.getActivity(this, 0, intent, flag)
//
//        val prevIntent = Intent(
//            baseContext, NotificationReciver::class.java
//        ).setAction(ApplicationClass.PREVIOUS)
//        val prevPendingIntent = PendingIntent.getBroadcast(baseContext, 0, prevIntent, flag)
//
//        val playIntent =
//            Intent(baseContext, NotificationReciver::class.java).setAction(ApplicationClass.PLAY)
//        val playPendingIntent = PendingIntent.getBroadcast(baseContext, 0, playIntent, flag)
//
//        val nextIntent =
//            Intent(baseContext, NotificationReciver::class.java).setAction(ApplicationClass.NEXT)
//        val nextPendingIntent = PendingIntent.getBroadcast(baseContext, 0, nextIntent, flag)
//
//        val exitIntent =
//            Intent(baseContext, NotificationReciver::class.java).setAction(ApplicationClass.EXIT)
//        val exitPendingIntent = PendingIntent.getBroadcast(baseContext, 0, exitIntent, flag)
//
//
//        // set image icon
//        val imgArt = getImgPath(PlaymusicList.musicPlayList[PlaymusicList.songPosition].musicPath)
//        val img = if (imgArt != null) {
//            BitmapFactory.decodeByteArray(imgArt, 0, imgArt.size)
//        } else {
//            BitmapFactory.decodeResource(resources, R.drawable.notification_music_img)
//        }
//
//        // set all resource on the notification
//        val notification = NotificationCompat.Builder(this, ApplicationClass.CHANNEL_ID)
//            .setContentIntent(contentIntent)
//            .setContentTitle(PlaymusicList.musicPlayList[PlaymusicList.songPosition].musicTitle)
//            .setContentText(PlaymusicList.musicPlayList[PlaymusicList.songPosition].musicArtist)
//            .setSmallIcon(R.mipmap.music_icon)
//            .setLargeIcon(img)
//            .setStyle(MediaStyle().setMediaSession(mediaSession.sessionToken))
//            .setPriority(NotificationCompat.PRIORITY_HIGH)
//            .setOnlyAlertOnce(true)
//            .addAction(R.drawable.previous_notification, "Previous", prevPendingIntent)
//            .addAction(playPauseBtn, "Play", playPendingIntent)
//            .addAction(R.drawable.nextplay_notification, "Next", nextPendingIntent)
//            .addAction(R.drawable.baseline_exit_to_app_24, "Exit", exitPendingIntent)
//            .build()
//
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//
//            mediaSession.setMetadata(
//                MediaMetadataCompat.Builder().putLong(
//                    MediaMetadataCompat.METADATA_KEY_DURATION, mediaPlayer!!.duration.toLong()
//                ).build()
//            )
//
//            mediaSession.setPlaybackState(getPlayBackState())
//            mediaSession.setCallback(object : MediaSessionCompat.Callback() {
//
//                //called when play button is pressed
//                override fun onPlay() {
//                    super.onPlay()
//                    handlePlayPause()
//                }
//
//                //called when pause button is pressed
//                override fun onPause() {
//                    super.onPause()
//                    handlePlayPause()
//                }
//
//                //called when next button is pressed
//                override fun onSkipToNext() {
//                    super.onSkipToNext()
//                    prevNextSong(increment = true, context = baseContext)
//                }
//
//                //called when previous button is pressed
//                override fun onSkipToPrevious() {
//                    super.onSkipToPrevious()
//                    prevNextSong(increment = false, context = baseContext)
//                }
//
//                //called when headphones buttons are pressed
//                //currently only pause or play music on button click
//                override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
//                    handlePlayPause()
//                    return super.onMediaButtonEvent(mediaButtonEvent)
//                }
//
//                //called when seekbar is changed
//                override fun onSeekTo(pos: Long) {
//                    super.onSeekTo(pos)
//                    mediaPlayer?.seekTo(pos.toInt())
//
//                    mediaSession.setPlaybackState(getPlayBackState())
//                }
//            })
//        }
//
//        startForeground(13, notification)
//    }
//
//    fun createMediaFun() {
//        try {
//            if (PlaymusicList.musicService?.mediaPlayer == null) PlaymusicList.musicService?.mediaPlayer = MediaPlayer()
//            PlaymusicList.musicService?.mediaPlayer?.reset()
//            PlaymusicList.musicService?.mediaPlayer?.setDataSource(PlaymusicList.musicPlayList[PlaymusicList.songPosition].musicPath)
//            PlaymusicList.musicService?.mediaPlayer?.prepare()
//            PlaymusicList.binding.playMusicplaypausebtn.setImageResource(R.drawable.pause_button)
//            PlaymusicList.musicService!!.showNotification(R.drawable.pause_notification)
//            PlaymusicList.binding.musicTimeStart.text = formateDuration(mediaPlayer!!.currentPosition.toLong())
//            PlaymusicList.binding.musicEndTime.text = formateDuration(mediaPlayer!!.duration.toLong())
//            PlaymusicList.binding.playMusicSeek.progress = 0
//            PlaymusicList.binding.playMusicSeek.max = PlaymusicList.musicService!!.mediaPlayer!!.duration
//            PlaymusicList.nowPlayingId = PlaymusicList.musicPlayList[PlaymusicList.songPosition].musicId
//        } catch (e: Exception) {
//            e.printStackTrace()  // Handle the exception appropriately
//        }
//    }
//
//    fun seekBarSetup() {
//        runnable = Runnable {
//            PlaymusicList.binding.musicTimeStart.text =
//                formateDuration(mediaPlayer!!.currentPosition.toLong())
//            PlaymusicList.binding.playMusicSeek.progress = mediaPlayer!!.currentPosition
//            Handler(Looper.getMainLooper()).postDelayed(runnable, 200)
//        }
//        Handler(Looper.getMainLooper()).postDelayed(runnable, 0)
//    }
//
//    fun getPlayBackState(): PlaybackStateCompat {
//        val playbackSpeed = if (PlaymusicList.isPlay) 1F else 0F
//
//        return PlaybackStateCompat.Builder().setState(
//            if (mediaPlayer?.isPlaying == true) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
//            mediaPlayer!!.currentPosition.toLong(), playbackSpeed)
//            .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_SEEK_TO or PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
//            .build()
//    }
//
//    fun handlePlayPause() {
//        if (PlaymusicList.isPlay) pauseMusic()
//        else playMusic()
//
//        //update playback state for notification
//        mediaSession.setPlaybackState(getPlayBackState())
//    }
//
//
//
//    private fun prevNextSong(increment: Boolean, context: Context){
//
//        setSongPosition(increment = increment)
//
//        PlaymusicList.musicService?.createMediaFun()
//        Glide.with(context)
//            .load(PlaymusicList.musicPlayList[PlaymusicList.songPosition].imgUri)
//            .apply(RequestOptions().placeholder(R.drawable.musicicon).centerCrop())
//            .into(PlaymusicList.binding.playmusicImg)
//
//        PlaymusicList.binding.playMusicTitle.text = PlaymusicList.musicPlayList[PlaymusicList.songPosition].musicTitle
//
//        Glide.with(context)
//            .load(PlaymusicList.musicPlayList[PlaymusicList.songPosition].imgUri)
//            .apply(RequestOptions().placeholder(R.drawable.musicicon).centerCrop())
//            .into(NowPlaying.binding.musicImage)
//
//        NowPlaying.binding.musicTitle.text = PlaymusicList.musicPlayList[PlaymusicList.songPosition].musicTitle
//
//        playMusic()
//
////        PlaymusicList.fabId = favoriteCheker(PlaymusicList .musicPlayList[PlaymusicList.songPosition].musicId)
//        if(PlaymusicList.isFav) PlaymusicList.binding.playMusicfavoritebtn.setImageResource(R.drawable.favoritefull)
//        else PlaymusicList.binding.playMusicfavoritebtn.setImageResource(R.drawable.favorite_empty)
//
//        //update playback state for notification
//        mediaSession.setPlaybackState(getPlayBackState())
//    }
//
//    override fun onAudioFocusChange(focusChange: Int) {
//        when (focusChange) {
//            AudioManager.AUDIOFOCUS_LOSS -> {
//                // Stop playback and release resources
//                pauseMusic()
//                // Consider stopping the service
//            }
//            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
//                // Pause playback temporarily
//                pauseMusic()
//            }
//            AudioManager.AUDIOFOCUS_GAIN -> {
//                // Resume playback if appropriate
//                playMusic()
//            }
//        }
//    }
//
//
//    private fun playMusic(){
//        //play music
//        PlaymusicList.binding.playMusicplaypausebtn.setImageResource(R.drawable.pause_button)
//        NowPlaying.binding.playPauseBtn.setIconResource(R.drawable.homepause)
//        PlaymusicList.isPlay = true
//        mediaPlayer?.start()
//        showNotification(R.drawable.pause_notification)
//    }
//
//    private fun pauseMusic(){
//        //pause music
//        PlaymusicList.binding.playMusicplaypausebtn.setImageResource(R.drawable.playbtn)
//        NowPlaying.binding.playPauseBtn.setIconResource(R.drawable.homeplay)
//        PlaymusicList.isPlay = false
//        mediaPlayer!!.pause()
//        showNotification(R.drawable.play_notificaiton)
//    }
//
//
//
//
//    //for making persistent
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        return START_STICKY
//    }
//}
