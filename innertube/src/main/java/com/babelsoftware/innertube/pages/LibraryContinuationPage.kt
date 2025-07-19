package com.babelsoftware.innertube.pages

import com.babelsoftware.innertube.models.YTItem

data class LibraryContinuationPage(
    val items: List<YTItem>,
    val continuation: String?,
)