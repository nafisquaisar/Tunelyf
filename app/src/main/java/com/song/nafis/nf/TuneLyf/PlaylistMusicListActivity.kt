//package com.song.nafis.nf.blissfulvibes
//
//import android.content.Intent
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.Menu
//import android.view.MenuItem
//import android.view.View
//import android.widget.TextView
//import android.widget.Toast
//import androidx.appcompat.app.AlertDialog
//import androidx.appcompat.app.AppCompatActivity
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.bumptech.glide.Glide
//import com.bumptech.glide.request.RequestOptions
//import com.song.nafis.nf.blissfulvibes.R.*
//import com.song.nafis.nf.blissfulvibes.adapter.MusicAdapter
//import com.song.nafis.nf.blissfulvibes.Model.checkPlaylist
//import com.song.nafis.nf.blissfulvibes.databinding.ActivityPlaylistMusicListBinding
//import com.google.android.material.bottomsheet.BottomSheetDialog
//import com.google.gson.GsonBuilder
//import com.song.nafis.nf.blissfulvibes.Activity.PlayMusicStreamActivity
//
//class PlaylistMusicListActivity : AppCompatActivity() {
//    private lateinit var binding: ActivityPlaylistMusicListBinding
//   private lateinit var adapter: MusicAdapter
//    companion object {
//        var currentPlaylistPos = -1
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityPlaylistMusicListBinding.inflate(LayoutInflater.from(this))
//        setContentView(binding.root)
//        setSupportActionBar(binding.playlisttoolbar)
//        binding.playlisttoolbar.setNavigationOnClickListener { onBackPressed() }
//        currentPlaylistPos = intent.extras?.get("index") as Int
//        PlayList.musicPlaylist.ref[currentPlaylistPos].playlist= checkPlaylist(PlayList.musicPlaylist.ref[currentPlaylistPos].playlist)
//        setadapter()
//        binding.PlaylistShufflebtn.setOnClickListener {
//            val intent=Intent(this, PlayMusicStreamActivity::class.java)
//            intent.putExtra("index",0)
//            intent.putExtra("class","PlaylistShuffle")
//            startActivity(intent)
//        }
//    }
//
//    private fun setadapter() {
//        binding.apply {
//            PlaylistmusicRecyclerView.setHasFixedSize(true)
//            PlaylistmusicRecyclerView.setItemViewCacheSize(13)
//            PlaylistmusicRecyclerView.layoutManager=LinearLayoutManager(this@PlaylistMusicListActivity)
//            adapter= MusicAdapter(this@PlaylistMusicListActivity,PlayList.musicPlaylist.ref[currentPlaylistPos].playlist, playlistDetail = true)
//            PlaylistmusicRecyclerView.adapter=adapter
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        binding.toolbarTitle.text=PlayList.musicPlaylist.ref[currentPlaylistPos].name
//        binding.createdOn.text=PlayList.musicPlaylist.ref[currentPlaylistPos].createdOn
//        binding.totalPlaylistsong.text="total ${adapter.itemCount} song"
//        if(adapter.itemCount>0){
//            Glide.with(this)
//                .load(PlayList.musicPlaylist.ref[currentPlaylistPos].playlist[0].imgUri)
//                .apply(RequestOptions().placeholder(mipmap.music_icon).centerCrop())
//                .into(binding.playlistImg)
//            binding.PlaylistShufflebtn.visibility=View.VISIBLE
//        }
//        adapter.notifyDataSetChanged()
//
//        // storing data in shareprefrence
//        val edit=getSharedPreferences("Favorite", MODE_PRIVATE).edit()
//        val jsonStringPlaylist= GsonBuilder().create().toJson(PlayList.musicPlaylist)
//        edit.putString("Playlistmusic",jsonStringPlaylist)
//        edit.apply()
//    }
//
//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.more_option, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            id.moreOption -> {
//                showBottomSheetDialog()
//                true
//            }
//            else -> super.onOptionsItemSelected(item)
//        }
//    }
//
//    private fun showBottomSheetDialog() {
//        val bottomSheetDialog = BottomSheetDialog(this)
//        val bottomSheetView = layoutInflater.inflate(layout.bottom_popup_more_option, null)
//        bottomSheetDialog.setContentView(bottomSheetView)
//        bottomSheetDialog.show()
//
//        // Handle item clicks
//        bottomSheetView.findViewById<TextView>(id.addSong).setOnClickListener {
//            startActivity(Intent(this,AddSongPlaylistActivity::class.java))
//            bottomSheetDialog.dismiss()
//        }
//
//        bottomSheetView.findViewById<TextView>(id.removeall).setOnClickListener {
//           if(adapter.itemCount>0){
//               val builder=AlertDialog.Builder(this)
//               builder.setTitle("Remove All song")
//                   .setMessage("Are you want to Remove all song from playlist")
//                   .setPositiveButton("Yes"){dialog,_,->
//                       PlayList.musicPlaylist.ref[currentPlaylistPos].playlist.clear()
//                       binding.PlaylistShufflebtn.visibility=View.INVISIBLE
//                       adapter.refreshList()
//                       dialog.dismiss()
//                   }
//                   .setNegativeButton("No"){dialog,_,->
//                       dialog.dismiss()
//                   }
//               val dialog=builder.create()
//               dialog.show()
//               dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getColor(color.icon_color))
//               dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getColor(color.icon_color))
//               bottomSheetDialog.dismiss()
//           }else{
//               Toast.makeText(this@PlaylistMusicListActivity,"Empty Playlist",Toast.LENGTH_SHORT).show()
//           }
//        }
//    }
//}
