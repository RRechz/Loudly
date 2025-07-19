package com.babelsoftware.loudly.ui.screens.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.BluetoothConnected
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.SdCard
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.babelsoftware.innertube.utils.parseCookieString
import com.babelsoftware.loudly.LocalPlayerAwareWindowInsets
import com.babelsoftware.loudly.R
import com.babelsoftware.loudly.constants.AddingPlayedSongsToYTMHistoryKey
import com.babelsoftware.loudly.constants.AudioNormalizationKey
import com.babelsoftware.loudly.constants.AudioOffload
import com.babelsoftware.loudly.constants.AudioQuality
import com.babelsoftware.loudly.constants.AudioQualityKey
import com.babelsoftware.loudly.constants.AutoLoadMoreKey
import com.babelsoftware.loudly.constants.AutoPlaySongWhenBluetoothDeviceConnectedKey
import com.babelsoftware.loudly.constants.AutoSkipNextOnErrorKey
import com.babelsoftware.loudly.constants.InnerTubeCookieKey
import com.babelsoftware.loudly.constants.PersistentQueueKey
import com.babelsoftware.loudly.constants.SkipSilenceKey
import com.babelsoftware.loudly.constants.StopMusicOnTaskClearKey
import com.babelsoftware.loudly.constants.StopPlayingSongWhenMinimumVolumeKey
import com.babelsoftware.loudly.constants.minPlaybackDurKey
import com.babelsoftware.loudly.ui.component.CounterDialog
import com.babelsoftware.loudly.ui.component.EnumListPreference
import com.babelsoftware.loudly.ui.component.IconButton
import com.babelsoftware.loudly.ui.component.PreferenceEntry
import com.babelsoftware.loudly.ui.component.SwitchPreference
import com.babelsoftware.loudly.ui.screens.settings.card_design.SettingCategory
import com.babelsoftware.loudly.ui.screens.settings.card_design.SettingsBox
import com.babelsoftware.loudly.ui.screens.settings.card_design.shapeManager
import com.babelsoftware.loudly.ui.utils.backToMain
import com.babelsoftware.loudly.utils.rememberEnumPreference
import com.babelsoftware.loudly.utils.rememberPreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val (audioQuality, onAudioQualityChange) = rememberEnumPreference(AudioQualityKey, defaultValue = AudioQuality.AUTO)
    val (persistentQueue, onPersistentQueueChange) = rememberPreference(PersistentQueueKey, defaultValue = true)
    val (skipSilence, onSkipSilenceChange) = rememberPreference(SkipSilenceKey, defaultValue = false)
    val (audioNormalization, onAudioNormalizationChange) = rememberPreference(AudioNormalizationKey, defaultValue = true)
    val (autoPlaySongWhenBluetoothDeviceConnected, onAutoPlaySongWhenBluetoothDeviceConnectedChange) = rememberPreference(AutoPlaySongWhenBluetoothDeviceConnectedKey, defaultValue = true)
    val (stopPlayingSongWhenMinimumVolume, onStopPlayingSongWhenMinimumVolumeChange) = rememberPreference(StopPlayingSongWhenMinimumVolumeKey, defaultValue = true)
    val (autoSkipNextOnError, onAutoSkipNextOnErrorChange) = rememberPreference(AutoSkipNextOnErrorKey, defaultValue = false)
    val (stopMusicOnTaskClear, onStopMusicOnTaskClearChange) = rememberPreference(StopMusicOnTaskClearKey, defaultValue = false)
    val (autoLoadMore, onAutoLoadMoreChange) = rememberPreference(AutoLoadMoreKey, defaultValue = true)
    val (minPlaybackDur, onMinPlaybackDurChange) = rememberPreference(minPlaybackDurKey, defaultValue = 30)
    val (audioOffload, onAudioOffloadChange) = rememberPreference(key = AudioOffload, defaultValue = false)
    val (addingPlayedSongsToYtmHistory, onAddingPlayedSongsToYtmHistoryChange) = rememberPreference(AddingPlayedSongsToYTMHistoryKey, defaultValue = true)
    val innerTubeCookie by rememberPreference(InnerTubeCookieKey, "")
    val isLoggedIn = remember(innerTubeCookie) { "SAPISID" in parseCookieString(innerTubeCookie) }

    var showMinPlaybackDur by remember { mutableStateOf(false) }
    if (showMinPlaybackDur) {
        CounterDialog(
            title = stringResource(R.string.minimum_playback_duration),
            description = stringResource(R.string.minimum_playback_duration_info),
            initialValue = minPlaybackDur,
            upperBound = 100,
            lowerBound = 0,
            resetValue = 30,
            unitDisplay = "%",
            onDismiss = { showMinPlaybackDur = false },
            onConfirm = {
                showMinPlaybackDur = false
                onMinPlaybackDurChange(it)
            },
            onCancel = { showMinPlaybackDur = false },
            onReset = { onMinPlaybackDurChange(30) },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.player_and_audio)) },
                navigationIcon = {
                    IconButton(
                        onClick = navController::navigateUp,
                        onLongClick = navController::backToMain
                    ) {
                        Icon(painterResource(R.drawable.arrow_back), contentDescription = null)
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item { SettingCategory(title = stringResource(R.string.player)) }
            item {
                SettingsBox(shape = shapeManager(isFirst = true)) {
                    EnumListPreference(
                        title = { Text(stringResource(R.string.audio_quality)) },
                        icon = { Icon(painterResource(R.drawable.graphic_eq), null) },
                        selectedValue = audioQuality,
                        onValueSelected = onAudioQualityChange,
                        valueText = {
                            when (it) {
                                AudioQuality.AUTO -> stringResource(R.string.audio_quality_auto)
                                AudioQuality.MAX -> stringResource(R.string.audio_quality_max)
                                AudioQuality.HIGH -> stringResource(R.string.audio_quality_high)
                                AudioQuality.LOW -> stringResource(R.string.audio_quality_low)
                            }
                        }
                    )
                }
            }
            item {
                SettingsBox(shape = shapeManager()) {
                    PreferenceEntry(
                        title = { Text(stringResource(R.string.lyrics_settings_title)) },
                        icon = { Icon(Icons.Rounded.Lyrics, null) },
                        onClick = { navController.navigate("settings/player/lyrics") }
                    )
                }
            }
            item {
                SettingsBox(shape = shapeManager()) {
                    PreferenceEntry(
                        title = { Text(stringResource(R.string.local_player_settings_title)) },
                        icon = { Icon(Icons.Rounded.SdCard, null) },
                        onClick = { navController.navigate("player/local") }
                    )
                }
            }
            item {
                SettingsBox(shape = shapeManager()) {
                    PreferenceEntry(
                        title = { Text(stringResource(R.string.minimum_playback_duration)) },
                        description = "$minPlaybackDur %",
                        icon = { Icon(Icons.Rounded.Sync, null) },
                        onClick = { showMinPlaybackDur = true }
                    )
                }
            }
            item {
                SettingsBox(shape = shapeManager()) {
                    SwitchPreference(
                        title = { Text(stringResource(R.string.skip_silence)) },
                        icon = { Icon(painterResource(R.drawable.fast_forward), null) },
                        checked = skipSilence,
                        onCheckedChange = onSkipSilenceChange
                    )
                }
            }
            item {
                if (isLoggedIn) {
                    SettingsBox(shape = shapeManager()) {
                        SwitchPreference(
                            title = { Text(stringResource(R.string.adding_played_songs_to_ytm_history)) },
                            icon = { Icon(painterResource(R.drawable.history), null) },
                            checked = addingPlayedSongsToYtmHistory,
                            onCheckedChange = onAddingPlayedSongsToYtmHistoryChange
                        )
                    }
                }
            }
            item {
                SettingsBox(shape = shapeManager()) {
                    SwitchPreference(
                        title = { Text(stringResource(R.string.audio_normalization)) },
                        icon = { Icon(Icons.AutoMirrored.Rounded.VolumeUp, null) },
                        checked = audioNormalization,
                        onCheckedChange = onAudioNormalizationChange
                    )
                }
            }
            item {
                SettingsBox(shape = shapeManager()) {
                    SwitchPreference(
                        title = { Text(stringResource(R.string.play_song_when_bluetooth_device_connected)) },
                        icon = { Icon(Icons.Rounded.BluetoothConnected, null) },
                        checked = autoPlaySongWhenBluetoothDeviceConnected,
                        onCheckedChange = onAutoPlaySongWhenBluetoothDeviceConnectedChange,
                    )
                }
            }
            item {
                SettingsBox(shape = shapeManager(isLast = true)) {
                    SwitchPreference(
                        title = { Text(stringResource(R.string.stop_playing_when_song_sound_minimum_volume)) },
                        icon = { Icon(Icons.AutoMirrored.Rounded.VolumeOff, null) },
                        checked = stopPlayingSongWhenMinimumVolume,
                        onCheckedChange = onStopPlayingSongWhenMinimumVolumeChange
                    )
                }
            }

            item { SettingCategory(title = stringResource(R.string.queue)) }
            item {
                SettingsBox(shape = shapeManager(isFirst = true)) {
                    SwitchPreference(
                        title = { Text(stringResource(R.string.persistent_queue)) },
                        description = stringResource(R.string.persistent_queue_desc),
                        icon = { Icon(painterResource(R.drawable.queue_music), null) },
                        checked = persistentQueue,
                        onCheckedChange = onPersistentQueueChange
                    )
                }
            }
            item {
                SettingsBox(shape = shapeManager()) {
                    SwitchPreference(
                        title = { Text(stringResource(R.string.auto_load_more)) },
                        description = stringResource(R.string.auto_load_more_desc),
                        icon = { Icon(painterResource(R.drawable.playlist_add), null) },
                        checked = autoLoadMore,
                        onCheckedChange = onAutoLoadMoreChange
                    )
                }
            }
            item {
                SettingsBox(shape = shapeManager(isLast = true)) {
                    SwitchPreference(
                        title = { Text(stringResource(R.string.auto_skip_next_on_error)) },
                        description = stringResource(R.string.auto_skip_next_on_error_desc),
                        icon = { Icon(painterResource(R.drawable.skip_next), null) },
                        checked = autoSkipNextOnError,
                        onCheckedChange = onAutoSkipNextOnErrorChange
                    )
                }
            }

            item { SettingCategory(title = stringResource(R.string.misc)) }
            item {
                SettingsBox(shape = shapeManager(isFirst = true)) {
                    SwitchPreference(
                        title = { Text(stringResource(R.string.audio_offload)) },
                        description = stringResource(R.string.audio_offload_description),
                        icon = { Icon(Icons.Rounded.Bolt, null) },
                        checked = audioOffload,
                        onCheckedChange = onAudioOffloadChange
                    )
                }
            }
            item {
                SettingsBox(shape = shapeManager(isLast = true)) {
                    SwitchPreference(
                        title = { Text(stringResource(R.string.stop_music_on_task_clear)) },
                        icon = { Icon(painterResource(R.drawable.clear_all), null) },
                        checked = stopMusicOnTaskClear,
                        onCheckedChange = onStopMusicOnTaskClearChange
                    )
                }
            }
        }
    }
}
