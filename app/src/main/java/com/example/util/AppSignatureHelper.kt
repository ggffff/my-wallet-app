package com.example.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import java.security.MessageDigest

object AppSignatureHelper {
    const val EXPECTED_SHA1 = "CE:C8:04:28:58:3D:82:05:D6:3A:9F:D0:55:DB:E2:0D:6C:E6:C1:D3"
    const val EXPECTED_SHA256 = "99:25:B4:22:AC:B6:F3:2D:20:36:DE:29:C0:DE:EC:6D:C8:97:CB:79:D3:6C:6A:7B:26:41:D4:6A:64:AC:0E:38"

    fun getAppSignature(context: Context): Pair<String, String>? {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                )
            }

            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }

            val firstSig = signatures?.firstOrNull() ?: return null
            val certBytes = firstSig.toByteArray()
            val sha1 = MessageDigest.getInstance("SHA-1").digest(certBytes)
                .joinToString(":") { "%02X".format(it) }
            val sha256 = MessageDigest.getInstance("SHA-256").digest(certBytes)
                .joinToString(":") { "%02X".format(it) }
            Pair(sha1, sha256)
        } catch (e: Exception) {
            Log.e("AppSignatureHelper", "Failed to retrieve app signature fingerprints", e)
            null
        }
    }
}
