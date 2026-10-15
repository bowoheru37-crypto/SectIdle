package com.sect.idle.render

import android.graphics.Bitmap
import android.graphics.Canvas
import com.sect.idle.systems.CameraSystem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ParticleEngineV2Test {

    @Test
    fun testParticleEngineSpawnUpdateAndRender() {
        val particleEngine = ParticleEngineV2(1080, 1920)

        // Spawn particles including TYPE_QI_PETAL
        val idx1 = particleEngine.spawn(100f, 100f, 5f, -5f, 1.0f, 10f, 0xFF00FF00.toInt(), ParticleEngineV2.TYPE_QI_PETAL)
        val idx2 = particleEngine.spawn(200f, 200f, -2f, 2f, 1.0f, 8f, 0xFFFF0000.toInt(), ParticleEngineV2.TYPE_FIRE)
        val idx3 = particleEngine.spawn(300f, 300f, 10f, 0f, 1.0f, 12f, 0xFF0000FF.toInt(), ParticleEngineV2.TYPE_SWORD_AURA)

        assertTrue(idx1 >= 0)
        assertTrue(idx2 >= 0)
        assertTrue(idx3 >= 0)
        assertTrue(particleEngine.activeCount > 0)

        // Update particles
        particleEngine.update(0.016f)

        // Render to canvas
        val bitmap = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val cam = CameraSystem(1080, 1920)

        particleEngine.render(canvas, cam)
        particleEngine.renderScreenSpace(canvas)

        // Clear particles
        particleEngine.clear()
        assertEquals(0, particleEngine.activeCount)
    }
}
