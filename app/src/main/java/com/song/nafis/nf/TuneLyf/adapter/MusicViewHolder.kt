package com.song.nafis.nf.TuneLyf.adapter

import androidx.recyclerview.widget.RecyclerView
import com.song.nafis.nf.TuneLyf.databinding.MusicLayoutBinding

class MusicViewHolder(binding: MusicLayoutBinding):RecyclerView.ViewHolder(binding.root) {
     val title=binding.musicName
     val album=binding.singerName
     val img=binding.musicphoto
     val durationMs=binding.songDuration
     val root=binding.root
}
