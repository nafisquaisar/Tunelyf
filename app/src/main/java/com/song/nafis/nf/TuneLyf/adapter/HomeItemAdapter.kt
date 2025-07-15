package com.song.nafis.nf.TuneLyf.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.song.nafis.nf.TuneLyf.Model.HomeItemSqModel
import com.song.nafis.nf.TuneLyf.databinding.HomeItemCircularBinding
import com.song.nafis.nf.TuneLyf.databinding.HomeItemSquareBinding

class HomeItemAdapter(private val isArtistLayout: Boolean = false, private val onItemClick: ((HomeItemSqModel) -> Unit)? = null) :
    ListAdapter<HomeItemSqModel, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        private const val TYPE_SQUARE = 0
        private const val TYPE_ARTIST = 1

        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<HomeItemSqModel>() {
            override fun areItemsTheSame(oldItem: HomeItemSqModel, newItem: HomeItemSqModel) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: HomeItemSqModel, newItem: HomeItemSqModel): Boolean {
                return oldItem.name == newItem.name &&
                        oldItem.description == newItem.description &&
                        oldItem.coverResId == newItem.coverResId
            }
        }
    }

    // Determine view type based on isArtistLayout flag
    override fun getItemViewType(position: Int): Int {
        return if (isArtistLayout) TYPE_ARTIST else TYPE_SQUARE
    }

    // ViewHolder for square layout
    inner class SquareViewHolder(private val binding: HomeItemSquareBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HomeItemSqModel) {
//            binding.songNameTextView.text = item.name
//            binding.descriptionTextView.text = item.description
            binding.coverImageView.setImageResource(item.coverResId)
            binding.root.setOnClickListener {
                onItemClick?.invoke(item)
            }
        }
    }

    // ViewHolder for artist layout
    inner class ArtistViewHolder(private val binding: HomeItemCircularBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HomeItemSqModel) {
            binding.songNameTextView.text = item.name
            binding.descriptionTextView.text = item.description
            binding.coverImageView.setImageResource(item.coverResId)
            binding.root.setOnClickListener {
                onItemClick?.invoke(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_ARTIST) {
            val binding = HomeItemCircularBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ArtistViewHolder(binding)
        } else {
            val binding = HomeItemSquareBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            SquareViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        if (holder is ArtistViewHolder) {
            holder.bind(item)
        } else if (holder is SquareViewHolder) {
            holder.bind(item)
        }
    }
}
