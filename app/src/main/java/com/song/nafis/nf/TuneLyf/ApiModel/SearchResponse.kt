package com.song.nafis.nf.TuneLyf.ApiModel

data class SearchResponse(
    val albums: AlbumResults,
    val songs: SongResults,
    val playlists: PlaylistResults,
    val artists: ArtistResults,
    val topquery: TopQueryResults,
    val shows: ShowResults,
    val episodes: EpisodeResults
)

data class AlbumResults(
    val data: List<Album>
)

data class SongResults(
    val data: List<Song>
)

data class PlaylistResults(
    val data: List<Playlist>
)

data class ArtistResults(
    val data: List<Artist>
)

data class TopQueryResults(
    val data: List<Song> // As per your JSON, topquery is a list of songs
)

data class ShowResults(
    val data: List<Show>
)

data class EpisodeResults(
    val data: List<Episode>
)

//data class Song(
//    val id: String,
//    val title: String,
//    val image: String,
//    val album: String,
//    val url: String,
//    val type: String,
//    val description: String,
//    val more_info: MoreInfo
//)

data class Album(
    val id: String,
    val title: String,
    val image: String,
    val music: String,
    val url: String,
    val type: String,
    val description: String,
    val more_info: MoreInfo
)

data class MoreInfo(
    val year: String,
    val language: String,
    val song_pids: String
)

data class Artist(
    val id: String,
    val name: String
)

data class Playlist(
    val id: String,
    val title: String
)

data class Show(
    val id: String,
    val name: String
)

data class Episode(
    val id: String,
    val title: String
)
