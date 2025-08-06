package com.babelsoftware.loudly.ui.screens

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.babelsoftware.innertube.models.AlbumItem
import com.babelsoftware.innertube.models.ArtistItem
import com.babelsoftware.innertube.models.PlaylistItem
import com.babelsoftware.innertube.models.SongItem
import com.babelsoftware.innertube.models.WatchEndpoint
import com.babelsoftware.innertube.models.YTItem
import com.babelsoftware.innertube.pages.HomePage
import com.babelsoftware.innertube.utils.parseCookieString
import com.babelsoftware.loudly.BuildConfig
import com.babelsoftware.loudly.LocalDatabase
import com.babelsoftware.loudly.LocalPlayerAwareWindowInsets
import com.babelsoftware.loudly.LocalPlayerConnection
import com.babelsoftware.loudly.R
import com.babelsoftware.loudly.constants.AccountNameKey
import com.babelsoftware.loudly.constants.GridThumbnailHeight
import com.babelsoftware.loudly.constants.HeaderImageKey
import com.babelsoftware.loudly.constants.InnerTubeCookieKey
import com.babelsoftware.loudly.constants.ListItemHeight
import com.babelsoftware.loudly.constants.ListThumbnailSize
import com.babelsoftware.loudly.constants.PlayerStyle
import com.babelsoftware.loudly.constants.ShowContentFilterKey
import com.babelsoftware.loudly.constants.ShowRecentActivityKey
import com.babelsoftware.loudly.constants.ThumbnailCornerRadius
import com.babelsoftware.loudly.constants.YtmSyncKey
import com.babelsoftware.loudly.constants.showNoInternetDialogKey
import com.babelsoftware.loudly.db.entities.Album
import com.babelsoftware.loudly.db.entities.Artist
import com.babelsoftware.loudly.db.entities.LocalItem
import com.babelsoftware.loudly.db.entities.Playlist
import com.babelsoftware.loudly.db.entities.RecentActivityType
import com.babelsoftware.loudly.db.entities.Song
import com.babelsoftware.loudly.extensions.togglePlayPause
import com.babelsoftware.loudly.isNewerVersion
import com.babelsoftware.loudly.models.toMediaMetadata
import com.babelsoftware.loudly.playback.queues.YouTubeAlbumRadio
import com.babelsoftware.loudly.playback.queues.YouTubeQueue
import com.babelsoftware.loudly.ui.component.AlbumGridItem
import com.babelsoftware.loudly.ui.component.ArtistGridItem
import com.babelsoftware.loudly.ui.component.ChipsRow
import com.babelsoftware.loudly.ui.component.FeaturedContentCard
import com.babelsoftware.loudly.ui.component.HideOnScrollFAB
import com.babelsoftware.loudly.ui.component.LocalMenuState
import com.babelsoftware.loudly.ui.component.NavigationTitle
import com.babelsoftware.loudly.ui.component.SongGridItem
import com.babelsoftware.loudly.ui.component.SongListItem
import com.babelsoftware.loudly.ui.component.YouTubeCardItem
import com.babelsoftware.loudly.ui.component.YouTubeGridItem
import com.babelsoftware.loudly.ui.component.shimmer.GridItemPlaceHolder
import com.babelsoftware.loudly.ui.component.shimmer.ShimmerHost
import com.babelsoftware.loudly.ui.component.shimmer.TextPlaceholder
import com.babelsoftware.loudly.ui.menu.AlbumMenu
import com.babelsoftware.loudly.ui.menu.ArtistMenu
import com.babelsoftware.loudly.ui.menu.SongMenu
import com.babelsoftware.loudly.ui.menu.YouTubeAlbumMenu
import com.babelsoftware.loudly.ui.menu.YouTubeArtistMenu
import com.babelsoftware.loudly.ui.menu.YouTubePlaylistMenu
import com.babelsoftware.loudly.ui.menu.YouTubeSongMenu
import com.babelsoftware.loudly.ui.utils.SnapLayoutInfoProvider
import com.babelsoftware.loudly.utils.getLatestReleaseInfo
import com.babelsoftware.loudly.utils.isInternetAvailable
import com.babelsoftware.loudly.utils.rememberPreference
import com.babelsoftware.loudly.viewmodels.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.Calendar
import kotlin.math.min
import kotlin.random.Random

@SuppressLint("SuspiciousIndentation")
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
    playerStyle: PlayerStyle
) {
    // --- YENİ EKLENEN KOD BAŞLANGICI ---

    // 1. Güncelleme durumunu tutacak bir state oluşturuyoruz.
    var updateAvailable by remember { mutableStateOf(false) }

    // 2. HomeScreen ekrana geldiğinde güncelleme kontrolünü BİR KEZ çalıştırıyoruz.
    LaunchedEffect(key1 = Unit) {
        updateAvailable = checkForUpdate()
    }

    // --- YENİ EKLENEN KOD SONU ---
    val menuState = LocalMenuState.current
    val database = LocalDatabase.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val haptic = LocalHapticFeedback.current

    val isPlaying by playerConnection.isPlaying.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    val quickPicks by viewModel.quickPicks.collectAsState()
    val forgottenFavorites by viewModel.forgottenFavorites.collectAsState()
    val keepListening by viewModel.keepListening.collectAsState()
    val similarRecommendations by viewModel.similarRecommendations.collectAsState()
    val accountPlaylists by viewModel.accountPlaylists.collectAsState()
    val homePage by viewModel.homePage.collectAsState()
    val explorePage by viewModel.explorePage.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val recentActivity by viewModel.recentActivity.collectAsState()

    val (showRecentActivity) = rememberPreference(ShowRecentActivityKey, defaultValue = true)
    val (innerTubeCookie) = rememberPreference(InnerTubeCookieKey, "")
    val isLoggedIn = remember(innerTubeCookie) {
        "SAPISID" in parseCookieString(innerTubeCookie)
    }

    val isLoading by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val selectedChip by viewModel.selectedChip.collectAsState()
    val pullRefreshState = rememberPullToRefreshState()

    val quickPicksLazyGridState = rememberLazyGridState()
    val forgottenFavoritesLazyGridState = rememberLazyGridState()

    val scope = rememberCoroutineScope()
    val lazylistState = rememberLazyListState()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val scrollToTop = backStackEntry?.savedStateHandle?.getStateFlow("scrollToTop", false)?.collectAsState()
    val (ytmSync) = rememberPreference(YtmSyncKey, true)
    val context = LocalContext.current
    var showNoInternetDialog by rememberPreference(showNoInternetDialogKey, defaultValue = false)

    LaunchedEffect(scrollToTop?.value) {
        if (scrollToTop?.value == true) {
            lazylistState.animateScrollToItem(0)
            backStackEntry?.savedStateHandle?.set("scrollToTop", false)
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { lazylistState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                val len = lazylistState.layoutInfo.totalItemsCount
                if (lastVisibleIndex != null && lastVisibleIndex >= len - 3) {
                    viewModel.loadMoreYouTubeItems(homePage?.continuation)
                }
            }
    }

    if (!isInternetAvailable(context)) {
        LaunchedEffect(Unit) {
            showNoInternetDialog = true
        }
    }

    if (selectedChip != null) {
        BackHandler {
            viewModel.toggleChip(null)
        }
    }

    val localGridItem: @Composable (LocalItem) -> Unit = {
        when (it) {
            is Song -> SongGridItem(
                song = it,
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = {
                            if (it.id == mediaMetadata?.id) {
                                playerConnection.player.togglePlayPause()
                            } else {
                                playerConnection.playQueue(
                                    YouTubeQueue.Companion.radio(it.toMediaMetadata()),
                                )
                            }
                        },
                        onLongClick = {
                            haptic.performHapticFeedback(
                                HapticFeedbackType.LongPress,
                            )
                            menuState.show {
                                SongMenu(
                                    originalSong = it,
                                    navController = navController,
                                    onDismiss = menuState::dismiss,
                                )
                            }
                        },
                    ),
                isActive = it.id == mediaMetadata?.id,
                isPlaying = isPlaying,
            )

            is Album -> AlbumGridItem(
                album = it,
                isActive = it.id == mediaMetadata?.album?.id,
                isPlaying = isPlaying,
                coroutineScope = scope,
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = {
                            navController.navigate("album/${it.id}")
                        },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            menuState.show {
                                AlbumMenu(
                                    originalAlbum = it,
                                    navController = navController,
                                    onDismiss = menuState::dismiss
                                )
                            }
                        }
                    )
            )

            is Artist -> ArtistGridItem(
                artist = it,
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = {
                            navController.navigate("artist/${it.id}")
                        },
                        onLongClick = {
                            haptic.performHapticFeedback(
                                HapticFeedbackType.LongPress,
                            )
                            menuState.show {
                                ArtistMenu(
                                    originalArtist = it,
                                    coroutineScope = scope,
                                    onDismiss = menuState::dismiss,
                                )
                            }
                        },
                    ),
            )

            is Playlist -> {}
        }
    }

    val ytGridItem: @Composable (YTItem) -> Unit = { item ->
        val itemShape = when (item) {
            is ArtistItem -> CircleShape
            else -> RoundedCornerShape(ThumbnailCornerRadius)
        }
        YouTubeGridItem(
            item = item,
            isActive = item.id in listOf(mediaMetadata?.album?.id, mediaMetadata?.id),
            isPlaying = isPlaying,
            coroutineScope = scope,
            thumbnailRatio = 1f,
            shape = itemShape,
            modifier = Modifier
                .combinedClickable(
                    onClick = {
                        when (item) {
                            is SongItem -> playerConnection.playQueue(
                                YouTubeQueue(
                                    item.endpoint ?: WatchEndpoint(videoId = item.id),
                                    item.toMediaMetadata()
                                )
                            )

                            is AlbumItem -> navController.navigate("album/${item.id}")
                            is ArtistItem -> navController.navigate("artist/${item.id}")
                            is PlaylistItem -> navController.navigate("online_playlist/${item.id}")
                        }
                    },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        menuState.show {
                            when (item) {
                                is SongItem -> YouTubeSongMenu(
                                    song = item,
                                    navController = navController,
                                    onDismiss = menuState::dismiss
                                )

                                is AlbumItem -> YouTubeAlbumMenu(
                                    albumItem = item,
                                    navController = navController,
                                    onDismiss = menuState::dismiss
                                )

                                is ArtistItem -> YouTubeArtistMenu(
                                    artist = item,
                                    onDismiss = menuState::dismiss
                                )

                                is PlaylistItem -> YouTubePlaylistMenu(
                                    navController = navController,
                                    playlist = item,
                                    coroutineScope = scope,
                                    onDismiss = menuState::dismiss
                                )
                            }
                        }
                    }
                )
        )
    }

    LaunchedEffect(quickPicks) {
        quickPicksLazyGridState.scrollToItem(0)
    }

    LaunchedEffect(forgottenFavorites) {
        forgottenFavoritesLazyGridState.scrollToItem(0)
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .pullToRefresh(
                state = pullRefreshState,
                isRefreshing = isRefreshing,
                onRefresh = viewModel::refresh
            ),
        contentAlignment = Alignment.TopStart
    ) {
        if (playerStyle == PlayerStyle.UI_2_0) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {}
        }

        val horizontalLazyGridItemWidthFactor = if (maxWidth * 0.475f >= 320.dp) 0.475f else 0.9f
        val horizontalLazyGridItemWidth = maxWidth * horizontalLazyGridItemWidthFactor
        val quickPicksSnapLayoutInfoProvider = remember(quickPicksLazyGridState) {
            SnapLayoutInfoProvider(
                lazyGridState = quickPicksLazyGridState,
                positionInLayout = { layoutSize, itemSize ->
                    (layoutSize * horizontalLazyGridItemWidthFactor / 2f - itemSize / 2f)
                }
            )
        }
        val forgottenFavoritesSnapLayoutInfoProvider = remember(forgottenFavoritesLazyGridState) {
            SnapLayoutInfoProvider(
                lazyGridState = forgottenFavoritesLazyGridState,
                positionInLayout = { layoutSize, itemSize ->
                    (layoutSize * horizontalLazyGridItemWidthFactor / 2f - itemSize / 2f)
                }
            )
        }

        val recentActivityGridState = rememberLazyGridState()
        val (showContentFilter) = rememberPreference(ShowContentFilterKey, defaultValue = true)

        if (showNoInternetDialog) {
            AlertDialog(
                onDismissRequest = { showNoInternetDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.signal_cellular_nodata),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.not_internet))
                    }
                },
                text = { Text(stringResource(R.string.internet_required)) },
                confirmButton = {
                    Button(onClick = {
                        showNoInternetDialog = false
                    }) {
                        Text(stringResource(android.R.string.cancel))
                    }
                },
                dismissButton = {
                    Button(onClick = {
                        navController.navigate("library")
                    }) {
                        Text(stringResource(R.string.filter_library))
                    }
                }
            )
        }

        LazyColumn(
            state = lazylistState,
            contentPadding = WindowInsets.systemBars
                .only(WindowInsetsSides.Horizontal)
                .add(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Vertical))
                .asPaddingValues(LocalDensity.current)
        ) {
            // ==================== CONDITIONAL INTERFACE LOGIC ====================
            if (playerStyle == PlayerStyle.UI_2_0) {
                // ============== HOME UI 2.0 ==============
                if (selectedChip == null) {
                    item {
                        HomeHeaderUI20(
                            modifier = Modifier.animateItem(),
                            homePage = homePage,
                            onChipClick = { chip -> viewModel.toggleChip(chip) },
                            updateAvailable = updateAvailable
                        )
                    }
                } else {
                    item {
                        val currentChips = homePage?.chips
                        if (!currentChips.isNullOrEmpty() && showContentFilter) {
                            ChipsRow(
                                chips = currentChips.mapNotNull { chip -> chip?.let { it to it.title } },
                                currentValue = selectedChip,
                                onValueUpdate = { viewModel.toggleChip(it) }
                            )
                        }
                    }
                }

                if (selectedChip == null && showRecentActivity && isLoggedIn && !recentActivity.isNullOrEmpty()) {
                    item {
                        NavigationTitle(
                            title = stringResource(R.string.recent_activity),
                            modifier = Modifier
                                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                                .animateItem()
                        )
                    }
                    item {
                        LazyHorizontalGrid(
                            state = recentActivityGridState,
                            rows = GridCells.Fixed(4),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            modifier = Modifier
                                .height(60.dp * 4)
                                .animateItem()
                        ) {
                            items(
                                items = recentActivity!!,
                                key = { it.id }
                            ) { item ->
                                YouTubeCardItem(
                                    item,
                                    onClick = {
                                        when (item.type) {
                                            RecentActivityType.PLAYLIST -> {
                                                val playlistDb = playlists
                                                    ?.firstOrNull { it.playlist.browseId == item.id }

                                                if (playlistDb != null && playlistDb.songCount != 0)
                                                    navController.navigate("local_playlist/${playlistDb.id}")
                                                else
                                                    navController.navigate("online_playlist/${item.id}")
                                            }
                                            RecentActivityType.ALBUM -> navController.navigate("album/${item.id}")
                                            RecentActivityType.ARTIST -> navController.navigate("artist/${item.id}")
                                        }
                                    },
                                )
                            }
                        }
                    }
                }

                homePage?.sections?.forEach { section ->
                    item {
                        NavigationTitle(
                            title = section.title,
                            onClick = if (section.endpoint != null) { { navController.navigate("youtube_browse/${section.endpoint!!.browseId}?params=${section.endpoint!!.params}") } } else null,
                            modifier = Modifier
                                .animateItem()
                                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                        )
                    }
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.animateItem()
                        ) {
                            items(section.items) { item ->
                                ytGridItem(item)
                            }
                        }
                    }
                }

            } else {
                // ============== CURRENT UI (1.0 and 1.5) APPEARANCE ==============
                item {
                    val currentChips = homePage?.chips
                    if (!currentChips.isNullOrEmpty() && showContentFilter) {
                        ChipsRow(
                            chips = currentChips.mapNotNull { chip ->
                                chip?.let { it to it.title }
                            },
                            currentValue = selectedChip,
                            onValueUpdate = {
                                viewModel.toggleChip(it)
                            }
                        )
                    }
                }

                if (selectedChip == null) {
                    item {
                        HomeGreetingCard(
                            updateAvailable = updateAvailable,
                            modifier = Modifier
                                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)
                                .animateItem()
                        )
                    }
                }

                if (selectedChip == null && showRecentActivity && isLoggedIn && !recentActivity.isNullOrEmpty()) {
                    item {
                        NavigationTitle(title = stringResource(R.string.recent_activity))
                    }
                    item {
                        LazyHorizontalGrid(
                            state = recentActivityGridState,
                            rows = GridCells.Fixed(4),
                            flingBehavior = rememberSnapFlingBehavior(
                                forgottenFavoritesLazyGridState
                            ),
                            contentPadding = WindowInsets.systemBars
                                .only(WindowInsetsSides.Horizontal)
                                .asPaddingValues(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp * 4)
                                .padding(6.dp)
                        ) {
                            items(
                                items = recentActivity!!,
                                key = { it.id }
                            ) { item ->
                                YouTubeCardItem(
                                    item,
                                    onClick = {
                                        when (item.type) {
                                            RecentActivityType.PLAYLIST -> {
                                                val playlistDb = playlists
                                                    ?.firstOrNull { it.playlist.browseId == item.id }

                                                if (playlistDb != null && playlistDb.songCount != 0)
                                                    navController.navigate("local_playlist/${playlistDb.id}")
                                                else
                                                    navController.navigate("online_playlist/${item.id}")
                                            }

                                            RecentActivityType.ALBUM -> navController.navigate("album/${item.id}")

                                            RecentActivityType.ARTIST -> navController.navigate("artist/${item.id}")
                                        }
                                    },
                                )
                            }
                        }
                    }
                }

                if (selectedChip == null) {
                    quickPicks?.takeIf { it.isNotEmpty() }?.let { quickPicks ->
                        item {
                            NavigationTitle(
                                title = stringResource(R.string.quick_picks),
                                modifier = Modifier.animateItem()
                            )
                        }

                        item {
                            LazyHorizontalGrid(
                                state = quickPicksLazyGridState,
                                rows = GridCells.Fixed(4),
                                flingBehavior = rememberSnapFlingBehavior(
                                    quickPicksSnapLayoutInfoProvider
                                ),
                                contentPadding = WindowInsets.systemBars
                                    .only(WindowInsetsSides.Horizontal)
                                    .asPaddingValues(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(ListItemHeight * 4)
                                    .animateItem()
                            ) {
                                items(
                                    items = quickPicks,
                                    key = { it.id }
                                ) { originalSong ->
                                    val song by database.song(originalSong.id)
                                        .collectAsState(initial = originalSong)

                                    SongListItem(
                                        song = song!!,
                                        showInLibraryIcon = true,
                                        isActive = song!!.id == mediaMetadata?.id,
                                        isPlaying = isPlaying,
                                        isSwipeable = false,
                                        trailingContent = {
                                            IconButton(
                                                onClick = {
                                                    menuState.show {
                                                        SongMenu(
                                                            originalSong = song!!,
                                                            navController = navController,
                                                            onDismiss = menuState::dismiss
                                                        )
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    painter = painterResource(R.drawable.more_vert),
                                                    contentDescription = null
                                                )
                                            }
                                        },
                                        modifier = Modifier
                                            .width(horizontalLazyGridItemWidth)
                                            .combinedClickable(
                                                onClick = {
                                                    if (song!!.id == mediaMetadata?.id) {
                                                        playerConnection.player.togglePlayPause()
                                                    } else {
                                                        playerConnection.playQueue(
                                                            YouTubeQueue.Companion.radio(
                                                                song!!.toMediaMetadata()
                                                            )
                                                        )
                                                    }
                                                },
                                                onLongClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    menuState.show {
                                                        SongMenu(
                                                            originalSong = song!!,
                                                            navController = navController,
                                                            onDismiss = menuState::dismiss
                                                        )
                                                    }
                                                }
                                            )
                                    )
                                }
                            }
                        }
                    }

                    forgottenFavorites?.takeIf { it.isNotEmpty() }?.let { forgottenFavorites ->
                        item {
                            NavigationTitle(
                                title = stringResource(R.string.forgotten_favorites),
                                modifier = Modifier.animateItem()
                            )
                        }

                        item {
                            val rows = min(4, forgottenFavorites.size)
                            LazyHorizontalGrid(
                                state = forgottenFavoritesLazyGridState,
                                rows = GridCells.Fixed(rows),
                                flingBehavior = rememberSnapFlingBehavior(
                                    forgottenFavoritesSnapLayoutInfoProvider
                                ),
                                contentPadding = WindowInsets.systemBars
                                    .only(WindowInsetsSides.Horizontal)
                                    .asPaddingValues(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(ListItemHeight * rows)
                                    .animateItem()
                            ) {
                                items(
                                    items = forgottenFavorites,
                                    key = { it.id }
                                ) { originalSong ->
                                    val song by database.song(originalSong.id)
                                        .collectAsState(initial = originalSong)

                                    SongListItem(
                                        song = song!!,
                                        showInLibraryIcon = true,
                                        isActive = song!!.id == mediaMetadata?.id,
                                        isPlaying = isPlaying,
                                        isSwipeable = false,
                                        modifier = Modifier
                                            .width(horizontalLazyGridItemWidth)
                                            .combinedClickable(
                                                onClick = {
                                                    if (song!!.id == mediaMetadata?.id) {
                                                        playerConnection.player.togglePlayPause()
                                                    } else {
                                                        playerConnection.playQueue(
                                                            YouTubeQueue.Companion.radio(
                                                                song!!.toMediaMetadata()
                                                            )
                                                        )
                                                    }
                                                },
                                                onLongClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    menuState.show {
                                                        SongMenu(
                                                            originalSong = song!!,
                                                            navController = navController,
                                                            onDismiss = menuState::dismiss
                                                        )
                                                    }
                                                }
                                            )
                                    )
                                }
                            }
                        }
                    }

                    keepListening?.takeIf { it.isNotEmpty() }?.let { fullList ->
                        val featuredItem = fullList.firstOrNull { it is Album || it is Artist }
                        val remainingItems = if (featuredItem != null) fullList.minus(featuredItem) else fullList
                        if (featuredItem != null) {
                            item {
                                FeaturedContentCard(
                                    item = featuredItem,
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                        .animateItem(),
                                    onClick = {
                                        when (featuredItem) {
                                            is Album -> navController.navigate("album/${featuredItem.id}")
                                            is Artist -> navController.navigate("artist/${featuredItem.id}")
                                            else -> {
                                            }
                                        }
                                    }
                                )
                            }
                        }

                        if (remainingItems.isNotEmpty()) {
                            item {
                                NavigationTitle(
                                    title = stringResource(R.string.keep_listening),
                                    modifier = Modifier.animateItem()
                                )
                            }
                            item {
                                val rows = if (remainingItems.size > 6) 2 else 1
                                LazyHorizontalGrid(
                                    state = rememberLazyGridState(),
                                    rows = GridCells.Fixed(rows),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height((GridThumbnailHeight + 24.dp + with(LocalDensity.current) {
                                            MaterialTheme.typography.bodyLarge.lineHeight.toDp() * 2 +
                                                    MaterialTheme.typography.bodyMedium.lineHeight.toDp() * 2
                                        }) * rows)
                                        .animateItem()
                                ) {
                                    items(remainingItems) {
                                        localGridItem(it)
                                    }
                                }
                            }
                        }
                    }
                }

                if (ytmSync && selectedChip == null) {
                    accountPlaylists?.takeIf { it.isNotEmpty() }?.let { accountPlaylists ->
                        item {
                            NavigationTitle(
                                title = stringResource(R.string.your_youtube_playlists),
                                onClick = {
                                    navController.navigate("YouTubePlaylists")
                                },
                                modifier = Modifier.animateItem()
                            )
                        }

                        item {
                            LazyRow(
                                contentPadding = WindowInsets.systemBars
                                    .only(WindowInsetsSides.Horizontal)
                                    .asPaddingValues(),
                                modifier = Modifier.animateItem()
                            ) {
                                items(
                                    items = accountPlaylists,
                                    key = { it.id },
                                ) { item ->
                                    ytGridItem(item)
                                }
                            }
                        }
                    }
                }

                if (selectedChip == null) {
                    similarRecommendations?.forEach {
                        item {
                            NavigationTitle(
                                label = stringResource(R.string.similar_to),
                                title = it.title.title,
                                thumbnail = it.title.thumbnailUrl?.let { thumbnailUrl ->
                                    {
                                        val shape = RoundedCornerShape(ThumbnailCornerRadius)
                                        AsyncImage(
                                            model = thumbnailUrl,
                                            contentDescription = null,
                                            modifier = Modifier.Companion
                                                .size(ListThumbnailSize)
                                                .clip(shape)
                                        )
                                    }
                                },
                                onClick = {
                                    when (it.title) {
                                        is Song -> navController.navigate("album/${it.title.album!!.id}")
                                        is Album -> navController.navigate("album/${it.title.id}")
                                        is Artist -> navController.navigate("artist/${it.title.id}")
                                        is Playlist -> {}
                                    }
                                },
                                modifier = Modifier.animateItem()
                            )
                        }

                        item {
                            LazyRow(
                                contentPadding = WindowInsets.systemBars
                                    .only(WindowInsetsSides.Horizontal)
                                    .asPaddingValues(),
                                modifier = Modifier.animateItem()
                            ) {
                                items(it.items) { item ->
                                    ytGridItem(item)
                                }
                            }
                        }
                    }
                }

                homePage?.sections?.forEach {
                    item {
                        NavigationTitle(
                            title = it.title,
                            label = it.label,
                            thumbnail = it.thumbnail?.let { thumbnailUrl ->
                                {
                                    val shape = RoundedCornerShape(ThumbnailCornerRadius)
                                    AsyncImage(
                                        model = thumbnailUrl,
                                        contentDescription = null,
                                        modifier = Modifier.Companion
                                            .size(ListThumbnailSize)
                                            .clip(shape)
                                    )
                                }
                            },
                            modifier = Modifier.animateItem()
                        )
                    }

                    item {
                        when (it.sectionType) {
                            HomePage.SectionType.LIST -> {
                                LazyRow(
                                    contentPadding = WindowInsets.systemBars
                                        .only(WindowInsetsSides.Horizontal)
                                        .asPaddingValues(),
                                    modifier = Modifier.animateItem()
                                ) {
                                    items(it.items) { item ->
                                        ytGridItem(item)
                                    }
                                }
                            }

                            HomePage.SectionType.GRID -> {
                                LazyRow (
                                    contentPadding = WindowInsets.systemBars
                                        .only(WindowInsetsSides.Horizontal)
                                        .asPaddingValues(),
                                    modifier = Modifier.animateItem()
                                ) {
                                    items(it.items) { item ->
                                        ytGridItem(item)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // =================================================================

            if (isLoading) {
                item {
                    ShimmerHost(
                        modifier = Modifier.animateItem()
                    ) {
                        TextPlaceholder(
                            height = 36.dp,
                            modifier = Modifier
                                .padding(12.dp)
                                .width(250.dp),
                        )
                        LazyRow {
                            items(4) {
                                GridItemPlaceHolder()
                            }
                        }
                    }
                }
            }
        }

        HideOnScrollFAB(
            visible = !quickPicks.isNullOrEmpty() || explorePage?.newReleaseAlbums?.isNotEmpty() == true,
            lazyListState = lazylistState,
            icon = (R.drawable.casino),
            onClick = {
                if (Random.nextBoolean() && !quickPicks.isNullOrEmpty()) {
                    val song = quickPicks!!.random()
                    playerConnection.playQueue(
                        YouTubeQueue(
                            WatchEndpoint(videoId = song.id),
                            song.toMediaMetadata()
                        )
                    )
                } else if (explorePage?.newReleaseAlbums?.isNotEmpty() == true) {
                    val album = explorePage?.newReleaseAlbums!!.random()
                    playerConnection.playQueue(YouTubeAlbumRadio(album.playlistId))
                }
            }
        )

        Indicator(
            isRefreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(LocalPlayerAwareWindowInsets.current.asPaddingValues()),
        )
    }
}

@Composable
private fun HomeGreetingCard(
    modifier: Modifier = Modifier,
    updateAvailable: Boolean
) {
    val accountName by rememberPreference(AccountNameKey, "")
    val innerTubeCookie by rememberPreference(InnerTubeCookieKey, "")
    val isLoggedIn = remember(innerTubeCookie) { "SAPISID" in parseCookieString(innerTubeCookie) }
    val (headerImageKey, _) = rememberPreference(HeaderImageKey, "Loudly-1")

    val painter = when {
        headerImageKey.startsWith("content://") -> {
            rememberAsyncImagePainter(model = Uri.parse(headerImageKey))
        }
        headerImageKey == "Loudly-1" -> painterResource(id = R.drawable.loudly_picutre_1)
        headerImageKey == "Loudly-2" -> painterResource(id = R.drawable.loudly_picutre_2)
        headerImageKey == "Loudly-3" -> painterResource(id = R.drawable.loudly_picutre_3)
        else -> painterResource(id = R.drawable.loudly_picutre_1)
    }

    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (currentHour) {
        in 6..11 -> stringResource(R.string.good_morning)
        in 12..17 -> stringResource(R.string.good_afternoon)
        in 18..23 -> stringResource(R.string.good_evening)
        else -> stringResource(R.string.good_night)
    }

    var showUpdateMessage by remember { mutableStateOf(false) }
    LaunchedEffect(updateAvailable) {
        showUpdateMessage = false
        if (updateAvailable) {
            delay(3000)
            showUpdateMessage = true
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomStart
        ) {
            Image(
                painter = painter,
                contentDescription = "Header Background",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                            startY = 150f
                        )
                    )
            )
            AnimatedContent(
                targetState = showUpdateMessage,
                modifier = Modifier.padding(20.dp),
                transitionSpec = {
                    (slideInVertically { height -> height } + fadeIn())
                        .togetherWith(slideOutVertically { height -> -height } + fadeOut())
                },
                label = "GreetingCardAnimation"
            ) { shouldShowUpdate ->
                Column(
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = if (shouldShowUpdate) Alignment.CenterHorizontally else Alignment.Start,
                    modifier = if (shouldShowUpdate) Modifier.fillMaxWidth() else Modifier
                ) {
                    if (!shouldShowUpdate) {
                        if (isLoggedIn && accountName.isNotBlank()) {
                            Text(
                                text = greeting,
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = accountName.replace("@", ""),
                                color = Color.White,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else {
                            Text(
                                stringResource(R.string.welcome_to_loudly),
                                color = Color.White,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.new_version_available),
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.home_card_update_desc),
                            color = Color.White.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeHeaderUI20(
    modifier: Modifier = Modifier,
    homePage: HomePage?,
    onChipClick: (HomePage.Chip?) -> Unit,
    updateAvailable: Boolean
) {
    val accountName by rememberPreference(AccountNameKey, "")
    val innerTubeCookie by rememberPreference(InnerTubeCookieKey, "")
    val isLoggedIn = remember(innerTubeCookie) { "SAPISID" in parseCookieString(innerTubeCookie) }

    var headerState by remember { mutableStateOf("INITIAL") }

    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (currentHour) {
        in 6..11 -> stringResource(R.string.good_morning)
        in 12..17 -> stringResource(R.string.good_afternoon)
        in 18..23 -> stringResource(R.string.good_evening)
        else -> stringResource(R.string.good_night)
    }

    val recommendedForYouText = stringResource(R.string.recommended_for_you_today)
    val updateAvailableText = stringResource(R.string.new_version_available)

    LaunchedEffect(updateAvailable, isLoggedIn) {
        val sequence = mutableListOf<String>()
        sequence.add("INITIAL")

        if (isLoggedIn) {
            sequence.add("SECONDARY")
        }
        if (updateAvailable) {
            sequence.add("UPDATE")
        }
        if (sequence.size <= 1) {
            headerState = "INITIAL"
            return@LaunchedEffect
        }
        for (i in sequence.indices) {
            headerState = sequence[i]

            // Son adımdan sonra bekleme yapma.
            if (i < sequence.size - 1) {
                delay(1500)
            }
        }
    }

    val abstractColors = listOf(
        Color(0xFFF3E5C3), Color(0xFF174E4F),
        Color(0xFFF953C6), Color(0xFFB91D73),
        Color(0xFFFF512F), Color(0xFFDD2476),
        Color(0xFF00C9FF), Color(0xFF92FE9D),
        Color(0xFFF7971E), Color(0xFFFFD200),
        Color(0xFF0052D4), Color(0xFF4364F7),
        Color(0xFF2772A0), Color(0xFFCCDDEA),
        Color(0xFF5C4F6E), Color(0xFFB3ABC9)
    )

    Column(modifier = modifier) {
        AnimatedContent(
            targetState = headerState,
            transitionSpec = {
                (slideInVertically { height -> height } + fadeIn())
                    .togetherWith(slideOutVertically { height -> -height } + fadeOut())
            },
            label = "GreetingTitleAnimation"
        ) { state ->
            val titleText = when (state) {
                "INITIAL" -> if (isLoggedIn) greeting else recommendedForYouText
                "SECONDARY" -> accountName.replace("@", "")
                "UPDATE" -> updateAvailableText
                else -> if (isLoggedIn) greeting else recommendedForYouText
            }
            Text(
                text = titleText,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )
        }

        val currentChips = homePage?.chips
        if (!currentChips.isNullOrEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(currentChips) { chip ->
                    chip?.let {
                        val colorIndex = (it.title.hashCode() % abstractColors.size).coerceAtLeast(0)
                        val startColor = abstractColors[colorIndex]
                        val endColor = abstractColors[(colorIndex + 1) % abstractColors.size]

                        AbstractChipButton(
                            chip = it,
                            startColor = startColor,
                            endColor = endColor,
                            onClick = { onChipClick(it) }
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun AbstractChipButton(
    chip: HomePage.Chip,
    startColor: Color,
    endColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(60.dp),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(colors = listOf(startColor, endColor))),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.4f), Color.Transparent),
                            startX = 0f,
                            endX = 250f
                        )
                    )
            )
            Text(
                text = chip.title,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private suspend fun checkForUpdate(): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            val latestInfo = getLatestReleaseInfo()
            if (latestInfo != null) {
                isNewerVersion(latestInfo.tagName, BuildConfig.VERSION_NAME)
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}