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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding

@Composable
fun GrandmasterAiScreen(
    viewModel: GameViewModel,
    state: SectUiState,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val presetPrompts = listOf(
        "Grandmaster, how should my disciples prepare for the Heavenly Tribulation?",
        "Ancestor, what is the best strategy to develop our Sect's spiritual roots?",
        "Please compose a mystical cultivation scripture for my disciples.",
        "How can I balance Yin and Yang in the Alchemy furnace?"
    )

    LaunchedEffect(state.aiMessages.size) {
        if (state.aiMessages.isNotEmpty()) {
            listState.animateScrollToItem(state.aiMessages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SectDarkBackground)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .imePadding()
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(GoldCelestial.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = GoldCelestial,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = "Grandmaster Ancestor Pavilion (Gemini AI)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = GoldCelestial,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "Seek immortal guidance, Daoist scriptures, and divination.",
                    style = MaterialTheme.typography.bodySmall.copy(color = CultivationTextSecondary)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Preset Quick Prompts & AI Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.requestGrandmasterProphecy() },
                colors = ButtonDefaults.buttonColors(containerColor = GoldCelestial),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).height(38.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "🔮 Prophecy",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color.Black, fontWeight = FontWeight.Bold)
                )
            }

            Button(
                onClick = { viewModel.generateDynamicSectQuest(1) },
                colors = ButtonDefaults.buttonColors(containerColor = JadePrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).height(38.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "📜 AI Quest",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color.Black, fontWeight = FontWeight.Bold)
                )
            }

            Button(
                onClick = { viewModel.triggerRandomHeavenlyEvent() },
                colors = ButtonDefaults.buttonColors(containerColor = SpiritBlue),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).height(38.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "⚡ Event",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // Preset Quick Prompts
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(presetPrompts) { prompt ->
                OutlinedButton(
                    onClick = {
                        viewModel.askGrandmaster(prompt)
                    },
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = prompt.take(28) + "...",
                        style = MaterialTheme.typography.labelSmall.copy(color = QiGlow)
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // Dialogue Log and Active AI Cards
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            // Latest Ancestral Prophecy Card if available
            state.latestWisdom?.let { wisdom ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF221F10)),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldCelestial.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📜 ${wisdom.title}",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = GoldCelestial,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = wisdom.sectBlessing,
                                    style = MaterialTheme.typography.labelSmall.copy(color = QiGlow)
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "“${wisdom.fortuneProphecy}”",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFFFFE082),
                                    lineHeight = 20.sp
                                )
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Ancestor's Guidance: ${wisdom.text}",
                                style = MaterialTheme.typography.bodySmall.copy(color = CultivationTextSecondary)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Suggested Practice: ${wisdom.recommendedAction}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = JadePrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            // Active Dynamic Quest Card if available
            state.activeDynamicQuest?.let { quest ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF13231B)),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, JadePrimary.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🌟 ${quest.title}",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = JadePrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "★".repeat(quest.difficulty),
                                    style = MaterialTheme.typography.labelSmall.copy(color = GoldCelestial)
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = quest.description,
                                style = MaterialTheme.typography.bodySmall.copy(color = CultivationTextPrimary)
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Target: ${quest.targetName}",
                                style = MaterialTheme.typography.labelSmall.copy(color = QiGlow)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Rewards: +${quest.rewardQi} Qi, +${quest.rewardStones} Stones, +${quest.rewardKarma} Karma",
                                style = MaterialTheme.typography.labelSmall.copy(color = GoldCelestial)
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.claimDynamicQuestReward() },
                                colors = ButtonDefaults.buttonColors(containerColor = JadePrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().height(36.dp)
                            ) {
                                Text(
                                    "Claim Divine Reward",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }

            if (state.aiMessages.isEmpty() && state.latestWisdom == null && state.activeDynamicQuest == null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SectDarkSurface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "🧘 'Welcome, Sect Master. I am the founding ancestor spirit residing in the Heavenly Mirror. Ask of me any question regarding your disciples, cultivation realms, pills, or the mysteries of the Dao.'",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = CultivationTextPrimary,
                                lineHeight = 20.sp
                            ),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            items(state.aiMessages) { msg ->
                val isUser = msg.sender == "Sect Master"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(0.85f),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUser) Color(0xFF1E3A5F) else SectDarkSurface
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = msg.sender,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isUser) SpiritBlue else GoldCelestial,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = msg.text,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = CultivationTextPrimary,
                                    lineHeight = 20.sp
                                )
                            )
                        }
                    }
                }
            }

            if (state.isAiThinking) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        CircularProgressIndicator(
                            color = GoldCelestial,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Ancestor is contemplating the heavenly secrets...",
                            style = MaterialTheme.typography.bodySmall.copy(color = CultivationTextMuted)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Input Field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = {
                    Text(
                        "Consult Grandmaster Ancestor...",
                        style = MaterialTheme.typography.bodySmall.copy(color = CultivationTextMuted)
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_grandmaster_input"),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldCelestial,
                    unfocusedBorderColor = Color(0xFF33334A),
                    focusedContainerColor = SectDarkSurface,
                    unfocusedContainerColor = SectDarkSurface,
                    focusedTextColor = CultivationTextPrimary,
                    unfocusedTextColor = CultivationTextPrimary
                ),
                maxLines = 3
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.askGrandmaster(inputText.trim())
                        inputText = ""
                    }
                },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(GoldCelestial)
                    .testTag("ai_grandmaster_send_button")
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Send",
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
