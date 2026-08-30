package com.sect.idle.render.pipeline

import android.graphics.Paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * FastFloatBuffer - Low-level flat array primitive buffer for batch vertex and line rendering.
 * Eliminates garbage collection pressure on low-entry Android devices by pre-allocating
 * memory and reusing primitive float arrays.
 */
class FastFloatBuffer(val capacity: Int = 1024) {
    val buffer = FloatArray(capacity)
    var count = 0
        private set

    fun reset() {
        count = 0
    }

    fun push(value: Float) {
        if (count < capacity) {
            buffer[count++] = value
        }
    }

    fun push(x: Float, y: Float) {
        if (count + 1 < capacity) {
            buffer[count++] = x
            buffer[count++] = y
        }
    }

    fun pushLine(x1: Float, y1: Float, x2: Float, y2: Float) {
        if (count + 3 < capacity) {
            buffer[count++] = x1
            buffer[count++] = y1
            buffer[count++] = x2
            buffer[count++] = y2
        }
    }

    fun pushQuad(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float, x4: Float, y4: Float) {
        if (count + 7 < capacity) {
            buffer[count++] = x1
            buffer[count++] = y1
            buffer[count++] = x2
            buffer[count++] = y2
            buffer[count++] = x3
            buffer[count++] = y3
            buffer[count++] = x4
            buffer[count++] = y4
        }
    }
}

/**
 * FastPathCache - Pre-allocated reusable Path pool to prevent allocating new Path()
 * objects on every animation tick or draw frame.
 */
class FastPathCache(val poolSize: Int = 16) {
    private val composePaths = Array(poolSize) { Path() }
    private val nativePaths = Array(poolSize) { android.graphics.Path() }
    private var index = 0

    fun reset() {
        index = 0
    }

    fun obtainComposePath(): Path {
        val p = composePaths[index % poolSize]
        p.reset()
        index++
        return p
    }

    fun obtainNativePath(): android.graphics.Path {
        val p = nativePaths[index % poolSize]
        p.reset()
        index++
        return p
    }
}

/**
 * FastTrigLUT - High-performance Sin/Cos Lookup Table with 512 discrete angle buckets.
 * Low-end mobile processors often struggle with repeated transcendental Math.sin/cos calls.
 */
object FastTrigLUT {
    private const val TABLE_SIZE = 512
    private const val TABLE_MASK = TABLE_SIZE - 1
    private const val RAD_TO_INDEX = (TABLE_SIZE / (2.0 * PI)).toFloat()

    private val sinTable = FloatArray(TABLE_SIZE)
    private val cosTable = FloatArray(TABLE_SIZE)

    init {
        for (i in 0 until TABLE_SIZE) {
            val rad = (i * 2.0 * PI / TABLE_SIZE).toFloat()
            sinTable[i] = sin(rad)
            cosTable[i] = cos(rad)
        }
    }

    fun fastSin(rad: Float): Float {
        val index = (rad * RAD_TO_INDEX).toInt() and TABLE_MASK
        return sinTable[index]
    }

    fun fastCos(rad: Float): Float {
        val index = (rad * RAD_TO_INDEX).toInt() and TABLE_MASK
        return cosTable[index]
    }
}

/**
 * FastColorCache - Pre-cached color table for common alpha steps (0..20)
 * to avoid repeated Color.copy(alpha = ...) heap allocations.
 */
object FastColorCache {
    private const val ALPHA_STEPS = 21 // 0.0 to 1.0 in 0.05 increments

    private fun buildAlphaArray(baseColor: Color): Array<Color> {
        return Array(ALPHA_STEPS) { step ->
            val alpha = (step * 0.05f).coerceIn(0f, 1f)
            baseColor.copy(alpha = alpha)
        }
    }

    private val jadeTable = buildAlphaArray(Color(0xFF00E676))
    private val goldTable = buildAlphaArray(Color(0xFFFFD700))
    private val cinnabarTable = buildAlphaArray(Color(0xFFFF5252))
    private val qiGlowTable = buildAlphaArray(Color(0xFF69F0AE))
    private val spiritBlueTable = buildAlphaArray(Color(0xFF448AFF))
    private val voidPurpleTable = buildAlphaArray(Color(0xFFB388FF))
    private val lightningTable = buildAlphaArray(Color(0xFF80D8FF))

    fun getJade(alpha: Float): Color {
        val idx = (alpha * 20f).toInt().coerceIn(0, 20)
        return jadeTable[idx]
    }

    fun getGold(alpha: Float): Color {
        val idx = (alpha * 20f).toInt().coerceIn(0, 20)
        return goldTable[idx]
    }

    fun getCinnabar(alpha: Float): Color {
        val idx = (alpha * 20f).toInt().coerceIn(0, 20)
        return cinnabarTable[idx]
    }

    fun getQiGlow(alpha: Float): Color {
        val idx = (alpha * 20f).toInt().coerceIn(0, 20)
        return qiGlowTable[idx]
    }

    fun getSpiritBlue(alpha: Float): Color {
        val idx = (alpha * 20f).toInt().coerceIn(0, 20)
        return spiritBlueTable[idx]
    }

    fun getVoidPurple(alpha: Float): Color {
        val idx = (alpha * 20f).toInt().coerceIn(0, 20)
        return voidPurpleTable[idx]
    }

    fun getLightning(alpha: Float): Color {
        val idx = (alpha * 20f).toInt().coerceIn(0, 20)
        return lightningTable[idx]
    }
}

/**
 * LowEndDeviceProfile - Hardware capability detector for adaptive dynamic Level-of-Detail (LOD).
 */
object LowEndDeviceProfile {
    enum class QualityLevel {
        LOW,
        MEDIUM,
        HIGH
    }

    val currentQuality: QualityLevel by lazy {
        val cores = Runtime.getRuntime().availableProcessors()
        val maxMemMB = Runtime.getRuntime().maxMemory() / (1024 * 1024)

        when {
            cores <= 4 || maxMemMB <= 128 -> QualityLevel.LOW
            cores <= 6 || maxMemMB <= 256 -> QualityLevel.MEDIUM
            else -> QualityLevel.HIGH
        }
    }

    val isLowEndDevice: Boolean
        get() = currentQuality == QualityLevel.LOW

    val maxParticles: Int
        get() = when (currentQuality) {
            QualityLevel.LOW -> 8
            QualityLevel.MEDIUM -> 14
            QualityLevel.HIGH -> 24
        }

    val enableComplexGradients: Boolean
        get() = currentQuality != QualityLevel.LOW
}
