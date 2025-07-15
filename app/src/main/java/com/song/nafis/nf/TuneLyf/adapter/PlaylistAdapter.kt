package com.song.nafis.nf.TuneLyf.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.song.nafis.nf.TuneLyf.PlayList
import com.song.nafis.nf.TuneLyf.R.*
import com.song.nafis.nf.TuneLyf.Model.Playlistdata
import com.song.nafis.nf.TuneLyf.databinding.PlaylistViewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class PlaylistAdapter(var context: Context, var playListL: ArrayList<Playlistdata>): RecyclerView.Adapter<PlaylistViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
       val view=PlaylistViewBinding.inflate(LayoutInflater.from(context),parent,false)
        return PlaylistViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.plName.text = playListL[position].name
        holder.totalsong.text = "total ${playListL[position].playlist.size} song"


        holder.morebtn.setOnClickListener { v -> showPopupMenu(v, position) }
        holder.root.setOnClickListener {
//            val intent = Intent(context, PlaylistMusicListActivity::class.java)
//            intent.putExtra("index", position)
//            ContextCompat.startActivity(context, intent, null)
        }

        // Ensure currentPlaylistPos is within bounds and the playlist is not empty
        if (position >= 0 && position < PlayList.musicPlaylist.ref.size && PlayList.musicPlaylist.ref[position].playlist.isNotEmpty()) {
            Glide.with(context)
                .load(PlayList.musicPlaylist.ref[position].playlist[0].imgUri)
                .apply(RequestOptions().placeholder(mipmap.music_icon).centerCrop())
                .into(holder.plimg)
        } else {
            holder.plimg.setImageResource(mipmap.music_icon) // Set a default image if no valid image URI
        }
    }


    override fun getItemCount(): Int {
        return playListL.size
    }

    private fun showPopupMenu(view: View,position: Int) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.inflate(menu.more_playlist)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                id.delete -> {
                    // Handle delete action
                    val builder = MaterialAlertDialogBuilder(context)
                        .setTitle("Delete ${PlayList.musicPlaylist.ref[position].name} Playlist")
                        .setMessage("Are You Sure want to Delete")
                        .setPositiveButton("Yes") { _, _ ->
                            PlayList.musicPlaylist.ref.removeAt(position)
                           Toast.makeText(view.context, "Delete Successfully", Toast.LENGTH_SHORT).show()
                            refreshPlaylist()
                        }
                        .setNegativeButton("No") { dialog, _ ->
                            dialog.dismiss()
                        }

                    val alertDialog = builder.create()
                    alertDialog.show()
                    val color = ContextCompat.getColor(context ,color.icon_color)
                    alertDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setTextColor(color)
                    alertDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(color)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }


    fun refreshPlaylist(){
        playListL= ArrayList()
        playListL.addAll(PlayList.musicPlaylist.ref)
        notifyDataSetChanged()
    }
}