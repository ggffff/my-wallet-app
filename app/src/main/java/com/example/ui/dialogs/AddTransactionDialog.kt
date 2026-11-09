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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
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
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.ui.motion.bounceClick
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.LocalAppTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Premium Blue & White "Add Transaction" Floating Card Dialog.
 * Compact floating card modal ("শর্ট স্কিন") for Income and Expense.
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
            usePlatformDefaultWidth = false
        )
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

        val cardBg = if (isDark) Color(0xFF0B1736) else Color.White
        val cardBorder = if (isDark) Color(0xFF1E3A8A) else Color(0xFFE2E8F0)
        val textPrimary = if (isDark) Color.White else Color(0xFF080F20)
        val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
        val brandBlue = Color(0xFF0066FF)
        val cyanAccent = Color(0xFF38BDF8)
        val activeAccent = if (selectedType == TransactionType.INCOME) IncomeGreen else (if (isDark) cyanAccent else brandBlue)
        val badgeBg = if (isDark) Color(0xFF132854) else Color(0xFFEBF4FF)

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .heightIn(max = 660.dp)
                .shadow(
                    elevation = if (isDark) 12.dp else 8.dp,
                    shape = RoundedCornerShape(28.dp),
                    spotColor = if (isDark) Color(0xFF0052FE).copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.12f)
                )
                .testTag("add_transaction_dialog"),
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
                // TOP HEADER: Title + 3D Icon + Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (selectedType == TransactionType.INCOME) "Add Income" else "Add Expense",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                letterSpacing = 0.2.sp
                            ),
                            color = textPrimary,
                            modifier = Modifier.testTag("add_transaction_title")
                        )
                    }

                    // 3D Illustration Badge
                    Box(
                        modifier = Modifier
                            .size(62.dp)
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
                            contentDescription = "Transaction Logo",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(14.dp))
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
                                    imageVector = if (selectedType == TransactionType.INCOME) Icons.Default.Add else Icons.Default.Remove,
                                    contentDescription = null,
                                    tint = if (selectedType == TransactionType.INCOME) IncomeGreen else ExpenseRed,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }

                    // Close Button
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

                Spacer(modifier = Modifier.height(16.dp))

                // TYPE SWITCHER: Income vs Spent
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val isIncome = selectedType == TransactionType.INCOME
                    Surface(
                        onClick = {
                            selectedType = TransactionType.INCOME
                            selectedCategory = "Job Salary"
                        },
                        shape = RoundedCornerShape(18.dp),
                        color = if (isIncome) (if (isDark) Color(0xFF0F3624) else Color(0xFFE8FDF2)) else cardBg,
                        border = BorderStroke(if (isIncome) 2.dp else 1.dp, if (isIncome) IncomeGreen else cardBorder),
                        shadowElevation = if (isIncome) 3.dp else 1.dp,
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .bounceClick()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.ArrowDownward, null, tint = if (isIncome) IncomeGreen else textSecondary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Income (আয়)",
                                fontWeight = if (isIncome) FontWeight.Bold else FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = if (isIncome) (if (isDark) Color(0xFF4ADE80) else IncomeGreen) else textPrimary
                            )
                        }
                    }

                    val isSpent = selectedType == TransactionType.SPENT
                    Surface(
                        onClick = {
                            selectedType = TransactionType.SPENT
                            selectedCategory = "Food"
                        },
                        shape = RoundedCornerShape(18.dp),
                        color = if (isSpent) (if (isDark) Color(0xFF38141C) else Color(0xFFFDE8EC)) else cardBg,
                        border = BorderStroke(if (isSpent) 2.dp else 1.dp, if (isSpent) ExpenseRed else cardBorder),
                        shadowElevation = if (isSpent) 3.dp else 1.dp,
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .bounceClick()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.ArrowUpward, null, tint = if (isSpent) ExpenseRed else textSecondary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Spent (ব্যয়)",
                                fontWeight = if (isSpent) FontWeight.Bold else FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = if (isSpent) (if (isDark) Color(0xFFFB7185) else ExpenseRed) else textPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // CATEGORIES (Horizontal Scroll)
                val currentCategories = if (selectedType == TransactionType.INCOME) incomeCategories else spentCategories
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(currentCategories) { cat ->
                        val isCatSelected = selectedCategory == cat
                        Surface(
                            onClick = { selectedCategory = cat },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isCatSelected) (if (isDark) Color(0xFF0F265C) else Color(0xFFEBF4FF)) else cardBg,
                            border = BorderStroke(if (isCatSelected) 1.5.dp else 1.dp, if (isCatSelected) activeAccent else cardBorder),
                            modifier = Modifier.bounceClick()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (isCatSelected) {
                                    Icon(Icons.Default.Check, null, tint = activeAccent, modifier = Modifier.size(12.dp))
                                }
                                Text(
                                    text = cat,
                                    fontSize = 13.sp,
                                    fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isCatSelected) activeAccent else textPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ACCOUNT SELECTOR
                Surface(
                    onClick = {
                        if (accounts.isEmpty()) onNeedAddAccount() else accountDropdownExpanded = true
                    },
                    shape = RoundedCornerShape(20.dp),
                    color = cardBg,
                    border = BorderStroke(1.dp, cardBorder),
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .bounceClick()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(badgeBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AccountBalanceWallet, null, tint = activeAccent, modifier = Modifier.size(20.dp))
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedAccount?.name ?: "No accounts available",
                                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = textPrimary)
                            )
                            Text(
                                text = selectedAccount?.let { "${it.type} • ৳${it.balance.toInt()}" } ?: "Tap to add account",
                                style = TextStyle(fontSize = 11.sp, color = textSecondary)
                            )
                        }

                        Icon(Icons.Default.KeyboardArrowDown, null, tint = textSecondary, modifier = Modifier.size(20.dp))

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

                Spacer(modifier = Modifier.height(14.dp))

                // INPUT FIELDS
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Field 1: Amount (৳ BDT) *
                    CompactInputField(
                        isFocused = isAmountFocused,
                        icon = {
                            Icon(painterResource(R.drawable.ic_coins_stack), null, tint = activeAccent, modifier = Modifier.size(24.dp))
                        },
                        content = {
                            BasicTextField(
                                value = amountText,
                                onValueChange = {
                                    amountText = it.filter { char -> char.isDigit() || char == '.' }
                                    if (it.isNotBlank()) errorMessage = null
                                },
                                textStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = textPrimary),
                                cursorBrush = SolidColor(activeAccent),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onFocusChanged { isAmountFocused = it.isFocused }
                                    .testTag("transaction_amount_input"),
                                decorationBox = { innerTextField ->
                                    if (amountText.isEmpty()) {
                                        Text("Amount (৳ BDT) *", style = TextStyle(fontSize = 15.sp, color = textSecondary))
                                    }
                                    innerTextField()
                                }
                            )
                        },
                        trailing = {
                            if (amountText.isNotEmpty()) {
                                IconButton(onClick = { amountText = "" }) {
                                    Icon(Icons.Default.Clear, "Clear", tint = textSecondary, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        cardBg = cardBg,
                        cardBorder = cardBorder,
                        activeAccent = activeAccent,
                        badgeBg = badgeBg
                    )

                    // Field 2: Title / Purpose *
                    CompactInputField(
                        isFocused = isTitleFocused,
                        icon = {
                            Icon(Icons.Default.Edit, null, tint = activeAccent, modifier = Modifier.size(22.dp))
                        },
                        content = {
                            BasicTextField(
                                value = title,
                                onValueChange = {
                                    title = it
                                    if (it.isNotBlank()) errorMessage = null
                                },
                                textStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = textPrimary),
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
                                            if (selectedType == TransactionType.INCOME) "Income Title (e.g. Salary, Client Pay) *" else "Expense Title (e.g. Grocery, Lunch) *",
                                            style = TextStyle(fontSize = 14.sp, color = textSecondary)
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        },
                        trailing = {
                            if (title.isNotEmpty()) {
                                IconButton(onClick = { title = "" }) {
                                    Icon(Icons.Default.Clear, "Clear", tint = textSecondary, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        cardBg = cardBg,
                        cardBorder = cardBorder,
                        activeAccent = activeAccent,
                        badgeBg = badgeBg
                    )

                    // Field 3: Note / Description (Optional)
                    CompactInputField(
                        isFocused = isNoteFocused,
                        icon = {
                            Icon(Icons.Default.Description, null, tint = activeAccent, modifier = Modifier.size(22.dp))
                        },
                        content = {
                            BasicTextField(
                                value = note,
                                onValueChange = { note = it },
                                textStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = textPrimary),
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
                                        Text("Note or Details (Optional)", style = TextStyle(fontSize = 14.sp, color = textSecondary))
                                    }
                                    innerTextField()
                                }
                            )
                        },
                        trailing = {
                            if (note.isNotEmpty()) {
                                IconButton(onClick = { note = "" }) {
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

                Spacer(modifier = Modifier.height(20.dp))

                // ACTION BUTTONS
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
                        shape = RoundedCornerShape(24.dp),
                        color = Color.Transparent,
                        shadowElevation = 3.dp,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
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
