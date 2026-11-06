package com.example.ui.debts

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Account
import com.example.data.model.Debt
import com.example.data.model.DebtType
import com.example.ui.motion.bounceClick
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.LocalAppTheme
import com.example.ui.theme.PureWhite
import com.example.util.LocalAppLanguage
import com.example.util.StringKeys
import com.example.util.appString
import com.example.util.formatBdt
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DebtsScreen(
    debts: List<Debt>,
    accounts: List<Account>,
    onAddDebt: (Debt, String?) -> Unit,
    onRecordRepayment: (Debt, Double, String?) -> Unit,
    onDeleteDebt: (Debt) -> Unit
) {
    val isDark = LocalAppTheme.current.isDark
    val language = LocalAppLanguage.current

    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, LENT, BORROWED, SETTLED
    var showAddDialog by remember { mutableStateOf(false) }
    var debtToRepay by remember { mutableStateOf<Debt?>(null) }
    var debtToDelete by remember { mutableStateOf<Debt?>(null) }

    // Computations
    val activeDebts = debts.filter { !it.isSettled }
    val totalReceivable = activeDebts.filter { it.type == DebtType.LENT.name }
        .sumOf { (it.amount - it.repaidAmount).coerceAtLeast(0.0) }
    val totalPayable = activeDebts.filter { it.type == DebtType.BORROWED.name }
        .sumOf { (it.amount - it.repaidAmount).coerceAtLeast(0.0) }
    val netBalance = totalReceivable - totalPayable

    val filteredDebts = debts.filter { debt ->
        when (selectedFilter) {
            "LENT" -> debt.type == DebtType.LENT.name && !debt.isSettled
            "BORROWED" -> debt.type == DebtType.BORROWED.name && !debt.isSettled
            "SETTLED" -> debt.isSettled
            else -> true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) Color(0xFF080F20) else Color(0xFFF4F7FF))
            .testTag("debts_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Title and Subtitle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = appString(StringKeys.DEBTS),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isDark) PureWhite else Color(0xFF080F20)
                        )
                        Text(
                            text = appString(StringKeys.DEBTS_SUBTITLE),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                        )
                    }
                }
            }

            // Summary Card (3 Column layout: পাবো / দিতে হবে / নেট স্থিতি)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = if (isDark) 6.dp else 2.dp,
                            shape = RoundedCornerShape(24.dp),
                            spotColor = if (isDark) Color(0xFF0070F3).copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.05f)
                        )
                        .border(
                            1.dp,
                            if (isDark) Color(0xFF1E3A8A) else Color(0xFFBFDBFE),
                            RoundedCornerShape(24.dp)
                        ),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF0B1733) else Color(0xFFFFFFFF)
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = appString(StringKeys.OVERVIEW),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0066FF)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isDark) Color(0xFF1E293B) else Color(0xFFEFF6FF))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${activeDebts.size} ${appString(StringKeys.ACTIVE_RECORDS)}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF3B82F6)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // 1. আমি পাবো (Receivable)
                            SummaryPill(
                                modifier = Modifier.weight(1f),
                                label = appString(StringKeys.TO_RECEIVE),
                                englishLabel = "Receivable",
                                amount = totalReceivable,
                                language = language,
                                icon = Icons.Default.CallReceived,
                                color = IncomeGreen,
                                isDark = isDark
                            )

                            // 2. দিতে হবে (Payable)
                            SummaryPill(
                                modifier = Modifier.weight(1f),
                                label = appString(StringKeys.TO_PAY),
                                englishLabel = "Payable",
                                amount = totalPayable,
                                language = language,
                                icon = Icons.Default.CallMade,
                                color = ExpenseRed,
                                isDark = isDark
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Net Status Banner
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (netBalance >= 0) {
                                        IncomeGreen.copy(alpha = if (isDark) 0.15f else 0.12f)
                                    } else {
                                        ExpenseRed.copy(alpha = if (isDark) 0.15f else 0.12f)
                                    }
                                )
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (netBalance >= 0) "মোট স্থিতি (আপনার লাভ)" else "মোট স্থিতি (দেনা আছে)",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (netBalance >= 0) IncomeGreen else ExpenseRed
                                )
                                Text(
                                    text = formatBdt(netBalance, language),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (netBalance >= 0) IncomeGreen else ExpenseRed
                                )
                            }
                        }
                    }
                }
            }

            // Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf(
                        "ALL" to "${appString(StringKeys.ALL)} (${debts.size})",
                        "LENT" to appString(StringKeys.TO_RECEIVE),
                        "BORROWED" to appString(StringKeys.TO_PAY),
                        "SETTLED" to appString(StringKeys.SETTLED)
                    )

                    filters.forEach { (key, label) ->
                        val isSelected = selectedFilter == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = key },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = if (isDark) Color(0xFF0F172A) else Color.White,
                                labelColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569),
                                selectedContainerColor = if (isDark) Color(0xFF0070F3) else Color(0xFF0066FF),
                                selectedLabelColor = Color.White
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0),
                                selectedBorderColor = Color.Transparent
                            )
                        )
                    }
                }
            }

            // Debts List
            if (filteredDebts.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) Color(0xFF0B1733) else Color.White
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Handshake,
                                contentDescription = null,
                                tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF0066FF),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = appString(StringKeys.NO_DEBTS),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isDark) PureWhite else Color(0xFF080F20)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = appString(StringKeys.NO_DEBTS_HINT),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                            )
                        }
                    }
                }
            } else {
                items(filteredDebts, key = { it.id }) { debt ->
                    DebtCardItem(
                        debt = debt,
                        isDark = isDark,
                        onRepayClick = { debtToRepay = debt },
                        onDeleteClick = { debtToDelete = debt }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(96.dp))
            }
        }

        // Floating Action Button to Add Debt
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .bounceClick()
                .testTag("add_debt_fab"),
            containerColor = if (isDark) Color(0xFF0070F3) else Color(0xFF0066FF),
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Debt Entry", modifier = Modifier.size(28.dp))
        }
    }

    // Add Debt Dialog
    if (showAddDialog) {
        AddDebtDialog(
            accounts = accounts,
            onDismiss = { showAddDialog = false },
            onSave = { newDebt, selectedAccountId ->
                onAddDebt(newDebt, selectedAccountId)
                showAddDialog = false
            }
        )
    }

    // Record Repayment Dialog
    if (debtToRepay != null) {
        RecordRepaymentDialog(
            debt = debtToRepay!!,
            accounts = accounts,
            onDismiss = { debtToRepay = null },
            onConfirm = { repayAmount, selectedAccountId ->
                onRecordRepayment(debtToRepay!!, repayAmount, selectedAccountId)
                debtToRepay = null
            }
        )
    }

    // Delete Confirmation Dialog
    if (debtToDelete != null) {
        AlertDialog(
            onDismissRequest = { debtToDelete = null },
            title = { Text("হিসাব মুছে ফেলবেন?") },
            text = { Text("\"${debtToDelete!!.personName}\"-এর এই দেনা-পাওনা রেকর্ডটি মুছে ফেলতে চান?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteDebt(debtToDelete!!)
                        debtToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text("মুছে ফেলুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { debtToDelete = null }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

@Composable
private fun SummaryPill(
    modifier: Modifier = Modifier,
    label: String,
    englishLabel: String,
    amount: Double,
    language: com.example.util.AppLanguage,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    isDark: Boolean
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = if (isDark) 0.12f else 0.08f))
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = color
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = formatBdt(amount, language),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isDark) PureWhite else Color(0xFF080F20)
            )
            Text(
                text = englishLabel,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
            )
        }
    }
}

@Composable
private fun DebtCardItem(
    debt: Debt,
    isDark: Boolean,
    onRepayClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val language = LocalAppLanguage.current
    val isLent = debt.type == DebtType.LENT.name
    val badgeColor = if (isLent) IncomeGreen else ExpenseRed
    val badgeLabel = if (isLent) "আমি পাবো (Lent)" else "দিতে হবে (Borrowed)"
    val remainingAmount = (debt.amount - debt.repaidAmount).coerceAtLeast(0.0)
    val progress = if (debt.amount > 0) (debt.repaidAmount / debt.amount).toFloat().coerceIn(0f, 1f) else 1f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDark) 4.dp else 1.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = if (isDark) Color(0xFF0070F3).copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.04f)
            )
            .border(
                1.dp,
                if (debt.isSettled) {
                    if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
                } else {
                    if (isDark) Color(0xFF1E3A8A) else Color(0xFFBFDBFE)
                },
                RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF0B1733) else Color.White
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Avatar, Name, Type Badge, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Initial Avatar
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    if (isLent) listOf(Color(0xFF059669), Color(0xFF10B981))
                                    else listOf(Color(0xFFDC2626), Color(0xFFEF4444))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = debt.personName.firstOrNull()?.uppercase() ?: "P",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = debt.personName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isDark) PureWhite else Color(0xFF080F20)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(badgeColor.copy(alpha = if (isDark) 0.2f else 0.12f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = badgeLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = badgeColor
                                )
                            }
                            if (debt.isSettled) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF10B981).copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "✓ পরিশোধিত",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = IncomeGreen
                                    )
                                }
                            }
                        }
                    }
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Amounts Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (debt.isSettled) "মোট পরিশোধিত" else "বাকি টাকা (Remaining)",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                    )
                    Text(
                        text = formatBdt(if (debt.isSettled) debt.amount else remainingAmount, language),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = if (debt.isSettled) IncomeGreen else (if (isDark) PureWhite else Color(0xFF080F20))
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "মূল হিসাব: ${formatBdt(debt.amount, language)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                    )
                    if (debt.repaidAmount > 0 && !debt.isSettled) {
                        Text(
                            text = "পরিশোধ হয়েছে: ${formatBdt(debt.repaidAmount, language)}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = IncomeGreen
                        )
                    }
                }
            }

            if (!debt.isSettled && debt.amount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = badgeColor,
                    trackColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
                )
            }

            // Note & Due Date
            if (debt.note.isNotBlank() || debt.dueDate.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (debt.note.isNotBlank()) {
                        Text(
                            text = "📝 ${debt.note}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (debt.dueDate.isNotBlank()) {
                        Text(
                            text = "📅 মেয়াদ: ${debt.dueDate}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0066FF)
                        )
                    }
                }
            }

            // Action: Repay Button (if not fully settled)
            if (!debt.isSettled) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.horizontalGradient(
                                if (isLent) listOf(Color(0xFF059669), Color(0xFF10B981))
                                else listOf(Color(0xFF0066FF), Color(0xFF0284C7))
                            )
                        )
                        .clickable { onRepayClick() }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isLent) "টাকা জমা দিন / রিসিভ করুন" else "টাকা পরিশোধ করুন",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun AddDebtDialog(
    accounts: List<Account>,
    onDismiss: () -> Unit,
    onSave: (Debt, String?) -> Unit
) {
    val isDark = LocalAppTheme.current.isDark
    var personName by remember { mutableStateOf("") }
    var debtType by remember { mutableStateOf(DebtType.LENT) }
    var amountText by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedAccountId by remember { mutableStateOf<String?>(null) }
    var adjustAccountBalance by remember { mutableStateOf(false) }

    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "নতুন দেনা-পাওনা যোগ করুন",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Type selector: আমি পাবো vs দিতে হবে
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (debtType == DebtType.LENT) IncomeGreen
                                else (if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                            )
                            .clickable { debtType = DebtType.LENT }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "আমি পাবো (Lent)",
                            fontWeight = FontWeight.Bold,
                            color = if (debtType == DebtType.LENT) Color.White else (if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)),
                            fontSize = 13.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (debtType == DebtType.BORROWED) ExpenseRed
                                else (if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                            )
                            .clickable { debtType = DebtType.BORROWED }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "দিতে হবে (Borrowed)",
                            fontWeight = FontWeight.Bold,
                            color = if (debtType == DebtType.BORROWED) Color.White else (if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)),
                            fontSize = 13.sp
                        )
                    }
                }

                OutlinedTextField(
                    value = personName,
                    onValueChange = { personName = it },
                    label = { Text("ব্যক্তির নাম (Person Name) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("টাকার পরিমাণ (৳ BDT) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("পরিশোধের সম্ভাব্য তারিখ (উদা: 2026-12-05)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("নোট বা বিবরণ (ঐচ্ছিক)") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (accounts.isNotEmpty()) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { adjustAccountBalance = !adjustAccountBalance }
                        ) {
                            androidx.compose.material3.Checkbox(
                                checked = adjustAccountBalance,
                                onCheckedChange = { adjustAccountBalance = it }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (debtType == DebtType.LENT) "অ্যাকাউন্ট ব্যালেন্স থেকে কর্তন করুন" else "অ্যাকাউন্টে টাকা যোগ করুন",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        if (adjustAccountBalance) {
                            Text(
                                text = "অ্যাকাউন্ট নির্বাচন করুন:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                accounts.take(3).forEach { acc ->
                                    val isSelected = selectedAccountId == acc.id || (selectedAccountId == null && acc == accounts.first())
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedAccountId = acc.id },
                                        label = { Text(acc.name, fontSize = 11.sp) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (personName.isNotBlank() && amt > 0) {
                        val newDebt = Debt(
                            personName = personName.trim(),
                            type = debtType.name,
                            amount = amt,
                            repaidAmount = 0.0,
                            date = todayStr,
                            dueDate = dueDate.trim(),
                            note = note.trim(),
                            isSettled = false
                        )
                        val accId = if (adjustAccountBalance) (selectedAccountId ?: accounts.firstOrNull()?.id) else null
                        onSave(newDebt, accId)
                    }
                },
                enabled = personName.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (debtType == DebtType.LENT) IncomeGreen else Color(0xFF0066FF)
                )
            ) {
                Text(appString(StringKeys.SAVE))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(appString(StringKeys.CANCEL))
            }
        }
    )
}

@Composable
private fun RecordRepaymentDialog(
    debt: Debt,
    accounts: List<Account>,
    onDismiss: () -> Unit,
    onConfirm: (Double, String?) -> Unit
) {
    val remaining = (debt.amount - debt.repaidAmount).coerceAtLeast(0.0)
    var repayAmountText by remember { mutableStateOf(if (remaining > 0) remaining.toInt().toString() else "") }
    var selectedAccountId by remember { mutableStateOf<String?>(accounts.firstOrNull()?.id) }
    var adjustAccount by remember { mutableStateOf(accounts.isNotEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "পরিশোধ / জমার হিসাব",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "${debt.personName}-এর বাকি আছে: ৳ ${remaining.toInt()}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )

                OutlinedTextField(
                    value = repayAmountText,
                    onValueChange = { repayAmountText = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("পরিশোধের পরিমাণ (৳ BDT)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (accounts.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { adjustAccount = !adjustAccount }
                    ) {
                        androidx.compose.material3.Checkbox(
                            checked = adjustAccount,
                            onCheckedChange = { adjustAccount = it }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (debt.type == DebtType.LENT.name) "টাকা ওয়ালেট অ্যাকাউন্টে যোগ করুন" else "টাকা ওয়ালেট অ্যাকাউন্ট থেকে কাটুন",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    if (adjustAccount) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            accounts.take(3).forEach { acc ->
                                val isSelected = selectedAccountId == acc.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedAccountId = acc.id },
                                    label = { Text(acc.name, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = repayAmountText.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        onConfirm(amt, if (adjustAccount) selectedAccountId else null)
                    }
                },
                enabled = (repayAmountText.toDoubleOrNull() ?: 0.0) > 0,
                colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen)
            ) {
                Text(appString(StringKeys.CONFIRM))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(appString(StringKeys.CANCEL))
            }
        }
    )
}
