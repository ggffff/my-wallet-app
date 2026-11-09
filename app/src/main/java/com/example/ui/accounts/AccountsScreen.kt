package com.example.ui.accounts

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.ui.dialogs.UpdateAccountDialog
import com.example.ui.motion.AnimatedBdtCounter
import com.example.ui.motion.GlassmorphicCardSheen
import com.example.ui.motion.PulsingAuraGlow
import com.example.ui.motion.bounceClick
import com.example.ui.motion.neonGlow
import com.example.ui.motion.staggeredEntrance
import com.example.ui.motion.tilt3D
import com.example.ui.theme.ColorBank
import com.example.ui.theme.ColorBkash
import com.example.ui.theme.ColorCash
import com.example.ui.theme.ColorNagad
import com.example.ui.theme.ColorRocket
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.LocalAppTheme
import com.example.ui.theme.PremiumBlue
import com.example.ui.theme.PremiumPink
import com.example.ui.theme.PureWhite
import com.example.util.LocalAppLanguage
import com.example.util.StringKeys
import com.example.util.appString
import com.example.util.formatBdt
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    accounts: List<Account>,
    onAddAccountClicked: () -> Unit,
    onUpdateAccount: (Account) -> Unit = {},
    onDeleteAccount: (Account) -> Unit
) {
    val context = LocalContext.current
    val language = LocalAppLanguage.current
    val coroutineScope = rememberCoroutineScope()
    var isHidden by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredAccounts = remember(accounts, selectedFilter) {
        when (selectedFilter) {
            "BANK" -> accounts.filter { it.type == AccountType.BANK.name }
            "MFS" -> accounts.filter {
                it.type == AccountType.MFS.name || it.type == AccountType.BKASH.name ||
                it.type == AccountType.NAGAD.name || it.type == AccountType.ROCKET.name
            }
            "CASH" -> accounts.filter {
                it.type == AccountType.CASH_FLOW.name || it.type == AccountType.CASH.name
            }
            else -> accounts
        }
    }

    val totalBalance = remember(accounts) { accounts.sumOf { it.balance } }

    var selectedAccountForDetails by remember { mutableStateOf<Account?>(null) }
    var accountToEdit by remember { mutableStateOf<Account?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddAccountClicked,
                containerColor = PremiumBlue,
                contentColor = PureWhite,
                modifier = Modifier
                    .testTag("add_account_fab")
                    .neonGlow(color = PremiumBlue, shape = CircleShape, strokeWidth = 1.dp)
                    .bounceClick(scaleDown = 0.9f)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Account")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("accounts_screen"),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .staggeredEntrance(index = 0),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = appString(StringKeys.ACCOUNTS),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Bank, MFS & Cash flow accounts in BDT",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = { isHidden = !isHidden },
                        modifier = Modifier.bounceClick(scaleDown = 0.9f) { isHidden = !isHidden }
                    ) {
                        Icon(
                            imageVector = if (isHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle Balance",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Total Liquidity Card with Adaptive Theme Gradient
            item {
                val isDark = LocalAppTheme.current.isDark
                val liquidityGradient = if (isDark) {
                    listOf(DarkNavy, Color(0xFF142443), Color(0xFF1E365E))
                } else {
                    listOf(Color(0xFF1E40AF), Color(0xFF2563EB), Color(0xFF3B82F6))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .staggeredEntrance(index = 1)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(22.dp), spotColor = Color.Black.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Brush.linearGradient(liquidityGradient))
                                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(22.dp))
                                .padding(20.dp)
                        ) {
                            GlassmorphicCardSheen(modifier = Modifier.matchParentSize())

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Total Liquidity (All Accounts)",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White.copy(alpha = 0.75f)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    AnimatedBdtCounter(
                                        targetAmount = totalBalance,
                                        language = language,
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        ),
                                        color = PureWhite,
                                        isMasked = isHidden
                                    )
                                    Text(
                                        text = "${accounts.size} active accounts (Bank, MFS, Cash flow)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .background(
                                            Brush.linearGradient(listOf(PremiumBlue, PremiumPink)),
                                            CircleShape
                                        )
                                        .neonGlow(color = PremiumPink, shape = CircleShape, strokeWidth = 1.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CreditCard,
                                        contentDescription = null,
                                        tint = PureWhite,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Account Type Filter Chips: All, Bank, MFS, Cash Flow
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .staggeredEntrance(index = 2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == "ALL",
                        onClick = { selectedFilter = "ALL" },
                        label = { Text("All (${accounts.size})") }
                    )
                    FilterChip(
                        selected = selectedFilter == "BANK",
                        onClick = { selectedFilter = "BANK" },
                        label = { Text("Bank") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PremiumBlue.copy(alpha = 0.2f),
                            selectedLabelColor = PremiumBlue
                        )
                    )
                    FilterChip(
                        selected = selectedFilter == "MFS",
                        onClick = { selectedFilter = "MFS" },
                        label = { Text("MFS") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PremiumPink.copy(alpha = 0.2f),
                            selectedLabelColor = PremiumPink
                        )
                    )
                    FilterChip(
                        selected = selectedFilter == "CASH",
                        onClick = { selectedFilter = "CASH" },
                        label = { Text("Cash Flow") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IncomeGreen.copy(alpha = 0.2f),
                            selectedLabelColor = IncomeGreen
                        )
                    )
                }
            }

            // 3D Animated Cards Carousel
            item {
                Text(
                    text = "Interactive 3D Cards",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 12.dp)
                        .staggeredEntrance(index = 3)
                )
            }

            if (filteredAccounts.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .staggeredEntrance(index = 3),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CreditCard,
                                contentDescription = null,
                                tint = PremiumBlue,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Accounts Found",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Add your Bank, MFS (bKash/Nagad/Rocket), or Cash flow wallet.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                            )
                            OutlinedButton(
                                onClick = onAddAccountClicked,
                                modifier = Modifier.bounceClick()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(appString(StringKeys.ADD_ACCOUNT))
                            }
                        }
                    }
                }
            } else {
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.staggeredEntrance(index = 3)
                    ) {
                        itemsIndexed(filteredAccounts, key = { _, acc -> acc.id }) { index, acc ->
                            Modern3DAccountCard(
                                account = acc,
                                isHidden = isHidden,
                                language = language,
                                onClick = { selectedAccountForDetails = acc }
                            )
                        }

                        // Add new card placeholder
                        item {
                            AddAccountActionCard(onClick = onAddAccountClicked)
                        }
                    }
                }
            }

            // Detailed Accounts List
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 12.dp)
                        .staggeredEntrance(index = 4),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Manage Accounts (${filteredAccounts.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(
                        onClick = onAddAccountClicked,
                        modifier = Modifier.bounceClick()
                    ) {
                        Text("+ Add Account", fontWeight = FontWeight.Bold, color = PremiumBlue)
                    }
                }
            }

            itemsIndexed(filteredAccounts, key = { _, acc -> "detail_${acc.id}" }) { index, acc ->
                AccountDetailListItem(
                    account = acc,
                    totalBalance = totalBalance,
                    isHidden = isHidden,
                    language = language,
                    modifier = Modifier.staggeredEntrance(index = 5 + index),
                    onClick = { selectedAccountForDetails = acc },
                    onEditBalance = { accountToEdit = acc },
                    onDelete = { onDeleteAccount(acc) }
                )
            }
        }
    }

    // Modal Details Sheet when an account is selected
    if (selectedAccountForDetails != null) {
        val acc = selectedAccountForDetails!!
        ModalBottomSheet(
            onDismissRequest = { selectedAccountForDetails = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .padding(bottom = 32.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = acc.name,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Account Type: ${acc.type}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(PremiumBlue.copy(alpha = 0.2f))
                            .neonGlow(color = PremiumBlue, shape = RoundedCornerShape(10.dp), strokeWidth = 1.dp)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "ACTIVE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = PremiumBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Balance summary box with Edit button
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Current Balance",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formatBdt(acc.balance, language),
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = PremiumBlue
                            )
                        }

                        Button(
                            onClick = {
                                val target = acc
                                coroutineScope.launch { sheetState.hide() }
                                selectedAccountForDetails = null
                                accountToEdit = target
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PremiumBlue, contentColor = PureWhite)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Update Balance")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Account Number Card with Copy button
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Account / Mobile Number",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (acc.accountNumber.isNotBlank()) acc.accountNumber else "Not Specified",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (acc.accountNumber.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    val clip = ClipData.newPlainText("Account Number", acc.accountNumber)
                                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                    cm?.setPrimaryClip(clip)
                                    Toast.makeText(context, "Account number copied", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = PremiumBlue)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions: Delete Account
                Button(
                    onClick = {
                        onDeleteAccount(acc)
                        coroutineScope.launch {
                            sheetState.hide()
                            selectedAccountForDetails = null
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ExpenseRed.copy(alpha = 0.15f),
                        contentColor = ExpenseRed
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Account", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Direct Update Account & Balance Dialog
    if (accountToEdit != null) {
        UpdateAccountDialog(
            account = accountToEdit!!,
            onDismiss = { accountToEdit = null },
            onUpdate = { updated ->
                onUpdateAccount(updated)
                accountToEdit = null
            },
            onDelete = { toDel ->
                onDeleteAccount(toDel)
                accountToEdit = null
            }
        )
    }
}

/**
 * Modern 3D Card with hardware-accelerated interactive tilt,
 * metallic gradient, chip, NFC emblem, and embossed number.
 */
@Composable
fun Modern3DAccountCard(
    account: Account,
    isHidden: Boolean,
    language: com.example.util.AppLanguage,
    onClick: () -> Unit = {}
) {
    val typeLabel = when (account.type) {
        AccountType.BANK.name -> "Bank"
        AccountType.MFS.name, AccountType.BKASH.name, AccountType.NAGAD.name, AccountType.ROCKET.name -> "MFS"
        AccountType.CASH_FLOW.name, AccountType.CASH.name -> "Cash Flow"
        else -> account.type
    }

    val gradientColors = when (account.type) {
        AccountType.BKASH.name -> listOf(ColorBkash, Color(0xFFC00C58), Color(0xFF8B073E))
        AccountType.NAGAD.name -> listOf(ColorNagad, Color(0xFFEA580C), Color(0xFFC2410C))
        AccountType.ROCKET.name -> listOf(ColorRocket, Color(0xFF6B21A8), Color(0xFF581C87))
        AccountType.MFS.name -> listOf(PremiumPink, Color(0xFFDB2777), Color(0xFFBE185D))
        AccountType.BANK.name -> listOf(PremiumBlue, Color(0xFF2563EB), Color(0xFF1D4ED8))
        AccountType.CASH_FLOW.name, AccountType.CASH.name -> listOf(ColorCash, Color(0xFF059669), Color(0xFF047857))
        else -> listOf(PremiumBlue, PremiumPink, Color(0xFF9333EA))
    }

    Card(
        modifier = Modifier
            .width(280.dp)
            .height(178.dp)
            .tilt3D(maxRotationX = 9f, maxRotationY = 9f, elevationDp = 14f)
            .neonGlow(color = gradientColors.first().copy(alpha = 0.5f), shape = RoundedCornerShape(22.dp), strokeWidth = 1.dp)
            .shadow(14.dp, RoundedCornerShape(22.dp))
            .bounceClick(scaleDown = 0.98f, onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(gradientColors))
                .padding(18.dp)
        ) {
            // Metallic specular sheen overlay
            GlassmorphicCardSheen(modifier = Modifier.matchParentSize())

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top row: Chip & NFC & Account Type
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Metallic chip
                        Box(
                            modifier = Modifier
                                .size(width = 30.dp, height = 22.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFEAB308))
                                .border(1.dp, Color(0xFFCA8A04), RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Nfc,
                            contentDescription = "Contactless",
                            tint = PureWhite.copy(alpha = 0.85f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.3f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = typeLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = PureWhite
                        )
                    }
                }

                // Middle: Account Number formatted
                val formattedNumber = remember(account.accountNumber) {
                    if (account.accountNumber.isBlank()) "•••• •••• •••• 0000"
                    else {
                        val last4 = account.accountNumber.takeLast(4).padStart(4, '0')
                        "•••• •••• •••• $last4"
                    }
                }

                Text(
                    text = formattedNumber,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = PureWhite.copy(alpha = 0.95f)
                )

                // Bottom row: Name & Balance
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "ACCOUNT NAME",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = PureWhite.copy(alpha = 0.7f)
                        )
                        Text(
                            text = account.name,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = PureWhite,
                            maxLines = 1
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "BALANCE",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = PureWhite.copy(alpha = 0.7f)
                        )
                        AnimatedBdtCounter(
                            targetAmount = account.balance,
                            language = language,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = PureWhite,
                            isMasked = isHidden
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddAccountActionCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(178.dp)
            .bounceClick(scaleDown = 0.95f, onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(PremiumBlue.copy(alpha = 0.15f), CircleShape)
                    .neonGlow(color = PremiumBlue.copy(alpha = 0.5f), shape = CircleShape, strokeWidth = 1.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Account",
                    tint = PremiumBlue,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "+ Add New\n(Bank/MFS/Cash)",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = PremiumBlue,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AccountDetailListItem(
    account: Account,
    totalBalance: Double,
    isHidden: Boolean,
    language: com.example.util.AppLanguage,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onEditBalance: () -> Unit,
    onDelete: () -> Unit
) {
    val percentage = if (totalBalance > 0) ((account.balance / totalBalance) * 100).toInt().coerceIn(0, 100) else 0

    val brandColor = when (account.type) {
        AccountType.BKASH.name -> ColorBkash
        AccountType.NAGAD.name -> ColorNagad
        AccountType.ROCKET.name -> ColorRocket
        AccountType.MFS.name -> PremiumPink
        AccountType.BANK.name -> PremiumBlue
        AccountType.CASH_FLOW.name, AccountType.CASH.name -> ColorCash
        else -> PremiumPink
    }

    val iconVector = when (account.type) {
        AccountType.MFS.name, AccountType.BKASH.name, AccountType.NAGAD.name, AccountType.ROCKET.name -> Icons.Default.PhoneAndroid
        AccountType.BANK.name -> Icons.Default.AccountBalance
        AccountType.CASH_FLOW.name, AccountType.CASH.name -> Icons.Default.AttachMoney
        else -> Icons.Default.CreditCard
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .shadow(1.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.04f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .bounceClick(scaleDown = 0.98f, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(brandColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .neonGlow(color = brandColor.copy(alpha = 0.35f), shape = RoundedCornerShape(12.dp), strokeWidth = 1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = brandColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = account.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${account.type} • ${if (account.accountNumber.isNotBlank()) "Ending in ${account.accountNumber.takeLast(4)}" else "Active"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    AnimatedBdtCounter(
                        targetAmount = account.balance,
                        language = language,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        isMasked = isHidden
                    )
                    Text(
                        text = "$percentage% of total",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onEditBalance,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Balance",
                        tint = PremiumBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Account",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
