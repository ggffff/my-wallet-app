package com.example.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.ui.motion.bounceClick
import com.example.ui.theme.LocalAppTheme

/**
 * Premium Blue & White "Add New Account" Floating Card Dialog.
 * Compact floating card modal ("শর্ট স্কিন") that overlays on top of the home screen.
 */
@Composable
fun AddAccountDialog(
    onDismiss: () -> Unit,
    onConfirm: (Account) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        val activeTheme = LocalAppTheme.current
        val isDark = activeTheme.isDark
        val focusManager = LocalFocusManager.current
        val scrollState = rememberScrollState()

        var name by remember { mutableStateOf("") }
        var selectedType by remember { mutableStateOf(AccountType.BANK) }
        var accountNumber by remember { mutableStateOf("") }
        var initialBalanceText by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var isSubmitting by remember { mutableStateOf(false) }

        var isNameFocused by remember { mutableStateOf(false) }
        var isAccountNoFocused by remember { mutableStateOf(false) }
        var isBalanceFocused by remember { mutableStateOf(false) }

        val cardBg = if (isDark) Color(0xFF0B1736) else Color.White
        val cardBorder = if (isDark) Color(0xFF1E3A8A) else Color(0xFFE2E8F0)
        val textPrimary = if (isDark) Color.White else Color(0xFF080F20)
        val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
        val brandBlue = Color(0xFF0066FF)
        val cyanAccent = Color(0xFF38BDF8)
        val activeAccent = if (isDark) cyanAccent else brandBlue
        val badgeBg = if (isDark) Color(0xFF132854) else Color(0xFFEBF4FF)

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .heightIn(max = 640.dp)
                .shadow(
                    elevation = if (isDark) 12.dp else 8.dp,
                    shape = RoundedCornerShape(28.dp),
                    spotColor = if (isDark) Color(0xFF0052FE).copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.12f)
                )
                .testTag("add_account_dialog"),
            shape = RoundedCornerShape(28.dp),
            color = cardBg,
            border = BorderStroke(1.2.dp, cardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(20.dp)
            ) {
                // TOP HEADER: Title + 3D Logo + Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Add New Account",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                letterSpacing = 0.2.sp
                            ),
                            color = textPrimary,
                            modifier = Modifier.testTag("add_account_title")
                        )
                    }

                    // 3D Wallet Logo with '+' badge
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .padding(end = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_wallet_3d_logo),
                            contentDescription = "Wallet Logo",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(54.dp)
                        )
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            shadowElevation = 3.dp,
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .size(20.dp)
                                .align(Alignment.BottomEnd)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = brandBlue,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }

                    // Close Button (X)
                    Surface(
                        onClick = onDismiss,
                        shape = CircleShape,
                        color = badgeBg,
                        modifier = Modifier
                            .size(36.dp)
                            .bounceClick()
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 3 ACCOUNT CATEGORIES: Bank, MFS, Cash Flow
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val categories = listOf(
                        Triple(AccountType.BANK, "Bank", Icons.Default.AccountBalance),
                        Triple(AccountType.MFS, "MFS", Icons.Default.PhoneAndroid),
                        Triple(AccountType.CASH_FLOW, "Cash Flow", Icons.Default.AttachMoney)
                    )

                    categories.forEach { (type, label, icon) ->
                        val isSelected = selectedType == type
                        val itemBg = if (isSelected) {
                            if (isDark) Color(0xFF0F265C) else Color(0xFFEBF4FF)
                        } else cardBg

                        val itemBorder = if (isSelected) {
                            BorderStroke(2.dp, activeAccent)
                        } else {
                            BorderStroke(1.dp, cardBorder)
                        }

                        Surface(
                            onClick = {
                                selectedType = type
                                errorMessage = null
                            },
                            shape = RoundedCornerShape(18.dp),
                            color = itemBg,
                            border = itemBorder,
                            shadowElevation = if (isSelected) 3.dp else 1.dp,
                            modifier = Modifier
                                .weight(1f)
                                .height(82.dp)
                                .bounceClick()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 4.dp, vertical = 8.dp)
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(end = 4.dp)
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(activeAccent),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(10.dp)
                                        )
                                    }
                                }

                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .align(Alignment.Center),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        tint = if (isSelected) activeAccent else textPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                            fontSize = 12.sp
                                        ),
                                        color = if (isSelected) activeAccent else textPrimary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 3 INPUT FIELDS
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Field 1: Account Name * (Required)
                    CompactInputField(
                        isFocused = isNameFocused,
                        icon = {
                            Icon(Icons.Default.Person, null, tint = activeAccent, modifier = Modifier.size(22.dp))
                        },
                        content = {
                            BasicTextField(
                                value = name,
                                onValueChange = {
                                    name = it
                                    if (it.isNotBlank()) errorMessage = null
                                },
                                textStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = textPrimary),
                                cursorBrush = SolidColor(activeAccent),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onFocusChanged { isNameFocused = it.isFocused }
                                    .testTag("account_name_input"),
                                decorationBox = { innerTextField ->
                                    if (name.isEmpty()) {
                                        Text("Account Name *", style = TextStyle(fontSize = 15.sp, color = textSecondary))
                                    }
                                    innerTextField()
                                }
                            )
                        },
                        trailing = {
                            if (name.isNotEmpty()) {
                                IconButton(onClick = { name = "" }) {
                                    Icon(Icons.Default.Clear, "Clear", tint = textSecondary, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        cardBg = cardBg,
                        cardBorder = cardBorder,
                        activeAccent = activeAccent,
                        badgeBg = badgeBg
                    )

                    // Field 2: Account / Card No (Optional)
                    CompactInputField(
                        isFocused = isAccountNoFocused,
                        icon = {
                            Icon(Icons.Default.CreditCard, null, tint = activeAccent, modifier = Modifier.size(22.dp))
                        },
                        content = {
                            BasicTextField(
                                value = accountNumber,
                                onValueChange = { accountNumber = it },
                                textStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = textPrimary),
                                cursorBrush = SolidColor(activeAccent),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = if (selectedType == AccountType.MFS) KeyboardType.Phone else KeyboardType.Text,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onFocusChanged { isAccountNoFocused = it.isFocused }
                                    .testTag("account_number_input"),
                                decorationBox = { innerTextField ->
                                    if (accountNumber.isEmpty()) {
                                        Text(
                                            if (selectedType == AccountType.MFS) "Mobile Number (Optional)" else "Account / Card No (Optional)",
                                            style = TextStyle(fontSize = 14.sp, color = textSecondary)
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        },
                        trailing = {
                            if (accountNumber.isNotEmpty()) {
                                IconButton(onClick = { accountNumber = "" }) {
                                    Icon(Icons.Default.Clear, "Clear", tint = textSecondary, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        cardBg = cardBg,
                        cardBorder = cardBorder,
                        activeAccent = activeAccent,
                        badgeBg = badgeBg
                    )

                    // Field 3: Initial Balance (৳ BDT) - Coins Stack Icon
                    CompactInputField(
                        isFocused = isBalanceFocused,
                        icon = {
                            Icon(painterResource(R.drawable.ic_coins_stack), null, tint = activeAccent, modifier = Modifier.size(24.dp))
                        },
                        content = {
                            BasicTextField(
                                value = initialBalanceText,
                                onValueChange = { initialBalanceText = it },
                                textStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = textPrimary),
                                cursorBrush = SolidColor(activeAccent),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onFocusChanged { isBalanceFocused = it.isFocused }
                                    .testTag("account_balance_input"),
                                decorationBox = { innerTextField ->
                                    if (initialBalanceText.isEmpty()) {
                                        Text("Initial Balance (৳ BDT)", style = TextStyle(fontSize = 15.sp, color = textSecondary))
                                    }
                                    innerTextField()
                                }
                            )
                        },
                        trailing = {
                            if (initialBalanceText.isNotEmpty()) {
                                IconButton(onClick = { initialBalanceText = "" }) {
                                    Icon(Icons.Default.Clear, "Clear", tint = textSecondary, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        cardBg = cardBg,
                        cardBorder = cardBorder,
                        activeAccent = activeAccent,
                        badgeBg = badgeBg
                    )
                }

                AnimatedVisibility(
                    visible = errorMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(top = 8.dp, start = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                // ACTION BUTTONS: Cancel and Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // CANCEL
                    Surface(
                        onClick = { if (!isSubmitting) onDismiss() },
                        shape = RoundedCornerShape(24.dp),
                        color = cardBg,
                        border = BorderStroke(1.5.dp, brandBlue),
                        shadowElevation = if (isDark) 0.dp else 1.dp,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .bounceClick()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Cancel, null, tint = activeAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cancel", style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp, color = activeAccent))
                        }
                    }

                    // SAVE
                    Surface(
                        onClick = {
                            if (isSubmitting) return@Surface

                            if (name.trim().isBlank()) {
                                errorMessage = "Please enter an account name."
                                return@Surface
                            }

                            isSubmitting = true
                            val balance = initialBalanceText.toDoubleOrNull() ?: 0.0

                            val colorHex = when (selectedType) {
                                AccountType.BANK -> "#3882F6"
                                AccountType.MFS -> "#EC4899"
                                AccountType.CASH_FLOW, AccountType.CASH -> "#10B981"
                                else -> "#3882F6"
                            }

                            val newAccount = Account(
                                name = name.trim(),
                                type = selectedType.name,
                                accountNumber = accountNumber.trim(),
                                balance = balance,
                                colorHex = colorHex,
                                iconName = selectedType.name.lowercase()
                            )

                            onConfirm(newAccount)
                        },
                        enabled = !isSubmitting,
                        shape = RoundedCornerShape(24.dp),
                        color = Color.Transparent,
                        shadowElevation = 3.dp,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .bounceClick()
                            .testTag("save_account_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.horizontalGradient(listOf(Color(0xFF0072FF), Color(0xFF0052FE)))),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Save, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Save", style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactInputField(
    isFocused: Boolean,
    icon: @Composable () -> Unit,
    content: @Composable () -> Unit,
    trailing: @Composable () -> Unit = {},
    cardBg: Color,
    cardBorder: Color,
    activeAccent: Color,
    badgeBg: Color
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = cardBg,
        border = BorderStroke(if (isFocused) 2.dp else 1.dp, if (isFocused) activeAccent else cardBorder),
        shadowElevation = if (isFocused) 3.dp else 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(badgeBg),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(modifier = Modifier.weight(1f)) {
                content()
            }

            trailing()
        }
    }
}
