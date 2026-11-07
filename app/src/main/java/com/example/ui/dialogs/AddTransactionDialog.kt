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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.ui.motion.bounceClick
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.LocalAppTheme
import com.example.util.StringKeys
import com.example.util.appString
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Premium Blue & White "Add Transaction" (Income / Expense) Screen matching "Add New Account" design.
 */
@Composable
fun AddTransactionDialog(
    accounts: List<Account>,
    initialType: TransactionType = TransactionType.SPENT,
    onDismiss: () -> Unit,
    onConfirm: (Transaction) -> Unit,
    onNeedAddAccount: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        AddTransactionScreenContent(
            accounts = accounts,
            initialType = initialType,
            onDismiss = onDismiss,
            onConfirm = onConfirm,
            onNeedAddAccount = onNeedAddAccount
        )
    }
}

@Composable
fun AddTransactionScreenContent(
    accounts: List<Account>,
    initialType: TransactionType,
    onDismiss: () -> Unit,
    onConfirm: (Transaction) -> Unit,
    onNeedAddAccount: () -> Unit
) {
    val activeTheme = LocalAppTheme.current
    val isDark = activeTheme.isDark
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    var selectedType by remember { mutableStateOf(initialType) }
    var amountText by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    val incomeCategories = listOf("Job Salary", "Pathao Drive", "Tips", "Business", "Bonus", "Others")
    val spentCategories = listOf("Food", "Family", "Rental cost", "Shopping", "Transport", "Bills", "Others")

    var selectedCategory by remember(selectedType) {
        mutableStateOf(if (selectedType == TransactionType.INCOME) "Job Salary" else "Food")
    }

    var selectedAccount by remember(accounts) {
        mutableStateOf(accounts.firstOrNull())
    }
    var accountDropdownExpanded by remember { mutableStateOf(false) }

    val todayDate = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }
    var transactionDate by remember { mutableStateOf(todayDate) }

    var isAmountFocused by remember { mutableStateOf(false) }
    var isTitleFocused by remember { mutableStateOf(false) }
    var isNoteFocused by remember { mutableStateOf(false) }

    BackHandler {
        if (!isSubmitting) onDismiss()
    }

    // Dynamic Theme Palette
    val screenBg = if (isDark) Color(0xFF060D1E) else Color(0xFFF6F9FF)
    val cardBg = if (isDark) Color(0xFF0B1736) else Color.White
    val cardBorder = if (isDark) Color(0xFF1E3A8A) else Color(0xFFE2E8F0)
    val textPrimary = if (isDark) Color.White else Color(0xFF080F20)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val brandBlue = Color(0xFF0066FF)
    val cyanAccent = Color(0xFF38BDF8)
    val activeAccent = if (selectedType == TransactionType.INCOME) IncomeGreen else (if (isDark) cyanAccent else brandBlue)
    val badgeBg = if (isDark) Color(0xFF132854) else Color(0xFFEBF4FF)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
            .testTag("add_transaction_screen")
    ) {
        // 1. TOP CURVED GRADIENT ACCENT
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

            val wavePath = Path().apply {
                moveTo(0f, 0f)
                lineTo(w, 0f)
                lineTo(w, h * 0.50f)
                cubicTo(w * 0.75f, h * 0.85f, w * 0.25f, h * 0.25f, 0f, h * 0.65f)
                close()
            }
            drawPath(
                path = wavePath,
                brush = Brush.linearGradient(
                    colors = if (isDark) listOf(Color(0xFF0052FE).copy(alpha = 0.25f), Color.Transparent)
                    else listOf(Color(0xFF0066FF).copy(alpha = 0.20f), Color.Transparent)
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

            val skylineColor = if (isDark) Color(0xFF1E3A8A).copy(alpha = 0.25f) else Color(0xFF93C5FD).copy(alpha = 0.30f)
            val baseH = h * 0.65f

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

            val bottomWave = Path().apply {
                moveTo(0f, h)
                lineTo(w, h)
                lineTo(w, h * 0.45f)
                cubicTo(w * 0.70f, h * 0.30f, w * 0.30f, h * 0.60f, 0f, h * 0.40f)
                close()
            }
            drawPath(
                path = bottomWave,
                brush = Brush.verticalGradient(
                    colors = if (isDark) listOf(Color(0xFF0052FE).copy(alpha = 0.35f), Color(0xFF002277).copy(alpha = 0.80f))
                    else listOf(Color(0xFF38BDF8).copy(alpha = 0.40f), Color(0xFF0072FF).copy(alpha = 0.85f))
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
            // TOP BAR: Circular Back Button & 3D Illustration
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    onClick = onDismiss,
                    shape = CircleShape,
                    color = cardBg,
                    border = BorderStroke(1.dp, cardBorder),
                    shadowElevation = if (isDark) 0.dp else 3.dp,
                    modifier = Modifier
                        .size(46.dp)
                        .testTag("add_transaction_back_button")
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // 3D Illustration with '+' or '-' badge
                Box(
                    modifier = Modifier
                        .size(115.dp)
                        .padding(end = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val illustrationRes = if (selectedType == TransactionType.INCOME) {
                        R.drawable.img_quick_add_income
                    } else {
                        R.drawable.img_quick_add_spent
                    }

                    Image(
                        painter = painterResource(id = illustrationRes),
                        contentDescription = "Transaction Illustration",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(105.dp)
                            .clip(RoundedCornerShape(20.dp))
                    )

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
                                imageVector = if (selectedType == TransactionType.INCOME) Icons.Default.Add else Icons.Default.Remove,
                                contentDescription = "Type Badge",
                                tint = if (selectedType == TransactionType.INCOME) IncomeGreen else ExpenseRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // TITLE (Clean, bold, NO subtitle)
            Text(
                text = if (selectedType == TransactionType.INCOME) "Add Income" else "Add Expense",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp,
                    letterSpacing = 0.2.sp
                ),
                color = textPrimary,
                modifier = Modifier.testTag("add_transaction_title")
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 2 MAIN CATEGORY PILLS: Income (আয়) vs Spent (ব্যয়)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Income Button
                val isIncome = selectedType == TransactionType.INCOME
                Surface(
                    onClick = {
                        selectedType = TransactionType.INCOME
                        selectedCategory = "Job Salary"
                    },
                    shape = RoundedCornerShape(22.dp),
                    color = if (isIncome) (if (isDark) Color(0xFF0F3624) else Color(0xFFE8FDF2)) else cardBg,
                    border = BorderStroke(if (isIncome) 2.dp else 1.dp, if (isIncome) IncomeGreen else cardBorder),
                    shadowElevation = if (isIncome) 4.dp else 1.dp,
                    modifier = Modifier
                        .weight(1f)
                        .height(72.dp)
                        .bounceClick()
                ) {
                    Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                        if (isIncome) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(IncomeGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(12.dp))
                            }
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = if (isIncome) IncomeGreen else textSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Income (আয়)",
                                fontWeight = if (isIncome) FontWeight.Bold else FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = if (isIncome) (if (isDark) Color(0xFF4ADE80) else IncomeGreen) else textPrimary
                            )
                        }
                    }
                }

                // Spent Button
                val isSpent = selectedType == TransactionType.SPENT
                Surface(
                    onClick = {
                        selectedType = TransactionType.SPENT
                        selectedCategory = "Food"
                    },
                    shape = RoundedCornerShape(22.dp),
                    color = if (isSpent) (if (isDark) Color(0xFF38141C) else Color(0xFFFDE8EC)) else cardBg,
                    border = BorderStroke(if (isSpent) 2.dp else 1.dp, if (isSpent) ExpenseRed else cardBorder),
                    shadowElevation = if (isSpent) 4.dp else 1.dp,
                    modifier = Modifier
                        .weight(1f)
                        .height(72.dp)
                        .bounceClick()
                ) {
                    Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                        if (isSpent) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(ExpenseRed),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(12.dp))
                            }
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = if (isSpent) ExpenseRed else textSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Spent (ব্যয়)",
                                fontWeight = if (isSpent) FontWeight.Bold else FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = if (isSpent) (if (isDark) Color(0xFFFB7185) else ExpenseRed) else textPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // CATEGORY SELECTOR CARDS (Horizontal Scroll)
            Text(
                text = "Category",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = textSecondary,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            val currentCategories = if (selectedType == TransactionType.INCOME) incomeCategories else spentCategories

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(currentCategories) { cat ->
                    val isCatSelected = selectedCategory == cat
                    Surface(
                        onClick = { selectedCategory = cat },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isCatSelected) {
                            if (isDark) Color(0xFF0F265C) else Color(0xFFEBF4FF)
                        } else cardBg,
                        border = BorderStroke(
                            if (isCatSelected) 2.dp else 1.dp,
                            if (isCatSelected) activeAccent else cardBorder
                        ),
                        shadowElevation = if (isCatSelected) 2.dp else 1.dp,
                        modifier = Modifier.bounceClick()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (isCatSelected) {
                                Icon(Icons.Default.Check, null, tint = activeAccent, modifier = Modifier.size(14.dp))
                            }
                            Text(
                                text = cat,
                                fontSize = 14.sp,
                                fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isCatSelected) activeAccent else textPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ACCOUNT SELECTOR CARD
            Text(
                text = "Account",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = textSecondary,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            Surface(
                onClick = {
                    if (accounts.isEmpty()) onNeedAddAccount() else accountDropdownExpanded = true
                },
                shape = RoundedCornerShape(24.dp),
                color = cardBg,
                border = BorderStroke(1.2.dp, cardBorder),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .bounceClick()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(badgeBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = activeAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedAccount?.name ?: "No accounts available",
                            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = textPrimary)
                        )
                        Text(
                            text = selectedAccount?.let { "${it.type} • ৳${it.balance.toInt()}" } ?: "Tap to add an account",
                            style = TextStyle(fontSize = 13.sp, color = textSecondary)
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Dropdown",
                        tint = textSecondary,
                        modifier = Modifier.size(22.dp)
                    )

                    DropdownMenu(
                        expanded = accountDropdownExpanded,
                        onDismissRequest = { accountDropdownExpanded = false }
                    ) {
                        accounts.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text("${acc.name} (${acc.type}) - ৳${acc.balance.toInt()}") },
                                onClick = {
                                    selectedAccount = acc
                                    accountDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3 INPUT FIELDS
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Field 1: Amount (৳ BDT) *
                TransactionInputField(
                    isFocused = isAmountFocused,
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_coins_stack),
                            contentDescription = "Coins Stack",
                            tint = activeAccent,
                            modifier = Modifier.size(26.dp)
                        )
                    },
                    content = {
                        BasicTextField(
                            value = amountText,
                            onValueChange = {
                                amountText = it.filter { char -> char.isDigit() || char == '.' }
                                if (it.isNotBlank()) errorMessage = null
                            },
                            textStyle = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textPrimary
                            ),
                            cursorBrush = SolidColor(activeAccent),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { isAmountFocused = it.isFocused }
                                .testTag("transaction_amount_input"),
                            decorationBox = { innerTextField ->
                                if (amountText.isEmpty()) {
                                    Text(
                                        text = "Amount (৳ BDT) *",
                                        style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium, color = textSecondary)
                                    )
                                }
                                innerTextField()
                            }
                        )
                    },
                    trailing = {
                        if (amountText.isNotEmpty()) {
                            IconButton(onClick = { amountText = "" }) {
                                Icon(Icons.Default.Clear, "Clear", tint = textSecondary, modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    cardBg = cardBg,
                    cardBorder = cardBorder,
                    activeAccent = activeAccent,
                    badgeBg = badgeBg
                )

                // Field 2: Title / Purpose *
                TransactionInputField(
                    isFocused = isTitleFocused,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Title Icon",
                            tint = activeAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    content = {
                        BasicTextField(
                            value = title,
                            onValueChange = {
                                title = it
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
                                .onFocusChanged { isTitleFocused = it.isFocused }
                                .testTag("transaction_title_input"),
                            decorationBox = { innerTextField ->
                                if (title.isEmpty()) {
                                    Text(
                                        text = if (selectedType == TransactionType.INCOME) "Income Title (e.g. Salary, Client Pay) *" else "Expense Title (e.g. Grocery, Lunch) *",
                                        style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium, color = textSecondary)
                                    )
                                }
                                innerTextField()
                            }
                        )
                    },
                    trailing = {
                        if (title.isNotEmpty()) {
                            IconButton(onClick = { title = "" }) {
                                Icon(Icons.Default.Clear, "Clear", tint = textSecondary, modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    cardBg = cardBg,
                    cardBorder = cardBorder,
                    activeAccent = activeAccent,
                    badgeBg = badgeBg
                )

                // Field 3: Note / Description (Optional)
                TransactionInputField(
                    isFocused = isNoteFocused,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "Note Icon",
                            tint = activeAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    content = {
                        BasicTextField(
                            value = note,
                            onValueChange = { note = it },
                            textStyle = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textPrimary
                            ),
                            cursorBrush = SolidColor(activeAccent),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { isNoteFocused = it.isFocused }
                                .testTag("transaction_note_input"),
                            decorationBox = { innerTextField ->
                                if (note.isEmpty()) {
                                    Text(
                                        text = "Note or Details (Optional)",
                                        style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium, color = textSecondary)
                                    )
                                }
                                innerTextField()
                            }
                        )
                    },
                    trailing = {
                        if (note.isNotEmpty()) {
                            IconButton(onClick = { note = "" }) {
                                Icon(Icons.Default.Clear, "Clear", tint = textSecondary, modifier = Modifier.size(18.dp))
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
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.padding(top = 10.dp, start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // ACTION BUTTONS: Cancel & Save
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // CANCEL
                Surface(
                    onClick = { if (!isSubmitting) onDismiss() },
                    shape = RoundedCornerShape(28.dp),
                    color = cardBg,
                    border = BorderStroke(2.dp, brandBlue),
                    shadowElevation = if (isDark) 0.dp else 2.dp,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .bounceClick()
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Cancel, "Cancel", tint = activeAccent, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                            color = activeAccent
                        )
                    }
                }

                // SAVE / RECORD
                Surface(
                    onClick = {
                        if (isSubmitting) return@Surface

                        val amount = amountText.toDoubleOrNull()
                        if (amount == null || amount <= 0.0) {
                            errorMessage = "Please enter a valid amount."
                            return@Surface
                        }

                        if (title.trim().isBlank()) {
                            errorMessage = "Please enter a title."
                            return@Surface
                        }

                        if (selectedAccount == null) {
                            errorMessage = "Please choose an account."
                            return@Surface
                        }

                        isSubmitting = true
                        val txn = Transaction(
                            accountId = selectedAccount!!.id,
                            accountName = selectedAccount!!.name,
                            amount = amount,
                            type = selectedType.name,
                            category = selectedCategory,
                            title = title.trim(),
                            note = note.trim(),
                            date = transactionDate
                        )
                        onConfirm(txn)
                    },
                    enabled = !isSubmitting,
                    shape = RoundedCornerShape(28.dp),
                    color = Color.Transparent,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .bounceClick()
                        .testTag("save_transaction_button")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    if (selectedType == TransactionType.INCOME) listOf(Color(0xFF059669), Color(0xFF10B981))
                                    else listOf(Color(0xFF0072FF), Color(0xFF0052FE))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Save, "Save", tint = Color.White, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Save",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
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

@Composable
private fun TransactionInputField(
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
        border = BorderStroke(if (isFocused) 2.dp else 1.2.dp, if (isFocused) activeAccent else cardBorder),
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

            Box(modifier = Modifier.weight(1f)) {
                content()
            }

            trailing()
        }
    }
}
