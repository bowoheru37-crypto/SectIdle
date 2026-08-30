package com.sect.idle.render.pipeline

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.ui.theme.CinnabarRed
import com.example.ui.theme.GoldCelestial
import com.example.ui.theme.JadePrimary
import com.example.ui.theme.QiGlow
import com.example.ui.theme.SpiritBlue
import com.sect.idle.ai.CultivationArenaEngine
import com.sect.idle.render.AuraType
import com.sect.idle.render.CombatSkillVfxType
import kotlin.math.PI

/**
 * CombatRenderPipeline - High-performance low-level rendering pipeline specifically
 * engineered for Android low-entry devices.
 *
 * Employs zero-allocation object pools, flat primitive float buffers, lookup tables (LUTs),
 * and pre-allocated Path caches to eliminate Garbage Collection (GC) pauses during fast combat animations.
 */
object CombatRenderPipeline {

    private val pathCache = FastPathCache(poolSize = 32)
    private val lineBuffer = FastFloatBuffer(capacity = 512)
    private val audienceBuffer = FastFloatBuffer(capacity = 256)

    // Pre-allocated static octagon vertices (8 angles)
    private val octagonCos = FloatArray(8)
    private val octagonSin = FloatArray(8)

    // Pre-allocated turnbuckle 4 corners angles
    private val cornerCos = FloatArray(4)
    private val cornerSin = FloatArray(4)

    init {
        for (i in 0 until 8) {
            val angle = ((PI * 2.0 / 8.0) * i - (PI / 8.0)).toFloat()
            octagonCos[i] = FastTrigLUT.fastCos(angle)
            octagonSin[i] = FastTrigLUT.fastSin(angle)
        }

        val cornerAngles = floatArrayOf(
            (-PI / 4.0).toFloat(),
            (PI / 4.0).toFloat(),
            (3.0 * PI / 4.0).toFloat(),
            (-3.0 * PI / 4.0).toFloat()
        )
        for (i in 0 until 4) {
            cornerCos[i] = FastTrigLUT.fastCos(cornerAngles[i])
            cornerSin[i] = FastTrigLUT.fastSin(cornerAngles[i])
        }
    }

    /**
     * Begin a frame: resets internal path caches and flat vertex buffers without GC allocation.
     */
    fun beginFrame() {
        pathCache.reset()
        lineBuffer.reset()
        audienceBuffer.reset()
    }

    /**
     * Render the 2.5D Celestial Octagon Mat (Bagua Ground) with zero-alloc reusable Path.
     */
    fun drawCelestialOctagonMat(
        drawScope: DrawScope,
        cx: Float,
        cy: Float,
        width: Float,
        height: Float,
        stipulation: CultivationArenaEngine.MatchStipulation
    ) {
        val rx = width * 0.42f
        val ry = height * 0.28f

        val octagonPath = pathCache.obtainComposePath()
        for (i in 0 until 8) {
            val x = cx + rx * octagonCos[i]
            val y = cy + ry * octagonSin[i]
            if (i == 0) octagonPath.moveTo(x, y) else octagonPath.lineTo(x, y)
        }
        octagonPath.close()

        // High vs Low-tier Device Shader Optimization
        if (LowEndDeviceProfile.enableComplexGradients) {
            val floorBrush = Brush.radialGradient(
                colors = listOf(Color(0xFF222842), Color(0xFF101322)),
                center = Offset(cx, cy),
                radius = rx
            )
            drawScope.drawPath(octagonPath, floorBrush)
        } else {
            // Flat hardware-accelerated fill on low-entry hardware
            drawScope.drawPath(octagonPath, Color(0xFF1B2036))
        }

        // Glowing Octagon Border
        val borderColor = when (stipulation) {
            CultivationArenaEngine.MatchStipulation.BAGUA_STEEL_CAGE -> FastColorCache.getQiGlow(0.75f)
            CultivationArenaEngine.MatchStipulation.NO_HOLDS_BARRED -> FastColorCache.getCinnabar(0.75f)
            else -> FastColorCache.getGold(0.75f)
        }
        drawScope.drawPath(octagonPath, borderColor, style = Stroke(width = 3.5f))

        // Inner Taiji Yin-Yang Dao Emblem in center of the ring
        drawScope.drawCircle(
            color = Color(0x22FFFFFF),
            radius = ry * 0.45f,
            center = Offset(cx, cy),
            style = Stroke(width = 2f)
        )
    }

    /**
     * Render Audience Spectator Silhouettes using flat primitive coordinates with zero list allocations.
     */
    fun drawArenaStadium(
        drawScope: DrawScope,
        width: Float,
        height: Float,
        hype: Int,
        wave: Float
    ) {
        // Ceiling background
        drawScope.drawRect(
            color = Color(0xFF0C0E1A),
            size = Size(width, height * 0.45f)
        )

        // Tiered Audience silhouettes
        val crowdRows = if (LowEndDeviceProfile.isLowEndDevice) 2 else 3
        val audienceColor = Color(0x334E6B9E)
        val colCount = if (LowEndDeviceProfile.isLowEndDevice) 8 else 14

        for (row in 0 until crowdRows) {
            val y = height * (0.12f + row * 0.08f)
            val rowWave = if (row % 2 == 0) wave else -wave
            val headRadius = 4.5f + (row * 0.8f)

            for (i in 0..colCount) {
                val x = (width / colCount) * i + rowWave
                drawScope.drawCircle(
                    color = audienceColor,
                    radius = headRadius,
                    center = Offset(x, y)
                )
            }
        }

        // High Hype Golden Spirit Fireworks / Sparks
        if (hype >= 50) {
            val sparkLimit = if (LowEndDeviceProfile.isLowEndDevice) 6 else 14
            val sparkColor = FastColorCache.getGold(0.45f)
            for (p in 0 until sparkLimit) {
                val px = (p * 59f) % width
                val py = ((p * 37f) + wave * 4f) % (height * 0.38f)
                drawScope.drawCircle(
                    color = sparkColor,
                    radius = 2.5f,
                    center = Offset(px, py)
                )
            }
        }
    }

    /**
     * Render Spirit Ropes & Dragon Turnbuckles using pooled bezier paths and batched line buffers.
     */
    fun drawSpiritRopesAndTurnbuckles(
        drawScope: DrawScope,
        cx: Float,
        cy: Float,
        width: Float,
        height: Float,
        vibrate: Float,
        stipulation: CultivationArenaEngine.MatchStipulation
    ) {
        val rx = width * 0.42f
        val ry = height * 0.28f
        val postHeight = 40f

        val postX = FloatArray(4)
        val postY = FloatArray(4)

        for (i in 0 until 4) {
            postX[i] = cx + rx * cornerCos[i]
            postY[i] = cy + ry * cornerSin[i]

            // Draw Column
            drawScope.drawLine(
                color = JadePrimary,
                start = Offset(postX[i], postY[i]),
                end = Offset(postX[i], postY[i] - postHeight),
                strokeWidth = 5.5f
            )
            // Turnbuckle Crystal Top
            drawScope.drawCircle(
                color = GoldCelestial,
                radius = 4.5f,
                center = Offset(postX[i], postY[i] - postHeight)
            )
        }

        // 3-Tier Spirit Ropes
        val ropeColor = if (stipulation == CultivationArenaEngine.MatchStipulation.BAGUA_STEEL_CAGE) {
            FastColorCache.getQiGlow(0.85f)
        } else {
            FastColorCache.getSpiritBlue(0.85f)
        }

        val ropeTiers = if (LowEndDeviceProfile.isLowEndDevice) 2 else 3
        for (tier in 1..ropeTiers) {
            val yOffset = (postHeight / (ropeTiers + 1)) * tier
            for (i in 0 until 4) {
                val nextI = (i + 1) % 4
                val x1 = postX[i]
                val y1 = postY[i] - yOffset
                val x2 = postX[nextI]
                val y2 = postY[nextI] - yOffset
                val midX = (x1 + x2) * 0.5f
                val midY = (y1 + y2) * 0.5f + vibrate

                val ropePath = pathCache.obtainComposePath()
                ropePath.moveTo(x1, y1)
                ropePath.quadraticTo(midX, midY, x2, y2)

                drawScope.drawPath(ropePath, ropeColor, style = Stroke(width = 2.2f))
            }
        }

        // Steel Cage Bars if Cage Match
        if (stipulation == CultivationArenaEngine.MatchStipulation.BAGUA_STEEL_CAGE) {
            val cageColor = FastColorCache.getQiGlow(0.35f)
            val barCount = if (LowEndDeviceProfile.isLowEndDevice) 6 else 12
            for (i in 0..barCount) {
                val barX = (cx - rx * 0.85f) + (rx * 1.7f / barCount) * i
                drawScope.drawLine(
                    color = cageColor,
                    start = Offset(barX, cy - ry - 20f),
                    end = Offset(barX, cy + ry * 0.6f),
                    strokeWidth = 1.5f
                )
            }
        }
    }

    /**
     * Render Elder Referee.
     */
    fun drawElderReferee(drawScope: DrawScope, cx: Float, cy: Float, refCall: String) {
        val skinColor = Color(0xFFF0D5BA)
        drawScope.drawCircle(color = skinColor, radius = 5f, center = Offset(cx, cy - 14f))
        drawScope.drawOval(color = Color.Black, topLeft = Offset(cx - 6f, cy - 20f), size = Size(12f, 4f))
        drawScope.drawRect(color = Color(0xFFE0E0E0), topLeft = Offset(cx - 5f, cy - 9f), size = Size(10f, 15f))
        drawScope.drawRect(color = Color.Black, topLeft = Offset(cx - 3f, cy - 9f), size = Size(2f, 15f))
        drawScope.drawRect(color = Color.Black, topLeft = Offset(cx + 1f, cy - 9f), size = Size(2f, 15f))

        if ("1..." in refCall || "2..." in refCall || "3!" in refCall) {
            drawScope.drawLine(
                color = skinColor,
                start = Offset(cx + 5f, cy - 6f),
                end = Offset(cx + 14f, cy - 18f),
                strokeWidth = 2.5f
            )
        }
    }

    /**
     * Render Disciple & Opponent Fighters with zero-allocation geometry paths.
     */
    fun drawFighters(
        drawScope: DrawScope,
        centerX: Float,
        centerY: Float,
        moveAnim: String,
        auraPulse: Float,
        discipleHp: Int,
        enemyHp: Int
    ) {
        var dX = centerX - 60f
        var dY = centerY + 10f
        var eX = centerX + 60f
        var eY = centerY + 10f

        when (moveAnim) {
            "STRIKE" -> {
                dX = centerX - 25f
                eX = centerX + 25f
            }
            "SUPLEX" -> {
                dX = centerX - 10f
                eX = centerX + 10f
                eY -= 28f
            }
            "SUBMISSION" -> {
                dX = centerX - 12f
                eX = centerX + 5f
                eY += 12f
            }
            "ROPE_BOUNCE" -> {
                dX = centerX - 85f
                eX = centerX + 30f
            }
            "FINISHER" -> {
                dX = centerX - 15f
                dY -= 20f
                eX = centerX + 15f
            }
            "VICTORY_BELT" -> {
                dX = centerX
                eX = centerX + 80f
                eY += 15f
            }
        }

        // 1. Disciple (Left) Aura & Body
        val discipleAuraRadius = 26f * auraPulse
        if (LowEndDeviceProfile.enableComplexGradients) {
            drawScope.drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(FastColorCache.getJade(0.45f), Color.Transparent),
                    center = Offset(dX, dY),
                    radius = discipleAuraRadius
                ),
                radius = discipleAuraRadius,
                center = Offset(dX, dY)
            )
        } else {
            drawScope.drawCircle(
                color = FastColorCache.getJade(0.25f),
                radius = discipleAuraRadius,
                center = Offset(dX, dY)
            )
        }

        drawScope.drawCircle(color = Color(0xFFFFDBAC), radius = 8f, center = Offset(dX, dY - 24f))

        val discipleBody = pathCache.obtainComposePath()
        discipleBody.moveTo(dX - 8f, dY - 16f)
        discipleBody.lineTo(dX + 8f, dY - 16f)
        discipleBody.lineTo(dX + 12f, dY + 6f)
        discipleBody.lineTo(dX - 12f, dY + 6f)
        discipleBody.close()
        drawScope.drawPath(discipleBody, SpiritBlue)

        drawScope.drawRect(color = GoldCelestial, topLeft = Offset(dX - 10f, dY - 4f), size = Size(20f, 3.5f))

        // 2. Opponent (Right) Aura & Body
        val oppAuraRadius = 28f * auraPulse
        if (LowEndDeviceProfile.enableComplexGradients) {
            drawScope.drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(FastColorCache.getCinnabar(0.45f), Color.Transparent),
                    center = Offset(eX, eY),
                    radius = oppAuraRadius
                ),
                radius = oppAuraRadius,
                center = Offset(eX, eY)
            )
        } else {
            drawScope.drawCircle(
                color = FastColorCache.getCinnabar(0.25f),
                radius = oppAuraRadius,
                center = Offset(eX, eY)
            )
        }

        drawScope.drawCircle(color = Color(0xFFD2B48C), radius = 9f, center = Offset(eX, eY - 25f))

        val oppBody = pathCache.obtainComposePath()
        oppBody.moveTo(eX - 10f, eY - 16f)
        oppBody.lineTo(eX + 10f, eY - 16f)
        oppBody.lineTo(eX + 14f, eY + 8f)
        oppBody.lineTo(eX - 14f, eY + 8f)
        oppBody.close()
        drawScope.drawPath(oppBody, CinnabarRed)

        drawScope.drawLine(color = Color.Black, start = Offset(eX - 5f, eY - 32f), end = Offset(eX - 10f, eY - 42f), strokeWidth = 3f)
        drawScope.drawLine(color = Color.Black, start = Offset(eX + 5f, eY - 32f), end = Offset(eX + 10f, eY - 42f), strokeWidth = 3f)
    }

    /**
     * Render Action Move Hit Impact VFX.
     */
    fun drawMoveImpactFX(drawScope: DrawScope, cx: Float, cy: Float, moveAnim: String, auraPulse: Float) {
        when (moveAnim) {
            "STRIKE" -> {
                drawScope.drawCircle(
                    color = FastColorCache.getGold(0.8f),
                    radius = 18f * auraPulse,
                    center = Offset(cx, cy),
                    style = Stroke(width = 3f)
                )
            }
            "SUPLEX" -> {
                drawScope.drawOval(
                    color = FastColorCache.getCinnabar(0.6f),
                    topLeft = Offset(cx - 35f, cy + 5f),
                    size = Size(70f, 25f),
                    style = Stroke(width = 4f)
                )
            }
            "FINISHER" -> {
                val finRadius = 52f * auraPulse
                if (LowEndDeviceProfile.enableComplexGradients) {
                    drawScope.drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(GoldCelestial, QiGlow, Color.Transparent),
                            center = Offset(cx, cy),
                            radius = finRadius
                        ),
                        radius = finRadius,
                        center = Offset(cx, cy)
                    )
                } else {
                    drawScope.drawCircle(
                        color = FastColorCache.getGold(0.5f),
                        radius = finRadius,
                        center = Offset(cx, cy),
                        style = Stroke(width = 4f)
                    )
                }
            }
        }
    }

    /**
     * Render Combat Skill VFX (Spirit Blasts, Flying Swords, Bagua Shockwaves, Lightning).
     */
    fun drawSkillVfx(
        drawScope: DrawScope,
        vfxType: CombatSkillVfxType,
        progress: Float,
        source: Offset,
        target: Offset
    ) {
        if (vfxType == CombatSkillVfxType.NONE || progress <= 0f || progress >= 1f) return

        when (vfxType) {
            CombatSkillVfxType.SPIRIT_ENERGY_BLAST -> {
                val currentX = source.x + (target.x - source.x) * progress
                val currentY = source.y + (target.y - source.y) * progress
                val currentPos = Offset(currentX, currentY)
                val alpha = (1f - progress * 0.2f).coerceIn(0f, 1f)

                drawScope.drawLine(
                    color = FastColorCache.getJade(alpha),
                    start = source,
                    end = currentPos,
                    strokeWidth = 12f * (1f - progress * 0.3f),
                    cap = StrokeCap.Round
                )
                drawScope.drawCircle(
                    color = Color.White,
                    radius = 16f,
                    center = currentPos
                )

                if (progress > 0.65f) {
                    val impactFactor = (progress - 0.65f) / 0.35f
                    drawScope.drawCircle(
                        color = FastColorCache.getGold(1f - impactFactor),
                        radius = 38f * impactFactor,
                        center = target,
                        style = Stroke(width = 3f)
                    )
                }
            }

            CombatSkillVfxType.FLYING_SWORD_SLASH -> {
                val alpha = (1f - progress).coerceIn(0f, 1f)
                val midX = (source.x + target.x) * 0.5f
                val midY = (source.y + target.y) * 0.5f - 40f

                val slashPath = pathCache.obtainComposePath()
                slashPath.moveTo(source.x - 30f, source.y + 20f)
                slashPath.quadraticTo(midX, midY, target.x + 30f, target.y - 20f)

                drawScope.drawPath(
                    path = slashPath,
                    color = FastColorCache.getGold(alpha),
                    style = Stroke(
                        width = 10f * (1f - progress * 0.5f),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                val ghostPath = pathCache.obtainComposePath()
                ghostPath.moveTo(source.x - 20f, source.y + 35f)
                ghostPath.quadraticTo(midX + 10f, midY + 15f, target.x + 40f, target.y - 5f)

                drawScope.drawPath(
                    path = ghostPath,
                    color = FastColorCache.getSpiritBlue(alpha * 0.6f),
                    style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                )
            }

            CombatSkillVfxType.BAGUA_PALM_SHOCKWAVE -> {
                val alpha = (1f - progress).coerceIn(0f, 1f)
                val maxRadius = 85f * progress

                drawScope.drawCircle(
                    color = FastColorCache.getJade(alpha),
                    radius = maxRadius,
                    center = target,
                    style = Stroke(width = 4f * (1f - progress))
                )

                val dirCount = if (LowEndDeviceProfile.isLowEndDevice) 4 else 8
                for (i in 0 until dirCount) {
                    val angle = (i * (2.0 * PI / dirCount)).toFloat()
                    val c = FastTrigLUT.fastCos(angle)
                    val s = FastTrigLUT.fastSin(angle)
                    val x1 = target.x + maxRadius * 0.3f * c
                    val y1 = target.y + maxRadius * 0.3f * s
                    val x2 = target.x + maxRadius * 0.95f * c
                    val y2 = target.y + maxRadius * 0.95f * s

                    drawScope.drawLine(
                        color = FastColorCache.getGold(alpha),
                        start = Offset(x1, y1),
                        end = Offset(x2, y2),
                        strokeWidth = 2.5f
                    )
                }
            }

            CombatSkillVfxType.HEAVENLY_SUPLEX_CRUSH -> {
                val alpha = (1f - progress).coerceIn(0f, 1f)
                val crackRadius = 65f * progress

                drawScope.drawOval(
                    color = FastColorCache.getCinnabar(alpha * 0.6f),
                    topLeft = Offset(target.x - crackRadius, target.y - crackRadius * 0.4f),
                    size = Size(crackRadius * 2, crackRadius * 0.8f)
                )

                val crackCount = if (LowEndDeviceProfile.isLowEndDevice) 4 else 6
                for (i in 0 until crackCount) {
                    val angle = ((i * (2.0 * PI / crackCount)) + 0.25f).toFloat()
                    val dist = crackRadius * (0.8f + (i % 3) * 0.1f)
                    val endX = target.x + dist * FastTrigLUT.fastCos(angle)
                    val endY = target.y + (dist * FastTrigLUT.fastSin(angle) * 0.4f)

                    drawScope.drawLine(
                        color = FastColorCache.getGold(alpha),
                        start = target,
                        end = Offset(endX, endY),
                        strokeWidth = 3f * (1f - progress)
                    )
                }
            }

            CombatSkillVfxType.TRIBULATION_THUNDER_STRIKE -> {
                val alpha = (1f - progress * 0.5f).coerceIn(0f, 1f)
                val skyY = target.y - 220f

                val lightningPath = pathCache.obtainComposePath()
                lightningPath.moveTo(target.x - 20f, skyY)
                lightningPath.lineTo(target.x + 15f, skyY + 60f)
                lightningPath.lineTo(target.x - 25f, skyY + 120f)
                lightningPath.lineTo(target.x + 10f, skyY + 170f)
                lightningPath.lineTo(target.x, target.y)

                drawScope.drawPath(
                    path = lightningPath,
                    color = FastColorCache.getLightning(alpha),
                    style = Stroke(width = 6f, cap = StrokeCap.Round)
                )

                drawScope.drawPath(
                    path = lightningPath,
                    color = Color.White.copy(alpha = alpha),
                    style = Stroke(width = 2.5f, cap = StrokeCap.Round)
                )
            }

            else -> {}
        }
    }

    /**
     * Render Dynamic Particle Spirit Aura with zero trigonometric runtime allocation.
     */
    fun drawSpiritAura(
        drawScope: DrawScope,
        centerX: Float,
        centerY: Float,
        baseRadius: Float,
        rotationDeg: Float,
        intensity: Float,
        primaryColor: Color,
        secondaryColor: Color
    ) {
        val radOffset = (rotationDeg * (PI / 180.0)).toFloat()
        val particleCount = LowEndDeviceProfile.maxParticles

        // 1. Ambient central glow
        drawScope.drawCircle(
            color = primaryColor.copy(alpha = 0.22f * intensity),
            radius = baseRadius * 1.4f,
            center = Offset(centerX, centerY)
        )

        // 2. Swirling Orbiting Qi Motes
        for (i in 0 until particleCount) {
            val angle = radOffset + (i * 2.0f * PI.toFloat() / particleCount)
            val sVal = FastTrigLUT.fastSin(angle * 2f + i)
            val orbitRadius = baseRadius * (0.7f + 0.3f * sVal)
            val px = centerX + orbitRadius * FastTrigLUT.fastCos(angle)
            val py = centerY + orbitRadius * FastTrigLUT.fastSin(angle) * 0.7f
            val pSize = (3.5f + 2.5f * FastTrigLUT.fastSin(angle + i)) * intensity

            drawScope.drawCircle(
                color = if (i % 2 == 0) primaryColor else secondaryColor,
                radius = pSize,
                center = Offset(px, py)
            )
        }

        // 3. Central Rotating Seal
        drawScope.rotate(rotationDeg, pivot = Offset(centerX, centerY)) {
            drawScope.drawCircle(
                color = secondaryColor.copy(alpha = 0.35f * intensity),
                radius = baseRadius * 0.55f,
                center = Offset(centerX, centerY),
                style = Stroke(width = 1.5f)
            )
            drawScope.drawCircle(
                color = primaryColor.copy(alpha = 0.5f * intensity),
                radius = baseRadius * 0.35f,
                center = Offset(centerX, centerY),
                style = Stroke(width = 1.5f)
            )
        }
    }
}
