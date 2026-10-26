package com.example.ui.auth

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.R
import com.example.ui.theme.BrandGlow
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.IncomeGreen
import com.example.util.AppSignatureHelper
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private fun Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    try {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard?.setPrimaryClip(clip)
        Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Log.e("Auth", "Failed to copy to clipboard", e)
    }
}

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDiagnostics by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val runtimeSig = remember { AppSignatureHelper.getAppSignature(context) }
    val currentSha1 = runtimeSig?.first ?: AppSignatureHelper.EXPECTED_SHA1
    val currentSha256 = runtimeSig?.second ?: AppSignatureHelper.EXPECTED_SHA256
    val isKeyMatching = currentSha1.equals(AppSignatureHelper.EXPECTED_SHA1, ignoreCase = true)

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("auth_screen"),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // App Logo
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            Brush.radialGradient(
                                listOf(BrandGlow.copy(alpha = 0.35f), Color.Transparent)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_icon_fg),
                        contentDescription = "My Wallet App Icon",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(20.dp))
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "My Wallet",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Your Smart Personal Financial Companion",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                )

                // Feature Highlights Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FeatureRow(
                            icon = Icons.Default.AccountBalanceWallet,
                            tint = BrandPrimary,
                            title = "Net Balance & Accounts",
                            subtitle = "Bank, Cash, bKash, Nagad, Rocket in one place"
                        )
                        FeatureRow(
                            icon = Icons.Default.Savings,
                            tint = IncomeGreen,
                            title = "Saving Goals & Bills",
                            subtitle = "Save for purchases and get bill due reminders"
                        )
                        FeatureRow(
                            icon = Icons.Default.Lock,
                            tint = BrandGlow,
                            title = "Private & Secure Cloud",
                            subtitle = "Zero-trust Google account sync with BDT currency"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Error Feedback Card
                if (errorMessage != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f),
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = "Error",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Google Sign-In Diagnostic",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            SelectionContainer {
                                Text(
                                    text = errorMessage ?: "",
                                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = {
                                        copyToClipboard(
                                            context,
                                            "SHA Fingerprints",
                                            "SHA-1: $currentSha1\nSHA-256: $currentSha256"
                                        )
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy SHA Keys", fontSize = 12.sp)
                                }
                                TextButton(
                                    onClick = { errorMessage = null }
                                ) {
                                    Text(
                                        text = "Dismiss",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                }

                // Google Sign In Button
                Button(
                    onClick = {
                        isLoading = true
                        errorMessage = null
                        signInWithGoogle(
                            context = context,
                            scope = coroutineScope,
                            onSuccess = {
                                isLoading = false
                                onAuthSuccess()
                            },
                            onError = { err ->
                                isLoading = false
                                errorMessage = err
                            },
                            onCancelled = {
                                isLoading = false
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("google_sign_in_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Connecting to Google...",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Sign in with Google",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Firebase Fingerprint Diagnostics Section
                OutlinedButton(
                    onClick = { showDiagnostics = !showDiagnostics },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (showDiagnostics) "Hide Firebase Setup Info" else "View Firebase Setup & SHA Keys",
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                if (showDiagnostics) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Firebase Project Configuration",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Application ID: ${context.packageName}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Project: gen-lang-client-0425667793",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Signing Key Status: ${if (isKeyMatching) "✓ Matches expected debug key" else "⚠️ Custom keystore detected"}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isKeyMatching) IncomeGreen else MaterialTheme.colorScheme.error
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Installed APK SHA-1:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            SelectionContainer {
                                Text(
                                    text = currentSha1,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "Installed APK SHA-256:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            SelectionContainer {
                                Text(
                                    text = currentSha256,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Button(
                                onClick = {
                                    copyToClipboard(
                                        context,
                                        "Firebase SHA Keys",
                                        "SHA-1:\n$currentSha1\n\nSHA-256:\n$currentSha256"
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Copy Fingerprints to Clipboard")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    title: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(tint.copy(alpha = 0.15f), shape = RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

internal fun getWebClientId(context: Context): String? {
    // 1. Dynamic reflection lookup on generated R.string class.
    try {
        val rClass = Class.forName("com.example.R\$string")
        val field = rClass.getField("default_web_client_id")
        val resId = field.getInt(null)
        if (resId != 0) {
            val clientId = context.getString(resId)
            if (clientId.isNotBlank()) {
                return clientId
            }
        }
    } catch (_: Throwable) {
        // Fall through to package lookup
    }

    try {
        val rClass = Class.forName("${context.packageName}.R\$string")
        val field = rClass.getField("default_web_client_id")
        val resId = field.getInt(null)
        if (resId != 0) {
            val clientId = context.getString(resId)
            if (clientId.isNotBlank()) {
                return clientId
            }
        }
    } catch (_: Throwable) {
        // Fall through to Resources.getIdentifier
    }

    // 2. Resource identifier lookup via application package name
    try {
        val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        if (resId != 0) {
            val clientId = context.getString(resId)
            if (clientId.isNotBlank()) {
                return clientId
            }
        }
    } catch (_: Throwable) {}

    // 3. Resource identifier lookup via namespace "com.example"
    try {
        val resId = context.resources.getIdentifier("default_web_client_id", "string", "com.example")
        if (resId != 0) {
            val clientId = context.getString(resId)
            if (clientId.isNotBlank()) {
                return clientId
            }
        }
    } catch (_: Throwable) {}

    // 4. Resource identifier lookup via alternative resource name "web_client_id"
    try {
        val resId = context.resources.getIdentifier("web_client_id", "string", context.packageName)
        if (resId != 0) {
            val clientId = context.getString(resId)
            if (clientId.isNotBlank()) {
                return clientId
            }
        }
    } catch (_: Throwable) {}

    return null
}

private fun signInWithGoogle(
    context: Context,
    scope: CoroutineScope,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
    onCancelled: () -> Unit
) {
    val activity = context.findActivity()
    if (activity == null) {
        Log.e("Auth", "Unable to resolve Activity from context")
        onError("Unable to launch Google Sign-In: Application activity not found.")
        return
    }

    val clientId = getWebClientId(context)
    if (clientId.isNullOrBlank()) {
        Log.e("Auth", "Web Client ID (default_web_client_id) not found")
        onError("Google Sign-In configuration missing: Web OAuth Client ID (default_web_client_id) was not found in google-services resources. Please ensure Google Sign-In is enabled in Firebase Console and a valid google-services.json is present.")
        return
    }

    Log.d("Auth", "Using Web Client ID prefix: ${clientId.take(12)}... (valid serverClientId)")

    val credentialManager = try {
        CredentialManager.create(context)
    } catch (t: Throwable) {
        Log.e("Auth", "Failed to initialize CredentialManager", t)
        onError("Google Sign-In service not available on this device: ${t.localizedMessage ?: t.javaClass.simpleName}")
        return
    }

    val signInOption = try {
        GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    } catch (t: Throwable) {
        Log.e("Auth", "Failed to create GetSignInWithGoogleOption", t)
        onError("Failed to initialize Google Sign-In options: ${t.localizedMessage}")
        return
    }

    val request = try {
        GetCredentialRequest.Builder().addCredentialOption(signInOption).build()
    } catch (t: Throwable) {
        Log.e("Auth", "Failed to create GetCredentialRequest", t)
        onError("Failed to create credential request: ${t.localizedMessage}")
        return
    }

    scope.launch {
        try {
            Log.d("Auth", "Requesting credential from CredentialManager...")
            val result = credentialManager.getCredential(activity, request)
            val credential = result.credential
            Log.d("Auth", "Credential returned of type: ${credential.type}")

            val idToken: String? = when {
                credential is CustomCredential && (
                    credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL ||
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_SIWG_CREDENTIAL
                ) -> {
                    try {
                        GoogleIdTokenCredential.createFrom(credential.data).idToken
                    } catch (e: Exception) {
                        Log.w("Auth", "GoogleIdTokenCredential.createFrom failed, reading bundle directly", e)
                        credential.data.getString("com.google.android.libraries.identity.googleid.BUNDLE_KEY_ID_TOKEN")
                            ?: credential.data.getString("id_token")
                    }
                }
                credential is CustomCredential -> {
                    try {
                        GoogleIdTokenCredential.createFrom(credential.data).idToken
                    } catch (e: Exception) {
                        Log.w("Auth", "Fallback bundle lookup for custom credential: ${credential.type}", e)
                        credential.data.getString("com.google.android.libraries.identity.googleid.BUNDLE_KEY_ID_TOKEN")
                            ?: credential.data.getString("id_token")
                    }
                }
                else -> null
            }

            if (idToken.isNullOrBlank()) {
                Log.e("Auth", "Credential did not provide an ID token. Credential type: ${credential.type}")
                onError("Google account was selected, but no ID token was received. Please ensure Google Sign-In is enabled in Firebase Console.")
                return@launch
            }

            Log.d("Auth", "Google ID token obtained (token length: ${idToken.length}). Signing into Firebase...")
            val authCredential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = Firebase.auth.signInWithCredential(authCredential).await()
            val user = authResult.user ?: Firebase.auth.currentUser

            if (user != null) {
                Log.d("Auth", "Firebase authentication succeeded for user: ${user.uid}")
                onSuccess()
            } else {
                Log.e("Auth", "signInWithCredential returned null user")
                onError("Authentication succeeded, but Firebase user profile is null.")
            }
        } catch (e: GetCredentialCancellationException) {
            val rawMsg = e.message ?: "Sign-in dismissed"
            Log.w("Auth", "Google Sign-In flow cancelled or dismissed: $rawMsg", e)
            val runtimeSig = AppSignatureHelper.getAppSignature(context)
            val currentSha1 = runtimeSig?.first ?: AppSignatureHelper.EXPECTED_SHA1
            val currentSha256 = runtimeSig?.second ?: AppSignatureHelper.EXPECTED_SHA256
            val matchesExpected = currentSha1.equals(AppSignatureHelper.EXPECTED_SHA1, ignoreCase = true)

            val detailMsg = buildString {
                append("Sign-in bottom sheet was closed or dismissed ($rawMsg).\n\n")
                if (!matchesExpected) {
                    append("⚠️ SIGNING KEY MISMATCH DETECTED:\n")
                    append("This installed APK was signed with:\nSHA-1: $currentSha1\n\n")
                    append("Expected in google-services.json:\nSHA-1: ${AppSignatureHelper.EXPECTED_SHA1}\n\n")
                    append("Action Required: Please register this APK's SHA-1 ($currentSha1) in Firebase Console under Project Settings > Your apps.\n")
                } else {
                    append("Important: If the account picker closed immediately after you tapped your Gmail account, Google Play Services rejected the signing certificate (Status Code 10: Developer Error).\n\n")
                    append("How to resolve in Firebase Console:\n")
                    append("1. Open Firebase Console -> Project Settings -> General -> Your apps -> com.aistudio.mywallet.bdtnas\n")
                    append("2. Add SHA-1:\n   $currentSha1\n")
                    append("3. Add SHA-256:\n   $currentSha256\n")
                    append("4. In Authentication -> Sign-in method, ensure Google is Enabled.\n")
                }
            }
            onError(detailMsg)
        } catch (e: GetCredentialException) {
            Log.e("Auth", "GetCredentialException during Google Sign-In", e)
            val rawMsg = e.message ?: e.javaClass.simpleName
            val runtimeSig = AppSignatureHelper.getAppSignature(context)
            val currentSha1 = runtimeSig?.first ?: AppSignatureHelper.EXPECTED_SHA1
            val currentSha256 = runtimeSig?.second ?: AppSignatureHelper.EXPECTED_SHA256

            val friendlyMsg = when {
                rawMsg.contains("10") || rawMsg.contains("DeveloperError") ->
                    "Google Play Services Developer Error (10): APK signing certificate is not recognized in Firebase Console.\n\nAdd both to Firebase Console:\nSHA-1: $currentSha1\nSHA-256: $currentSha256"
                rawMsg.contains("16") || rawMsg.contains("Cannot find matching credential") ->
                    "Google Sign-In configuration error: Ensure Google provider is enabled in Firebase Authentication console."
                rawMsg.contains("7") || rawMsg.contains("NETWORK", ignoreCase = true) ->
                    "Network error: Unable to contact Google authentication servers. Please check your internet connection."
                rawMsg.contains("NoCredentialException") ->
                    "No Google account available or selected on this device."
                else ->
                    "Google Sign-In failed: $rawMsg"
            }
            onError(friendlyMsg)
        } catch (e: FirebaseNetworkException) {
            Log.e("Auth", "Firebase network error", e)
            onError("Network error: Could not reach Firebase servers. Please check your internet connection and try again.")
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Log.e("Auth", "Invalid Firebase credentials", e)
            onError("Authentication error: The Google ID token could not be verified by Firebase. Ensure Google Sign-In is enabled in Firebase Console.")
        } catch (e: FirebaseAuthException) {
            Log.e("Auth", "Firebase Auth error [${e.errorCode}]", e)
            onError("Firebase Authentication error (${e.errorCode}): ${e.localizedMessage ?: "Please try again"}")
        } catch (t: Throwable) {
            Log.e("Auth", "Unexpected error during Google Sign-In", t)
            onError(t.localizedMessage ?: "Sign in failed: ${t.javaClass.simpleName}")
        }
    }
}


