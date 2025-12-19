package com.song.nafis.nf.TuneLyf.UI

import android.widget.Toast
import androidx.lifecycle.*
import com.song.nafis.nf.TuneLyf.Cache.TrackRoomCache
import com.song.nafis.nf.TuneLyf.Model.UnifiedMusic
import com.song.nafis.nf.TuneLyf.Repository.AudiusRepository
import com.song.nafis.nf.TuneLyf.resource.Resource
import com.song.nafis.nf.TuneLyf.Model.toUnifiedMusic
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AudiusViewModel @Inject constructor(
    private val repository: AudiusRepository
) : ViewModel() {

    private val _tracksResource = MutableLiveData<Resource<List<UnifiedMusic>>>()
    val tracksResource: LiveData<Resource<List<UnifiedMusic>>> = _tracksResource

    private val _updatedTrack = MutableLiveData<UnifiedMusic>()
    val updatedTrack: LiveData<UnifiedMusic> = _updatedTrack

    fun search(query: String, limit: Int = 20) {
        viewModelScope.launch {
            _tracksResource.value = Resource.Loading
            val result = repository.search(query, limit)
            if (result.isNotEmpty()) {
                _tracksResource.value = Resource.Success(result)
            } else {
                _tracksResource.value = Resource.Error("No tracks found")
            }
        }
    }

    suspend fun getStreamUrl(trackId: String): String? {
        val list = (tracksResource.value as? Resource.Success)?.data ?: return null
        val track = list.find { it.musicId == trackId } ?: return null
        if (!track.musicPath.isNullOrBlank()) {
            Timber.d("📦 STREAM URL FROM DB → ${track.musicId}")
            return track.musicPath
        }
        Timber.d("🌐 API CALL → stream for ${track.musicId}")

        return repository.getStreamUrl( track)
    }

    fun playTrack(
        track: UnifiedMusic,
        titleKey: String,
        onUrlReady: (String?) -> Unit
    ) {
        viewModelScope.launch {

            val url = repository.getStreamUrl(track)

            if (url.isNullOrBlank()) {
                Timber.e("❌ Play failed, retry allowed: ${track.musicId}")
                onUrlReady(null)
                return@launch
            }

            _updatedTrack.postValue(track.copy(musicPath = url))
            onUrlReady(url)

            // prefetch next
            val list = (tracksResource.value as? Resource.Success)?.data ?: return@launch
            val index = list.indexOfFirst { it.musicId == track.musicId }
            repository.prefetchNext(titleKey, index, list)
        }
    }


    fun loadNextPage(query: String) {
        viewModelScope.launch {
            val newItems = repository.loadNextPage(query, 10)
            if (newItems.isNotEmpty()) {
                _tracksResource.value = Resource.Success(newItems)
            }
        }
    }


    fun loadTrending() {
        viewModelScope.launch {
            _tracksResource.value = Resource.Loading
            val result = repository.loadTrending()
            if (result.isNotEmpty()) {
                _tracksResource.value = Resource.Success(result)
            } else {
                _tracksResource.value = Resource.Error("No trending songs")
            }
        }

    }


    fun loadNewUploads() {
        viewModelScope.launch {
            _tracksResource.value = Resource.Loading
            val result = repository.loadNewUploads()
            if (result.isNotEmpty()) {
                _tracksResource.value = Resource.Success(result)
            } else {
                _tracksResource.value = Resource.Error("No new uploads")
            }
        }
    }



    fun fetchSuggestions(query: String, onResult: (List<String>) -> Unit) {
        viewModelScope.launch {
            if (query.length < 3) {
                onResult(emptyList())
                return@launch
            }

            Timber.d("🔍 fetchSuggestions called with = $query")



            val result = repository.search(query, limit = 7)
            Timber.d("📦 suggestion result size = ${result.size}")

            val titles = result
                .mapNotNull { it.musicTitle }
                .distinct()

            onResult(titles)
        }
    }



}
