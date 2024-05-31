package com.hugo.meow.ui

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hugo.imagepreviewer.utils.AppDatabase
import com.hugo.imagepreviewer.utils.DownloadRecordEntity
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

    var downloadRecords: List<DownloadRecordEntity> by mutableStateOf(emptyList())
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
        // 设置状态为 Loading
        meowUiState = MeowUiState.Loading
        try {
            val listResult = myService.getMeowPicture(_requestCount.value)
            meowUiState = MeowUiState.Success
            // 利用 async 创建一个协程任务，并使用 awaitAll 等待所有任务完成
            val downloadJob = listResult.map { listItem ->
                async {
                    val image = myService.downloadImage(listItem.path, listItem.id)
                    if (image != null) {
                        // 记录到本地图片数据库和下载记录数据库
                        database.localImageDao().insert(
                            LocalImageEntity(
                                id = image.name,
                                path = image.absolutePath,
                                width = listItem.width,
                                height = listItem.height
                            )
                        )
                        database.downloadRecordDao().insert(
                            DownloadRecordEntity(
                                id = listItem.id,
                                url = listItem.path,
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
            // 获取图片列表，然后下载到本地，并记录到数据库
            val pictures = getMeowPhotos().await()
        }
    }

    /**
     * 获取数据库中的下载记录
     */
    fun loadDownloadRecords() {
        viewModelScope.launch {
            try {
                val records = database.downloadRecordDao().getAll()
                downloadRecords = records
                Log.d("viewModel", "Load download record success: $downloadRecords")
            } catch (e: Exception) {
                Log.e("viewModel", "Failed to load download records: $e")
            }
        }
    }
}