//package com.song.nafis.nf.blissfulvibes
//
//import android.annotation.SuppressLint
//import android.content.Intent
//import android.os.Bundle
//import androidx.fragment.app.Fragment
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.core.content.ContextCompat
//import com.bumptech.glide.Glide
//import com.bumptech.glide.request.RequestOptions
//import com.song.nafis.nf.blissfulvibes.Model.setSongPosition
//import com.song.nafis.nf.blissfulvibes.databinding.FragmentNowPlayingBinding
//
//class NowPlaying : Fragment() {
//
//    companion object{
//             @SuppressLint("StaticFieldLeak")
//             lateinit var binding: FragmentNowPlayingBinding
//    }
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        val view=inflater.inflate(R.layout.fragment_now_playing, container, false)
//        binding= FragmentNowPlayingBinding.bind(view)
//        binding.root.visibility=View.INVISIBLE
//        binding.playPauseBtn.setOnClickListener{
//            if(PlaymusicList.isPlay)pauseMusic() else playmusic()
//        }
//        binding.nextBtn.setOnClickListener{
//            prenextprocess(true)
//        }
//        binding.root.setOnClickListener {
//            val intent=Intent(requireContext(),PlaymusicList::class.java)
//            intent.putExtra("index",PlaymusicList.songPosition)
//            intent.putExtra("class","NowPlaying")
//            ContextCompat.startActivity(requireContext(),intent,null)
//        }
//        return view
//    }
//
//    private fun prenextprocess(increment:Boolean) {
//        setSongPosition(increment=increment)
//        PlaymusicList.musicService!!.createMediaFun()
//
//
//        Glide.with(this)
//            .load(PlaymusicList.musicPlayList[PlaymusicList.songPosition].imgUri)
//            .apply(RequestOptions().placeholder(R.mipmap.music_icon).centerCrop())
//            .into(binding.musicImage)
//        binding.musicTitle.text = PlaymusicList.musicPlayList[PlaymusicList.songPosition].musicTitle
//        PlaymusicList.musicService!!.showNotification(R.drawable.pause_notification)
//        playmusic()
//    }
//
//    override fun onResume() {
//        super.onResume()
//        if(PlaymusicList.musicService!=null){
//            binding.root.visibility=View.VISIBLE
//            binding.musicTitle.isSelected=true
//            Glide.with(this)
//                .load(PlaymusicList.musicPlayList[PlaymusicList.songPosition].imgUri)
//                .apply(RequestOptions().placeholder(R.mipmap.music_icon).centerCrop())
//                .into(binding.musicImage)
//            binding.musicTitle.text = PlaymusicList.musicPlayList[PlaymusicList.songPosition].musicTitle
//            if(PlaymusicList.isPlay) binding.playPauseBtn.setIconResource(R.drawable.pause_notification)
//            else binding.playPauseBtn.setIconResource(R.drawable.play_notificaiton)
//        }
//    }
//
//    private fun playmusic(){
//        PlaymusicList.musicService!!.mediaPlayer!!.start()
//        PlaymusicList.binding.playMusicplaypausebtn.setImageResource(R.drawable.pause_button)
//        PlaymusicList.musicService!!.showNotification(R.drawable.pause_notification)
//        binding.playPauseBtn.setIconResource(R.drawable.pause_notification)
//        PlaymusicList.isPlay=true
//    }
//    private fun pauseMusic(){
//        PlaymusicList.musicService!!.mediaPlayer!!.pause()
//        PlaymusicList.binding.playMusicplaypausebtn.setImageResource(R.drawable.playbtn)
//        PlaymusicList.musicService!!.showNotification(R.drawable.play_notificaiton)
//        binding.playPauseBtn.setIconResource(R.drawable.play_notificaiton)
//        PlaymusicList.isPlay=false
//    }
//
//
//}