package com.sect.idle.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sect.idle.render.pipeline.CombatRenderPipeline
import com.sect.idle.ui.base.BattleState

/**
 * ArenaRingCanvas - 2.5D Isometric / Perspective Celestial Octagon Ring and Donghua
 * Martial Arts Arena for WWE / MMA Style Immortal Cultivation Showdowns.
 *
 * Fully optimized for low-end Android 5.0+ devices using low-level buffer management
 * and zero-allocation path caches via CombatRenderPipeline.
 */
@Composable
fun ArenaRingCanvas(
    battleState: BattleState,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "arena_anim")
    
    // Continuous aura & rope oscillation
    val auraPulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura_pulse"
    )

    val crowdCheerWave by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "crowd_wave"
    )

    val ropeVibrate by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rope_vib"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0A0A14))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerX = width / 2f
            val centerY = height * 0.55f

            // Begin Frame: Reset low-level buffer indices and path caches
            CombatRenderPipeline.beginFrame()

            // 1. Draw Arena Stadium Atmosphere & Spectator Crowd Silhouettes
            CombatRenderPipeline.drawArenaStadium(this, width, height, battleState.crowdHype, crowdCheerWave)

            // 2. Draw 2.5D Celestial Octagon Mat (Bagua Ground)
            CombatRenderPipeline.drawCelestialOctagonMat(this, centerX, centerY, width, height, battleState.stipulation)

            // 3. Draw Spirit Dao Ropes & Dragon Turnbuckles
            CombatRenderPipeline.drawSpiritRopesAndTurnbuckles(this, centerX, centerY, width, height, ropeVibrate, battleState.stipulation)

            // 4. Draw Elder Referee in the background/center
            CombatRenderPipeline.drawElderReferee(this, centerX, centerY - 25f, battleState.refereeCall)

            // 5. Draw Disciple Fighter (Left Corner) & Opponent Fighter (Right Corner)
            val moveAnim = battleState.lastMoveVisual
            CombatRenderPipeline.drawFighters(
                drawScope = this,
                centerX = centerX,
                centerY = centerY,
                moveAnim = moveAnim,
                auraPulse = auraPulse,
                discipleHp = battleState.discipleHp,
                enemyHp = battleState.enemyHp
            )

            // 6. Draw Shockwave & Hit Impact FX if actively performing a move
            if (moveAnim != "IDLE") {
                CombatRenderPipeline.drawMoveImpactFX(this, centerX, centerY, moveAnim, auraPulse)
            }
        }
    }
}
