package com.babelsoftware.innertube.models.body

import com.babelsoftware.innertube.models.Context
import kotlinx.serialization.Serializable
@Serializable
data class SubscribeBody(
    val channelIds: List<String>,
    val context: Context,
)