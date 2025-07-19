package com.babelsoftware.innertube.pages

import com.babelsoftware.innertube.models.Album
import com.babelsoftware.innertube.models.Artist
import com.babelsoftware.innertube.models.MusicResponsiveListItemRenderer
import com.babelsoftware.innertube.models.MusicShelfRenderer
import com.babelsoftware.innertube.models.SongItem
import com.babelsoftware.innertube.models.getItems
import com.babelsoftware.innertube.models.oddElements
import com.babelsoftware.innertube.utils.parseTime

data class HistoryPage(
    val sections: List<HistorySection>?,
) {
    data class HistorySection(
        val title: String,
        val songs: List<SongItem>
    )
    companion object {
        fun fromMusicShelfRenderer(renderer: MusicShelfRenderer): HistorySection {
            return HistorySection(
                title = renderer.title?.runs?.firstOrNull()?.text!!,
                songs = renderer.contents?.getItems()?.mapNotNull {
                    fromMusicResponsiveListItemRenderer(it)
                }!!
            )
        }
        private fun fromMusicResponsiveListItemRenderer(renderer: MusicResponsiveListItemRenderer): SongItem? {
            return SongItem(
                id = renderer.playlistItemData?.videoId ?: return null,
                title = renderer.flexColumns.firstOrNull()
                    ?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()
                    ?.text ?: return null,
                artists = renderer.flexColumns.getOrNull(1)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.oddElements()
                    ?.map {
                        Artist(
                            name = it.text,
                            id = it.navigationEndpoint?.browseEndpoint?.browseId
                        )
                    } ?: emptyList(),
                album = renderer.flexColumns.getOrNull(2)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()
                    ?.takeIf {
                        it.navigationEndpoint?.browseEndpoint?.browseId != null
                    }?.let {
                        it.navigationEndpoint?.browseEndpoint?.browseId?.let { it1 ->
                            Album(
                                name = it.text, id = it1
                            )
                        }
                    },
                duration = renderer.fixedColumns?.firstOrNull()?.musicResponsiveListItemFlexColumnRenderer
                    ?.text?.runs?.firstOrNull()?.text?.parseTime(),
                thumbnail = renderer.thumbnail?.musicThumbnailRenderer?.getThumbnailUrl()
                    ?: return null,
                explicit = renderer.badges?.find {
                    it.musicInlineBadgeRenderer?.icon?.iconType == "MUSIC_EXPLICIT_BADGE"
                } != null,
                endpoint = renderer.overlay?.musicItemThumbnailOverlayRenderer?.content
                    ?.musicPlayButtonRenderer?.playNavigationEndpoint?.watchEndpoint
            )
        }
    }
}