package com.babelsoftware.loudly.ui.screens.settings

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Cached
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.DesignServices
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.QueryStats
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.babelsoftware.innertube.utils.parseCookieString
import com.babelsoftware.loudly.LocalPlayerAwareWindowInsets
import com.babelsoftware.loudly.R
import com.babelsoftware.loudly.constants.AppDesignVariantKey
import com.babelsoftware.loudly.constants.AppDesignVariantType
import com.babelsoftware.loudly.constants.AutoPlaylistCachedPlaylistShowKey
import com.babelsoftware.loudly.constants.AutoPlaylistDownloadShowKey
import com.babelsoftware.loudly.constants.AutoPlaylistLikedShowKey
import com.babelsoftware.loudly.constants.AutoPlaylistLocalPlaylistShowKey
import com.babelsoftware.loudly.constants.AutoPlaylistTopPlaylistShowKey
import com.babelsoftware.loudly.constants.AutoPlaylistsCustomizationKey
import com.babelsoftware.loudly.constants.ChipSortTypeKey
import com.babelsoftware.loudly.constants.DarkModeKey
import com.babelsoftware.loudly.constants.DefaultOpenTabKey
import com.babelsoftware.loudly.constants.DefaultOpenTabOldKey
import com.babelsoftware.loudly.constants.DynamicThemeKey
import com.babelsoftware.loudly.constants.GridCellSize
import com.babelsoftware.loudly.constants.GridCellSizeKey
import com.babelsoftware.loudly.constants.HeaderImageKey
import com.babelsoftware.loudly.constants.InnerTubeCookieKey
import com.babelsoftware.loudly.constants.LibraryFilter
import com.babelsoftware.loudly.constants.MiniPlayerStyle
import com.babelsoftware.loudly.constants.MiniPlayerStyleKey
import com.babelsoftware.loudly.constants.PlayerBackgroundStyle
import com.babelsoftware.loudly.constants.PlayerBackgroundStyleKey
import com.babelsoftware.loudly.constants.PlayerStyle
import com.babelsoftware.loudly.constants.PlayerStyleKey
import com.babelsoftware.loudly.constants.PureBlackKey
import com.babelsoftware.loudly.constants.ShowContentFilterKey
import com.babelsoftware.loudly.constants.ShowRecentActivityKey
import com.babelsoftware.loudly.constants.SliderStyle
import com.babelsoftware.loudly.constants.SliderStyleKey
import com.babelsoftware.loudly.constants.SlimNavBarKey
import com.babelsoftware.loudly.constants.SwipeSongToDismissKey
import com.babelsoftware.loudly.constants.SwipeThumbnailKey
import com.babelsoftware.loudly.constants.ThumbnailCornerRadiusV2Key
import com.babelsoftware.loudly.constants.UiPresetKey
import com.babelsoftware.loudly.ui.component.CounterDialog
import com.babelsoftware.loudly.ui.component.DefaultDialog
import com.babelsoftware.loudly.ui.component.EnumListPreference
import com.babelsoftware.loudly.ui.component.IconButton
import com.babelsoftware.loudly.ui.component.ListPreference
import com.babelsoftware.loudly.ui.component.PlayerSliderTrack
import com.babelsoftware.loudly.ui.component.PreferenceEntry
import com.babelsoftware.loudly.ui.component.PreferenceGroupTitle
import com.babelsoftware.loudly.ui.component.SwitchPreference
import com.babelsoftware.loudly.ui.screens.settings.card_design.SettingCategory
import com.babelsoftware.loudly.ui.screens.settings.card_design.SettingsBox
import com.babelsoftware.loudly.ui.screens.settings.card_design.shapeManager
import com.babelsoftware.loudly.ui.utils.backToMain
import com.babelsoftware.loudly.utils.rememberEnumPreference
import com.babelsoftware.loudly.utils.rememberPreference
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import me.saket.squiggles.SquigglySlider

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val (appDesignVariant, onAppDesignVariantChange) = rememberEnumPreference(
        AppDesignVariantKey,
        defaultValue = AppDesignVariantType.NEW
    )
    val (dynamicTheme, onDynamicThemeChange) = rememberPreference(
        DynamicThemeKey,
        defaultValue = true
    )
    val (darkMode, onDarkModeChange) = rememberEnumPreference(
        DarkModeKey,
        defaultValue = DarkMode.AUTO
    )
    val (pureBlack, onPureBlackChange) = rememberPreference(PureBlackKey, defaultValue = false)
    val (autoPlaylistsCustomization, onAutoPlaylistsCustomizationChange) = rememberPreference(
        AutoPlaylistsCustomizationKey, defaultValue = false
    )
    val (autoPlaylistLiked, onAutoPlaylistLikedChange) = rememberPreference(
        AutoPlaylistLikedShowKey,
        defaultValue = true
    )
    val (autoPlaylistDownload, onAutoPlaylistDownloadChange) = rememberPreference(
        AutoPlaylistDownloadShowKey, defaultValue = true
    )
    val (autoPlaylistTopPlaylist, onAutoPlaylistTopPlaylistChange) = rememberPreference(
        AutoPlaylistTopPlaylistShowKey, defaultValue = true
    )
    val (autoPlaylistCached, onAutoPlaylistCachedChange) = rememberPreference(
        AutoPlaylistCachedPlaylistShowKey, defaultValue = true
    )
    val (autoPlaylistLocal, onAutoPlaylistLocalChange) = rememberPreference(
        AutoPlaylistLocalPlaylistShowKey, defaultValue = true
    )
    val (swipeSongToDismiss, onSwipeSongToDismissChange) = rememberPreference(
        SwipeSongToDismissKey,
        defaultValue = true
    )
    val (sliderStyle, onSliderStyleChange) = rememberEnumPreference(
        SliderStyleKey,
        defaultValue = SliderStyle.DEFAULT
    )
    val (defaultOpenTabOld, onDefaultOpenTabOldChange) = rememberEnumPreference(
        DefaultOpenTabOldKey,
        defaultValue = NavigationTabOld.HOME
    )
    val (defaultOpenTab, onDefaultOpenTabChange) = rememberEnumPreference(
        DefaultOpenTabKey,
        defaultValue = NavigationTab.HOME
    )
    val (gridCellSize, onGridCellSizeChange) = rememberEnumPreference(
        GridCellSizeKey,
        defaultValue = GridCellSize.SMALL
    )
    val (defaultChip, onDefaultChipChange) = rememberEnumPreference(
        key = ChipSortTypeKey,
        defaultValue = LibraryFilter.LIBRARY
    )
    val (swipeThumbnail, onSwipeThumbnailChange) = rememberPreference(
        SwipeThumbnailKey,
        defaultValue = true
    )
    val (slimNav, onSlimNavChange) = rememberPreference(SlimNavBarKey, defaultValue = true)
    val (thumbnailCornerRadius, onThumbnailCornerRadius) = rememberPreference(
        ThumbnailCornerRadiusV2Key,
        defaultValue = 6
    )
    val (showContentFilter, onShowContentFilterChange) = rememberPreference(
        ShowContentFilterKey,
        defaultValue = true
    )
    val (showRecentActivity,onShowRecentActivityChange) = rememberPreference(
        ShowRecentActivityKey,
        defaultValue = true
    )
    val (playerStyle, onPlayerStyle) = rememberEnumPreference(
        PlayerStyleKey,
        defaultValue = PlayerStyle.NEW
    )
    val (miniPlayerStyle, onMiniPlayerStyle) = rememberEnumPreference(
        MiniPlayerStyleKey,
        defaultValue = MiniPlayerStyle.NEW
    )
    val (innerTubeCookie) = rememberPreference(InnerTubeCookieKey, "")
    val isLoggedIn = remember(innerTubeCookie) {
        "SAPISID" in parseCookieString(innerTubeCookie)
    }

    val isSystemInDarkTheme = isSystemInDarkTheme()
    val useDarkTheme = remember(darkMode, isSystemInDarkTheme) { if (darkMode == DarkMode.AUTO) isSystemInDarkTheme else darkMode == DarkMode.ON }
    val (playerBackground, onPlayerBackgroundChange) = rememberEnumPreference( PlayerBackgroundStyleKey, defaultValue = PlayerBackgroundStyle.DEFAULT,)
    var showCornerRadiusDialog by remember { mutableStateOf(false) }

    val (headerImage, onHeaderImageChange) = rememberPreference(HeaderImageKey, "Loudly-1")
    var showHeaderImageDialog by remember { mutableStateOf(false) }

    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

    val cropActivityTitle = stringResource(id = R.string.edit_photo)
    val cropButtonTitle = stringResource(id = R.string.save)
    val toolbarColor = MaterialTheme.colorScheme.surface
    val toolbarWidgetColor = MaterialTheme.colorScheme.onSurface

    val imageCropper = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            tempImageUri = result.uriContent
        }
    }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let {
            val cropOptions = CropImageContractOptions(
                uri = it,
                cropImageOptions = CropImageOptions(
                    aspectRatioX = 16,
                    aspectRatioY = 9,
                    fixAspectRatio = true,
                    guidelines = CropImageView.Guidelines.ON,
                    outputCompressQuality = 70,
                    activityTitle = cropActivityTitle,
                    toolbarColor = toolbarColor.toArgb(),
                    toolbarBackButtonColor = toolbarWidgetColor.toArgb(),
                    activityMenuIconColor = toolbarWidgetColor.toArgb(),
                    borderLineColor = toolbarWidgetColor.toArgb(),
                    cropMenuCropButtonTitle = cropButtonTitle
                )
            )
            imageCropper.launch(cropOptions)
        }
    }

    if (showHeaderImageDialog) {
        HeaderImagePickerDialog(
            initialValue = headerImage,
            tempSelectedUri = tempImageUri,
            onDismiss = {
                showHeaderImageDialog = false
                tempImageUri = null
            },
            onSave = { selectedValue ->
                onHeaderImageChange(selectedValue)
                showHeaderImageDialog = false
                tempImageUri = null
            },
            onGalleryClick = {
                imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            onPredefinedClick = { key ->
                tempImageUri = Uri.parse(key)
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showCornerRadiusDialog) {
            CounterDialog(
                title = stringResource(R.string.thumbnail_corner_radius),
                onDismiss = { showCornerRadiusDialog = false },
                initialValue = thumbnailCornerRadius,
                upperBound = 10,
                lowerBound = 0,
                resetValue = 6,
                unitDisplay = "0%",
                onConfirm = {
                    showCornerRadiusDialog = false
                    onThumbnailCornerRadius(it)
                },
                onCancel = {
                    showCornerRadiusDialog = false
                },
                onReset = { onThumbnailCornerRadius(6) },
            )
        }
    }

    var showSliderOptionDialog by rememberSaveable {
        mutableStateOf(false)
    }

    if (showSliderOptionDialog) {
        DefaultDialog(
            buttons = {
                TextButton(
                    onClick = { showSliderOptionDialog = false }
                ) {
                    Text(text = stringResource(android.R.string.cancel))
                }
            },
            onDismiss = {
                showSliderOptionDialog = false
            }
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .aspectRatio(1f)
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            1.dp,
                            if (sliderStyle == SliderStyle.DEFAULT) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            onSliderStyleChange(SliderStyle.DEFAULT)
                            showSliderOptionDialog = false
                        }
                        .padding(16.dp)
                ) {
                    var sliderValue by remember {
                        mutableFloatStateOf(0.5f)
                    }
                    Slider(
                        value = sliderValue,
                        valueRange = 0f..1f,
                        onValueChange = {
                            sliderValue = it
                        },
                        thumb = { Spacer(modifier = Modifier.size(0.dp)) },
                        track = { sliderState ->
                            PlayerSliderTrack(
                                sliderState = sliderState,
                                colors = SliderDefaults.colors()
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {}
                                )
                            }
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .aspectRatio(1f)
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            1.dp,
                            if (sliderStyle == SliderStyle.SQUIGGLY) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            onSliderStyleChange(SliderStyle.SQUIGGLY)
                            showSliderOptionDialog = false
                        }
                        .padding(16.dp)
                ) {
                    var sliderValue by remember {
                        mutableFloatStateOf(0.5f)
                    }
                    SquigglySlider(
                        value = sliderValue,
                        valueRange = 0f..1f,
                        onValueChange = {
                            sliderValue = it
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .aspectRatio(1f)
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            1.dp,
                            if (sliderStyle == SliderStyle.COMPOSE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            onSliderStyleChange(SliderStyle.COMPOSE)
                            showSliderOptionDialog = false
                        }
                        .padding(16.dp)
                ) {
                    var sliderValue by remember {
                        mutableFloatStateOf(0.5f)
                    }
                    Slider(
                        value = sliderValue,
                        valueRange = 0f..1f,
                        onValueChange = {
                            sliderValue = it
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
    val (selectedPreset, onSelectedPresetChange) = rememberPreference(UiPresetKey, "1.5")

    var showUiDialog by remember { mutableStateOf(false) }

    if (showUiDialog) {
        UiPresetDialog(
            onDismiss = { showUiDialog = false },
            onPresetSelected = { version ->
                onSelectedPresetChange(version)
                when (version) {
                    "1.0" -> {
                        onAppDesignVariantChange(AppDesignVariantType.OLD)
                        onPlayerStyle(PlayerStyle.OLD)
                        onMiniPlayerStyle(MiniPlayerStyle.OLD)
                    }
                    "1.5" -> {
                        onAppDesignVariantChange(AppDesignVariantType.NEW)
                        onPlayerStyle(PlayerStyle.NEW)
                        onMiniPlayerStyle(MiniPlayerStyle.NEW)
                    }
                    "2.0" -> {
                        onPlayerStyle(PlayerStyle.UI_2_0)
                        onAppDesignVariantChange(AppDesignVariantType.NEW)
                    }
                }
            },
            activePreset = selectedPreset
        )
    }

    LazyColumn(
        modifier = Modifier
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
    ) {
        item {
            Spacer(Modifier.windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Top)))
        }

        // --- THEME GROUP ---
        item {
            SettingCategory(title = stringResource(R.string.theme))
        }

        item {
            SettingsBox(shape = shapeManager(isFirst = true)) {
                SwitchPreference(
                    title = { Text(stringResource(R.string.enable_dynamic_theme)) },
                    icon = { Icon(painterResource(R.drawable.palette), null) },
                    checked = dynamicTheme,
                    onCheckedChange = onDynamicThemeChange
                )
            }
        }

        item {
            SettingsBox(shape = shapeManager()) {
                EnumListPreference(
                    title = { Text(stringResource(R.string.dark_theme)) },
                    icon = { Icon(painterResource(R.drawable.dark_mode), null) },
                    selectedValue = darkMode,
                    onValueSelected = onDarkModeChange,
                    valueText = {
                        when (it) {
                            DarkMode.ON -> stringResource(R.string.dark_theme_on)
                            DarkMode.OFF -> stringResource(R.string.dark_theme_off)
                            DarkMode.AUTO -> stringResource(R.string.dark_theme_follow_system)
                        }
                    }
                )
            }
        }

        item {
            AnimatedVisibility(useDarkTheme) {
                SettingsBox(shape = shapeManager()) {
                    SwitchPreference(
                        title = { Text(stringResource(R.string.pure_black)) },
                        icon = { Icon(painterResource(R.drawable.contrast), null) },
                        checked = pureBlack,
                        onCheckedChange = { checked ->
                            onPureBlackChange(checked)
                        }
                    )
                }
            }
        }

        item {
            SettingsBox(shape = shapeManager(isBoth = true)) {
                PreferenceEntry(
                    title = { Text(stringResource(R.string.header_background)) },
                    description = stringResource(R.string.header_background_description),
                    icon = { Icon(Icons.Rounded.Image, null) },
                    onClick = { showHeaderImageDialog = true }
                )
            }
        }

        item {
            SettingsBox(shape = shapeManager(isFirst = true, isLast = selectedPreset != "Custom")) {
                PreferenceEntry(
                    title = { Text(stringResource(R.string.app_ui)) },
                    description = "v$selectedPreset",
                    icon = { Icon(Icons.Rounded.DesignServices, null) },
                    onClick = { showUiDialog = true }
                )
            }
        }

        item {
            // This group will ONLY appear when the `selectedPreset` status is “Custom”.
            AnimatedVisibility(visible = selectedPreset == "Custom") {
                Column {
                    PreferenceGroupTitle(title = stringResource(R.string.custom_app_ui))

                    SettingsBox(shape = shapeManager(isFirst = true)) {
                        EnumListPreference(
                            title = { Text(stringResource(R.string.app_design_variant)) },
                            icon = { Icon(Icons.Rounded.DesignServices, null) },
                            selectedValue = appDesignVariant,
                            onValueSelected = onAppDesignVariantChange,
                            valueText = {
                                when (it) {
                                    AppDesignVariantType.NEW -> stringResource(R.string.app_design_ui_1_5)
                                    AppDesignVariantType.OLD -> stringResource(R.string.app_design_ui_1_0)
                                }
                            }
                        )
                    }
                    SettingsBox(shape = shapeManager()) {
                        ListPreference(
                            title = { Text(stringResource(R.string.player_style)) },
                            icon = { Icon(painterResource(R.drawable.play), null) },
                            selectedValue = playerStyle,
                            values = listOf(PlayerStyle.OLD, PlayerStyle.NEW, PlayerStyle.UI_2_0),
                            valueText = {
                                when (it) {
                                    PlayerStyle.OLD -> stringResource(R.string.player_style_old_ui)
                                    PlayerStyle.NEW -> stringResource(R.string.player_style_ui_1_0)
                                    PlayerStyle.UI_2_0 -> stringResource(R.string.player_style_ui_2_0)
                                }
                            },
                            onValueSelected = onPlayerStyle
                        )
                    }
                    SettingsBox(shape = shapeManager(isLast = true)) {
                        ListPreference(
                            title = { Text(stringResource(R.string.mini_player_style)) },
                            icon = { Icon(painterResource(R.drawable.play), null) },
                            selectedValue = miniPlayerStyle,
                            values = listOf(MiniPlayerStyle.OLD, MiniPlayerStyle.NEW),
                            valueText = {
                                when (it) {
                                    MiniPlayerStyle.OLD -> stringResource(R.string.player_style_old_ui)
                                    MiniPlayerStyle.NEW -> stringResource(R.string.player_style_ui_1_0)
                                }
                            },
                            onValueSelected = onMiniPlayerStyle
                        )
                    }
                }
            }
        }

        // --- HOME GROUP ---
        item {
            SettingCategory(title = stringResource(R.string.home))
        }

        item {
            SettingsBox(shape = shapeManager(isFirst = true, isLast = !isLoggedIn)) {
                SwitchPreference(
                    title = { Text(stringResource(R.string.show_content_filter)) },
                    icon = { Icon(Icons.Rounded.FilterList, null) },
                    checked = showContentFilter,
                    onCheckedChange = onShowContentFilterChange
                )
            }
            if (isLoggedIn) {
                SettingsBox(shape = shapeManager(isLast = true)) {
                    SwitchPreference(
                        title = { Text(stringResource(R.string.recent_activity)) },
                        icon = { Icon(Icons.Rounded.QueryStats, null) },
                        checked = showRecentActivity,
                        onCheckedChange = onShowRecentActivityChange
                    )
                }
            }
        }

        // --- PLAYER GROUP ---
        item {
            SettingCategory(title = stringResource(R.string.player))
        }

        item {
            AnimatedVisibility(visible = playerStyle != PlayerStyle.UI_2_0) {
                Column{
                    PreferenceGroupTitle(title = stringResource(R.string.player))
                    SettingsBox(shape = shapeManager(isFirst = true)) {
                        PreferenceEntry(
                            title = { Text(stringResource(R.string.slider_style)) },
                            description = when (sliderStyle) {
                                SliderStyle.DEFAULT -> stringResource(R.string.default_)
                                SliderStyle.SQUIGGLY -> stringResource(R.string.squiggly)
                                SliderStyle.COMPOSE -> stringResource(R.string.compose)
                            },
                            icon = { Icon(painterResource(R.drawable.sliders), null) },
                            onClick = { /* showSliderOptionDialog = true */ }
                        )
                    }
                }
            }
        }

        item {
            SettingsBox(shape = shapeManager()) {
                EnumListPreference(
                    title = { Text(stringResource(R.string.player_background_style)) },
                    icon = { Icon(painterResource(R.drawable.gradient), null) },
                    selectedValue = playerBackground,
                    onValueSelected = onPlayerBackgroundChange,
                    valueText = {
                        when (it) {
                            PlayerBackgroundStyle.DEFAULT -> stringResource(R.string.follow_theme)
                            PlayerBackgroundStyle.GRADIENT -> stringResource(R.string.gradient)
                            PlayerBackgroundStyle.BLURMOV -> stringResource(R.string.blurmv)
                            PlayerBackgroundStyle.BLUR -> stringResource(R.string.blur)
                        }
                    }
                )
            }
        }

        item {
            SettingsBox(shape = shapeManager()) {
                SwitchPreference(
                    title = { Text(stringResource(R.string.enable_swipe_thumbnail)) },
                    icon = { Icon(painterResource(R.drawable.swipe), null) },
                    checked = swipeThumbnail,
                    onCheckedChange = onSwipeThumbnailChange,
                )
            }
        }

        item {
            SettingsBox(shape = shapeManager(isLast = true)) {
                PreferenceEntry(
                    title = { Text(stringResource(R.string.thumbnail_corner_radius)) },
                    description = "$thumbnailCornerRadius" + "0%",
                    icon = { Icon(Icons.Rounded.Image, null) },
                    onClick = { showCornerRadiusDialog = true }
                )
            }
        }

        // --- MISC GROUP ---
        item {
            SettingCategory(title = stringResource(R.string.misc))
        }

        item {
            SettingsBox(shape = shapeManager(isFirst = true)) {
                SwitchPreference(
                    title = { Text(stringResource(R.string.auto_playlists_customization)) },
                    icon = { Icon(painterResource(R.drawable.playlist_play), null) },
                    checked = autoPlaylistsCustomization,
                    onCheckedChange = onAutoPlaylistsCustomizationChange
                )
            }
        }

        item {
            AnimatedVisibility(autoPlaylistsCustomization) {
                Column(modifier = Modifier.padding(start = 16.dp)) {
                    SettingsBox(shape = shapeManager()) {
                        SwitchPreference(
                            title = { Text(stringResource(R.string.show_liked_auto_playlist)) },
                            icon = { Icon(Icons.Rounded.Favorite, null) },
                            checked = autoPlaylistLiked,
                            onCheckedChange = onAutoPlaylistLikedChange
                        )
                    }
                    SettingsBox(shape = shapeManager()) {
                        SwitchPreference(
                            title = { Text(stringResource(R.string.show_download_auto_playlist)) },
                            icon = { Icon(Icons.Rounded.CloudDownload, null) },
                            checked = autoPlaylistDownload,
                            onCheckedChange = onAutoPlaylistDownloadChange
                        )
                    }
                    SettingsBox(shape = shapeManager()) {
                        SwitchPreference(
                            title = { Text(stringResource(R.string.show_top_auto_playlist)) },
                            icon = { Icon(Icons.AutoMirrored.Rounded.TrendingUp, null) },
                            checked = autoPlaylistTopPlaylist,
                            onCheckedChange = onAutoPlaylistTopPlaylistChange
                        )
                    }
                    SettingsBox(shape = shapeManager()) {
                        SwitchPreference(
                            title = { Text(stringResource(R.string.show_cached_auto_playlist)) },
                            icon = { Icon(Icons.Rounded.Cached, null) },
                            checked = autoPlaylistCached,
                            onCheckedChange = onAutoPlaylistCachedChange
                        )
                    }
                    SettingsBox(shape = shapeManager()) {
                        SwitchPreference(
                            title = { Text(stringResource(R.string.show_local_auto_playlist)) },
                            icon = { Icon(Icons.Rounded.MusicNote, null) },
                            checked = autoPlaylistLocal,
                            onCheckedChange = onAutoPlaylistLocalChange
                        )
                    }
                }
            }
        }

        item {
            SettingsBox(shape = shapeManager()) {
                SwitchPreference(
                    title = { Text(stringResource(R.string.swipe_song_to_dismiss)) },
                    icon = { Icon(painterResource(R.drawable.queue_music), null) },
                    checked = swipeSongToDismiss,
                    onCheckedChange = onSwipeSongToDismissChange
                )
            }
        }

        item {
            if (appDesignVariant == AppDesignVariantType.NEW) {
                SettingsBox(shape = shapeManager()) {
                    EnumListPreference(
                        title = { Text(stringResource(R.string.default_open_tab)) },
                        icon = { Icon(painterResource(R.drawable.tab), null) },
                        selectedValue = defaultOpenTab,
                        onValueSelected = onDefaultOpenTabChange,
                        valueText = { stringResource(it.stringId) }
                    )
                }
            } else {
                SettingsBox(shape = shapeManager()) {
                    EnumListPreference(
                        title = { Text(stringResource(R.string.default_open_tab)) },
                        icon = { Icon(painterResource(R.drawable.tab), null) },
                        selectedValue = defaultOpenTabOld,
                        onValueSelected = onDefaultOpenTabOldChange,
                        valueText = { stringResource(it.stringId) }
                    )
                }
            }
        }

        item {
            SettingsBox(shape = shapeManager()) {
                SwitchPreference(
                    title = { Text(stringResource(R.string.slim_navbar)) },
                    icon = { Icon(painterResource(R.drawable.nav_bar), null) },
                    checked = slimNav,
                    onCheckedChange = onSlimNavChange
                )
            }
        }

        item {
            SettingsBox(shape = shapeManager()) {
                EnumListPreference(
                    title = { Text(stringResource(R.string.grid_cell_size)) },
                    icon = { Icon(painterResource(R.drawable.grid_view), null) },
                    selectedValue = gridCellSize,
                    onValueSelected = onGridCellSizeChange,
                    valueText = {
                        when (it) {
                            GridCellSize.SMALL -> stringResource(R.string.small)
                            GridCellSize.BIG -> stringResource(R.string.big)
                        }
                    },
                )
            }
        }

        item {
            SettingsBox(shape = shapeManager(isLast = true)) {
                ListPreference(
                    title = { Text(stringResource(R.string.default_lib_chips)) },
                    icon = { Icon(painterResource(R.drawable.list), null) },
                    selectedValue = defaultChip,
                    values = listOf(
                        LibraryFilter.LIBRARY,
                        LibraryFilter.PLAYLISTS,
                        LibraryFilter.SONGS,
                        LibraryFilter.ALBUMS,
                        LibraryFilter.ARTISTS,
                    ),
                    valueText = {
                        when (it) {
                            LibraryFilter.SONGS -> stringResource(R.string.songs)
                            LibraryFilter.ARTISTS -> stringResource(R.string.artists)
                            LibraryFilter.ALBUMS -> stringResource(R.string.albums)
                            LibraryFilter.PLAYLISTS -> stringResource(R.string.playlists)
                            LibraryFilter.LIBRARY -> stringResource(R.string.filter_library)
                        }
                    },
                    onValueSelected = onDefaultChipChange,
                )
            }
        }
    }


    TopAppBar(
        title = { Text(stringResource(R.string.appearance)) },
        navigationIcon = {
            IconButton(
                onClick = navController::navigateUp,
                onLongClick = navController::backToMain
            ) {
                Icon(
                    painterResource(R.drawable.arrow_back),
                    contentDescription = null
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}

@Composable
private fun HeaderImagePickerDialog(
    initialValue: String,
    tempSelectedUri: Uri?,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onGalleryClick: () -> Unit,
    onPredefinedClick: (String) -> Unit
) {
    var currentSelection by remember {
        mutableStateOf(tempSelectedUri?.toString() ?: initialValue)
    }

    if (tempSelectedUri != null) {
        currentSelection = tempSelectedUri.toString()
    }

    val predefinedImages = mapOf(
        "Loudly-1" to R.drawable.loudly_picutre_1,
        "Loudly-2" to R.drawable.loudly_picutre_2,
        "Loudly-3" to R.drawable.loudly_picutre_3
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(id = R.string.select_header_image),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val glassmorphismModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Modifier.blur(radius = 24.dp, edgeTreatment = BlurredEdgeTreatment(RoundedCornerShape(24.dp)))
                    } else {
                        Modifier
                    }
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                            .then(glassmorphismModifier)
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                            .aspectRatio(16 / 9f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            val painter = when {
                                currentSelection.startsWith("content://") -> {
                                    rememberAsyncImagePainter(model = Uri.parse(currentSelection))
                                }
                                else -> painterResource(id = predefinedImages[currentSelection] ?: R.drawable.loudly_picutre_1)
                            }
                            Image(
                                painter = painter,
                                contentDescription = "Preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    predefinedImages.forEach { (key, drawableRes) ->
                        val isSelected = currentSelection == key
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            tonalElevation = 4.dp,
                            modifier = Modifier
                                .size(64.dp)
                                .clickable {
                                    onPredefinedClick(key)
                                    currentSelection = key
                                }
                                .border(
                                    width = 2.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(20.dp)
                                )
                        ) {
                            Image(
                                painter = painterResource(id = drawableRes),
                                contentDescription = key,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onGalleryClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Text(
                        text = stringResource(id = R.string.select_from_gallery),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(currentSelection) }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun UiPresetDialog(
    onDismiss: () -> Unit,
    onPresetSelected: (String) -> Unit,
    activePreset: String
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.ui_version_selection)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                mapOf(
                    "1.0" to stringResource(R.string.ui_version_1_0_desc),
                    "1.5" to stringResource(R.string.ui_version_1_5_desc),
                    "2.0" to stringResource(R.string.ui_version_2_0_desc),
                    "Custom" to stringResource(R.string.ui_version_custom_desc)
                ).forEach { (version, description) ->
                    val isSelected = activePreset == version
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onPresetSelected(version)
                                onDismiss()
                            }
                            .border(
                                width = 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("App UI v$version", fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text(description, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

enum class DarkMode {
    ON, OFF, AUTO
}

enum class NavigationTabOld(val stringId: Int) {
    HOME(R.string.home),
    EXPLORE(R.string.explore),
    SONGS(R.string.songs),
    ARTISTS(R.string.artists),
    ALBUMS(R.string.albums),
    PLAYLISTS(R.string.playlists)
}

enum class NavigationTab(val stringId: Int) {
    HOME(R.string.home),
    LIBRARY(R.string.library),
    EXPLORE(R.string.explore),
    SONGS(R.string.songs),
    ARTISTS(R.string.artists),
    ALBUMS(R.string.albums),
    PLAYLISTS(R.string.playlists)
}

enum class LyricsPosition {
    LEFT, CENTER, RIGHT
}
