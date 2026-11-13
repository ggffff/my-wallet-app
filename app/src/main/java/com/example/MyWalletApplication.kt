package com.example

import android.app.Application
import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class MyWalletApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initFirebaseSafely()
        initAdMobSafely()
    }

    private fun initAdMobSafely() {
        if (!adMobInitStarted.compareAndSet(false, true)) {
            Log.d(TAG, "AdMob MobileAds initialization already triggered; skipping duplicate call")
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            try {
                MobileAds.initialize(this@MyWalletApplication) { status ->
                    Log.d(TAG, "AdMob MobileAds initialized successfully: ${status.adapterStatusMap.keys}")
                }
            } catch (t: Throwable) {
                adMobInitStarted.set(false)
                Log.e(TAG, "AdMob initialization encountered an exception", t)
            }
        }
    }

    private fun initFirebaseSafely() {
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val options = FirebaseOptions.fromResource(this)
                if (options != null) {
                    FirebaseApp.initializeApp(this, options)
                    Log.d(TAG, "Firebase initialized via fromResource")
                } else {
                    // Fallback using legitimate parameters from google-services.json
                    val fallbackOptions = FirebaseOptions.Builder()
                        .setApplicationId("1:995031776795:android:3f95076008bebc0a86c2be")
                        .setApiKey("AIzaSyBncnZeP6cxbxBSVGOGHAhgC7MroFDYWLw")
                        .setProjectId("gen-lang-client-0425667793")
                        .setStorageBucket("gen-lang-client-0425667793.firebasestorage.app")
                        .setGcmSenderId("995031776795")
                        .build()
                    FirebaseApp.initializeApp(this, fallbackOptions)
                    Log.d(TAG, "Firebase initialized via fallback FirebaseOptions")
                }
            } else {
                Log.d(TAG, "FirebaseApp already initialized by FirebaseInitProvider")
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Firebase initialization encountered an exception", t)
        }
    }

    companion object {
        private const val TAG = "MyWalletApplication"
        private val adMobInitStarted = AtomicBoolean(false)
    }
}
