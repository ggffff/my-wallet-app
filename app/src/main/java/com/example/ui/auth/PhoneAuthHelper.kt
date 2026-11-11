package com.example.ui.auth

import android.app.Activity
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

object PhoneAuthHelper {
    private const val TAG = "PhoneAuthHelper"

    /**
     * Sends an SMS verification code to the given E.164 phone number (e.g. +8801712345678).
     */
    fun sendVerificationCode(
        activity: Activity,
        phoneNumber: String,
        forceResendingToken: PhoneAuthProvider.ForceResendingToken? = null,
        onCodeSent: (verificationId: String, token: PhoneAuthProvider.ForceResendingToken) -> Unit,
        onAutoVerified: (PhoneAuthCredential) -> Unit,
        onError: (String) -> Unit
    ) {
        val auth = Firebase.auth
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d(TAG, "onVerificationCompleted auto-retrieval successful")
                onAutoVerified(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.e(TAG, "onVerificationFailed: ${e.message}", e)
                val friendlyMessage = when (e) {
                    is FirebaseAuthInvalidCredentialsException -> {
                        "Invalid phone number format. Please ensure it includes the country code (e.g. +880)."
                    }
                    is FirebaseTooManyRequestsException -> {
                        "SMS quota or rate limit exceeded. Please wait a few moments before trying again, or use a test phone number."
                    }
                    else -> {
                        val msg = e.localizedMessage ?: e.javaClass.simpleName
                        if (msg.contains("app not authorized", ignoreCase = true) || msg.contains("reCAPTCHA", ignoreCase = true)) {
                            "Phone verification requires Phone provider enabled in Firebase Console (Authentication -> Sign-in method -> Phone)."
                        } else {
                            msg
                        }
                    }
                }
                onError(friendlyMessage)
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d(TAG, "onCodeSent verificationId: $verificationId")
                onCodeSent(verificationId, token)
            }
        }

        val optionsBuilder = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)

        if (forceResendingToken != null) {
            optionsBuilder.setForceResendingToken(forceResendingToken)
        }

        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
    }

    /**
     * Signs in with the verification ID and SMS OTP code.
     */
    suspend fun signInWithSmsCode(
        verificationId: String,
        smsCode: String,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val credential = PhoneAuthProvider.getCredential(verificationId, smsCode)
            val authResult = Firebase.auth.signInWithCredential(credential).await()
            val user = authResult.user ?: Firebase.auth.currentUser
            if (user != null) {
                onSuccess(user)
            } else {
                onError("Sign-in succeeded but user session could not be established.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "signInWithSmsCode error", e)
            val message = when (e) {
                is FirebaseAuthInvalidCredentialsException -> "Incorrect verification code. Please check your SMS and try again."
                else -> e.localizedMessage ?: "Failed to sign in with verification code."
            }
            onError(message)
        }
    }

    /**
     * Signs in directly when auto-retrieval completed.
     */
    suspend fun signInWithCredential(
        credential: PhoneAuthCredential,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val authResult = Firebase.auth.signInWithCredential(credential).await()
            val user = authResult.user ?: Firebase.auth.currentUser
            if (user != null) {
                onSuccess(user)
            } else {
                onError("Auto-verification succeeded but user session could not be established.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "signInWithCredential error", e)
            onError(e.localizedMessage ?: "Failed to sign in with phone credential.")
        }
    }

    /**
     * Links a verified phone credential to the current logged-in Google user.
     */
    suspend fun linkCurrentAccountWithSmsCode(
        verificationId: String,
        smsCode: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val user = Firebase.auth.currentUser
        if (user == null) {
            onError("No active logged-in user to link phone number to.")
            return
        }

        try {
            val credential = PhoneAuthProvider.getCredential(verificationId, smsCode)
            val linkResult = user.linkWithCredential(credential).await()
            val linkedPhoneNumber = linkResult.user?.phoneNumber.orEmpty()
            onSuccess(linkedPhoneNumber)
        } catch (e: Exception) {
            Log.e(TAG, "linkCurrentAccountWithSmsCode error", e)
            val msg = when (e) {
                is FirebaseAuthUserCollisionException -> {
                    "This mobile number is already linked to another account. You can log in using this mobile number directly to access that account."
                }
                is FirebaseAuthInvalidCredentialsException -> {
                    "Invalid verification code. Please try again."
                }
                else -> e.localizedMessage ?: "Failed to link mobile number."
            }
            onError(msg)
        }
    }

    /**
     * Links direct credential when auto-retrieval completed.
     */
    suspend fun linkCurrentAccountWithCredential(
        credential: PhoneAuthCredential,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val user = Firebase.auth.currentUser
        if (user == null) {
            onError("No active logged-in user to link phone number to.")
            return
        }

        try {
            val linkResult = user.linkWithCredential(credential).await()
            val linkedPhoneNumber = linkResult.user?.phoneNumber.orEmpty()
            onSuccess(linkedPhoneNumber)
        } catch (e: Exception) {
            Log.e(TAG, "linkCurrentAccountWithCredential error", e)
            onError(e.localizedMessage ?: "Failed to link phone credential.")
        }
    }
}
