package com.example.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.model.Goal
import com.example.ui.motion.bounceClick
import com.example.ui.theme.LocalAppTheme

/**
 * Premium Blue & White "Add Goal" Floating Card Dialog.
 * Compact floating card modal ("শর্ট স্কিন") for Saving Goals.
 */
@Composable
fun AddGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (Goal) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        val activeTheme = LocalAppTheme.current
        val isDark = activeTheme.isDark
        val focusManager = LocalFocusManager.current
        val scrollState = rememberScrollState()

        var title by remember { mutableStateOf("") }
        var targetAmountText by remember { mutableStateOf("") }
        var targetDate by remember { mutableStateOf("") }
        var selectedIcon by remember { mutableStateOf("laptop") }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var isSubmitting by remember { mutableStateOf(false) }

        var isTitleFocused by remember { mutableStateOf(false) }
        var isAmountFocused by remember { mutableStateOf(false) }
        var showDatePicker by remember { mutableStateOf(false) }

        val cardBg = if (isDark) Color(0xFF0B1736) else Color.White
        val cardBorder = if (isDark) Color(0xFF1E3A8A) else Color(0xFFE2E8F0)
        val textPrimary = if (isDark) Color.White else Color(0xFF080F20)
        val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
        val brandBlue = Color(0xFF0066FF)
        val cyanAccent = Color(0xFF38BDF8)
        val activeAccent = if (isDark) cyanAccent else brandBlue
        val badgeBg = if (isDark) Color(0xFF132854) else Color(0xFFEBF4FF)

        val iconCategories = listOf(
            Triple("laptop", "Laptop", Icons.Default.Laptop),
            Triple("car", "Car", Icons.Default.DirectionsCar),
            Triple("home", "Home", Icons.Default.Home),
            Triple("savings", "Savings", Icons.Default.Savings),
            Triple("phone", "Phone", Icons.Default.Smartphone),
            Triple("flight", "Tour", Icons.Default.Flight),
            Triple("bag", "Shopping", Icons.Default.ShoppingBag)
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .heightIn(max = 640.dp)
                .shadow(
                    elevation = if (isDark) 12.dp else 8.dp,
                    shape = RoundedCornerShape(28.dp),
                    spotColor = if (isDark) Color(0xFF0052FE).copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.12f)
                )
                .testTag("add_goal_dialog"),
            shape = RoundedCornerShape(28.dp),
            color = cardBg,
            border = BorderStroke(1.2.dp, cardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(20.dp)
            ) {
                // TOP HEADER: Title + 3D Goal Icon + Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "New Saving Goal",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                letterSpacing = 0.2.sp
                            ),
                            color = textPrimary,
                            modifier = Modifier.testTag("add_goal_title")
                        )
                    }

                    // 3D Target & Coins Logo
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .padding(end = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_quick_savings_goal),
                            contentDescription = "Goal Logo",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(14.dp))
                        )
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            shadowElevation = 3.dp,
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .size(20.dp)
                                .align(Alignment.BottomEnd)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Add, null, tint = brandBlue, modifier = Modifier.size(13.dp))
                            }
                        }
                    }

                    // Close Button
                    Surface(
                        onClick = onDismiss,
                        shape = CircleShape,
                        color = badgeBg,
                        modifier = Modifier
                            .size(36.dp)
                            .bounceClick()
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Close, "Close", tint = textSecondary, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // CATEGORIES (Horizontal Scroll)
                Text(
                    text = "Goal Category",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = textSecondary,
                    modifier = Modifier.padding(start = 2.dp, bottom = 6.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(iconCategories) { (key, label, icon) ->
                        val isSelected = selectedIcon == key
                        Surface(
                            onClick = { selectedIcon = key },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) (if (isDark) Color(0xFF0F265C) else Color(0xFFEBF4FF)) else cardBg,
                            border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) activeAccent else cardBorder),
                            shadowElevation = if (isSelected) 2.dp else 1.dp,
                            modifier = Modifier
                                .height(76.dp)
                                .width(74.dp)
                                .bounceClick()
                        ) {
                            Box(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(activeAccent),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(10.dp))
                                    }
                                }
                                Column(
                                    modifier = Modifier.align(Alignment.Center),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        tint = if (isSelected) activeAccent else textPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) activeAccent else textPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // INPUT FIELDS
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Field 1: Goal Title *
                    CompactInputField(
                        isFocused = isTitleFocused,
                        icon = {
                            Icon(Icons.Default.Flag, null, tint = activeAccent, modifier = Modifier.size(22.dp))
                        },
                        content = {
                            BasicTextField(
                                value = title,
                                onValueChange = {
                                    title = it
                                    if (it.isNotBlank()) errorMessage = null
                                },
                                textStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = textPrimary),
                                cursorBrush = SolidColor(activeAccent),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onFocusChanged { isTitleFocused = it.isFocused }
                                    .testTag("goal_title_input"),
                                decorationBox = { innerTextField ->
                                    if (title.isEmpty()) {
                                        Text("Goal Title (e.g. New Laptop) *", style = TextStyle(fontSize = 14.sp, color = textSecondary))
                                    }
                                    innerTextField()
                                }
                            )
                        },
                        trailing = {
                            if (title.isNotEmpty()) {
                                IconButton(onClick = { title = "" }) {
                                    Icon(Icons.Default.Clear, "Clear", tint = textSecondary, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        cardBg = cardBg,
                        cardBorder = cardBorder,
                        activeAccent = activeAccent,
                        badgeBg = badgeBg
                    )

                    // Field 2: Target Amount (৳ BDT) *
                    CompactInputField(
                        isFocused = isAmountFocused,
                        icon = {
                            Icon(painterResource(R.drawable.ic_coins_stack), null, tint = activeAccent, modifier = Modifier.size(24.dp))
                        },
                        content = {
                            BasicTextField(
                                value = targetAmountText,
                                onValueChange = {
                                    targetAmountText = it.filter { char -> char.isDigit() || char == '.' }
                                    if (it.isNotBlank()) errorMessage = null
                                },
                                textStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = textPrimary),
                                cursorBrush = SolidColor(activeAccent),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onFocusChanged { isAmountFocused = it.isFocused }
                                    .testTag("goal_target_input"),
                                decorationBox = { innerTextField ->
                                    if (targetAmountText.isEmpty()) {
                                        Text("Target Amount (৳ BDT) *", style = TextStyle(fontSize = 15.sp, color = textSecondary))
                                    }
                                    innerTextField()
                                }
                            )
                        },
                        trailing = {
                            if (targetAmountText.isNotEmpty()) {
                                IconButton(onClick = { targetAmountText = "" }) {
                                    Icon(Icons.Default.Clear, "Clear", tint = textSecondary, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        cardBg = cardBg,
                        cardBorder = cardBorder,
                        activeAccent = activeAccent,
                        badgeBg = badgeBg
                    )

                    // Field 3: Target Date (Optional - Calendar Picker Only)
                    CompactInputField(
                        isFocused = showDatePicker,
                        icon = {
                            Icon(Icons.Default.CalendarToday, null, tint = activeAccent, modifier = Modifier.size(20.dp))
                        },
                        content = {
                            Text(
                                text = targetDate.ifBlank { "Target Date (Optional - Tap Calendar)" },
                                style = TextStyle(
                                    fontSize = if (targetDate.isNotBlank()) 15.sp else 14.sp,
                                    fontWeight = if (targetDate.isNotBlank()) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (targetDate.isNotBlank()) textPrimary else textSecondary
                                )
                            )
                        },
                        trailing = {
                            if (targetDate.isNotEmpty()) {
                                IconButton(onClick = { targetDate = "" }) {
                                    Icon(Icons.Default.Clear, "Clear date", tint = textSecondary, modifier = Modifier.size(16.dp))
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Open Calendar",
                                    tint = activeAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        cardBg = cardBg,
                        cardBorder = cardBorder,
                        activeAccent = activeAccent,
                        badgeBg = badgeBg,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                                focusManager.clearFocus()
                                showDatePicker = true
                            }
                    )
                }

                AnimatedVisibility(
                    visible = errorMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(top = 8.dp, start = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ACTION BUTTONS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // CANCEL
                    Surface(
                        onClick = { if (!isSubmitting) onDismiss() },
                        shape = RoundedCornerShape(24.dp),
                        color = cardBg,
                        border = BorderStroke(1.5.dp, brandBlue),
                        shadowElevation = if (isDark) 0.dp else 1.dp,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .bounceClick()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Cancel, null, tint = activeAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cancel", style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp, color = activeAccent))
                        }
                    }

                    // SAVE GOAL
                    Surface(
                        onClick = {
                            if (isSubmitting) return@Surface

                            if (title.trim().isBlank()) {
                                errorMessage = "Please enter a goal title."
                                return@Surface
                            }

                            val targetAmount = targetAmountText.toDoubleOrNull()
                            if (targetAmount == null || targetAmount <= 0.0) {
                                errorMessage = "Please enter a valid target amount."
                                return@Surface
                            }

                            isSubmitting = true
                            val goal = Goal(
                                title = title.trim(),
                                targetAmount = targetAmount,
                                savedAmount = 0.0,
                                targetDate = targetDate.trim(),
                                icon = selectedIcon
                            )
                            onConfirm(goal)
                        },
                        enabled = !isSubmitting,
                        shape = RoundedCornerShape(24.dp),
                        color = Color.Transparent,
                        shadowElevation = 3.dp,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .bounceClick()
                            .testTag("save_goal_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.horizontalGradient(listOf(Color(0xFF0072FF), Color(0xFF0052FE)))),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Save, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Save Goal", style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White))
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showDatePicker) {
            WalletCalendarDatePickerDialog(
                selectedDateStr = targetDate,
                onDateSelected = { pickedDate -> targetDate = pickedDate },
                onDismiss = { showDatePicker = false }
            )
        }
    }
}

@Composable
private fun CompactInputField(
    isFocused: Boolean,
    icon: @Composable () -> Unit,
    content: @Composable () -> Unit,
    trailing: @Composable () -> Unit = {},
    cardBg: Color,
    cardBorder: Color,
    activeAccent: Color,
    badgeBg: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = cardBg,
        border = BorderStroke(if (isFocused) 2.dp else 1.dp, if (isFocused) activeAccent else cardBorder),
        shadowElevation = if (isFocused) 3.dp else 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(badgeBg),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(modifier = Modifier.weight(1f)) {
                content()
            }

            trailing()
        }
    }
}
