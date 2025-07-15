    package com.song.nafis.nf.TuneLyf.adapter


    import android.view.LayoutInflater
    import android.view.ViewGroup
    import androidx.recyclerview.widget.RecyclerView
    import com.song.nafis.nf.TuneLyf.Entity.PlaylistEntity
    import com.song.nafis.nf.TuneLyf.databinding.ItemPlaylistSelectBinding


    class PlaylistSelectAdapter(
        private var playlists: List<PlaylistEntity>,
        private val onPlaylistToggle: (PlaylistEntity, Boolean) -> Unit
    ) : RecyclerView.Adapter<PlaylistSelectAdapter.PlaylistSelectViewHolder>() {

        private val selectedPlaylistIds = mutableSetOf<String>()

        inner class PlaylistSelectViewHolder(val binding: ItemPlaylistSelectBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(playlist: PlaylistEntity) {
                binding.tvPlaylistName.text = playlist.name
                val id = playlist.playlistId ?: ""
                val isChecked = selectedPlaylistIds.contains(id)
                binding.checkboxAddToPlaylist.isChecked = isChecked


                binding.root.setOnClickListener {
                    val currentlyChecked = binding.checkboxAddToPlaylist.isChecked

                    if (currentlyChecked) {
                        selectedPlaylistIds.remove(playlist.playlistId)
                        binding.checkboxAddToPlaylist.isChecked = false
                        onPlaylistToggle(playlist, false)
                    } else {
                        selectedPlaylistIds.add(playlist.playlistId ?: "")
                        binding.checkboxAddToPlaylist.isChecked = true
                        onPlaylistToggle(playlist, true)
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistSelectViewHolder {
            val binding = ItemPlaylistSelectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return PlaylistSelectViewHolder(binding)
        }

        override fun onBindViewHolder(holder: PlaylistSelectViewHolder, position: Int) {
            holder.bind(playlists[position])
        }

        override fun getItemCount() = playlists.size

        fun updatePlaylists(newPlaylists: List<PlaylistEntity>) {
            this.playlists = newPlaylists
            notifyDataSetChanged()
        }

        // ✅ New method to preselect checkboxes
        fun setSelectedPlaylists(ids: List<String>) {
            selectedPlaylistIds.clear()
            selectedPlaylistIds.addAll(ids.filterNotNull())
            notifyDataSetChanged()
        }


        fun clearSelection() {
            selectedPlaylistIds.clear()
            notifyDataSetChanged()
        }

    }
