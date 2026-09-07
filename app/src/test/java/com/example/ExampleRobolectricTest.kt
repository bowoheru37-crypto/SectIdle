package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.sect.idle.models.Building
import com.sect.idle.models.Disciple
import com.sect.idle.gameplay.GameViewModel
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
    fun `verify disciple recruitment action`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = GameViewModel(app)

        val disciple = Disciple().apply {
            id = "test_disciple_999"
            name = "Test Disciple"
        }
        vm.recruitDisciple(disciple)
        val disciples = vm.data.disciples
        assertTrue(disciples.contains(disciple))
    }
}
