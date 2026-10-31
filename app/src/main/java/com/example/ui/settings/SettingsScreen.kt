package com.example.ui.settings

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.UserProfile
import com.example.ui.motion.PulsingAuraGlow
import com.example.ui.motion.bounceClick
import com.example.ui.motion.neonGlow
import com.example.ui.motion.tilt3D
import com.example.ui.theme.AppTheme
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.LocalAppTheme
import com.example.ui.theme.PremiumBlue
import com.example.ui.theme.PremiumPink
import com.example.ui.theme.PureWhite
import com.example.util.AppLanguage
import com.example.util.StringKeys
import com.example.util.appString
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun SettingsScreen(
    userProfile: UserProfile?,
    currentDarkTheme: Boolean = true,
    currentTheme: AppTheme = AppTheme.CYBER_NEON,
    currentLanguage: AppLanguage,
    onToggleDarkTheme: (Boolean) -> Unit = {},
    onSelectTheme: (AppTheme) -> Unit = {},
    onToggleLanguage: (AppLanguage) -> Unit,
    onSaveProfile: (String, String) -> Unit,
    onSignOut: () -> Unit,
    onOpenGoals: () -> Unit = {},
    onOpenBills: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val activeTheme = LocalAppTheme.current

    var displayName by remember(userProfile) {
        mutableStateOf(userProfile?.displayName?.ifBlank { Firebase.auth.currentUser?.displayName ?: "User" } ?: "User")
    }

    var selectedAvatar by remember(userProfile) {
        mutableStateOf(userProfile?.photoUrl?.ifBlank { "avatar_1" } ?: "avatar_1")
    }

    // Modern Zero-Permission Android Photo Picker for Custom Profile Picture
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val profileDir = File(context.filesDir, "profiles")
                    if (!profileDir.exists()) profileDir.mkdirs()
                    val photoFile = File(profileDir, "user_avatar_${System.currentTimeMillis()}.jpg")
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        photoFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    val localUriString = Uri.fromFile(photoFile).toString()
                    withContext(Dispatchers.Main) {
                        selectedAvatar = localUriString
                        onSaveProfile(displayName, localUriString)
                    }
                } catch (e: Exception) {
                    Log.e("SettingsScreen", "Failed to save profile picture", e)
                }
            }
        }
    }

    val isCustomPhoto = selectedAvatar.startsWith("file:") ||
            selectedAvatar.startsWith("content:") ||
            selectedAvatar.startsWith("http:") ||
            selectedAvatar.startsWith("https:") ||
            selectedAvatar.contains("/")

    val presetAvatars = listOf("avatar_1", "avatar_2", "avatar_3", "avatar_4")
    val avatarColors = listOf(PremiumBlue, PremiumPink, IncomeGreen, Color(0xFFA855F7))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 96.dp)
            .testTag("settings_screen")
    ) {
        // Header
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
            Text(
                text = "Profile & Settings",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Personalize photo, 5 distinct themes & financial utilities",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // ==========================================
        // 1. Profile Photo & Credentials Card
        // ==========================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .tilt3D(maxRotationX = 4f, maxRotationY = 4f, elevationDp = 8f),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Your Profile",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isCustomPhoto) "Custom Photo" else "Default Avatar",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive 3D Avatar Frame with Neon Glow & Camera Badge
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .bounceClick(scaleDown = 0.94f) {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Pulsing background aura
                    PulsingAuraGlow(
                        modifier = Modifier
                            .size(106.dp)
                            .clip(CircleShape),
                        primaryColor = activeTheme.primaryColor,
                        secondaryColor = activeTheme.secondaryColor
                    )

                    // Profile Image Container
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(activeTheme.primaryColor, activeTheme.secondaryColor)
                                )
                            )
                            .border(
                                width = 3.dp,
                                brush = Brush.sweepGradient(
                                    listOf(
                                        activeTheme.primaryColor,
                                        activeTheme.secondaryColor,
                                        activeTheme.glowColor,
                                        activeTheme.primaryColor
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCustomPhoto) {
                            AsyncImage(
                                model = selectedAvatar,
                                contentDescription = "User Profile Picture",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            val presetIndex = presetAvatars.indexOf(selectedAvatar).coerceAtLeast(0)
                            val col = avatarColors[presetIndex % avatarColors.size]
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Preset Avatar",
                                tint = col,
                                modifier = Modifier.size(54.dp)
                            )
                        }
                    }

                    // Camera / Upload Badge at bottom right
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(activeTheme.primaryColor)
                            .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Upload Photo",
                            tint = PureWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Photo Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = activeTheme.primaryColor,
                            contentColor = PureWhite
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isCustomPhoto) "Change Photo" else "Upload Photo",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (isCustomPhoto) {
                        OutlinedButton(
                            onClick = {
                                selectedAvatar = "avatar_1"
                                onSaveProfile(displayName, "avatar_1")
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Reset to Avatar",
                                modifier = Modifier.size(18.dp),
                                tint = ExpenseRed
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Alternative Preset Avatars
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Or choose a stylized 3D avatar:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        presetAvatars.forEachIndexed { index, avKey ->
                            val isSelected = selectedAvatar == avKey
                            val col = avatarColors[index % avatarColors.size]
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(col.copy(alpha = 0.2f))
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) col else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        selectedAvatar = avKey
                                        onSaveProfile(displayName, avKey)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = avKey,
                                    tint = col,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Display Name Text Field
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Display Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_name_input"),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = activeTheme.primaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Google Account: ${Firebase.auth.currentUser?.email ?: "Signed in"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = { onSaveProfile(displayName, selectedAvatar) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = activeTheme.primaryColor,
                        contentColor = PureWhite
                    )
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(appString(StringKeys.SAVE))
                }
            }
        }

        // ==========================================
        // 2. 5 Distinct Themes Selection Section
        // ==========================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .tilt3D(maxRotationX = 3f, maxRotationY = 3f, elevationDp = 6f),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(activeTheme.primaryColor.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ColorLens,
                                contentDescription = null,
                                tint = activeTheme.primaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "App Theme & Visual Effects",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "5 Distinct Ultra-Premium Styles",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Text(
                    text = "Select a theme to transform colors, ambient glowing halos, and card aesthetics:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Render the 5 distinct themes
                AppTheme.entries.forEach { themeOption ->
                    val isSelected = currentTheme == themeOption
                    ThemeSelectionCard(
                        theme = themeOption,
                        isSelected = isSelected,
                        onClick = {
                            onSelectTheme(themeOption)
                            onToggleDarkTheme(themeOption.isDark)
                        }
                    )
                }
            }
        }

        // ==========================================
        // 3. Language & Currency Section
        // ==========================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Language Toggle (English / Bangla)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(activeTheme.secondaryColor.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = activeTheme.secondaryColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = appString(StringKeys.LANGUAGE),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "English / বাংলা",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = currentLanguage == AppLanguage.ENGLISH,
                            onClick = { onToggleLanguage(AppLanguage.ENGLISH) },
                            label = { Text("EN") }
                        )
                        FilterChip(
                            selected = currentLanguage == AppLanguage.BANGLA,
                            onClick = { onToggleLanguage(AppLanguage.BANGLA) },
                            label = { Text("বাংলা") }
                        )
                    }
                }

                // Currency Display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(IncomeGreen.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = null,
                            tint = IncomeGreen,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = appString(StringKeys.CURRENCY_BDT),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Bangladeshi Taka (৳ BDT) default currency",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // ==========================================
        // 4. Financial Utilities Section (Goals & Bills)
        // ==========================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Financial Utilities",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Savings Goals shortcut
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onOpenGoals() }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(activeTheme.primaryColor.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Savings, contentDescription = null, tint = activeTheme.primaryColor, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Savings Goals", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                            Text("Track purchase targets & funds", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                }

                // Bills shortcut
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onOpenBills() }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(activeTheme.secondaryColor.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, tint = activeTheme.secondaryColor, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Upcoming Bills & Dues", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                            Text("Bill due alerts and payment logs", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ==========================================
        // 5. Sign Out Button
        // ==========================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Button(
                onClick = onSignOut,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ExpenseRed.copy(alpha = 0.15f),
                    contentColor = ExpenseRed
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
                    .testTag("sign_out_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(appString(StringKeys.SIGN_OUT), fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Interactive card displaying an individual theme with live preview colors and active glow
 */
@Composable
private fun ThemeSelectionCard(
    theme: AppTheme,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) theme.primaryColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    val borderWidth = if (isSelected) 2.dp else 1.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(theme.previewBg.copy(alpha = if (theme.isDark) 0.85f else 0.45f))
            .border(borderWidth, borderColor, RoundedCornerShape(16.dp))
            .then(
                if (isSelected) {
                    Modifier.neonGlow(color = theme.primaryColor, shape = RoundedCornerShape(16.dp), strokeWidth = 1.5.dp)
                } else Modifier
            )
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Color preview dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy((-6).dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(theme.primaryColor)
                            .border(1.5.dp, PureWhite, CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(theme.secondaryColor)
                            .border(1.5.dp, PureWhite, CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(theme.glowColor)
                            .border(1.5.dp, PureWhite, CircleShape)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = theme.displayName,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (theme.isDark) PureWhite else Color(0xFF0F172A)
                        )
                        if (theme.id == "cyber_neon") {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(theme.primaryColor.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "DEFAULT",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.primaryColor
                                )
                            }
                        }
                    }
                    Text(
                        text = theme.subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (theme.isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                    )
                }
            }

            AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(theme.primaryColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = PureWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
