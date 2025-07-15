package com.song.nafis.nf.TuneLyf.adapter

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.song.nafis.nf.TuneLyf.Model.UnifiedMusic
import com.song.nafis.nf.TuneLyf.R
import timber.log.Timber

class SongAdapter(
    private val onItemClick: (UnifiedMusic) -> Unit
) : ListAdapter<UnifiedMusic, SongAdapter.SongViewHolder>(DiffCallback) {

    private var lastPosition = -1
    private var isSelectionMode = false
    private val selectedSongs = mutableSetOf<UnifiedMusic>()

    object DiffCallback : DiffUtil.ItemCallback<UnifiedMusic>() {
        override fun areItemsTheSame(oldItem: UnifiedMusic, newItem: UnifiedMusic): Boolean {
            return oldItem.musicId == newItem.musicId
        }

        override fun areContentsTheSame(oldItem: UnifiedMusic, newItem: UnifiedMusic): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: UnifiedMusic, newItem: UnifiedMusic): Any? {
            return if (oldItem.musicPath != newItem.musicPath) {
                Bundle().apply {
                    putString("audio", newItem.musicPath)
                }
            } else null
        }
    }

    inner class SongViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val titleText: TextView = view.findViewById(R.id.musicName)
        private val artistText: TextView = view.findViewById(R.id.singerName)
        private val artworkImage: ImageView = view.findViewById(R.id.musicphoto)
        private val songDuration: TextView = view.findViewById(R.id.songDuration)

        fun bind(music: UnifiedMusic, isPartialUpdate: Boolean = false) {
            updateStreamStatus(music.musicPath, music.isLocal)

            if (!isPartialUpdate) {
                titleText.text = music.musicTitle
                artistText.text = music.musicArtist

                val totalSeconds = music.duration / 1000
                val minutes = totalSeconds / 60
                val seconds = totalSeconds % 60
                val formattedDuration = String.format("%02d:%02d", minutes, seconds)
                songDuration.text = formattedDuration


                Glide.with(itemView.context)
                    .load(music.imgUri)
                    .placeholder(R.mipmap.music_icon)
                    .into(artworkImage)

                itemView.setOnClickListener {
                    if (isSelectionMode) {
                        toggleSelection(music, this)
                    } else {
                        onItemClick(music)
                    }
                }


                setAnimation(itemView, bindingAdapterPosition)
            }
        }

        private fun updateStreamStatus(audioPath: String?, isLocal: Boolean) {
            val alphaValue = if (!isLocal && audioPath.isNullOrBlank()) 0.5f else 1.0f
            itemView.alpha = alphaValue
            Timber.tag("update").d("Track ${bindingAdapterPosition}: path = $audioPath | alpha = $alphaValue")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.music_layout, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            val payload = payloads[0] as? Bundle
            val audio = payload?.getString("audio")
            val item = getItem(position)
            if (audio != null) {
                holder.bind(item, isPartialUpdate = true)
            }
        }
    }

    private fun setAnimation(viewToAnimate: View, position: Int) {
        if (position > lastPosition) {
            val sideAnimation = AnimationUtils.loadAnimation(viewToAnimate.context, android.R.anim.slide_in_left)
            viewToAnimate.startAnimation(sideAnimation)
            lastPosition = position
        }
    }



    fun enableSelectionMode(enable: Boolean) {
        isSelectionMode = enable
        selectedSongs.clear()
        notifyDataSetChanged()
    }

    private fun toggleSelection(song: UnifiedMusic, holder: SongViewHolder) {
        if (selectedSongs.contains(song)) {
            selectedSongs.remove(song)
            holder.itemView.setBackgroundColor(Color.TRANSPARENT)
        } else {
            selectedSongs.add(song)
            holder.itemView.setBackgroundColor(Color.LTGRAY)
        }
    }

    fun getSelectedSongs(): List<UnifiedMusic> = selectedSongs.toList()

}
