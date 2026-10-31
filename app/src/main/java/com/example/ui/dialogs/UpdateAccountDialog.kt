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
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.sp
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.ui.motion.bounceClick
import com.example.ui.motion.neonGlow
import com.example.ui.theme.ColorBank
import com.example.ui.theme.ColorBkash
import com.example.ui.theme.ColorCash
import com.example.ui.theme.ColorNagad
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PremiumBlue
import com.example.ui.theme.PremiumPink
import com.example.ui.theme.PureWhite
import com.example.util.formatBdt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UpdateAccountDialog(
    account: Account,
    onDismiss: () -> Unit,
    onUpdate: (Account) -> Unit,
    onDelete: (Account) -> Unit
) {
    var name by remember { mutableStateOf(account.name) }
    var selectedType by remember {
        mutableStateOf(
            try {
                AccountType.valueOf(account.type)
            } catch (e: Exception) {
                AccountType.BANK
            }
        )
    }
    var accountNumber by remember { mutableStateOf(account.accountNumber) }
    var balanceText by remember { mutableStateOf(if (account.balance % 1.0 == 0.0) account.balance.toLong().toString() else account.balance.toString()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    val typeOptions = listOf(
        Triple(AccountType.BANK, ColorBank, Icons.Default.AccountBalance),
        Triple(AccountType.MFS, PremiumPink, Icons.Default.PhoneAndroid),
        Triple(AccountType.CASH_FLOW, IncomeGreen, Icons.Default.AttachMoney)
    )

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = {
                Text("Delete Account?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Are you sure you want to delete \"${account.name}\"? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete(account)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text("Delete", color = PureWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Update Account & Balance",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                IconButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Account",
                        tint = ExpenseRed
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Account Type Selector: Bank / MFS / Cash Flow
                Text(
                    text = "Account Type",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    typeOptions.forEach { (type, color, icon) ->
                        val isSelected = selectedType == type ||
                                (type == AccountType.MFS && (selectedType == AccountType.BKASH || selectedType == AccountType.NAGAD || selectedType == AccountType.ROCKET)) ||
                                (type == AccountType.CASH_FLOW && selectedType == AccountType.CASH)

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                                .neonGlow(
                                    color = if (isSelected) color else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp),
                                    strokeWidth = if (isSelected) 1.5.dp else 0.dp
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) color else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedType = type }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = type.defaultLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Balance Update Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "Current Balance: ${formatBdt(account.balance, com.example.util.AppLanguage.ENGLISH)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = balanceText,
                            onValueChange = { balanceText = it },
                            label = { Text("Update Balance (BDT ৳)") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("update_balance_input"),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick +/- adjustments
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(500.0, 1000.0, 5000.0).forEach { delta ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(PremiumBlue.copy(alpha = 0.15f))
                                        .clickable {
                                            val current = balanceText.toDoubleOrNull() ?: account.balance
                                            val newVal = (current + delta).coerceAtLeast(0.0)
                                            balanceText = if (newVal % 1.0 == 0.0) newVal.toLong().toString() else newVal.toString()
                                        }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "+৳${delta.toInt()}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = PremiumBlue
                                    )
                                }
                            }
                        }
                    }
                }

                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Account Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Number field
                OutlinedTextField(
                    value = accountNumber,
                    onValueChange = { accountNumber = it },
                    label = { Text("Account / Wallet Number (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
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
                    if (isSubmitting) return@Button
                    if (name.isBlank()) {
                        errorMessage = "Please enter an account name."
                        return@Button
                    }
                    isSubmitting = true
                    val updatedBalance = balanceText.toDoubleOrNull() ?: account.balance
                    val updatedColorHex = when (selectedType) {
                        AccountType.BANK -> "#3882F6"
                        AccountType.MFS -> "#EC4899"
                        AccountType.CASH_FLOW, AccountType.CASH -> "#10B981"
                        else -> account.colorHex
                    }
                    val updated = account.copy(
                        name = name.trim(),
                        type = selectedType.name,
                        accountNumber = accountNumber.trim(),
                        balance = updatedBalance,
                        colorHex = updatedColorHex
                    )
                    onUpdate(updated)
                },
                enabled = !isSubmitting,
                modifier = Modifier
                    .neonGlow(color = PremiumBlue, shape = RoundedCornerShape(10.dp), strokeWidth = 1.dp)
                    .bounceClick(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PremiumBlue, contentColor = PureWhite)
            ) {
                Text("Update", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}
