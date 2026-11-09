package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.ui.motion.bounceClick
import com.example.ui.theme.LocalAppTheme
import com.example.util.AppCurrency

/**
 * Premium Blue & White "Select Currency" Floating Card Dialog.
 * Compact floating card modal ("শর্ট স্কিন") that overlays on top of the screen.
 * Changes ONLY currency format without affecting app language.
 */
@Composable
fun CurrencyDialog(
    currentCurrency: AppCurrency,
    onDismiss: () -> Unit,
    onSelectCurrency: (AppCurrency) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        val activeTheme = LocalAppTheme.current
        val isDark = activeTheme.isDark

        var searchQuery by remember { mutableStateOf("") }

        val cardBg = if (isDark) Color(0xFF0B1736) else Color.White
        val cardBorder = if (isDark) Color(0xFF1E3A8A) else Color(0xFFE2E8F0)
        val textPrimary = if (isDark) Color.White else Color(0xFF080F20)
        val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
        val brandBlue = Color(0xFF0066FF)
        val cyanAccent = Color(0xFF38BDF8)
        val activeAccent = if (isDark) cyanAccent else brandBlue
        val badgeBg = if (isDark) Color(0xFF132854) else Color(0xFFEBF4FF)

        val filteredCurrencies = remember(searchQuery) {
            val q = searchQuery.trim().lowercase()
            if (q.isBlank()) {
                AppCurrency.entries
            } else {
                AppCurrency.entries.filter {
                    it.code.lowercase().contains(q) ||
                    it.displayName.lowercase().contains(q) ||
                    it.countryName.lowercase().contains(q) ||
                    it.symbol.contains(q)
                }
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .heightIn(max = 620.dp)
                .shadow(
                    elevation = if (isDark) 12.dp else 8.dp,
                    shape = RoundedCornerShape(28.dp),
                    spotColor = if (isDark) Color(0xFF0052FE).copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.12f)
                )
                .testTag("currency_dialog"),
            shape = RoundedCornerShape(28.dp),
            color = cardBg,
            border = BorderStroke(1.2.dp, cardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Top Header: Title + Coins Stack Icon + Close (X) button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Select Currency",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                letterSpacing = 0.2.sp
                            ),
                            color = textPrimary,
                            modifier = Modifier.testTag("currency_dialog_title")
                        )
                        Text(
                            text = "Choose your preferred transaction currency",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = textSecondary
                        )
                    }

                    // Coins Stack Badge
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(badgeBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_coins_stack),
                            contentDescription = "Currency Icon",
                            tint = activeAccent,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search Bar
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isDark) Color(0xFF132042) else Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            textStyle = TextStyle(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = textPrimary
                            ),
                            cursorBrush = SolidColor(activeAccent),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            decorationBox = { innerTextField ->
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search currency, code or country...",
                                        style = TextStyle(fontSize = 14.sp, color = textSecondary)
                                    )
                                }
                                innerTextField()
                            }
                        )
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Clear, "Clear", tint = textSecondary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Currency List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredCurrencies) { currency ->
                        val isSelected = currency == currentCurrency

                        Surface(
                            onClick = {
                                onSelectCurrency(currency)
                                onDismiss()
                            },
                            shape = RoundedCornerShape(18.dp),
                            color = if (isSelected) {
                                if (isDark) Color(0xFF152A55) else Color(0xFFEBF4FF)
                            } else {
                                if (isDark) Color(0xFF0F1E40) else Color(0xFFFFFFFF)
                            },
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) activeAccent else cardBorder
                            ),
                            shadowElevation = if (isSelected) 3.dp else 1.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .bounceClick()
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
                                    // Country Flag Emoji
                                    Text(
                                        text = currency.flagEmoji,
                                        fontSize = 24.sp
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = currency.code,
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp
                                                ),
                                                color = textPrimary
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "(${currency.symbol})",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp
                                                ),
                                                color = activeAccent
                                            )
                                        }
                                        Text(
                                            text = "${currency.displayName} • ${currency.countryName}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                            color = textSecondary
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(activeAccent),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
