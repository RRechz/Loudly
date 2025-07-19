// Dosya Yolu: com/babelsoftware/loudly/ui/screens/settings/ContentSettings.kt
@file:Suppress("DEPRECATION")

package com.babelsoftware.loudly.ui.screens.settings

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.babelsoftware.loudly.LocalPlayerAwareWindowInsets
import com.babelsoftware.loudly.R
import com.babelsoftware.loudly.constants.ContentCountryKey
import com.babelsoftware.loudly.constants.ContentLanguageKey
import com.babelsoftware.loudly.constants.CountryCodeToName
import com.babelsoftware.loudly.constants.HideExplicitKey
import com.babelsoftware.loudly.constants.LanguageCodeToName
import com.babelsoftware.loudly.constants.LikedAutoDownloadKey
import com.babelsoftware.loudly.constants.LikedAutodownloadMode
import com.babelsoftware.loudly.constants.ProxyEnabledKey
import com.babelsoftware.loudly.constants.ProxyTypeKey
import com.babelsoftware.loudly.constants.ProxyUrlKey
import com.babelsoftware.loudly.constants.SYSTEM_DEFAULT
import com.babelsoftware.loudly.constants.SelectedLanguageKey
import com.babelsoftware.loudly.ui.component.EditTextPreference
import com.babelsoftware.loudly.ui.component.IconButton
import com.babelsoftware.loudly.ui.component.ListPreference
import com.babelsoftware.loudly.ui.component.PreferenceEntry
import com.babelsoftware.loudly.ui.component.SwitchPreference
import com.babelsoftware.loudly.ui.screens.settings.card_design.SettingCategory
import com.babelsoftware.loudly.ui.screens.settings.card_design.SettingsBox
import com.babelsoftware.loudly.ui.screens.settings.card_design.shapeManager
import com.babelsoftware.loudly.ui.utils.backToMain
import com.babelsoftware.loudly.utils.rememberEnumPreference
import com.babelsoftware.loudly.utils.rememberPreference
import java.net.Proxy
import java.util.Locale

@SuppressLint("PrivateResource")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val context = LocalContext.current
    val (likedAutoDownload, onLikedAutoDownload) = rememberEnumPreference(LikedAutoDownloadKey, LikedAutodownloadMode.OFF)
    val (contentLanguage, onContentLanguageChange) = rememberPreference(key = ContentLanguageKey, defaultValue = "system")
    val (contentCountry, onContentCountryChange) = rememberPreference(key = ContentCountryKey, defaultValue = "system")
    val (selectedLanguage, onSelectedLanguage) = rememberPreference(key = SelectedLanguageKey, defaultValue = "system")
    val (hideExplicit, onHideExplicitChange) = rememberPreference(key = HideExplicitKey, defaultValue = false)
    val (proxyEnabled, onProxyEnabledChange) = rememberPreference(key = ProxyEnabledKey, defaultValue = false)
    val (proxyType, onProxyTypeChange) = rememberEnumPreference(key = ProxyTypeKey, defaultValue = Proxy.Type.HTTP)
    val (proxyUrl, onProxyUrlChange) = rememberPreference(key = ProxyUrlKey, defaultValue = "host:port")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.content)) },
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
            item { SettingCategory(title = stringResource(R.string.content)) }
            item {
                SettingsBox(shape = shapeManager(isFirst = true)) {
                    ListPreference(
                        title = { Text(stringResource(R.string.content_language)) },
                        icon = { Icon(painterResource(R.drawable.language), null) },
                        selectedValue = contentLanguage,
                        values = listOf(SYSTEM_DEFAULT) + LanguageCodeToName.keys.toList(),
                        valueText = { LanguageCodeToName.getOrElse(it) { stringResource(R.string.system_default) } },
                        onValueSelected = onContentLanguageChange
                    )
                }
            }
            item {
                SettingsBox(shape = shapeManager()) {
                    ListPreference(
                        title = { Text(stringResource(R.string.content_country)) },
                        icon = { Icon(painterResource(R.drawable.location_on), null) },
                        selectedValue = contentCountry,
                        values = listOf(SYSTEM_DEFAULT) + CountryCodeToName.keys.toList(),
                        valueText = { CountryCodeToName.getOrElse(it) { stringResource(R.string.system_default) } },
                        onValueSelected = onContentCountryChange
                    )
                }
            }
            item {
                SettingsBox(shape = shapeManager()) {
                    SwitchPreference(
                        title = { Text(stringResource(R.string.hide_explicit)) },
                        icon = { Icon(painterResource(R.drawable.explicit), null) },
                        checked = hideExplicit,
                        onCheckedChange = onHideExplicitChange
                    )
                }
            }
            item {
                SettingsBox(shape = shapeManager(isLast = true)) {
                    ListPreference(
                        title = { Text(stringResource(R.string.like_autodownload)) },
                        icon = { Icon(Icons.Rounded.Favorite, null) },
                        values = listOf(LikedAutodownloadMode.OFF, LikedAutodownloadMode.ON, LikedAutodownloadMode.WIFI_ONLY),
                        selectedValue = likedAutoDownload,
                        valueText = {
                            when (it) {
                                LikedAutodownloadMode.OFF -> stringResource(R.string.state_off)
                                LikedAutodownloadMode.ON -> stringResource(R.string.state_on)
                                LikedAutodownloadMode.WIFI_ONLY -> stringResource(R.string.wifi_only)
                            }
                        },
                        onValueSelected = onLikedAutoDownload
                    )
                }
            }

            item { SettingCategory(title = stringResource(R.string.system_integration)) }
            item {
                val shape = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) shapeManager(isFirst = true) else shapeManager(isBoth = true)
                SettingsBox(shape = shape) {
                    PreferenceEntry(
                        title = { Text(stringResource(R.string.notifications_settings)) },
                        icon = { Icon(painterResource(R.drawable.notification_on), null) },
                        onClick = { navController.navigate("settings/content/notification") }
                    )
                }
            }
            item {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    SettingsBox(shape = shapeManager(isLast = true)) {
                        PreferenceEntry(
                            title = { Text(stringResource(R.string.open_supported_links)) },
                            description = stringResource(R.string.configure_supported_links),
                            icon = { Icon(painterResource(R.drawable.add_link), null) },
                            onClick = {
                                try {
                                    context.startActivity(
                                        Intent(Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS, "package:${context.packageName}".toUri()),
                                    )
                                } catch (e: ActivityNotFoundException) {
                                    Toast.makeText(context, R.string.intent_supported_links_not_found, Toast.LENGTH_LONG).show()
                                }
                            },
                        )
                    }
                }
            }

            item { SettingCategory(title = stringResource(R.string.app_language)) }
            item {
                SettingsBox(shape = shapeManager(isBoth = true)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        PreferenceEntry(
                            title = { Text(stringResource(R.string.app_language)) },
                            icon = { Icon(painterResource(R.drawable.translate), null) },
                            onClick = {
                                context.startActivity(Intent(Settings.ACTION_APP_LOCALE_SETTINGS, "package:${context.packageName}".toUri()))
                            }
                        )
                    } else {
                        ListPreference(
                            title = { Text(stringResource(R.string.app_language)) },
                            icon = { Icon(painterResource(R.drawable.translate), null) },
                            selectedValue = selectedLanguage,
                            values = listOf(SYSTEM_DEFAULT) + LanguageCodeToName.keys.toList(),
                            valueText = { LanguageCodeToName[it] ?: stringResource(R.string.system_default) },
                            onValueSelected = {
                                onSelectedLanguage(it)
                                updateLanguage(context, it)
                                saveLanguagePreference(context, it)
                            }
                        )
                    }
                }
            }

            item { SettingCategory(title = stringResource(R.string.proxy)) }
            item {
                SettingsBox(shape = shapeManager(isFirst = true, isLast = !proxyEnabled)) {
                    SwitchPreference(
                        title = { Text(stringResource(R.string.enable_proxy)) },
                        icon = { Icon(painterResource(R.drawable.wifi_proxy), null) },
                        checked = proxyEnabled,
                        onCheckedChange = onProxyEnabledChange
                    )
                }
            }
            item {
                AnimatedVisibility(proxyEnabled) {
                    Column {
                        SettingsBox(shape = shapeManager()) {
                            ListPreference(
                                title = { Text(stringResource(R.string.proxy_type)) },
                                selectedValue = proxyType,
                                values = listOf(Proxy.Type.HTTP, Proxy.Type.SOCKS),
                                valueText = { it.name },
                                onValueSelected = onProxyTypeChange
                            )
                        }
                        SettingsBox(shape = shapeManager(isLast = true)) {
                            EditTextPreference(
                                title = { Text(stringResource(R.string.proxy_url)) },
                                value = proxyUrl,
                                onValueChange = onProxyUrlChange
                            )
                        }
                    }
                }
            }
        }
    }
}

// Orijinal dosyanızdaki yardımcı fonksiyonlar, dokunulmadan korundu.
fun saveLanguagePreference(context: Context, languageCode: String) {
    val sharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    sharedPreferences.edit { putString("app_language", languageCode) }
}

fun updateLanguage(context: Context, languageCode: String) {
    val locale = Locale(languageCode)
    val config = Configuration(context.resources.configuration)
    config.setLocales(LocaleList(locale))
    context.resources.updateConfiguration(config, context.resources.displayMetrics)
}
