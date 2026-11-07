package com.example.ui.dialogs

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
 * Premium Blue & White "Add New Account" Screen strictly matching the approved design.
 * Features:
 * - Back button with circular frosted container
 * - 3D Wallet illustration with '+' badge at top right
 * - Clean "Add New Account" header (NO subtitle)
 * - 3 Category Cards: Bank, MFS, Cash Flow (with top-right checkmark badge when active)
 * - 3 Clean Input fields: Account Name (Required), Account / Card No (Optional), Initial Balance (৳ BDT)
 * - Custom Coins / Money Stack icon for Initial Balance
 * - Cancel and Save action pill buttons
 * - Subtle bottom skyline & blue wave decoration
 * - Full Dark & Lite theme fidelity
 */
@Composable
fun AddAccountDialog(
    onDismiss: () -> Unit,
    onConfirm: (Account) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        AddAccountScreenContent(
            onDismiss = onDismiss,
            onConfirm = onConfirm
        )
    }
}

@Composable
fun AddAccountScreenContent(
    onDismiss: () -> Unit,
    onConfirm: (Account) -> Unit
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

    BackHandler {
        if (!isSubmitting) onDismiss()
    }

    // Dynamic Color Palette for Lite and Dark modes
    val screenBg = if (isDark) Color(0xFF060D1E) else Color(0xFFF6F9FF)
    val cardBg = if (isDark) Color(0xFF0B1736) else Color.White
    val cardBorder = if (isDark) Color(0xFF1E3A8A) else Color(0xFFE2E8F0)
    val textPrimary = if (isDark) Color.White else Color(0xFF080F20)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val brandBlue = Color(0xFF0066FF)
    val cyanAccent = Color(0xFF38BDF8)
    val activeAccent = if (isDark) cyanAccent else brandBlue
    val badgeBg = if (isDark) Color(0xFF132854) else Color(0xFFEBF4FF)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
            .testTag("add_account_screen")
    ) {
        // 1. TOP CURVED GRADIENT ACCENT (Ethereal Blue Waves)
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .align(Alignment.TopCenter)
        ) {
            val w = size.width
            val h = size.height

            val topGradient = if (isDark) {
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0038A8).copy(alpha = 0.35f),
                        Color(0xFF001F60).copy(alpha = 0.15f),
                        Color.Transparent
                    )
                )
            } else {
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0072FF).copy(alpha = 0.22f),
                        Color(0xFFBAE6FD).copy(alpha = 0.12f),
                        Color.Transparent
                    )
                )
            }
            drawRect(brush = topGradient)

            // Flowing curved wave header
            val wavePath = Path().apply {
                moveTo(0f, 0f)
                lineTo(w, 0f)
                lineTo(w, h * 0.50f)
                cubicTo(
                    w * 0.75f, h * 0.85f,
                    w * 0.25f, h * 0.25f,
                    0f, h * 0.65f
                )
                close()
            }
            drawPath(
                path = wavePath,
                brush = Brush.linearGradient(
                    colors = if (isDark) listOf(
                        Color(0xFF0052FE).copy(alpha = 0.25f),
                        Color(0xFF00E5FF).copy(alpha = 0.08f),
                        Color.Transparent
                    ) else listOf(
                        Color(0xFF0066FF).copy(alpha = 0.20f),
                        Color(0xFF60A5FA).copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(w, h)
                )
            )
        }

        // 2. BOTTOM SKYLINE & BASE WAVE DECORATION
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .align(Alignment.BottomCenter)
        ) {
            val w = size.width
            val h = size.height

            // Soft City Skyline Silhouette
            val skylineColor = if (isDark) Color(0xFF1E3A8A).copy(alpha = 0.25f) else Color(0xFF93C5FD).copy(alpha = 0.30f)
            val baseH = h * 0.65f

            // Building rectangles in background
            val bWidth = w / 14f
            val heights = listOf(0.35f, 0.55f, 0.40f, 0.70f, 0.50f, 0.80f, 0.45f, 0.60f, 0.75f, 0.40f, 0.65f, 0.50f, 0.70f, 0.45f)
            heights.forEachIndexed { i, factor ->
                val x = i * bWidth
                val bH = h * 0.45f * factor
                drawRect(
                    color = skylineColor,
                    topLeft = Offset(x, baseH - bH),
                    size = androidx.compose.ui.geometry.Size(bWidth - 2.dp.toPx(), bH + h * 0.4f)
                )
            }

            // Flowing base wave ribbon
            val bottomWave = Path().apply {
                moveTo(0f, h)
                lineTo(w, h)
                lineTo(w, h * 0.45f)
                cubicTo(
                    w * 0.70f, h * 0.30f,
                    w * 0.30f, h * 0.60f,
                    0f, h * 0.40f
                )
                close()
            }
            drawPath(
                path = bottomWave,
                brush = Brush.verticalGradient(
                    colors = if (isDark) listOf(
                        Color(0xFF0052FE).copy(alpha = 0.35f),
                        Color(0xFF002277).copy(alpha = 0.80f)
                    ) else listOf(
                        Color(0xFF38BDF8).copy(alpha = 0.40f),
                        Color(0xFF0072FF).copy(alpha = 0.85f)
                    )
                )
            )
        }

        // 3. MAIN SCROLLABLE CONTENT
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // TOP BAR: Circular Back Button & 3D Wallet Illustration
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Circular Back Button
                Surface(
                    onClick = onDismiss,
                    shape = CircleShape,
                    color = cardBg,
                    border = BorderStroke(1.dp, cardBorder),
                    shadowElevation = if (isDark) 0.dp else 3.dp,
                    modifier = Modifier
                        .size(46.dp)
                        .testTag("add_account_back_button")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // 3D Wallet illustration with '+' badge
                Box(
                    modifier = Modifier
                        .size(115.dp)
                        .padding(end = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_wallet_3d_logo),
                        contentDescription = "Wallet Illustration",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(105.dp)
                    )

                    // Circular White/Blue Plus Badge on Wallet
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 4.dp,
                        border = BorderStroke(1.5.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .size(32.dp)
                            .align(Alignment.BottomEnd)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Badge",
                                tint = brandBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // TITLE: "Add New Account" (Completely without subtitle as required)
            Text(
                text = "Add New Account",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp,
                    letterSpacing = 0.2.sp
                ),
                color = textPrimary,
                modifier = Modifier.testTag("add_account_title")
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 3 ACCOUNT CATEGORIES: Bank, MFS, Cash Flow
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                    } else {
                        cardBg
                    }

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
                        shape = RoundedCornerShape(22.dp),
                        color = itemBg,
                        border = itemBorder,
                        shadowElevation = if (isSelected) 4.dp else 1.dp,
                        modifier = Modifier
                            .weight(1f)
                            .height(108.dp)
                            .bounceClick()
                            .testTag("category_card_${type.name.lowercase()}")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 4.dp, vertical = 12.dp)
                        ) {
                            // Checkmark badge in top-right when selected
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(end = 6.dp)
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(activeAccent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }

                            // Centered icon + label
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
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    ),
                                    color = if (isSelected) activeAccent else textPrimary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // 3 INPUT FIELDS
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Field 1: Account Name * (Required)
                InputFieldCard(
                    isFocused = isNameFocused,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Account Name Icon",
                            tint = activeAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    content = {
                        BasicTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                if (it.isNotBlank()) errorMessage = null
                            },
                            textStyle = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textPrimary
                            ),
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
                                    Text(
                                        text = "Account Name *",
                                        style = TextStyle(
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = textSecondary
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        )
                    },
                    trailing = {
                        if (name.isNotEmpty()) {
                            IconButton(onClick = { name = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear Name",
                                    tint = textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    cardBg = cardBg,
                    cardBorder = cardBorder,
                    activeAccent = activeAccent,
                    badgeBg = badgeBg
                )

                // Field 2: Account / Card No (Optional)
                InputFieldCard(
                    isFocused = isAccountNoFocused,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = "Card No Icon",
                            tint = activeAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    content = {
                        BasicTextField(
                            value = accountNumber,
                            onValueChange = { accountNumber = it },
                            textStyle = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textPrimary
                            ),
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
                                        text = if (selectedType == AccountType.MFS) "Mobile Number (Optional)" else "Account / Card No (Optional)",
                                        style = TextStyle(
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = textSecondary
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        )
                    },
                    trailing = {
                        if (accountNumber.isNotEmpty()) {
                            IconButton(onClick = { accountNumber = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear Number",
                                    tint = textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    cardBg = cardBg,
                    cardBorder = cardBorder,
                    activeAccent = activeAccent,
                    badgeBg = badgeBg
                )

                // Field 3: Initial Balance (৳ BDT) - Uses Premium Coins Stack Icon
                InputFieldCard(
                    isFocused = isBalanceFocused,
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_coins_stack),
                            contentDescription = "Coins Stack Icon",
                            tint = activeAccent,
                            modifier = Modifier.size(26.dp)
                        )
                    },
                    content = {
                        BasicTextField(
                            value = initialBalanceText,
                            onValueChange = { initialBalanceText = it },
                            textStyle = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textPrimary
                            ),
                            cursorBrush = SolidColor(activeAccent),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { isBalanceFocused = it.isFocused }
                                .testTag("account_balance_input"),
                            decorationBox = { innerTextField ->
                                if (initialBalanceText.isEmpty()) {
                                    Text(
                                        text = "Initial Balance (৳ BDT)",
                                        style = TextStyle(
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = textSecondary
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        )
                    },
                    trailing = {
                        if (initialBalanceText.isNotEmpty()) {
                            IconButton(onClick = { initialBalanceText = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear Balance",
                                    tint = textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    cardBg = cardBg,
                    cardBorder = cardBorder,
                    activeAccent = activeAccent,
                    badgeBg = badgeBg
                )
            }

            // Error Message Banner
            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.padding(top = 10.dp, start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ACTION BUTTONS: Cancel and Save
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // CANCEL BUTTON (White/Navy with Blue Border)
                Surface(
                    onClick = {
                        if (!isSubmitting) onDismiss()
                    },
                    shape = RoundedCornerShape(28.dp),
                    color = cardBg,
                    border = BorderStroke(2.dp, brandBlue),
                    shadowElevation = if (isDark) 0.dp else 2.dp,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .bounceClick()
                        .testTag("cancel_account_button")
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = "Cancel",
                            tint = activeAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = activeAccent
                        )
                    }
                }

                // SAVE BUTTON (Rich Blue Gradient Pill)
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
                    shape = RoundedCornerShape(28.dp),
                    color = Color.Transparent,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .bounceClick()
                        .testTag("save_account_button")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF0072FF),
                                        Color(0xFF0052FE)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = "Save",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Save",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    ),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Reusable elevated input field card with circular left icon badge.
 */
@Composable
private fun InputFieldCard(
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
        shape = RoundedCornerShape(24.dp),
        color = cardBg,
        border = BorderStroke(
            width = if (isFocused) 2.dp else 1.2.dp,
            color = if (isFocused) activeAccent else cardBorder
        ),
        shadowElevation = if (isFocused) 4.dp else 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left circular icon container
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(badgeBg),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Middle input content
            Box(modifier = Modifier.weight(1f)) {
                content()
            }

            // Right trailing icon
            trailing()
        }
    }
}
