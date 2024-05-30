package com.hugo.meow.model

import android.icu.text.ListFormatter.Width
import androidx.core.app.NotificationCompat.MessagingStyle.Message
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MeowPicture(
    val id: String,
    val url: String,
    val width: Int,
    val height: Int,
)