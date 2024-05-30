package com.hugo.meow.ui

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hugo.imagepreviewer.utils.AppDatabase
import com.hugo.imagepreviewer.utils.ImageEntity
import com.hugo.imagepreviewer.utils.LocalImageEntity
import com.hugo.meow.data.NetworkService
import com.hugo.meow.model.MeowPicture
import kotlinx.coroutines.async
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


    private fun meowPicToImageEntity(meowPic: MeowPicture): ImageEntity {
        return ImageEntity(
            id = meowPic.id,
            url = meowPic.url,
            width = meowPic.width,
            height = meowPic.height,
        )
    }

    /**
     * 获取图片列表，然后插入数据库
     */
    private fun getMeowPhotos() = viewModelScope.async {
        meowUiState = MeowUiState.Loading
        try {
            val listResult = myService.getMeowPicture(5)
            database.imageDao()
                .insertAll(listResult.map { meowPic -> meowPicToImageEntity(meowPic) }) // 插入数据库
            for (meowPicture in listResult) {
                // 下载图片, 将路径存入数据库
                val image = myService.downloadImage(meowPicture.url, meowPicture.id)
                if (image != null) {
                    database.localImageDao().insert(
                        LocalImageEntity(
                            id = image.name,
                            path = image.absolutePath,
                            width = meowPicture.width,
                            height = meowPicture.height
                        )
                    )
                }
            }
            loadingStatus.postValue(listResult.toString())
            Log.d("viewModel", "getMeowPhotos try success")
            MeowUiState.Success
            listResult
        } catch (e: Exception) {
            Log.e("viewModel", "getMeowPhotos try fail cuz $e")
            MeowUiState.Error
            emptyList<MeowPicture>()
        }
    }


    /**
     * 清除数据库所有图片资料
     */
    private fun CleanDatabase() = viewModelScope.async {
        viewModelScope.launch {
            database.imageDao().deleteAllSync() // 先清除旧记录（for debug)
            Log.d("viewModel", "Deleted all")
        }
    }

    /**
     * 从数据库加载所有图片,透过 [meowPics] 暴露给 UI
     */
    private fun loadAllPhotos() = viewModelScope.async {
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

    fun loadAllPhotosFromLocal() {
        viewModelScope.launch {
            // 显示加载状态
            meowUiState = MeowUiState.Loading
            // 从数据库获取本地图片列表，储存到 [meowPictures]，然后透过 meowPics 暴露给 UI
            meowUiState = try {
                val localImages = database.localImageDao().getAll()
                Log.d("viewModel", localImages.toString())
                val meowPictures = localImages.map { localImageEntity ->
                    MeowPicture(
                        id = localImageEntity.id,
                        url = localImageEntity.path,
                        width = localImageEntity.width,
                        height = localImageEntity.height
                    )
                }
                Log.d("viewModel", "loadAllPhotosFromLocal try success")
                meowPics = meowPictures
                // 更新 UI 状态
                MeowUiState.Success
            } catch (e: Exception) {
                Log.e("viewModel", "Failed to load Local photos from database: $e")
                MeowUiState.Error
            }
        }
    }


    /**
     * 获取图片列表，然后插入数据库，最后加载数据库所有图片
     */
    fun getMeowPhotosAndLoadAll() {
        viewModelScope.launch {
//            // 清理现有储存，避免一次请求太多
//            CleanDatabase().await()
            // 获取图片列表，然后下载到本地，并记录到数据库
            val pictures = getMeowPhotos().await()
            pictures.forEach { picture ->
                val file = myService.downloadImage(picture.url, "${picture.id}.jpg")
                if (file != null) {
                    Log.d("MeowViewModel", "Image downloaded: ${file.absolutePath}")
                } else {
                    Log.e("MeowViewModel", "Failed to download image: ${picture.url}")
                }
            }
            loadAllPhotos().await()
        }
    }
}