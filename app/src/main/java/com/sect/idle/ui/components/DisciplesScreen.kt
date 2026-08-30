package com.sect.idle.ui.components

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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.ui.theme.SectDarkSurfaceCard
import com.example.ui.theme.SpiritBlue
import com.example.ui.theme.TribulationLightning
import com.sect.idle.core.GameConfig
import com.sect.idle.models.Disciple
import com.sect.idle.models.Item
import com.sect.idle.ui.GameViewModel
import com.sect.idle.ui.SectUiState

@Composable
fun DisciplesScreen(
    viewModel: GameViewModel,
    state: SectUiState,
    modifier: Modifier = Modifier
) {
    var discipleToDismiss by remember { mutableStateOf<Disciple?>(null) }

    CriticalActionDialog(
        visible = discipleToDismiss != null,
        title = "Sever Cultivation Ties",
        subtitle = "Permanent Disciple Dismissal",
        description = "Are you sure you want to dismiss this disciple from the sect? All accumulated Qi, martial stats, and current tasks will be permanently removed from the mountain.",
        actionType = CriticalActionType.DISMISS_DISCIPLE,
        targetName = discipleToDismiss?.name,
        targetDetail = discipleToDismiss?.let { "${GameConfig.getRealmName(it.realm)} · ${GameConfig.getElementName(it.element)} Root" },
        confirmButtonText = "Dismiss Disciple",
        onConfirm = {
            discipleToDismiss?.let {
                viewModel.dismissDisciple(it.id)
            }
        },
        onDismiss = { discipleToDismiss = null }
    )
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SectDarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // 1. Recruitment Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SectDarkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = JadePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Mountain Gate Candidates (${state.disciples.size}/${state.buildings.find { it.type == 0 }?.level?.times(5) ?: 10})",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = CultivationTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        IconButton(onClick = { viewModel.refreshCandidates() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = QiGlow)
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(state.candidateDisciples) { candidate ->
                            CandidateCard(
                                candidate = candidate,
                                onRecruit = { viewModel.recruitDisciple(candidate) }
                            )
                        }
                    }
                }
            }
        }

        // 2. Active Disciples Header & Batch Controls
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sect Disciples (${state.disciples.size})",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = GoldCelestial,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Batch action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.assignAllTasks(0) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.weight(1f).height(32.dp)
                    ) {
                        Text("All Cultivate", style = MaterialTheme.typography.labelSmall.copy(color = QiGlow))
                    }
                    OutlinedButton(
                        onClick = { viewModel.assignAllTasks(1) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.weight(1f).height(32.dp)
                    ) {
                        Text("All Gather", style = MaterialTheme.typography.labelSmall.copy(color = JadePrimary))
                    }
                    OutlinedButton(
                        onClick = { viewModel.assignAllTasks(4) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.weight(1f).height(32.dp)
                    ) {
                        Text("All Hunt", style = MaterialTheme.typography.labelSmall.copy(color = GoldCelestial))
                    }
                }
            }
        }

        // 3. Disciples List
        items(state.disciples) { disciple ->
            DiscipleCard(
                disciple = disciple,
                onBreakthrough = { viewModel.attemptBreakthrough(disciple.id) },
                onAssignTask = { taskIdx -> viewModel.assignTask(disciple.id, taskIdx) },
                availablePills = state.inventory.filter { it.getType() == Item.TYPE_CONSUMABLE },
                onBestowPill = { itemId -> viewModel.bestowPill(disciple.id, itemId) },
                onInteract = { viewModel.interactWithDisciple(disciple.id) },
                onDismiss = { discipleToDismiss = disciple }
            )
        }
    }
}

@Composable
private fun CandidateCard(
    candidate: Disciple,
    onRecruit: () -> Unit
) {
    val elemColor = Color(GameConfig.getElementColor(candidate.element))
    Card(
        colors = CardDefaults.cardColors(containerColor = SectDarkSurfaceCard),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .width(180.dp)
            .border(1.dp, elemColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(elemColor)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = candidate.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = CultivationTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${GameConfig.getElementName(candidate.element)} Root",
                style = MaterialTheme.typography.bodySmall.copy(color = elemColor)
            )
            Text(
                text = "Talent Grade: ${candidate.talentGrade}/10",
                style = MaterialTheme.typography.bodySmall.copy(color = CultivationTextSecondary)
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onRecruit,
                colors = ButtonDefaults.buttonColors(containerColor = JadePrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .testTag("recruit_button_${candidate.name}"),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    "Recruit (50 Stones)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
private fun DiscipleCard(
    disciple: Disciple,
    onBreakthrough: () -> Unit,
    onAssignTask: (Int) -> Unit,
    availablePills: List<Item>,
    onBestowPill: (String) -> Unit,
    onInteract: () -> Unit,
    onDismiss: () -> Unit
) {
    var taskMenuExpanded by remember { mutableStateOf(false) }
    var pillMenuExpanded by remember { mutableStateOf(false) }

    val elemColor = Color(GameConfig.getElementColor(disciple.element))
    val realmName = GameConfig.getRealmName(disciple.realm)
    val expNeeded = (disciple.realm + 1) * 300
    val progress = (disciple.realmExp.toFloat() / expNeeded.coerceAtLeast(1)).coerceIn(0f, 1f)

    val taskNames = listOf(
        "🧘 Cultivate Qi",
        "🌿 Gather Herbs",
        "🔥 Refine Pills",
        "🛡️ Guard Gate",
        "⚔️ Beast Hunt",
        "📜 Study Dao"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SectDarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onInteract)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(elemColor.copy(alpha = 0.2f))
                            .border(1.5.dp, elemColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = disciple.name.take(1),
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = elemColor,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = disciple.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = CultivationTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = realmName,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = GoldCelestial,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Text(
                                text = " · ${GameConfig.getElementName(disciple.element)}",
                                style = MaterialTheme.typography.bodySmall.copy(color = elemColor)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Task Dropdown Chip
                    Box {
                        OutlinedButton(
                            onClick = { taskMenuExpanded = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(
                                text = taskNames.getOrElse(disciple.currentTask) { "Cultivate" },
                                style = MaterialTheme.typography.labelSmall.copy(color = QiGlow)
                            )
                        }
                        DropdownMenu(
                            expanded = taskMenuExpanded,
                            onDismissRequest = { taskMenuExpanded = false },
                            modifier = Modifier.background(SectDarkSurfaceCard)
                        ) {
                            taskNames.forEachIndexed { index, taskStr ->
                                DropdownMenuItem(
                                    text = { Text(taskStr, color = CultivationTextPrimary) },
                                    onClick = {
                                        onAssignTask(index)
                                        taskMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.width(4.dp))

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("dismiss_disciple_button_${disciple.name}")
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Dismiss Disciple",
                            tint = CinnabarRed.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Cultivation Qi Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Cultivation Qi: ${disciple.realmExp} / $expNeeded",
                    style = MaterialTheme.typography.bodySmall.copy(color = CultivationTextSecondary)
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall.copy(color = JadePrimary, fontWeight = FontWeight.Bold)
                )
            }
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = JadePrimary,
                trackColor = SectDarkSurfaceCard
            )

            Spacer(Modifier.height(10.dp))

            // Attributes Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("HP: ${disciple.hp}/${disciple.maxHp}", style = MaterialTheme.typography.bodySmall.copy(color = CultivationTextMuted))
                Text("ATK: ${disciple.atk}", style = MaterialTheme.typography.bodySmall.copy(color = CultivationTextMuted))
                Text("DEF: ${disciple.def}", style = MaterialTheme.typography.bodySmall.copy(color = CultivationTextMuted))
                Text("Talent: ${disciple.talentGrade}/10", style = MaterialTheme.typography.bodySmall.copy(color = CultivationTextMuted))
            }

            Spacer(Modifier.height(12.dp))

            // Action Buttons: Pill Bestow & Breakthrough
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Bestow Pill Button
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { pillMenuExpanded = true },
                        modifier = Modifier.fillMaxWidth().height(36.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.Healing, contentDescription = null, tint = SpiritBlue, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Bestow Pill", style = MaterialTheme.typography.labelSmall.copy(color = SpiritBlue))
                    }
                    DropdownMenu(
                        expanded = pillMenuExpanded,
                        onDismissRequest = { pillMenuExpanded = false },
                        modifier = Modifier.background(SectDarkSurfaceCard)
                    ) {
                        if (availablePills.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No pills in inventory", color = CultivationTextMuted) },
                                onClick = { pillMenuExpanded = false }
                            )
                        } else {
                            availablePills.forEach { pill ->
                                DropdownMenuItem(
                                    text = { Text("💊 ${pill.getName()}", color = CultivationTextPrimary) },
                                    onClick = {
                                        onBestowPill(pill.id)
                                        pillMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Breakthrough Tribulation Button
                val canBreakthrough = disciple.realmExp >= expNeeded
                Button(
                    onClick = onBreakthrough,
                    enabled = canBreakthrough,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TribulationLightning,
                        disabledContainerColor = SectDarkSurfaceCard
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("breakthrough_button_${disciple.name}"),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        Icons.Default.Bolt,
                        contentDescription = null,
                        tint = if (canBreakthrough) Color.Black else CultivationTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Breakthrough",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (canBreakthrough) Color.Black else CultivationTextMuted,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}
