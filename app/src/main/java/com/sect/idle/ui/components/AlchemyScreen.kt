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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.sect.idle.ui.GameViewModel
import com.sect.idle.ui.SectUiState

data class PillRecipe(
    val type: Int,
    val name: String,
    val description: String,
    val costQi: Long,
    val costStones: Long,
    val tier: String
)

@Composable
fun AlchemyScreen(
    viewModel: GameViewModel,
    state: SectUiState,
    modifier: Modifier = Modifier
) {
    val recipes = listOf(
        PillRecipe(
            type = 0,
            name = "Qi Condensation Pill",
            description = "Cleanses the meridians. Grants +500 Cultivation Exp.",
            costQi = 150L,
            costStones = 50L,
            tier = "Tier 1"
        ),
        PillRecipe(
            type = 1,
            name = "Foundation Breakthrough Pill",
            description = "Stabilizes spiritual sea. Grants +1,500 Cultivation Exp.",
            costQi = 350L,
            costStones = 120L,
            tier = "Tier 2"
        ),
        PillRecipe(
            type = 2,
            name = "Nine Revolutions Golden Core Elixir",
            description = "Celestial elixir. Grants +4,000 Cultivation Exp and purifies Dao heart.",
            costQi = 800L,
            costStones = 300L,
            tier = "Tier 3"
        ),
        PillRecipe(
            type = 3,
            name = "Tribulation Shield Pill",
            description = "Shields soul against heavenly lightning during realm ascensions.",
            costQi = 1200L,
            costStones = 500L,
            tier = "Tier 4"
        )
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
                text = "Alchemy Chamber & Elixir Furnace",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = GoldCelestial,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Refine spirit herbs and celestial essence into miraculous breakthrough pills.",
                style = MaterialTheme.typography.bodySmall.copy(color = CultivationTextSecondary)
            )
        }

        // Pill Recipes
        items(recipes) { recipe ->
            val canRefine = state.qi >= recipe.costQi && state.spiritStones >= recipe.costStones
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
                                .background(CinnabarRed.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = CinnabarRed,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = recipe.name,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = CultivationTextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "[${recipe.tier}]",
                                    style = MaterialTheme.typography.labelSmall.copy(color = GoldCelestial)
                                )
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = recipe.description,
                                style = MaterialTheme.typography.bodySmall.copy(color = CultivationTextSecondary)
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "Cost: ${recipe.costQi} Qi · ${recipe.costStones} Stones",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (canRefine) QiGlow else CinnabarRed
                                )
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.craftPill(recipe.type) },
                        enabled = canRefine,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CinnabarRed,
                            disabledContainerColor = Color(0xFF2B2B3D)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("refine_pill_${recipe.type}")
                    ) {
                        Text(
                            "Refine",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (canRefine) Color.White else CultivationTextMuted,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        // Inventory Display
        item {
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Refined Pills & Inventory (${state.inventory.size})",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = CultivationTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        if (state.inventory.isEmpty()) {
            item {
                Text(
                    text = "No pills or materials in sect inventory.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = CultivationTextMuted)
                )
            }
        } else {
            items(state.inventory) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E30)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = if (item.type == 0) "💊" else "🌿", fontSize = 18.sp)
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = CultivationTextPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                Text(
                                    text = "Rarity: ★".repeat(item.rarity + 1),
                                    style = MaterialTheme.typography.labelSmall.copy(color = GoldCelestial)
                                )
                            }
                        }
                        Text(
                            text = if (item.type == 0) "Consumable" else "Material",
                            style = MaterialTheme.typography.labelSmall.copy(color = SpiritBlue)
                        )
                    }
                }
            }
        }
    }
}
