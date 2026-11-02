package com.example.ui.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.Bill
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.data.model.UserProfile
import com.example.ui.dialogs.UpdateAccountDialog
import com.example.ui.motion.AnimatedBdtCounter
import com.example.ui.motion.bounceClick
import com.example.ui.motion.staggeredEntrance
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PureWhite
import com.example.util.AppLanguage
import com.example.util.LocalAppLanguage
import com.example.util.StringKeys
import com.example.util.appString
import com.example.util.formatBdt

@Composable
fun HomeScreen(
    userProfile: UserProfile?,
    accounts: List<Account>,
    transactions: List<Transaction>,
    bills: List<Bill>,
    onAddAccountClicked: () -> Unit,
    onUpdateAccount: (Account) -> Unit = {},
    onDeleteAccount: (Account) -> Unit = {},
    onAddTransactionClicked: (TransactionType) -> Unit,
    onViewAllStatementsClicked: () -> Unit,
    onViewBillsClicked: () -> Unit,
    onViewGoalsClicked: () -> Unit,
    onOpenAiAdvisor: () -> Unit = {},
    onOpenSettings: () -> Unit = {}
) {
    val language = LocalAppLanguage.current
    var isBalanceHidden by remember { mutableStateOf(false) }
    var accountToUpdate by remember { mutableStateOf<Account?>(null) }

    val netBalance = remember(accounts) {
        accounts.sumOf { it.balance }
    }

    val totalIncome = remember(transactions) {
        transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    }

    val totalExpense = remember(transactions) {
        transactions.filter { it.type == "SPENT" }.sumOf { it.amount }
    }

    val upcomingBills = remember(bills) {
        bills.filter { !it.isPaid && it.notifyEnabled }.take(2)
    }

    val eyeRotation by animateFloatAsState(
        targetValue = if (isBalanceHidden) 180f else 0f,
        animationSpec = spring(),
        label = "eyeRotation"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // 1. Top Header: My Wallet title, Greeting, Sun/Moon, Notification and Settings Icons
        item {
            TimeBasedGreetingHeader(
                userProfile = userProfile,
                unreadNotificationsCount = upcomingBills.size,
                onOpenNotifications = onViewBillsClicked,
                onOpenSettings = onOpenSettings
            )
        }

        // 2. Large Net Balance Hero Card (Clean Modern Card - No harsh neon)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .shadow(4.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF1E3A8A),
                                    Color(0xFF2563EB),
                                    Color(0xFF1D4ED8)
                                )
                            )
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                        .padding(22.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Net Balance",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp
                                    ),
                                    color = Color(0xFFDCEBFE)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { isBalanceHidden = !isBalanceHidden },
                                    modifier = Modifier
                                        .size(30.dp)
                                        .rotate(eyeRotation)
                                ) {
                                    Icon(
                                        imageVector = if (isBalanceHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle Balance",
                                        tint = PureWhite,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Clean currency pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "BDT (৳)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = PureWhite
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Large Net Balance Counter
                        AnimatedBdtCounter(
                            targetAmount = netBalance,
                            language = language,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 35.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = PureWhite,
                            isMasked = isBalanceHidden
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Total Income and Total Spent Clean Capsules
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Total Income Capsule
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.Black.copy(alpha = 0.20f))
                                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                                    .bounceClick(scaleDown = 0.96f) {
                                        onAddTransactionClicked(TransactionType.INCOME)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(IncomeGreen.copy(alpha = 0.25f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDownward,
                                            contentDescription = "Income",
                                            tint = IncomeGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Total Income",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            ),
                                            color = PureWhite.copy(alpha = 0.85f)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        AnimatedBdtCounter(
                                            targetAmount = totalIncome,
                                            language = language,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            ),
                                            color = PureWhite,
                                            isMasked = isBalanceHidden
                                        )
                                    }
                                }
                            }

                            // Total Spent Capsule
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.Black.copy(alpha = 0.20f))
                                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                                    .bounceClick(scaleDown = 0.96f) {
                                        onAddTransactionClicked(TransactionType.SPENT)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(ExpenseRed.copy(alpha = 0.25f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowUpward,
                                            contentDescription = "Spent",
                                            tint = ExpenseRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Total Spent",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            ),
                                            color = PureWhite.copy(alpha = 0.85f)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        AnimatedBdtCounter(
                                            targetAmount = totalExpense,
                                            language = language,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            ),
                                            color = PureWhite,
                                            isMasked = isBalanceHidden
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Five 3D Quick Action Picture Items
        // User requested: "Apps Theme And Visual Effects Remove koro, Suggest Pic Gulo Animated dew 2/3ta"
        // 3 Animated Suggested Pictures:
        // 1. Saving Goals: Target Breathing Pulse & Micro-aim rotation
        // 2. AI Tips: Magical Sparkle rotation & shimmer scale
        // 3. Add Income / Add Spent: Floating bounce vertical bobbing motion
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Picture 1: Add Income
                QuickAction3DItem(
                    drawableRes = R.drawable.img_quick_add_income,
                    label = if (language == AppLanguage.BANGLA) "আয় যোগ" else "Add Income",
                    glowColor = IncomeGreen,
                    testTag = "quick_action_add_income",
                    onClick = { onAddTransactionClicked(TransactionType.INCOME) }
                )

                // Picture 2: Add Spent
                QuickAction3DItem(
                    drawableRes = R.drawable.img_quick_add_spent,
                    label = if (language == AppLanguage.BANGLA) "ব্যয় যোগ" else "Add Spent",
                    glowColor = ExpenseRed,
                    testTag = "quick_action_add_spent",
                    onClick = { onAddTransactionClicked(TransactionType.SPENT) }
                )

                // Picture 3: Saving Goals
                QuickAction3DItem(
                    drawableRes = R.drawable.img_quick_savings_goal,
                    label = if (language == AppLanguage.BANGLA) "সঞ্চয় লক্ষ্য" else "Saving Goals",
                    glowColor = Color(0xFF2563EB),
                    testTag = "quick_action_saving_goals",
                    onClick = onViewGoalsClicked
                )

                // Picture 4: Bills & Dues
                QuickAction3DItem(
                    drawableRes = R.drawable.img_quick_bills_dues,
                    label = if (language == AppLanguage.BANGLA) "বিল ও বকেয়া" else "Bills & Dues",
                    glowColor = Color(0xFF0284C7),
                    testTag = "quick_action_bills_dues",
                    onClick = onViewBillsClicked
                )

                // Picture 5: AI Tips
                QuickAction3DItem(
                    drawableRes = R.drawable.img_quick_ai_tips,
                    label = if (language == AppLanguage.BANGLA) "এআই টিপস" else "AI Tips",
                    glowColor = Color(0xFF9333EA),
                    testTag = "quick_action_ai_tips",
                    onClick = onOpenAiAdvisor
                )
            }
        }

        // 4. Upcoming Bill Glassmorphism Card
        item {
            val nextBill = upcomingBills.firstOrNull() ?: bills.firstOrNull { !it.isPaid }
            val billTitle = nextBill?.title ?: "WiFi bill"
            val billAmount = nextBill?.amount ?: 450.0
            val billDueDate = nextBill?.dueDate ?: "2026-11-25"

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
                    .shadow(1.5.dp, RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.06f))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                    .bounceClick { onViewBillsClicked() },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFFF59E0B).copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Bill Alert",
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Upcoming Bill: $billTitle",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Amount: ${formatBdt(billAmount, language)} • Due: $billDueDate",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF59E0B).copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "View",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            color = Color(0xFFF59E0B)
                        )
                    }
                }
            }
        }

        // 5. Accounts Section
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Accounts",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                TextButton(
                    onClick = onAddAccountClicked,
                    modifier = Modifier.bounceClick()
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Add Account",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(accounts) { account ->
                    HomeAccountCard(
                        account = account,
                        isHidden = isBalanceHidden,
                        language = language,
                        onClick = { accountToUpdate = account }
                    )
                }

                item {
                    HomeAddAccountCard(onClick = onAddAccountClicked)
                }
            }
        }

        // 6. Recent Transactions Section
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 10.dp)
                    .staggeredEntrance(index = 5),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = appString(StringKeys.RECENT_TRANSACTIONS),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (transactions.isNotEmpty()) {
                    TextButton(onClick = onViewAllStatementsClicked) {
                        Text("See All", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        if (transactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = appString(StringKeys.NO_TRANSACTIONS),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = appString(StringKeys.EMPTY_STATE_HINT),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = { onAddTransactionClicked(TransactionType.INCOME) },
                                modifier = Modifier.border(1.dp, Color(0xFF10B981).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            ) {
                                Text("+ Income", color = Color(0xFF10B981))
                            }
                            OutlinedButton(
                                onClick = { onAddTransactionClicked(TransactionType.SPENT) },
                                modifier = Modifier.border(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            ) {
                                Text("- Expense", color = Color(0xFFEF4444))
                            }
                        }
                    }
                }
            }
        } else {
            itemsIndexed(transactions.take(6)) { index, txn ->
                HomeTransactionRow(
                    txn = txn,
                    isHidden = isBalanceHidden,
                    language = language,
                    modifier = Modifier.staggeredEntrance(index = 6 + index)
                )
            }
        }
    }

    // Direct Update Account / Balance Dialog from Home
    if (accountToUpdate != null) {
        UpdateAccountDialog(
            account = accountToUpdate!!,
            onDismiss = { accountToUpdate = null },
            onUpdate = { updated ->
                onUpdateAccount(updated)
                accountToUpdate = null
            },
            onDelete = { toDel ->
                onDeleteAccount(toDel)
                accountToUpdate = null
            }
        )
    }
}

/**
 * Clean static Quick Action Button component with high-fidelity 3D icons,
 * perfectly optimized with zero lag, zero battery drain, and no moving animations.
 */
@Composable
fun QuickAction3DItem(
    drawableRes: Int,
    label: String,
    glowColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier.size(62.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .shadow(1.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.04f))
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(16.dp)
                    )
            )

            Image(
                painter = painterResource(id = drawableRes),
                contentDescription = label,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(14.dp))
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp
            ),
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1
        )
    }
}

@Composable
fun HomeAccountCard(
    account: Account,
    isHidden: Boolean,
    language: com.example.util.AppLanguage,
    onClick: () -> Unit
) {
    val (icon, brandColor) = when (account.type) {
        AccountType.BKASH.name -> Icons.Default.PhoneAndroid to Color(0xFFE2136E)
        AccountType.NAGAD.name -> Icons.Default.PhoneAndroid to Color(0xFFF7931E)
        AccountType.ROCKET.name -> Icons.Default.PhoneAndroid to Color(0xFF8C3494)
        AccountType.MFS.name -> Icons.Default.PhoneAndroid to Color(0xFF0284C7)
        AccountType.BANK.name -> Icons.Default.AccountBalance to Color(0xFF2563EB)
        AccountType.CASH_FLOW.name, AccountType.CASH.name -> Icons.Default.AttachMoney to Color(0xFF10B981)
        else -> Icons.Default.AccountBalance to MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .width(160.dp)
            .height(130.dp)
            .shadow(2.dp, RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.05f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
            .bounceClick(scaleDown = 0.97f, onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(brandColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = brandColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Clean active status dot
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp, end = 2.dp)
                        .size(8.dp)
                        .background(brandColor, CircleShape)
                )
            }

            Column {
                Text(
                    text = account.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                AnimatedBdtCounter(
                    targetAmount = account.balance,
                    language = language,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    isMasked = isHidden
                )
            }
        }
    }
}

@Composable
fun HomeAddAccountCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .height(130.dp)
            .shadow(1.dp, RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.04f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
            .bounceClick(scaleDown = 0.96f, onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Account",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Add Account",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun HomeTransactionRow(
    txn: Transaction,
    isHidden: Boolean,
    language: com.example.util.AppLanguage,
    modifier: Modifier = Modifier
) {
    val isIncome = txn.type == "INCOME"
    val tintColor = if (isIncome) Color(0xFF10B981) else Color(0xFFEF4444)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp)
            .shadow(1.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.04f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .bounceClick(scaleDown = 0.98f) { },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(tintColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isIncome) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = tintColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = txn.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${txn.category} • ${txn.accountName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (isHidden) "৳ •••" else "${if (isIncome) "+" else "-"} ${formatBdt(txn.amount, language)}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = tintColor
                )
                Text(
                    text = txn.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

