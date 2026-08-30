package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.sect.idle.models.Building
import com.sect.idle.models.Disciple
import com.sect.idle.ui.GameViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertNotNull(appName)
    }

    @Test
    fun `verify disciple dismissal and facility demolition actions`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = GameViewModel(app)

        // Test disciple dismiss
        val disciple = Disciple().apply {
            id = "test_disciple_999"
            name = "Test Disciple"
        }
        vm.recruitDisciple(disciple)
        vm.dismissDisciple(disciple.id)
        val remaining = vm.uiState.value.disciples.find { it.id == disciple.id }
        assertEquals(null, remaining)

        // Test building demolish
        val pavilion = vm.uiState.value.buildings.firstOrNull()
        if (pavilion != null) {
            val initialLevel = pavilion.level
            vm.demolishBuilding(pavilion.type)
            if (initialLevel > 1) {
                assertEquals(initialLevel - 1, pavilion.level)
            } else {
                assertEquals(1, pavilion.level)
            }
        }
    }
}
