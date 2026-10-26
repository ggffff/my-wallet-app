package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

import com.example.ui.auth.getWebClientId
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("My Wallet", appName)
  }

  @Test
  fun `verify getWebClientId resolves generated default_web_client_id`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val webClientId = getWebClientId(context)
    assertNotNull("Web client ID should not be null when google-services.json is present", webClientId)
    assertTrue("Web client ID should contain .apps.googleusercontent.com", webClientId?.contains(".apps.googleusercontent.com") == true)
  }
}

