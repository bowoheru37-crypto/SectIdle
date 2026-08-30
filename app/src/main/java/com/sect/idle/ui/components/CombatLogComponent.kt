package com.sect.idle.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

data class CombatLogEntry(
    val id: Long,
    val text: String,
    val category: LogCategory,
    val timestampFormatted: String = ""
)

enum class LogCategory(val tag: String, val tagColor: Color, val bgTint: Color) {
    SWORD_TECHNIQUE("🗡️ SWORD", JadePrimary, Color(0xFF132F20)),
    SUPLEX_GRAPPLE("🥋 SUPLEX", Color(0xFFFF9800), Color(0xFF2C1E0A)),
    MERIDIAN_SUBMISSION("🔒 LOCK", Color(0xFF80D8FF), Color(0xFF0D2538)),
    CELESTIAL_FINISHER("👑 FINISHER", GoldCelestial, Color(0xFF332B0D)),
    ROPE_REBOUND("⚡ REBOUND", Color(0xFFEA80FC), Color(0xFF281033)),
    REFEREE_COUNT("⚖️ REFEREE", Color(0xFFFFD54F), Color(0xFF242211)),
    DAMAGE_TAKEN("💥 IMPACT", CinnabarRed, Color(0xFF2D1111)),
    GENERAL_COMMENTARY("📜 CHRONICLE", CultivationTextSecondary, Color(0xFF151928))
}

@Composable
fun CombatLogComponent(
    logs: List<CombatLogEntry>,
    modifier: Modifier = Modifier,
    maxHeight: Int = 140
) {
    val listState = rememberLazyListState()

    // Auto-scroll to latest log entry
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF2B334B), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0F1A))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.ListAlt,
                        contentDescription = null,
                        tint = QiGlow,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "📜 HEAVENLY COMBAT CHRONICLE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = QiGlow,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    )
                }
                Text(
                    text = "${logs.size} Events",
                    style = MaterialTheme.typography.labelSmall.copy(color = CultivationTextMuted)
                )
            }

            Spacer(Modifier.height(6.dp))

            // Scrolling Event Stream
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp, max = maxHeight.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                itemsIndexed(logs, key = { _, item -> item.id }) { _, entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(entry.category.bgTint)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Action Category Tag Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(entry.category.tagColor.copy(alpha = 0.2f))
                                .border(0.5.dp, entry.category.tagColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = entry.category.tag,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = entry.category.tagColor,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                        }

                        Spacer(Modifier.width(8.dp))

                        // Log Description Text
                        Text(
                            text = entry.text,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = CultivationTextPrimary,
                                fontSize = 11.5.sp,
                                lineHeight = 15.sp
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
