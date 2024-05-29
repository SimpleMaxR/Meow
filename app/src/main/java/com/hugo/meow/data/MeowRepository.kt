package com.hugo.meow.data

import android.content.Context
import com.hugo.meow.model.MeowPicture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request


interface MeowRepository {
    suspend fun getMeowPicture(): List<MeowPicture>
}

class NetworkMeowRepository(private val client: OkHttpClient, context: Context) : MeowRepository {
    override suspend fun getMeowPicture(): List<MeowPicture> {
        withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url("https://api.thecatapi.com/v1/images/search?limit=10")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val decodeRes = Json.decodeFromString<List<MeowPicture>>(response.toString())
                } else {
                    throw Exception("Error")
                }
            }
        }
        return TODO("Provide the return value")
    }
}