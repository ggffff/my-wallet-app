package com.example.ui.ads

import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.R
import com.example.ui.theme.LocalAppTheme
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView

private const val NATIVE_AD_TAG = "AdMobNativeAd"

/**
 * Loads and manages the lifecycle of a single [NativeAd] for the Home Screen.
 *
 * - Automatically destroys the [NativeAd] when the caller leaves composition or the Activity is destroyed.
 * - Guards against async callbacks completing after disposal.
 * - Requests ads without any financial, balance, or personal transaction data.
 */
@Composable
fun rememberHomeNativeAd(): NativeAd? {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val adUnitId = remember(context) { AdMobConfig.resolveNativeAdUnitId(context) }

    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }

    DisposableEffect(context, adUnitId, lifecycleOwner) {
        var isDisposed = false
        var currentAd: NativeAd? = null

        val videoOptions = VideoOptions.Builder()
            .setStartMuted(true)
            .build()

        val nativeAdOptions = NativeAdOptions.Builder()
            .setVideoOptions(videoOptions)
            .build()

        try {
            val adLoader = AdLoader.Builder(context, adUnitId)
                .forNativeAd { loadedAd ->
                    if (isDisposed) {
                        loadedAd.destroy()
                        return@forNativeAd
                    }
                    // Recycle any previously held NativeAd before replacing
                    currentAd?.destroy()
                    currentAd = loadedAd
                    nativeAd = loadedAd
                    Log.d(NATIVE_AD_TAG, "Native Ad loaded successfully")
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Log.w(
                            NATIVE_AD_TAG,
                            "Native Ad failed to load: code=${adError.code}, message=${adError.message}"
                        )
                    }
                })
                .withNativeAdOptions(nativeAdOptions)
                .build()

            // Request ad strictly without personal or financial parameters
            adLoader.loadAd(AdRequest.Builder().build())
        } catch (t: Throwable) {
            Log.e(NATIVE_AD_TAG, "Error initializing Native AdLoader", t)
        }

        val lifecycleObserver = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                isDisposed = true
                currentAd?.destroy()
                currentAd = null
                nativeAd = null
            }
        }
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

        onDispose {
            isDisposed = true
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
            try {
                currentAd?.destroy()
            } catch (t: Throwable) {
                Log.w(NATIVE_AD_TAG, "Error destroying NativeAd onDispose", t)
            }
            currentAd = null
            nativeAd = null
        }
    }

    return nativeAd
}

/**
 * Renders a loaded [NativeAd] inside a Material 3 Card styled to blend naturally
 * between Transaction History items in both Midnight Blue Dark Mode and Lite Mode.
 *
 * Uses official [NativeAdView] and registers all asset views properly.
 * If [nativeAd] is null (loading or failed), nothing is rendered (zero empty space).
 */
@Composable
fun AdMobNativeAdCard(
    nativeAd: NativeAd?,
    modifier: Modifier = Modifier
) {
    if (nativeAd == null) return

    val isDark = LocalAppTheme.current.isDark

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp)
            .testTag("admob_native_ad_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF0F1E36) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isDark) {
                Color(0xFF1E3A5F).copy(alpha = 0.7f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            }
        )
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { ctx ->
                val inflater = LayoutInflater.from(ctx)
                val adView = inflater.inflate(
                    R.layout.native_ad_transaction_card,
                    null,
                    false
                ) as NativeAdView

                adView.headlineView = adView.findViewById(R.id.ad_headline)
                adView.bodyView = adView.findViewById(R.id.ad_body)
                adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
                adView.iconView = adView.findViewById(R.id.ad_app_icon)
                adView.advertiserView = adView.findViewById(R.id.ad_advertiser)
                adView.mediaView = adView.findViewById<MediaView>(R.id.ad_media)

                adView.findViewById<ImageView>(R.id.ad_app_icon)?.apply {
                    outlineProvider = ViewOutlineProvider.BACKGROUND
                    clipToOutline = true
                }
                adView.findViewById<MediaView>(R.id.ad_media)?.apply {
                    outlineProvider = ViewOutlineProvider.BACKGROUND
                    clipToOutline = true
                }

                adView
            },
            update = { adView ->
                if (adView.getTag(R.id.ad_attribution_badge) != isDark) {
                    applyNativeAdTheme(adView, isDark)
                    adView.setTag(R.id.ad_attribution_badge, isDark)
                }
                if (adView.tag !== nativeAd) {
                    bindNativeAdAssets(nativeAd, adView)
                    adView.tag = nativeAd
                }
            },
            onRelease = { adView ->
                // Do NOT call adView.destroy() when scrolling off-screen in LazyColumn,
                // as rememberHomeNativeAd() owns and destroys the NativeAd when leaving HomeScreen.
                adView.tag = null
            }
        )
    }
}

internal fun applyNativeAdTheme(adView: NativeAdView, isDark: Boolean) {
    val density = adView.resources.displayMetrics.density

    val headlineColor = if (isDark) Color(0xFFF8FAFC).toArgb() else Color(0xFF0F172A).toArgb()
    val secondaryTextColor = if (isDark) Color(0xFF94A3B8).toArgb() else Color(0xFF64748B).toArgb()
    val badgeBgColor = if (isDark) Color(0xFF1E3A5F).toArgb() else Color(0xFFE0F2FE).toArgb()
    val badgeTextColor = if (isDark) Color(0xFF38BDF8).toArgb() else Color(0xFF0369A1).toArgb()
    val iconBgColor = if (isDark) Color(0xFF162B4D).toArgb() else Color(0xFFF1F5F9).toArgb()
    val ctaBgColor = if (isDark) Color(0xFF0284C7).toArgb() else Color(0xFF0052FE).toArgb()
    val ctaTextColor = Color.White.toArgb()

    adView.findViewById<TextView>(R.id.ad_attribution_badge)?.apply {
        setTextColor(badgeTextColor)
        background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 8f * density
            setColor(badgeBgColor)
        }
    }

    adView.findViewById<ImageView>(R.id.ad_app_icon)?.apply {
        background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 10f * density
            setColor(iconBgColor)
        }
    }

    adView.findViewById<TextView>(R.id.ad_headline)?.setTextColor(headlineColor)
    adView.findViewById<TextView>(R.id.ad_advertiser)?.setTextColor(secondaryTextColor)
    adView.findViewById<TextView>(R.id.ad_body)?.setTextColor(secondaryTextColor)

    adView.findViewById<Button>(R.id.ad_call_to_action)?.apply {
        setTextColor(ctaTextColor)
        background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 12f * density
            setColor(ctaBgColor)
        }
    }

    adView.findViewById<MediaView>(R.id.ad_media)?.apply {
        background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 12f * density
            setColor(iconBgColor)
        }
    }
}

internal fun bindNativeAdAssets(nativeAd: NativeAd, adView: NativeAdView) {
    val headlineView = adView.headlineView as? TextView
    val bodyView = adView.bodyView as? TextView
    val callToActionView = adView.callToActionView as? Button
    val iconView = adView.iconView as? ImageView
    val advertiserView = adView.advertiserView as? TextView
    val mediaView = adView.mediaView

    headlineView?.text = nativeAd.headline

    if (nativeAd.body.isNullOrBlank()) {
        bodyView?.visibility = View.GONE
    } else {
        bodyView?.visibility = View.VISIBLE
        bodyView?.text = nativeAd.body
    }

    if (nativeAd.callToAction.isNullOrBlank()) {
        callToActionView?.visibility = View.GONE
    } else {
        callToActionView?.visibility = View.VISIBLE
        callToActionView?.text = nativeAd.callToAction
    }

    val icon = nativeAd.icon
    if (icon == null) {
        iconView?.visibility = View.GONE
    } else {
        iconView?.visibility = View.VISIBLE
        iconView?.setImageDrawable(icon.drawable)
    }

    if (nativeAd.advertiser.isNullOrBlank()) {
        advertiserView?.visibility = View.GONE
    } else {
        advertiserView?.visibility = View.VISIBLE
        advertiserView?.text = nativeAd.advertiser
    }

    val mediaContent = nativeAd.mediaContent
    if (mediaContent == null) {
        mediaView?.visibility = View.GONE
    } else {
        mediaView?.visibility = View.VISIBLE
        mediaView?.mediaContent = mediaContent
    }

    // Register NativeAd with NativeAdView last after populating all asset views
    adView.setNativeAd(nativeAd)
}
