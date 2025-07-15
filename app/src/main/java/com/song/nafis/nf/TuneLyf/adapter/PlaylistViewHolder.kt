package com.song.nafis.nf.TuneLyf.adapter

import androidx.recyclerview.widget.RecyclerView
import com.song.nafis.nf.TuneLyf.databinding.PlaylistViewBinding

class PlaylistViewHolder (var binding: PlaylistViewBinding) :RecyclerView.ViewHolder(binding.root){
        var plName=binding.playlistName
        var plimg=binding.plalistImg
        var root=binding.root
        var morebtn=binding.morebtn
        var totalsong=binding.playlistTotalsong
}
