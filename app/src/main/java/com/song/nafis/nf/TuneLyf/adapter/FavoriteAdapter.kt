//package com.song.nafis.nf.blissfulvibes.adapter
//
//import android.content.Context
//import android.content.Intent
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import androidx.core.content.ContextCompat
//import androidx.recyclerview.widget.RecyclerView
//import com.bumptech.glide.Glide
//import com.bumptech.glide.request.RequestOptions
//import com.song.nafis.nf.blissfulvibes.PlaymusicList
//import com.song.nafis.nf.blissfulvibes.R
//import com.song.nafis.nf.blissfulvibes.Model.MusicDetail
//import com.song.nafis.nf.blissfulvibes.databinding.FavItemLayoutBinding
//
//
//class FavoriteAdapter (var context: Context, private var musicListfb:ArrayList<MusicDetail>):RecyclerView.Adapter<FavoriteViewHolder>(){
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
//         val view =FavItemLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)
//         return FavoriteViewHolder(view)
//    }
//
//    override fun getItemCount(): Int {
//           return musicListfb.size
//    }
//
//    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
//           holder.musictitle.text=musicListfb[position].musicTitle
//           Glide.with(context)
//               .load(musicListfb[position].imgUri)
//               .apply(  RequestOptions().placeholder(R.mipmap.music_icon).centerCrop() )
//               .into(holder.musicImg)
//           holder.musictitle.isSelected=true
//           holder.root.setOnClickListener {
//               val intent= Intent(context, PlaymusicList::class.java)
//               intent.putExtra("index",position)
//               intent.putExtra("class","FavoriteAdapter")
//               ContextCompat.startActivity(context,intent,null)
//           }
//    }
//}