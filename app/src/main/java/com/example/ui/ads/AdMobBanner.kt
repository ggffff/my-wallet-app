package com.example.ui.ads

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.theme.LocalAppTheme
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

private const val TAG = "AdMobBanner"

/**
 * Anchored Adaptive Banner Ad for Google AdMob.
 *
 * Features:
 * - Responsive width matching the device orientation and container.
 * - Collapses completely when loading fails or when no ad is available (zero empty space).
 * - Full Android lifecycle management (resume, pause, destroy).
 * - Polished card container matching both Dark Midnight Blue and Lite themes.
 * - Complies with AdMob privacy guidelines (no personal or financial data passed).
 */
@Composable
fun AdMobBanner(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = LocalAppTheme.current.isDark
    val lifecycleOwner = LocalLifecycleOwner.current

    // Read configured banner unit id from AdMobConfig (official Google test banner ID)
    val configuredAdUnitId = remember(context) {
        AdMobConfig.resolveBannerAdUnitId(context)
    }

    var isAdLoaded by remember { mutableStateOf(false) }
    var adViewInstance by remember { mutableStateOf<AdView?>(null) }

    // Tie AdView lifecycle to Android Composable lifecycle
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    try {
                        adViewInstance?.resume()
                    } catch (t: Throwable) {
                        Log.w(TAG, "AdView resume error", t)
                    }
                }
                Lifecycle.Event.ON_PAUSE -> {
                    try {
                        adViewInstance?.pause()
                    } catch (t: Throwable) {
                        Log.w(TAG, "AdView pause error", t)
                    }
                }
                Lifecycle.Event.ON_DESTROY -> {
                    try {
                        adViewInstance?.destroy()
                    } catch (t: Throwable) {
                        Log.w(TAG, "AdView destroy error", t)
                    }
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            try {
                adViewInstance?.destroy()
            } catch (t: Throwable) {
                Log.w(TAG, "AdView destroy onDispose error", t)
            }
            adViewInstance = null
        }
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        val adWidthDp = maxWidth.value.toInt().coerceAtLeast(320)
        val adSize = remember(adWidthDp) {
            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidthDp)
        }

        // Dedicated Card Container: Collapses to 0dp when not loaded, expands cleanly when loaded
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .testTag("admob_banner_container")
                .then(
                    if (!isAdLoaded) Modifier.height(0.dp) else Modifier
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) Color(0xFF0F1E36) else Color(0xFFF8FAFC)
            ),
            border = if (isAdLoaded) {
                BorderStroke(
                    width = 1.dp,
                    color = if (isDark) Color(0xFF1E3A5F).copy(alpha = 0.6f) else Color(0xFFE2E8F0)
                )
            } else null,
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isAdLoaded) (if (isDark) 4.dp else 2.dp) else 0.dp
            )
        ) {
            if (isAdLoaded) {
                Text(
                    text = "SPONSORED",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.8f) else Color(0xFF64748B),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 6.dp, bottom = 4.dp)
                )
            }

            AndroidView(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = if (isAdLoaded) 6.dp else 0.dp),
                factory = { ctx ->
                    AdView(ctx).apply {
                        setAdSize(adSize)
                        adUnitId = configuredAdUnitId
                        adListener = object : AdListener() {
                            override fun onAdLoaded() {
                                super.onAdLoaded()
                                Log.d(TAG, "AdMob Banner successfully loaded")
                                isAdLoaded = true
                            }

                            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                                super.onAdFailedToLoad(loadAdError)
                                Log.w(
                                    TAG,
                                    "AdMob Banner failed to load: code=${loadAdError.code}, message=${loadAdError.message}"
                                )
                                isAdLoaded = false
                            }
                        }
                        adViewInstance = this
                        // Asynchronously request ad without passing financial or user data
                        loadAd(AdRequest.Builder().build())
                    }
                }
            )
        }
    }
}
