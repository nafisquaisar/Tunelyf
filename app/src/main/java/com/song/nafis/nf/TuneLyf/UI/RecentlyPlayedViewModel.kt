    package com.song.nafis.nf.TuneLyf.UI

    import androidx.lifecycle.LiveData
    import androidx.lifecycle.MutableLiveData
    import androidx.lifecycle.ViewModel
    import androidx.lifecycle.viewModelScope
    import com.song.nafis.nf.TuneLyf.Model.UnifiedMusic
    import com.song.nafis.nf.TuneLyf.Repository.RecentlyPlayedRepository
    import dagger.hilt.android.lifecycle.HiltViewModel
    import kotlinx.coroutines.launch
    import javax.inject.Inject

    @HiltViewModel
    class RecentlyPlayedViewModel @Inject constructor(
        private val repository: RecentlyPlayedRepository
    ) : ViewModel() {

        private val _recentlyPlayed = MutableLiveData<List<UnifiedMusic>>()
        val recentlyPlayed: LiveData<List<UnifiedMusic>> = _recentlyPlayed



        fun loadRecentlyPlayed(limit: Int = 100) {
            viewModelScope.launch {
                val recentList = repository.getRecentlyPlayed(limit)
                _recentlyPlayed.postValue(recentList)
            }
        }

        fun addToRecentlyPlayed(song: UnifiedMusic) {
            viewModelScope.launch {
                repository.addToRecentlyPlayed(song)
            }
        }
    }
