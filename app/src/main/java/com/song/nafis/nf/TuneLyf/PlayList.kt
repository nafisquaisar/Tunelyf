package com.song.nafis.nf.TuneLyf

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.song.nafis.nf.TuneLyf.adapter.PlaylistAdapter
import com.song.nafis.nf.TuneLyf.Model.MusicPlaylist
import com.song.nafis.nf.TuneLyf.Model.Playlistdata
import com.song.nafis.nf.TuneLyf.databinding.ActivityPlayListBinding
import com.song.nafis.nf.TuneLyf.databinding.PlaylistCreateDailogBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.*

class PlayList : AppCompatActivity() {

    private lateinit var binding: ActivityPlayListBinding
    private lateinit var playAdapter: PlaylistAdapter

    companion object {
        var musicPlaylist: MusicPlaylist = MusicPlaylist()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayListBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        setSupportActionBar(binding.playlisttoolbar)
        binding.playlisttoolbar.setNavigationOnClickListener { onBackPressed() }
        setAdapter()
        binding.createPlaylistbtn.setOnClickListener { showCreatePlaylistDialog() }

    }

    private fun setAdapter() {
        binding.apply {
            PlaylistRecylerView.layoutManager = GridLayoutManager(this@PlayList, 2)
            PlaylistRecylerView.setHasFixedSize(true)
            PlaylistRecylerView.setItemViewCacheSize(13)
            playAdapter = PlaylistAdapter(this@PlayList, musicPlaylist.ref)
            PlaylistRecylerView.adapter = playAdapter
        }
    }

    private fun showCreatePlaylistDialog() {
        val customDialogView = LayoutInflater.from(this).inflate(R.layout.playlist_create_dailog, binding.root, false)
        val binder = PlaylistCreateDailogBinding.bind(customDialogView)
        val dialog = MaterialAlertDialogBuilder(this).setView(customDialogView).show()

        binder.createbtn.setOnClickListener {
            val playlistName = binder.createPlaylist.editText?.text?.toString()
            if (!playlistName.isNullOrEmpty()) {
                createPlaylist(playlistName)
                Toast.makeText(this@PlayList, "$playlistName Playlist Created", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                binder.createPlaylist.error = "Playlist name cannot be empty"

            }
        }

        binder.cancelbtn.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun createPlaylist(name: String) {
        if (musicPlaylist.ref.any { it.name == name }) {
            Toast.makeText(this@PlayList, "Playlist Exists", Toast.LENGTH_SHORT).show()
        } else {
            val newPlaylist = Playlistdata().apply {
                this.name = name
                this.playlist = ArrayList()
                this.createdOn = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(Calendar.getInstance().time)
            }
            musicPlaylist.ref.add(newPlaylist)
            playAdapter.notifyDataSetChanged()
        }
    }

    override fun onResume() {
        super.onResume()
        playAdapter.notifyDataSetChanged()
    }
}
