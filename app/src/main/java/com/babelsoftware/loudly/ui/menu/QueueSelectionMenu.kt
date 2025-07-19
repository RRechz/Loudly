package com.babelsoftware.loudly.ui.menu

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.media3.common.Timeline
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.Download.STATE_COMPLETED
import androidx.media3.exoplayer.offline.Download.STATE_DOWNLOADING
import androidx.media3.exoplayer.offline.Download.STATE_QUEUED
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import androidx.navigation.NavController
import com.babelsoftware.loudly.LocalDatabase
import com.babelsoftware.loudly.LocalDownloadUtil
import com.babelsoftware.loudly.LocalPlayerConnection
import com.babelsoftware.loudly.extensions.metadata
import com.babelsoftware.loudly.playback.ExoDownloadService
import com.babelsoftware.loudly.playback.queues.ListQueue
import com.babelsoftware.loudly.ui.component.DownloadListMenu
import com.babelsoftware.loudly.ui.component.ListMenu
import com.babelsoftware.loudly.ui.component.ListMenuItem
import com.babelsoftware.loudly.R
import kotlin.collections.get

@Composable
fun QueueSelectionMenu(
    navController: NavController,
    selection: List<Timeline.Window>,
    onExitSelectionMode: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val database = LocalDatabase.current
    val playerConnection = LocalPlayerConnection.current ?: return

    val downloadUtil = LocalDownloadUtil.current
    var downloadState by remember {
        mutableIntStateOf(Download.STATE_STOPPED)
    }
    LaunchedEffect(selection) {
        if (selection.isEmpty()) return@LaunchedEffect
        downloadUtil.downloads.collect { downloads ->
            downloadState = when {
                selection.all { downloads[it.mediaItem.metadata?.id]?.state == STATE_COMPLETED } -> STATE_COMPLETED
                selection.all { downloads[it.mediaItem.metadata?.id]?.state in listOf(STATE_QUEUED, STATE_DOWNLOADING, STATE_COMPLETED) } -> STATE_DOWNLOADING
                else -> Download.STATE_STOPPED
            }
        }
    }

    var showChoosePlaylistDialog by rememberSaveable {
        mutableStateOf(false)
    }

    AddToPlaylistDialog(
        navController = navController,
        isVisible = showChoosePlaylistDialog,
        onGetSong = {
            database.transaction {
                selection.forEach { item ->
                    insert(item.mediaItem.metadata!!)
                }
            }
            selection.mapNotNull { it.mediaItem.metadata?.id }
        },
        onDismiss = {
            showChoosePlaylistDialog = false
            onDismiss()
            onExitSelectionMode()
        }
    )

    ListMenu(
        contentPadding = PaddingValues(
            start = 8.dp,
            top = 8.dp,
            end = 8.dp,
            bottom = 8.dp + WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
        )
    ) {
        ListMenuItem(
            icon = R.drawable.play,
            title = R.string.play,
        ) {
            onDismiss()
            playerConnection.playQueue(
                ListQueue(
                    items = selection.map { it.mediaItem },
                ),
            )
            onExitSelectionMode()
        }

        ListMenuItem(
            icon = R.drawable.shuffle,
            title = R.string.shuffle,
        ) {
            onDismiss()
            playerConnection.playQueue(
                ListQueue(
                    items = selection.map { it.mediaItem }.shuffled(),
                )
            )
            onExitSelectionMode()
        }

        ListMenuItem(
            icon = R.drawable.playlist_play,
            title = R.string.play_next,
        ) {
            onDismiss()
            playerConnection.playNext(selection.map { it.mediaItem })
            onExitSelectionMode()
        }

        ListMenuItem(
            icon = R.drawable.queue_music,
            title = R.string.add_to_queue,
        ) {
            onDismiss()
            playerConnection.addToQueue(selection.map { it.mediaItem })
            onExitSelectionMode()
        }

        ListMenuItem(
            icon = R.drawable.playlist_add,
            title = R.string.add_to_playlist
        ) {
            showChoosePlaylistDialog = true
        }

        DownloadListMenu(
            state = downloadState,
            onDownload = {
                selection.forEach {
                    val mediaMetadata = it.mediaItem.metadata ?: return@forEach
                    database.query {
                        insert(mediaMetadata)
                    }
                    val downloadRequest =
                        DownloadRequest.Builder(mediaMetadata.id, mediaMetadata.id.toUri())
                            .setCustomCacheKey(mediaMetadata.id)
                            .setData(mediaMetadata.title.toByteArray())
                            .build()
                    DownloadService.sendAddDownload(
                        context,
                        ExoDownloadService::class.java,
                        downloadRequest,
                        false
                    )
                }
            },
            onRemoveDownload = {
                selection.forEach {
                    DownloadService.sendRemoveDownload(
                        context,
                        ExoDownloadService::class.java,
                        it.mediaItem.metadata!!.id,
                        false
                    )
                }
            }
        )

        ListMenuItem(
            icon = R.drawable.playlist_remove,
            title = R.string.remove_from_queue
        ) {
            selection.sortedBy {
                -it.firstPeriodIndex
            }.forEach { item ->
                playerConnection.player.removeMediaItem(item.firstPeriodIndex)
            }
            onDismiss()
        }
    }
}