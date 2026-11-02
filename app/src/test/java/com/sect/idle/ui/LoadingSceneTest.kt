package com.sect.idle.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.test.core.app.ApplicationProvider
import com.sect.idle.systems.CameraSystem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LoadingSceneTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testLoadingSceneProgressAndCompletion() {
        val scene = LoadingScene(context)
        assertEquals(0f, scene.progress, 0.001f)

        var completed = false
        scene.setCompleteListener(object : LoadingScene.LoadingCompleteListener {
            override fun onLoadingComplete() {
                completed = true
            }
        })

        // Step progress forward
        scene.setProgress(1.0f)
        scene.update(1.0f) // 1.5 * 1.0f = 1.5f -> caps at 1.0f
        assertEquals(1.0f, scene.progress, 0.001f)
        assertTrue(completed)

        // Reset progress
        scene.resetProgress()
        assertEquals(0f, scene.progress, 0.001f)
    }

    @Test
    fun testLoadingSceneRenderWithoutAndWithParallax() {
        val scene = LoadingScene(context)
        val bitmap = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        val cam = CameraSystem(1080, 1920)

        // Render without parallax bitmap
        scene.render(canvas, paint, cam)

        // Render with parallax bitmap
        val parallax = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888)
        scene.setAtlas(parallax)
        scene.render(canvas, paint, cam)

        // Multiple render passes to verify reusable RectF stability
        scene.update(0.016f)
        scene.render(canvas, paint, cam)
        scene.render(canvas, paint, null)
    }
}
