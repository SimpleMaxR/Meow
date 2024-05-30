package com.hugo.meow.ui

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hugo.imagepreviewer.utils.AppDatabase
import com.hugo.meow.data.NetworkService
import com.hugo.meow.model.MeowPicture
import kotlinx.coroutines.launch


/**
 * UI state for the Home screen
 */
sealed interface MeowUiState {
    object Success : MeowUiState
    object Error : MeowUiState
    object Loading : MeowUiState
}


class MeowViewModel(service: NetworkService, private val database: AppDatabase) : ViewModel() {
    var meowUiState: MeowUiState by mutableStateOf(MeowUiState.Loading)
        private set

    var meowPics: List<MeowPicture> by mutableStateOf(emptyList())
        private set

    val loadingStatus = MutableLiveData<String>()

    private val myService = service

    init {
        getMeowPhotos()
        Log.d("viewModel", "viewModel init")
    }

    fun getMeowPhotos() {
        viewModelScope.launch {
            meowUiState = MeowUiState.Loading
            meowUiState = try {
                val listResult = myService.getMeowPicture(10)
                loadingStatus.postValue(listResult.toString())
                Log.d("viewModel", "getMeowPhotos try success")
                MeowUiState.Success
            } catch (e: Exception) {
                Log.e("viewModel", "getMeowPhotos try fail cuz $e")
                MeowUiState.Error
            }
        }
    }

    fun loadAllPhotos() {
        viewModelScope.launch {
            meowUiState = MeowUiState.Loading
            meowUiState = try {
                val images = database.imageDao().getAll()
                val meowPictures = images.map { imageEntity ->
                    MeowPicture(
                        id = imageEntity.id,
                        url = imageEntity.url,
                        width = imageEntity.width,
                        height = imageEntity.height
                    )
                }
                Log.d("viewModel", "loadAllPhotos try success")
                meowPics = meowPictures
                MeowUiState.Success
            } catch (e: Exception) {
                Log.e("viewModel", "Failed to load photos from database: $e")
                MeowUiState.Error
            }
        }
    }
}