package com.babelsoftware.loudly.ui.menu

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.babelsoftware.loudly.LocalPlayerConnection
import com.babelsoftware.loudly.db.entities.Event
import com.babelsoftware.loudly.extensions.toMediaItem
import com.babelsoftware.loudly.ui.component.ListMenu
import com.babelsoftware.loudly.ui.component.ListMenuItem
import com.babelsoftware.loudly.ui.component.SongFolderItem
import com.babelsoftware.loudly.ui.utils.DirectoryTree
import com.babelsoftware.loudly.R
import kotlin.collections.forEach

@Composable
fun FolderMenu(
    folder: DirectoryTree,
    event: Event? = null,
    navController: NavController,
    onDismiss: () -> Unit,
) {
    val playerConnection = LocalPlayerConnection.current ?: return

    val allFolderSongs = folder.toList()

    var showChoosePlaylistDialog by rememberSaveable {
        mutableStateOf(false)
    }

    AddToPlaylistDialog(
        navController = navController,
        isVisible = showChoosePlaylistDialog,
        onGetSong = {
            if (allFolderSongs.isEmpty()) return@AddToPlaylistDialog emptyList()
            allFolderSongs.map { it.id }
        },
        onDismiss = { showChoosePlaylistDialog = false }
    )

    SongFolderItem(
        folderTitle = folder.currentDir,
        modifier = Modifier,
        subtitle = pluralStringResource(
            R.plurals.n_song,
            folder.toList().size,
            folder.toList().size
        ),
    )

    HorizontalDivider()

    ListMenu(
        contentPadding = PaddingValues(
            start = 8.dp,
            top = 8.dp,
            end = 8.dp,
            bottom = 8.dp + WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
        )
    ) {
        ListMenuItem(
            icon = R.drawable.playlist_play,
            title = R.string.play_next
        ) {
            onDismiss()
            allFolderSongs.forEach {
                playerConnection.playNext(it.toMediaItem())
            }
        }
        ListMenuItem(
            icon = R.drawable.queue_music,
            title = R.string.add_to_queue
        ) {
            onDismiss()
            allFolderSongs.forEach {
                playerConnection.addToQueue((it.toMediaItem()))
            }
        }
        ListMenuItem(
            icon = R.drawable.playlist_add,
            title = R.string.add_to_playlist
        ) {
            showChoosePlaylistDialog = true
        }
    }
}