package com.babelsoftware.innertube.pages

import com.babelsoftware.innertube.models.MusicResponsiveListItemRenderer
import com.babelsoftware.innertube.models.Run

object PageHelper {
    fun extractRuns(columns: List<MusicResponsiveListItemRenderer.FlexColumn>, typeLike: String): List<Run> {
        val filteredRuns = mutableListOf<Run>()
        for (column in columns) {
            val runs = column.musicResponsiveListItemFlexColumnRenderer.text?.runs
                ?: continue

            for (run in runs) {
                val typeStr = run.navigationEndpoint?.watchEndpoint?.watchEndpointMusicSupportedConfigs?.watchEndpointMusicConfig?.musicVideoType
                    ?: run.navigationEndpoint?.browseEndpoint?.browseEndpointContextSupportedConfigs?.browseEndpointContextMusicConfig?.pageType
                    ?: continue

                if (typeLike in typeStr) {
                    filteredRuns.add(run)
                }
            }
        }
        return filteredRuns
    }
}