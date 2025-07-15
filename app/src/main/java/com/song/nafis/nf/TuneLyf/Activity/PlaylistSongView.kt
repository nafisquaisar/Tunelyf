    package com.song.nafis.nf.TuneLyf.Activity

    import android.content.Intent
    import android.os.Bundle
    import android.view.Menu
    import android.view.MenuItem
    import android.view.View
    import android.widget.TextView
    import android.widget.Toast
    import androidx.activity.result.contract.ActivityResultContracts
    import androidx.activity.viewModels
    import androidx.appcompat.app.AlertDialog
    import androidx.appcompat.app.AppCompatActivity
    import androidx.core.content.ContextCompat
    import androidx.recyclerview.widget.LinearLayoutManager
    import com.bumptech.glide.Glide
    import com.bumptech.glide.request.RequestOptions
    import com.google.android.material.bottomsheet.BottomSheetDialog
    import com.song.nafis.nf.TuneLyf.Model.UnifiedMusic
    import com.song.nafis.nf.TuneLyf.R
    import com.song.nafis.nf.TuneLyf.R.id
    import com.song.nafis.nf.TuneLyf.UI.PlaylistViewModel
    import com.song.nafis.nf.TuneLyf.adapter.SongAdapter
    import com.song.nafis.nf.TuneLyf.AddSongPlaylistActivity
    import com.song.nafis.nf.TuneLyf.UI.MusicViewModel
    import com.song.nafis.nf.TuneLyf.databinding.ActivityPlaylistSongViewBinding
    import dagger.hilt.android.AndroidEntryPoint

    @AndroidEntryPoint
    class PlaylistSongView : AppCompatActivity() {

        private lateinit var binding: ActivityPlaylistSongViewBinding
        private lateinit var adapter: SongAdapter
        private var playlistName: String = "My Playlist"
        private var playlistId: String = ""
        private var songList: List<UnifiedMusic> = emptyList()
        private val viewModel: PlaylistViewModel by viewModels()
        private val musicViewModel: MusicViewModel by viewModels()
        private var isInSelectionMode = false


        companion object {
            private const val REQUEST_CODE_ADD_SONG = 1001
        }

        private val addSongLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val newSongs = result.data?.getParcelableArrayListExtra<UnifiedMusic>("selected_songs") ?: return@registerForActivityResult
                addSongsToPlaylist(newSongs)
            }
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = ActivityPlaylistSongViewBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Receive intent data
            playlistName = intent.getStringExtra("playlist_name") ?: "My Playlist"
            playlistId = intent.getStringExtra("playlist_id") ?: ""
            binding.toolbarTitle.text = playlistName
            setupToolbar()
            observe()
            setupRecyclerView()
            setupShuffleButton()
            loadPlaylistInfo()
        }

        private fun observe() {
            viewModel.getSongsLive(playlistId).observe(this) { songs ->
                val unifiedSongs = songs.map {
                    UnifiedMusic(
                        musicId = it.songId,
                        musicTitle = it.title,
                        musicAlbum = it.album,
                        musicArtist = it.artist,
                        duration = it.duration,
                        musicPath = it.audioUrl,
                        imgUri = it.image,
                        isLocal = it.isLocal
                    )
                }
                songList = unifiedSongs
                adapter.submitList(songList)
                binding.totalPlaylistsong.text = "Total songs: ${songList.size}"
                binding.PlaylistShufflebtn.visibility = if (songList.isNotEmpty()) View.VISIBLE else View.GONE

                // ✅ ✅ Load image here when data is ready
                if (songList.isNotEmpty()) {
                    Glide.with(this)
                        .load(songList[0].imgUri)
                        .apply(RequestOptions().placeholder(R.drawable.bg_playlist_image).centerCrop())
                        .into(binding.playlistImg)
                } else {
                    Glide.with(this)
                        .load(R.drawable.bg_playlist_image)
                        .apply(RequestOptions().centerCrop())
                        .into(binding.playlistImg)
                }
            }
        }

        private fun setupToolbar() {
            setSupportActionBar(binding.playlisttoolbar)
            binding.playlisttoolbar.setNavigationOnClickListener {
                if (isInSelectionMode) {
                    // Exit selection mode
                    adapter.enableSelectionMode(false)
                    isInSelectionMode = false

                    // Reset toolbar
                    binding.playlisttoolbar.menu.clear()
                    onCreateOptionsMenu(binding.playlisttoolbar.menu)  // Inflate original menu again
                    setupToolbar()
                } else {
                    onBackPressed()
                }
            }
            binding.toolbarTitle.text = playlistName
        }

        private fun setupRecyclerView() {
            adapter = SongAdapter { clickedSong ->
                val index = songList.indexOfFirst { it.musicId == clickedSong.musicId }
                if (index != -1) {
                    musicViewModel.setPlaylist(songList)            // ✅ use actual list
                    musicViewModel.setInitialIndex(index,this)           // ✅ set index before opening player

                    val intent = Intent(this, PlayMusicStreamActivity::class.java).apply {
                        putExtra("SONG_INDEX", index)
                        putParcelableArrayListExtra("SONG_LIST", ArrayList(songList))
                    }
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Song not found in playlist", Toast.LENGTH_SHORT).show()
                }
            }
            binding.PlaylistmusicRecyclerView.layoutManager = LinearLayoutManager(this)
            binding.PlaylistmusicRecyclerView.adapter = adapter
            adapter.submitList(songList)
        }


        private fun setupShuffleButton() {
            binding.PlaylistShufflebtn.setOnClickListener {
                if (songList.isNotEmpty()) {
                    // Shuffle the songs
                    val shuffledList = songList.shuffled()

                    // Start playing shuffled list from first song
                    val intent = Intent(this, PlayMusicStreamActivity::class.java).apply {
                        putExtra("SONG_INDEX", 0)  // Start from first in shuffled list
                        putParcelableArrayListExtra("SONG_LIST", ArrayList(shuffledList))
                    }
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Playlist is empty", Toast.LENGTH_SHORT).show()
                }
            }
        }

        private fun loadPlaylistInfo() {
            binding.totalPlaylistsong.text = "Total songs: ${songList.size}"
            binding.createdOn.text = "Created on 17 July 2024"

        }

        override fun onCreateOptionsMenu(menu: Menu?): Boolean {
            menuInflater.inflate(R.menu.more_option, menu)
            return true
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            return when (item.itemId) {
                id.moreOption -> {
                    showBottomSheetDialog()
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
        }

        private fun showBottomSheetDialog() {
            val bottomSheetDialog = BottomSheetDialog(this)
            val bottomSheetView = layoutInflater.inflate(R.layout.bottom_popup_more_option, null)
            bottomSheetDialog.setContentView(bottomSheetView)

            bottomSheetView.findViewById<TextView>(R.id.addSong).setOnClickListener {
                val intent = Intent(this, AddSongPlaylistActivity::class.java).apply {
                    putExtra("PLAYLIST_ID", playlistId)
                    putExtra("PLAYLIST_NAME", playlistName)
                }
                addSongLauncher.launch(intent)

                bottomSheetDialog.dismiss()
            }

            bottomSheetView.findViewById<TextView>(R.id.selectDelete).setOnClickListener {
                bottomSheetDialog.dismiss()
                adapter.enableSelectionMode(true)
                Toast.makeText(this, "Tap on songs to select. Click again to delete.", Toast.LENGTH_SHORT).show()
                isInSelectionMode = true
                binding.playlisttoolbar.menu.clear()
                binding.playlisttoolbar.inflateMenu(R.menu.menu_selection)
                val deleteIcon = binding.playlisttoolbar.menu.findItem(R.id.deleteSelected)?.icon
                deleteIcon?.setTint(ContextCompat.getColor(this, android.R.color.white))

                binding.playlisttoolbar.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.deleteSelected -> {
                            val selectedSongs = adapter.getSelectedSongs()
                            if (selectedSongs.isNotEmpty()) {
                                viewModel.removeSongsFromPlaylist(selectedSongs, playlistId)
                                val updatedList = songList.filterNot { s -> selectedSongs.any { it.musicId == s.musicId } }
                                songList = updatedList
                                adapter.submitList(songList)
                                binding.totalPlaylistsong.text = "Total songs: ${songList.size}"
                                Toast.makeText(this, "Songs removed", Toast.LENGTH_SHORT).show()
                            }
                            adapter.enableSelectionMode(false)
                            setupToolbar()
                            true
                        }
                        else -> false
                    }
                }
            }

            bottomSheetView.findViewById<TextView>(R.id.removeall).setOnClickListener {
                if (adapter.itemCount > 0) {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Remove All Songs")
                        .setMessage("Are you sure you want to remove all songs from the playlist?")
                        .setPositiveButton("Yes") { dialog, _ ->
                            songList = emptyList()
                            viewModel.removeAllSongsFromPlaylist(playlistId)
                            adapter.submitList(songList)
                            binding.totalPlaylistsong.text = "Total songs: 0"
                            Toast.makeText(this, "All songs removed", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        }
                        .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }

                    val dialog = builder.create()
                    dialog.show()
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getColor(R.color.icon_color))
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getColor(R.color.icon_color))
                } else {
                    Toast.makeText(this, "Playlist is already empty", Toast.LENGTH_SHORT).show()
                }
                bottomSheetDialog.dismiss()
            }

            bottomSheetDialog.show()
        }


        private fun addSongsToPlaylist(songs: List<UnifiedMusic>) {
            val updatedList = songList.toMutableList()

            val newSongs = songs.filter { song ->
                updatedList.none { it.musicId == song.musicId }
            }

            if (newSongs.isEmpty()) {
                Toast.makeText(this, "All selected songs are already in playlist", Toast.LENGTH_SHORT).show()
                return
            }

            // ✅ Convert to PlaylistSongEntity using correct property names
            val songEntities = newSongs.map {
                com.song.nafis.nf.TuneLyf.Entity.PlaylistSongEntity(
                    playlistId = playlistId,
                    songId = it.musicId,
                    title = it.musicTitle,
                    artist = it.musicArtist ?: "Unknown",
                    album = it.musicAlbum ?: "Unknown",
                    image = it.imgUri ?: "",
                    audioUrl = it.musicPath ?: "",
                    duration = it.duration,
                    isLocal = it.isLocal // or however you're marking it
                )
            }

            // ✅ Save to Room
            viewModel.addSongsToPlaylist(songEntities)

            // Update UI list
            updatedList.addAll(newSongs)
            songList = updatedList
            adapter.submitList(songList)
            binding.totalPlaylistsong.text = "Total songs: ${songList.size}"

            Toast.makeText(this, "${newSongs.size} songs added to playlist", Toast.LENGTH_SHORT).show()
        }


        override fun onBackPressed() {
            if (isInSelectionMode) {
                // Exit selection mode
                adapter.enableSelectionMode(false)
                isInSelectionMode = false

                // Reset toolbar
                binding.playlisttoolbar.menu.clear()
                onCreateOptionsMenu(binding.playlisttoolbar.menu)  // Inflate original menu again
                setupToolbar()
            } else {
                super.onBackPressed()
            }
        }



    }
