package com.babelsoftware.loudly.ui.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import com.babelsoftware.loudly.LocalPlayerConnection
import com.babelsoftware.loudly.extensions.backToMain
import com.babelsoftware.loudly.ui.screens.Screens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun UnifiedBottomBar(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val currentMediaItem by playerConnection.mediaMetadata.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    // Animasyon için Dikey Ofset
    var offsetY by remember { mutableStateOf(0f) }
    // Kartın yüksekliği, MiniPlayer'ın görünürlüğüne göre animasyonlu olarak değişir
    val cardHeight by animateDpAsState(
        targetValue = if (currentMediaItem != null) 136.dp else 80.dp,
        animationSpec = spring(stiffness = 500f),
        label = "CardHeightAnimation"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp, start = 8.dp, end = 8.dp)
            .height(cardHeight)
            .offset(y = with(density) { offsetY.toDp() })
            .pointerInput(currentMediaItem) {
                // Sadece müzik çalarken aşağı kaydırarak kapatma aktif
                if (currentMediaItem != null) {
                    detectVerticalDragGestures(
                        onDragEnd = {
                            // Belirli bir mesafeden fazla sürüklendiyse kapat
                            if (offsetY > 150f) {
                                playerConnection.player.stop()
                            }
                            // Trambolin efekti için ofseti animasyonla sıfırla
                            coroutineScope.launch {
                                offsetY = 0f // Bu satırı animasyonlu hale getirebiliriz, şimdilik basit tutalım
                            }
                        },
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            // Sadece aşağı doğru sürüklemeye izin ver
                            if (dragAmount > 0) {
                                offsetY += dragAmount
                            }
                        }
                    )
                }
            }
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            // PlayerUI20'deki ile aynı buzlu cam efekti
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
            ),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Bottom
            ) {
                // MiniPlayer sadece müzik çalarken animasyonla görünür olur
                AnimatedVisibility(
                    visible = currentMediaItem != null,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column {
                        MiniPlayer()
                        PlayerProgressBar() // İlerleme çubuğu ayrı bir bileşen oldu
                    }
                }

                // Navigasyon Bar her zaman görünür
                NavigationBar(containerColor = Color.Transparent) {
                    Screens.MainScreens.forEach { screen ->
                        val isSelected = navBackStackEntry?.destination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            icon = { Icon(painterResource(if (isSelected) screen.iconIdActive else screen.iconIdInactive), null) },
                            label = { Text(stringResource(screen.titleId), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            selected = isSelected,
                            onClick = { navController.backToMain(screen.route) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerProgressBar() {
    val playerConnection = LocalPlayerConnection.current ?: return
    var position by remember { mutableStateOf(playerConnection.player.currentPosition) }
    var duration by remember { mutableStateOf(playerConnection.player.duration) }

    LaunchedEffect(Unit) {
        while (true) {
            position = playerConnection.player.currentPosition
            duration = playerConnection.player.duration
            delay(200)
        }
    }

    if (duration > 0) {
        LinearProgressIndicator(
            progress = { position.toFloat() / duration },
            modifier = Modifier
                .height(2.dp)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.onSurface,
            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )
    }
}