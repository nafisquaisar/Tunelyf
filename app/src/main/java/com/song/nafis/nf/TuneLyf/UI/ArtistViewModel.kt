package com.song.nafis.nf.TuneLyf.UI

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.song.nafis.nf.TuneLyf.ApiModel.Artist
import com.song.nafis.nf.TuneLyf.resource.Resource


class ArtistViewModel : ViewModel() {

    private val _artists = MutableLiveData<Resource<List<Artist>>>()
    val artists: LiveData<Resource<List<Artist>>> = _artists
//
//    fun searchArtist(query: String) {
//        viewModelScope.launch {
//            _artists.value = Resource.Loading
//            try {
//                val response = repository.searchArtist(query)
//                if (response.isSuccessful) {
//                    val artistList = response.body()?.results?.artists ?: emptyList()
//                    _artists.value = Resource.Success(artistList)
//                } else {
//                    _artists.value = Resource.Error("Failed to fetch artists: ${response.message()}")
//                    Log.e("ArtistAPI", "Error: ${response.code()} - ${response.message()}")
//                }
//
//            } catch (e: Exception) {
//                _artists.value = Resource.Error(e.message ?: "Error occurred")
//            }
//        }
//    }
}
