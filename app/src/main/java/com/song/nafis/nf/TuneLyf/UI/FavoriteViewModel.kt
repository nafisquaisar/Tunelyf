package com.song.nafis.nf.TuneLyf.UI

import androidx.lifecycle.*
import com.song.nafis.nf.TuneLyf.Entity.FavoriteEntity
import com.song.nafis.nf.TuneLyf.Repository.FavoriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val repository: FavoriteRepository
) : ViewModel() {

    private val refreshTrigger = MutableLiveData(Unit) // triggers refresh

    val allFavorites: LiveData<List<FavoriteEntity>> =
        refreshTrigger.switchMap {
            repository.getAllFavorites()
        }

    fun refreshFavorites() {
        refreshTrigger.value = Unit
    }

    fun isFavorite(id: String): LiveData<Boolean> = repository.isFavorite(id)

    fun addToFavorite(song: FavoriteEntity) = viewModelScope.launch {
        repository.addFavorite(song)
    }

    fun removeFromFavorite(song: FavoriteEntity) = viewModelScope.launch {
        repository.removeFavorite(song)
    }
}

