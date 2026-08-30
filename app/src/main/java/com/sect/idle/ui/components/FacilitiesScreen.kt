package com.sect.idle.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CinnabarRed
import com.example.ui.theme.CultivationTextMuted
import com.example.ui.theme.CultivationTextPrimary
import com.example.ui.theme.CultivationTextSecondary
import com.example.ui.theme.GoldCelestial
import com.example.ui.theme.JadePrimary
import com.example.ui.theme.QiGlow
import com.example.ui.theme.SectDarkBackground
import com.example.ui.theme.SectDarkSurface
import com.example.ui.theme.SpiritBlue
import com.sect.idle.models.Building
import com.sect.idle.ui.GameViewModel
import com.sect.idle.ui.SectUiState

@Composable
fun FacilitiesScreen(
    viewModel: GameViewModel,
    state: SectUiState,
    modifier: Modifier = Modifier
) {
    var buildingToDemolish by remember { mutableStateOf<Building?>(null) }

    CriticalActionDialog(
        visible = buildingToDemolish != null,
        title = "Dismantle Pavilion",
        subtitle = "Facility Downgrade",
        description = "Are you sure you want to demolish this pavilion? Its tier will decrease by 1, reducing passive spiritual gathering and sect bonuses.",
        actionType = CriticalActionType.DEMOLISH_BUILDING,
        targetName = buildingToDemolish?.name,
        targetDetail = buildingToDemolish?.let { "Current Level: ${it.level} ➔ New Level: ${(it.level - 1).coerceAtLeast(1)}" },
        confirmButtonText = "Demolish Facility",
        onConfirm = {
            buildingToDemolish?.let {
                viewModel.demolishBuilding(it.type)
            }
        },
        onDismiss = { buildingToDemolish = null }
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SectDarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            Text(
                text = "Sect Pavilions & Spiritual Arrays",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = GoldCelestial,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Upgrading pavilions expands Qi gathering, disciple capacity, and crafting efficiencies.",
                style = MaterialTheme.typography.bodySmall.copy(color = CultivationTextSecondary)
            )
        }

        // Sect Rank Ascension Card
        item {
            val reqRep = state.sectLevel * 100L
            val reqStones = state.sectLevel * 300L
            val canAscend = state.reputation >= reqRep && state.spiritStones >= reqStones

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1D36)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "🌟 ${state.sectRankName} (Tier ${state.sectLevel})",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = GoldCelestial,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "Req: $reqRep Fame (Current: ${state.reputation}) · $reqStones Stones",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (canAscend) QiGlow else CultivationTextSecondary
                                )
                            )
                        }

                        Button(
                            onClick = { viewModel.promoteSectRank() },
                            enabled = canAscend,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldCelestial,
                                disabledContainerColor = Color(0xFF2B2B3D)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("ascend_sect_rank_button")
                        ) {
                            Text(
                                "Ascend Rank",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (canAscend) Color.Black else CultivationTextMuted,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }

        items(state.buildings) { building ->
            BuildingCard(
                building = building,
                state = state,
                onUpgrade = { viewModel.upgradeBuilding(building.type) },
                onDemolish = { buildingToDemolish = building }
            )
        }
    }
}

@Composable
private fun BuildingCard(
    building: Building,
    state: SectUiState,
    onUpgrade: () -> Unit,
    onDemolish: () -> Unit
) {
    val icon: ImageVector
    val iconColor: Color
    val effectDescription: String

    when (building.type) {
        0 -> {
            icon = Icons.Default.AccountBalance
            iconColor = GoldCelestial
            effectDescription = "Max Disciples Capacity: ${building.level * 5}"
        }
        1 -> {
            icon = Icons.Default.AutoAwesome
            iconColor = SpiritBlue
            effectDescription = "Qi Gathering Rate: +${building.level * 3} Qi/sec, Max Qi: ${state.maxQi}"
        }
        2 -> {
            icon = Icons.Default.LocalFireDepartment
            iconColor = CinnabarRed
            effectDescription = "Pill Crafting Quality: Tier ${building.level}, Speed +${building.level * 10}%"
        }
        3 -> {
            icon = Icons.Default.Grass
            iconColor = JadePrimary
            effectDescription = "Herb Gathering Yield: +${building.level * 25}%"
        }
        4 -> {
            icon = Icons.Default.Book
            iconColor = Color(0xFFAB47BC)
            effectDescription = "Disciple Cultivation Speed: +${building.level * 15}%"
        }
        else -> {
            icon = Icons.Default.Shield
            iconColor = Color(0xFFFF9800)
            effectDescription = "Sect Defense Array & Beast Suppression: Power ${building.level * 100}"
        }
    }

    val costQi = building.level * 200L
    val costStones = building.level * 100L
    val canAfford = state.qi >= costQi && state.spiritStones >= costStones

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SectDarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(iconColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "${building.name} (Lv.${building.level})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = CultivationTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = effectDescription,
                        style = MaterialTheme.typography.bodySmall.copy(color = CultivationTextSecondary)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Cost: $costQi Qi · $costStones Stones",
                        style = MaterialTheme.typography.labelSmall.copy(color = if (canAfford) QiGlow else CinnabarRed)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (building.level > 1) {
                    IconButton(
                        onClick = onDemolish,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("demolish_building_${building.name}")
                    ) {
                        Icon(
                            Icons.Default.DeleteSweep,
                            contentDescription = "Demolish Facility",
                            tint = Color(0xFFFF9800).copy(alpha = 0.85f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                }

                Button(
                    onClick = onUpgrade,
                    enabled = canAfford,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JadePrimary,
                        disabledContainerColor = Color(0xFF2B2B3D)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("upgrade_building_${building.name}")
                ) {
                    Text(
                        "Upgrade",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (canAfford) Color.Black else CultivationTextMuted,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}
