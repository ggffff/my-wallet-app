package com.example.ui.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalAppTheme
import com.example.util.LocalAppLanguage
import com.example.util.StringKeys
import com.example.util.appString
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class PhoneAuthDialogMode {
    SIGN_IN,
    LINK_ACCOUNT
}

private fun Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}

/**
 * Modern, fintech-grade dialog for:
 * 1. Mobile Number + SMS OTP Login & Account Recovery (Mode.SIGN_IN)
 * 2. Linking verified mobile number to existing account (Mode.LINK_ACCOUNT)
 */
@Composable
fun PhoneOtpAuthDialog(
    mode: PhoneAuthDialogMode = PhoneAuthDialogMode.SIGN_IN,
    onDismiss: () -> Unit,
    onSuccess: (phoneNumber: String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val isDark = LocalAppTheme.current.isDark

    // Form states
    var countryCode by remember { mutableStateOf("+880") } // Default Bangladesh
    var rawPhoneNumber by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf<String?>(null) }
    var resendToken by remember { mutableStateOf<PhoneAuthProvider.ForceResendingToken?>(null) }
    var otpCode by remember { mutableStateOf("") }

    var isSendingOtp by remember { mutableStateOf(false) }
    var isVerifyingOtp by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var countdownSeconds by remember { mutableIntStateOf(0) }

    // Countdown timer for OTP resend
    LaunchedEffect(countdownSeconds) {
        if (countdownSeconds > 0) {
            delay(1000L)
            countdownSeconds--
        }
    }

    val fullFormattedPhoneNumber = remember(countryCode, rawPhoneNumber) {
        val trimmed = rawPhoneNumber.trim().trimStart('0')
        "$countryCode$trimmed"
    }

    val activity = remember(context) { context.findActivity() }

    val handleSendOtp: () -> Unit = {
        if (activity == null) {
            errorMessage = "Unable to start verification: Activity context not found."
        } else if (rawPhoneNumber.trim().length < 6) {
            errorMessage = "Please enter a valid mobile number."
        } else {
            isSendingOtp = true
            errorMessage = null
            focusManager.clearFocus()

            PhoneAuthHelper.sendVerificationCode(
                activity = activity,
                phoneNumber = fullFormattedPhoneNumber,
                forceResendingToken = resendToken,
                onCodeSent = { vId, token ->
                    isSendingOtp = false
                    verificationId = vId
                    resendToken = token
                    countdownSeconds = 60
                },
                onAutoVerified = { credential ->
                    isSendingOtp = false
                    coroutineScope.launch {
                        if (mode == PhoneAuthDialogMode.SIGN_IN) {
                            PhoneAuthHelper.signInWithCredential(
                                credential = credential,
                                onSuccess = { user ->
                                    onSuccess(user.phoneNumber ?: fullFormattedPhoneNumber)
                                },
                                onError = { err -> errorMessage = err }
                            )
                        } else {
                            PhoneAuthHelper.linkCurrentAccountWithCredential(
                                credential = credential,
                                onSuccess = { phone ->
                                    onSuccess(phone.ifBlank { fullFormattedPhoneNumber })
                                },
                                onError = { err -> errorMessage = err }
                            )
                        }
                    }
                },
                onError = { err ->
                    isSendingOtp = false
                    errorMessage = err
                }
            )
        }
    }

    val handleVerifyOtp: () -> Unit = {
        val currentVerificationId = verificationId
        if (currentVerificationId.isNullOrBlank()) {
            errorMessage = "Verification session expired. Please request a new OTP code."
        } else if (otpCode.trim().length < 6) {
            errorMessage = "Please enter the complete 6-digit OTP code."
        } else {
            isVerifyingOtp = true
            errorMessage = null
            focusManager.clearFocus()

            coroutineScope.launch {
                if (mode == PhoneAuthDialogMode.SIGN_IN) {
                    PhoneAuthHelper.signInWithSmsCode(
                        verificationId = currentVerificationId,
                        smsCode = otpCode.trim(),
                        onSuccess = { user ->
                            isVerifyingOtp = false
                            onSuccess(user.phoneNumber ?: fullFormattedPhoneNumber)
                        },
                        onError = { err ->
                            isVerifyingOtp = false
                            errorMessage = err
                        }
                    )
                } else {
                    PhoneAuthHelper.linkCurrentAccountWithSmsCode(
                        verificationId = currentVerificationId,
                        smsCode = otpCode.trim(),
                        onSuccess = { phone ->
                            isVerifyingOtp = false
                            onSuccess(phone.ifBlank { fullFormattedPhoneNumber })
                        },
                        onError = { err ->
                            isVerifyingOtp = false
                            errorMessage = err
                        }
                    )
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("phone_otp_dialog"),
        shape = RoundedCornerShape(26.dp),
        containerColor = if (isDark) Color(0xFF0F1E3D) else Color.White,
        titleContentColor = if (isDark) Color.White else Color(0xFF0F172A),
        textContentColor = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF1E3A8A) else Color(0xFFDBEAFE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (verificationId == null) Icons.Default.Phone else Icons.Default.Sms,
                            contentDescription = null,
                            tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF0066FF),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (mode == PhoneAuthDialogMode.SIGN_IN) {
                            if (verificationId == null) appString(StringKeys.SIGN_IN_WITH_PHONE) else appString(StringKeys.ENTER_OTP)
                        } else {
                            if (verificationId == null) appString(StringKeys.LINK_PHONE) else appString(StringKeys.ENTER_OTP)
                        },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isDark) Color.White else Color(0xFF0F172A)
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                if (verificationId == null) {
                    // Step 1: Mobile Number Input
                    Text(
                        text = if (mode == PhoneAuthDialogMode.SIGN_IN) {
                            "Enter your mobile number to sign in or recover your wallet account with an SMS OTP code."
                        } else {
                            "Link a verified mobile number to enable instant account recovery and seamless SMS OTP access."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Country Code Pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isDark) Color(0xFF15284F) else Color(0xFFF1F5F9),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isDark) Color(0xFF1E3566) else Color(0xFFCBD5E1)),
                            modifier = Modifier.height(56.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "🇧🇩", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = countryCode,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.White else Color(0xFF0F172A),
                                    fontSize = 15.sp
                                )
                            }
                        }

                        // Phone Number Input
                        OutlinedTextField(
                            value = rawPhoneNumber,
                            onValueChange = { input ->
                                val digitsOnly = input.filter { it.isDigit() }
                                if (digitsOnly.length <= 11) {
                                    rawPhoneNumber = digitsOnly
                                }
                            },
                            placeholder = { Text("17XXXXXXXX", fontSize = 15.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { handleSendOtp() }),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("phone_number_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0070F3),
                                unfocusedBorderColor = if (isDark) Color(0xFF1E3566) else Color(0xFFCBD5E1),
                                focusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                                unfocusedTextColor = if (isDark) Color.White else Color(0xFF0F172A)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Full number: $fullFormattedPhoneNumber",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
                    )
                } else {
                    // Step 2: 6-Digit SMS OTP Input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { verificationId = null },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF0066FF)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Code sent to $fullFormattedPhoneNumber",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = if (isDark) Color.White else Color(0xFF0F172A)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = otpCode,
                        onValueChange = { input ->
                            val digits = input.filter { it.isDigit() }
                            if (digits.length <= 6) {
                                otpCode = digits
                                if (digits.length == 6) {
                                    // Trigger verification automatically when 6 digits are typed
                                    handleVerifyOtp()
                                }
                            }
                        },
                        placeholder = { Text("● ● ● ● ● ●", fontSize = 16.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { handleVerifyOtp() }),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("otp_code_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0070F3),
                            unfocusedBorderColor = if (isDark) Color(0xFF1E3566) else Color(0xFFCBD5E1),
                            focusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                            unfocusedTextColor = if (isDark) Color.White else Color(0xFF0F172A)
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (countdownSeconds > 0) {
                            Text(
                                text = "Resend in ${countdownSeconds}s",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                            )
                        } else {
                            TextButton(
                                onClick = handleSendOtp,
                                enabled = !isSendingOtp && !isVerifyingOtp,
                                modifier = Modifier.testTag("resend_otp_button")
                            ) {
                                Text(
                                    text = appString(StringKeys.RESEND_OTP),
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0066FF)
                                )
                            }
                        }
                    }
                }

                // Error feedback banner
                AnimatedVisibility(visible = errorMessage != null) {
                    Column {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x33EF4444))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = errorMessage.orEmpty(),
                                color = Color(0xFFEF4444),
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (verificationId == null) {
                Button(
                    onClick = handleSendOtp,
                    enabled = !isSendingOtp && rawPhoneNumber.trim().length >= 6,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0070F3),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("send_otp_button")
                ) {
                    if (isSendingOtp) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(appString(StringKeys.SEND_OTP))
                }
            } else {
                Button(
                    onClick = handleVerifyOtp,
                    enabled = !isVerifyingOtp && otpCode.trim().length >= 6,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("verify_otp_button")
                ) {
                    if (isVerifyingOtp) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(appString(StringKeys.VERIFY_OTP))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = appString(StringKeys.CANCEL),
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                )
            }
        }
    )
}
