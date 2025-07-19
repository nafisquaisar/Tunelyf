package com.song.nafis.nf.TuneLyf

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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


        ViewCompat.setOnApplyWindowInsetsListener(binding.favRoot) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setSupportActionBar(binding.playlisttoolbar)
        binding.playlisttoolbar.setNavigationOnClickListener { onBackPressed() }

        setupObserver()
        setupRecyclerView()
        binding.swipeRefreshLayout.setOnRefreshListener {
            favoriteViewModel.refreshFavorites()
            binding.swipeRefreshLayout.isRefreshing = false
        }

        binding.swipeRefreshLayout.setColorSchemeResources(
            R.color.secondary_dark_blue,
            R.color.icon_color,
            android.R.color.holo_green_light
        )


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

            val isEmpty = favoriteList.isEmpty()
            binding.favoriteShufflebtn.visibility = if (isEmpty) View.INVISIBLE else View.VISIBLE
            binding.FavoriteRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
            binding.emptyStateWrapper.visibility = if (isEmpty) View.VISIBLE else View.GONE
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
