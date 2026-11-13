package com.example.ui.ads

import android.content.Context
import com.example.BuildConfig
import com.example.R

/**
 * Central AdMob ID configuration resolver.
 *
 * Automatically serves Google's official test AdMob IDs in Debug builds
 * and switches to the production Release AdMob IDs in Release builds.
 */
object AdMobConfig {
    const val OFFICIAL_TEST_APP_ID = "ca-app-pub-3940256099942544~3347511713"
    const val OFFICIAL_TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741"
    const val OFFICIAL_TEST_NATIVE_AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110"

    fun resolveAppId(
        context: Context,
        isDebugBuild: Boolean = BuildConfig.DEBUG
    ): String {
        return try {
            if (isDebugBuild) {
                context.getString(R.string.admob_test_app_id).ifBlank {
                    OFFICIAL_TEST_APP_ID
                }
            } else {
                context.getString(R.string.admob_real_app_id).ifBlank {
                    OFFICIAL_TEST_APP_ID
                }
            }
        } catch (_: Exception) {
            OFFICIAL_TEST_APP_ID
        }
    }

    fun resolveBannerAdUnitId(
        context: Context,
        isDebugBuild: Boolean = BuildConfig.DEBUG
    ): String {
        return try {
            if (isDebugBuild) {
                context.getString(R.string.admob_test_banner_ad_unit_id).ifBlank {
                    OFFICIAL_TEST_BANNER_AD_UNIT_ID
                }
            } else {
                context.getString(R.string.admob_banner_ad_unit_id).ifBlank {
                    OFFICIAL_TEST_BANNER_AD_UNIT_ID
                }
            }
        } catch (_: Exception) {
            OFFICIAL_TEST_BANNER_AD_UNIT_ID
        }
    }

    fun resolveNativeAdUnitId(
        context: Context,
        isDebugBuild: Boolean = BuildConfig.DEBUG
    ): String {
        return try {
            if (isDebugBuild) {
                context.getString(R.string.admob_test_native_ad_unit_id).ifBlank {
                    OFFICIAL_TEST_NATIVE_AD_UNIT_ID
                }
            } else {
                context.getString(R.string.admob_native_ad_unit_id).ifBlank {
                    OFFICIAL_TEST_NATIVE_AD_UNIT_ID
                }
            }
        } catch (_: Exception) {
            OFFICIAL_TEST_NATIVE_AD_UNIT_ID
        }
    }
}
