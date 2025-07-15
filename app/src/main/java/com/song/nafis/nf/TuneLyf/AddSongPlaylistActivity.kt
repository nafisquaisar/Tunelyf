package com.song.nafis.nf.TuneLyf

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.song.nafis.nf.TuneLyf.Entity.PlaylistSongEntity
import com.song.nafis.nf.TuneLyf.Model.UnifiedMusic
import com.song.nafis.nf.TuneLyf.UI.PlaylistViewModel
import com.song.nafis.nf.TuneLyf.adapter.UnifiedMusicAdapter
import com.song.nafis.nf.TuneLyf.databinding.ActivityAddSongPlaylistBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class  AddSongPlaylistActivity  : AppCompatActivity() {

    private lateinit var binding: ActivityAddSongPlaylistBinding
    private lateinit var adapter: UnifiedMusicAdapter
    private var fullUnifiedList = arrayListOf<UnifiedMusic>()
    private lateinit var playlistViewModel: PlaylistViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddSongPlaylistBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        setSupportActionBar(binding.playlisttoolbar)
        binding.playlisttoolbar.setNavigationOnClickListener { onBackPressed() }
        playlistViewModel = ViewModelProvider(this)[PlaylistViewModel::class.java]
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_AUDIO), 101)
            }
        }


        setupAdapter()
        setupSearch()

        binding.doneaddsong.setOnClickListener {
            val selectedSongs = adapter.getSelectedSongs()

            val playlistId = intent.getStringExtra("PLAYLIST_ID")  // Make sure you pass this when opening this screen
            val playlistName = intent.getStringExtra("PLAYLIST_NAME") ?: "Playlist"

            if (playlistId == null) {
                Toast.makeText(this, "Playlist ID missing", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val playlistSongs = selectedSongs.map { song ->
                PlaylistSongEntity(
                    playlistId = playlistId,
                    songId = song.musicId,
                    title = song.musicTitle,
                    artist = song.musicArtist,
                    album = song.musicAlbum,
                    image = song.imgUri,
                    audioUrl = song.musicPath,
                    duration = song.duration,
                    isLocal = song.isLocal
                )
            }

            playlistViewModel.addSongsToPlaylist(playlistSongs)

            Toast.makeText(this, "Added ${playlistSongs.size} songs to \"$playlistName\"", Toast.LENGTH_SHORT).show()
            finish()
        }

    }

    private fun setupAdapter() {
        // Local songs
        val localList = fetchLocalMusic()

        // Streaming songs passed from Intent
//        val onlineList = intent.getParcelableArrayListExtra<UnifiedMusic>("STREAM_SONGS") ?: arrayListOf()

        // Combine both
//        fullUnifiedList = ArrayList(localList + onlineList)

        adapter = UnifiedMusicAdapter(
            context = this,
            songs = localList,
            isSelectionMode = true,
            onSongClick = { clickedIndex ->
                // handle click if needed
            },
            isSquareLayout = false // or true if you want square layout
        )



        binding.PlaylistRecylerView.apply {
            setHasFixedSize(true)
            setItemViewCacheSize(13)
            layoutManager = LinearLayoutManager(this@AddSongPlaylistActivity)
            this.adapter = this@AddSongPlaylistActivity.adapter
        }

//        binding.totalsong.text = "Total Songs : ${adapter.itemCount}"
    }

    private fun setupSearch() {
        binding.searchPlaylistMusic.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true

            override fun onQueryTextChange(newText: String?): Boolean {
                val query = newText?.lowercase() ?: ""
                val filtered = fullUnifiedList.filter {
                    it.musicTitle.lowercase().contains(query)
                }
                adapter.updateSongs(filtered)
                return true
            }
        })
    }

    private fun fetchLocalMusic(): List<UnifiedMusic> {
        val localList = mutableListOf<UnifiedMusic>()
        val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID
        )

        val cursor = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            MediaStore.Audio.Media.TITLE + " ASC",
            null
        )

        cursor?.use {
            val albumArtUri = Uri.parse("content://media/external/audio/albumart")
            while (cursor.moveToNext()) {
                val song = UnifiedMusic(
                    musicId = cursor.getString(0),
                    musicTitle = cursor.getString(1),
                    musicAlbum = cursor.getString(2),
                    musicArtist = cursor.getString(3),
                    duration = cursor.getLong(4),
                    musicPath = cursor.getString(5),
                    imgUri = Uri.withAppendedPath(albumArtUri, cursor.getLong(6).toString()).toString(),
                    isLocal = true
                )
                localList.add(song)
            }
        }

        return localList
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            setupAdapter()
        } else {
            Toast.makeText(this, "Permission required to load songs", Toast.LENGTH_SHORT).show()
        }
    }


}