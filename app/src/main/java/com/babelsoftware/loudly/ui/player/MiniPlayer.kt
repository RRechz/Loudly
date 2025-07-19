package com.babelsoftware.loudly.ui.player

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.PlaybackException
import coil.compose.AsyncImage
import com.babelsoftware.loudly.LocalPlayerConnection
import com.babelsoftware.loudly.R
import com.babelsoftware.loudly.constants.MiniPlayerHeight
import com.babelsoftware.loudly.constants.MiniPlayerStyle
import com.babelsoftware.loudly.constants.MiniPlayerStyleKey
import com.babelsoftware.loudly.constants.PlayerStyle
import com.babelsoftware.loudly.constants.PlayerStyleKey
import com.babelsoftware.loudly.constants.ThumbnailCornerRadius
import com.babelsoftware.loudly.extensions.togglePlayPause
import com.babelsoftware.loudly.models.MediaMetadata
import com.babelsoftware.loudly.ui.component.AsyncLocalImage
import com.babelsoftware.loudly.ui.utils.imageCache
import com.babelsoftware.loudly.utils.rememberEnumPreference
import kotlin.math.roundToInt

private const val HQ_BITRATE = 25000

@SuppressLint("UseOfNonLambdaOffsetOverload")
@Composable
fun MiniPlayer(
    position: Long,
    duration: Long,
    modifier: Modifier = Modifier,
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val playbackState by playerConnection.playbackState.collectAsState()
    val error by playerConnection.error.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val canSkipNext by playerConnection.canSkipNext.collectAsState()
    val canSkipPrevious by playerConnection.canSkipPrevious.collectAsState()
    val currentSong by playerConnection.currentSong.collectAsState(initial = null)
    val currentFormat by playerConnection.currentFormat.collectAsState(initial = null)


    val currentView = LocalView.current
    val layoutDirection = LocalLayoutDirection.current
    var offsetX by remember { mutableFloatStateOf(0f) }

    val (miniPlayerStyle) = rememberEnumPreference(
        MiniPlayerStyleKey,
        defaultValue = MiniPlayerStyle.NEW
    )
    val (playerStyle) = rememberEnumPreference(
        PlayerStyleKey,
        defaultValue = PlayerStyle.UI_2_0
    )

    val isDarkTheme = isSystemInDarkTheme()
    val cardBackgroundColor = if (isDarkTheme) {
        MaterialTheme.colorScheme.surface.copy(alpha = 1.2f)
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 1.0f)
    }
    val cardBorderColor = if (isDarkTheme) {
        Color.White.copy(alpha = 0.2f)
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
    }
    val onCardColor = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
            .padding(start = 8.dp, end = 8.dp, top = 6.dp, bottom = 12.dp)
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = cardBackgroundColor
            ),
            border = BorderStroke(1.dp, cardBorderColor)
        ) {
            if (playerStyle == PlayerStyle.UI_2_0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragCancel = { offsetX = 0f },
                                onHorizontalDrag = { _, dragAmount ->
                                    val adjustedDragAmount = if (layoutDirection == LayoutDirection.Rtl) -dragAmount else dragAmount
                                    offsetX += adjustedDragAmount
                                },
                                onDragEnd = {
                                    val threshold = 0.15f * currentView.width
                                    when {
                                        offsetX > threshold && canSkipPrevious -> playerConnection.player.seekToPreviousMediaItem()
                                        offsetX < -threshold && canSkipNext -> playerConnection.player.seekToNext()
                                    }
                                    offsetX = 0f
                                }
                            )
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxSize()
                            .offset { IntOffset(offsetX.roundToInt(), 0) }
                            .padding(start = 8.dp, end = 4.dp)
                    ) {
                        mediaMetadata?.let {
                            Box(modifier = Modifier.size(48.dp)) {
                                if (it.isLocal) {
                                    AsyncLocalImage(
                                        image = { imageCache.getLocalThumbnail(it.localPath, false) },
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp))
                                    )
                                } else {
                                    AsyncImage(
                                        model = it.thumbnailUrl,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp))
                                    )
                                }
                                val bitrate = currentFormat?.bitrate
                                if (bitrate != null) {
                                    val isHq = bitrate > HQ_BITRATE
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(2.dp)
                                    ) {
                                        QualityIndicator(isHq = isHq)
                                    }
                                }
                            }
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp)
                        ) {
                            mediaMetadata?.let {
                                Text(
                                    text = it.title,
                                    color = onCardColor,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.basicMarquee(),
                                )
                                Text(
                                    text = it.artists.joinToString { artist -> artist.name },
                                    color = onCardColor.copy(alpha = 0.7f),
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.basicMarquee(),
                                )
                            }
                        }
                        IconButton(onClick = playerConnection.player::togglePlayPause) {
                            Icon(
                                painter = painterResource(if (isPlaying) R.drawable.pause else R.drawable.play),
                                contentDescription = "Play/Pause",
                                tint = onCardColor,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        IconButton(enabled = canSkipNext, onClick = { playerConnection.player.seekToNext() }) {
                            Icon(
                                painter = painterResource(R.drawable.skip_next),
                                contentDescription = "Next",
                                tint = onCardColor,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    LinearProgressIndicator(
                        progress = { (position.toFloat() / duration).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .align(Alignment.BottomCenter),
                        color = onCardColor,
                        trackColor = Color.Transparent
                    )
                }
            } else if (miniPlayerStyle == MiniPlayerStyle.NEW) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(MiniPlayerHeight)
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragCancel = { offsetX = 0f },
                                onHorizontalDrag = { _, dragAmount ->
                                    val adjustedDragAmount = if (layoutDirection == LayoutDirection.Rtl) -dragAmount else dragAmount
                                    offsetX += adjustedDragAmount
                                },
                                onDragEnd = {
                                    val threshold = 0.15f * currentView.width
                                    when {
                                        offsetX > threshold && canSkipPrevious -> playerConnection.player.seekToPreviousMediaItem()
                                        offsetX < -threshold && canSkipNext -> playerConnection.player.seekToNext()
                                    }
                                    offsetX = 0f
                                }
                            )
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxSize()
                            .offset { IntOffset(offsetX.roundToInt(), 0) }
                            .padding(end = 12.dp)
                    ) {
                        Box(Modifier.weight(1f)) {
                            mediaMetadata?.let {
                                MiniMediaInfo(
                                    mediaMetadata = it,
                                    error = error,
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                )
                            }
                        }
                        IconButton(onClick = { playerConnection.toggleLike() }) {
                            Icon(
                                painter = if (currentSong?.song?.liked == true) painterResource(R.drawable.favorite) else painterResource(R.drawable.favorite_border),
                                tint = if (currentSong?.song?.liked == true) MaterialTheme.colorScheme.error else onCardColor,
                                contentDescription = null
                            )
                        }
                        IconButton(onClick = playerConnection.player::togglePlayPause) {
                            Icon(
                                painter = painterResource(if (isPlaying) R.drawable.pause else R.drawable.play),
                                contentDescription = null,
                                tint = onCardColor
                            )
                        }
                    }
                    LinearProgressIndicator(
                        progress = { (position.toFloat() / duration).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .align(Alignment.BottomCenter),
                        color = onCardColor,
                        trackColor = Color.Transparent
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(MiniPlayerHeight)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = modifier
                            .fillMaxSize()
                            .padding(end = 6.dp),
                    ) {
                        Box(Modifier.weight(1f)) {
                            mediaMetadata?.let {
                                MiniMediaInfo(
                                    mediaMetadata = it,
                                    error = error,
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                )
                            }
                        }
                        IconButton(enabled = canSkipPrevious, onClick = playerConnection::seekToPrevious) {
                            Icon(
                                painter = painterResource(R.drawable.skip_previous),
                                contentDescription = null,
                                tint = onCardColor,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        IconButton(onClick = playerConnection.player::togglePlayPause) {
                            Icon(
                                painter = painterResource(if (isPlaying) R.drawable.pause else R.drawable.play),
                                contentDescription = null,
                                tint = onCardColor
                            )
                        }
                        IconButton(enabled = canSkipNext, onClick = { playerConnection.player.seekToNext() }) {
                            Icon(
                                painter = painterResource(R.drawable.skip_next),
                                contentDescription = null,
                                tint = onCardColor,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                    LinearProgressIndicator(
                        progress = { (position.toFloat() / duration).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .align(Alignment.BottomCenter),
                        color = onCardColor,
                        trackColor = Color.Transparent
                    )
                }
            }
        }
    }
}


@Composable
fun MiniMediaInfo(
    mediaMetadata: MediaMetadata,
    error: PlaybackException?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
) {
    val onCardColor = MaterialTheme.colorScheme.onSurface

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Box(modifier = Modifier.padding(6.dp)) {
            if (mediaMetadata.isLocal) {
                mediaMetadata.let {
                    AsyncLocalImage(
                        image = { imageCache.getLocalThumbnail(it.localPath, false) },
                        contentDescription = null,
                        contentScale = contentScale,
                        modifier = Modifier
                            .size(48.dp)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(ThumbnailCornerRadius))
                    )
                }
            } else {
                AsyncImage(
                    model = mediaMetadata.thumbnailUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(ThumbnailCornerRadius))
                )
            }
            if (error != null) {
                Box(
                    Modifier
                        .size(48.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(ThumbnailCornerRadius)
                        )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.info),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 6.dp)
        ) {
            AnimatedContent(
                targetState = mediaMetadata.title,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "",
            ) { title ->
                Text(
                    text = title,
                    color = onCardColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.basicMarquee(),
                )
            }
            AnimatedContent(
                targetState = mediaMetadata.artists.joinToString { it.name },
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "",
            ) { artists ->
                Text(
                    text = artists,
                    color = onCardColor.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.basicMarquee(),
                )
            }
        }
    }
}

@Composable
private fun QualityIndicator(isHq: Boolean) {
    Box(
        modifier = Modifier
            .background(
                color = Color.Black.copy(alpha = 0.5f),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(
            text = if (isHq) "HQ" else "SQ",
            color = Color.White,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
