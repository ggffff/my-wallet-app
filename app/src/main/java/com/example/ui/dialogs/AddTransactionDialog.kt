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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
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
import com.example.data.model.Account
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.util.StringKeys
import com.example.util.appString
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    accounts: List<Account>,
    initialType: TransactionType = TransactionType.SPENT,
    onDismiss: () -> Unit,
    onConfirm: (Transaction) -> Unit,
    onNeedAddAccount: () -> Unit
) {
    var selectedType by remember { mutableStateOf(initialType) }
    var amountText by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val incomeCategories = listOf("Job Salary", "Pathao Drive", "Tips", "Business", "Others")
    val spentCategories = listOf("Food", "Family", "Rental cost", "Others")

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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (selectedType == TransactionType.INCOME) appString(StringKeys.ADD_INCOME) else appString(StringKeys.ADD_EXPENSE),
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
                // Type Switcher Tabs (Income / Spent)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp)
                ) {
                    val isIncome = selectedType == TransactionType.INCOME
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isIncome) IncomeGreen else Color.Transparent)
                            .clickable { selectedType = TransactionType.INCOME }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = appString(StringKeys.INCOME),
                            fontWeight = if (isIncome) FontWeight.Bold else FontWeight.Normal,
                            color = if (isIncome) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!isIncome) ExpenseRed else Color.Transparent)
                            .clickable { selectedType = TransactionType.SPENT }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = appString(StringKeys.SPENT),
                            fontWeight = if (!isIncome) FontWeight.Bold else FontWeight.Normal,
                            color = if (!isIncome) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Amount Field (User input, no fake default)
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (BDT ৳)") },
                    placeholder = { Text("0.00") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transaction_amount_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Title Field
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title / Purpose (e.g. Weekly Groceries, Fuel)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transaction_title_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Category Chips
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val currentCategories = if (selectedType == TransactionType.INCOME) incomeCategories else spentCategories
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    currentCategories.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        val catColor = if (selectedType == TransactionType.INCOME) IncomeGreen else ExpenseRed
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) catColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) catColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Account Selection
                if (accounts.isEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "No accounts available",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        TextButton(onClick = onNeedAddAccount) {
                            Text("+ Add Account", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    ExposedDropdownMenuBox(
                        expanded = accountDropdownExpanded,
                        onExpandedChange = { accountDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedAccount?.let { "${it.name} (৳${it.balance})" } ?: "Select Account",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Account / Payment Source") },
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

                // Date Field
                OutlinedTextField(
                    value = transactionDate,
                    onValueChange = { transactionDate = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
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
                    val amount = amountText.toDoubleOrNull()
                    if (amount == null || amount <= 0) {
                        errorMessage = "Please enter a valid amount greater than 0."
                        return@Button
                    }
                    if (title.isBlank()) {
                        title = selectedCategory
                    }
                    if (selectedAccount == null && accounts.isNotEmpty()) {
                        selectedAccount = accounts.first()
                    }

                    val txn = Transaction(
                        title = title.trim(),
                        amount = amount,
                        type = selectedType.name,
                        category = selectedCategory,
                        accountId = selectedAccount?.id ?: "cash_default",
                        accountName = selectedAccount?.name ?: "Cash",
                        date = transactionDate.trim(),
                        note = note.trim()
                    )
                    onConfirm(txn)
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedType == TransactionType.INCOME) IncomeGreen else ExpenseRed
                ),
                modifier = Modifier.testTag("confirm_add_transaction_button")
            ) {
                Text(appString(StringKeys.SAVE), color = Color.White)
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
