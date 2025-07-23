package com.babelsoftware.loudly.ui.player

import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.navigation.NavController
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.babelsoftware.innertube.YouTube
import com.babelsoftware.loudly.LocalDatabase
import com.babelsoftware.loudly.LocalPlayerConnection
import com.babelsoftware.loudly.R
import com.babelsoftware.loudly.extensions.metadata
import com.babelsoftware.loudly.extensions.togglePlayPause
import com.babelsoftware.loudly.extensions.toggleRepeatMode
import com.babelsoftware.loudly.extensions.toggleShuffleMode
import com.babelsoftware.loudly.lyrics.LyricsEntry
import com.babelsoftware.loudly.lyrics.LyricsUtils
import com.babelsoftware.loudly.models.MediaMetadata
import com.babelsoftware.loudly.ui.component.BottomSheetState
import com.babelsoftware.loudly.ui.component.LocalMenuState
import com.babelsoftware.loudly.ui.component.ResizableIconButton
import com.babelsoftware.loudly.ui.menu.AddToPlaylistDialog
import com.babelsoftware.loudly.ui.menu.PlayerMenu
import com.babelsoftware.loudly.ui.theme.extractGradientColors
import com.babelsoftware.loudly.utils.LogReportHelper
import com.babelsoftware.loudly.utils.makeTimeString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.absoluteValue
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

private const val HQ_BITRATE = 25000

enum class NetworkType {
    WIFI, CELLULAR, NONE
}

fun getCurrentNetworkType(context: Context): NetworkType {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return NetworkType.NONE
    val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return NetworkType.NONE

    return when {
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.CELLULAR
        else -> NetworkType.NONE
    }
}

/**
 * Master Composable for UI 2.0.
 * Manages switching between the player and playlist screens.
 */
@Composable
fun PlayerUI20(
    position: Long,
    duration: Long,
    navController: NavController,
    bottomSheetState: BottomSheetState,
    modifier: Modifier = Modifier
) {
    var showPlaylist by remember { mutableStateOf(false) }
    val playerConnection = LocalPlayerConnection.current ?: return
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val context = LocalContext.current

    var dominantColor by remember { mutableStateOf<Color?>(null) }

    LaunchedEffect(mediaMetadata?.id) {
        val currentMediaMetadata = mediaMetadata
        if (currentMediaMetadata == null) {
            dominantColor = null
            return@LaunchedEffect
        }
        withContext(Dispatchers.IO) {
            val result = (ImageLoader(context).execute(
                ImageRequest.Builder(context)
                    .data(currentMediaMetadata.thumbnailUrl)
                    .allowHardware(false)
                    .build()
            ).drawable as? BitmapDrawable)?.bitmap?.extractGradientColors()

            dominantColor = result?.firstOrNull()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        val currentMediaMetadata = mediaMetadata
        if (currentMediaMetadata != null) {
            AsyncImage(
                model = currentMediaMetadata.thumbnailUrl,
                contentDescription = "Background Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(80.dp)
                    .alpha(0.6f)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
            )
        }

        AnimatedContent(
            targetState = showPlaylist,
            transitionSpec = {
                if (targetState) {
                    slideInVertically { height -> height } + fadeIn() togetherWith
                            slideOutVertically { height -> -height } + fadeOut()
                } else {
                    slideInVertically { height -> -height } + fadeIn() togetherWith
                            slideOutVertically { height -> height } + fadeOut()
                }
            },
            label = "Player/Playlist"
        ) { isPlaylistVisible ->
            if (isPlaylistVisible) {
                PlaylistScreen(
                    onClose = { showPlaylist = false },
                    dominantColor = dominantColor ?: MaterialTheme.colorScheme.primary
                )
            } else {
                NowPlayingScreen(
                    position = position,
                    duration = duration,
                    navController = navController,
                    bottomSheetState = bottomSheetState,
                    onShowPlaylist = { showPlaylist = true },
                    onClose = { bottomSheetState.collapseSoft() },
                    dominantColor = dominantColor
                )
            }
        }
    }
}

/**
 * Main player screen with circular progress broadcast.
 */
@Composable
fun NowPlayingScreen(
    position: Long,
    duration: Long,
    navController: NavController,
    bottomSheetState: BottomSheetState,
    onShowPlaylist: () -> Unit,
    onClose: () -> Unit,
    dominantColor: Color?
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val isPlaying by playerConnection.isPlaying.collectAsState()

    var sliderPosition by remember { mutableStateOf<Long?>(null) }
    var showLyrics by remember { mutableStateOf(false) }

    val errorState by playerConnection.errorManagerState.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val context = LocalContext.current
    val onSurfaceColor = Color.White

    var showDetailsDialog by remember { mutableStateOf(false) }

    if (showDetailsDialog) {
        DetailsDialog(
            onDismiss = { showDetailsDialog = false }
        )
    }

    var showPersistentErrorUI by remember { mutableStateOf(false) }

    LaunchedEffect(errorState) {
        if (errorState == PlayerErrorManager.State.RECOVERING) {
            delay(5000)
            if (playerConnection.errorManagerState.value == PlayerErrorManager.State.RECOVERING) {
                showPersistentErrorUI = true
            }
        } else { showPersistentErrorUI = false }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures { change, dragAmount ->
                    if (dragAmount < -50) {
                        onShowPlaylist()
                        change.consume()
                    }
                }
            }
            .padding(horizontal = 24.dp)
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Vertical)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(painterResource(R.drawable.arrow_back), contentDescription = "Geri", tint = onSurfaceColor)
            }

            AnimatedVisibility(visible = errorState == PlayerErrorManager.State.RECOVERING) {
                if (showPersistentErrorUI) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val error by playerConnection.error.collectAsState()
                        if (error != null) {
                            PlaybackError(
                                error = error!!,
                                retry = { /* Retrying is now fully automated */ }
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    LogReportHelper.createAndShareErrorReport(
                                        context,
                                        error!!,
                                        mediaMetadata?.id
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(painterResource(R.drawable.share), contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Share Error Report")
                            }
                        }
                    }
                } else {
                    InfoChip(
                        icon = R.drawable.ic_autorenew,
                        text = stringResource(R.string.recovering_playback),
                        color = onSurfaceColor.copy(alpha = 0.8f),
                        onClick = {}
                    )
                }
            }
            AnimatedVisibility(visible = errorState == PlayerErrorManager.State.IDLE) { NetworkStatusChip() }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = showLyrics,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "Player/Lyrics"
            ) { lyricsVisible ->
                if (lyricsVisible) {
                    LyricsScreen(
                        position = position,
                        onDismiss = { showLyrics = false }
                    )
                } else {
                    CircularArcPlayer(
                        position = sliderPosition ?: position,
                        duration = duration,
                        mediaMetadata = playerConnection.mediaMetadata.collectAsState().value,
                        dominantColor = dominantColor,
                        isPlaying = isPlaying,
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .fillMaxWidth(0.85f),
                        onSeek = { newPosition ->
                            playerConnection.player.seekTo(newPosition)
                            sliderPosition = null
                        },
                        onSeekChange = { tempPosition ->
                            sliderPosition = tempPosition
                        },
                        onSwipe = { direction ->
                            if (direction > 0) playerConnection.player.seekToPrevious()
                            else playerConnection.player.seekToNext()
                        }
                    )
                }
            }
        }

        ContextualInfoRow(
            navController = navController,
            bottomSheetState = bottomSheetState,
            onShowPlaylist = onShowPlaylist,
            onShowDetailsDialog = { showDetailsDialog = true }
        )
        Spacer(modifier = Modifier.height(8.dp))

        ControlPanel(
            position = sliderPosition ?: position,
            duration = duration,
            isPlaying = isPlaying,
            dominantColor = dominantColor,
            shuffleModeEnabled = playerConnection.shuffleModeEnabled.collectAsState().value,
            repeatMode = playerConnection.repeatMode.collectAsState().value,
            onPlayPause = {
                playerConnection.player.togglePlayPause()
            },
            onShuffle = playerConnection.player::toggleShuffleMode,
            onPrevious = playerConnection.player::seekToPrevious,
            onNext = playerConnection.player::seekToNext,
            onRepeat = playerConnection.player::toggleRepeatMode,
            onToggleLyrics = { showLyrics = !showLyrics },
            canSkipPrevious = playerConnection.canSkipPrevious.collectAsState().value,
            canSkipNext = playerConnection.canSkipNext.collectAsState().value,
            navController = navController
        )
    }
}

@Composable
private fun NetworkStatusChip() {
    val context = LocalContext.current
    val networkType = remember { getCurrentNetworkType(context) }

    AnimatedVisibility(visible = networkType != NetworkType.NONE) {
        val icon = when (networkType) {
            NetworkType.WIFI -> R.drawable.wifi
            NetworkType.CELLULAR -> R.drawable.signal_cellular_alt
            else -> null
        }
        val text = when (networkType) {
            NetworkType.WIFI -> "Wi-Fi"
            NetworkType.CELLULAR -> "Mobile Data"
            else -> null
        }

        if (icon != null) {
            InfoChip(icon = icon, text = text, onClick = {})
        }
    }
}


@Composable
fun ContextualInfoRow(
    navController: NavController,
    bottomSheetState: BottomSheetState,
    onShowPlaylist: () -> Unit,
    onShowDetailsDialog: () -> Unit
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val menuState = LocalMenuState.current
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val currentFormat by playerConnection.currentFormat.collectAsState(initial = null)

    val showArtistChip = mediaMetadata?.artists?.any { it.id != null } == true
    val bitrate = currentFormat?.bitrate
    val isHq = bitrate != null && bitrate > HQ_BITRATE

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showArtistChip || bitrate != null) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
                ),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.height(IntrinsicSize.Min)
                ) {
                    if (showArtistChip) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable {
                                    navController.navigate("artist/${mediaMetadata!!.artists.first { it.id != null }.id}")
                                    bottomSheetState.collapseSoft()
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.artist),
                                contentDescription = stringResource(R.string.view_artist),
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.artist),
                                color = Color.White.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                    if (bitrate != null && showArtistChip) {
                        HorizontalDivider(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(1.dp),
                            color = Color.White.copy(alpha = 0.25f)
                        )
                    }
                    if (bitrate != null) {
                        val qualityRowModifier = if (!isHq) {
                            Modifier.clickable {
                                navController.navigate("settings/player")
                                bottomSheetState.collapseSoft()
                            }
                        } else {
                            Modifier
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = qualityRowModifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                painter = painterResource(if (isHq) R.drawable.high_quality else R.drawable.headphones),
                                contentDescription = "Quality",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (isHq) "HD" else "SD",
                                color = Color.White.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.width(8.dp))

        InfoChip(
            text = stringResource(R.string.queue),
            icon = R.drawable.queue_music,
            onClick = onShowPlaylist
        )

        Spacer(Modifier.width(8.dp))

        InfoChip(
            icon = R.drawable.more_vert,
            text = stringResource(R.string.menu),
            onClick = {
                menuState.show {
                    PlayerMenu(
                        mediaMetadata = mediaMetadata,
                        navController = navController,
                        bottomSheetState = bottomSheetState,
                        onShowDetailsDialog = onShowDetailsDialog,
                        onDismiss = menuState::dismiss
                    )
                }
            }
        )
    }
}


@Composable
fun ControlPanel(
    position: Long,
    duration: Long,
    isPlaying: Boolean,
    dominantColor: Color?,
    shuffleModeEnabled: Boolean,
    repeatMode: Int,
    onPlayPause: () -> Unit,
    onShuffle: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onRepeat: () -> Unit,
    onToggleLyrics: () -> Unit,
    canSkipPrevious: Boolean,
    canSkipNext: Boolean,
    navController: NavController
) {
    val onSurfaceColor = Color.White
    val onSurfaceVariantColor = Color.White.copy(alpha = 0.7f)
    val haptic = LocalHapticFeedback.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val playbackState by playerConnection.playbackState.collectAsState()
    val currentSong by playerConnection.currentSong.collectAsState(initial = null)
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    var showChoosePlaylistDialog by remember { mutableStateOf(false) }
    val database = LocalDatabase.current
    val coroutineScope = rememberCoroutineScope()

    if (mediaMetadata != null) {
        AddToPlaylistDialog(
            navController = navController,
            isVisible = showChoosePlaylistDialog,
            onGetSong = { playlist ->
                database.transaction {
                    insert(mediaMetadata!!)
                }

                coroutineScope.launch(Dispatchers.IO) {
                    playlist.playlist.browseId?.let {
                        YouTube.addToPlaylist(it, mediaMetadata!!.id)
                    }
                }

                listOf(mediaMetadata!!.id)
            },
            onDismiss = {
                showChoosePlaylistDialog = false
            }
        )
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            val currentPosition by rememberUpdatedState(newValue = position)
            Text(
                text = "${makeTimeString(currentPosition)} / ${makeTimeString(duration)}",
                color = onSurfaceVariantColor,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ResizableIconButton(
                    icon = R.drawable.shuffle,
                    modifier = Modifier.size(28.dp),
                    color = if (shuffleModeEnabled) dominantColor ?: onSurfaceColor else onSurfaceVariantColor,
                    onClick = onShuffle
                )

                ResizableIconButton(
                    icon = R.drawable.skip_previous,
                    enabled = canSkipPrevious,
                    color = onSurfaceColor,
                    modifier = Modifier.size(40.dp),
                    onClick = onPrevious
                )

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(onSurfaceColor)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (playbackState == Player.STATE_ENDED) {
                                playerConnection.player.seekTo(0, 0)
                                playerConnection.player.playWhenReady = true
                            } else {
                                onPlayPause()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = isPlaying,
                        label = "PlayPauseIcon",
                        contentAlignment = Alignment.Center
                    ) { playing ->
                        Icon(
                            painter = painterResource(if (playing) R.drawable.pause else R.drawable.play),
                            contentDescription = "Play/Pause",
                            tint = Color.Black,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                ResizableIconButton(
                    icon = R.drawable.skip_next,
                    enabled = canSkipNext,
                    color = onSurfaceColor,
                    modifier = Modifier.size(40.dp),
                    onClick = onNext
                )

                ResizableIconButton(
                    icon = when (repeatMode) {
                        Player.REPEAT_MODE_OFF, Player.REPEAT_MODE_ALL -> R.drawable.repeat
                        Player.REPEAT_MODE_ONE -> R.drawable.repeat_one
                        else -> throw IllegalStateException()
                    },
                    modifier = Modifier.size(28.dp),
                    color = if (repeatMode != Player.REPEAT_MODE_OFF) dominantColor ?: onSurfaceColor else onSurfaceVariantColor,
                    onClick = onRepeat
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isLiked = currentSong?.song?.liked == true
                val likeColor by animateColorAsState(
                    targetValue = if (isLiked) dominantColor ?: MaterialTheme.colorScheme.error else onSurfaceVariantColor,
                    label = "LikeColor"
                )
                TextButton(onClick = { playerConnection.toggleLike() }) {
                    Icon(
                        painter = if (isLiked) painterResource(R.drawable.favorite) else painterResource(R.drawable.favorite_border),
                        contentDescription = stringResource(R.string.like),
                        tint = likeColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.like),
                        color = likeColor,
                        modifier = Modifier
                            .widthIn(max = 75.dp)
                            .basicMarquee()
                    )
                }

                TextButton(onClick = { showChoosePlaylistDialog = true }) {
                    Icon(
                        painter = painterResource(R.drawable.playlist_add),
                        contentDescription = stringResource(R.string.add_to_playlist),
                        tint = onSurfaceVariantColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.add_to_playlist),
                        color = onSurfaceVariantColor,
                        modifier = Modifier
                            .widthIn(max = 75.dp)
                            .basicMarquee()
                    )
                }
                TextButton(onClick = onToggleLyrics) {
                    Icon(
                        painter = painterResource(R.drawable.lyrics),
                        contentDescription = stringResource(R.string.lyrics),
                        tint = onSurfaceVariantColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.lyrics),
                        color = onSurfaceVariantColor,
                        modifier = Modifier
                            .widthIn(max = 75.dp)
                            .basicMarquee()
                    )
                }
            }
        }
    }
}


@Composable
fun CircularArcPlayer(
    position: Long,
    duration: Long,
    mediaMetadata: MediaMetadata?,
    dominantColor: Color?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    onSeek: (newPosition: Long) -> Unit,
    onSeekChange: (tempPosition: Long) -> Unit,
    onSwipe: (direction: Int) -> Unit
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .graphicsLayer {
                shadowElevation = 8.dp.toPx()
                shape = CircleShape
                clip = true
            },
        contentAlignment = Alignment.Center
    ) {
        AudioVisualizer(isPlaying = isPlaying, dominantColor = dominantColor)

        PlayerArtworkAndInfo(
            mediaMetadata = mediaMetadata,
            onSwipe = onSwipe
        )

        CircularProgressBar(
            progress = if (duration > 0) (position.toFloat() / duration).coerceIn(0f, 1f) else 0f,
            duration = duration,
            dominantColor = dominantColor,
            modifier = Modifier.fillMaxSize(),
            onSeek = onSeek,
            onSeekChange = onSeekChange
        )
    }
}

@Composable
fun PlayerArtworkAndInfo(
    mediaMetadata: MediaMetadata?,
    onSwipe: (direction: Int) -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        when {
                            offsetX > 150 -> onSwipe(1) // Previous
                            offsetX < -150 -> onSwipe(-1) // Next
                        }
                        offsetX = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        offsetX += dragAmount
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        val alpha by animateFloatAsState(
            targetValue = 1f - (offsetX.coerceIn(-300f, 300f).absoluteValue / 300f) * 0.7f,
            label = "ArtworkAlpha"
        )

        AnimatedContent(
            targetState = mediaMetadata,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "Artwork"
        ) { currentArtworkMetadata ->
            AsyncImage(
                model = currentArtworkMetadata?.thumbnailUrl,
                contentDescription = "Album Thumbnail",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .padding(24.dp)
                    .clip(CircleShape)
                    .alpha(alpha)
                    .offset(x = (offsetX / 2).dp)
            )
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(24.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.5f))
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(horizontal = 48.dp)
                .alpha(alpha)
        ) {
            AnimatedContent(
                targetState = mediaMetadata?.title,
                transitionSpec = {
                    (slideInVertically { it } + fadeIn()) togetherWith (slideOutVertically { -it } + fadeOut())
                },
                label = "Title"
            ) { title ->
                Text(
                    text = title ?: "",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.basicMarquee()
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            AnimatedContent(
                targetState = mediaMetadata?.artists?.joinToString { it.name },
                transitionSpec = {
                    (slideInVertically { it } + fadeIn()) togetherWith (slideOutVertically { -it } + fadeOut())
                },
                label = "Artist"
            ) { artist ->
                Text(
                    text = artist ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.basicMarquee()
                )
            }
        }
    }
}


@Composable
fun CircularProgressBar(
    progress: Float,
    duration: Long,
    dominantColor: Color?,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 4.dp,
    trackColor: Color = Color.White.copy(alpha = 0.2f),
    onSeek: (newPosition: Long) -> Unit,
    onSeekChange: (tempPosition: Long) -> Unit
) {
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "ProgressAnimation")
    val strokeWidthPx = with(LocalDensity.current) { strokeWidth.toPx() }
    val haptic = LocalHapticFeedback.current

    var dragProgress by remember { mutableStateOf<Float?>(null) }

    val progressColor by animateColorAsState(
        targetValue = dominantColor ?: Color.White,
        label = "ProgressColor"
    )

    Canvas(
        modifier = modifier
            .padding(strokeWidth * 2)
            .pointerInput(duration) {
                if (duration <= 0) return@pointerInput
                val center = Offset(size.width / 2f, size.height / 2f)
                detectDragGestures(
                    onDragStart = { startOffset ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val touchX = startOffset.x - center.x
                        val touchY = center.y - startOffset.y
                        val angle = Math.toDegrees(atan2(touchY.toDouble(), touchX.toDouble())).toFloat()
                        val adjustedAngle = (360f - angle + 90f) % 360f
                        val newProgress = adjustedAngle / 360f
                        dragProgress = newProgress
                        onSeekChange((newProgress * duration).toLong())
                    },
                    onDragEnd = {
                        dragProgress?.let { onSeek((it * duration).toLong()) }
                        dragProgress = null
                    },
                    onDragCancel = {
                        dragProgress = null
                    }
                ) { change, _ ->
                    val touchX = change.position.x - center.x
                    val touchY = center.y - change.position.y
                    val angle = Math.toDegrees(atan2(touchY.toDouble(), touchX.toDouble())).toFloat()
                    val adjustedAngle = (360f - angle + 90f) % 360f
                    val newProgress = adjustedAngle / 360f
                    dragProgress = newProgress
                    onSeekChange((newProgress * duration).toLong())
                    change.consume()
                }
            }
    ) {
        val diameter = size.minDimension
        val radius = diameter / 2f
        val drawCenter = Offset(size.width / 2, size.height / 2)

        val displayProgress = dragProgress ?: animatedProgress
        val sweepAngle = displayProgress * 360f

        drawArc(
            color = trackColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = strokeWidthPx)
        )

        drawArc(
            color = progressColor,
            startAngle = -90f,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
        )

        val angleInRadians = (sweepAngle - 90) * (Math.PI / 180f).toFloat()
        val thumbX = drawCenter.x + radius * cos(angleInRadians)
        val thumbY = drawCenter.y + radius * sin(angleInRadians)

        drawCircle(
            color = progressColor,
            radius = strokeWidthPx * 2f,
            center = Offset(thumbX, thumbY)
        )
    }
}

@Composable
fun PlaylistScreen(onClose: () -> Unit, dominantColor: Color) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val queueWindows by playerConnection.queueWindows.collectAsState()
    val currentWindowIndex by playerConnection.currentWindowIndex.collectAsState()
    val lazyListState = rememberLazyListState()
    val queueTitle by playerConnection.queueTitle.collectAsState()


    LaunchedEffect(currentWindowIndex) {
        if (currentWindowIndex != -1) {
            lazyListState.animateScrollToItem(currentWindowIndex)
        }
    }

    val backgroundColor = MaterialTheme.colorScheme.surface

    Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 280.dp, bottom = 16.dp)
        ) {
            itemsIndexed(queueWindows, key = { _, window -> window.mediaItem.mediaId }) { index, window ->
                SimplePlaylistItem(
                    metadata = window.mediaItem.metadata!!,
                    isActive = index == currentWindowIndex,
                    activeColor = dominantColor,
                    onClick = {
                        if (index == currentWindowIndex) {
                            playerConnection.player.togglePlayPause()
                        } else {
                            playerConnection.player.seekToDefaultPosition(index)
                            if (!playerConnection.player.playWhenReady) {
                                playerConnection.player.playWhenReady = true
                            }
                        }
                    }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, backgroundColor)
                    )
                )
        )

        val parallaxOffset = if (lazyListState.firstVisibleItemIndex == 0) {
            lazyListState.firstVisibleItemScrollOffset * 0.5f
        } else {
            0f
        }

        Box(modifier = Modifier.graphicsLayer { translationY = parallaxOffset }) {
            CurvedHeader(
                onClose = onClose,
                queueWindows = queueWindows,
                queueTitle = queueTitle
            )
        }
    }
}

@Composable
fun SimplePlaylistItem(
    metadata: MediaMetadata,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    val inactiveColor = MaterialTheme.colorScheme.onSurface
    val textColor by animateColorAsState(
        targetValue = if (isActive) activeColor else inactiveColor,
        label = "TextColor"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = metadata.title,
                color = textColor,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Spacer(Modifier.width(16.dp))
        Text(
            text = metadata.duration?.let { makeTimeString(it * 1000L) } ?: "0:00",
            color = textColor.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun CurvedHeader(
    onClose: () -> Unit,
    queueWindows: List<Timeline.Window>,
    queueTitle: String?
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val context = LocalContext.current

    val headerColor = MaterialTheme.colorScheme.surface
    val onHeaderColor = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(CurvedHeaderShape())
            .background(headerColor)
    ) {
        val currentMediaMetadata = mediaMetadata
        if (currentMediaMetadata != null) {
            AsyncImage(
                model = currentMediaMetadata.thumbnailUrl,
                contentDescription = "Albüm Kapağı",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().alpha(if(isSystemInDarkTheme()) 0.4f else 1f)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, headerColor.copy(alpha = 0.8f), headerColor),
                        startY = 150f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top)),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(painterResource(id = R.drawable.arrow_back), contentDescription = "Geri", tint = onHeaderColor)
                }
                InfoChip(
                    icon = R.drawable.share,
                    text = stringResource(R.string.share_list),
                    color = onHeaderColor,
                    onClick = {
                        val shareText = buildString {
                            appendLine(queueTitle ?: "Playlist")
                            appendLine()
                            queueWindows.forEach { window ->
                                val metadata = window.mediaItem.metadata!!
                                appendLine("${metadata.title} - ${metadata.artists.joinToString { it.name }}")
                            }
                        }
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    }
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val currentMediaMetadata = mediaMetadata
                if (currentMediaMetadata != null) {
                    Text(
                        text = queueTitle ?: "Playlist",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = onHeaderColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = currentMediaMetadata.artists.joinToString { it.name },
                        style = MaterialTheme.typography.titleMedium,
                        color = onHeaderColor.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

class CurvedHeaderShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, size.height - 60f)
            quadraticTo(
                x1 = size.width / 2, y1 = size.height + 60f,
                x2 = 0f, y2 = size.height - 60f
            )
            close()
        }
        return Outline.Generic(path)
    }
}

@Composable
fun AudioVisualizer(
    isPlaying: Boolean,
    dominantColor: Color?
) {
    val infiniteTransition = rememberInfiniteTransition(label = "VisualizerTransition")

    val color by animateColorAsState(
        targetValue = (dominantColor ?: Color.White).copy(alpha = 0.5f),
        label = "VisualizerColor"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "VisualizerScale"
    )

    if (isPlaying) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color, Color.Transparent),
                    center = center,
                    radius = (size.minDimension / 2) * scale
                ),
                radius = (size.minDimension / 2) * scale
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoChip(
    modifier: Modifier = Modifier,
    icon: Int? = null,
    text: String? = null,
    color: Color = LocalContentColor.current,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            if (icon != null) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = color
                )
            }
            if (text != null) {
                if (icon != null) Spacer(Modifier.width(4.dp))
                Text(
                    text = text,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = color
                )
            }
        }
    }
}

/**
 * A special Composable that manages and displays song lyrics.
 * Handles loading, not found, and display states.
 */
@Composable
private fun LyricsScreen(
    position: Long,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val lyricsEntity by playerConnection.currentLyrics.collectAsState()
    val isTranslating by playerConnection.translating.collectAsState()
    val onSurfaceColor = Color.White

    val parsedLyrics: List<LyricsEntry> = remember(lyricsEntity) {
        LyricsUtils.parseLyrics(lyricsEntity?.lyrics ?: "", trim = true, multilineEnable = false)
    }

    val currentLineIndex by remember(parsedLyrics, position) {
        derivedStateOf {
            if (parsedLyrics.isNotEmpty()) {
                LyricsUtils.findCurrentLineIndex(parsedLyrics, position)
            } else {
                -1
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(onClick = onDismiss)
            .padding(horizontal = 24.dp, vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            lyricsEntity == null || isTranslating -> {
                CircularProgressIndicator(color = onSurfaceColor)
            }
            parsedLyrics.isEmpty() -> {
                Text(
                    text = "No lyrics were found for this song.",
                    color = onSurfaceColor.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
            }
            else -> {
                val listState = rememberLazyListState()

                LaunchedEffect(currentLineIndex) {
                    if (currentLineIndex != -1 && currentLineIndex < listState.layoutInfo.totalItemsCount) {
                        listState.animateScrollToItem(index = currentLineIndex, scrollOffset = -250)
                    }
                }

                LazyColumn(
                    state = listState,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item { Spacer(modifier = Modifier.height(200.dp)) }

                    itemsIndexed(parsedLyrics) { index, line ->
                        if (line.isTranslation) return@itemsIndexed

                        val isActive = index == currentLineIndex

                        val color by animateColorAsState(
                            targetValue = if (isActive) onSurfaceColor else onSurfaceColor.copy(alpha = 0.6f),
                            label = "LyricColor"
                        )
                        val fontSize by animateFloatAsState(
                            targetValue = if (isActive) 26f else 22f,
                            label = "LyricFontSize"
                        )
                        val fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal

                        Text(
                            text = line.text,
                            color = color,
                            fontSize = fontSize.sp,
                            fontWeight = fontWeight,
                            textAlign = TextAlign.Center,
                            lineHeight = 36.sp
                        )
                    }

                    item { Spacer(modifier = Modifier.height(200.dp)) }
                }
            }
        }
    }
}