package com.babelsoftware.loudly.ui.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.babelsoftware.loudly.R
import com.babelsoftware.loudly.ui.screens.Screens

// Data class representing navigation bar elements
private data class NavItem(
    val route: String,
    val iconResId: Int,
    val activeIconResId: Int,
    val labelResId: Int
)

// List of navigation elements
private val navItems = listOf(
    NavItem(Screens.Home.route, R.drawable.home_outlined, R.drawable.home_filled, R.string.home),
    NavItem(Screens.Explore.route, R.drawable.explore_outlined, R.drawable.explore_filled, R.string.explore),
    NavItem(Screens.Library.route, R.drawable.library_music_outlined, R.drawable.library_music_filled, R.string.filter_library)
)

/**
 * Custom-designed animated and scroll-sensitive bottom navigation bar, inspired by Apple Music.
 */
@Composable
fun LoudlyBottomNavBar(
    navController: NavController,
    currentDestination: NavDestination?,
    isScrolled: Boolean,
    onSearchClick: () -> Unit
) {
    val liquidAnimationSpec = spring<Dp>(
        dampingRatio = 0.8f,
        stiffness = Spring.StiffnessMedium
    )

    val containerHeight by animateDpAsState(targetValue = 60.dp, animationSpec = liquidAnimationSpec, label = "containerHeight")
    val iconSize by animateDpAsState(targetValue = 30.dp, animationSpec = liquidAnimationSpec, label = "iconSize")
    val cardPadding by animateDpAsState(if (isScrolled) 8.dp else 12.dp, liquidAnimationSpec, label = "cardPadding")
    val navCardShape = RoundedCornerShape(50.dp)
    val cardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.80f)
    )
    val cardBorder = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
    val cardElevation = CardDefaults.cardElevation(defaultElevation = 0.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = cardPadding)
            .height(containerHeight + 20.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Main Floating Island Card
        Card(
            modifier = Modifier
                .height(containerHeight)
                .weight(1f),
            shape = navCardShape,
            colors = cardColors,
            border = cardBorder,
            elevation = cardElevation
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                navItems.forEach { navItem ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == navItem.route } == true
                    NavButton(
                        item = navItem,
                        isSelected = isSelected,
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

        Spacer(modifier = Modifier.width(12.dp))

        // Search Button
        Card(
            modifier = Modifier
                .size(containerHeight)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onSearchClick
                ),
            shape = CircleShape,
            colors = cardColors,
            border = cardBorder,
            elevation = cardElevation
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
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
 * Composable representing each button in the navigation bar.
 */
@Composable
private fun NavButton(
    item: NavItem,
    isSelected: Boolean,
    iconSize: Dp,
    onClick: () -> Unit
) {
    val contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = if (isSelected) item.activeIconResId else item.iconResId),
            contentDescription = stringResource(id = item.labelResId),
            modifier = Modifier.size(iconSize),
            tint = contentColor
        )
    }
}

