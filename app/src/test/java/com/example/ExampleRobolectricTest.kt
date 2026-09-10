package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.engine.TerminalEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("lemon_box", appName)
  }

  @Test
  fun `test terminal engine echo and pwd`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val engine = TerminalEngine(context)
    engine.executeCommand("echo hello lemon_box")
    val lines = engine.lines.value
    assertTrue(lines.any { it.text.contains("hello lemon_box") })
  }
}
