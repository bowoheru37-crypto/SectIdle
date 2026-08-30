package com.sect.idle.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.example.ui.theme.SectDarkSurfaceCard
import com.example.ui.theme.SpiritBlue
import com.example.ui.theme.TribulationLightning
import com.sect.idle.core.GameConfig
import com.sect.idle.models.Disciple
import com.sect.idle.ui.GameViewModel
import com.sect.idle.ui.SectUiState
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CultivationScreen(
    viewModel: GameViewModel,
    state: SectUiState,
    modifier: Modifier = Modifier
) {
    var selectedSection by remember { mutableIntStateOf(0) }
    val sectionTitles = listOf(
        "🧘 Qi Meditation",
        "⚡ Tribulation Altar",
        "☯️ Breath Resonance",
        "📜 Dao Scriptures",
        "🔥 Cauldron Refinement"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SectDarkBackground)
    ) {
        // Top Sub-Navigation Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedSection,
            containerColor = SectDarkSurface,
            contentColor = GoldCelestial,
            edgePadding = 12.dp,
            indicator = { tabPositions ->
                if (selectedSection < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedSection]),
                        color = GoldCelestial,
                        height = 3.dp
                    )
                }
            }
        ) {
            sectionTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedSection == index,
                    onClick = { selectedSection = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selectedSection == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedSection == index) GoldCelestial else CultivationTextSecondary
                            )
                        )
                    },
                    modifier = Modifier.testTag("cultivation_subtab_$index")
                )
            }
        }

        // Section Content
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            when (selectedSection) {
                0 -> MeditationChambersSection(viewModel = viewModel, state = state)
                1 -> TribulationAltarSection(viewModel = viewModel, state = state)
                2 -> BreathResonanceSection(viewModel = viewModel, state = state)
                3 -> DaoScripturesSection(viewModel = viewModel, state = state)
                4 -> AlchemyCauldronSection(viewModel = viewModel, state = state)
            }
        }
    }
}

// ======================================================================
// 1. SECLUDED MEDITATION CHAMBERS SECTION
// ======================================================================
@Composable
private fun MeditationChambersSection(
    viewModel: GameViewModel,
    state: SectUiState
) {
    val chambers = listOf(
        Triple("Azure Wood Grove", 1, Color(0xFF4CAF50)),
        Triple("Vermilion Fire Peak", 3, Color(0xFFFF5722)),
        Triple("Yellow Earth Cavern", 4, Color(0xFFFFB300)),
        Triple("White Metal Chamber", 0, Color(0xFFCFD8DC)),
        Triple("Black Water Spring", 2, Color(0xFF29B6F6))
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SectDarkSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Five Elements Secluded Chambers",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = GoldCelestial,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Disciples meditating in matching elemental chambers gain a +50% Qi absorption rate and accelerated comprehension.",
                        style = MaterialTheme.typography.bodySmall.copy(color = CultivationTextSecondary)
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.assignAllTasks(0) },
                            colors = ButtonDefaults.buttonColors(containerColor = JadePrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(36.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text("Assign All to Meditate", style = MaterialTheme.typography.labelSmall.copy(color = Color.Black, fontWeight = FontWeight.Bold))
                        }
                        Button(
                            onClick = { viewModel.collectAllQi() },
                            colors = ButtonDefaults.buttonColors(containerColor = QiGlow),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(36.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text("Harvest Spiritual Veins", style = MaterialTheme.typography.labelSmall.copy(color = Color.Black, fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }

        items(chambers) { (chamberName, element, elemColor) ->
            val disciplesInChamber = state.disciples.filter { it.element == element }

            Card(
                colors = CardDefaults.cardColors(containerColor = SectDarkSurfaceCard),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                border = androidx.compose.foundation.BorderStroke(1.dp, elemColor.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(elemColor)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = chamberName,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = elemColor,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Text(
                            text = "${disciplesInChamber.size} Disciples Resonating",
                            style = MaterialTheme.typography.labelSmall.copy(color = CultivationTextSecondary)
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    if (disciplesInChamber.isEmpty()) {
                        Text(
                            text = "No disciples with ${GameConfig.getElementName(element)} affinity meditating currently.",
                            style = MaterialTheme.typography.bodySmall.copy(color = CultivationTextMuted)
                        )
                    } else {
                        disciplesInChamber.forEach { d ->
                            val reqExp = (d.realm + 1) * 300
                            val expProgress = (d.realmExp.toFloat() / reqExp.coerceAtLeast(1)).coerceIn(0f, 1f)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${d.name} (${GameConfig.getRealmName(d.realm)})",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = CultivationTextPrimary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    LinearProgressIndicator(
                                        progress = { expProgress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(2.dp)),
                                        color = elemColor,
                                        trackColor = Color(0xFF1E2638)
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = "${(expProgress * 100).toInt()}% Qi",
                                    style = MaterialTheme.typography.labelSmall.copy(color = QiGlow)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ======================================================================
// 2. HEAVENLY TRIBULATION ALTAR SECTION
// ======================================================================
@Composable
private fun TribulationAltarSection(
    viewModel: GameViewModel,
    state: SectUiState
) {
    val readyDisciples = state.disciples.filter { d ->
        val reqExp = (d.realm + 1) * 300
        d.realmExp >= reqExp
    }

    var selectedDiscipleId by remember { mutableStateOf(state.disciples.firstOrNull()?.id ?: "") }
    val selectedDisciple = state.disciples.find { it.id == selectedDiscipleId } ?: state.disciples.firstOrNull()

    val infiniteTransition = rememberInfiniteTransition(label = "lightning_glow")
    val lightningAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lightning"
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1630)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TribulationLightning.copy(alpha = lightningAlpha), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(TribulationLightning.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            com.sect.idle.render.SpiritAuraEffect(
                                auraType = com.sect.idle.render.AuraType.TRIBULATION_LIGHTNING,
                                intensity = 0.9f
                            )
                            Icon(
                                Icons.Default.Bolt,
                                contentDescription = null,
                                tint = TribulationLightning,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Nine Heavens Tribulation Spire",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = TribulationLightning,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Ascending realms requires weathering celestial thunder. Higher realms face fierce tribulations with high risk of Qi deviation if unprepared.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = CultivationTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    )

                    Spacer(Modifier.height(14.dp))

                    // Candidate Selector
                    Text(
                        text = "Select Ascending Disciple:",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = CultivationTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(state.disciples) { d ->
                            val isSelected = d.id == selectedDiscipleId
                            val isReady = d.realmExp >= (d.realm + 1) * 300

                            Card(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedDiscipleId = d.id },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) TribulationLightning.copy(alpha = 0.25f) else Color(0xFF26263D)
                                ),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, TribulationLightning) else null
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                    Text(
                                        text = d.name,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = if (isSelected) GoldCelestial else CultivationTextPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = if (isReady) "⚡ Ready" else "${d.realmExp}/${(d.realm + 1) * 300}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isReady) TribulationLightning else CultivationTextMuted
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        selectedDisciple?.let { d ->
            item {
                val reqExp = (d.realm + 1) * 300
                val canBreakthrough = d.realmExp >= reqExp
                val successOdds = ((0.75f - d.realm * 0.04f).coerceIn(0.2f, 0.95f) * 100).toInt()
                val currentRealm = GameConfig.getRealmName(d.realm)
                val nextRealm = GameConfig.getRealmName(d.realm + 1)

                Card(
                    colors = CardDefaults.cardColors(containerColor = SectDarkSurface),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = d.name,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = GoldCelestial,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Current Realm: $currentRealm",
                                    style = MaterialTheme.typography.bodySmall.copy(color = CultivationTextSecondary)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(TribulationLightning.copy(alpha = 0.2f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Next: $nextRealm",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TribulationLightning,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Tribulation Stats Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Success Probability", style = MaterialTheme.typography.labelSmall.copy(color = CultivationTextMuted))
                                Text("$successOdds%", style = MaterialTheme.typography.titleMedium.copy(color = if (successOdds >= 60) JadePrimary else CinnabarRed, fontWeight = FontWeight.Bold))
                            }
                            Column {
                                Text("Exp Accumulated", style = MaterialTheme.typography.labelSmall.copy(color = CultivationTextMuted))
                                Text("${d.realmExp} / $reqExp", style = MaterialTheme.typography.titleMedium.copy(color = QiGlow, fontWeight = FontWeight.Bold))
                            }
                            Column {
                                Text("Thunder Strikes", style = MaterialTheme.typography.labelSmall.copy(color = CultivationTextMuted))
                                Text("${(d.realm + 1) * 3} Bolts", style = MaterialTheme.typography.titleMedium.copy(color = TribulationLightning, fontWeight = FontWeight.Bold))
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.attemptBreakthrough(d.id) },
                            enabled = canBreakthrough,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (canBreakthrough) TribulationLightning else Color.Gray,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(44.dp)
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (canBreakthrough) "Commence Heavenly Tribulation Crossing!" else "Cultivating Qi (${d.realmExp}/$reqExp)",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ======================================================================
// 3. DAOIST BREATH RESONANCE SECTION
// ======================================================================
@Composable
private fun BreathResonanceSection(
    viewModel: GameViewModel,
    state: SectUiState
) {
    var resonanceCount by remember { mutableIntStateOf(0) }
    var lastFeedback by remember { mutableStateOf("Align your breathing with the Celestial Dao pulse...") }

    val infiniteTransition = rememberInfiniteTransition(label = "breath_circle")
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val breathRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SectDarkSurface),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Daoist Qi Circulation Technique",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = GoldCelestial,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Tap the Taiji Meridian Core when the celestial ring expands to trigger Qi Resonance surges.",
                    style = MaterialTheme.typography.bodySmall.copy(color = CultivationTextSecondary)
                )
            }
        }

        // Interactive Yin-Yang Canvas
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(CircleShape)
                .clickable {
                    val isPeak = breathScale >= 1.05f && breathScale <= 1.25f
                    if (isPeak) {
                        resonanceCount++
                        viewModel.tapMountainQiGather()
                        viewModel.tapMountainQiGather()
                        lastFeedback = "✨ PERFECT RESONANCE! +${15 * state.sectLevel} Heavenly Qi absorbed!"
                    } else {
                        viewModel.tapMountainQiGather()
                        lastFeedback = "☯️ Circulated meridian veins (+${5 * state.sectLevel} Qi)."
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = size.minDimension / 2f
                val center = Offset(size.width / 2f, size.height / 2f)

                // Outer Glowing Ring
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(QiGlow.copy(alpha = 0.4f), Color.Transparent),
                        center = center,
                        radius = radius * breathScale
                    ),
                    radius = radius * breathScale,
                    center = center
                )

                // Pulsing Ring
                drawCircle(
                    color = GoldCelestial.copy(alpha = 0.7f),
                    radius = radius * 0.75f * breathScale,
                    center = center,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Inner Core
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(JadePrimary, Color(0xFF0F3B2E)),
                        center = center,
                        radius = radius * 0.45f
                    ),
                    radius = radius * 0.45f,
                    center = center
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "☯️",
                    style = MaterialTheme.typography.headlineLarge
                )
                Text(
                    text = "TAP CORE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        // Feedback & Stats Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SectDarkSurfaceCard),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = lastFeedback,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = GoldCelestial,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Text("Total Cycles: $resonanceCount", style = MaterialTheme.typography.labelSmall.copy(color = CultivationTextSecondary))
                    Text("Sect Qi: ${state.qi} / ${state.maxQi}", style = MaterialTheme.typography.labelSmall.copy(color = QiGlow))
                }
            }
        }
    }
}

// ======================================================================
// 4. DAO SCRIPTURES RESEARCH SECTION
// ======================================================================
data class ScriptureData(
    val id: String,
    val name: String,
    val element: String,
    val description: String,
    val bonusSummary: String,
    val costQi: Long,
    val costStones: Long
)

@Composable
private fun DaoScripturesSection(
    viewModel: GameViewModel,
    state: SectUiState
) {
    val scriptures = listOf(
        ScriptureData(
            id = "script_1",
            name = "Heavenly Yang Sun Sutra",
            element = "Fire",
            description = "Ancient scripture channeling pure solar essence through all disciples' Dantian.",
            bonusSummary = "+25% Fire Affinity Atk & Breakthrough odds",
            costQi = 400L,
            costStones = 150L
        ),
        ScriptureData(
            id = "script_2",
            name = "Nine Heavens Sword Formula",
            element = "Metal",
            description = "Peerless sword comprehension manuscript sharpening spiritual blades.",
            bonusSummary = "+20% Base ATK for all Disciples",
            costQi = 600L,
            costStones = 250L
        ),
        ScriptureData(
            id = "script_3",
            name = "Primordial Chaos Scripture",
            element = "Chaos",
            description = "Supreme manual elucidating the origin of yin and yang before heaven and earth.",
            bonusSummary = "+1,500 Max Qi Pool & +100 Max HP",
            costQi = 1000L,
            costStones = 400L
        ),
        ScriptureData(
            id = "script_4",
            name = "Hundred Herbs Spirit Compendium",
            element = "Wood",
            description = "Secret herbal guide detailing thousand-year spirit grasses and cauldrons.",
            bonusSummary = "+30% Alchemy Pill Crafting Efficiency",
            costQi = 500L,
            costStones = 200L
        )
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SectDarkSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Book, contentDescription = null, tint = GoldCelestial)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Grand Scripture Depository",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = GoldCelestial,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Comprehend ancient scrolls to bestow permanent Daoist passive enhancements across the entire sect.",
                        style = MaterialTheme.typography.bodySmall.copy(color = CultivationTextSecondary)
                    )
                }
            }
        }

        items(scriptures) { s ->
            val canAfford = state.qi >= s.costQi && state.spiritStones >= s.costStones

            Card(
                colors = CardDefaults.cardColors(containerColor = SectDarkSurfaceCard),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📜 ${s.name}",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = GoldCelestial,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(JadePrimary.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = s.element,
                                style = MaterialTheme.typography.labelSmall.copy(color = JadePrimary, fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = s.description,
                        style = MaterialTheme.typography.bodySmall.copy(color = CultivationTextPrimary)
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Buff: ${s.bonusSummary}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = QiGlow,
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cost: ${s.costQi} Qi · ${s.costStones} Stones",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (canAfford) CultivationTextSecondary else CinnabarRed
                            )
                        )

                        Button(
                            onClick = {
                                if (canAfford) {
                                    viewModel.collectAllQi()
                                }
                            },
                            enabled = canAfford,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (canAfford) JadePrimary else Color.Gray,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Comprehend", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }
    }
}

// ======================================================================
// 5. ALCHEMY CAULDRON REFINEMENT SECTION
// ======================================================================
@Composable
private fun AlchemyCauldronSection(
    viewModel: GameViewModel,
    state: SectUiState
) {
    val recipes = listOf(
        PillRecipe(0, "Qi Condensation Pill", "Cleanses the meridians. Grants +500 Cultivation Exp.", 150L, 50L, "Tier 1"),
        PillRecipe(1, "Foundation Breakthrough Pill", "Stabilizes spiritual sea. Grants +1,500 Cultivation Exp.", 350L, 120L, "Tier 2"),
        PillRecipe(2, "Nine Revolutions Golden Core Elixir", "Celestial elixir. Grants +4,000 Cultivation Exp.", 800L, 300L, "Tier 3"),
        PillRecipe(3, "Tribulation Shield Pill", "Shields soul against heavenly lightning.", 1200L, 500L, "Tier 4")
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF26181A)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = CinnabarRed)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Nine Dragons Alchemy Cauldron",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = CinnabarRed,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Channel spiritual flame and celestial herbs to concoct miraculous medicinal pills.",
                        style = MaterialTheme.typography.bodySmall.copy(color = CultivationTextSecondary)
                    )
                }
            }
        }

        items(recipes) { r ->
            val canConcoct = state.qi >= r.costQi && state.spiritStones >= r.costStones

            Card(
                colors = CardDefaults.cardColors(containerColor = SectDarkSurfaceCard),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "💊 ${r.name}",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = GoldCelestial,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = r.tier,
                            style = MaterialTheme.typography.labelSmall.copy(color = CinnabarRed, fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(Modifier.height(4.dp))
                    Text(text = r.description, style = MaterialTheme.typography.bodySmall.copy(color = CultivationTextSecondary))
                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${r.costQi} Qi · ${r.costStones} Stones",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (canConcoct) CultivationTextMuted else CinnabarRed
                            )
                        )

                        Button(
                            onClick = { viewModel.craftPill(r.type) },
                            enabled = canConcoct,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (canConcoct) CinnabarRed else Color.Gray,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Refine Pill", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }
    }
}
