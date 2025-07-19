package com.babelsoftware.loudly.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import androidx.navigation.NavController
import com.babelsoftware.loudly.LocalPlayerAwareWindowInsets
import com.babelsoftware.loudly.constants.EnableKugouKey
import com.babelsoftware.loudly.constants.EnableLrcLibKey
import com.babelsoftware.loudly.constants.LyricFontSizeKey
import com.babelsoftware.loudly.constants.LyricTrimKey
import com.babelsoftware.loudly.constants.LyricsTextPositionKey
import com.babelsoftware.loudly.constants.MultilineLrcKey
import com.babelsoftware.loudly.constants.PreferredLyricsProvider
import com.babelsoftware.loudly.constants.PreferredLyricsProviderKey
import com.babelsoftware.loudly.ui.component.CounterDialog
import com.babelsoftware.loudly.ui.component.EnumListPreference
import com.babelsoftware.loudly.ui.component.IconButton
import com.babelsoftware.loudly.ui.component.ListPreference
import com.babelsoftware.loudly.ui.component.PreferenceEntry
import com.babelsoftware.loudly.ui.component.PreferenceGroupTitle
import com.babelsoftware.loudly.ui.component.SwitchPreference
import com.babelsoftware.loudly.ui.utils.backToMain
import com.babelsoftware.loudly.utils.rememberEnumPreference
import com.babelsoftware.loudly.utils.rememberPreference
import com.babelsoftware.loudly.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val (enableKugou, onEnableKugouChange) = rememberPreference(
        key = EnableKugouKey,
        defaultValue = true
    )
    val (lyricsPosition, onLyricsPositionChange) = rememberEnumPreference(
        LyricsTextPositionKey,
        defaultValue = LyricsPosition.CENTER
    )
    val (enableLrcLib, onEnableLrcLibChange) = rememberPreference(
        key = EnableLrcLibKey,
        defaultValue = true
    )
    val (preferredProvider, onPreferredProviderChange) = rememberEnumPreference(
        key = PreferredLyricsProviderKey,
        defaultValue = PreferredLyricsProvider.LRCLIB
    )
    val (multilineLrc, onMultilineLrcChange) = rememberPreference(
        MultilineLrcKey,
        defaultValue = true
    )
    val (lyricTrim, onLyricTrimChange) = rememberPreference(LyricTrimKey, defaultValue = false)
    val (lyricFontSize, onLyricFontSizeChange) = rememberPreference(
        LyricFontSizeKey,
        defaultValue = 20
    )

    var showFontSizeDialog by remember {
        mutableStateOf(false)
    }
    if (showFontSizeDialog) {
        CounterDialog(
            title = stringResource(R.string.lyrics_font_size),
            initialValue = lyricFontSize,
            upperBound = 28,
            lowerBound = 10,
            resetValue = 20,
            unitDisplay = " sp",
            onDismiss = { showFontSizeDialog = false },
            onConfirm = {
                onLyricFontSizeChange(it)
                showFontSizeDialog = false
            },
            onCancel = { showFontSizeDialog = false },
            onReset = { onLyricFontSizeChange(20) },
        )
    }
    Column(
        Modifier
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
            .verticalScroll(rememberScrollState())
    ) {
        PreferenceGroupTitle(
            title = stringResource(R.string.main)
        )

        SwitchPreference(
            title = { Text(stringResource(R.string.enable_kugou)) },
            icon = { Icon(Icons.Rounded.Lyrics, null) },
            checked = enableKugou,
            onCheckedChange = onEnableKugouChange
        )
        SwitchPreference(
            title = { Text(stringResource(R.string.enable_lrclib)) },
            icon = { Icon(Icons.Rounded.Lyrics, null) },
            checked = enableLrcLib,
            onCheckedChange = onEnableLrcLibChange
        )
        SwitchPreference(
            title = { Text(stringResource(R.string.lyrics_multiline_title)) },
            description = stringResource(R.string.lyrics_multiline_description),
            icon = { Icon(Icons.AutoMirrored.Rounded.Sort, null) },
            checked = multilineLrc,
            onCheckedChange = onMultilineLrcChange
        )
        SwitchPreference(
            title = { Text(stringResource(R.string.lyrics_trim_title)) },
            icon = { Icon(Icons.Rounded.ContentCut, null) },
            checked = lyricTrim,
            onCheckedChange = onLyricTrimChange
        )

        EnumListPreference(
            title = { Text(stringResource(R.string.lyrics_text_position)) },
            icon = { Icon(Icons.Rounded.Lyrics, null) },
            selectedValue = lyricsPosition,
            onValueSelected = onLyricsPositionChange,
            valueText = {
                when (it) {
                    LyricsPosition.LEFT -> stringResource(R.string.left)
                    LyricsPosition.CENTER -> stringResource(R.string.center)
                    LyricsPosition.RIGHT -> stringResource(R.string.right)
                }
            }
        )
        PreferenceEntry(
            title = { Text(stringResource(R.string.lyrics_font_size)) },
            description = "$lyricFontSize sp",
            icon = { Icon(Icons.Rounded.TextFields, null) },
            onClick = { showFontSizeDialog = true }
        )

        PreferenceGroupTitle(
            title = stringResource(R.string.misc)
        )

        ListPreference(
            title = { Text(stringResource(R.string.default_lyrics_provider)) },
            selectedValue = preferredProvider,
            values = listOf(PreferredLyricsProvider.KUGOU, PreferredLyricsProvider.LRCLIB),
            valueText = {
                it.name.toLowerCase(Locale.current)
                    .capitalize(Locale.current)
            },
            onValueSelected = onPreferredProviderChange
        )
    }
    TopAppBar(
        title = { Text(stringResource(R.string.lyrics_settings_title)) },
        navigationIcon = {
            IconButton(
                onClick = navController::navigateUp,
                onLongClick = navController::backToMain
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = null
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}