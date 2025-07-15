package com.song.nafis.nf.TuneLyf

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.song.nafis.nf.TuneLyf.Activity.PlayMusicStreamActivity
import com.song.nafis.nf.TuneLyf.Model.UnifiedMusic
import com.song.nafis.nf.TuneLyf.Model.toJamendoTrack
import com.song.nafis.nf.TuneLyf.Model.toUnifiedMusic
import com.song.nafis.nf.TuneLyf.UI.FavoriteViewModel
import com.song.nafis.nf.TuneLyf.adapter.SongAdapter
import com.song.nafis.nf.TuneLyf.databinding.ActivityFavoriteMusicBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FavoriteMusic : AppCompatActivity() {

    private lateinit var binding: ActivityFavoriteMusicBinding
    private lateinit var adapter: SongAdapter
    private val favoriteViewModel: FavoriteViewModel by viewModels()
    private var favoriteList: List<UnifiedMusic> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoriteMusicBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.playlisttoolbar)
        binding.playlisttoolbar.setNavigationOnClickListener { onBackPressed() }

        setupObserver()
        setupRecyclerView()

        binding.favoriteShufflebtn.setOnClickListener {
            if (favoriteList.isNotEmpty()) {
                val shuffledList = favoriteList.shuffled()
                val intent = Intent(this, PlayMusicStreamActivity::class.java).apply {
                    putExtra("SONG_INDEX", 0)
                    putParcelableArrayListExtra("SONG_LIST", ArrayList(shuffledList))
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "No favorite songs to play", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupObserver() {
        favoriteViewModel.allFavorites.observe(this) { favEntities ->
            val converted = favEntities.map { it.toJamendoTrack().toUnifiedMusic() }
            favoriteList = converted
            adapter.submitList(favoriteList)

            binding.favoriteShufflebtn.visibility =
                if (favoriteList.isEmpty()) View.INVISIBLE else View.VISIBLE
        }
    }

    private fun setupRecyclerView() {
        adapter = SongAdapter { clickedSong ->
            val index = favoriteList.indexOfFirst { it.musicId == clickedSong.musicId }
            if (index != -1) {
                val intent = Intent(this, PlayMusicStreamActivity::class.java).apply {
                    putExtra("SONG_INDEX", index)
                    putParcelableArrayListExtra("SONG_LIST", ArrayList(favoriteList))
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "Song not found in favorites", Toast.LENGTH_SHORT).show()
            }
        }

        binding.FavoriteRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.FavoriteRecyclerView.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.playlist_menu, menu)
        val searchView = menu?.findItem(R.id.search_playlist)?.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true
            override fun onQueryTextChange(newText: String?): Boolean {
                val filtered = favoriteList.filter {
                    it.musicTitle.contains(newText ?: "", ignoreCase = true) ||
                            it.musicArtist.contains(newText ?: "", ignoreCase = true)
                }
                adapter.submitList(filtered)
                return true
            }
        })
        return true
    }
}
