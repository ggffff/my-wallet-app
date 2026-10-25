package com.example.ui.dialogs

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.ui.theme.ColorBank
import com.example.ui.theme.ColorBkash
import com.example.ui.theme.ColorCash
import com.example.ui.theme.ColorNagad
import com.example.ui.theme.ColorRocket
import com.example.util.StringKeys
import com.example.util.appString

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddAccountDialog(
    onDismiss: () -> Unit,
    onConfirm: (Account) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(AccountType.BANK) }
    var accountNumber by remember { mutableStateOf("") }
    var initialBalanceText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val typeOptions = listOf(
        Triple(AccountType.BANK, ColorBank, Icons.Default.AccountBalance),
        Triple(AccountType.BKASH, ColorBkash, Icons.Default.PhoneAndroid),
        Triple(AccountType.NAGAD, ColorNagad, Icons.Default.PhoneAndroid),
        Triple(AccountType.ROCKET, ColorRocket, Icons.Default.PhoneAndroid),
        Triple(AccountType.CASH, ColorCash, Icons.Default.AttachMoney),
        Triple(AccountType.OTHER, Color(0xFF6366F1), Icons.Default.CreditCard)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = appString(StringKeys.ADD_ACCOUNT),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Select Account Type",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    typeOptions.forEach { (type, color, icon) ->
                        val isSelected = selectedType == type
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) color else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    selectedType = type
                                    if (name.isBlank() && type != AccountType.OTHER) {
                                        name = when (type) {
                                            AccountType.BANK -> "Bank Account"
                                            AccountType.BKASH -> "bKash"
                                            AccountType.NAGAD -> "Nagad"
                                            AccountType.ROCKET -> "Rocket"
                                            AccountType.CASH -> "Cash Wallet"
                                            AccountType.OTHER -> ""
                                        }
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(color, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = type.defaultLabel,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Account Name (e.g. BRAC Bank, Personal bKash)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("account_name_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors()
                )

                OutlinedTextField(
                    value = accountNumber,
                    onValueChange = { accountNumber = it },
                    label = { Text("Account / Mobile Number (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                OutlinedTextField(
                    value = initialBalanceText,
                    onValueChange = { initialBalanceText = it },
                    label = { Text("Initial Balance (BDT ৳) - leave blank for 0") },
                    placeholder = { Text("0.00") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("account_balance_input"),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "Please enter an account name."
                        return@Button
                    }
                    val balance = initialBalanceText.toDoubleOrNull() ?: 0.0
                    val colorHex = when (selectedType) {
                        AccountType.BKASH -> "#E2136E"
                        AccountType.NAGAD -> "#F7931E"
                        AccountType.ROCKET -> "#8C3494"
                        AccountType.BANK -> "#2563EB"
                        AccountType.CASH -> "#10B981"
                        AccountType.OTHER -> "#6366F1"
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
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("confirm_add_account_button")
            ) {
                Text(appString(StringKeys.SAVE))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(appString(StringKeys.CANCEL))
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}
