package com.song.nafis.nf.TuneLyf.Model

import com.song.nafis.nf.TuneLyf.ApiModel.AudiusTrack
import com.song.nafis.nf.TuneLyf.ApiModel.JamendoTrack
import com.song.nafis.nf.TuneLyf.Entity.CachedTrackEntity
import com.song.nafis.nf.TuneLyf.Entity.FavoriteEntity
import com.song.nafis.nf.TuneLyf.Entity.PlaylistSongEntity
import com.song.nafis.nf.TuneLyf.Entity.RecentlyPlayedEntity

// Convert from MusicDetail (local) to UnifiedMusic
fun MusicDetail.toUnifiedMusic(): UnifiedMusic {
    return UnifiedMusic(
        musicId = musicId,
        musicTitle = musicTitle,
        musicAlbum = musicAlbum,
        musicArtist = musicArtist,
        musicPath = musicPath,
        duration = duration,
        imgUri = imgUri,
        isLocal = true
    )
}

// Convert from JamendoTrack (online) to UnifiedMusic
fun JamendoTrack.toUnifiedMusic(): UnifiedMusic {
    return UnifiedMusic(
        musicId = id,
        musicTitle = name,
        musicAlbum = albumName,
        musicArtist = artistName,
        musicPath = audio,
        duration = duration.toLong() * 1000,  // Assuming Jamendo gives seconds
        imgUri = image,
        isLocal = false
    )
}

// Convert UnifiedMusic (for both) to JamendoTrack
fun UnifiedMusic.toJamendoTrack(): JamendoTrack {
    return JamendoTrack(
        id = this.musicId,
        name = this.musicTitle,
        albumName = this.musicAlbum ?: "Unknown",
        artistName = this.musicArtist ?: "Unknown",
        audio = this.musicPath ?: "",
        duration = (this.duration / 1000).toInt(),
        image = this.imgUri ?: ""
    )
}

fun MusicDetail.toFavoriteEntity(): FavoriteEntity {
    return FavoriteEntity(
        id = musicId,
        title = musicTitle,
        artist = musicArtist,
        album = musicAlbum,
        artworkUrl = imgUri,
        durationMs = duration,
        audioUrl = musicPath,
        isLocal = true
    )
}

fun JamendoTrack.toFavoriteEntity(): FavoriteEntity {
    return FavoriteEntity(
        id = id,
        title = name,
        artist = artistName,
        album = albumName,
        artworkUrl = image,
        durationMs = duration * 1000L,  // Convert seconds to milliseconds
        audioUrl = audio,
        isLocal = false
    )
}
fun FavoriteEntity.toUnifiedMusic(): UnifiedMusic {
    return UnifiedMusic(
        musicId = id,
        musicTitle = title,
        musicArtist = artist,
        musicAlbum = album,
        imgUri = artworkUrl,
        duration = durationMs,
        musicPath = audioUrl,
        isLocal = isLocal
    )
}

fun FavoriteEntity.toJamendoTrack(): JamendoTrack {
    return JamendoTrack(
        id = this.id,
        name = this.title,
        artistName = this.artist,
        audio = this.audioUrl, // ✅ FIX: use actual audio URL
        albumName = this.album,
        image = this.artworkUrl,
        duration = (this.durationMs / 1000).toInt() // seconds
    )
}



// Helper: "mm:ss" string to seconds
private fun parseDuration(durationStr: String): Int {
    return durationStr.split(":").let {
        val min = it.getOrNull(0)?.toIntOrNull() ?: 0
        val sec = it.getOrNull(1)?.toIntOrNull() ?: 0
        min * 60 + sec
    }
}

fun UnifiedMusic.toPlaylistSongEntity(playlistId: String): PlaylistSongEntity {
    return PlaylistSongEntity(
        playlistId = playlistId,
        songId = this.musicId,
        title = this.musicTitle,
        artist = this.musicArtist,
        album = this.musicAlbum,
        image = this.imgUri,
        audioUrl = this.musicPath,
        duration = this.duration,
        isLocal = this.isLocal,
        addedAt = System.currentTimeMillis()
    )
}
// Add this to your ModelConverters.kt or similar file
fun PlaylistSongEntity.toUnifiedMusic(): UnifiedMusic {
    return UnifiedMusic(
        musicId = songId,
        musicTitle = title,
        musicAlbum = album,
        musicArtist = artist,
        musicPath = audioUrl,
        duration = duration,
        imgUri = image,
        isLocal = isLocal
    )
}

fun AudiusTrack.toUnifiedMusic(): UnifiedMusic {
    return UnifiedMusic(
        musicId = this.id,
        musicTitle = this.title,
        musicArtist = this.user.name ?: this.user.handle ?: "Unknown Artist",
        musicAlbum = this.album?:"",
        musicPath = this.streamUrl ?: "",
        duration = this.duration * 1000L,
        imgUri = this.artwork?.url ?: "",
        isLocal = false
    )
}

fun UnifiedMusic.toCachedEntity(query: String): CachedTrackEntity {
    return CachedTrackEntity(
        id = musicId,
        searchKey = query,
        title = musicTitle,
        artist = musicArtist,
        album = musicAlbum,
        duration = duration,
        streamUrl = musicPath,
        imageUrl = imgUri,
        isLocal = isLocal
    )
}
fun CachedTrackEntity.toUnifiedMusic(): UnifiedMusic {
    return UnifiedMusic(
        musicId = id,
        musicTitle = title,
        musicArtist = artist,
        musicAlbum = album,
        duration = duration,
        musicPath = streamUrl,
        imgUri = imageUrl,
        isLocal = isLocal
    )
}

fun UnifiedMusic.toRecentlyPlayedEntity(): RecentlyPlayedEntity {
    return RecentlyPlayedEntity(
        songId = musicId,
        title = musicTitle,
        artist = musicArtist,
        album = musicAlbum,
        image = imgUri,
        audioUrl = musicPath,
        duration = duration,
        isLocal = isLocal,
        playedAt = System.currentTimeMillis()
    )
}

fun RecentlyPlayedEntity.toUnifiedMusic(): UnifiedMusic {
    return UnifiedMusic(
        musicId = songId,
        musicTitle = title?:"",
        musicArtist = artist?:"",
        musicAlbum = album?:"",
        imgUri = image?:"",
        musicPath = audioUrl?:"",
        duration = duration,
        isLocal = isLocal
    )
}





