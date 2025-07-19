package com.babelsoftware.loudly.ui.menu

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
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
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.babelsoftware.loudly.db.entities.Song
import com.babelsoftware.loudly.extensions.toMediaItem
import com.babelsoftware.loudly.playback.ExoDownloadService
import com.babelsoftware.loudly.playback.queues.ListQueue
import com.babelsoftware.loudly.ui.component.DownloadListMenu
import com.babelsoftware.loudly.ui.component.ListMenu
import com.babelsoftware.loudly.ui.component.ListMenuItem
import com.babelsoftware.loudly.viewmodels.CachePlaylistViewModel
import com.babelsoftware.loudly.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

@Composable
fun SongSelectionMenu(
    navController: NavController,
    selection: List<Song>,
    onDismiss: () -> Unit,
    onExitSelectionMode: () -> Unit,
    onRemoveFromQueue: (() -> Unit)? = null,
    onRemoveFromHistory: (() -> Unit)? = null,
    isFromCache: Boolean = false,
) {
    val context = LocalContext.current
    val database = LocalDatabase.current
    val downloadUtil = LocalDownloadUtil.current
    val playerConnection = LocalPlayerConnection.current ?: return

    val allInLibrary by remember(selection) {
        mutableStateOf(selection.isNotEmpty() && selection.all { it.song.inLibrary != null })
    }
    val allLiked by remember(selection) {
        mutableStateOf(selection.isNotEmpty() && selection.all { it.song.liked })
    }

    var downloadState by remember {
        mutableIntStateOf(Download.STATE_STOPPED)
    }

    val cacheViewModel = viewModel<CachePlaylistViewModel>()

    LaunchedEffect(selection) {
        if (selection.isEmpty()) {
            onDismiss()
        } else {
            downloadUtil.downloads.collect { downloads ->
                downloadState = when {
                    selection.all { downloads[it.id]?.state == STATE_COMPLETED } -> STATE_COMPLETED
                    selection.all { downloads[it.id]?.state in listOf(STATE_QUEUED, STATE_DOWNLOADING, STATE_COMPLETED) } -> STATE_DOWNLOADING
                    else -> Download.STATE_STOPPED
                }
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
            selection.map {
                runBlocking {
                    withContext(Dispatchers.IO) {
                        it.song.id
                    }
                }
            }
        },
        onDismiss = { showChoosePlaylistDialog = false }
    )

    ListMenu(
        contentPadding =
            PaddingValues(
                start = 8.dp,
                top = 8.dp,
                end = 8.dp,
                bottom = 8.dp + WindowInsets.systemBars.asPaddingValues().calculateBottomPadding(),
            ),
    ) {
        ListMenuItem(
            icon = R.drawable.play,
            title = R.string.play,
        ) {
            onDismiss()
            playerConnection.playQueue(
                ListQueue(
                    items = selection.map { it.toMediaItem() },
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
                    items = selection.shuffled().map { it.toMediaItem() },
                ),
            )
            onExitSelectionMode()
        }

        ListMenuItem(
            icon = R.drawable.queue_music,
            title = R.string.add_to_queue,
        ) {
            onDismiss()
            playerConnection.addToQueue(selection.map { it.toMediaItem() })
            onExitSelectionMode()
        }

        ListMenuItem(
            icon = R.drawable.playlist_add,
            title = R.string.add_to_playlist,
        ) {
            showChoosePlaylistDialog = true
        }
        DownloadListMenu(
            state = downloadState,
            onDownload = {
                selection.forEach { song ->
                    val downloadRequest =
                        DownloadRequest
                            .Builder(song.id, song.id.toUri())
                            .setCustomCacheKey(song.id)
                            .setData(song.song.title.toByteArray())
                            .build()
                    DownloadService.sendAddDownload(
                        context,
                        ExoDownloadService::class.java,
                        downloadRequest,
                        false,
                    )
                }
            },
            onRemoveDownload = {
                selection.forEach { song ->
                    DownloadService.sendRemoveDownload(
                        context,
                        ExoDownloadService::class.java,
                        song.song.id,
                        false
                    )
                }
            },
        )

        if (isFromCache) {
            ListMenuItem(
                icon = R.drawable.cached,
                title = R.string.remove_from_cache
            ) {
                selection.forEach { song ->
                    onDismiss()
                    cacheViewModel.removeSongFromCache(song.id)
                }
            }
        }

        if (allInLibrary) {
            ListMenuItem(
                icon = R.drawable.library_add_check,
                title = R.string.remove_from_library,
            ) {
                database.query {
                    selection.forEach { song ->
                        inLibrary(song.id, null)
                    }
                }
            }
        } else {
            ListMenuItem(
                icon = R.drawable.library_add,
                title = R.string.add_to_library,
            ) {
                database.transaction {
                    selection.forEach { song ->
                        inLibrary(song.id, LocalDateTime.now())
                    }
                }
            }
        }

        ListMenuItem(
            icon = if (allLiked) R.drawable.favorite else R.drawable.favorite_border,
            tint = { if (allLiked) MaterialTheme.colorScheme.error else LocalContentColor.current },
            title = if (allLiked) R.string.action_remove_like_all else R.string.action_like_all,
        ) {
            database.query {
                if (allLiked) {
                    selection.forEach { song ->
                        update(song.song.copy(liked = false))
                    }
                } else {
                    selection.forEach { song ->
                        val likedSong = song.song.copy(liked = true)
                        update(likedSong)
                        downloadUtil.autoDownloadIfLiked(likedSong)
                    }
                }
            }
        }

        if (onRemoveFromQueue != null) {
            ListMenuItem(
                icon = R.drawable.playlist_remove,
                title = R.string.remove_from_playlist,
            ) {
                onDismiss()
                onRemoveFromQueue()
                onExitSelectionMode()
            }
        }
        if (onRemoveFromHistory != null) {
            ListMenuItem(
                icon = R.drawable.delete,
                title = R.string.remove_from_history,
            ) {
                onDismiss()
                onRemoveFromHistory()
                onExitSelectionMode()
            }
        }
    }
}
