//package com.song.nafis.nf.blissfulvibes
//
//import android.annotation.SuppressLint
//import android.content.ComponentName
//import android.content.Intent
//import android.content.ServiceConnection
//import android.media.MediaPlayer
//import android.media.audiofx.AudioEffect
//import android.net.Uri
//import androidx.appcompat.app.AppCompatActivity
//import android.os.Bundle
//import android.os.IBinder
//import android.view.LayoutInflater
//import android.widget.LinearLayout
//import android.widget.SeekBar
//import android.widget.Toast
//import androidx.appcompat.app.AlertDialog
//import androidx.core.content.ContextCompat
//import com.bumptech.glide.Glide
//import com.bumptech.glide.request.RequestOptions
//import com.song.nafis.nf.blissfulvibes.Service.MusicService
//import com.song.nafis.nf.blissfulvibes.Model.MusicDetail
////import com.song.nafis.nf.blissfulvibes.Model.favoriteCheker
//import com.song.nafis.nf.blissfulvibes.Model.formateDuration
//
//import com.song.nafis.nf.blissfulvibes.Model.setSongPosition
//import com.song.nafis.nf.blissfulvibes.Model.stopApplication
//import com.song.nafis.nf.blissfulvibes.databinding.ActivityPlaymusicListBinding
//import com.google.android.material.bottomsheet.BottomSheetDialog
//import com.google.android.material.dialog.MaterialAlertDialogBuilder
//
//class PlaymusicList : AppCompatActivity(), ServiceConnection ,  MediaPlayer.OnCompletionListener{
//
//    private var serviceBound = false
//
//    companion object {
//        lateinit var musicPlayList: ArrayList<MusicDetail>
//        var songPosition: Int = 0
//        var isPlay: Boolean = false
//        var musicService: MusicService? = null
//        var repeat:Boolean=false
//        var min15:Boolean=false
//        var min30:Boolean=false
//        var min60:Boolean=false
//        var nowPlayingId:String=""
//        var isFav:Boolean=false
//        var fabId:Int=-1
//        @SuppressLint("StaticFieldLeak")
//        lateinit var binding: ActivityPlaymusicListBinding
//
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityPlaymusicListBinding.inflate(LayoutInflater.from(this))
//        setContentView(binding.root)
//
//        // ===================== main Function Work Space  start =================
//
//        initialiseLayout()
//        binding.playMusicplaypausebtn.setOnClickListener {
//            if (isPlay) pauseMusic()
//            else playMusic()
//        }
//        binding.playMusicNextbtn.setOnClickListener { preNext(true) }
//        binding.playMusicPreviousbtn.setOnClickListener { preNext(false) }
//        binding.playMusicSeek.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
//            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
//                 if(p2) musicService!!.mediaPlayer!!.seekTo(p1)
//            }
//
//            override fun onStartTrackingTouch(p0: SeekBar?) =Unit
//
//            override fun onStopTrackingTouch(p0: SeekBar?)=Unit
//        })
//
//        binding.repeat.setOnClickListener {
//            if(!repeat){
//                repeat=true
//                binding.repeat.setColorFilter(ContextCompat.getColor(this,R.color.primary_dark_purple))
//            }else{
//                repeat=false
//                binding.repeat.setColorFilter(ContextCompat.getColor(this,R.color.icon_color))
//            }
//        }
//         binding.playMuiscBackbtn.setOnClickListener { finish() }
//        binding.playMusicequiliserbtn.setOnClickListener {
//            try {
//                val eqIntent=Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
//                eqIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, musicService!!.mediaPlayer!!.audioSessionId)
//                eqIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME,baseContext.packageName)
//                eqIntent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE,AudioEffect.CONTENT_TYPE_MUSIC)
//                startActivityForResult(eqIntent,101)
//            }catch (e:Exception){
//                Toast.makeText(this,"Equiliser Feature Not Avilable in your Phone",Toast.LENGTH_SHORT).show()
//            }
//        }
//        binding.stopTimer.setOnClickListener {
//                   var timer=min15||min30|| min60
//                   if(!timer){
//                   showButtomSheetDailog()}
//                   else{
//                       val builder=MaterialAlertDialogBuilder(this)
//                           .setTitle("Stop Timer")
//                           .setMessage("Are you Want to close the timer")
//                           .setPositiveButton("Yes"){_,_,->
//                               min15=false
//                               min30=false
//                               min60=false
//                               binding.stopTimer.setColorFilter(ContextCompat.getColor(this,R.color.icon_color))
//                           }
//                           .setNegativeButton("No"){dailog,_,->
//                               dailog.dismiss()
//                           }
//
//                       val dialog=builder.create()
//                       dialog.show()
//                       dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this,R.color.icon_color))
//                       dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this,R.color.icon_color))
//                   }
//        }
//        binding.sharebtn.setOnClickListener {
//            val shareIntent=Intent()
//            shareIntent.action=Intent.ACTION_SEND
//            shareIntent.type="audio/*"
//            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(musicPlayList[songPosition].musicPath))
//            startActivity(Intent.createChooser(shareIntent,"share Music file"))
//        }
////        binding.playMusicfavoritebtn.setOnClickListener {
////            if(isFav){
////                isFav=false
////                binding.playMusicfavoritebtn.setImageResource(R.drawable.favorite_empty)
////                if (fabId >= 0 && fabId < FavoriteMusic.musicListfb.size) {
////                    FavoriteMusic.musicListfb.removeAt(fabId)
////                }
////            }else{
////                isFav=true
////                binding.playMusicfavoritebtn.setImageResource(R.drawable.favoritefull)
////                FavoriteMusic.musicListfb.add(musicPlayList[songPosition])
////            }
////        }
//        // ===================== main Function Work Space  end =================
//    }
//
//
//
//    // ==================== Service Start =================
//    private fun serviceStart() {
//        val intent = Intent(this, MusicService::class.java)
//        bindService(intent, this, BIND_AUTO_CREATE)
//        startService(intent)
//    }
//
//
//    // =================pause btn function ===================
//    private fun pauseMusic() {
//        binding.playMusicplaypausebtn.setImageResource(R.drawable.playbtn)
//        musicService!!.showNotification(R.drawable.play_notificaiton)
//        NowPlaying.binding.playPauseBtn.setIconResource(R.drawable.play_notificaiton)
//        isPlay = false
//        musicService?.mediaPlayer?.pause()
//    }
//
////    ================play btn function===============
//    private fun playMusic() {
//        binding.playMusicplaypausebtn.setImageResource(R.drawable.pause_button)
//        musicService!!.showNotification(R.drawable.pause_notification)
//        NowPlaying.binding.playPauseBtn.setIconResource(R.drawable.pause_notification)
//        isPlay = true
//        musicService?.mediaPlayer?.start()
//    }
//
//    // =============initilise the music  to play=====================
//    private fun createMediaFun() {
//        try {
//            if (musicService?.mediaPlayer == null) musicService?.mediaPlayer = MediaPlayer()
//            musicService?.mediaPlayer?.reset()
//            musicService?.mediaPlayer?.setDataSource(musicPlayList[songPosition].musicPath)
//            musicService?.mediaPlayer?.prepare()
//            musicService?.mediaPlayer?.start()
//            isPlay=true
//            binding.playMusicplaypausebtn.setImageResource(R.drawable.pause_button)
//            musicService!!.showNotification(R.drawable.pause_notification)
//            binding.musicTimeStart.text= formateDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
//            binding.musicEndTime.text= formateDuration(musicService!!.mediaPlayer!!.duration.toLong())
//            binding.playMusicSeek.progress=0
//            binding.playMusicSeek.max= musicService!!.mediaPlayer!!.duration
//            musicService!!.mediaPlayer!!.setOnCompletionListener(this)
//            nowPlayingId= musicPlayList[songPosition].musicId
//        } catch (e: Exception) {
//            e.printStackTrace()  // Handle the exception appropriately
//        }
//    }
//
//
//    //  =============initilise the layout to and set the music=======================
//    private fun initialiseLayout() {
//        songPosition = intent.getIntExtra("index", 0)
//        when (intent.getStringExtra("class")) {
//            "FavoriteAdapter"->{
//                serviceStart()
//                musicPlayList = ArrayList()
////                musicPlayList.addAll(FavoriteMusic.musicListfb)
//                setLayout()
//            }
//            "NowPlaying"->{
//                setLayout()
//                binding.musicTimeStart.text= formateDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
//                binding.musicEndTime.text= formateDuration(musicService!!.mediaPlayer!!.duration.toLong())
//                binding.playMusicSeek.progress= musicService!!.mediaPlayer!!.currentPosition
//                binding.playMusicSeek.max= musicService!!.mediaPlayer!!.duration
//                if(isPlay) binding.playMusicplaypausebtn.setImageResource(R.drawable.pause_button)
//                else binding.playMusicplaypausebtn.setImageResource(R.drawable.playbtn)
//            }
//            "MusicAdapterSearch"->{
//                serviceStart()
//                musicPlayList= ArrayList()
//                musicPlayList.addAll(MainActivity.musiclistSerach)
//                setLayout()
//            }
//            "MusicAdapter" -> {
//                serviceStart()
//                musicPlayList = ArrayList()
//                musicPlayList.addAll(MainActivity.musiclist)
//                setLayout()
//            }
//            "MainActivity" -> {
//                serviceStart()
//                musicPlayList = ArrayList()
//                musicPlayList.addAll(MainActivity.musiclist)
//                musicPlayList.shuffle()
//                setLayout()
//            }
//            "FavoriteShuffle"->{
//                serviceStart()
//                musicPlayList = ArrayList()
////                musicPlayList.addAll(FavoriteMusic.musicListfb)
//                musicPlayList.shuffle()
//                setLayout()
//            }
//            "PlaylistShuffle"->{
//                serviceStart()
//                musicPlayList = ArrayList()
//                musicPlayList.addAll(PlayList.musicPlaylist.ref[PlaylistMusicListActivity.currentPlaylistPos].playlist)
//                musicPlayList.shuffle()
//                setLayout()
//            }
//            "PlaylistMusicList"->{
//                serviceStart()
//                musicPlayList = ArrayList()
//                musicPlayList.addAll(PlayList.musicPlaylist.ref[PlaylistMusicListActivity.currentPlaylistPos].playlist)
//                setLayout()
//            }
//        }
//        if (serviceBound) {
//            createMediaFun()
//        }
//    }
//
//
//
//    // ================================set all the resourse into the layout start=============================
//    private fun setLayout() {
////        fabId= favoriteCheker(musicPlayList[songPosition].musicId)
//        Glide.with(this)
//            .load(musicPlayList[songPosition].imgUri)
//            .apply(RequestOptions().placeholder(R.mipmap.music_icon).centerCrop())
//            .into(binding.playmusicImg)
//        binding.playMusicTitle.text = musicPlayList[songPosition].musicTitle
//        if(repeat){
//            binding.repeat.setColorFilter(ContextCompat.getColor(this,R.color.primary_dark_purple))
//        }
//        if(min15 || min30||min60) binding.stopTimer.setColorFilter(ContextCompat.getColor(this,R.color.primary_dark_purple))
//        if(isFav) binding.playMusicfavoritebtn.setImageResource(R.drawable.favoritefull)
//        else binding.playMusicfavoritebtn.setImageResource(R.drawable.favorite_empty)
//    }
//
//    // ================================set all the resourse into the layout end=============================
//
//
//    // ============previous or next play function ===========================
//    private fun preNext(increment: Boolean) {
//        setSongPosition(increment)
//        setLayout()
//        createMediaFun()
//    }
//
//
//
//    // ================== Service Connect Fun ===========================
//    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
//        val binder = service as MusicService.MyBinder
//        musicService = binder.currentService()
//        serviceBound = true
//        createMediaFun()
//        musicService!!.seekBarSetup()
//
//    }
//    // ================== Service Connect Fun end ===========================
//
//    // ================== Service Disconnect Fun start ===========================
//    override fun onServiceDisconnected(name: ComponentName?) {
//        musicService = null
//        serviceBound = false
//    }
//    // ================== Service Disconnect Fun end ===========================
//
//    override fun onDestroy() {
//        super.onDestroy()
//        if (serviceBound) {
//            unbindService(this)
//            serviceBound = false
//        }
//    }
//
//    // =================== next song play after first ==============================
//    override fun onCompletion(p0: MediaPlayer?) {
//        if (repeat) {
//            createMediaFun()
//        } else {
//            setSongPosition(increment = true)
//            createMediaFun()
//            try {
//                setLayout()
//            } catch (e: Exception) {
//                return
//            }
//        }
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//       if(resultCode== RESULT_OK || requestCode==101)
//           return
//    }
//
////    ================== show bottomsheetdialog===========================
//    private fun showButtomSheetDailog(){
//        val dialog=BottomSheetDialog(this)
//        dialog.setContentView(R.layout.timer_dilaog)
//        dialog.show()
//
//       dialog.findViewById<LinearLayout>(R.id.time15min)?.setOnClickListener{
//           Toast.makeText(this,"Music will stop after 15min",Toast.LENGTH_SHORT).show()
//           binding.stopTimer.setColorFilter(ContextCompat.getColor(this,R.color.primary_dark_purple))
//           min15=true
//           Thread{
//               Thread.sleep((15*60000).toLong())
//               if(min15) stopApplication()
//           }.start()
//           dialog.dismiss()
//       }
//    dialog.findViewById<LinearLayout>(R.id.time30min)?.setOnClickListener{
//           Toast.makeText(this,"Music will stop after 30min",Toast.LENGTH_SHORT).show()
//        binding.stopTimer.setColorFilter(ContextCompat.getColor(this,R.color.primary_dark_purple))
//        min30=true
//        Thread{
//            Thread.sleep((30*60000).toLong())
//            if(min30) stopApplication()
//        }.start()
//           dialog.dismiss()
//       }
//    dialog.findViewById<LinearLayout>(R.id.time60min)?.setOnClickListener{
//           Toast.makeText(this,"Music will stop after 60min",Toast.LENGTH_SHORT).show()
//        binding.stopTimer.setColorFilter(ContextCompat.getColor(this,R.color.primary_dark_purple))
//        min60=true
//        Thread{
//            Thread.sleep((60*60000).toLong())
//            if(min60) stopApplication()
//        }.start()
//           dialog.dismiss()
//       }
//    }
//}
