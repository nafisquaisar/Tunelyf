package com.song.nafis.nf.TuneLyf.Model

import android.media.MediaMetadataRetriever
import java.util.concurrent.TimeUnit

data class MusicDetail(
    val musicId: String = "",
    val musicTitle: String = "",
    val musicAlbum: String = "",
    val musicArtist: String = "",
    val musicPath: String = "",
    val duration: Long = 0,
    val imgUri: String = "",
    val id: String
)

class Playlistdata {
    lateinit var name: String
    lateinit var playlist: ArrayList<MusicDetail>
    lateinit var createdOn: String
}

class MusicPlaylist {
    var ref: ArrayList<Playlistdata> = ArrayList()
}

fun formateDuration(duration: Long): String {
    val minutes = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
    val seconds = (TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS) -
            minutes * TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES))
    return String.format("%02d:%02d", minutes, seconds)
}

fun getImgPath(path: String): ByteArray? {
    val retrieve = MediaMetadataRetriever()
    retrieve.setDataSource(path)
    return retrieve.embeddedPicture
}

//fun setSongPosition(increment: Boolean) {
//        PlaymusicList.songPosition = if (increment) {
//            (PlaymusicList.songPosition + 1) % PlaymusicList.musicPlayList.size
//        } else {
//            if (PlaymusicList.songPosition == 0) PlaymusicList.musicPlayList.size - 1
//            else PlaymusicList.songPosition - 1
//        }
//}

//fun stopApplication() {
//    PlaymusicList.musicService?.let {
//        it.stopForeground(true)
//        it.mediaPlayer?.release()
//        PlaymusicList.musicService = null
//    }
//    exitProcess(1)
//}

//fun favoriteCheker(id: String): Int {
//    PlaymusicList.isFav = false
//    FavoriteMusic.musicListfb.forEachIndexed { index, musicDetail ->
//        if (id == musicDetail.musicId) {
//            PlaymusicList.isFav = true
//            return index
//        }
//    }
//    return -1
//}
//
//fun checkPlaylist(playlist:ArrayList<MusicDetail>):ArrayList<MusicDetail>{
//       playlist.forEachIndexed { index, musicDetail ->
//           val file=File(musicDetail.musicPath)
//           if(!file.exists()){
//               playlist.removeAt(index)
//           }
//       }
//    return playlist
//}
