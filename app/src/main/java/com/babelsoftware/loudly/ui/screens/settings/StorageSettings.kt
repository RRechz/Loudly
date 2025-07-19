// Dosya Yolu: com/babelsoftware/loudly/ui/screens/settings/StorageSettings.kt
package com.babelsoftware.loudly.ui.screens.settings

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.annotation.ExperimentalCoilApi
import coil.imageLoader
import com.babelsoftware.loudly.BuildConfig
import com.babelsoftware.loudly.LocalPlayerConnection
import com.babelsoftware.loudly.R
import com.babelsoftware.loudly.constants.MaxImageCacheSizeKey
import com.babelsoftware.loudly.constants.MaxSongCacheSizeKey
import com.babelsoftware.loudly.extensions.tryOrNull
import com.babelsoftware.loudly.ui.component.IconButton
import com.babelsoftware.loudly.ui.utils.backToMain
import com.babelsoftware.loudly.ui.utils.formatFileSize
import com.babelsoftware.loudly.utils.rememberPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressLint("PrivateResource")
@OptIn(ExperimentalCoilApi::class, ExperimentalMaterial3Api::class)
@Composable
fun StorageSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val context = LocalContext.current
    val imageDiskCache = context.imageLoader.diskCache
    val playerCache = LocalPlayerConnection.current?.service?.playerCache
    val downloadCache = LocalPlayerConnection.current?.service?.downloadCache

    val coroutineScope = rememberCoroutineScope()
    val (maxImageCacheSize, onMaxImageCacheSizeChange) = rememberPreference(key = MaxImageCacheSizeKey, defaultValue = 512)
    val (maxSongCacheSize, onMaxSongCacheSizeChange) = rememberPreference(key = MaxSongCacheSizeKey, defaultValue = 1024)

    var imageCacheSize by remember { mutableLongStateOf(imageDiskCache?.size ?: 0L) }
    var playerCacheSize by remember { mutableLongStateOf(tryOrNull { playerCache?.cacheSpace } ?: 0L) }
    var downloadCacheSize by remember { mutableLongStateOf(tryOrNull { downloadCache?.cacheSpace } ?: 0L) }

    LaunchedEffect(Unit, maxImageCacheSize, maxSongCacheSize) {
        imageCacheSize = imageDiskCache?.size ?: 0L
        playerCacheSize = tryOrNull { playerCache?.cacheSpace } ?: 0L
        downloadCacheSize = tryOrNull { downloadCache?.cacheSpace } ?: 0L
    }

    var showClearAllDownloadsDialog by remember { mutableStateOf(false) }
    var showClearSongCacheDialog by remember { mutableStateOf(false) }
    var showClearImagesCacheDialog by remember { mutableStateOf(false) }
    var showClearTranslationModels by remember { mutableStateOf(false) }

    if (showClearAllDownloadsDialog) {
        ConfirmationDialog(
            title = R.string.clear_all_downloads,
            icon = Icons.Outlined.CloudOff,
            onDismiss = { showClearAllDownloadsDialog = false },
            onConfirm = {
                coroutineScope.launch(Dispatchers.IO) { downloadCache?.keys?.forEach { downloadCache.removeResource(it) } }
                showClearAllDownloadsDialog = false
            }
        )
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.storage)) },
                navigationIcon = {
                    IconButton(onClick = navController::navigateUp, onLongClick = navController::backToMain) {
                        Icon(painterResource(R.drawable.arrow_back), contentDescription = stringResource(R.string.back))
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                val totalUsed = downloadCacheSize + playerCacheSize + imageCacheSize
                StorageSummaryCard(
                    totalUsed = totalUsed,
                    downloadSize = downloadCacheSize,
                    songCacheSize = playerCacheSize,
                    imageCacheSize = imageCacheSize
                )
            }

            item {
                StorageDetailCard(
                    title = stringResource(R.string.downloaded_songs),
                    icon = Icons.Outlined.CloudDownload,
                    usedSpace = downloadCacheSize,
                    onClearClick = { showClearAllDownloadsDialog = true }
                )
            }

            item {
                StorageDetailCard(
                    title = stringResource(R.string.song_cache),
                    icon = Icons.Rounded.MusicNote,
                    usedSpace = playerCacheSize,
                    onClearClick = { showClearSongCacheDialog = true }
                ) {
                    CacheSizeSelector(
                        label = stringResource(R.string.max_song_cache_size),
                        selectedValue = maxSongCacheSize,
                        onValueChange = onMaxSongCacheSizeChange
                    )
                }
            }

            item {
                StorageDetailCard(
                    title = stringResource(R.string.image_cache),
                    icon = Icons.Rounded.Image,
                    usedSpace = imageCacheSize,
                    onClearClick = { showClearImagesCacheDialog = true }
                ) {
                    CacheSizeSelector(
                        label = stringResource(R.string.max_image_cache_size),
                        selectedValue = maxImageCacheSize,
                        onValueChange = onMaxImageCacheSizeChange
                    )
                }
            }

            if (BuildConfig.FLAVOR != "foss") {
                item {
                    StorageDetailCard(
                        title = stringResource(R.string.translation_models),
                        icon = Icons.Rounded.Translate,
                        usedSpace = null, // Boyut bilgisi yok
                        onClearClick = { showClearTranslationModels = true }
                    )
                }
            }
        }
    }
}

@Composable
private fun StorageSummaryCard(
    totalUsed: Long,
    downloadSize: Long,
    songCacheSize: Long,
    imageCacheSize: Long
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.total_used_space),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatFileSize(totalUsed),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))

            val totalForProgress = (downloadSize + songCacheSize + imageCacheSize).toFloat().coerceAtLeast(1f)
            val downloadProgress by animateFloatAsState((downloadSize / totalForProgress), label = "downloadProgress")
            val songCacheProgress by animateFloatAsState((songCacheSize / totalForProgress), label = "songCacheProgress")

            Row(Modifier.fillMaxWidth()) {
                LinearProgressIndicator(
                    progress = { downloadProgress },
                    modifier = Modifier.weight(if(downloadProgress > 0) downloadProgress else 0.001f),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.Transparent,
                    strokeCap = StrokeCap.Round
                )
                LinearProgressIndicator(
                    progress = { songCacheProgress },
                    modifier = Modifier.weight(if(songCacheProgress > 0) songCacheProgress else 0.001f),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = Color.Transparent,
                    strokeCap = StrokeCap.Round
                )
                LinearProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.weight(if((1f - downloadProgress - songCacheProgress) > 0) (1f - downloadProgress - songCacheProgress) else 0.001f),
                    color = MaterialTheme.colorScheme.tertiary,
                    trackColor = Color.Transparent,
                    strokeCap = StrokeCap.Round
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                StorageLegend(color = MaterialTheme.colorScheme.primary, text = stringResource(R.string.downloaded_songs), size = downloadSize)
                StorageLegend(color = MaterialTheme.colorScheme.secondary, text = stringResource(R.string.song_cache), size = songCacheSize)
                StorageLegend(color = MaterialTheme.colorScheme.tertiary, text = stringResource(R.string.image_cache), size = imageCacheSize)
            }
        }
    }
}

@Composable
private fun StorageLegend(color: Color, text: String, size: Long) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier
            .size(8.dp)
            .background(color, CircleShape))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(text = formatFileSize(size), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun StorageDetailCard(
    title: String,
    icon: ImageVector,
    usedSpace: Long?,
    onClearClick: () -> Unit,
    content: @Composable (() -> Unit)? = null
) {
    Column {
        Row(
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(text = title, style = MaterialTheme.typography.titleSmall)
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (usedSpace != null) {
                        Text(
                            text = stringResource(R.string.currently_used),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = formatFileSize(usedSpace),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                content?.invoke()

                OutlinedButton(
                    onClick = onClearClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Rounded.DeleteForever, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.clear_cache))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CacheSizeSelector(
    label: String,
    selectedValue: Int,
    onValueChange: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            readOnly = true,
            value = when (selectedValue) {
                0 -> stringResource(R.string.off)
                -1 -> stringResource(R.string.unlimited)
                else -> formatFileSize(selectedValue * 1024 * 1024L)
            },
            onValueChange = {},
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            listOf(0, 128, 256, 512, 1024, 2048, 4096, 8192, -1).forEach { size ->
                DropdownMenuItem(
                    text = {
                        Text(
                            when (size) {
                                0 -> stringResource(R.string.off)
                                -1 -> stringResource(R.string.unlimited)
                                else -> formatFileSize(size * 1024 * 1024L)
                            }
                        )
                    },
                    onClick = {
                        onValueChange(size)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ConfirmationDialog(
    @StringRes title: Int,
    icon: ImageVector,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(icon, null) },
        title = { Text(stringResource(title)) },
        confirmButton = {
            TextButton(onClick = {
                onConfirm()
                onDismiss()
            }) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    )
}
