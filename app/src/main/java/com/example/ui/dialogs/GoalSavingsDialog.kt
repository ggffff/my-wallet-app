package com.example.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.Account
import com.example.data.model.Goal
import com.example.util.StringKeys
import com.example.util.appString
import com.example.util.formatBdt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalSavingsDialog(
    goal: Goal,
    accounts: List<Account>,
    onDismiss: () -> Unit,
    onConfirm: (deltaAmount: Double, accountId: String?) -> Unit
) {
    var isDeposit by remember { mutableStateOf(true) }
    var amountText by remember { mutableStateOf("") }
    var selectedAccount by remember(accounts) { mutableStateOf(accounts.firstOrNull()) }
    var accountDropdownExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "${if (isDeposit) "Add Money to" else "Withdraw from"} ${goal.title}",
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
                    text = "Current: ${formatBdt(goal.savedAmount)} / ${formatBdt(goal.targetAmount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = isDeposit,
                        onClick = { isDeposit = true },
                        label = { Text("Deposit (+)") }
                    )
                    FilterChip(
                        selected = !isDeposit,
                        onClick = { isDeposit = false },
                        label = { Text("Withdraw (-)") }
                    )
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (BDT ৳)") },
                    placeholder = { Text("0.00") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("goal_adjust_amount_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                if (accounts.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = accountDropdownExpanded,
                        onExpandedChange = { accountDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedAccount?.let { "${it.name} (৳${it.balance})" } ?: "Select Account",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(if (isDeposit) "Deduct from Account" else "Return to Account") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = accountDropdownExpanded,
                            onDismissRequest = { accountDropdownExpanded = false }
                        ) {
                            accounts.forEach { acc ->
                                DropdownMenuItem(
                                    text = { Text("${acc.name} (${acc.type}) - ৳${acc.balance}") },
                                    onClick = {
                                        selectedAccount = acc
                                        accountDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

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
                    val amount = amountText.toDoubleOrNull()
                    if (amount == null || amount <= 0) {
                        errorMessage = "Please enter an amount greater than 0."
                        return@Button
                    }
                    if (!isDeposit && amount > goal.savedAmount) {
                        errorMessage = "Cannot withdraw more than saved balance."
                        return@Button
                    }
                    val delta = if (isDeposit) amount else -amount
                    onConfirm(delta, selectedAccount?.id)
                },
                shape = RoundedCornerShape(10.dp)
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
