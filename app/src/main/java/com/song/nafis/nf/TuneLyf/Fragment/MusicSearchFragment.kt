package com.song.nafis.nf.TuneLyf.Fragment

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.song.nafis.nf.TuneLyf.Activity.PlayMusicStreamActivity
import com.song.nafis.nf.TuneLyf.UI.AudiusViewModel
import com.song.nafis.nf.TuneLyf.UI.JamendoViewModel
import com.song.nafis.nf.TuneLyf.UI.MusicViewModel
import com.song.nafis.nf.TuneLyf.Model.UnifiedMusic
import com.song.nafis.nf.TuneLyf.R
import com.song.nafis.nf.TuneLyf.adapter.SongAdapter
import com.song.nafis.nf.TuneLyf.adapter.SuggestionAdapter
import com.song.nafis.nf.TuneLyf.databinding.FragmentMusicSearchBinding
import com.song.nafis.nf.TuneLyf.resource.Loading
import com.song.nafis.nf.TuneLyf.resource.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MusicSearchFragment : Fragment() {

    private val audiusViewModel: AudiusViewModel by activityViewModels()
    private val musicViewModel: MusicViewModel by activityViewModels()
    private val jamendoViewModel: JamendoViewModel by activityViewModels()

    private lateinit var binding: FragmentMusicSearchBinding
    private lateinit var songAdapter: SongAdapter
    private lateinit var suggestionAdapter: SuggestionAdapter

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

        // Initial search to show default list+
//        audiusViewModel.search("hindi")
        jamendoViewModel.search("romantic")
    }

    private fun setupRecyclerViews() {
        songAdapter = SongAdapter { track -> fetchAndPlayStream(track) }
        binding.recyclerView.apply {
            adapter = songAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        suggestionAdapter = SuggestionAdapter(emptyList()) { selectedSuggestion ->
            jamendoViewModel.search(selectedSuggestion)
//            hideSuggestions()
        }
        binding.suggestionsRecyclerView.apply {
            adapter = suggestionAdapter
            layoutManager = LinearLayoutManager(requireContext())
            visibility = View.GONE
        }
    }

    private fun observeViewModels() {
        jamendoViewModel.tracks.observe(viewLifecycleOwner) { result ->
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

                    val tracks = result.data ?: emptyList()
                    songAdapter.submitList(tracks)
                }

                is Resource.Error -> {
                    binding.shimmerLayout.stopShimmer()
                    binding.shimmerLayout.visibility = View.GONE
                    binding.recyclerView.visibility = View.VISIBLE

                    Toast.makeText(requireContext(), result.message ?: "Error loading songs", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun fetchAndPlayStream(track: UnifiedMusic) {
        lifecycleScope.launch {
            Loading.show(requireContext())

            val streamUrl = track.musicPath
            if (streamUrl.isNullOrEmpty()) {
                Loading.hide()
                Toast.makeText(requireContext(), "Stream URL not available!", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val currentList = songAdapter.currentList.toList()
            val trackIndex = currentList.indexOfFirst { it.musicId == track.musicId }

            if (trackIndex == -1) {
                Loading.hide()
                Toast.makeText(requireContext(), "Track not found!", Toast.LENGTH_SHORT).show()
                return@launch
            }

            Loading.hide()

            // ✅ Just launch with intent. Don't set in ViewModel directly.
            val intent = Intent(requireContext(), PlayMusicStreamActivity::class.java).apply {
                putExtra("SONG_INDEX", trackIndex)
                putParcelableArrayListExtra("SONG_LIST", ArrayList(currentList))
            }
            startActivity(intent)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val searchItem = menu.findItem(R.id.search_playlist)
        searchItem?.isVisible = false // Hide search in this fragment
    }

//
//    private fun showSuggestions(suggestions: List<String>) {
//        suggestionAdapter = SuggestionAdapter(suggestions) { selectedSuggestion ->
//            audiusViewModel.search(selectedSuggestion)
//            hideSuggestions()
//        }
//        binding.suggestionsRecyclerView.adapter = suggestionAdapter
//        binding.suggestionsRecyclerView.visibility = View.VISIBLE
//    }
//
//    private fun hideSuggestions() {
//        binding.suggestionsRecyclerView.visibility = View.GONE
//    }
//
//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        super.onCreateOptionsMenu(menu, inflater)
//
//        val searchItem = menu.findItem(R.id.search_playlist)
//        val searchView = searchItem.actionView as SearchView
//
//        // EditText color
//        val searchEditText = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
//        searchEditText.setHintTextColor(Color.WHITE)
//        searchEditText.setTextColor(Color.WHITE)
//        searchEditText.hint = "Search songs..."
//
//        // Search icon and X button color
//        val closeButton = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
//        closeButton.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
//
//        val searchIcon = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_mag_icon)
//        searchIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
//
//        // 🔁 Fix navigation (back arrow) icon color when search is expanded
//        searchView.setOnSearchClickListener {
//            searchView.postDelayed({
//                val toolbar = requireActivity().findViewById<androidx.appcompat.widget.Toolbar>(R.id.hometoolbar)
//                toolbar.navigationIcon?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
//            }, 100)
//        }
//
//        // 🔁 When focus is lost (keyboard down), collapse search and hide lists
//        searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
//            if (!hasFocus) {
//                searchView.onActionViewCollapsed()
//                hideSuggestions()
//                binding.suggestionsRecyclerView.visibility = View.GONE // ✅ Hide results list
//            }
//        }
//
//        // 🔁 On Search Close (X button or back press), hide both lists
//        searchView.setOnCloseListener {
//            hideSuggestions()
//            binding.suggestionsRecyclerView.visibility = View.GONE // ✅ Hide results list
//            false
//        }
//
//        // 🔎 Text change listener
//        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//            override fun onQueryTextChange(newText: String?): Boolean {
//                newText?.let {
//                    audiusViewModel.onQueryChanged(it)
//                }
//                return true
//            }
//
//            override fun onQueryTextSubmit(query: String?): Boolean {
//                query?.let {
//                    audiusViewModel.search(it)
//                    binding.recyclerView.visibility = View.VISIBLE
//                }
//                return true
//            }
//        })
//    }

}
