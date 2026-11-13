package com.example.ui.ads

import android.app.Activity
import android.util.Log
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

object AdMobConsentManager {
    private const val TAG = "AdMobConsentManager"

    /**
     * Gathers user consent using the Google User Messaging Platform (UMP) SDK.
     * Complies with Google AdMob consent policies and EU User Consent Policy.
     */
    fun requestConsentIfRequired(activity: Activity, onConsentCompleted: () -> Unit = {}) {
        try {
            val params = ConsentRequestParameters.Builder()
                .setTagForUnderAgeOfConsent(false)
                .build()

            val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
            consentInformation.requestConsentInfoUpdate(
                activity,
                params,
                {
                    UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                        if (formError != null) {
                            Log.w(TAG, "Consent form dismiss/error: ${formError.errorCode} - ${formError.message}")
                        }
                        if (consentInformation.canRequestAds()) {
                            onConsentCompleted()
                        }
                    }
                },
                { requestConsentError ->
                    Log.w(TAG, "Consent info update failed: ${requestConsentError.errorCode} - ${requestConsentError.message}")
                    if (consentInformation.canRequestAds()) {
                        onConsentCompleted()
                    }
                }
            )

            if (consentInformation.canRequestAds()) {
                onConsentCompleted()
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Consent collection encountered an error", t)
            onConsentCompleted()
        }
    }
}
