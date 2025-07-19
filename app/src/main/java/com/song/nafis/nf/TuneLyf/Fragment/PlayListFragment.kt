package com.song.nafis.nf.TuneLyf.Fragment

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ImageSpan
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.song.nafis.nf.TuneLyf.Activity.PlaylistSongView
import com.song.nafis.nf.TuneLyf.Entity.PlaylistEntity
import com.song.nafis.nf.TuneLyf.FavoriteMusic
import com.song.nafis.nf.TuneLyf.MainActivity
import com.song.nafis.nf.TuneLyf.Model.toUnifiedMusic
import com.song.nafis.nf.TuneLyf.R
import com.song.nafis.nf.TuneLyf.UI.PlaylistViewModel
import com.song.nafis.nf.TuneLyf.adapter.PlaylistsAdapter
import com.song.nafis.nf.TuneLyf.databinding.FragmentPlayListBinding
import com.song.nafis.nf.TuneLyf.databinding.PlaylistCreateDailogBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlayListFragment : Fragment() {
    private lateinit var binding: FragmentPlayListBinding
    private val viewModel: PlaylistViewModel by activityViewModels()
    private lateinit var playlistAdapter: PlaylistsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlayListBinding.inflate(inflater, container, false)

        // Navigate to My Music
        binding.cardMyMusic.setOnClickListener {
            startActivity(Intent(requireContext(), MainActivity::class.java))
        }

        // Navigate to Favorites
        binding.cardFavorites.setOnClickListener {
            startActivity(Intent(requireContext(), FavoriteMusic::class.java))
        }

        // Create new playlist dialog
        binding.createPlaylistbtn.setOnClickListener {
            showCreatePlaylistDialog()
        }

        // Setup RecyclerView and Adapter
        playlistAdapter = PlaylistsAdapter(
            emptyList(),
            onPlaylistClick = { playlist ->
                val id = playlist.playlistId ?: return@PlaylistsAdapter
                viewModel.getSongsForPlaylist(id) { songEntities ->
                    val unifiedSongs = songEntities.map { it.toUnifiedMusic() }
                    val intent = Intent(requireContext(), PlaylistSongView::class.java).apply {
                        putExtra("playlist_name", playlist.name)
                        putExtra("playlist_id", id)
                        putParcelableArrayListExtra("playlist_songs", ArrayList(unifiedSongs))
                    }
                    startActivity(intent)
                }

            },
            onDeleteClick = { playlist ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Delete Playlist")
                    .setMessage("Are you sure you want to delete '${playlist.name}'?")
                    .setPositiveButton("Delete") { dialog, _ ->
                        viewModel.deletePlaylist(playlist)
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            },
            onUpdateClick = { playlist ->
                showUpdatePlaylistDialog(playlist)
            },
            loadSongs = { playlistId, callback ->
                viewModel.getSongsForPlaylist(playlistId, callback)
            }
        )


        binding.recyclerPlaylists.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = playlistAdapter
        }

        // Observe playlists LiveData
        viewModel.allPlaylists.observe(viewLifecycleOwner) { playlists ->
            playlistAdapter.updateList(playlists)

            if (playlists.isNullOrEmpty()) {
                binding.recyclerPlaylists.visibility = View.GONE
                binding.emptyStateWrapper.visibility = View.VISIBLE
            } else {
                binding.recyclerPlaylists.visibility = View.VISIBLE
                binding.emptyStateWrapper.visibility = View.GONE
            }
        }


        setimagetext()

        return binding.root
    }

    private fun setimagetext() {
        val textView = binding.emptyMessage

        // Create the base text with a placeholder
        val text = "No playlists yet.\nTap the   to create one"
        // Load your image as Drawable
        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.create_playlist_add_circle)!!
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        // Create a SpannableString
        val spannable = SpannableString(text)
        // Find the position where to insert image (after "Tap the ")
        val imagePosition = text.indexOf("  ") + 1  // 1st space to hold image
        // Insert image
        val imageSpan = ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM)
        spannable.setSpan(imageSpan, imagePosition, imagePosition + 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)

// Set text to TextView
        textView.text = spannable

    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.search_playlist)?.isVisible = false
    }

    private fun showCreatePlaylistDialog() {
        val customDialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.playlist_create_dailog, binding.root, false)
        val binder = PlaylistCreateDailogBinding.bind(customDialogView)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(customDialogView)
            .show()

        binder.createbtn.setOnClickListener {
            val playlistName = binder.createPlaylist.editText?.text?.toString()?.trim()
            if (!playlistName.isNullOrEmpty()) {
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                viewModel.createPlaylist(playlistName, userId)
                Toast.makeText(
                    requireContext(),
                    "$playlistName Playlist Created",
                    Toast.LENGTH_SHORT
                ).show()
                dialog.dismiss()
            } else {
                binder.createPlaylist.error = "Playlist name cannot be empty"
            }
        }

        binder.cancelbtn.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun showUpdatePlaylistDialog(playlist: PlaylistEntity) {
        val customDialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.playlist_create_dailog, binding.root, false)
        val binder = PlaylistCreateDailogBinding.bind(customDialogView)

        // Pre-fill the current playlist name
        binder.createPlaylist.editText?.setText(playlist.name)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(customDialogView)
            .show()

        binder.createbtn.text = "Update"
        binder.title.text="Update new Playlist"
        binder.createbtn.setOnClickListener {
            val newName = binder.createPlaylist.editText?.text?.toString()?.trim()
            if (!newName.isNullOrEmpty()) {
                if (newName != playlist.name) {
                    // Create a copy with updated name
                    val updatedPlaylist = playlist.copy(name = newName)

                    // Call ViewModel to update playlist
                    viewModel.updatePlaylist(updatedPlaylist)

                    Toast.makeText(requireContext(), "Playlist updated", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                } else {
                    binder.createPlaylist.error = "Please enter a new name"
                }
            } else {
                binder.createPlaylist.error = "Playlist name cannot be empty"
            }
        }

        binder.cancelbtn.setOnClickListener {
            dialog.dismiss()
        }
    }

}
