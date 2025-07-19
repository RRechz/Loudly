package com.babelsoftware.innertube.pages

import com.babelsoftware.innertube.models.SongItem

data class PlaylistContinuationPage(
    val songs: List<SongItem>,
    val continuation: String?,
)
