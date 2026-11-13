package com.example.ui.ads

import android.content.Context
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import com.example.R
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAdView
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AdMobIntegrationTest {

    @Test
    fun `verify AdMob App ID uses official Google test ID in debug and real ID in release`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val expectedTestAppId = "ca-app-pub-3940256099942544~3347511713"
        val expectedRealAppId = "ca-app-pub-6482896166784785~3253113511"

        // Debug manifest/resources resolve to Google's official test App ID
        assertEquals(expectedTestAppId, context.getString(R.string.admob_app_id))
        assertEquals(expectedTestAppId, AdMobConfig.resolveAppId(context, isDebugBuild = true))

        // Production Release App ID is stored and resolved cleanly for release builds
        assertEquals(expectedRealAppId, context.getString(R.string.admob_real_app_id))
        assertEquals(expectedRealAppId, AdMobConfig.resolveAppId(context, isDebugBuild = false))

        val appInfo = context.packageManager.getApplicationInfo(
            context.packageName,
            PackageManager.GET_META_DATA
        )
        val manifestAppId = appInfo.metaData?.getString("com.google.android.gms.ads.APPLICATION_ID")
        assertEquals(expectedTestAppId, manifestAppId)
    }

    @Test
    fun `verify Banner Ad Unit ID uses official test ID in debug and real ID in release`() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val debugBannerId = AdMobConfig.resolveBannerAdUnitId(context, isDebugBuild = true)
        assertEquals("ca-app-pub-3940256099942544/9214589741", debugBannerId)

        val releaseBannerId = AdMobConfig.resolveBannerAdUnitId(context, isDebugBuild = false)
        assertEquals("ca-app-pub-6482896166784785/4207102538", releaseBannerId)

        // Verify Banner AdView can be independently configured with debug ad unit ID
        val adView = AdView(context).apply {
            setAdSize(AdSize.BANNER)
            adUnitId = debugBannerId
        }
        assertEquals(debugBannerId, adView.adUnitId)
        adView.destroy()
    }

    @Test
    fun `verify Native Ad Unit ID uses official test ID in debug and real ID in release`() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val debugNativeId = AdMobConfig.resolveNativeAdUnitId(context, isDebugBuild = true)
        assertEquals("ca-app-pub-3940256099942544/2247696110", debugNativeId)

        val releaseNativeId = AdMobConfig.resolveNativeAdUnitId(context, isDebugBuild = false)
        assertEquals("ca-app-pub-6482896166784785/3831377215", releaseNativeId)
    }

    @Test
    fun `verify NativeAdView XML layout inflates independently with all required asset views and disclosure label`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val inflated = LayoutInflater.from(context)
            .inflate(R.layout.native_ad_transaction_card, null, false)

        assertTrue("Root view must be NativeAdView", inflated is NativeAdView)
        val nativeAdView = inflated as NativeAdView

        val attributionBadge = nativeAdView.findViewById<TextView>(R.id.ad_attribution_badge)
        val headline = nativeAdView.findViewById<TextView>(R.id.ad_headline)
        val advertiser = nativeAdView.findViewById<TextView>(R.id.ad_advertiser)
        val body = nativeAdView.findViewById<TextView>(R.id.ad_body)
        val icon = nativeAdView.findViewById<ImageView>(R.id.ad_app_icon)
        val cta = nativeAdView.findViewById<Button>(R.id.ad_call_to_action)
        val media = nativeAdView.findViewById<MediaView>(R.id.ad_media)

        assertNotNull(attributionBadge)
        assertNotNull(headline)
        assertNotNull(advertiser)
        assertNotNull(body)
        assertNotNull(icon)
        assertNotNull(cta)
        assertNotNull(media)

        val labelText = attributionBadge.text.toString()
        assertTrue(
            "Ad attribution label must clearly contain Ad or Sponsored",
            labelText.contains("Ad", ignoreCase = true) || labelText.contains("Sponsored", ignoreCase = true)
        )

        nativeAdView.headlineView = headline
        nativeAdView.advertiserView = advertiser
        nativeAdView.bodyView = body
        nativeAdView.iconView = icon
        nativeAdView.callToActionView = cta
        nativeAdView.mediaView = media

        // Verify both Dark Mode and Lite Mode theming apply without errors
        applyNativeAdTheme(nativeAdView, isDark = true)
        assertEquals(View.VISIBLE, attributionBadge.visibility)
        applyNativeAdTheme(nativeAdView, isDark = false)
        assertEquals(View.VISIBLE, attributionBadge.visibility)

        nativeAdView.destroy()
    }
}
