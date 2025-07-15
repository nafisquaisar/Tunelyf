package com.song.nafis.nf.TuneLyf.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.song.nafis.nf.TuneLyf.Model.UnifiedMusic
import com.song.nafis.nf.TuneLyf.R
import com.song.nafis.nf.TuneLyf.databinding.MusicLayoutBinding
import com.song.nafis.nf.TuneLyf.databinding.MusicitemsquareBinding
import com.song.nafis.nf.TuneLyf.Model.formateDuration

class UnifiedMusicAdapter(
    private val context: Context,
    private var songs: List<UnifiedMusic>,
    private val isSelectionMode: Boolean = false,
    private val onSongClick: ((Int) -> Unit)? = null,
    private val isSquareLayout: Boolean = false // 🆕 Flag to control layout
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SQUARE = 1
        private const val VIEW_TYPE_LIST = 2
    }

    private val selectedItems = mutableSetOf<String>()

    override fun getItemViewType(position: Int): Int {
        return if (isSquareLayout) VIEW_TYPE_SQUARE else VIEW_TYPE_LIST
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SQUARE) {
            val binding = MusicitemsquareBinding.inflate(LayoutInflater.from(context), parent, false)
            SquareViewHolder(binding)
        } else {
            val binding = MusicLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
            ListViewHolder(binding)
        }
    }

    override fun getItemCount(): Int = songs.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val song = songs[position]
        when (holder) {
            is SquareViewHolder -> holder.bind(song)
            is ListViewHolder -> holder.bind(song, position)
        }
    }

    // ViewHolder for List Layout (full row)
    inner class ListViewHolder(val binding: MusicLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(song: UnifiedMusic, position: Int) {
            binding.musicName.text = song.musicTitle
            binding.singerName.text = if (!song.musicAlbum.isNullOrBlank()) {
                song.musicAlbum
            } else {
                "Unknown Album"
            }
            binding.songDuration.text = formateDuration(song.duration)

            Glide.with(context)
                .load(song.imgUri)
                .placeholder(R.mipmap.music_icon)
                .centerCrop()
                .into(binding.musicphoto)

            // Highlight if selected
            val isSelected = selectedItems.contains(song.musicId)
            val bgColor = if (isSelected) R.color.icon_color else R.color.white
            val textColor = if (isSelected) R.color.white else R.color.black

            binding.root.setBackgroundColor(ContextCompat.getColor(context, bgColor))
            binding.musicName.setTextColor(ContextCompat.getColor(context, textColor))
            binding.singerName.setTextColor(ContextCompat.getColor(context, textColor))
            binding.songDuration.setTextColor(ContextCompat.getColor(context, textColor))

            binding.root.setOnClickListener {
                if (isSelectionMode) {
                    toggleSelection(song.musicId)
                    notifyItemChanged(position)
                } else {
                    onSongClick?.invoke(position)
                }
            }
        }
    }

    // ViewHolder for Square Layout
    inner class SquareViewHolder(val binding: MusicitemsquareBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(song: UnifiedMusic) {
            binding.musicTitle.text = song.musicTitle
            binding.musicArtist.text = song.musicAlbum

            Glide.with(context)
                .load(song.imgUri)
                .placeholder(R.mipmap.music_icon)
                .centerCrop()
                .into(binding.musicCover)

            binding.root.setOnClickListener {
                onSongClick?.invoke(adapterPosition)
            }
        }
    }

    private fun toggleSelection(musicId: String) {
        if (selectedItems.contains(musicId)) {
            selectedItems.remove(musicId)
        } else {
            selectedItems.add(musicId)
        }
    }

    fun getSelectedSongs(): List<UnifiedMusic> {
        return songs.filter { selectedItems.contains(it.musicId) }
    }

    fun updateSongs(newSongs: List<UnifiedMusic>) {
        selectedItems.clear()
        songs = newSongs
        notifyDataSetChanged()
    }

    fun clearSelection() {
        selectedItems.clear()
        notifyDataSetChanged()
    }

    val selectedCount: Int
        get() = selectedItems.size

    val isAnyItemSelected: Boolean
        get() = selectedItems.isNotEmpty()

    val allSongs: List<UnifiedMusic>
        get() = songs
}
