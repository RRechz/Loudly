package com.babelsoftware.innertube.models.body

import com.babelsoftware.innertube.models.Context
import kotlinx.serialization.Serializable
@Serializable
data class PlaylistDeleteBody(
    val context: Context,
    val playlistId: String
)