package com.babelsoftware.loudly.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.babelsoftware.loudly.R
import com.babelsoftware.loudly.ui.screens.Screens

// Navigasyon barı elemanlarını temsil eden veri sınıfı
private data class NavItem(
    val route: String,
    val iconResId: Int,
    val activeIconResId: Int,
    val labelResId: Int
)

// Navigasyon elemanlarının listesi
private val navItems = listOf(
    NavItem(Screens.Home.route, R.drawable.home_outlined, R.drawable.home_filled, R.string.home),
    NavItem(Screens.Explore.route, R.drawable.explore_outlined, R.drawable.explore_filled, R.string.explore),
    NavItem(Screens.Library.route, R.drawable.library_music_outlined, R.drawable.library_music_filled, R.string.filter_library)
)

/**
 * Loudly uygulaması için özel olarak tasarlanmış, animasyonlu ve kaydırma durumuna duyarlı alt navigasyon barı.
 */
@Composable
fun LoudlyBottomNavBar(
    navController: NavController,
    currentDestination: NavDestination?,
    isScrolled: Boolean,
    onSearchClick: () -> Unit
) {
    val liquidAnimationSpec = spring<Dp>(
        dampingRatio = 0.75f,
        stiffness = Spring.StiffnessLow
    )

    val containerHeight by animateDpAsState(if (isScrolled) 58.dp else 64.dp, liquidAnimationSpec, label = "containerHeight")
    val iconSize by animateDpAsState(if (isScrolled) 24.dp else 26.dp, liquidAnimationSpec, label = "iconSize")
    val gapWidth by animateDpAsState(if (isScrolled) 0.dp else 8.dp, liquidAnimationSpec, label = "gapWidth")
    val navItemsArrangement by animateDpAsState(if (isScrolled) 16.dp else 20.dp, liquidAnimationSpec, label = "navItemsArrangement")
    val navGroupHorizontalPadding by animateDpAsState(if (isScrolled) 18.dp else 20.dp, liquidAnimationSpec, label = "navGroupHorizontalPadding")
    val navGroupShape = RoundedCornerShape(
        topStart = 32.dp,
        bottomStart = 32.dp,
        topEnd = if (isScrolled) 0.dp else 32.dp,
        bottomEnd = if (isScrolled) 0.dp else 32.dp
    )
    val searchGroupShape = RoundedCornerShape(
        topStart = if (isScrolled) 0.dp else 32.dp,
        bottomStart = if (isScrolled) 0.dp else 32.dp,
        topEnd = 32.dp,
        bottomEnd = 32.dp
    )

    val cardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
    )
    val cardBorder = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
    val cardElevation = CardDefaults.cardElevation(defaultElevation = 8.dp)


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(containerHeight + 24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            modifier = Modifier.height(containerHeight),
            shape = navGroupShape,
            colors = cardColors,
            border = cardBorder,
            elevation = cardElevation
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = navGroupHorizontalPadding),
                horizontalArrangement = Arrangement.spacedBy(navItemsArrangement),
                verticalAlignment = Alignment.CenterVertically
            ) {
                navItems.forEach { navItem ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == navItem.route } == true
                    NavButton(
                        item = navItem,
                        isSelected = isSelected,
                        isScrolled = isScrolled,
                        iconSize = iconSize,
                        onClick = {
                            if (!isSelected) {
                                navController.navigate(navItem.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(gapWidth))

        Card(
            modifier = Modifier
                .size(containerHeight)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onSearchClick
                ),
            shape = searchGroupShape,
            colors = cardColors,
            border = cardBorder,
            elevation = cardElevation
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = isScrolled,
                    enter = fadeIn(animationSpec = spring()),
                    exit = fadeOut(animationSpec = spring())
                ) {
                    HorizontalDivider(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .height(24.dp)
                            .width(1.dp)
                            .padding(start = 0.5.dp),
                        color = Color.White.copy(alpha = 0.3f)
                    )
                }
                Icon(
                    painter = painterResource(id = R.drawable.search),
                    contentDescription = stringResource(R.string.search),
                    modifier = Modifier.size(iconSize),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * Navigasyon barı içindeki her bir butonu temsil eden Composable.
 */
@Composable
private fun NavButton(
    item: NavItem,
    isSelected: Boolean,
    isScrolled: Boolean,
    iconSize: Dp,
    onClick: () -> Unit
) {
    val contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        // DEĞİŞİKLİK: İçeriği dikeyde mükemmel ortalamak için Arrangement.Center kullanılıyor.
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = if (isSelected) item.activeIconResId else item.iconResId),
            contentDescription = stringResource(id = item.labelResId),
            modifier = Modifier.size(iconSize),
            tint = contentColor
        )

        // DEĞİŞİKLİK: AnimatedVisibility, metin kaybolduğunda düzen ağacından tamamen çıkarak
        // ikonun doğru şekilde ortalanmasını sağlar.
        AnimatedVisibility(
            visible = !isScrolled,
            enter = fadeIn(spring(stiffness = Spring.StiffnessMedium)),
            exit = fadeOut(spring(stiffness = Spring.StiffnessMedium))
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(id = item.labelResId),
                    color = contentColor,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false
                )
            }
        }
    }
}

