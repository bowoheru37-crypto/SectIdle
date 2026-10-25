package com.sect.idle.render

import android.graphics.Bitmap
import android.graphics.Canvas
import com.sect.idle.systems.CameraSystem
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class IsometricWorldTest {

    @Test
    fun testIsometricWorldRender3DAndLegacyRender() {
        val world = IsometricWorld()
        assertNotNull(world.chunkManager)
        assertNotNull(world.camera3D)

        // Update world animation / camera state
        world.update(0.016f)
        world.setWeather(1) // Rain

        val bitmap = Bitmap.createBitmap(720, 1280, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val cam = CameraSystem(720, 1280)

        // Render 3D map
        world.render3D(canvas, 720, 1280)

        // Render legacy wrapper
        world.render(canvas, cam, 720, 1280)

        // Render game object
        world.renderObject(canvas, cam, 5f, 5f, 2f, 2f, 0xFF00FF00.toInt(), 10f)
    }
}
