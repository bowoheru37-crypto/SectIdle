package com.sect.idle.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.CinnabarRed
import com.example.ui.theme.CultivationTextPrimary
import com.example.ui.theme.CultivationTextSecondary
import com.example.ui.theme.GoldCelestial
import com.example.ui.theme.JadePrimary
import com.example.ui.theme.QiGlow
import com.example.ui.theme.SectDarkSurface
import com.sect.idle.render.AuraType
import com.sect.idle.render.SpiritAuraEffect

@Composable
fun CombatResultDialog(
    isVictory: Boolean,
    discipleName: String,
    discipleLevel: Int,
    expGained: Long,
    currentExp: Long,
    maxExp: Long,
    rewards: List<String>,
    stipulationName: String,
    crowdHypePeak: Int,
    finisherUsed: String?,
    onReturnToSect: () -> Unit,
    onRematch: () -> Unit
) {
    val expAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        val targetRatio = (currentExp.toFloat() / maxExp.coerceAtLeast(1L)).coerceIn(0f, 1f)
        expAnim.animateTo(
            targetValue = targetRatio,
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
        )
    }

    Dialog(onDismissRequest = onReturnToSect) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(animationSpec = tween(300)) + scaleIn(
                initialScale = 0.85f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(
                        width = 2.dp,
                        brush = Brush.verticalGradient(
                            if (isVictory) listOf(GoldCelestial, JadePrimary)
                            else listOf(CinnabarRed, Color(0xFF4A148C))
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isVictory) Color(0xFF0F1A15) else Color(0xFF1B0F13)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. Victory/Defeat Icon with Spirit Aura
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                if (isVictory) GoldCelestial.copy(alpha = 0.15f)
                                else CinnabarRed.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        SpiritAuraEffect(
                            auraType = if (isVictory) AuraType.GOLDEN_SWORD else AuraType.CRIMSON_FIRE,
                            intensity = 0.8f
                        )
                        Icon(
                            imageVector = if (isVictory) Icons.Default.EmojiEvents else Icons.Default.Shield,
                            contentDescription = null,
                            tint = if (isVictory) GoldCelestial else CinnabarRed,
                            modifier = Modifier.size(42.dp)
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    // 2. Banner Header
                    Text(
                        text = if (isVictory) "VICTORY · TOURNAMENT TRIUMPH" else "DEFEAT · VALIANT EFFORT",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = if (isVictory) GoldCelestial else CinnabarRed,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            textAlign = TextAlign.Center
                        )
                    )
                    Text(
                        text = stipulationName,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = CultivationTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    )

                    Spacer(Modifier.height(14.dp))

                    // 3. Disciple Experience Gain Bar
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SectDarkSurface),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "🥋 $discipleName",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = CultivationTextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Lv.$discipleLevel (+${expGained} EXP)",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = if (isVictory) JadePrimary else QiGlow,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { expAnim.value },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (isVictory) JadePrimary else QiGlow,
                                trackColor = Color(0xFF1E2824)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Exp: $currentExp / $maxExp",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = CultivationTextSecondary
                                ),
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // 4. Rewards Summary
                    if (rewards.isNotEmpty()) {
                        Text(
                            text = "🎁 REWARDS & TRIBUTE EARNED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = GoldCelestial,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(6.dp))
                        rewards.forEach { reward ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = GoldCelestial,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = reward,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = CultivationTextPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }

                    // 5. Match Highlights (Finisher / Crowd Hype)
                    if (finisherUsed != null || crowdHypePeak > 0) {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF141A24))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (finisherUsed != null) {
                                Text(
                                    text = "👑 Finisher: $finisherUsed",
                                    style = MaterialTheme.typography.labelSmall.copy(color = QiGlow)
                                )
                            }
                            Text(
                                text = "🔥 Peak Hype: $crowdHypePeak%",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFFFB74D))
                            )
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    // 6. Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onRematch,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("rematch_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Rematch", style = MaterialTheme.typography.labelMedium)
                        }

                        Button(
                            onClick = onReturnToSect,
                            modifier = Modifier
                                .weight(1.3f)
                                .height(44.dp)
                                .testTag("return_to_sect_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isVictory) JadePrimary else Color(0xFF424242)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                "Return to Sect",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = if (isVictory) Color.Black else Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
