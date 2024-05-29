package com.hugo.meow.data

import android.content.Context
import androidx.compose.ui.platform.LocalContext
import okhttp3.OkHttpClient

interface AppContainer {
    val meowRepository: MeowRepository
}

class DefaultAppContainer(context: Context) : AppContainer {
    private val client: OkHttpClient = OkHttpClient().newBuilder().build()


    override val meowRepository: MeowRepository by lazy {
        NetworkMeowRepository(client, context)
    }
}