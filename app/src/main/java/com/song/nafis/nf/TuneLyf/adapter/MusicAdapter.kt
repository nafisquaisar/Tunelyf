package com.song.nafis.nf.TuneLyf.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.song.nafis.nf.TuneLyf.Model.MusicDetail
import com.song.nafis.nf.TuneLyf.Model.UnifiedMusic
import com.song.nafis.nf.TuneLyf.Model.formateDuration
import com.song.nafis.nf.TuneLyf.Model.toUnifiedMusic
import com.song.nafis.nf.TuneLyf.databinding.MusicLayoutBinding
import com.song.nafis.nf.TuneLyf.R

class MusicAdapter(
    private val context: Context,
    private var list: ArrayList<MusicDetail>,
    private val playlistDetail: Boolean = false,
    private val selection: Boolean = false,
    private val onItemClick: ((Int) -> Unit)? = null // ✅ NEW: Callback for item click
) : RecyclerView.Adapter<MusicViewHolder>() {

    private var currentList: List<MusicDetail> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        return MusicViewHolder(
            MusicLayoutBinding.inflate(
                LayoutInflater.from(context), parent, false
            )
        )
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        val song = list[position]
        holder.title.text = song.musicTitle
        holder.album.text = song.musicAlbum
        holder.durationMs.text = formateDuration(song.duration)

        Glide.with(context)
            .load(song.imgUri)
            .apply(RequestOptions().placeholder(R.mipmap.music_icon).centerCrop())
            .into(holder.img)

        when {
            playlistDetail -> {
                holder.root.setOnClickListener {
                    onItemClick?.invoke(position)
                }
            }

            selection -> {
                holder.root.setOnClickListener {
//                    if (addMusic(song)) {
//                        holder.root.setBackgroundColor(ContextCompat.getColor(context, R.color.icon_color))
//                    } else {
//                        holder.root.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
//                    }
           }
            }

            else -> {
                holder.root.setOnClickListener {
                    onItemClick?.invoke(position)
                }
            }
        }
    }

    fun updateMusicList(searchList: ArrayList<MusicDetail>) {
        list = ArrayList(searchList)
        currentList = list
        notifyDataSetChanged()
    }


//    fun addMusic(song: MusicDetail): Boolean {
//        val playlist = PlayList.musicPlaylist.ref[PlaylistMusicListActivity.currentPlaylistPos].playlist
//        playlist.forEachIndexed { index, musicDetail ->
//            if (song.musicId == musicDetail.musicId) {
//                playlist.removeAt(index)
//                return false
//            }
//        }
//        playlist.add(song)
//        return true
//    }

//    fun refreshList() {
//        list = PlayList.musicPlaylist.ref[PlaylistMusicListActivity.currentPlaylistPos].playlist
//        notifyDataSetChanged()
//    }
    fun getCurrentUnifiedList(): List<UnifiedMusic> {
        return currentList.map { it.toUnifiedMusic() }
    }

    fun getMusicAt(index: Int): MusicDetail {
        if (index !in currentList.indices) {
            Log.e("MusicAdapter", "Invalid click: index $index, list size ${currentList.size}")
            throw IndexOutOfBoundsException("Invalid index $index for music list of size ${currentList.size}")
        }
        return currentList[index]
    }



}
