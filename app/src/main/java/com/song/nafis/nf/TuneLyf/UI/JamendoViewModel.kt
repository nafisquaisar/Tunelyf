package com.song.nafis.nf.TuneLyf.UI

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.song.nafis.nf.TuneLyf.Model.UnifiedMusic
import com.song.nafis.nf.TuneLyf.Model.toUnifiedMusic
import com.song.nafis.nf.TuneLyf.Repository.JamendoRepository
import com.song.nafis.nf.TuneLyf.resource.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JamendoViewModel @Inject constructor(
    private val repository: JamendoRepository
) : ViewModel() {

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _tracks = MutableLiveData<Resource<List<UnifiedMusic>>>()
    val tracks: LiveData<Resource<List<UnifiedMusic>>> = _tracks

    fun search(query: String) {
        viewModelScope.launch {
            _tracks.postValue(Resource.Loading)
            try {
                val result = repository.searchTracks(query)
                if (result != null) {
                    // Convert List<JamendoTrack> to List<UnifiedMusic>
                    val unifiedList = result.map { it.toUnifiedMusic() }
                    _tracks.postValue(Resource.Success(unifiedList))
                } else {
                    _tracks.postValue(Resource.Error("No results found"))
                }
            } catch (e: Exception) {
                _tracks.postValue(Resource.Error(e.message ?: "Unknown error"))
            }
        }
    }
}
