package com.hugo.meow.data

import android.util.Log
import com.hugo.imagepreviewer.utils.AppDatabase
import com.hugo.imagepreviewer.utils.ImageEntity
import com.hugo.meow.model.MeowPicture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

class NetworkService(private val database: AppDatabase) {
    private val client: OkHttpClient = OkHttpClient().newBuilder().build()

    suspend fun getMeowPicture(count: Int): List<MeowPicture> {
        return withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url("https://api.thecatapi.com/v1/images/search?limit=$count")
                .build()

            // 执行请求并获取响应
            client.newCall(request).execute().use { response ->
                // 检查响应是否成功
                if (response.isSuccessful) {
                    // 获取响应体并将其转换为字符串
                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        // 解析 JSON 字符串为 List<MeowPicture>
                        val decodeRes = Json.decodeFromString<List<MeowPicture>>(responseBody)
                        Log.i("request", "$decodeRes")

                        // Insert the fetched images into the database
                        val imageEntities = decodeRes.map { meowPicture ->
                            ImageEntity(
                                id = meowPicture.id,
                                url = meowPicture.url,
                                width = meowPicture.width,
                                height = meowPicture.height
                            )
                        }
                        database.imageDao().insertAll(imageEntities)
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
}