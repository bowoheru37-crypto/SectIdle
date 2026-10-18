package com.sect.idle.core

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.cos
import kotlin.math.sin

class MathUtilsTest {

    @Test
    fun testSinAndCosLookups() {
        val delta = 0.05f

        // Test standard angles
        val angles = floatArrayOf(
            0f,
            (Math.PI / 6).toFloat(),
            (Math.PI / 4).toFloat(),
            (Math.PI / 2).toFloat(),
            Math.PI.toFloat(),
            (3 * Math.PI / 2).toFloat(),
            (2 * Math.PI).toFloat(),
            -1.0f,
            -3.14f,
            10.0f
        )

        for (rad in angles) {
            val expectedSin = sin(rad.toDouble()).toFloat()
            val expectedCos = cos(rad.toDouble()).toFloat()

            val actualSin = MathUtils.sin(rad)
            val actualCos = MathUtils.cos(rad)

            assertEquals("Sin failed for rad: $rad", expectedSin, actualSin, delta)
            assertEquals("Cos failed for rad: $rad", expectedCos, actualCos, delta)
        }
    }
}
