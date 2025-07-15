package com.song.nafis.nf.TuneLyf.UI

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
    private val repository: AudiusRepository,
    private val trackRoomCache: TrackRoomCache
) : ViewModel() {

    private val _tracksResource = MutableLiveData<Resource<List<UnifiedMusic>>>()
    val tracksResource: LiveData<Resource<List<UnifiedMusic>>> = _tracksResource

    private val _suggestions = MutableLiveData<List<String>>()
    val suggestions: LiveData<List<String>> = _suggestions

    private val _queryFlow = MutableSharedFlow<String>(replay = 0)

    private val _updatedTrack = MutableLiveData<UnifiedMusic>()
    val updatedTrack: LiveData<UnifiedMusic> = _updatedTrack

    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var currentOffset = 0
    private val pageSize = 10
    private var isLastPage = false
    private var isLoading = false

    init {
        observeSearchSuggestions()
    }

    private fun observeSearchSuggestions() {
        viewModelScope.launch {
            _queryFlow
                .debounce(300)
                .distinctUntilChanged()
                .collect { query -> fetchSuggestions(query) }
        }
    }

    fun onQueryChanged(query: String) {
        viewModelScope.launch {
            _queryFlow.emit(query)
        }
    }

    private suspend fun fetchSuggestions(query: String) {
        try {
            val results = repository.searchTracks(query)
            val titles = results?.mapNotNull { it.title }?.distinct() ?: emptyList()
            _suggestions.postValue(titles)
        } catch (e: Exception) {
            Timber.e(e, "Suggestion fetch failed")
            _suggestions.postValue(emptyList())
        }
    }

    fun search(query: String, limit: Int = 20) {
        viewModelScope.launch {
            _tracksResource.value = Resource.Loading
            currentOffset = 0
            isLastPage = false

            try {
                val cached = trackRoomCache.getTracksForQuery(query)

                val result = if (cached.isNotEmpty()) {
                    cached
                } else {
                    val fresh = repository.searchTracks(query, limit)
                    val unified = fresh?.filterNot { it.title.isNullOrBlank() }
                        ?.map { it.toUnifiedMusic() }
                        .orEmpty()
                    trackRoomCache.saveTracks(query, unified)
                    currentOffset = unified.size
                    unified
                }

                if (result.isNotEmpty()) {
                    _tracksResource.value = Resource.Success(result)

                    val semaphore = Semaphore(5)
                    result.forEach { track ->
                        backgroundScope.launch {
                            semaphore.withPermit {
                                val cachedTrack = trackRoomCache.getTracksForQueryById(track.musicId, query)
                                if (cachedTrack?.musicPath.isNullOrEmpty()) {
                                    val streamUrl = getStreamUrl(track.musicId, query)
                                    if (!streamUrl.isNullOrEmpty()) {
                                        val updated = track.copy(musicPath = streamUrl)
                                        _updatedTrack.postValue(updated)
                                        saveSingleTrack(query, updated)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    _tracksResource.value = Resource.Error("No tracks found")
                }

            } catch (e: Exception) {
                _tracksResource.value = Resource.Error(e.message ?: "Unknown error")
            }
        }
    }

    suspend fun getStreamUrl(trackId: String, titleKey: String): String? = withContext(Dispatchers.IO) {
        val cachedTracks = trackRoomCache.getTracksForQuery(titleKey)
        val cached = cachedTracks.find { it.musicId == trackId }?.musicPath

        if (!cached.isNullOrBlank()) {
            Timber.d("Using cached stream URL for $trackId")
            return@withContext cached
        }

        try {
            withTimeout(60000L) {
                val url = repository.getStreamUrl(trackId)
                Timber.d("Fetched new stream URL for $trackId")
                return@withTimeout url
            }
        } catch (e: TimeoutCancellationException) {
            Timber.e(e, "Timeout while fetching stream URL for $trackId")
            null
        } catch (e: Exception) {
            Timber.e(e, "Error fetching stream URL for $trackId")
            null
        }
    }

    fun playTrack(track: UnifiedMusic, titleKey: String, onUrlReady: (String?) -> Unit) {
        if (!track.musicPath.isNullOrBlank()) {
            onUrlReady(track.musicPath)
        } else {
            viewModelScope.launch {
                val url = getStreamUrl(track.musicId, titleKey)
                val updated = track.copy(musicPath = url ?: "")
                _updatedTrack.postValue(updated)
                saveSingleTrack(titleKey, updated)
                onUrlReady(url)
            }
        }
    }

    private suspend fun saveSingleTrack(query: String, track: UnifiedMusic) {
        Timber.tag("update").d("💾 Saving unified track ${track.musicId} with streamUrl = ${track.musicPath}")
        val existing = trackRoomCache.getTracksForQuery(query)
        val updatedList = (existing.filterNot { it.musicId == track.musicId } + track)
            .distinctBy { it.musicId }
        trackRoomCache.saveTracks(query, updatedList)
    }

    fun loadCachedTracks(query: String) {
        viewModelScope.launch {
            val cached = trackRoomCache.getTracksForQuery(query)
            cached.forEach {
                Timber.tag("update").d("📦 Cached: ${it.musicTitle} - ${it.musicPath}")
            }
            if (cached.isNotEmpty()) {
                _tracksResource.value = Resource.Success(cached)
            }
        }
    }

    fun loadNextPage(query: String) {
        if (isLoading || isLastPage) return
        isLoading = true

        viewModelScope.launch {
            _tracksResource.value = Resource.Loading

            try {
                val cached = trackRoomCache.getTracksForQuery(query)
                val newTracks = repository.searchTracks(query, limit = pageSize).orEmpty()

                val newUnified = newTracks.map { it.toUnifiedMusic() }
                val newUnique = newUnified.filterNot { u -> cached.any { it.musicId == u.musicId } }

                if (newUnique.isEmpty()) {
                    isLastPage = true
                    _tracksResource.value = Resource.Error("No more unique tracks found")
                } else {
                    val merged = (cached + newUnique).distinctBy { it.musicId }
                    trackRoomCache.saveTracks(query, merged)
                    currentOffset += newUnique.size
                    _tracksResource.value = Resource.Success(newUnique)
                    fetchMissingStreamUrls(newUnique, query)
                    isLastPage = newUnique.size < pageSize
                }

            } catch (e: Exception) {
                Timber.e(e)
                _tracksResource.value = Resource.Error("Error loading tracks: ${e.localizedMessage}")
            }

            isLoading = false
        }
    }

    private fun fetchMissingStreamUrls(tracks: List<UnifiedMusic>, query: String) {
        val semaphore = Semaphore(5)

        tracks.forEach { track ->
            backgroundScope.launch {
                semaphore.withPermit {
                    val cached = trackRoomCache.getTracksForQueryById(track.musicId, query)

                    if (cached?.musicPath.isNullOrEmpty()) {
                        val streamUrl = getStreamUrl(track.musicId, query)
                        if (!streamUrl.isNullOrEmpty()) {
                            val updated = track.copy(musicPath = streamUrl)
                            _updatedTrack.postValue(updated)
                            saveSingleTrack(query, updated)
                        }
                    }
                }
            }
        }
    }

    fun refresh(query: String) {
        currentOffset = 0
        isLastPage = false
        loadNextPage(query)
    }

    override fun onCleared() {
        super.onCleared()
        backgroundScope.cancel()
    }
}
