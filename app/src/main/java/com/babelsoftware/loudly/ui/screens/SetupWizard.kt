package com.babelsoftware.loudly.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.NavigateBefore
import androidx.compose.material.icons.automirrored.rounded.NavigateNext
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.MusicVideo
import androidx.compose.material.icons.rounded.NotInterested
import androidx.compose.material.icons.rounded.OfflinePin
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.babelsoftware.loudly.R
import com.babelsoftware.loudly.constants.AppDesignVariantKey
import com.babelsoftware.loudly.constants.AppDesignVariantType
import com.babelsoftware.loudly.constants.FirstSetupPassed
import com.babelsoftware.loudly.constants.PlayerStyle
import com.babelsoftware.loudly.constants.PlayerStyleKey
import com.babelsoftware.loudly.utils.rememberEnumPreference
import com.babelsoftware.loudly.utils.rememberPreference

@Composable
fun SetupWizard(
    navController: NavController,
) {
    val (firstSetupPassed, onFirstSetupPassedChange) = rememberPreference(
        FirstSetupPassed,
        defaultValue = false
    )

    var position by remember { mutableIntStateOf(0) }
    val maxPosition = 6 // 0: Welcome, 1: Style, 2: Story, 3: Header, 4: Playlist, 5: Settings, 6: Finish

    if (position > 0) {
        BackHandler {
            position--
        }
    }

    if (firstSetupPassed) {
        navController.navigateUp()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Image(
            painter = painterResource(id = R.drawable.loudly_monochrome),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(40.dp)
                .alpha(0.5f)
        )

        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                WizardBottomBar(
                    position = position,
                    maxPosition = maxPosition,
                    onBack = { if (position > 0) position-- },
                    onNext = { if (position < maxPosition) position++ },
                    onFinish = {
                        onFirstSetupPassedChange(true)
                        navController.navigateUp()
                    }
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                AnimatedContent(
                    targetState = position,
                    label = "WizardStep",
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally(animationSpec = tween(300)) { it } + fadeIn()) togetherWith (slideOutHorizontally(animationSpec = tween(300)) { -it } + fadeOut())
                        } else {
                            (slideInHorizontally(animationSpec = tween(300)) { -it } + fadeIn()) togetherWith (slideOutHorizontally(animationSpec = tween(300)) { it } + fadeOut())
                        }
                    }
                ) { targetPosition ->
                    when (targetPosition) {
                        0 -> WelcomeStep()
                        1 -> AppUiStyleStep()
                        2 -> StoryShareFeatureStep()
                        3 -> SettingsHeaderFeatureStep()
                        4 -> PlayerPlaylistFeatureStep()
                        5 -> NewSettingsUiFeatureStep()
                        6 -> FinishStep()
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomeStep() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(NavigationBarDefaults.Elevation))
        ) {
            Image(
                painter = painterResource(R.drawable.loudly_monochrome),
                contentDescription = "Logo",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground, BlendMode.SrcIn),
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = stringResource(R.string.welcome_to_loudly),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.welcome_to_loudly_description),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FeatureChip(icon = Icons.Rounded.MusicVideo, text = stringResource(R.string.yt_music_fingertips))
            FeatureChip(icon = Icons.Rounded.NotInterested, text = stringResource(R.string.ad_free_playback))
            FeatureChip(icon = Icons.Rounded.Sync, text = stringResource(R.string.ytm_account_sync))
            FeatureChip(icon = Icons.Rounded.OfflinePin, text = stringResource(R.string.offline_music_experience))
            FeatureChip(icon = Icons.Rounded.DirectionsCar, text = stringResource(R.string.android_auto))
            FeatureChip(painterIcon = R.drawable.discord, text = stringResource(R.string.discord_support))
            FeatureChip(painterIcon = R.drawable.github, text = stringResource(R.string.fos_info))
        }
    }
}

@Composable
fun AppUiStyleStep() {
    val (appDesignVariant, onAppDesignVariantChange) = rememberEnumPreference(AppDesignVariantKey, AppDesignVariantType.NEW)
    val (playerStyle, onPlayerStyleChange) = rememberEnumPreference(PlayerStyleKey, PlayerStyle.UI_2_0)

    val selectedPackage = remember(appDesignVariant, playerStyle) {
        when {
            playerStyle == PlayerStyle.UI_2_0 -> PlayerStyle.UI_2_0
            appDesignVariant == AppDesignVariantType.NEW && playerStyle == PlayerStyle.NEW -> PlayerStyle.NEW
            else -> PlayerStyle.OLD
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.select_player_ui),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.select_player_ui_description),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PlayerStylePreview(
                title = "App UI v2.0",
                isSelected = selectedPackage == PlayerStyle.UI_2_0,
                onClick = {
                    onPlayerStyleChange(PlayerStyle.UI_2_0)
                    onAppDesignVariantChange(AppDesignVariantType.NEW)
                },
                modifier = Modifier.weight(1f)
            ) {
                PlayerUIV2Preview()
            }
            PlayerStylePreview(
                title = "App UI v1.5",
                isSelected = selectedPackage == PlayerStyle.NEW,
                onClick = {
                    onPlayerStyleChange(PlayerStyle.NEW)
                    onAppDesignVariantChange(AppDesignVariantType.NEW)
                },
                modifier = Modifier.weight(1f)
            ) {
                PlayerUIV15Preview()
            }
            PlayerStylePreview(
                title = "App UI v1.0",
                isSelected = selectedPackage == PlayerStyle.OLD,
                onClick = {
                    onPlayerStyleChange(PlayerStyle.OLD)
                    onAppDesignVariantChange(AppDesignVariantType.OLD)
                },
                modifier = Modifier.weight(1f)
            ) {
                PlayerUIV10Preview()
            }
        }
    }
}

@Composable
private fun PlayerUIV2Preview() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(9f / 16f)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .aspectRatio(1f)
                .align(Alignment.TopCenter)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
        )
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth(0.9f).height(4.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)))
                Row(
                    modifier = Modifier.fillMaxWidth(0.8f),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)))
                    Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface))
                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)))
                }
            }
        }
    }
}

@Composable
private fun PlayerUIV15Preview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(9f / 16f)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(2.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .weight(1f, fill = false)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth(0.9f).height(4.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)))
            Row(
                modifier = Modifier.fillMaxWidth(0.8f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)))
                Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface))
                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)))
            }
        }
    }
}

@Composable
private fun PlayerUIV10Preview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(9f / 16f)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp))
            .border(2.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .weight(1f, fill = false)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth(0.9f).height(4.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)))
                Box(modifier = Modifier.size(20.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.onSurface))
                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)))
            }
        }
    }
}


@Composable
private fun FeatureGuideStep(
    title: String,
    description: String,
    content: @Composable BoxScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .aspectRatio(9f / 16f)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(2.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Composable
fun StoryShareFeatureStep() {
    FeatureGuideStep(
        title = stringResource(R.string.create_your_story),
        description = stringResource(R.string.share_your_story_description)
    ) {
        StorySharePreview()
    }
}

@Composable
fun SettingsHeaderFeatureStep() {
    FeatureGuideStep(
        title = stringResource(R.string.customizable_settings),
        description = stringResource(R.string.customizable_settings_description)
    ) {
        SettingsHeaderPreview()
    }
}

@Composable
fun PlayerPlaylistFeatureStep() {
    FeatureGuideStep(
        title = stringResource(R.string.new_song_list),
        description = stringResource(R.string.new_song_list_description)
    ) {
        PlayerPlaylistPreview()
    }
}

@Composable
fun NewSettingsUiFeatureStep() {
    FeatureGuideStep(
        title = stringResource(R.string.remastered_settings),
        description = stringResource(R.string.remastered_settings_description)
    ) {
        NewSettingsUiPreview()
    }
}

@Composable
private fun StorySharePreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(0.5f))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth(0.7f).height(8.dp).clip(RoundedCornerShape(4.dp)).background(Color.White.copy(alpha = 0.8f)))
            Spacer(modifier = Modifier.height(4.dp))
            Box(modifier = Modifier.fillMaxWidth(0.5f).height(6.dp).clip(RoundedCornerShape(4.dp)).background(Color.White.copy(alpha = 0.5f)))
            Spacer(modifier = Modifier.weight(0.2f))
            Column(
                modifier = Modifier.fillMaxWidth(0.8f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.White.copy(alpha = 0.3f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.3f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color.White)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.7f)))
                    Box(modifier = Modifier.size(30.dp).clip(CircleShape).background(Color.White))
                    Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.7f)))
                }
            }
            Spacer(modifier = Modifier.weight(0.3f))
        }
    }
}

@Composable
private fun SettingsHeaderPreview() {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.35f)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(15.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiary))
                Spacer(modifier = Modifier.height(2.dp))
                Box(modifier = Modifier.fillMaxWidth(0.5f).height(20.dp).clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)).background(MaterialTheme.colorScheme.primary))
            }
        }
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            repeat(2) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(18.dp).clip(RoundedCornerShape(6.dp)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.width(80.dp).height(8.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)))
                        Box(modifier = Modifier.width(50.dp).height(6.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)))
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerPlaylistPreview() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    MaterialTheme.colorScheme.surfaceVariant
                )
            )
        ))
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.65f)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.primary))
            Box(modifier = Modifier.fillMaxWidth(0.8f).height(10.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)))
            Box(modifier = Modifier.fillMaxWidth(0.9f).height(10.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)))
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 20.dp)
                .fillMaxWidth(0.5f)
                .aspectRatio(1f)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        )
    }
}

@Composable
private fun NewSettingsUiPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(modifier = Modifier.width(50.dp).height(8.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)))
        Box(modifier = Modifier.fillMaxWidth().height(30.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)))
        Spacer(modifier = Modifier.height(4.dp))
        Box(modifier = Modifier.width(60.dp).height(8.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)).background(MaterialTheme.colorScheme.surfaceVariant))
            Box(modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)).background(MaterialTheme.colorScheme.surfaceVariant))
        }
    }
}

@Composable
fun FinishStep() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.setup_complete),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.setup_complete_description),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun PlayerStylePreview(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .border(BorderStroke(2.dp, borderColor), RoundedCornerShape(16.dp))
                .clickable(onClick = onClick)
                .padding(8.dp)
        ) {
            content()
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = if (isSelected) MaterialTheme.colorScheme.primary else LocalContentColor.current
        )
    }
}

@Composable
fun WizardBottomBar(
    position: Int,
    maxPosition: Int,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onFinish: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .height(80.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onBack,
                enabled = position > 0
            ) {
                Icon(Icons.AutoMirrored.Rounded.NavigateBefore, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.back))
            }

            LinearProgressIndicator(
                progress = { (position.toFloat() / maxPosition) },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            )

            Button(
                onClick = {
                    if (position < maxPosition) {
                        onNext()
                    } else {
                        onFinish()
                    }
                }
            ) {
                Text(if (position < maxPosition) stringResource(R.string.next) else stringResource(R.string.finish))
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    if (position < maxPosition) Icons.AutoMirrored.Rounded.NavigateNext else Icons.Rounded.CheckCircle,
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
fun FeatureChip(
    icon: ImageVector? = null,
    painterIcon: Int? = null,
    text: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )
            }
            if (painterIcon != null) {
                Icon(
                    painter = painterResource(painterIcon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
