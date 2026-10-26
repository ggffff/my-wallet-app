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

  @Test
  fun `verify AppSignatureHelper expected fingerprints`() {
    assertEquals("CE:C8:04:28:58:3D:82:05:D6:3A:9F:D0:55:DB:E2:0D:6C:E6:C1:D3", com.example.util.AppSignatureHelper.EXPECTED_SHA1)
    assertEquals("99:25:B4:22:AC:B6:F3:2D:20:36:DE:29:C0:DE:EC:6D:C8:97:CB:79:D3:6C:6A:7B:26:41:D4:6A:64:AC:0E:38", com.example.util.AppSignatureHelper.EXPECTED_SHA256)
  }
}

