package com.sect.idle.render

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.CinnabarRed
import com.example.ui.theme.GoldCelestial
import com.example.ui.theme.JadePrimary
import com.example.ui.theme.QiGlow
import com.example.ui.theme.SpiritBlue
import com.sect.idle.render.pipeline.CombatRenderPipeline

/**
 * High-performance Particle & Spirit Aura System for Xianxia Immortal Cultivators.
 * Built with zero-allocation Compose Canvas rendering pipeline for Android 5.0+ low-end devices.
 */

enum class AuraType {
    IDLE_QI,
    JADE_WOOD,
    SPIRIT_WATER,
    CRIMSON_FIRE,
    GOLDEN_SWORD,
    PURPLE_VOID,
    TRIBULATION_LIGHTNING
}

enum class CombatSkillVfxType {
    NONE,
    SPIRIT_ENERGY_BLAST,
    FLYING_SWORD_SLASH,
    BAGUA_PALM_SHOCKWAVE,
    HEAVENLY_SUPLEX_CRUSH,
    TRIBULATION_THUNDER_STRIKE
}

@Composable
fun SpiritAuraEffect(
    modifier: Modifier = Modifier,
    auraType: AuraType = AuraType.IDLE_QI,
    intensity: Float = 1.0f,
    centerOffsetRatio: Offset = Offset(0.5f, 0.5f)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "spirit_aura_anim")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "aura_rotation"
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura_pulse"
    )

    val primaryColor = remember(auraType) {
        when (auraType) {
            AuraType.IDLE_QI -> QiGlow
            AuraType.JADE_WOOD -> JadePrimary
            AuraType.SPIRIT_WATER -> SpiritBlue
            AuraType.CRIMSON_FIRE -> CinnabarRed
            AuraType.GOLDEN_SWORD -> GoldCelestial
            AuraType.PURPLE_VOID -> Color(0xFFB388FF)
            AuraType.TRIBULATION_LIGHTNING -> Color(0xFF80D8FF)
        }
    }

    val secondaryColor = remember(auraType) {
        when (auraType) {
            AuraType.IDLE_QI -> GoldCelestial
            AuraType.JADE_WOOD -> Color(0xFFA7FFEB)
            AuraType.SPIRIT_WATER -> Color(0xFF82B1FF)
            AuraType.CRIMSON_FIRE -> Color(0xFFFF9E80)
            AuraType.GOLDEN_SWORD -> Color(0xFFFFFF8D)
            AuraType.PURPLE_VOID -> Color(0xFFEA80FC)
            AuraType.TRIBULATION_LIGHTNING -> Color(0xFFFFFFFF)
        }
    }

    Canvas(modifier = modifier) {
        val centerX = size.width * centerOffsetRatio.x
        val centerY = size.height * centerOffsetRatio.y
        val baseRadius = (size.minDimension / 2.5f) * pulse * intensity

        CombatRenderPipeline.drawSpiritAura(
            drawScope = this,
            centerX = centerX,
            centerY = centerY,
            baseRadius = baseRadius,
            rotationDeg = rotation,
            intensity = intensity,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor
        )
    }
}

/**
 * Combat Skill Visual Effect Canvas (Spirit Energy Blast & Sword Slashes).
 * Optimized via low-level zero-allocation path caches for 60fps on budget hardware.
 */
@Composable
fun CombatSkillVfxCanvas(
    vfxType: CombatSkillVfxType,
    progress: Float, // 0.0 to 1.0 animation progress
    sourcePos: Offset,
    targetPos: Offset,
    modifier: Modifier = Modifier
) {
    if (vfxType == CombatSkillVfxType.NONE || progress <= 0f || progress >= 1f) return

    Canvas(modifier = modifier.fillMaxSize()) {
        CombatRenderPipeline.drawSkillVfx(
            drawScope = this,
            vfxType = vfxType,
            progress = progress,
            source = sourcePos,
            target = targetPos
        )
    }
}

