package com.sect.idle.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CultivationTextPrimary
import com.example.ui.theme.CultivationTextSecondary
import com.example.ui.theme.GoldCelestial
import com.example.ui.theme.JadePrimary
import com.example.ui.theme.QiGlow
import com.example.ui.theme.SectDarkBackground
import com.example.ui.theme.SectDarkSurface
import com.example.ui.theme.SectDarkSurfaceCard
import com.sect.idle.ui.components.BattleScreen
import com.sect.idle.ui.components.CultivationScreen
import com.sect.idle.ui.components.DisciplesScreen
import com.sect.idle.ui.components.FacilitiesScreen
import com.sect.idle.ui.components.GrandmasterAiScreen
import com.sect.idle.ui.components.SectCanvasView
import kotlinx.coroutines.delay

data class NavItem(
    val label: String,
    val icon: ImageVector,
    val index: Int
)

val SECT_NAV_ITEMS = listOf(
    NavItem("Mountain", Icons.Default.Landscape, 0),
    NavItem("Disciples", Icons.Default.Group, 1),
    NavItem("Buildings", Icons.Default.AccountBalance, 2),
    NavItem("Cultivation", Icons.Default.SelfImprovement, 3),
    NavItem("Expedition", Icons.Default.Explore, 4),
    NavItem("Ancestor AI", Icons.Default.AutoAwesome, 5)
)

@Composable
fun MainGameScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    // Auto-dismiss transient notifications
    LaunchedEffect(state.notificationMessage) {
        if (state.notificationMessage != null) {
            delay(3000)
            viewModel.clearNotification()
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(SectDarkBackground)
    ) {
        val isWideScreen = maxWidth >= 600.dp

        if (isWideScreen) {
            // Adaptive Layout for Tablet / Landscape / Foldable devices
            Row(modifier = Modifier.fillMaxSize()) {
                // Adaptive Left Navigation Rail
                NavigationRail(
                    containerColor = SectDarkSurface,
                    header = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 12.dp)
                        ) {
                            Text("🏔️", fontSize = 24.sp)
                            Text(
                                text = state.sectName.take(8),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = GoldCelestial,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    },
                    modifier = Modifier.fillMaxHeight()
                ) {
                    SECT_NAV_ITEMS.forEach { item ->
                        NavigationRailItem(
                            selected = state.activeTab == item.index,
                            onClick = { viewModel.selectTab(item.index) },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label, fontSize = 10.sp) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = JadePrimary,
                                selectedTextColor = JadePrimary,
                                unselectedIconColor = CultivationTextSecondary,
                                unselectedTextColor = CultivationTextSecondary,
                                indicatorColor = SectDarkSurfaceCard
                            ),
                            modifier = Modifier.testTag("nav_rail_${item.label}")
                        )
                    }
                }

                // Right Content Area
                Column(modifier = Modifier.fillMaxSize()) {
                    TopSectHeader(
                        state = state,
                        onToggleSound = { viewModel.toggleSound() }
                    )

                    Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                        ActiveScreenContent(viewModel = viewModel, state = state)
                    }
                }
            }
        } else {
            // Standard Mobile Handheld Layout
            Scaffold(
                topBar = {
                    TopSectHeader(
                        state = state,
                        onToggleSound = { viewModel.toggleSound() }
                    )
                },
                bottomBar = {
                    BottomSectNav(
                        activeTab = state.activeTab,
                        onSelectTab = { viewModel.selectTab(it) }
                    )
                },
                modifier = Modifier.fillMaxSize(),
                containerColor = SectDarkBackground
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    ActiveScreenContent(viewModel = viewModel, state = state)
                }
            }
        }

        // Heavenly Random Event Dialog
        state.activeRandomEvent?.let { event ->
            com.sect.idle.ui.components.HeavenlyEventDialog(
                event = event,
                state = state,
                viewModel = viewModel,
                onDismiss = { viewModel.dismissRandomEvent() }
            )
        }

        // Notification Banner overlay
        AnimatedVisibility(
            visible = state.notificationMessage != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = GoldCelestial),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Text(
                    text = state.notificationMessage ?: "",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun ActiveScreenContent(
    viewModel: GameViewModel,
    state: SectUiState
) {
    when (state.activeTab) {
        0 -> SectCanvasView(viewModel = viewModel, state = state)
        1 -> DisciplesScreen(viewModel = viewModel, state = state)
        2 -> FacilitiesScreen(viewModel = viewModel, state = state)
        3 -> CultivationScreen(viewModel = viewModel, state = state)
        4 -> BattleScreen(viewModel = viewModel, state = state)
        5 -> GrandmasterAiScreen(viewModel = viewModel, state = state)
        else -> SectCanvasView(viewModel = viewModel, state = state)
    }
}

@Composable
private fun TopSectHeader(
    state: SectUiState,
    onToggleSound: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = SectDarkSurface)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🏔️ ${state.sectName}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = GoldCelestial,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Year ${state.year} · ${state.season} Day ${state.day % 30 + 1} (${state.sectRankName})",
                        style = MaterialTheme.typography.labelSmall.copy(color = CultivationTextSecondary)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Spirit Stones
                    Text(
                        text = "💎 ${state.spiritStones}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = QiGlow,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(Modifier.width(12.dp))
                    IconButton(
                        onClick = onToggleSound,
                        modifier = Modifier.size(32.dp).testTag("sound_toggle_button")
                    ) {
                        Icon(
                            if (state.soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                            contentDescription = "Sound Toggle",
                            tint = CultivationTextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // Sect Spiritual Qi Pool Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Heavenly Qi: ${state.qi} / ${state.maxQi}",
                    style = MaterialTheme.typography.labelSmall.copy(color = JadePrimary, fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = "Sect Fame: ${state.reputation} · Karma: ${state.karma}",
                    style = MaterialTheme.typography.labelSmall.copy(color = CultivationTextSecondary)
                )
            }
        }
    }
}

@Composable
private fun BottomSectNav(
    activeTab: Int,
    onSelectTab: (Int) -> Unit
) {
    NavigationBar(
        containerColor = SectDarkSurface,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        SECT_NAV_ITEMS.forEach { item ->
            NavigationBarItem(
                selected = activeTab == item.index,
                onClick = { onSelectTab(item.index) },
                icon = {
                    Icon(
                        item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 9.sp,
                        fontWeight = if (activeTab == item.index) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = JadePrimary,
                    selectedTextColor = JadePrimary,
                    unselectedIconColor = CultivationTextSecondary,
                    unselectedTextColor = CultivationTextSecondary,
                    indicatorColor = SectDarkSurfaceCard
                ),
                modifier = Modifier.testTag("nav_tab_${item.label}")
            )
        }
    }
}

