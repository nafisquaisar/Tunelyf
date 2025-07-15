package com.song.nafis.nf.TuneLyf.Activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.song.nafis.nf.TuneLyf.UI.RecentlyPlayedViewModel
import com.song.nafis.nf.TuneLyf.adapter.UnifiedMusicAdapter
import com.song.nafis.nf.TuneLyf.databinding.ActivityRecentPlayListBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecentPlayList : AppCompatActivity() {

    private lateinit var binding: ActivityRecentPlayListBinding
    private lateinit var adapter: UnifiedMusicAdapter

    private val viewModel: RecentlyPlayedViewModel by viewModels()

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

        setSupportActionBar(binding.recentToolbar)
        binding.recentToolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        observeRecentlyPlayed()

        // Load songs
        viewModel.loadRecentlyPlayed(limit = 100)
    }

    private fun setupRecyclerView() {
        adapter = UnifiedMusicAdapter(
            context = this,
            songs = emptyList(),
            onSongClick = { index ->
                val song = adapter.allSongs[index]
                // TODO: Play the song or navigate
            }
        )
        binding.recentRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.recentRecyclerView.adapter = adapter
    }

    private fun observeRecentlyPlayed() {
        viewModel.recentlyPlayed.observe(this) { songs ->
            adapter.updateSongs(songs)
        }
    }
}
