package com.song.nafis.nf.TuneLyf

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.song.nafis.nf.TuneLyf.Activity.BaseActivity
import com.song.nafis.nf.TuneLyf.Entity.PlaylistSongEntity
import com.song.nafis.nf.TuneLyf.Model.UnifiedMusic
import com.song.nafis.nf.TuneLyf.UI.PlaylistViewModel
import com.song.nafis.nf.TuneLyf.adapter.UnifiedMusicAdapter
import com.song.nafis.nf.TuneLyf.databinding.ActivityAddSongPlaylistBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class  AddSongPlaylistActivity  : BaseActivity() {

    private lateinit var binding: ActivityAddSongPlaylistBinding
    private lateinit var adapter: UnifiedMusicAdapter
    private var fullUnifiedList = arrayListOf<UnifiedMusic>()
    private lateinit var playlistViewModel: PlaylistViewModel

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.playlist_menu, menu)

        val searchItem = menu.findItem(R.id.search_playlist)
        val searchView = searchItem.actionView as SearchView

        setupSearch(searchView)
        return true
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddSongPlaylistBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        setSupportActionBar(binding.playlisttoolbar.toolbar)
        binding.playlisttoolbar.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }



        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Add Song In Playlist"
        }

        playlistViewModel = ViewModelProvider(this)[PlaylistViewModel::class.java]
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_AUDIO), 101)
            }
        }


        setupAdapter()

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

        // Assign to fullUnifiedList so search and UI toggle can use it
        fullUnifiedList = ArrayList(localList)

        adapter = UnifiedMusicAdapter(
            context = this,
            songs = fullUnifiedList,
            isSelectionMode = true,
            onSongClick = { clickedIndex ->
                // handle click if needed
            },
            isSquareLayout = false
        )

        binding.PlaylistRecylerView.apply {
            setHasFixedSize(true)
            setItemViewCacheSize(13)
            layoutManager = LinearLayoutManager(this@AddSongPlaylistActivity)
            adapter = this@AddSongPlaylistActivity.adapter
        }

        // Correctly check based on actual song list
        toggleEmptyStateAndDoneButton(fullUnifiedList.isEmpty())
    }

    private fun setupSearch(searchView: SearchView) {

        val searchAutoComplete =
            searchView.findViewById<android.widget.AutoCompleteTextView>(
                androidx.appcompat.R.id.search_src_text
            )

        // Text colors
        searchAutoComplete.setTextColor(
            ContextCompat.getColor(this, R.color.alwayswhite)
        )
        searchAutoComplete.setHintTextColor(
            ContextCompat.getColor(this, R.color.alwayswhite)
        )

        searchView.queryHint = "Search Music"

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val query = newText?.trim()?.lowercase() ?: ""

                val filtered = if (query.isEmpty()) {
                    fullUnifiedList
                } else {
                    fullUnifiedList.filter {
                        it.musicTitle.lowercase().contains(query)
                    }
                }

                adapter.updateSongs(filtered)
                toggleEmptyStateAndDoneButton(filtered.isEmpty())
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

    private fun toggleEmptyStateAndDoneButton(isEmpty: Boolean) {
        binding.emptyStateWrapper.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.doneaddsong.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

}