package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalAppTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Timezone-safe date formatting/parsing utilities for Material 3 DatePicker ("yyyy-MM-dd").
 * Material 3 DatePicker operates in UTC midnight milliseconds; parsing/formatting in UTC
 * prevents off-by-one-day shifts across timezones.
 */
object WalletDateUtils {
    private const val DATE_PATTERN = "yyyy-MM-dd"

    fun todayLocalDateStr(): String {
        val localFormat = SimpleDateFormat(DATE_PATTERN, Locale.US)
        return localFormat.format(Date())
    }

    fun parseDateToUtcMillis(dateStr: String): Long? {
        if (dateStr.isBlank()) return null
        return try {
            val utcFormat = SimpleDateFormat(DATE_PATTERN, Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
                isLenient = false
            }
            utcFormat.parse(dateStr.trim())?.time
        } catch (_: Exception) {
            null
        }
    }

    fun formatUtcMillisToDateStr(millis: Long): String {
        val utcFormat = SimpleDateFormat(DATE_PATTERN, Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return utcFormat.format(Date(millis))
    }
}

/**
 * Pure Calendar DatePickerDialog (with manual keyboard entry toggle disabled).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletCalendarDatePickerDialog(
    selectedDateStr: String,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    confirmLabel: String = "OK",
    cancelLabel: String = "Cancel"
) {
    val isDark = LocalAppTheme.current.isDark

    val initialUtcMillis = remember(selectedDateStr) {
        WalletDateUtils.parseDateToUtcMillis(selectedDateStr)
            ?: WalletDateUtils.parseDateToUtcMillis(WalletDateUtils.todayLocalDateStr())
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialUtcMillis,
        initialDisplayMode = DisplayMode.Picker
    )

    val containerColor = if (isDark) Color(0xFF0B1736) else Color.White
    val accentColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF0066FF)
    val primaryText = if (isDark) Color.White else Color(0xFF080F20)
    val secondaryText = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    val pickerColors = DatePickerDefaults.colors(
        containerColor = containerColor,
        titleContentColor = secondaryText,
        headlineContentColor = primaryText,
        weekdayContentColor = secondaryText,
        subheadContentColor = primaryText,
        navigationContentColor = primaryText,
        yearContentColor = primaryText,
        currentYearContentColor = accentColor,
        selectedYearContentColor = if (isDark) Color(0xFF081226) else Color.White,
        selectedYearContainerColor = accentColor,
        dayContentColor = primaryText,
        selectedDayContentColor = if (isDark) Color(0xFF081226) else Color.White,
        selectedDayContainerColor = accentColor,
        todayContentColor = accentColor,
        todayDateBorderColor = accentColor
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        colors = pickerColors,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        onDateSelected(WalletDateUtils.formatUtcMillisToDateStr(millis))
                    }
                    onDismiss()
                }
            ) {
                Text(
                    text = confirmLabel,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = cancelLabel,
                    fontWeight = FontWeight.SemiBold,
                    color = secondaryText
                )
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            showModeToggle = false,
            colors = pickerColors
        )
    }
}

/**
 * Clickable calendar-only date selector card for dialogs using standard M3 form layout
 * (e.g., HabitsScreen). Replaces manual OutlinedTextField typing.
 */
@Composable
fun CalendarDateSelectorCard(
    label: String,
    selectedDate: String,
    placeholder: String = "তারিখ নির্বাচন করুন",
    onClick: () -> Unit,
    onClear: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isDark = LocalAppTheme.current.isDark
    val containerColor = if (isDark) Color(0xFF0F1E36) else Color(0xFFF8FAFC)
    val borderColor = if (isDark) Color(0xFF1E3A8A) else Color(0xFFCBD5E1)
    val accentColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF0066FF)
    val primaryText = if (isDark) Color.White else Color(0xFF080F20)
    val secondaryText = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = label,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = secondaryText
                    )
                    Text(
                        text = selectedDate.ifBlank { placeholder },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (selectedDate.isNotBlank()) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 15.sp
                        ),
                        color = if (selectedDate.isNotBlank()) primaryText else secondaryText
                    )
                }
            }

            if (onClear != null && selectedDate.isNotBlank()) {
                IconButton(
                    onClick = onClear,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear date",
                        tint = secondaryText,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                Box(modifier = Modifier.padding(start = 6.dp)) {
                    Text(
                        text = "Change",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor
                    )
                }
            }
        }
    }
}
