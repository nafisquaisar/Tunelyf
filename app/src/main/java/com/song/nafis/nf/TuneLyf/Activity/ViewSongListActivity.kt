package com.song.nafis.nf.TuneLyf.Activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.song.nafis.nf.TuneLyf.Model.UnifiedMusic
import com.song.nafis.nf.TuneLyf.Model.toUnifiedMusic
import com.song.nafis.nf.TuneLyf.R
import com.song.nafis.nf.TuneLyf.UI.AudiusViewModel
import com.song.nafis.nf.TuneLyf.adapter.SongAdapter
import com.song.nafis.nf.TuneLyf.databinding.ActivityViewSongListBinding
import com.song.nafis.nf.TuneLyf.resource.Loading
import com.song.nafis.nf.TuneLyf.resource.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class ViewSongListActivity : AppCompatActivity() {

    private val audiusViewModel: AudiusViewModel by viewModels()
    private lateinit var binding: ActivityViewSongListBinding
    private lateinit var songAdapter: SongAdapter

    private var isLoading = false
    private var finalQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewSongListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.emptyAnimation.visibility = View.VISIBLE
        binding.emptyAnimation.playAnimation()
        setupToolbar()
        setupRecyclerView()
        observeViewModel()

        val searchQuery = intent.getStringExtra("search_query")
        val artistName = intent.getStringExtra("artist_name")
        finalQuery = (searchQuery ?: artistName).orEmpty()
        val toolbarTitle = artistName ?: searchQuery ?: "Songs"
        binding.hometoolbar.title = toolbarTitle

        audiusViewModel.search(finalQuery)
    }

    private fun setupToolbar() {
        binding.hometoolbar.setNavigationIcon(R.drawable.back_arrow)
        binding.hometoolbar.setTitleTextColor(resources.getColor(R.color.alwayswhite))
        binding.hometoolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupRecyclerView() {
        songAdapter = SongAdapter { track -> playUnifiedMusicTrack(track) }
        binding.musicListRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.musicListRecyclerView.adapter = songAdapter

        binding.musicListRecyclerView.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                if (!recyclerView.canScrollVertically(1) && !isLoading) {
                    isLoading = true
                    audiusViewModel.loadNextPage(finalQuery)
                }
            }
        })
    }

    private fun observeViewModel() {
        audiusViewModel.tracksResource.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    if (songAdapter.itemCount == 0) {
                        // Show shimmer
                        binding.shimmerLayout.visibility = View.VISIBLE
                        binding.shimmerLayout.startShimmer()

                        // Hide actual views
                        binding.musicListRecyclerView.visibility = View.GONE
                        binding.emptyAnimation.visibility = View.GONE
                    } else {
                        binding.bottomLoader.visibility = View.VISIBLE
                    }
                }

                is Resource.Success -> {
                    binding.shimmerLayout.stopShimmer()
                    binding.shimmerLayout.visibility = View.GONE

                    val newList = resource.data ?: emptyList()

                    if (songAdapter.itemCount == 0 && newList.isEmpty()) {
                        binding.musicListRecyclerView.visibility = View.GONE
                        binding.emptyAnimation.visibility = View.VISIBLE
                        binding.bottomLoader.visibility = View.GONE
                        isLoading = false
                        return@observe
                    }

                    val updatedList = (songAdapter.currentList + newList)
                        .distinctBy { it.musicId }

                    songAdapter.submitList(updatedList)

                    binding.musicListRecyclerView.visibility = View.VISIBLE
                    binding.emptyAnimation.visibility = View.GONE
                    binding.bottomLoader.visibility = View.GONE
                    isLoading = false
                }

                is Resource.Error -> {
                    binding.shimmerLayout.stopShimmer()
                    binding.shimmerLayout.visibility = View.GONE

                    isLoading = false
                    binding.bottomLoader.visibility = View.GONE
                    binding.musicListRecyclerView.visibility = View.VISIBLE

                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show()
                    Timber.tag("error").e("Error loading tracks: ${resource.message}")
                }
            }
        }
    }

    private fun playUnifiedMusicTrack(track: UnifiedMusic) {
        val trackList = songAdapter.currentList
        val index = trackList.indexOfFirst { it.musicId == track.musicId }
        if (index == -1) {
            Toast.makeText(this, "Track not found!", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, PlayMusicStreamActivity::class.java).apply {
            putExtra("SONG_INDEX", index)
            putExtra("SONG_TRACK", track.imgUri)
            putParcelableArrayListExtra("SONG_LIST", ArrayList(trackList))
        }
        startActivity(intent)
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}
