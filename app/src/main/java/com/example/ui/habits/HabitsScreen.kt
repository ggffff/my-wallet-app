package com.example.ui.habits

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.PedalBike
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Account
import com.example.data.model.HabitRoutine
import com.example.ui.dialogs.CalendarDateSelectorCard
import com.example.ui.dialogs.WalletCalendarDatePickerDialog
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
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HabitsScreen(
    habits: List<HabitRoutine>,
    accounts: List<Account>,
    onAddHabit: (HabitRoutine) -> Unit,
    onMarkCompleted: (HabitRoutine, String?, String?) -> Unit,
    onDeleteHabit: (HabitRoutine) -> Unit
) {
    val isDark = LocalAppTheme.current.isDark
    val language = LocalAppLanguage.current

    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, UPCOMING, THIS_MONTH
    var showAddDialog by remember { mutableStateOf(false) }
    var habitToComplete by remember { mutableStateOf<HabitRoutine?>(null) }
    var habitToDelete by remember { mutableStateOf<HabitRoutine?>(null) }

    val todaySdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val todayDate = remember { Date() }

    val filteredHabits = habits.filter { habit ->
        when (selectedFilter) {
            "UPCOMING" -> {
                val days = calculateDaysRemaining(habit.nextDueDate, todaySdf, todayDate)
                days in 0..7
            }
            "THIS_MONTH" -> {
                val days = calculateDaysRemaining(habit.nextDueDate, todaySdf, todayDate)
                days in 0..31
            }
            else -> true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) Color(0xFF080F20) else Color(0xFFF4F7FF))
            .testTag("habits_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = appString(StringKeys.HABITS),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isDark) PureWhite else Color(0xFF080F20)
                        )
                        Text(
                            text = appString(StringKeys.HABITS_SUBTITLE),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                        )
                    }
                }
            }

            // Highlights Card (Haircut example & active routines count)
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
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
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = appString(StringKeys.HABITS_TITLE),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isDark) PureWhite else Color(0xFF080F20)
                                    )
                                    Text(
                                        text = appString(StringKeys.HABITS_DESC),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isDark) Color(0xFF1E293B) else Color(0xFFEFF6FF))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${habits.size} ${appString(StringKeys.ACTIVE_RECORDS)}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0066FF)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // How it works tip badge
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9))
                                .border(
                                    1.dp,
                                    if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0),
                                    RoundedCornerShape(14.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "💡",
                                    fontSize = 18.sp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "যেমন: ৫ তারিখে চুল কাটলে ১ মাস পর স্বয়ংক্রিয়ভাবে পরের ৫ তারিখের রিমাইন্ডার তৈরি হবে!",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155)
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
                        "ALL" to "সবগুলো (${habits.size})",
                        "THIS_MONTH" to "এই মাসে করণীয়",
                        "UPCOMING" to "শীঘ্রই (৭ দিন)"
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

            // Habits List
            if (filteredHabits.isEmpty()) {
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
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF0066FF),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = appString(StringKeys.NO_HABITS),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isDark) PureWhite else Color(0xFF080F20)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = appString(StringKeys.NO_HABITS_HINT),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                            )
                        }
                    }
                }
            } else {
                items(filteredHabits, key = { it.id }) { habit ->
                    HabitCardItem(
                        habit = habit,
                        isDark = isDark,
                        todaySdf = todaySdf,
                        todayDate = todayDate,
                        onMarkDone = { habitToComplete = habit },
                        onDelete = { habitToDelete = habit }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(96.dp))
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .bounceClick()
                .testTag("add_habit_fab"),
            containerColor = if (isDark) Color(0xFF0070F3) else Color(0xFF0066FF),
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Habit Routine", modifier = Modifier.size(28.dp))
        }
    }

    // Add Habit Dialog
    if (showAddDialog) {
        AddHabitDialog(
            accounts = accounts,
            onDismiss = { showAddDialog = false },
            onSave = { newHabit ->
                onAddHabit(newHabit)
                showAddDialog = false
            }
        )
    }

    // Mark Done Confirmation Dialog
    if (habitToComplete != null) {
        MarkHabitDoneDialog(
            habit = habitToComplete!!,
            accounts = accounts,
            onDismiss = { habitToComplete = null },
            onConfirm = { completionDate, costAccId ->
                onMarkCompleted(habitToComplete!!, completionDate, costAccId)
                habitToComplete = null
            }
        )
    }

    // Delete Confirmation Dialog
    if (habitToDelete != null) {
        AlertDialog(
            onDismissRequest = { habitToDelete = null },
            title = { Text("রুটিন মুছে ফেলবেন?") },
            text = { Text("\"${habitToDelete!!.title}\" রুটিনটি মুছে ফেলতে চান?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteHabit(habitToDelete!!)
                        habitToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text("মুছে ফেলুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { habitToDelete = null }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

@Composable
private fun HabitCardItem(
    habit: HabitRoutine,
    isDark: Boolean,
    todaySdf: SimpleDateFormat,
    todayDate: Date,
    onMarkDone: () -> Unit,
    onDelete: () -> Unit
) {
    val language = LocalAppLanguage.current
    val daysRemaining = calculateDaysRemaining(habit.nextDueDate, todaySdf, todayDate)

    val (badgeText, badgeColor) = when {
        daysRemaining < 0 -> "মেয়াদ শেষ (${-daysRemaining} দিন পার)" to ExpenseRed
        daysRemaining == 0 -> "আজকেই সময়!" to IncomeGreen
        daysRemaining <= 7 -> "${daysRemaining} দিন বাকি" to Color(0xFFF59E0B)
        else -> "${daysRemaining} দিন বাকি" to (if (isDark) Color(0xFF38BDF8) else Color(0xFF0066FF))
    }

    val iconVector = getCategoryIcon(habit.category, habit.title)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isDark) Color(0xFF1E3A8A) else Color(0xFFBFDBFE),
                RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF0B1733) else Color.White
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Category Icon, Title, Days Remaining Badge, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        Color(0xFF38BDF8),
                                        Color(0xFF0066FF)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = habit.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isDark) PureWhite else Color(0xFF080F20)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "প্রতি ${habit.intervalLabel}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0066FF)
                            )
                            if (habit.estimatedCost > 0) {
                                Text(
                                    text = " • আনুমানিক ৳${habit.estimatedCost.toInt()}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }

                // Days remaining badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(badgeColor.copy(alpha = if (isDark) 0.2f else 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = badgeColor
                    )
                }

                IconButton(
                    onClick = onDelete,
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

            // Dates Box (Previous Done Date vs Next Due Date)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC))
                    .border(
                        1.dp,
                        if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0),
                        RoundedCornerShape(14.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Last Completed
                Column {
                    Text(
                        text = "আগের বার করেছিলাম",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                    )
                    Text(
                        text = if (habit.lastDoneDate.isNotBlank()) habit.lastDoneDate else "এখনো হয়নি",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (isDark) PureWhite else Color(0xFF080F20)
                    )
                }

                // Arrow
                Icon(
                    imageVector = Icons.Default.Repeat,
                    contentDescription = null,
                    tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF0066FF),
                    modifier = Modifier.size(18.dp)
                )

                // Next Due
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "পরের বার করতে হবে",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                    )
                    Text(
                        text = habit.nextDueDate,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (daysRemaining <= 0) ExpenseRed else (if (isDark) PureWhite else Color(0xFF080F20))
                        )
                    )
                }
            }

            if (habit.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "📝 ${habit.note}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action: "সম্পন্ন করেছি (Mark Done)" button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF059669), Color(0xFF10B981))
                        )
                    )
                    .clickable { onMarkDone() }
                    .padding(vertical = 11.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "সম্পন্ন করেছি (Mark Done)",
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
private fun AddHabitDialog(
    accounts: List<Account>,
    onDismiss: () -> Unit,
    onSave: (HabitRoutine) -> Unit
) {
    val isDark = LocalAppTheme.current.isDark
    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }

    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Routine") }
    var lastDoneDate by remember { mutableStateOf(todayStr) }
    var showLastDoneDatePicker by remember { mutableStateOf(false) }
    var intervalDays by remember { mutableStateOf(30) }
    var intervalLabel by remember { mutableStateOf("1 Month (30 days)") }
    var estimatedCostText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    // Preset chips
    val presets = listOf(
        "রুটিন / Routine 🔄" to ("Routine" to 30),
        "রুম ভাড়া / Rent 🏠" to ("Living" to 30),
        "সার্ভিসিং / Service 🏍️" to ("Vehicle" to 60),
        "জিম / Fitness 🏋️" to ("Health" to 30),
        "বিল / Bills 📶" to ("Utilities" to 30)
    )

    // Preset intervals
    val intervalOptions = listOf(
        "15 Days" to 15,
        "1 Month (30d)" to 30,
        "2 Months (60d)" to 60,
        "3 Months (90d)" to 90,
        "6 Months (180d)" to 180,
        "1 Year (365d)" to 365
    )

    val nextCalculatedDate = remember(lastDoneDate, intervalDays) {
        calculateNextDateString(lastDoneDate, intervalDays)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = appString(StringKeys.ADD_HABIT),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Quick Presets
                Text(
                    text = "Quick Presets:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.take(3).forEach { (presetName, pair) ->
                        FilterChip(
                            selected = title.startsWith(presetName.take(4)),
                            onClick = {
                                title = presetName
                                category = pair.first
                                intervalDays = pair.second
                                intervalLabel = if (pair.second == 30) "1 Month (30d)" else "${pair.second} Days"
                            },
                            label = { Text(presetName, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("${appString(StringKeys.ROUTINE_NAME)} *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Interval selection
                Text(
                    text = "কতদিন পর পর করবেন? (Interval):",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    intervalOptions.take(3).forEach { (optLabel, days) ->
                        FilterChip(
                            selected = intervalDays == days,
                            onClick = {
                                intervalDays = days
                                intervalLabel = optLabel
                            },
                            label = { Text(optLabel, fontSize = 11.sp) }
                        )
                    }
                }

                CalendarDateSelectorCard(
                    label = "আগের বার করার তারিখ",
                    selectedDate = lastDoneDate,
                    onClick = { showLastDoneDatePicker = true }
                )

                // Calculated Next Due Date preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(IncomeGreen.copy(alpha = 0.12f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "পরবর্তী তারিখ হবে:",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = IncomeGreen
                        )
                        Text(
                            text = nextCalculatedDate,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = IncomeGreen
                        )
                    }
                }

                OutlinedTextField(
                    value = estimatedCostText,
                    onValueChange = { estimatedCostText = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("আনুমানিক খরচ (৳ BDT - ঐচ্ছিক)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("নোট বা বিবরণ (ঐচ্ছিক)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val newHabit = HabitRoutine(
                            title = title.trim(),
                            category = category.trim(),
                            lastDoneDate = lastDoneDate.trim(),
                            nextDueDate = nextCalculatedDate,
                            intervalDays = intervalDays,
                            intervalLabel = intervalLabel,
                            estimatedCost = estimatedCostText.toDoubleOrNull() ?: 0.0,
                            note = note.trim(),
                            totalTimesCompleted = 1
                        )
                        onSave(newHabit)
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0070F3))
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

    if (showLastDoneDatePicker) {
        WalletCalendarDatePickerDialog(
            selectedDateStr = lastDoneDate,
            onDateSelected = { pickedDate -> lastDoneDate = pickedDate },
            onDismiss = { showLastDoneDatePicker = false }
        )
    }
}

@Composable
private fun MarkHabitDoneDialog(
    habit: HabitRoutine,
    accounts: List<Account>,
    onDismiss: () -> Unit,
    onConfirm: (String?, String?) -> Unit
) {
    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    var completionDate by remember { mutableStateOf(todayStr) }
    var showCompletionDatePicker by remember { mutableStateOf(false) }
    var deductFromAccount by remember { mutableStateOf(accounts.isNotEmpty() && habit.estimatedCost > 0) }
    var selectedAccountId by remember { mutableStateOf<String?>(accounts.firstOrNull()?.id) }

    val nextDate = remember(completionDate) {
        calculateNextDateString(completionDate, habit.intervalDays)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "সম্পন্ন নিশ্চিত করুন",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "আপনি কি \"${habit.title}\" সম্পন্ন করেছেন?",
                    style = MaterialTheme.typography.bodyMedium
                )

                CalendarDateSelectorCard(
                    label = "সম্পন্নের তারিখ",
                    selectedDate = completionDate,
                    onClick = { showCompletionDatePicker = true }
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(IncomeGreen.copy(alpha = 0.12f))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "পরের বার স্বয়ংক্রিয়ভাবে নির্ধারণ হবে: $nextDate",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = IncomeGreen
                        )
                    )
                }

                if (habit.estimatedCost > 0 && accounts.isNotEmpty()) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { deductFromAccount = !deductFromAccount }
                        ) {
                            androidx.compose.material3.Checkbox(
                                checked = deductFromAccount,
                                onCheckedChange = { deductFromAccount = it }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ওয়ালেট থেকে ৳${habit.estimatedCost.toInt()} কর্তন করুন",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        if (deductFromAccount) {
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        completionDate.ifBlank { todayStr },
                        if (deductFromAccount) selectedAccountId else null
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen)
            ) {
                Text("নিশ্চিত করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )

    if (showCompletionDatePicker) {
        WalletCalendarDatePickerDialog(
            selectedDateStr = completionDate,
            onDateSelected = { pickedDate -> completionDate = pickedDate },
            onDismiss = { showCompletionDatePicker = false }
        )
    }
}

private fun getCategoryIcon(category: String, title: String): ImageVector {
    val lower = "$category $title".lowercase()
    return when {
        lower.contains("chul") || lower.contains("hair") || lower.contains("cut") -> Icons.Default.ContentCut
        lower.contains("bike") || lower.contains("motor") || lower.contains("car") || lower.contains("vehicle") -> Icons.Default.PedalBike
        lower.contains("room") || lower.contains("rent") || lower.contains("house") -> Icons.Default.Home
        lower.contains("gym") || lower.contains("fit") || lower.contains("workout") -> Icons.Default.FitnessCenter
        lower.contains("medicine") || lower.contains("med") || lower.contains("health") -> Icons.Default.Medication
        lower.contains("wifi") || lower.contains("internet") || lower.contains("bill") -> Icons.Default.Wifi
        else -> Icons.Default.EventRepeat
    }
}

private fun calculateDaysRemaining(nextDueDateStr: String, sdf: SimpleDateFormat, today: Date): Int {
    return try {
        val target = sdf.parse(nextDueDateStr) ?: return 0
        val diffMillis = target.time - today.time
        (diffMillis / (1000 * 60 * 60 * 24)).toInt()
    } catch (_: Exception) {
        0
    }
}

private fun calculateNextDateString(fromDateStr: String, intervalDays: Int): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()
        if (fromDateStr.isNotBlank()) {
            val parsed = sdf.parse(fromDateStr)
            if (parsed != null) cal.time = parsed
        }
        cal.add(Calendar.DAY_OF_YEAR, intervalDays.coerceAtLeast(1))
        sdf.format(cal.time)
    } catch (_: Exception) {
        fromDateStr
    }
}
