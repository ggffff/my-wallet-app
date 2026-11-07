package com.example.ui.dialogs

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
 * Premium Blue & White "Add Goal" Screen matching "Add New Account" design.
 */
@Composable
fun AddGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (Goal) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        AddGoalScreenContent(
            onDismiss = onDismiss,
            onConfirm = onConfirm
        )
    }
}

@Composable
fun AddGoalScreenContent(
    onDismiss: () -> Unit,
    onConfirm: (Goal) -> Unit
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
    var isDateFocused by remember { mutableStateOf(false) }

    BackHandler {
        if (!isSubmitting) onDismiss()
    }

    val screenBg = if (isDark) Color(0xFF060D1E) else Color(0xFFF6F9FF)
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
            .testTag("add_goal_screen")
    ) {
        // 1. TOP CURVED GRADIENT
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .align(Alignment.TopCenter)
        ) {
            val w = size.width
            val h = size.height

            val topGradient = if (isDark) {
                Brush.verticalGradient(
                    listOf(Color(0xFF0038A8).copy(alpha = 0.35f), Color.Transparent)
                )
            } else {
                Brush.verticalGradient(
                    listOf(Color(0xFF0072FF).copy(alpha = 0.22f), Color(0xFFBAE6FD).copy(alpha = 0.12f), Color.Transparent)
                )
            }
            drawRect(brush = topGradient)

            val wavePath = Path().apply {
                moveTo(0f, 0f)
                lineTo(w, 0f)
                lineTo(w, h * 0.50f)
                cubicTo(w * 0.75f, h * 0.85f, w * 0.25f, h * 0.25f, 0f, h * 0.65f)
                close()
            }
            drawPath(
                path = wavePath,
                brush = Brush.linearGradient(
                    colors = if (isDark) listOf(Color(0xFF0052FE).copy(alpha = 0.25f), Color.Transparent)
                    else listOf(Color(0xFF0066FF).copy(alpha = 0.20f), Color.Transparent)
                )
            )
        }

        // 2. BOTTOM SKYLINE & BASE WAVE
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .align(Alignment.BottomCenter)
        ) {
            val w = size.width
            val h = size.height

            val skylineColor = if (isDark) Color(0xFF1E3A8A).copy(alpha = 0.25f) else Color(0xFF93C5FD).copy(alpha = 0.30f)
            val baseH = h * 0.65f

            val bWidth = w / 14f
            val heights = listOf(0.35f, 0.55f, 0.40f, 0.70f, 0.50f, 0.80f, 0.45f, 0.60f, 0.75f, 0.40f, 0.65f, 0.50f, 0.70f, 0.45f)
            heights.forEachIndexed { i, factor ->
                val x = i * bWidth
                val bH = h * 0.45f * factor
                drawRect(
                    color = skylineColor,
                    topLeft = Offset(x, baseH - bH),
                    size = androidx.compose.ui.geometry.Size(bWidth - 2.dp.toPx(), bH + h * 0.4f)
                )
            }

            val bottomWave = Path().apply {
                moveTo(0f, h)
                lineTo(w, h)
                lineTo(w, h * 0.45f)
                cubicTo(w * 0.70f, h * 0.30f, w * 0.30f, h * 0.60f, 0f, h * 0.40f)
                close()
            }
            drawPath(
                path = bottomWave,
                brush = Brush.verticalGradient(
                    colors = if (isDark) listOf(Color(0xFF0052FE).copy(alpha = 0.35f), Color(0xFF002277).copy(alpha = 0.80f))
                    else listOf(Color(0xFF38BDF8).copy(alpha = 0.40f), Color(0xFF0072FF).copy(alpha = 0.85f))
                )
            )
        }

        // 3. MAIN CONTENT
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    onClick = onDismiss,
                    shape = CircleShape,
                    color = cardBg,
                    border = BorderStroke(1.dp, cardBorder),
                    shadowElevation = if (isDark) 0.dp else 3.dp,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // 3D Target & Coins illustration with '+' badge
                Box(
                    modifier = Modifier
                        .size(115.dp)
                        .padding(end = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_quick_savings_goal),
                        contentDescription = "Goal Illustration",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(105.dp)
                            .clip(RoundedCornerShape(20.dp))
                    )

                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 4.dp,
                        border = BorderStroke(1.5.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .size(32.dp)
                            .align(Alignment.BottomEnd)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Add, "Add", tint = brandBlue, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "New Saving Goal",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp,
                    letterSpacing = 0.2.sp
                ),
                color = textPrimary,
                modifier = Modifier.testTag("add_goal_title")
            )

            Spacer(modifier = Modifier.height(20.dp))

            // CATEGORY / GOAL TYPE SELECTOR CARDS
            Text(
                text = "Goal Category",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = textSecondary,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(iconCategories) { (key, label, icon) ->
                    val isSelected = selectedIcon == key
                    Surface(
                        onClick = { selectedIcon = key },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) {
                            if (isDark) Color(0xFF0F265C) else Color(0xFFEBF4FF)
                        } else cardBg,
                        border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) activeAccent else cardBorder),
                        shadowElevation = if (isSelected) 3.dp else 1.dp,
                        modifier = Modifier
                            .height(90.dp)
                            .width(84.dp)
                            .bounceClick()
                    ) {
                        Box(modifier = Modifier.fillMaxSize().padding(6.dp)) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(activeAccent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(11.dp))
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
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) activeAccent else textPrimary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3 INPUT FIELDS
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Field 1: Goal Title *
                GoalInputField(
                    isFocused = isTitleFocused,
                    icon = {
                        Icon(Icons.Default.Flag, "Title", tint = activeAccent, modifier = Modifier.size(24.dp))
                    },
                    content = {
                        BasicTextField(
                            value = title,
                            onValueChange = {
                                title = it
                                if (it.isNotBlank()) errorMessage = null
                            },
                            textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = textPrimary),
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
                                    Text("Goal Title (e.g. New Laptop, Eid Shopping) *", style = TextStyle(fontSize = 15.sp, color = textSecondary))
                                }
                                innerTextField()
                            }
                        )
                    },
                    trailing = {
                        if (title.isNotEmpty()) {
                            IconButton(onClick = { title = "" }) {
                                Icon(Icons.Default.Clear, "Clear", tint = textSecondary, modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    cardBg = cardBg,
                    cardBorder = cardBorder,
                    activeAccent = activeAccent,
                    badgeBg = badgeBg
                )

                // Field 2: Target Amount (৳ BDT) *
                GoalInputField(
                    isFocused = isAmountFocused,
                    icon = {
                        Icon(painterResource(R.drawable.ic_coins_stack), "Coins Stack", tint = activeAccent, modifier = Modifier.size(26.dp))
                    },
                    content = {
                        BasicTextField(
                            value = targetAmountText,
                            onValueChange = {
                                targetAmountText = it.filter { char -> char.isDigit() || char == '.' }
                                if (it.isNotBlank()) errorMessage = null
                            },
                            textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = textPrimary),
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
                                    Text("Target Amount (৳ BDT) *", style = TextStyle(fontSize = 16.sp, color = textSecondary))
                                }
                                innerTextField()
                            }
                        )
                    },
                    trailing = {
                        if (targetAmountText.isNotEmpty()) {
                            IconButton(onClick = { targetAmountText = "" }) {
                                Icon(Icons.Default.Clear, "Clear", tint = textSecondary, modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    cardBg = cardBg,
                    cardBorder = cardBorder,
                    activeAccent = activeAccent,
                    badgeBg = badgeBg
                )

                // Field 3: Target Date (YYYY-MM-DD)
                GoalInputField(
                    isFocused = isDateFocused,
                    icon = {
                        Icon(Icons.Default.CalendarToday, "Calendar", tint = activeAccent, modifier = Modifier.size(22.dp))
                    },
                    content = {
                        BasicTextField(
                            value = targetDate,
                            onValueChange = { targetDate = it },
                            textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = textPrimary),
                            cursorBrush = SolidColor(activeAccent),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { isDateFocused = it.isFocused },
                            decorationBox = { innerTextField ->
                                if (targetDate.isEmpty()) {
                                    Text("Target Date (Optional: YYYY-MM-DD)", style = TextStyle(fontSize = 15.sp, color = textSecondary))
                                }
                                innerTextField()
                            }
                        )
                    },
                    trailing = {
                        if (targetDate.isNotEmpty()) {
                            IconButton(onClick = { targetDate = "" }) {
                                Icon(Icons.Default.Clear, "Clear", tint = textSecondary, modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    cardBg = cardBg,
                    cardBorder = cardBorder,
                    activeAccent = activeAccent,
                    badgeBg = badgeBg
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
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.padding(top = 10.dp, start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // ACTION BUTTONS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // CANCEL
                Surface(
                    onClick = { if (!isSubmitting) onDismiss() },
                    shape = RoundedCornerShape(28.dp),
                    color = cardBg,
                    border = BorderStroke(2.dp, brandBlue),
                    shadowElevation = if (isDark) 0.dp else 2.dp,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .bounceClick()
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Cancel, "Cancel", tint = activeAccent, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                            color = activeAccent
                        )
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
                    shape = RoundedCornerShape(28.dp),
                    color = Color.Transparent,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
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
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Save, "Save", tint = Color.White, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Save Goal",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalInputField(
    isFocused: Boolean,
    icon: @Composable () -> Unit,
    content: @Composable () -> Unit,
    trailing: @Composable () -> Unit = {},
    cardBg: Color,
    cardBorder: Color,
    activeAccent: Color,
    badgeBg: Color
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = cardBg,
        border = BorderStroke(if (isFocused) 2.dp else 1.2.dp, if (isFocused) activeAccent else cardBorder),
        shadowElevation = if (isFocused) 4.dp else 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(badgeBg),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }

            Spacer(modifier = Modifier.width(14.dp))

            Box(modifier = Modifier.weight(1f)) {
                content()
            }

            trailing()
        }
    }
}
