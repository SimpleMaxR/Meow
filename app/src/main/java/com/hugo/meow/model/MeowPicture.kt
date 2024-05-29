package com.hugo.meow.model

import kotlinx.serialization.Serializable

@Suppress("PLUGIN_IS_NOT_ENABLED")
@Serializable
data class MeowPicture(val id: Int, val img_url: String)