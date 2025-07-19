package com.song.nafis.nf.TuneLyf.Fragment

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.VideoView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.song.nafis.nf.TuneLyf.Activity.PlayMusicStreamActivity
import com.song.nafis.nf.TuneLyf.Activity.RecentPlayList
import com.song.nafis.nf.TuneLyf.Activity.ViewSongListActivity
import com.song.nafis.nf.TuneLyf.Model.Itemlist
import com.song.nafis.nf.TuneLyf.R
import com.song.nafis.nf.TuneLyf.UI.ArtistViewModel
import com.song.nafis.nf.TuneLyf.UI.AudiusViewModel
import com.song.nafis.nf.TuneLyf.UI.RecentlyPlayedViewModel
import com.song.nafis.nf.TuneLyf.adapter.HomeItemAdapter
import com.song.nafis.nf.TuneLyf.adapter.UnifiedMusicAdapter
import com.song.nafis.nf.TuneLyf.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val artistViewModel: ArtistViewModel by viewModels()
    private val audiusViewModel: AudiusViewModel by viewModels()
    private val recentlyPlayedViewModel: RecentlyPlayedViewModel by viewModels()


    private lateinit var recentAdapter: UnifiedMusicAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= FragmentHomeBinding.inflate(inflater,container,false)

        binding = FragmentHomeBinding.inflate(inflater, container, false)


        val videoView = binding.bannerVideo
        val videoUri = Uri.parse("android.resource://${requireContext().packageName}/raw/homebanner3")
        videoView.setBackgroundColor(Color.WHITE)

        videoView.setVideoURI(videoUri)
        videoView.setOnPreparedListener { mp ->
            mp.isLooping = true
            mp.setVolume(0f, 0f)

            // Clear background once first frame is rendered
            videoView.setBackgroundColor(Color.TRANSPARENT)

            videoView.start()
        }


        // For Trending RecyclerView (square layout)
        val trendingAdapter = HomeItemAdapter(isArtistLayout = false,
                onItemClick = { artist ->
                val intent = Intent(requireContext(), ViewSongListActivity::class.java)
                intent.putExtra("search_query", artist.name) // ✅ Use this for search
                startActivity(intent)
        })
        binding.tredingRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.tredingRecyclerView.adapter = trendingAdapter
        trendingAdapter.submitList(Itemlist.trendingList)



        recentAdapter = UnifiedMusicAdapter(
            context = requireContext(),
            songs = emptyList(),
            isSelectionMode = false,
            onSongClick = { position ->
                val song = recentAdapter.allSongs.getOrNull(position)
                if (song != null) {
                    val intent = Intent(requireContext(), PlayMusicStreamActivity::class.java).apply {
                        putExtra("SONG_LIST", ArrayList(recentAdapter.allSongs))
                        putExtra("SONG_INDEX", position)
                        putExtra("SONG_TITLE", song.musicTitle)
                        putExtra("SONG_TRACK", song.imgUri)
                    }
                    startActivity(intent)

                }
            },
            isSquareLayout = true // ✅ Set this to enable square layout
        )

        binding.RecentRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.RecentRecyclerView.adapter = recentAdapter

        recentlyPlayedViewModel.recentlyPlayed.observe(viewLifecycleOwner) { recentList ->
            recentAdapter.updateSongs(recentList)
        }

        recentlyPlayedViewModel.loadRecentlyPlayed()




//         For Artist RecyclerView (artist layout)
        val artistAdapter = HomeItemAdapter(
            isArtistLayout = true,
            onItemClick = { artist ->
                val intent = Intent(requireContext(), ViewSongListActivity::class.java)
                intent.putExtra("artist_name", artist.name)
                startActivity(intent)
            }
        )
        binding.ArtistRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.ArtistRecyclerView.adapter = artistAdapter
        artistAdapter.submitList(Itemlist.artistList)



        binding.clickMoreRecent.setOnClickListener {
            startActivity(Intent(requireContext(), RecentPlayList::class.java))
        }

        return binding.root

    }


    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val searchItem = menu.findItem(R.id.search_playlist)
        searchItem?.isVisible = false // Hide search in this fragment
    }
    override fun onResume() {
        super.onResume()
        binding.bannerVideo.start()
    }


}