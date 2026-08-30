package com.sect.idle.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import com.sect.idle.ai.EventCategory
import com.sect.idle.ai.SectRandomEvent
import com.sect.idle.ui.GameViewModel
import com.sect.idle.ui.SectUiState

@Composable
fun HeavenlyEventDialog(
    event: SectRandomEvent,
    state: SectUiState,
    viewModel: GameViewModel,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(2.dp, GoldCelestial, RoundedCornerShape(20.dp))
                .testTag("heavenly_event_dialog"),
            color = SectDarkBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Header with Category Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val categoryColor = when (event.category) {
                        EventCategory.CELESTIAL_PHENOMENON -> GoldCelestial
                        EventCategory.DEMONIC_INCURSION -> CinnabarRed
                        EventCategory.DAO_EPIPHANY -> QiGlow
                        EventCategory.TRAVELING_IMMORTAL -> SpiritBlue
                        EventCategory.ANCIENT_RUIN -> JadePrimary
                        EventCategory.SECT_DILEMMA -> Color(0xFFFFB300)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(categoryColor.copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = event.category.name.replace("_", " "),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = categoryColor,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = CultivationTextMuted
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = GoldCelestial,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = GoldCelestial,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(Modifier.height(10.dp))

                // Lore Description Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = SectDarkSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = event.description,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = CultivationTextPrimary,
                            lineHeight = 22.sp
                        ),
                        modifier = Modifier.padding(14.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Sect Master's Decrees:",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = CultivationTextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(Modifier.height(8.dp))

                // Choice Options
                event.choices.forEachIndexed { index, choice ->
                    val canAfford = state.qi >= choice.requiredQi && state.spiritStones >= choice.requiredStones

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .testTag("event_choice_$index"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (canAfford) Color(0xFF16253B) else Color(0xFF261824)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        border = if (canAfford) null else androidx.compose.foundation.BorderStroke(1.dp, CinnabarRed.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = choice.text,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = if (canAfford) GoldCelestial else CultivationTextMuted,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "${(choice.successRate * 100).toInt()}% Chance",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (choice.successRate >= 0.8f) JadePrimary else Color(0xFFFFB300)
                                    )
                                )
                            }

                            Spacer(Modifier.height(4.dp))

                            Text(
                                text = choice.description,
                                style = MaterialTheme.typography.bodySmall.copy(color = CultivationTextSecondary)
                            )

                            Spacer(Modifier.height(8.dp))

                            // Cost and Rewards Info
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    if (choice.requiredQi > 0 || choice.requiredStones > 0) {
                                        Text(
                                            text = "Cost: ${if (choice.requiredQi > 0) "${choice.requiredQi} Qi " else ""}${if (choice.requiredStones > 0) "${choice.requiredStones} Stones" else ""}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (canAfford) CultivationTextMuted else CinnabarRed
                                            )
                                        )
                                    }
                                    if (choice.rewardSummary.isNotBlank()) {
                                        Text(
                                            text = "Reward: ${choice.rewardSummary}",
                                            style = MaterialTheme.typography.labelSmall.copy(color = QiGlow)
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        viewModel.resolveEventChoice(choice)
                                    },
                                    enabled = canAfford,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (canAfford) JadePrimary else Color.Gray,
                                        contentColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = if (canAfford) "Execute" else "Lacking Qi",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Observe from Mountain Peak (Ignore)",
                        style = MaterialTheme.typography.labelSmall.copy(color = CultivationTextMuted)
                    )
                }
            }
        }
    }
}
