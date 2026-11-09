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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.Handshake
import com.example.data.model.Account
import com.example.data.model.AccountType
import com.example.data.model.Bill
import com.example.data.model.Debt
import com.example.data.model.HabitRoutine
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
import com.example.ui.dialogs.CountryLanguageDialog
import com.example.ui.dialogs.CurrencyDialog
import com.example.util.AppCurrency
import com.example.util.AppLanguage
import com.example.util.LocalAppCurrency
import com.example.util.LocalAppLanguage
import com.example.util.StringKeys
import com.example.util.appString
import com.example.util.formatBdt
import com.example.util.formatCurrency

@Composable
fun HomeScreen(
    userProfile: UserProfile?,
    accounts: List<Account>,
    transactions: List<Transaction>,
    bills: List<Bill>,
    debts: List<Debt> = emptyList(),
    habits: List<HabitRoutine> = emptyList(),
    onAddAccountClicked: () -> Unit,
    onUpdateAccount: (Account) -> Unit = {},
    onDeleteAccount: (Account) -> Unit = {},
    onAddTransactionClicked: (TransactionType) -> Unit,
    onViewAllStatementsClicked: () -> Unit,
    onViewBillsClicked: () -> Unit,
    onViewGoalsClicked: () -> Unit,
    onViewDebtsClicked: () -> Unit = {},
    onViewHabitsClicked: () -> Unit = {},
    onOpenAiAdvisor: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onToggleLanguage: (AppLanguage) -> Unit = {},
    currentCurrency: AppCurrency = LocalAppCurrency.current,
    onSelectCurrency: (AppCurrency) -> Unit = {}
) {
    val language = LocalAppLanguage.current
    var isBalanceHidden by remember { mutableStateOf(false) }
    var accountToUpdate by remember { mutableStateOf<Account?>(null) }
    var showCurrencyDialog by remember { mutableStateOf(false) }

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
            .background(Color.Transparent)
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

        // 2. Large Net Balance Hero Card (Clean Modern Card strictly matching reference design)
        item {
            val isDark = com.example.ui.theme.LocalAppTheme.current.isDark

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .shadow(
                        elevation = if (isDark) 8.dp else 4.dp,
                        shape = RoundedCornerShape(26.dp),
                        spotColor = if (isDark) Color(0xFF0052FE).copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.08f)
                    ),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = if (isDark) {
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF0052FE),
                                        Color(0xFF0284C7),
                                        Color(0xFF0A2268)
                                    )
                                )
                            } else {
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFFFFFFF),
                                        Color(0xFFF0F9FF),
                                        Color(0xFFE0F2FE)
                                    )
                                )
                            }
                        )
                        .border(
                            width = 1.5.dp,
                            color = if (isDark) Color(0xFF00D2FF).copy(alpha = 0.55f) else Color(0xFFBFDBFE),
                            shape = RoundedCornerShape(26.dp)
                        )
                ) {
                    // Flowing Background Wave Curve
                    androidx.compose.foundation.Canvas(modifier = Modifier.matchParentSize()) {
                        val w = size.width
                        val h = size.height
                        val wavePath = androidx.compose.ui.graphics.Path().apply {
                            moveTo(0f, h * 0.70f)
                            cubicTo(w * 0.35f, h * 0.54f, w * 0.65f, h * 0.88f, w, h * 0.64f)
                            lineTo(w, h)
                            lineTo(0f, h)
                            close()
                        }
                        drawPath(
                            path = wavePath,
                            brush = Brush.linearGradient(
                                colors = if (isDark) listOf(
                                    Color(0xFF0070F3).copy(alpha = 0.30f),
                                    Color(0xFF00E5FF).copy(alpha = 0.15f),
                                    Color.Transparent
                                ) else listOf(
                                    Color(0xFFBAE6FD).copy(alpha = 0.65f),
                                    Color(0xFF7DD3FC).copy(alpha = 0.30f),
                                    Color.Transparent
                                )
                            )
                        )
                    }

                    Column(modifier = Modifier.padding(20.dp)) {
                        // Top Row: Net Balance Title | Eye Toggle ... BDT (৳) ▾ Pill
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // 3D Blue Wallet Icon Badge
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isDark) Color(0xFF0284C7) else Color(0xFF0066FF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalance,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = appString(StringKeys.NET_BALANCE),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    ),
                                    color = if (isDark) Color.White else Color(0xFF080F20)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(14.dp)
                                        .background(if (isDark) Color(0xFF94A3B8).copy(alpha = 0.4f) else Color(0xFFCBD5E1))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { isBalanceHidden = !isBalanceHidden },
                                    modifier = Modifier
                                        .size(28.dp)
                                        .rotate(eyeRotation)
                                ) {
                                    Icon(
                                        imageVector = if (isBalanceHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle Balance",
                                        tint = if (isDark) Color.White else Color(0xFF080F20),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Currency pill dropdown: e.g. ৳ BDT ▾
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isDark) Color.White.copy(alpha = 0.16f) else Color(0xFFE0F2FE))
                                    .border(1.dp, if (isDark) Color.White.copy(alpha = 0.25f) else Color(0xFFBFDBFE), RoundedCornerShape(14.dp))
                                    .clickable { showCurrencyDialog = true }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "${currentCurrency.symbol} ${currentCurrency.code} ▾",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    ),
                                    color = if (isDark) Color.White else Color(0xFF0284C7)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Middle Row: Large Amount + Visa Card & Gold Coins Illustration
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            AnimatedBdtCounter(
                                targetAmount = netBalance,
                                language = language,
                                currency = currentCurrency,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 36.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                color = if (isDark) Color.White else Color(0xFF080F20),
                                isMasked = isBalanceHidden
                            )

                            // Contactless Card + Stack of Gold Coins
                            VisaCardCoinsIllustration()
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Bottom Row: Total Income and Total Spent Summary Capsules
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Total Income Capsule
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(if (isDark) Color(0x33003B15) else Color(0xFFDCFCE7))
                                    .border(1.dp, if (isDark) Color(0x3310B981) else Color(0xFF86EFAC), RoundedCornerShape(18.dp))
                                    .bounceClick(scaleDown = 0.96f) {
                                        onAddTransactionClicked(TransactionType.INCOME)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDownward,
                                            contentDescription = "Income",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = appString(StringKeys.TOTAL_INCOME),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            ),
                                            color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF64748B)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        AnimatedBdtCounter(
                                            targetAmount = totalIncome,
                                            language = language,
                                            currency = currentCurrency,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            ),
                                            color = if (isDark) Color.White else Color(0xFF080F20),
                                            isMasked = isBalanceHidden
                                        )
                                    }
                                }
                            }

                            // Total Spent Capsule
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(if (isDark) Color(0x33450505) else Color(0xFFFEE2E2))
                                    .border(1.dp, if (isDark) Color(0x33EF4444) else Color(0xFFFCA5A5), RoundedCornerShape(18.dp))
                                    .bounceClick(scaleDown = 0.96f) {
                                        onAddTransactionClicked(TransactionType.SPENT)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFEF4444)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowUpward,
                                            contentDescription = "Spent",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = appString(StringKeys.TOTAL_EXPENSE),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            ),
                                            color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF64748B)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        AnimatedBdtCounter(
                                            targetAmount = totalExpense,
                                            language = language,
                                            currency = currentCurrency,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            ),
                                            color = if (isDark) Color.White else Color(0xFF080F20),
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
        // 1. Add Income (Green Wallet)
        // 2. Add Spent (Red Wallet)
        // 3. Saving Goals (Target with Coins - NO PIGGY BANK)
        // 4. Bills & Dues (Calendar and Clock)
        // 5. AI Tips (AI Lightbulb)
        item {
            val isDark = com.example.ui.theme.LocalAppTheme.current.isDark
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Picture 1: Add Income
                QuickAction3DItem(
                    drawableRes = if (isDark) R.drawable.img_quick_add_income else R.drawable.img_quick_income_light,
                    label = appString(StringKeys.ADD_INCOME),
                    glowColor = IncomeGreen,
                    testTag = "quick_action_add_income",
                    onClick = { onAddTransactionClicked(TransactionType.INCOME) }
                )

                // Picture 2: Add Spent
                QuickAction3DItem(
                    drawableRes = if (isDark) R.drawable.img_quick_add_spent else R.drawable.img_quick_spent_light,
                    label = appString(StringKeys.ADD_EXPENSE),
                    glowColor = ExpenseRed,
                    testTag = "quick_action_add_spent",
                    onClick = { onAddTransactionClicked(TransactionType.SPENT) }
                )

                // Picture 3: Saving Goals (Target with coins - NO PIGGY BANK)
                QuickAction3DItem(
                    drawableRes = if (isDark) R.drawable.img_quick_savings_goal else R.drawable.img_quick_goals_light,
                    label = appString(StringKeys.SAVING_GOALS),
                    glowColor = Color(0xFF9333EA),
                    testTag = "quick_action_saving_goals",
                    onClick = onViewGoalsClicked
                )

                // Picture 4: Bills & Dues (Calendar and Clock)
                QuickAction3DItem(
                    drawableRes = if (isDark) R.drawable.img_quick_bills_dues else R.drawable.img_quick_bills_light,
                    label = appString(StringKeys.BILLS_AND_DUES),
                    glowColor = Color(0xFF0284C7),
                    testTag = "quick_action_bills_dues",
                    onClick = onViewBillsClicked
                )

                // Picture 5: AI Tips (AI Lightbulb)
                QuickAction3DItem(
                    drawableRes = if (isDark) R.drawable.img_quick_ai_tips else R.drawable.img_quick_ai_light,
                    label = appString(StringKeys.AI_TIPS),
                    glowColor = Color(0xFFF59E0B),
                    testTag = "quick_action_ai_tips",
                    onClick = onOpenAiAdvisor
                )
            }
        }

        // 4. Upcoming Bill Card (Exact reference design)
        item {
            val isDark = com.example.ui.theme.LocalAppTheme.current.isDark
            val nextBill = upcomingBills.firstOrNull() ?: bills.firstOrNull { !it.isPaid }
            val billTitle = nextBill?.title ?: "Wifi Bill"
            val billAmount = nextBill?.amount ?: 450.0
            val billDueDate = nextBill?.dueDate ?: "2026-11-25"

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .shadow(
                        elevation = if (isDark) 4.dp else 2.dp,
                        shape = RoundedCornerShape(22.dp),
                        spotColor = if (isDark) Color(0xFF0052FE).copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.05f)
                    )
                    .border(
                        width = 1.dp,
                        color = if (isDark) Color(0xFF1E3A8A) else Color(0xFFBFDBFE),
                        shape = RoundedCornerShape(22.dp)
                    )
                    .bounceClick { onViewBillsClicked() },
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF0B1733) else Color(0xFFFFFFFF)
                )
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Subtle background wave accent
                    androidx.compose.foundation.Canvas(modifier = Modifier.matchParentSize()) {
                        val w = size.width
                        val h = size.height
                        val wavePath = androidx.compose.ui.graphics.Path().apply {
                            moveTo(0f, h * 0.75f)
                            cubicTo(w * 0.4f, h * 0.6f, w * 0.7f, h * 0.95f, w, h * 0.7f)
                            lineTo(w, h)
                            lineTo(0f, h)
                            close()
                        }
                        drawPath(
                            path = wavePath,
                            color = if (isDark) Color(0xFF0070F3).copy(alpha = 0.12f) else Color(0xFFBAE6FD).copy(alpha = 0.35f)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Amber Circular Bell Badge
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFFFDE68A),
                                            Color(0xFFF59E0B),
                                            Color(0xFFD97706)
                                        )
                                    )
                                )
                                .border(1.dp, Color(0xFFFEF08A), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Bill Alert",
                                tint = Color(0xFF78350F),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Vertical divider line
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(28.dp)
                                .background(if (isDark) Color(0xFF1E3A8A) else Color(0xFFCBD5E1))
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${appString(StringKeys.UPCOMING_BILL)}: $billTitle",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ),
                                color = if (isDark) Color.White else Color(0xFF080F20)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${appString(StringKeys.AMOUNT)}: ${formatCurrency(billAmount, language)} • ${appString(StringKeys.DUE_DATE)}: $billDueDate",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                            )
                        }

                        // Action pill button: View >
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isDark) {
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFF0070F3), Color(0xFF0284C7))
                                        )
                                    } else {
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFF0066FF), Color(0xFF0284C7))
                                        )
                                    }
                                )
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = appString(StringKeys.VIEW),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                ),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // 4.5. Quick Utilities: Debts & Habits
        item {
            val isDark = com.example.ui.theme.LocalAppTheme.current.isDark
            val activeDebts = debts.filter { !it.isSettled }
            val lentSum = activeDebts.filter { it.type == "LENT" }.sumOf { (it.amount - it.repaidAmount).coerceAtLeast(0.0) }
            val borrowSum = activeDebts.filter { it.type == "BORROWED" }.sumOf { (it.amount - it.repaidAmount).coerceAtLeast(0.0) }

            val nextHabit = habits.firstOrNull()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 1: Debts
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .shadow(
                            elevation = if (isDark) 4.dp else 1.dp,
                            shape = RoundedCornerShape(20.dp),
                            spotColor = if (isDark) Color(0xFF0070F3).copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.04f)
                        )
                        .border(
                            1.dp,
                            if (isDark) Color(0xFF1E3A8A) else Color(0xFFBFDBFE),
                            RoundedCornerShape(20.dp)
                        )
                        .bounceClick { onViewDebtsClicked() },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF0B1733) else Color.White
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF059669), Color(0xFF10B981))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Handshake,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "${appString(StringKeys.DEBTS)} >",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0066FF)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = appString(StringKeys.DEBTS),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isDark) PureWhite else Color(0xFF080F20)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (activeDebts.isNotEmpty()) {
                                "${appString(StringKeys.TO_RECEIVE)} ${formatCurrency(lentSum, language)} • ${appString(StringKeys.TO_PAY)} ${formatCurrency(borrowSum, language)}"
                            } else {
                                appString(StringKeys.NO_DEBTS)
                            },
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                            maxLines = 1
                        )
                    }
                }

                // Card 2: Habits
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .shadow(
                            elevation = if (isDark) 4.dp else 1.dp,
                            shape = RoundedCornerShape(20.dp),
                            spotColor = if (isDark) Color(0xFF0070F3).copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.04f)
                        )
                        .border(
                            1.dp,
                            if (isDark) Color(0xFF1E3A8A) else Color(0xFFBFDBFE),
                            RoundedCornerShape(20.dp)
                        )
                        .bounceClick { onViewHabitsClicked() },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF0B1733) else Color.White
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF0070F3), Color(0xFF38BDF8))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EventRepeat,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "${appString(StringKeys.HABITS)} >",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0066FF)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = appString(StringKeys.HABITS),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isDark) PureWhite else Color(0xFF080F20)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (nextHabit != null) {
                                "${nextHabit.title.take(12)}: ${nextHabit.nextDueDate}"
                            } else {
                                appString(StringKeys.NO_HABITS)
                            },
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // 5. Accounts Section (Exact reference design)
        item {
            val isDark = com.example.ui.theme.LocalAppTheme.current.isDark

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF38BDF8) else Color(0xFF0066FF))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = appString(StringKeys.ACCOUNTS),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = if (isDark) Color.White else Color(0xFF080F20)
                    )
                }
                TextButton(
                    onClick = onAddAccountClicked,
                    modifier = Modifier.bounceClick()
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF0066FF),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = appString(StringKeys.ADD_ACCOUNT),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0066FF)
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
                        Text(appString(StringKeys.SEE_ALL), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
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

    if (showCurrencyDialog) {
        CurrencyDialog(
            currentCurrency = currentCurrency,
            onDismiss = { showCurrencyDialog = false },
            onSelectCurrency = { selected ->
                onSelectCurrency(selected)
                showCurrencyDialog = false
            }
        )
    }
}

/**
 * Tilted Contactless Blue Card and 3D Gold Coin Stack Illustration
 * matching the user's reference design.
 */
@Composable
fun VisaCardCoinsIllustration(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(112.dp)
            .height(72.dp),
        contentAlignment = Alignment.Center
    ) {
        // 1. Tilted Contactless Blue Card
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-4).dp, y = 2.dp)
                .rotate(-10f)
                .width(78.dp)
                .height(48.dp)
                .shadow(6.dp, RoundedCornerShape(8.dp), spotColor = Color(0xFF0038A8).copy(alpha = 0.5f))
                .clip(RoundedCornerShape(8.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0284C7),
                            Color(0xFF0369A1),
                            Color(0xFF1E40AF)
                        )
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                .padding(horizontal = 6.dp, vertical = 5.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Mini chip
                Box(
                    modifier = Modifier
                        .size(width = 12.dp, height = 9.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFFFBBF24))
                )
                // Contactless Wi-Fi symbol
                Text(
                    text = "•)))",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
            }
            // Card magnetic lines / embossed styling
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .width(36.dp)
                    .height(3.dp)
                    .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(2.dp))
            )
        }

        // 2. 3D Stack of Gold Coins in front of the card
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-2).dp, y = 6.dp)
        ) {
            // Coin 3 (Bottom)
            Box(
                modifier = Modifier
                    .offset(x = (-8).dp, y = 8.dp)
                    .size(width = 24.dp, height = 10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color(0xFFD97706))
                    .border(0.5.dp, Color(0xFFF59E0B), RoundedCornerShape(5.dp))
            )
            // Coin 2 (Middle)
            Box(
                modifier = Modifier
                    .offset(x = (-8).dp, y = 3.dp)
                    .size(width = 24.dp, height = 10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color(0xFFF59E0B))
                    .border(0.5.dp, Color(0xFFFCD34D), RoundedCornerShape(5.dp))
            )
            // Coin 1 (Top / Front with Taka symbol ৳)
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .shadow(4.dp, CircleShape, spotColor = Color(0xFF78350F).copy(alpha = 0.4f))
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFDE047),
                                Color(0xFFF59E0B),
                                Color(0xFFD97706)
                            )
                        )
                    )
                    .border(1.dp, Color(0xFFFEF08A), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "৳",
                    color = Color(0xFF78350F),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

/**
 * Clean Quick Action Button component with high-fidelity 3D icons,
 * perfectly matching reference design in Dark and Lite modes.
 */
@Composable
fun QuickAction3DItem(
    drawableRes: Int,
    label: String,
    glowColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    val isDark = com.example.ui.theme.LocalAppTheme.current.isDark

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
            // Ambient soft neon radial glow matching the icon's color
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = if (isDark) listOf(
                                glowColor.copy(alpha = 0.32f),
                                Color.Transparent
                            ) else listOf(
                                glowColor.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // 3D Icon container:
            // - In Dark mode: Pure black background (#000000) seamlessly blending with the home background
            // - In Lite mode: Clean elevated white card (#FFFFFF) with subtle shadow and border
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .then(
                        if (isDark) {
                            Modifier
                                .shadow(
                                    elevation = 3.dp,
                                    shape = RoundedCornerShape(18.dp),
                                    spotColor = Color(0xFF0052FE).copy(alpha = 0.25f)
                                )
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFF0D1B38))
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFF1C3460),
                                    shape = RoundedCornerShape(18.dp)
                                )
                        } else {
                            Modifier
                                .shadow(
                                    elevation = 3.dp,
                                    shape = RoundedCornerShape(18.dp),
                                    spotColor = Color.Black.copy(alpha = 0.08f)
                                )
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color.White)
                                .border(
                                    width = 1.2.dp,
                                    color = Color(0xFFE2E8F0),
                                    shape = RoundedCornerShape(18.dp)
                                )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = drawableRes),
                    contentDescription = label,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(if (isDark) 0.dp else 4.dp)
                        .clip(RoundedCornerShape(if (isDark) 18.dp else 14.dp))
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp
            ),
            color = if (isDark) Color.White else Color(0xFF080F20),
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
    val isDark = com.example.ui.theme.LocalAppTheme.current.isDark

    val (icon, brandColor) = when (account.type) {
        AccountType.BKASH.name -> Icons.Default.PhoneAndroid to Color(0xFFE2136E)
        AccountType.NAGAD.name -> Icons.Default.PhoneAndroid to Color(0xFFF7931E)
        AccountType.ROCKET.name -> Icons.Default.PhoneAndroid to Color(0xFF8C3494)
        AccountType.MFS.name -> Icons.Default.PhoneAndroid to Color(0xFF0284C7)
        AccountType.BANK.name -> Icons.Default.AccountBalance to Color(0xFF2563EB)
        AccountType.CASH_FLOW.name, AccountType.CASH.name -> Icons.Default.AttachMoney to Color(0xFF10B981)
        else -> Icons.Default.AccountBalance to (if (isDark) Color(0xFF38BDF8) else Color(0xFF0066FF))
    }

    Card(
        modifier = Modifier
            .width(165.dp)
            .height(115.dp)
            .shadow(
                elevation = if (isDark) 3.dp else 1.5.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = if (isDark) Color(0xFF0052FE).copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.05f)
            )
            .border(
                width = 1.dp,
                color = if (isDark) Color(0xFF152D5A) else Color(0xFFBFDBFE),
                shape = RoundedCornerShape(20.dp)
            )
            .bounceClick(scaleDown = 0.97f, onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF0A1630) else Color(0xFFF0F7FF)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Subtle wave accent
            androidx.compose.foundation.Canvas(modifier = Modifier.matchParentSize()) {
                val w = size.width
                val h = size.height
                val wavePath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(0f, h * 0.8f)
                    cubicTo(w * 0.35f, h * 0.65f, w * 0.7f, h * 0.95f, w, h * 0.75f)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(
                    path = wavePath,
                    color = if (isDark) Color(0xFF0070F3).copy(alpha = 0.10f) else Color(0xFFBAE6FD).copy(alpha = 0.25f)
                )
            }

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
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDark) brandColor.copy(alpha = 0.20f) else Color(0xFFDBEAFE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isDark) brandColor else Color(0xFF0066FF),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Clean active status dot
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp, end = 2.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF38BDF8) else Color(0xFF0066FF))
                    )
                }

                Column {
                    Text(
                        text = account.name,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = if (isDark) Color.White else Color(0xFF080F20),
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    AnimatedBdtCounter(
                        targetAmount = account.balance,
                        language = language,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0066FF),
                        isMasked = isHidden
                    )
                }
            }
        }
    }
}

@Composable
fun HomeAddAccountCard(onClick: () -> Unit) {
    val isDark = com.example.ui.theme.LocalAppTheme.current.isDark

    Card(
        modifier = Modifier
            .width(140.dp)
            .height(115.dp)
            .shadow(
                elevation = if (isDark) 2.dp else 1.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = if (isDark) Color(0xFF0052FE).copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.04f)
            )
            .border(
                width = 1.dp,
                color = if (isDark) Color(0xFF152D5A) else Color(0xFFBFDBFE),
                shape = RoundedCornerShape(20.dp)
            )
            .bounceClick(scaleDown = 0.96f, onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF0A1630) else Color(0xFFF0F7FF)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF0070F3).copy(alpha = 0.25f) else Color(0xFFDBEAFE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Account",
                        tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF0066FF),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Add Account",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    ),
                    color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0066FF)
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
            val appCurrency = LocalAppCurrency.current
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (isHidden) "${appCurrency.symbol} •••" else "${if (isIncome) "+" else "-"} ${appCurrency.format(txn.amount)}",
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

