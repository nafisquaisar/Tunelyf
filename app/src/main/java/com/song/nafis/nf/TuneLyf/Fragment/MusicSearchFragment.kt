package com.song.nafis.nf.TuneLyf.Fragment

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.song.nafis.nf.TuneLyf.Activity.PlayMusicStreamActivity
import com.song.nafis.nf.TuneLyf.UI.AudiusViewModel
import com.song.nafis.nf.TuneLyf.UI.MusicViewModel
import com.song.nafis.nf.TuneLyf.Model.UnifiedMusic
import com.song.nafis.nf.TuneLyf.R
import com.song.nafis.nf.TuneLyf.adapter.SongAdapter
import com.song.nafis.nf.TuneLyf.adapter.SuggestionAdapter
import com.song.nafis.nf.TuneLyf.databinding.FragmentMusicSearchBinding
import com.song.nafis.nf.TuneLyf.resource.Loading
import com.song.nafis.nf.TuneLyf.resource.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MusicSearchFragment : Fragment() {

    private val audiusViewModel: AudiusViewModel by activityViewModels()

    private lateinit var binding: FragmentMusicSearchBinding
    private lateinit var songAdapter: SongAdapter
    private lateinit var suggestionAdapter: SuggestionAdapter
    private lateinit var searchView: SearchView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMusicSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        observeViewModels()

        binding.swipeRefresh.setOnRefreshListener {
            val query = searchView.query.toString()
            if (query.isNotBlank()) {
                audiusViewModel.search(query)
            } else {
                binding.swipeRefresh.isRefreshing = false
            }
        }



        audiusViewModel.loadNewUploads()
    }

    private fun setupRecyclerViews() {

        // 🔹 Main song list
        songAdapter = SongAdapter { track ->
            fetchAndPlayStream(track)
        }

        binding.recyclerView.apply {
            adapter = songAdapter
            layoutManager = LinearLayoutManager(requireContext())
            if (itemDecorationCount == 0) {
                val divider = DividerItemDecoration(
                    requireContext(),
                    DividerItemDecoration.VERTICAL
                )
                addItemDecoration(divider)
            }
        }

        // 🔹 Suggestions list
        suggestionAdapter = SuggestionAdapter(mutableListOf()) { selectedSuggestion ->

            binding.suggestionsRecyclerView.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE


            // 🔁 Purani list clear
            songAdapter.submitList(emptyList())

            // 🔍 New search (Audius)
            audiusViewModel.search(selectedSuggestion)
        }



        suggestionAdapter = SuggestionAdapter(mutableListOf()) { selected ->
            binding.suggestionsRecyclerView.visibility = View.GONE
            searchView.setQuery(selected, true) // 🔥 auto search + list load
        }

        binding.suggestionsRecyclerView.apply {
            adapter = suggestionAdapter
            layoutManager = LinearLayoutManager(requireContext())
            visibility = View.GONE
        }
    }

    private fun observeViewModels() {
        audiusViewModel.tracksResource.observe(viewLifecycleOwner) { result ->
            binding.swipeRefresh.isRefreshing = false

            when (result) {

                is Resource.Loading -> {
                    binding.shimmerLayout.visibility = View.VISIBLE
                    binding.shimmerLayout.startShimmer()
                    binding.recyclerView.visibility = View.GONE
                }

                is Resource.Success -> {
                    binding.shimmerLayout.stopShimmer()
                    binding.shimmerLayout.visibility = View.GONE
                    binding.recyclerView.visibility = View.VISIBLE
                    songAdapter.submitList(result.data)
                    binding.swipeRefresh.isRefreshing = false
                }

                is Resource.Error -> {
                    binding.shimmerLayout.stopShimmer()
                    binding.shimmerLayout.visibility = View.GONE
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun fetchAndPlayStream(track: UnifiedMusic) {
        audiusViewModel.playTrack(
            track = track,
            titleKey = "SEARCH" // 🔧 TRENDING hata diya
        ) { url ->

            Loading.hide()

            if (url.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Stream not playable", Toast.LENGTH_SHORT).show()
                return@playTrack
            }

            val currentList = songAdapter.currentList.toList()
            val index = currentList.indexOfFirst { it.musicId == track.musicId }
            if (index == -1) return@playTrack

            val intent = Intent(requireContext(), PlayMusicStreamActivity::class.java).apply {
                putExtra("SONG_INDEX", index)
                putParcelableArrayListExtra("SONG_LIST", ArrayList(currentList))
            }
            startActivity(intent)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val searchItem = menu.findItem(R.id.search_playlist)
        searchItem?.isVisible = true // 🔧 Search ENABLED
    }

    /* ---------------- SEARCH MENU ---------------- */

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        val searchItem = menu.findItem(R.id.search_playlist)
        searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank() || newText.length < 3) {
                    binding.suggestionsRecyclerView.visibility = View.GONE
                    return true
                }

                audiusViewModel.fetchSuggestions(newText) { suggestions ->
                    if (suggestions.isEmpty()) {
                        binding.suggestionsRecyclerView.visibility = View.GONE
                    } else {
                        suggestionAdapter.update(suggestions)
                        binding.suggestionsRecyclerView.visibility = View.VISIBLE
                    }
                }
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    binding.suggestionsRecyclerView.visibility = View.GONE
                    audiusViewModel.search(query)
                }
                return true
            }
        })
    }
}
