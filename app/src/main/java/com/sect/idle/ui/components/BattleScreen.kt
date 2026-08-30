package com.sect.idle.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SportsMma
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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
import com.sect.idle.ai.CultivationArenaEngine
import com.sect.idle.ui.GameViewModel
import com.sect.idle.ui.SectUiState
import com.sect.idle.ui.base.BattleState

data class ArenaTournamentCard(
    val title: String,
    val stipulation: CultivationArenaEngine.MatchStipulation,
    val difficulty: Int,
    val opponentName: String,
    val opponentTitle: String,
    val prizePool: String,
    val recommendedRealm: String
)

@Composable
fun BattleScreen(
    viewModel: GameViewModel,
    state: SectUiState,
    modifier: Modifier = Modifier
) {
    val bState = state.battleState

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SectDarkBackground)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        if (bState.inBattle) {
            // Live WWE / MMA / Xianxia Donghua Arena Battle Scene
            ActiveArenaBattleView(
                viewModel = viewModel,
                state = state,
                onExit = { viewModel.exitBattle() }
            )
        } else {
            // Arena Tournament & Match Stipulation Selection Screen
            ArenaLobbySelectionView(viewModel = viewModel, state = state)
        }

        // Tale of the Tape Modal Dialog
        if (bState.inBattle && bState.showTaleOfTheTape) {
            TaleOfTheTapeDialog(
                discipleProfile = bState.discipleProfile,
                opponentProfile = bState.opponentProfile,
                stipulation = bState.stipulation,
                onDismiss = { viewModel.toggleTaleOfTheTape() }
            )
        }

        // Post-match Victory/Defeat Animated Result Dialog
        if (bState.inBattle && bState.isVictory != null) {
            val disciple = state.disciples.firstOrNull()
            CombatResultDialog(
                isVictory = bState.isVictory == true,
                discipleName = bState.discipleProfile?.name ?: (disciple?.name ?: "Disciple"),
                discipleLevel = bState.discipleLevel,
                expGained = bState.discipleExpGain,
                currentExp = bState.discipleCurrentExp,
                maxExp = bState.discipleMaxExp,
                rewards = bState.rewards,
                stipulationName = bState.stipulation.displayName,
                crowdHypePeak = bState.peakCrowdHype,
                finisherUsed = if (bState.isVictory == true) bState.discipleProfile?.finisherMove else null,
                onReturnToSect = { viewModel.exitBattle() },
                onRematch = {
                    viewModel.startArenaMatch(
                        stipulation = bState.stipulation,
                        difficulty = 1,
                        customOpponentName = bState.opponentProfile?.name
                    )
                }
            )
        }
    }
}

// ============================================================================
// ARENA LOBBY & TOURNAMENT SELECTION
// ============================================================================

@Composable
private fun ArenaLobbySelectionView(
    viewModel: GameViewModel,
    state: SectUiState
) {
    val tournaments = listOf(
        ArenaTournamentCard(
            title = "All-Heavens Martial Dao Championship",
            stipulation = CultivationArenaEngine.MatchStipulation.GRAND_DAO_CHAMPIONSHIP,
            difficulty = 1,
            opponentName = "Viper Fang of Green Mist",
            opponentTitle = "The Venomous Striker (12-4, 9 KO)",
            prizePool = "350 Spirit Stones · Sect Contender Belt",
            recommendedRealm = "Qi Condensation"
        ),
        ArenaTournamentCard(
            title = "Eight Trigrams 10,000V Dao Steel Cage",
            stipulation = CultivationArenaEngine.MatchStipulation.BAGUA_STEEL_CAGE,
            difficulty = 2,
            opponentName = "Blood Asura Berserker",
            opponentTitle = "Demonic Grappler Champion (24-2, 21 KO)",
            prizePool = "650 Spirit Stones · Inter-Sect Title Belt",
            recommendedRealm = "Foundation Establishment"
        ),
        ArenaTournamentCard(
            title = "Demonic Gauntlet Royal Rumble",
            stipulation = CultivationArenaEngine.MatchStipulation.DEMON_ROYAL_RUMBLE,
            difficulty = 3,
            opponentName = "Sword Sovereign Xie Tian",
            opponentTitle = "Apex Sword Virtuoso (38-1, 34 KO)",
            prizePool = "1,200 Spirit Stones · Grand Celestial World Belt",
            recommendedRealm = "Golden Core"
        ),
        ArenaTournamentCard(
            title = "Forbidden Sect No-Holds-Barred Extinction",
            stipulation = CultivationArenaEngine.MatchStipulation.NO_HOLDS_BARRED,
            difficulty = 4,
            opponentName = "Primordial Void Dragon God",
            opponentTitle = "Universal Destruction Emperor (99-0, 95 KO)",
            prizePool = "3,000 Spirit Stones · Undisputed Universal Title",
            recommendedRealm = "Nascent Soul / Tribulation"
        )
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            // Header Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SectDarkSurface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(GoldCelestial.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = GoldCelestial,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Grand Celestial Martial Arts Arena",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = GoldCelestial,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "WWE / MMA Xianxia Donghua Championship Arena · Broadcast Live to All Sects!",
                            style = MaterialTheme.typography.bodySmall.copy(color = CultivationTextSecondary)
                        )
                    }
                }
            }
        }

        items(tournaments) { t ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SectDarkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(t.stipulation.icon, fontSize = 20.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = t.title,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = CultivationTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Text(
                            text = "★".repeat(t.difficulty),
                            style = MaterialTheme.typography.labelSmall.copy(color = GoldCelestial)
                        )
                    }

                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "🥋 Opponent: ${t.opponentName} · ${t.opponentTitle}",
                        style = MaterialTheme.typography.bodySmall.copy(color = CinnabarRed, fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = t.stipulation.description,
                        style = MaterialTheme.typography.bodySmall.copy(color = CultivationTextSecondary)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "🏆 Stakes: ${t.prizePool} · Req: ${t.recommendedRealm}",
                        style = MaterialTheme.typography.labelSmall.copy(color = QiGlow)
                    )
                    Spacer(Modifier.height(10.dp))

                    Button(
                        onClick = {
                            viewModel.startArenaMatch(
                                stipulation = t.stipulation,
                                difficulty = t.difficulty,
                                customOpponentName = t.opponentName
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = JadePrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("enter_arena_${t.difficulty}")
                    ) {
                        Text(
                            "⚡ ENTER CELESTIAL OCTAGON RING",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// ACTIVE WWE / MMA XIANXIA ARENA BATTLE VIEW
// ============================================================================

@Composable
private fun ActiveArenaBattleView(
    viewModel: GameViewModel,
    state: SectUiState,
    onExit: () -> Unit
) {
    val bState = state.battleState
    val discipleProf = bState.discipleProfile
    val oppProf = bState.opponentProfile

    val dHpPercent = (bState.discipleHp.toFloat() / bState.discipleMaxHp.coerceAtLeast(1)).coerceIn(0f, 1f)
    val eHpPercent = (bState.enemyHp.toFloat() / bState.enemyMaxHp.coerceAtLeast(1)).coerceIn(0f, 1f)
    val hypePercent = (bState.crowdHype / 100f).coerceIn(0f, 1f)

    val infiniteTransition = rememberInfiniteTransition(label = "hype_glow")
    val finisherPulse by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "finisher_pulse"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // 1. Live Broadcast Header Bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SectDarkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("👑", fontSize = 18.sp)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = bState.stipulation.displayName,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = GoldCelestial,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { viewModel.toggleTaleOfTheTape() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = "Tale of the Tape",
                                tint = QiGlow,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. Crowd Hype Meter & Live Fan Chant
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161928)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🔥 CROWD HYPE:", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFFF9800), fontWeight = FontWeight.Bold))
                            Spacer(Modifier.width(6.dp))
                            Text("${bState.crowdHype}%", style = MaterialTheme.typography.labelSmall.copy(color = GoldCelestial, fontWeight = FontWeight.Bold))
                        }
                        if (bState.crowdHype >= 100) {
                            Text("⚡ FINISHER READY! ⚡", style = MaterialTheme.typography.labelSmall.copy(color = QiGlow, fontWeight = FontWeight.Bold))
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { hypePercent },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (bState.crowdHype >= 100) QiGlow else Color(0xFFFF9800),
                        trackColor = Color(0xFF332005)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = bState.crowdChant,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = CultivationTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }

        // 3. Real-time Dual Fighter Health & Realm Overlay
        item {
            BattleHudOverlay(
                discipleProfile = discipleProf,
                opponentProfile = oppProf,
                discipleHp = bState.discipleHp,
                discipleMaxHp = bState.discipleMaxHp,
                enemyHp = bState.enemyHp,
                enemyMaxHp = bState.enemyMaxHp
            )
        }

        // 4. 2.5D Isometric Celestial Octagon Canvas Ring with Combat Skill VFX Overlay!
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                ArenaRingCanvas(battleState = bState)

                // Spirit Skill VFX Canvas (Sword slashes, spirit blasts, shockwaves)
                com.sect.idle.render.CombatSkillVfxCanvas(
                    vfxType = bState.activeVfx,
                    progress = bState.vfxProgress,
                    sourcePos = androidx.compose.ui.geometry.Offset(140f, 420f),
                    targetPos = androidx.compose.ui.geometry.Offset(380f, 320f),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // 5. Elder Referee Callout Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF20202F))
                    .border(1.dp, GoldCelestial.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⚖️ ${bState.refereeCall}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = GoldCelestial,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        // 6. Dual Announcer Desk Commentary Box & Scrolling Combat Chronicle Log
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF101322)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "🎙️ RINGSIDE COMMENTARY DESK",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = QiGlow,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = bState.playByPlayCommentary,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = CultivationTextPrimary,
                                lineHeight = 16.sp
                            )
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = bState.colorCommentary,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFFFD54F),
                                lineHeight = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                // Scrolling Thematic Combat Chronicle
                if (bState.structuredLogs.isNotEmpty()) {
                    CombatLogComponent(
                        logs = bState.structuredLogs,
                        maxHeight = 110
                    )
                }
            }
        }

        // 7. Interactive Action Wheel & Move Commands
        if (bState.isVictory == null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SectDarkSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "🥋 EXECUTED TECHNIQUE (PILIH JURUS):",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = GoldCelestial,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(Modifier.height(8.dp))

                        // Row 1: Strikes & Suplex Grapples
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.executeArenaAction(CultivationArenaEngine.ArenaActionType.MARTIAL_STRIKE) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("action_strike"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF263238)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp)
                            ) {
                                Text("👊 Pukulan Kombo", style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
                            }

                            Button(
                                onClick = { viewModel.executeArenaAction(CultivationArenaEngine.ArenaActionType.HEAVENLY_GRAPPLE) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("action_grapple"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp)
                            ) {
                                Text("🤼 Suplex Slam", style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        // Row 2: Submissions & Rope Rebounds
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.executeArenaAction(CultivationArenaEngine.ArenaActionType.BAGUA_SUBMISSION) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("action_submission"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A5F)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp)
                            ) {
                                Text("🥋 Kuncian Dao", style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
                            }

                            Button(
                                onClick = { viewModel.executeArenaAction(CultivationArenaEngine.ArenaActionType.ROPE_REBOUND) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("action_rope_rebound"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A148C)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp)
                            ) {
                                Text("⚡ Pantulan Tali", style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        // Row 3: Taunt Audience (+Hype)
                        Button(
                            onClick = { viewModel.executeArenaAction(CultivationArenaEngine.ArenaActionType.TAUNT_CROWD) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .testTag("action_taunt"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D4037)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("🌟 Panaskan Penonton (+35% Hype)", style = MaterialTheme.typography.labelSmall.copy(color = GoldCelestial, fontWeight = FontWeight.Bold))
                        }

                        // Row 4: CELESTIAL FINISHER (TEKNIK PAMUNGKAS DEWA)
                        if (bState.crowdHype >= 100) {
                            Spacer(Modifier.height(10.dp))
                            Button(
                                onClick = { viewModel.executeArenaAction(CultivationArenaEngine.ArenaActionType.CELESTIAL_FINISHER) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("action_finisher"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GoldCelestial
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    "👑 ${discipleProf?.finisherMove?.uppercase() ?: "CELESTIAL FINISHER"}",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = Color.Black,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // 8. Victory & Championship Presentation Card
        if (bState.isVictory != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (bState.isVictory) Color(0xFF00381B) else Color(0xFF380000)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (bState.isVictory) "🏆 TOURNAMENT TRIUMPH & BELT AWARDED!" else "MATCH CONCLUDED (PINFALL / SUBMISSION)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = if (bState.isVictory) GoldCelestial else CinnabarRed,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        )
                        Spacer(Modifier.height(8.dp))
                        bState.rewards.forEach { r ->
                            Text(r, style = MaterialTheme.typography.bodyMedium.copy(color = CultivationTextPrimary))
                        }
                        Spacer(Modifier.height(14.dp))
                        Button(
                            onClick = onExit,
                            colors = ButtonDefaults.buttonColors(containerColor = JadePrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("exit_battle_button")
                        ) {
                            Text("Return to Sect Pavilion", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// TALE OF THE TAPE PRE-MATCH COMPARISON DIALOG
// ============================================================================

@Composable
private fun TaleOfTheTapeDialog(
    discipleProfile: CultivationArenaEngine.FighterProfile?,
    opponentProfile: CultivationArenaEngine.FighterProfile?,
    stipulation: CultivationArenaEngine.MatchStipulation,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141424))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header
                Text(
                    text = "🥋 TALE OF THE TAPE 🥋",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = GoldCelestial,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = stipulation.displayName,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = CultivationTextSecondary,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(14.dp))

                // Stats Comparison Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left Fighter (Disciple)
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                        Text(discipleProfile?.name ?: "Disciple", style = MaterialTheme.typography.bodyMedium.copy(color = JadePrimary, fontWeight = FontWeight.Bold))
                        Text("\"${discipleProfile?.nickname ?: ""}\"", style = MaterialTheme.typography.labelSmall.copy(color = GoldCelestial))
                        Spacer(Modifier.height(6.dp))
                        Text("Record: ${discipleProfile?.wins ?: 0}W - ${discipleProfile?.losses ?: 0}L (${discipleProfile?.knockouts ?: 0} KO)", style = MaterialTheme.typography.labelSmall.copy(color = CultivationTextPrimary))
                        Text("Style: ${discipleProfile?.archetype?.title ?: ""}", style = MaterialTheme.typography.labelSmall.copy(color = QiGlow))
                        Spacer(Modifier.height(6.dp))
                        Text("Signature:", style = MaterialTheme.typography.labelSmall.copy(color = CultivationTextSecondary))
                        Text(discipleProfile?.signatureMove ?: "", style = MaterialTheme.typography.labelSmall.copy(color = CultivationTextPrimary))
                        Spacer(Modifier.height(6.dp))
                        Text("Finisher:", style = MaterialTheme.typography.labelSmall.copy(color = CultivationTextSecondary))
                        Text(discipleProfile?.finisherMove ?: "", style = MaterialTheme.typography.labelSmall.copy(color = GoldCelestial, fontWeight = FontWeight.Bold))
                    }

                    Spacer(Modifier.width(12.dp))

                    // Right Fighter (Opponent)
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        Text(opponentProfile?.name ?: "Challenger", style = MaterialTheme.typography.bodyMedium.copy(color = CinnabarRed, fontWeight = FontWeight.Bold), textAlign = TextAlign.End)
                        Text("\"${opponentProfile?.nickname ?: ""}\"", style = MaterialTheme.typography.labelSmall.copy(color = GoldCelestial), textAlign = TextAlign.End)
                        Spacer(Modifier.height(6.dp))
                        Text("Record: ${opponentProfile?.wins ?: 0}W - ${opponentProfile?.losses ?: 0}L (${opponentProfile?.knockouts ?: 0} KO)", style = MaterialTheme.typography.labelSmall.copy(color = CultivationTextPrimary), textAlign = TextAlign.End)
                        Text("Style: ${opponentProfile?.archetype?.title ?: ""}", style = MaterialTheme.typography.labelSmall.copy(color = QiGlow), textAlign = TextAlign.End)
                        Spacer(Modifier.height(6.dp))
                        Text("Signature:", style = MaterialTheme.typography.labelSmall.copy(color = CultivationTextSecondary), textAlign = TextAlign.End)
                        Text(opponentProfile?.signatureMove ?: "", style = MaterialTheme.typography.labelSmall.copy(color = CultivationTextPrimary), textAlign = TextAlign.End)
                        Spacer(Modifier.height(6.dp))
                        Text("Finisher:", style = MaterialTheme.typography.labelSmall.copy(color = CultivationTextSecondary), textAlign = TextAlign.End)
                        Text(opponentProfile?.finisherMove ?: "", style = MaterialTheme.typography.labelSmall.copy(color = CinnabarRed, fontWeight = FontWeight.Bold), textAlign = TextAlign.End)
                    }
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = JadePrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Resume Match Broadcast", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
