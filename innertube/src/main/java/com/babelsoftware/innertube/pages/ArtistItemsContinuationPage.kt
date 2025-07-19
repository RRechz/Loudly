package com.babelsoftware.innertube.pages


import com.babelsoftware.innertube.models.YTItem

data class ArtistItemsContinuationPage(
    val items: List<YTItem>,
    val continuation: String?,
)
