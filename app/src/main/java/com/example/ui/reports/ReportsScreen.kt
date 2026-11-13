package com.example.ui.reports

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Transaction
import com.example.ui.motion.AnimatedBdtCounter
import com.example.ui.motion.AnimatedGoalProgressBar
import com.example.ui.motion.PulsingAuraGlow
import com.example.ui.motion.bounceClick
import com.example.ui.motion.neonGlow
import com.example.ui.motion.staggeredEntrance
import com.example.ui.motion.tilt3D
import androidx.compose.foundation.border
import androidx.compose.ui.draw.shadow
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.LocalAppTheme
import com.example.ui.theme.PremiumBlue
import com.example.ui.theme.PremiumPink
import com.example.ui.theme.PureWhite
import com.example.util.LocalAppLanguage
import com.example.util.formatBdt
import kotlin.math.max

@Composable
fun ReportsScreen(
    transactions: List<Transaction>,
    onOpenAiAdvisor: () -> Unit
) {
    val language = LocalAppLanguage.current

    val totalIncome = remember(transactions) {
        transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    }

    val totalExpense = remember(transactions) {
        transactions.filter { it.type == "SPENT" }.sumOf { it.amount }
    }

    val netSavings = remember(totalIncome, totalExpense) {
        totalIncome - totalExpense
    }

    val savingsRate = remember(totalIncome, totalExpense) {
        if (totalIncome > 0) {
            (((totalIncome - totalExpense) / totalIncome) * 100).toInt().coerceIn(-100, 100)
        } else 0
    }

    val categoryBreakdown = remember(transactions) {
        val expenseTxns = transactions.filter { it.type == "SPENT" }
        val sumTotal = expenseTxns.sumOf { it.amount }.coerceAtLeast(1.0)
        expenseTxns.groupBy { it.category }
            .map { (cat, list) ->
                val catSum = list.sumOf { it.amount }
                CategoryStat(
                    category = cat,
                    amount = catSum,
                    percentage = ((catSum / sumTotal) * 100).toInt(),
                    count = list.size
                )
            }
            .sortedByDescending { it.amount }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("reports_screen"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Header
        item {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .staggeredEntrance(index = 0)
            ) {
                Text(
                    text = "Financial Reports",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Cash flow analytics, category distribution & savings rate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Summary Analytics Card with Ambient Glow & 3D Tilt
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .staggeredEntrance(index = 1)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(22.dp), spotColor = Color.Black.copy(alpha = 0.05f))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), RoundedCornerShape(22.dp)),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Net Cash Flow",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            AnimatedBdtCounter(
                                targetAmount = netSavings,
                                language = language,
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = if (netSavings >= 0) IncomeGreen else ExpenseRed
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (savingsRate >= 20) IncomeGreen.copy(alpha = 0.2f)
                                        else PremiumPink.copy(alpha = 0.2f)
                                    )
                                    .neonGlow(
                                        color = if (savingsRate >= 20) IncomeGreen else PremiumPink,
                                        shape = RoundedCornerShape(10.dp),
                                        strokeWidth = 1.dp
                                    )
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "Savings Rate: $savingsRate%",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (savingsRate >= 20) IncomeGreen else PremiumPink
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Income Stat
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.ArrowDownward,
                                            contentDescription = null,
                                            tint = IncomeGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Income", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = formatBdt(totalIncome, language),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = IncomeGreen
                                    )
                                }
                            }

                            // Expense Stat
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.ArrowUpward,
                                            contentDescription = null,
                                            tint = ExpenseRed,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Expense", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = formatBdt(totalExpense, language),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = ExpenseRed
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Native Compose Canvas Comparison Chart
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .staggeredEntrance(index = 2),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Income vs Expense Comparison",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = PremiumBlue
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    NativeCashFlowBarChart(
                        income = totalIncome.toFloat(),
                        expense = totalExpense.toFloat()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(PremiumBlue, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Income (BDT)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(20.dp))
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(PremiumPink, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Expense (BDT)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // AI Financial Advisor Banner with bounce animation
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .staggeredEntrance(index = 3)
                    .bounceClick(scaleDown = 0.97f, onClick = onOpenAiAdvisor),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                val isDark = LocalAppTheme.current.isDark
                val aiGradient = if (isDark) {
                    listOf(Color(0xFF1E3A8A), Color(0xFF312E81))
                } else {
                    listOf(Color(0xFF1D4ED8), Color(0xFF2563EB))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.08f))
                        .background(Brush.linearGradient(aiGradient), RoundedCornerShape(20.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(20.dp))
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = PureWhite, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "AI Financial Advisor",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = PureWhite
                                )
                            }
                            Text(
                                text = "Ask Gemini 3.8 Flash for personalized savings tips based on your spending.",
                                style = MaterialTheme.typography.bodySmall,
                                color = PureWhite.copy(alpha = 0.85f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        Button(
                            onClick = onOpenAiAdvisor,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PureWhite,
                                contentColor = PremiumBlue
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Chat", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Spending by Category
        item {
            Text(
                text = "Expense Distribution by Category",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 10.dp)
                    .staggeredEntrance(index = 4)
            )
        }

        if (categoryBreakdown.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .staggeredEntrance(index = 5),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = "No expenses recorded yet to show category distribution.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }
        } else {
            itemsIndexed(categoryBreakdown, key = { _, item -> item.category }) { index, item ->
                CategoryBreakdownRow(
                    stat = item,
                    language = language,
                    modifier = Modifier.staggeredEntrance(index = 5 + index)
                )
            }
        }
    }
}

data class CategoryStat(
    val category: String,
    val amount: Double,
    val percentage: Int,
    val count: Int
)

@Composable
fun CategoryBreakdownRow(
    stat: CategoryStat,
    language: com.example.util.AppLanguage,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${stat.category} (${stat.count})",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = formatBdt(stat.amount, language),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = ExpenseRed
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Animated progress bar with traveling shimmer highlight
            AnimatedGoalProgressBar(
                progressPercent = (stat.percentage / 100f).coerceIn(0f, 1f),
                isCompleted = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${stat.percentage}% of total expenses",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Native Jetpack Compose Canvas Bar Chart with animated entrance and glowing top caps.
 */
@Composable
fun NativeCashFlowBarChart(
    income: Float,
    expense: Float,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(160.dp)
) {
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(income, expense) {
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
        )
    }

    val maxVal = max(income, expense).coerceAtLeast(1f)

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val barWidth = width * 0.25f

        // Gridlines
        val lineCount = 3
        for (i in 0..lineCount) {
            val y = height * (i.toFloat() / lineCount)
            drawLine(
                color = Color.Gray.copy(alpha = 0.15f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Income Bar (Premium Blue)
        val incomeHeight = (height * (income / maxVal) * 0.85f) * animProgress.value
        val incomeX = width * 0.2f
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(PremiumBlue, Color(0xFF60A5FA))
            ),
            topLeft = Offset(incomeX, height - incomeHeight),
            size = Size(barWidth, incomeHeight),
            cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
        )

        // Expense Bar (Premium Pink)
        val expenseHeight = (height * (expense / maxVal) * 0.85f) * animProgress.value
        val expenseX = width * 0.55f
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(PremiumPink, Color(0xFFF472B6))
            ),
            topLeft = Offset(expenseX, height - expenseHeight),
            size = Size(barWidth, expenseHeight),
            cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
        )
    }
}
