package com.hugo.meow.data

import android.util.Log
import com.hugo.imagepreviewer.utils.AppDatabase
import com.hugo.meow.model.MeowPicture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class NetworkService(
    private val database: AppDatabase,
    private val appContext: android.content.Context
) {
    private val client: OkHttpClient = OkHttpClient().newBuilder().build()

    /**
     * 获取图片列表，包含图片地址和信息，返回 List<MeowPicture>
     */
    suspend fun getMeowPicture(count: Int): List<MeowPicture> {
        return withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url("https://api.thecatapi.com/v1/images/search?limit=$count&api_key=live_qpcpqD9WFsTXjfffwQxA0YEs4CNbInpvnB9zDDITvvLpXeJejdT6GwNph7YGl63F")
                .build()

            // 执行请求并获取响应
            client.newCall(request).execute().use { response ->
                // 检查响应是否成功
                if (response.isSuccessful) {
                    // 获取响应体并将其转换为字符串
                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        // 解析 JSON 字符串为 List<MeowPicture>
                        val decodeRes =
                            Json { ignoreUnknownKeys = true }.decodeFromString<List<MeowPicture>>(
                                responseBody
                            )
                        Log.i("request", "$decodeRes")
                        return@withContext decodeRes
                    } else {
                        // 处理响应体为空的情况
                        Log.e("request", "Response body is null")
                        throw Exception("Response body is null")
                    }
                } else {
                    // 处理请求失败的情况
                    Log.e("request", "error: ${response.code}")
                    throw Exception("Error: ${response.code}")
                }
            }
        }
    }

    /**
     * 从指定 url 下载图片保存到 cacheDir，以 File 格式返回储存成功的文件信息
     */
    suspend fun downloadImage(url: String, fileName: String): File? {
        Log.d("Network", "Start download $fileName from $url")
        return withContext(Dispatchers.IO) {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val file = File(appContext.cacheDir, fileName)
                val inputStream = response.body?.byteStream()
                val outputStream = FileOutputStream(file)
                inputStream?.copyTo(outputStream)
                outputStream.close()
                inputStream?.close()
                file
            } else {
                null
            }
        }
    }

    /**
     * 清除 cacheDir 所有文件
     */
    suspend fun clearCacheDir() {
        withContext(Dispatchers.IO) {
            val cacheDir = appContext.cacheDir
            cacheDir.listFiles()?.forEach { file ->
                if (file.isFile) {
                    file.delete()
                }
            }
            Log.d("Network", "Cache directory cleared")
        }
    }
}