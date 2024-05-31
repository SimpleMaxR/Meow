package com.hugo.meow.ui

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hugo.imagepreviewer.utils.AppDatabase
import com.hugo.imagepreviewer.utils.LocalImageEntity
import com.hugo.meow.data.NetworkService
import com.hugo.meow.model.MeowPicture
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val myService = service

    init {
        meowUiState = MeowUiState.Success
    }

    private val _requestCount = MutableStateFlow(0)
    val requestCount: StateFlow<Int> = _requestCount.asStateFlow()



    /**
     * 获取图片列表，然后插入数据库
     */
    private fun getMeowPhotos() = viewModelScope.async {
        meowUiState = MeowUiState.Loading
        try {
            val listResult = myService.getMeowPicture(_requestCount.value)
//            database.imageDao().insertAll(listResult.map { meowPic -> meowPicToImageEntity(meowPic) }) // 插入数据库
            meowUiState = MeowUiState.Success
            val downloadJob = listResult.map { listItem ->
                async {
                    val image = myService.downloadImage(listItem.path, listItem.id)
                    if (image != null) {
                        database.localImageDao().insert(
                            LocalImageEntity(
                                id = image.name,
                                path = image.absolutePath,
                                width = listItem.width,
                                height = listItem.height
                            )
                        )
                        // 构建一个 meowPicture 并添加到 meowPics
                        val meowPic = MeowPicture(
                            id = listItem.id,
                            path = image.absolutePath,
                            width = listItem.width,
                            height = listItem.height,
                        )
                        meowPics = meowPics + meowPic
                        Log.d("viewModel", "meowPics update $meowPics")
                    }
                }
            }
            downloadJob.awaitAll()
            Log.d("viewModel", "getMeowPhotos try success")
            listResult
        } catch (e: Exception) {
            Log.e("viewModel", "getMeowPhotos try fail cuz $e")
            meowUiState = MeowUiState.Error
            emptyList<MeowPicture>()
        }
    }


    /**
     * 清除数据库所有图片资料
     */
    fun CleanDatabase() {
        viewModelScope.launch {
            database.downloadRecordDao().deleteAllSync() // 删除下载记录
            database.localImageDao().deleteAllSync() // 删除本地图片记录
            myService.clearCacheDir() // 清空文件
            meowPics = emptyList<MeowPicture>()
            Log.d("viewModel", "Deleted all")
        }
    }

    /**
     * 更新请求数量的值
     */
    fun updateRequestCount(count: Int) {
        _requestCount.value = count
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
                        path = localImageEntity.path,
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
            // 清理现有储存，避免一次请求太多
            CleanDatabase()
            // 获取图片列表，然后下载到本地，并记录到数据库
            val pictures = getMeowPhotos().await()

//            pictures.forEach { picture ->
//                val file = myService.downloadImage(picture.url, "${picture.id}.jpg")
//                if (file != null) {
//                    Log.d("MeowViewModel", "Image downloaded: ${file.absolutePath}")
//                } else {
//                    Log.e("MeowViewModel", "Failed to download image: ${picture.url}")
//                }
//            }


//            // 使用 async 并行下载图片
//            val asyncDownload = pictures.map{picture ->
//                async {
//                    val file = myService.downloadImage(picture.url, "${picture.id}.jpg")
//                    if (file != null) {
//                        Log.d("MeowViewModel", "Image downloaded: ${file.absolutePath}")
//                    } else {
//                        Log.e("MeowViewModel", "Failed to download image: ${picture.url}")
//                    }
//                }
//            }
//            asyncDownload.awaitAll()

//            loadAllPhotos().await()
        }
    }
}