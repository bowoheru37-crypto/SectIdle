package com.sect.idle.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.example.ui.theme.SectDarkSurface
import com.example.ui.theme.SpiritBlue
import com.sect.idle.ai.CultivationArenaEngine
import com.sect.idle.render.AuraType
import com.sect.idle.render.SpiritAuraEffect

@Composable
fun BattleHudOverlay(
    discipleProfile: CultivationArenaEngine.FighterProfile?,
    opponentProfile: CultivationArenaEngine.FighterProfile?,
    discipleHp: Int,
    discipleMaxHp: Int,
    enemyHp: Int,
    enemyMaxHp: Int,
    modifier: Modifier = Modifier
) {
    var expandedStats by remember { mutableStateOf(false) }

    val dHpPercent by animateFloatAsState(
        targetValue = (discipleHp.toFloat() / discipleMaxHp.coerceAtLeast(1)).coerceIn(0f, 1f),
        animationSpec = tween(300),
        label = "disciple_hp_anim"
    )

    val eHpPercent by animateFloatAsState(
        targetValue = (enemyHp.toFloat() / enemyMaxHp.coerceAtLeast(1)).coerceIn(0f, 1f),
        animationSpec = tween(300),
        label = "enemy_hp_anim"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                1.dp,
                Brush.horizontalGradient(listOf(JadePrimary.copy(alpha = 0.5f), GoldCelestial.copy(alpha = 0.5f), CinnabarRed.copy(alpha = 0.5f))),
                RoundedCornerShape(14.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111422))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // 1. Dual Fighter Titles & Realms
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Disciple Header (Left)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(JadePrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        SpiritAuraEffect(auraType = AuraType.JADE_WOOD, intensity = 0.6f)
                        Text("☯", fontSize = 16.sp, color = JadePrimary)
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = discipleProfile?.name ?: "Lin Chen",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = JadePrimary,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                        Text(
                            text = discipleProfile?.realmName ?: "Qi Refining · Realm 1",
                            style = MaterialTheme.typography.labelSmall.copy(color = CultivationTextSecondary, fontSize = 10.sp)
                        )
                    }
                }

                // VS Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(GoldCelestial.copy(alpha = 0.15f))
                        .border(1.dp, GoldCelestial.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "VS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = GoldCelestial,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }

                // Challenger Header (Right)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = opponentProfile?.name ?: "Blood Asura",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = CinnabarRed,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                        Text(
                            text = opponentProfile?.realmName ?: "Foundation Realm",
                            style = MaterialTheme.typography.labelSmall.copy(color = CultivationTextSecondary, fontSize = 10.sp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(CinnabarRed.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        SpiritAuraEffect(auraType = AuraType.CRIMSON_FIRE, intensity = 0.6f)
                        Text("🔥", fontSize = 14.sp)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // 2. Health Gauges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Disciple HP Bar
                Column(modifier = Modifier.weight(1f)) {
                    LinearProgressIndicator(
                        progress = { dHpPercent },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = JadePrimary,
                        trackColor = Color(0xFF13321B)
                    )
                    Spacer(Modifier.height(2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("HP $discipleHp / $discipleMaxHp", style = MaterialTheme.typography.labelSmall.copy(color = JadePrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold))
                        Text("${(dHpPercent * 100).toInt()}%", style = MaterialTheme.typography.labelSmall.copy(color = CultivationTextSecondary, fontSize = 10.sp))
                    }
                }

                // Opponent HP Bar
                Column(modifier = Modifier.weight(1f)) {
                    LinearProgressIndicator(
                        progress = { eHpPercent },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = CinnabarRed,
                        trackColor = Color(0xFF381313)
                    )
                    Spacer(Modifier.height(2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${(eHpPercent * 100).toInt()}%", style = MaterialTheme.typography.labelSmall.copy(color = CultivationTextSecondary, fontSize = 10.sp))
                        Text("HP $enemyHp / $enemyMaxHp", style = MaterialTheme.typography.labelSmall.copy(color = CinnabarRed, fontSize = 10.sp, fontWeight = FontWeight.Bold))
                    }
                }
            }

            // 3. Expandable Martial Stats Toggle
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedStats = !expandedStats }
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (expandedStats) "Hide Martial Attributes" else "View Martial Attributes & Stats",
                    style = MaterialTheme.typography.labelSmall.copy(color = QiGlow, fontSize = 10.5.sp)
                )
                Icon(
                    imageVector = if (expandedStats) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = QiGlow,
                    modifier = Modifier.size(16.dp)
                )
            }

            AnimatedVisibility(visible = expandedStats) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0A0C16))
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left Fighter Stats
                        Column(modifier = Modifier.weight(1f)) {
                            Text("⚔️ ATK: ${discipleProfile?.atk ?: 45}", style = MaterialTheme.typography.labelSmall.copy(color = CultivationTextPrimary, fontSize = 10.sp))
                            Text("🛡️ DEF: ${discipleProfile?.def ?: 28}", style = MaterialTheme.typography.labelSmall.copy(color = CultivationTextPrimary, fontSize = 10.sp))
                            Text("💨 SPD: ${discipleProfile?.spd ?: 18}", style = MaterialTheme.typography.labelSmall.copy(color = CultivationTextPrimary, fontSize = 10.sp))
                            Text("🥋 Style: ${discipleProfile?.archetype?.title ?: "Striker"}", style = MaterialTheme.typography.labelSmall.copy(color = QiGlow, fontSize = 10.sp))
                        }

                        // Right Fighter Stats
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                            Text("ATK: ${opponentProfile?.atk ?: 55} ⚔️", style = MaterialTheme.typography.labelSmall.copy(color = CultivationTextPrimary, fontSize = 10.sp))
                            Text("DEF: ${opponentProfile?.def ?: 30} 🛡️", style = MaterialTheme.typography.labelSmall.copy(color = CultivationTextPrimary, fontSize = 10.sp))
                            Text("SPD: ${opponentProfile?.spd ?: 15} 💨", style = MaterialTheme.typography.labelSmall.copy(color = CultivationTextPrimary, fontSize = 10.sp))
                            Text("Style: ${opponentProfile?.archetype?.title ?: "Grappler"} 🥋", style = MaterialTheme.typography.labelSmall.copy(color = CinnabarRed, fontSize = 10.sp))
                        }
                    }
                }
            }
        }
    }
}
