// Dosya Yolu: com/babelsoftware/loudly/ui/screens/settings/AccountScreen.kt
package com.babelsoftware.loudly.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.babelsoftware.innertube.YouTube
import com.babelsoftware.innertube.utils.parseCookieString
import com.babelsoftware.loudly.LocalPlayerAwareWindowInsets
import com.babelsoftware.loudly.R
import com.babelsoftware.loudly.constants.AccountChannelHandleKey
import com.babelsoftware.loudly.constants.AccountEmailKey
import com.babelsoftware.loudly.constants.AccountNameKey
import com.babelsoftware.loudly.constants.InnerTubeCookieKey
import com.babelsoftware.loudly.constants.UseLoginForBrowse
import com.babelsoftware.loudly.constants.YtmSyncKey
import com.babelsoftware.loudly.ui.component.IconButton
import com.babelsoftware.loudly.ui.component.InfoLabel
import com.babelsoftware.loudly.ui.component.PreferenceEntry
import com.babelsoftware.loudly.ui.component.SwitchPreference
import com.babelsoftware.loudly.ui.component.TextFieldDialog
import com.babelsoftware.loudly.ui.screens.settings.card_design.ActionType
import com.babelsoftware.loudly.ui.screens.settings.card_design.IconResource
import com.babelsoftware.loudly.ui.screens.settings.card_design.SettingCategory
import com.babelsoftware.loudly.ui.screens.settings.card_design.SettingsBox
import com.babelsoftware.loudly.ui.screens.settings.card_design.shapeManager
import com.babelsoftware.loudly.ui.utils.backToMain
import com.babelsoftware.loudly.utils.rememberPreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val accountName by rememberPreference(AccountNameKey, "")
    val accountEmail by rememberPreference(AccountEmailKey, "")
    val accountChannelHandle by rememberPreference(AccountChannelHandleKey, "")
    val (innerTubeCookie, onInnerTubeCookieChange) = rememberPreference(InnerTubeCookieKey, "")
    val isLoggedIn = remember(innerTubeCookie) { "SAPISID" in parseCookieString(innerTubeCookie) }
    val (ytmSync, onYtmSyncChange) = rememberPreference(YtmSyncKey, defaultValue = true)
    val (useLoginForBrowse, onUseLoginForBrowseChange) = rememberPreference(key = UseLoginForBrowse, defaultValue = false)

    var showToken by remember { mutableStateOf(false) }
    var showTokenEditor by remember { mutableStateOf(false) }

    if (showTokenEditor) {
        TextFieldDialog(
            modifier = Modifier,
            initialTextFieldValue = TextFieldValue(innerTubeCookie),
            onDone = { onInnerTubeCookieChange(it) },
            onDismiss = { showTokenEditor = false },
            singleLine = false,
            maxLines = 20,
            isInputValid = {
                it.isNotEmpty() &&
                        try {
                            "SAPISID" in parseCookieString(it)
                            true
                        } catch (e: Exception) {
                            false
                        }
            },
            extraContent = {
                InfoLabel(text = stringResource(R.string.token_adv_login_description))
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.account)) },
                navigationIcon = {
                    IconButton(
                        onClick = navController::navigateUp,
                        onLongClick = navController::backToMain
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
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
                .padding(horizontal = 8.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                SettingCategory(title = stringResource(R.string.account))
            }
            item {
                SettingsBox(shape = shapeManager(isFirst = true)) {
                    PreferenceEntry(
                        title = { Text(if (isLoggedIn) accountName else stringResource(R.string.login)) },
                        description = if (isLoggedIn) {
                            accountEmail.takeIf { it.isNotEmpty() }
                                ?: accountChannelHandle.takeIf { it.isNotEmpty() }
                        } else {
                            null
                        },
                        icon = { Icon(painterResource(R.drawable.person), null) },
                        trailingContent = {
                            if (isLoggedIn) {
                                OutlinedButton(
                                    onClick = { onInnerTubeCookieChange("") },
                                ) {
                                    Text(stringResource(R.string.logout))
                                }
                            }
                        },
                        onClick = { if (!isLoggedIn) navController.navigate("login") }
                    )
                }
            }

            item {
                SettingsBox(shape = shapeManager()) {
                    PreferenceEntry(
                        title = {
                            Column {
                                if (showToken) {
                                    Text(stringResource(R.string.token_shown))
                                    Text(
                                        text = if (isLoggedIn) innerTubeCookie else stringResource(R.string.not_logged_in),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Light,
                                        overflow = TextOverflow.Ellipsis,
                                        maxLines = 1
                                    )
                                } else {
                                    Text(stringResource(R.string.token_hidden))
                                }
                            }
                        },
                        icon = { Icon(painterResource(R.drawable.token), null) },
                        onClick = {
                            if (!showToken) {
                                showToken = true
                            } else {
                                showTokenEditor = true
                            }
                        },
                    )
                }
            }

            item {
                if (isLoggedIn) {
                    SettingsBox(shape = shapeManager(isLast = true)) {
                        SwitchPreference(
                            title = { Text(stringResource(R.string.ytm_sync)) },
                            icon = { Icon(painterResource(R.drawable.cached), null) },
                            checked = ytmSync,
                            onCheckedChange = onYtmSyncChange,
                            isEnabled = true
                        )
                    }
                }
            }

            item {
                SettingCategory(title = stringResource(R.string.integrations))
            }

            item {
                SettingsBox(
                    title = stringResource(R.string.import_from_spotify),
                    icon = IconResource.Drawable(painterResource(R.drawable.spotify)),
                    shape = shapeManager(isFirst = true),
                    onClick = { navController.navigate("settings/import_from_spotify/ImportFromSpotify") }
                )
            }

            item {
                SettingsBox(
                    title = stringResource(R.string.discord_integration),
                    icon = IconResource.Drawable(painterResource(R.drawable.discord)),
                    shape = shapeManager(isLast = true),
                    onClick = { navController.navigate("settings/discord") }
                )
            }

            item {
                SettingCategory(title = stringResource(R.string.misc))
            }

            item {
                SettingsBox(
                    title = stringResource(R.string.use_login_for_browse),
                    description = stringResource(R.string.use_login_for_browse_desc),
                    icon = IconResource.Drawable(painterResource(R.drawable.person)),
                    actionType = ActionType.SWITCH,
                    isChecked = useLoginForBrowse,
                    onCheckedChange = {
                        YouTube.useLoginForBrowse = it
                        onUseLoginForBrowseChange(it)
                    },
                    shape = shapeManager(isBoth = true),
                    onClick = {}
                )
            }

            item {
                Spacer(modifier = Modifier.padding(bottom = 16.dp))
            }
        }
    }
}
