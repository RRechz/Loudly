package com.babelsoftware.loudly.ui.screens

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.babelsoftware.loudly.R

@Immutable
sealed class Screens(
    @StringRes val titleId: Int,
    @DrawableRes val iconIdInactive: Int,
    @DrawableRes val iconIdActive: Int,
    val route: String,
) {
    data object Home : Screens(
        titleId = R.string.home,
        iconIdInactive = R.drawable.home_outlined,
        iconIdActive = R.drawable.home_filled,
        route = "home"
    )

    data object Explore : Screens(
        titleId = R.string.explore,
        iconIdInactive = R.drawable.explore_outlined,
        iconIdActive = R.drawable.explore_filled,
        route = "Explore"
    )

    data object Library : Screens(
        titleId = R.string.filter_library,
        iconIdInactive = R.drawable.library_music_outlined,
        iconIdActive = R.drawable.library_music_filled,
        route = "library"
    )

    data object Songs : Screens(
        titleId = R.string.songs,
        iconIdInactive = R.drawable.music_note,
        iconIdActive = R.drawable.music_note,
        route = "songs"
    )

    data object Artists : Screens(
        titleId = R.string.artists,
        iconIdInactive = R.drawable.artist,
        iconIdActive = R.drawable.artist,
        route = "artists"
    )

    data object Albums : Screens(
        titleId = R.string.albums,
        iconIdInactive = R.drawable.album,
        iconIdActive = R.drawable.album,
        route = "albums"
    )

    data object Playlists : Screens(
        titleId = R.string.playlists,
        iconIdInactive = R.drawable.queue_music,
        iconIdActive = R.drawable.queue_music,
        route = "playlists"
    )

    companion object {
        val MainScreens = listOf(Home, Explore, Library)
        val MainScreensOld = listOf(Home, Explore, Songs, Artists, Albums, Playlists)
    }
}
