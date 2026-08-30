package com.sect.idle.ui.components

import android.graphics.BitmapFactory
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CinnabarRed
import com.example.ui.theme.CultivationTextSecondary
import com.example.ui.theme.GoldCelestial
import com.example.ui.theme.JadePrimary
import com.example.ui.theme.QiGlow
import com.example.ui.theme.SectDarkBackground
import com.example.ui.theme.SectDarkSurface
import com.example.ui.theme.SpiritBlue
import com.sect.idle.core.GameConfig
import com.sect.idle.ui.GameViewModel
import com.sect.idle.ui.SectUiState
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SectCanvasView(
    viewModel: GameViewModel,
    state: SectUiState,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mountain_anim")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave"
    )

    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val particleDrift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "drift"
    )

    val context = LocalContext.current
    val parallaxBitmap = remember {
        try {
            BitmapFactory.decodeResource(context.resources, R.drawable.bg_parallax)?.asImageBitmap()
        } catch (e: Throwable) {
            null
        }
    }

    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SectDarkBackground)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("sect_mountain_canvas")
                .pointerInput(state.qiOrbs) {
                    detectTapGestures { tapOffset ->
                        val width = size.width.toFloat()
                        val height = size.height.toFloat()

                        // Normalized hit testing for all screen densities and aspect ratios
                        val clickedOrb = state.qiOrbs.find { orb ->
                            val ox = (orb.x / 400f) * width
                            val oy = (orb.y / 600f) * height
                            val dx = tapOffset.x - ox
                            val dy = tapOffset.y - oy
                            (dx * dx + dy * dy) <= (45f * 45f)
                        }

                        if (clickedOrb != null) {
                            viewModel.collectQiOrb(clickedOrb.id)
                        } else {
                            viewModel.tapMountainQiGather()
                        }
                    }
                }
        ) {
            val width = size.width
            val height = size.height

            // 1. Sky & Celestial Gradient
            val skyColors = when (state.season) {
                "Spring" -> listOf(Color(0xFF0A0A1F), Color(0xFF1B143A), Color(0xFF142B28))
                "Summer" -> listOf(Color(0xFF080D21), Color(0xFF1A1A40), Color(0xFF261D1A))
                "Autumn" -> listOf(Color(0xFF0F0B1E), Color(0xFF261A1E), Color(0xFF2E2214))
                else -> listOf(Color(0xFF070B18), Color(0xFF121E36), Color(0xFF18293D))
            }

            drawRect(
                brush = Brush.verticalGradient(
                    colors = skyColors
                ),
                size = size
            )

            // Draw generated Xianxia mountain parallax backdrop
            if (parallaxBitmap != null) {
                drawImage(
                    image = parallaxBitmap,
                    dstOffset = IntOffset(0, 0),
                    dstSize = IntSize(width.toInt(), (height * 0.52f).toInt()),
                    alpha = 0.72f
                )
            }

            // Celestial Moon & Star Radiance
            drawCircle(
                color = Color(0x22FFEA00),
                radius = 50f * pulseGlow,
                center = Offset(width * 0.82f, height * 0.12f)
            )
            drawCircle(
                color = Color(0xFFFFFDE7),
                radius = 32f,
                center = Offset(width * 0.82f, height * 0.12f)
            )

            // 2. Seasonal Floating Atmosphere Particles (Low memory impact)
            val seasonParticleColor = when (state.season) {
                "Spring" -> Color(0xFFFF80AB) // Cherry blossom petals
                "Summer" -> QiGlow.copy(alpha = 0.8f) // Firefly / Qi sparks
                "Autumn" -> Color(0xFFFFB74D) // Maple leaf embers
                else -> Color(0xFFE0F7FA) // Snow crystals
            }

            for (i in 0..12) {
                val px = ((i * 37f + particleDrift * width * 0.8f) % width)
                val py = ((i * 49f + particleDrift * height * 0.6f) % (height * 0.85f))
                val pRadius = if (i % 3 == 0) 3.5f else 2f
                drawCircle(
                    color = seasonParticleColor.copy(alpha = (0.3f + (i % 5) * 0.12f)),
                    radius = pRadius,
                    center = Offset(px, py)
                )
            }

            // 3. Distant Mist Mountain Peaks
            drawMountainLayer(
                width = width,
                height = height,
                baseY = height * 0.45f,
                peakHeight = 120f,
                color = Color(0xFF191438),
                wave = waveOffset * 0.5f
            )

            drawMountainLayer(
                width = width,
                height = height,
                baseY = height * 0.60f,
                peakHeight = 160f,
                color = Color(0xFF16253B),
                wave = waveOffset * 0.8f
            )

            // 4. Foreground Sect Mountain
            val mainPeakPath = Path().apply {
                moveTo(0f, height * 0.95f)
                lineTo(width * 0.2f, height * 0.55f)
                lineTo(width * 0.5f, height * 0.35f)
                lineTo(width * 0.8f, height * 0.52f)
                lineTo(width, height * 0.95f)
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }
            drawPath(
                path = mainPeakPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1B3830),
                        Color(0xFF112520),
                        Color(0xFF0C1916)
                    ),
                    startY = height * 0.35f,
                    endY = height
                )
            )

            // 5. Sect Pavilions & Buildings
            state.buildings.forEach { b ->
                val bx = (b.posX / 400f) * width
                val by = (b.posY / 600f) * height
                drawPavilion(
                    center = Offset(bx, by),
                    name = b.name,
                    level = b.level,
                    type = b.type,
                    pulse = pulseGlow,
                    textMeasurer = textMeasurer
                )
            }

            // 6. Cultivating Disciples roaming with autonomous AI behavior & emotes
            val agents = com.sect.idle.ai.CultivationAiEngine.getAllAgents()
            state.disciples.forEach { d ->
                val agent = agents[d.id]
                val dx = (d.position.x / 400f) * width
                val dy = (d.position.y / 600f) * height

                drawDiscipleAvatar(
                    center = Offset(dx, dy),
                    discipleName = d.name,
                    realm = d.realm,
                    element = d.element,
                    pulse = pulseGlow,
                    emote = agent?.emoteIcon ?: "🧘",
                    thought = agent?.currentThought ?: "",
                    isSparring = agent?.state == com.sect.idle.ai.CultivationAiEngine.NpcActionState.SPARRING,
                    textMeasurer = textMeasurer
                )
            }

            // 7. Floating Qi Orbs
            state.qiOrbs.forEach { orb ->
                val ox = (orb.x / 400f) * width
                val oy = (orb.y / 600f) * height
                drawQiOrb(
                    center = Offset(ox, oy),
                    amount = orb.amount,
                    color = Color(orb.color),
                    pulse = pulseGlow,
                    textMeasurer = textMeasurer
                )
            }
        }

        // Top Floating AI Quick Controls
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.requestGrandmasterProphecy() },
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldCelestial.copy(alpha = 0.95f)),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "🔮 Prophecy",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color.Black, fontWeight = FontWeight.Bold)
                )
            }

            Button(
                onClick = { viewModel.triggerRandomHeavenlyEvent() },
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SpiritBlue.copy(alpha = 0.95f)),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "⚡ Divine Event",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
                )
            }

            Button(
                onClick = { viewModel.generateDynamicSectQuest(1) },
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = JadePrimary.copy(alpha = 0.95f)),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "📜 AI Quest",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color.Black, fontWeight = FontWeight.Bold)
                )
            }
        }

        // Floating Instructions overlay
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = SectDarkSurface.copy(alpha = 0.92f)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "✨ Mountain Qi Gathering (${state.season})",
                        style = MaterialTheme.typography.labelMedium.copy(color = GoldCelestial, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Tap spirit orbs or anywhere on canvas to absorb ambient heavenly essence.",
                        style = MaterialTheme.typography.bodySmall.copy(color = CultivationTextSecondary)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = { viewModel.collectAllQi() },
                    colors = ButtonDefaults.buttonColors(containerColor = QiGlow),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("Collect All", style = MaterialTheme.typography.labelSmall.copy(color = Color.Black, fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

private fun DrawScope.drawMountainLayer(
    width: Float,
    height: Float,
    baseY: Float,
    peakHeight: Float,
    color: Color,
    wave: Float
) {
    val path = Path().apply {
        moveTo(0f, height)
        lineTo(0f, baseY)
        val step = width / 4f
        for (i in 0..4) {
            val x = i * step
            val yOffset = sin((wave + i * 90f) * Math.PI.toFloat() / 180f) * 15f
            val y = if (i % 2 == 1) baseY - peakHeight + yOffset else baseY + yOffset
            lineTo(x, y)
        }
        lineTo(width, height)
        close()
    }
    drawPath(path = path, color = color)
}

private fun DrawScope.drawPavilion(
    center: Offset,
    name: String,
    level: Int,
    type: Int,
    pulse: Float,
    textMeasurer: androidx.compose.ui.text.TextMeasurer
) {
    val roofColor = when (type) {
        0 -> GoldCelestial // Main Hall (Golden Imperial)
        1 -> SpiritBlue   // Spirit Gathering Array (Celestial Azure)
        2 -> CinnabarRed  // Alchemy Chamber (Cinnabar Orange-Red)
        3 -> JadePrimary  // Herb Garden (Emerald Jade)
        else -> Color(0xFFAB47BC) // Scripture Pagoda
    }

    // 1. Isometric Stone Foundation Platform
    val stoneBase = Path().apply {
        moveTo(center.x, center.y + 20f)
        lineTo(center.x + 28f, center.y + 8f)
        lineTo(center.x, center.y - 4f)
        lineTo(center.x - 28f, center.y + 8f)
        close()
    }
    drawPath(path = stoneBase, color = Color(0xFF383B46))

    // 2. Wooden Pillars / Chamber Walls
    drawRoundRect(
        color = Color(0xFF231E20),
        topLeft = Offset(center.x - 18f, center.y - 14f),
        size = Size(36f, 24f),
        cornerRadius = CornerRadius(3f, 3f)
    )
    // Red lacquered pillar accents
    drawLine(color = CinnabarRed.copy(alpha = 0.8f), start = Offset(center.x - 14f, center.y - 12f), end = Offset(center.x - 14f, center.y + 8f), strokeWidth = 2.5f)
    drawLine(color = CinnabarRed.copy(alpha = 0.8f), start = Offset(center.x + 14f, center.y - 12f), end = Offset(center.x + 14f, center.y + 8f), strokeWidth = 2.5f)

    // 3. Ornate Curved Chinese Eaves Roof
    val roofPath = Path().apply {
        moveTo(center.x - 34f, center.y - 10f)
        quadraticTo(center.x - 16f, center.y - 18f, center.x, center.y - 32f)
        quadraticTo(center.x + 16f, center.y - 18f, center.x + 34f, center.y - 10f)
        lineTo(center.x + 26f, center.y - 8f)
        lineTo(center.x, center.y - 22f)
        lineTo(center.x - 26f, center.y - 8f)
        close()
    }
    drawPath(path = roofPath, color = roofColor)

    // Golden Ridge Finial / Spire
    drawLine(
        color = GoldCelestial,
        start = Offset(center.x, center.y - 32f),
        end = Offset(center.x, center.y - 40f),
        strokeWidth = 2f
    )
    drawCircle(
        color = GoldCelestial,
        radius = 3f,
        center = Offset(center.x, center.y - 40f)
    )

    // Ambient Daoist Formation Glow
    drawCircle(
        color = roofColor.copy(alpha = 0.18f * pulse),
        radius = 38f * pulse,
        center = center
    )

    val text = "$name Lv.$level"
    val result = textMeasurer.measure(
        text = text,
        style = TextStyle(
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    )
    drawText(
        textMeasurer = textMeasurer,
        text = text,
        topLeft = Offset(center.x - result.size.width / 2f, center.y + 24f),
        style = TextStyle(
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    )
}

private fun DrawScope.drawDiscipleAvatar(
    center: Offset,
    discipleName: String,
    realm: Int,
    element: Int,
    pulse: Float,
    emote: String = "🧘",
    thought: String = "",
    isSparring: Boolean = false,
    textMeasurer: androidx.compose.ui.text.TextMeasurer
) {
    val elemColor = Color(GameConfig.getElementColor(element))

    // 1. Daoist Qi Aura Ring
    drawCircle(
        color = elemColor.copy(alpha = 0.35f * pulse),
        radius = 20f * pulse,
        center = center
    )

    // 2. Disciple Hanfu Robe Base (matching blue/purple/green pixel robes)
    drawRoundRect(
        color = elemColor,
        topLeft = Offset(center.x - 8f, center.y - 6f),
        size = Size(16f, 16f),
        cornerRadius = CornerRadius(5f, 5f)
    )

    // 3. Sash / Belt
    drawLine(
        color = GoldCelestial,
        start = Offset(center.x - 7f, center.y + 1f),
        end = Offset(center.x + 7f, center.y + 1f),
        strokeWidth = 2f
    )

    // 4. Cultivator Head with Topknot
    drawCircle(
        color = Color(0xFFFFE0B2),
        radius = 6.5f,
        center = Offset(center.x, center.y - 11f)
    )
    // Topknot hair bun
    drawCircle(
        color = Color(0xFF1E1B18),
        radius = 3.5f,
        center = Offset(center.x, center.y - 17f)
    )

    // Sparring sword effect
    if (isSparring) {
        drawLine(
            color = Color.White,
            start = Offset(center.x - 12f, center.y - 14f),
            end = Offset(center.x + 12f, center.y + 10f),
            strokeWidth = 2f
        )
        drawLine(
            color = GoldCelestial,
            start = Offset(center.x + 12f, center.y - 14f),
            end = Offset(center.x - 12f, center.y + 10f),
            strokeWidth = 2f
        )
    }

    // Emote badge above head
    val emoteResult = textMeasurer.measure(
        text = emote,
        style = TextStyle(fontSize = 11.sp)
    )
    drawText(
        textMeasurer = textMeasurer,
        text = emote,
        topLeft = Offset(center.x - emoteResult.size.width / 2f, center.y - 28f),
        style = TextStyle(fontSize = 11.sp)
    )

    // Name & Realm tag
    val realmTag = "${GameConfig.getRealmName(realm).take(4)}·$discipleName"
    val result = textMeasurer.measure(
        text = realmTag,
        style = TextStyle(color = QiGlow, fontSize = 8.sp, fontWeight = FontWeight.SemiBold)
    )
    drawText(
        textMeasurer = textMeasurer,
        text = realmTag,
        topLeft = Offset(center.x - result.size.width / 2f, center.y + 12f),
        style = TextStyle(color = QiGlow, fontSize = 8.sp, fontWeight = FontWeight.SemiBold)
    )

    // Thought bubble if present
    if (thought.isNotBlank()) {
        val shortThought = if (thought.length > 22) thought.take(20) + "…" else thought
        val thoughtResult = textMeasurer.measure(
            text = shortThought,
            style = TextStyle(color = Color(0xFFE0E0E0), fontSize = 7.sp, fontWeight = FontWeight.Normal)
        )
        drawRoundRect(
            color = Color(0xCC1A1A2E),
            topLeft = Offset(center.x - thoughtResult.size.width / 2f - 4f, center.y + 24f),
            size = Size(thoughtResult.size.width + 8f, thoughtResult.size.height + 4f),
            cornerRadius = CornerRadius(4f, 4f)
        )
        drawText(
            textMeasurer = textMeasurer,
            text = shortThought,
            topLeft = Offset(center.x - thoughtResult.size.width / 2f, center.y + 26f),
            style = TextStyle(color = Color(0xFFE0E0E0), fontSize = 7.sp, fontWeight = FontWeight.Normal)
        )
    }
}

private fun DrawScope.drawQiOrb(
    center: Offset,
    amount: Long,
    color: Color,
    pulse: Float,
    textMeasurer: androidx.compose.ui.text.TextMeasurer
) {
    drawCircle(
        color = color.copy(alpha = 0.3f),
        radius = 24f * pulse,
        center = center
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White, color),
            center = center,
            radius = 14f
        ),
        radius = 14f,
        center = center
    )
    drawCircle(
        color = Color.White,
        radius = 14f,
        center = center,
        style = Stroke(width = 1.5f)
    )

    val label = "+$amount"
    val result = textMeasurer.measure(
        text = label,
        style = TextStyle(color = Color.Yellow, fontSize = 8.sp, fontWeight = FontWeight.Bold)
    )
    drawText(
        textMeasurer = textMeasurer,
        text = label,
        topLeft = Offset(center.x - result.size.width / 2f, center.y + 16f),
        style = TextStyle(color = Color.Yellow, fontSize = 8.sp, fontWeight = FontWeight.Bold)
    )
}
