package com.song.nafis.nf.TuneLyf.UI

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.song.nafis.nf.TuneLyf.Cache.PreloadConfig
import com.song.nafis.nf.TuneLyf.Cache.TrackRoomCache
import com.song.nafis.nf.TuneLyf.Model.UnifiedMusic
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val trackRoomCache: TrackRoomCache
) : ViewModel() {

    private val _homeSections =
        MutableLiveData<Map<String, List<UnifiedMusic>>>()
    val homeSections: LiveData<Map<String, List<UnifiedMusic>>> =
        _homeSections

    fun loadHomeSections() {
        viewModelScope.launch {
            val map = linkedMapOf<String, List<UnifiedMusic>>()

            PreloadConfig.sections.forEach { key ->
                map[key] = trackRoomCache.getTracksForQuery(key)
            }

            _homeSections.postValue(map)
        }
    }
}
