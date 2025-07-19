package com.song.nafis.nf.TuneLyf.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.song.nafis.nf.TuneLyf.Entity.PlaylistEntity
import com.song.nafis.nf.TuneLyf.Entity.PlaylistSongEntity
import com.song.nafis.nf.TuneLyf.R
import com.song.nafis.nf.TuneLyf.databinding.ItemPlaylistBinding


class PlaylistsAdapter(
    private var playlists: List<PlaylistEntity>,
    private val onPlaylistClick: (PlaylistEntity) -> Unit,
    private val onDeleteClick: (PlaylistEntity) -> Unit,
    private val onUpdateClick: (PlaylistEntity) -> Unit,
    private val loadSongs: (String, (List<PlaylistSongEntity>) -> Unit) -> Unit  // ✅ Add this

) : RecyclerView.Adapter<PlaylistsAdapter.PlaylistViewHolder>() {

    inner class PlaylistViewHolder(val binding: ItemPlaylistBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val binding = ItemPlaylistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlaylistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = playlists[position]
        with(holder.binding) {
            tvPlaylistTitle.text = playlist.name
            tvPlaylistCount.text = "${playlist.songCount} songs"



            val playlistIcon = holder.binding.playlistIcon
            val context = holder.itemView.context
            val playlistId = playlist.playlistId ?: return

            loadSongs(playlistId) { songs ->
                if (songs.isNotEmpty()) {

                    // ❌ No padding when default icon is used
                    playlistIcon.setPadding(0, 0, 0, 0)
                    Glide.with(context)
                        .load(songs.first().image)
                        .placeholder(R.mipmap.logo_round)
                        .transform(RoundedCorners(20))
                        .into(playlistIcon)
                } else {

                    // ✅ Set padding when real image is used
                    playlistIcon.setPadding(20, 20, 20, 20)  // Adjust padding as needed
                    Glide.with(context)
                        .load(R.mipmap.logo_round)
                        .placeholder(R.mipmap.logo_round)
                        .transform(RoundedCorners(20))
                        .into(playlistIcon)
                }
            }


            root.setOnClickListener {
                onPlaylistClick(playlist)
            }

            imgMore.setOnClickListener { view ->
                val bottomSheetDialog = BottomSheetDialog(view.context)
                val sheetView = LayoutInflater.from(view.context)
                    .inflate(R.layout.playlist_options_bottom_sheet, null)
                bottomSheetDialog.setContentView(sheetView)

                val btnUpdate = sheetView.findViewById<TextView>(R.id.btnUpdate)
                val btnDelete = sheetView.findViewById<TextView>(R.id.btnDelete)

                btnUpdate.setOnClickListener {
                    bottomSheetDialog.dismiss()
                    onUpdateClick(playlist)
                }

                btnDelete.setOnClickListener {
                    bottomSheetDialog.dismiss()
                    onDeleteClick(playlist)
                }

                bottomSheetDialog.show()
            }
        }
    }

    override fun getItemCount(): Int = playlists.size

    fun updateList(newList: List<PlaylistEntity>) {
        playlists = newList
        notifyDataSetChanged()
    }
}

