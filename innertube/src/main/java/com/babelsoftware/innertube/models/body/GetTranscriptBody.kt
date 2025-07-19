package com.babelsoftware.innertube.models.body

import com.babelsoftware.innertube.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class GetTranscriptBody(
    val context: Context,
    val params: String,
)
