package com.babelsoftware.innertube.models.body

import com.babelsoftware.innertube.models.Context
import kotlinx.serialization.Serializable
@Serializable
data class CreatePlaylistBody(
    val context: Context,
    val title: String,
    val privacyStatus: String = PrivacyStatus.PRIVATE,
    val videoIds: List<String>? = null
) {
    object PrivacyStatus {
        const val PRIVATE = "PRIVATE"
    }
}