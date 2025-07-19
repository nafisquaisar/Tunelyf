package com.song.nafis.nf.TuneLyf.Activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.song.nafis.nf.TuneLyf.Model.UnifiedMusic
import com.song.nafis.nf.TuneLyf.R
import com.song.nafis.nf.TuneLyf.UI.MusicViewModel
import com.song.nafis.nf.TuneLyf.UI.RecentlyPlayedViewModel
import com.song.nafis.nf.TuneLyf.adapter.UnifiedMusicAdapter
import com.song.nafis.nf.TuneLyf.databinding.ActivityRecentPlayListBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecentPlayList : AppCompatActivity() {

    private lateinit var binding: ActivityRecentPlayListBinding
    private lateinit var adapter: UnifiedMusicAdapter

    private val viewModel: RecentlyPlayedViewModel by viewModels()
    private val musicViewModel: MusicViewModel by viewModels()
    private var recentSongList: List<UnifiedMusic> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRecentPlayListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val navIcon = binding.recentToolbar.navigationIcon
        navIcon?.setTint(ContextCompat.getColor(this, android.R.color.white))
        binding.recentToolbar.navigationIcon = navIcon


        setSupportActionBar(binding.recentToolbar)
        binding.recentToolbar.setNavigationOnClickListener { finish() }
        supportActionBar?.title = "Recent Play"

        setupRecyclerView()
        observeRecentlyPlayed()
        handleShuffleButton()
        viewModel.loadRecentlyPlayed(limit = 100)

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadRecentlyPlayed(limit = 100)
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun setupRecyclerView() {
        adapter = UnifiedMusicAdapter(
            context = this,
            songs = emptyList(),
            onSongClick = { index ->
                val songList = adapter.allSongs
                val intent = Intent(this, PlayMusicStreamActivity::class.java).apply {
                    putExtra("SONG_INDEX", index)
                    putParcelableArrayListExtra("SONG_LIST", ArrayList(songList))
                }
                startActivity(intent)
            }

        )
        binding.recentRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.recentRecyclerView.adapter = adapter
    }

    private fun observeRecentlyPlayed() {
        viewModel.recentlyPlayed.observe(this) { songs ->
            recentSongList = songs
            adapter.updateSongs(songs)

            val isEmpty = songs.isEmpty()
            binding.recentRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
            binding.emptyStateWrapper.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.shuffleButton.visibility = if (isEmpty) View.GONE else View.VISIBLE
        }
        // Stop swipe refresh animation
        binding.swipeRefreshLayout.isRefreshing = false
    }

    private fun handleShuffleButton() {
        binding.shuffleButton.setOnClickListener {
            val currentList = viewModel.recentlyPlayed.value.orEmpty()

            if (currentList.isNotEmpty()) {
                val shuffled = currentList.shuffled()
                val firstSong = shuffled[0]

                musicViewModel.setPlaylist(shuffled)
                musicViewModel.setInitialIndex(0, this)

                // Optional: Open full player UI
                val intent = Intent(this, PlayMusicStreamActivity::class.java).apply {
                    putExtra("SONG_INDEX", 0)
                    putParcelableArrayListExtra("SONG_LIST", ArrayList(shuffled))
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "No recent songs to play", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.playlist_menu, menu)

        val searchItem = menu?.findItem(R.id.search_playlist)
        val searchView = searchItem?.actionView as androidx.appcompat.widget.SearchView

        // Get views inside the SearchView
        val searchEditText = searchView.findViewById<android.widget.EditText>(androidx.appcompat.R.id.search_src_text)
        val closeButton = searchView.findViewById<android.widget.ImageView>(androidx.appcompat.R.id.search_close_btn)
        val magIcon = searchView.findViewById<android.widget.ImageView>(androidx.appcompat.R.id.search_mag_icon)
        val searchGoButton = searchView.findViewById<android.widget.ImageView>(androidx.appcompat.R.id.search_go_btn)
        val collapseIcon = searchView.findViewById<android.widget.ImageView>(androidx.appcompat.R.id.search_button)
        val searchPlate = searchView.findViewById<View>(androidx.appcompat.R.id.search_plate)

        collapseIcon.setOnClickListener {
            Toast.makeText(this,"Back", Toast.LENGTH_SHORT).show()
        }

        // ✅ Set white text and hint
        searchEditText.setTextColor(ContextCompat.getColor(this, android.R.color.white))
        searchEditText.setHintTextColor(ContextCompat.getColor(this, android.R.color.white))

        // ✅ Tint all icons to white
        closeButton.setColorFilter(ContextCompat.getColor(this, android.R.color.white))
        magIcon.setColorFilter(ContextCompat.getColor(this, android.R.color.white))
        searchGoButton?.setColorFilter(ContextCompat.getColor(this, android.R.color.white))
        collapseIcon.setColorFilter(ContextCompat.getColor(this, android.R.color.white))

        // ✅ Remove the gray underline
        searchPlate?.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent))

        // ✅ Setup filter logic
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true
            override fun onQueryTextChange(newText: String?): Boolean {
                val filtered = recentSongList.filter {
                    it.musicTitle.contains(newText ?: "", ignoreCase = true) ||
                            it.musicArtist.contains(newText ?: "", ignoreCase = true)
                }
                adapter.updateSongs(filtered)
                return true
            }
        })

        return true
    }



}
