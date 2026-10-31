package com.example.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhoneAndroid
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.ui.motion.bounceClick
import com.example.ui.motion.neonGlow
import com.example.ui.theme.ColorBank
import com.example.ui.theme.ColorBkash
import com.example.ui.theme.ColorNagad
import com.example.ui.theme.ColorRocket
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.LocalAppTheme
import com.example.ui.theme.PremiumBlue
import com.example.ui.theme.PremiumPink
import com.example.ui.theme.PureWhite
import com.example.util.StringKeys
import com.example.util.appString

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddAccountDialog(
    onDismiss: () -> Unit,
    onConfirm: (Account) -> Unit
) {
    val activeTheme = LocalAppTheme.current
    val focusManager = LocalFocusManager.current

    // No default name as requested by the user: starts completely blank!
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(AccountType.BANK) }
    var accountNumber by remember { mutableStateOf("") }
    var initialBalanceText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    val typeOptions = listOf(
        Triple(AccountType.BANK, ColorBank, Icons.Default.AccountBalance),
        Triple(AccountType.MFS, PremiumPink, Icons.Default.PhoneAndroid),
        Triple(AccountType.CASH_FLOW, IncomeGreen, Icons.Default.AttachMoney)
    )

    // Quick suggestions based on selected type to make writing fast and easy
    val bankSuggestions = listOf("City Bank", "DBBL", "BRAC Bank", "Islami Bank", "EBL")
    val mfsSuggestions = listOf("bKash", "Nagad", "Rocket", "Upay")
    val cashSuggestions = listOf("Daily Cash", "Main Wallet", "Petty Cash")

    val activeColor = when (selectedType) {
        AccountType.BANK -> ColorBank
        AccountType.MFS -> PremiumPink
        AccountType.CASH_FLOW, AccountType.CASH -> IncomeGreen
        else -> activeTheme.primaryColor
    }

    AlertDialog(
        onDismissRequest = {
            if (!isSubmitting) onDismiss()
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(activeColor.copy(alpha = 0.16f))
                        .neonGlow(color = activeColor, shape = CircleShape, strokeWidth = 1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (selectedType) {
                            AccountType.BANK -> Icons.Default.AccountBalance
                            AccountType.MFS -> Icons.Default.PhoneAndroid
                            else -> Icons.Default.AttachMoney
                        },
                        contentDescription = null,
                        tint = activeColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Add New Account",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Bank, MFS (bKash/Nagad), or Cash Flow",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Account Type Selector Tabs
                Text(
                    text = "1. Account Category",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    typeOptions.forEach { (type, color, icon) ->
                        val isSelected = selectedType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) color.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) color else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .then(
                                    if (isSelected) Modifier.neonGlow(color = color, shape = RoundedCornerShape(14.dp), strokeWidth = 1.5.dp)
                                    else Modifier
                                )
                                .clickable {
                                    selectedType = type
                                    errorMessage = null
                                }
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = type.defaultLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Quick Suggestion Chips for Fast Entry
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Quick Suggestion (Tap to fill name):",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val suggestions = when (selectedType) {
                            AccountType.BANK -> bankSuggestions
                            AccountType.MFS -> mfsSuggestions
                            else -> cashSuggestions
                        }

                        suggestions.forEach { suggestion ->
                            val isCurrentName = name.equals(suggestion, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isCurrentName) activeColor.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isCurrentName) activeColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable {
                                        name = suggestion
                                        errorMessage = null
                                    }
                            ) {
                                Text(
                                    text = suggestion,
                                    fontSize = 11.sp,
                                    fontWeight = if (isCurrentName) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCurrentName) activeColor else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }

                // 2. Account Name Input
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            if (it.isNotBlank()) errorMessage = null
                        },
                        label = { Text("Account Name *") },
                        placeholder = {
                            Text(
                                when (selectedType) {
                                    AccountType.MFS -> "e.g. bKash Personal, Nagad"
                                    AccountType.BANK -> "e.g. City Bank, Dutch-Bangla"
                                    else -> "e.g. Daily Cash, Wallet"
                                }
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = activeColor,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (name.isNotEmpty()) {
                                IconButton(onClick = { name = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("account_name_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = activeColor,
                            focusedLabelColor = activeColor
                        )
                    )
                }

                // 3. Account / Mobile Number (Optional)
                OutlinedTextField(
                    value = accountNumber,
                    onValueChange = { accountNumber = it },
                    label = {
                        Text(
                            when (selectedType) {
                                AccountType.MFS -> "Mobile Number (e.g. 017xxxxxxxx)"
                                AccountType.BANK -> "Account / Card No (Optional)"
                                else -> "Account Note / Identifier (Optional)"
                            }
                        )
                    },
                    placeholder = {
                        Text(
                            when (selectedType) {
                                AccountType.MFS -> "017XXXXXXXX"
                                AccountType.BANK -> "1234 5678 XXXX"
                                else -> "Personal Cash"
                            }
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Dialpad,
                            contentDescription = null,
                            tint = activeColor.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (selectedType == AccountType.MFS) KeyboardType.Phone else KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = activeColor,
                        focusedLabelColor = activeColor
                    )
                )

                // 4. Opening Balance Input & Quick Adjustment Chips
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = initialBalanceText,
                        onValueChange = { initialBalanceText = it },
                        label = { Text("Initial Balance (৳ BDT)") },
                        placeholder = { Text("0.00") },
                        prefix = {
                            Text(
                                text = "৳ ",
                                fontWeight = FontWeight.Bold,
                                color = activeColor
                            )
                        },
                        trailingIcon = {
                            if (initialBalanceText.isNotEmpty()) {
                                IconButton(onClick = { initialBalanceText = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("account_balance_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = activeColor,
                            focusedLabelColor = activeColor
                        )
                    )

                    // Quick Balance Amount Pills (+৳500, +৳1,000, +৳5,000, +৳10,000, 0)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val quickAmounts = listOf(500.0, 1000.0, 5000.0, 10000.0)
                        quickAmounts.forEach { amount ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        val current = initialBalanceText.toDoubleOrNull() ?: 0.0
                                        initialBalanceText = (current + amount).toInt().toString()
                                    }
                            ) {
                                Text(
                                    text = "+৳${amount.toInt()}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = activeTheme.primaryColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        if (initialBalanceText.isNotBlank() && initialBalanceText != "0") {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = ExpenseRed.copy(alpha = 0.12f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { initialBalanceText = "" }
                            ) {
                                Text(
                                    text = "Reset",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ExpenseRed,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                AnimatedVisibility(
                    visible = errorMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isSubmitting) return@Button

                    if (name.trim().isBlank()) {
                        errorMessage = "Please enter an account name."
                        return@Button
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
                modifier = Modifier
                    .neonGlow(color = activeColor, shape = RoundedCornerShape(12.dp), strokeWidth = 1.dp)
                    .bounceClick(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = activeColor,
                    contentColor = PureWhite
                )
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = PureWhite,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Saving...")
                } else {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(appString(StringKeys.SAVE), fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSubmitting
            ) {
                Text(appString(StringKeys.CANCEL))
            }
        },
        shape = RoundedCornerShape(26.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}
