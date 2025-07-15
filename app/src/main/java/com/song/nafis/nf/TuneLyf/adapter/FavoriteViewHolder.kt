package com.song.nafis.nf.TuneLyf.adapter

import androidx.recyclerview.widget.RecyclerView
import com.song.nafis.nf.TuneLyf.databinding.FavItemLayoutBinding

class FavoriteViewHolder(
    var binding: FavItemLayoutBinding
):RecyclerView.ViewHolder(binding.root) {
        val musictitle=binding.songName
        val musicImg=binding.songimg
      val root=binding.root
}
