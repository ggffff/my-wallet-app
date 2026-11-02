package com.example

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import com.example.ui.theme.LocalAppTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Account
import com.example.data.model.Bill
import com.example.data.model.Goal
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.data.model.UserProfile
import com.example.data.repository.WalletRepository
import com.example.ui.accounts.AccountsScreen
import com.example.ui.auth.AuthScreen
import com.example.ui.bills.BillsScreen
import com.example.ui.chat.ChatBotScreen
import com.example.ui.dialogs.AddAccountDialog
import com.example.ui.dialogs.AddBillDialog
import com.example.ui.dialogs.AddGoalDialog
import com.example.ui.dialogs.AddTransactionDialog
import com.example.ui.goals.GoalsScreen
import com.example.ui.home.HomeScreen
import com.example.ui.motion.bounceClick
import com.example.ui.motion.neonGlow
import com.example.ui.reports.ReportsScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.splash.SplashScreen
import com.example.ui.statements.StatementsScreen
import com.example.ui.theme.AppTheme
import com.example.ui.theme.MyWalletTheme
import com.example.ui.theme.PremiumBlue
import com.example.ui.theme.PremiumPink
import com.example.ui.theme.PureWhite
import com.example.util.AppLanguage
import com.example.util.LocalAppLanguage
import com.example.util.StringKeys
import com.example.util.appString
import com.example.util.configureAppCheck
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

enum class Screen {
    HOME,
    ACCOUNTS,
    REPORTS,
    SETTINGS,
    // Working Sub-screens
    TRANSACTIONS,
    GOALS,
    BILLS,
    CHATBOT
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Install logging for uncaught exceptions
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("MyWallet", "Uncaught exception on thread ${thread.name}", throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }

        // App Check intent wiring offloaded to background thread to keep main thread and window rendering free
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                configureAppCheck(applicationContext, intent)
            } catch (e: Throwable) {
                Log.e("MainActivity", "Error during configureAppCheck", e)
            }
        }

        // EdgeToEdge invocation
        try {
            enableEdgeToEdge()
        } catch (t: Throwable) {
            Log.w("MainActivity", "enableEdgeToEdge error", t)
        }

        setContent {
            val systemDark = isSystemInDarkTheme()
            val context = LocalContext.current
            val prefs = remember { context.getSharedPreferences("wallet_prefs", Context.MODE_PRIVATE) }
            val savedThemeId = remember { prefs.getString("selected_theme", AppTheme.CYBER_NEON.id) ?: AppTheme.CYBER_NEON.id }
            var currentTheme by remember { mutableStateOf(AppTheme.fromId(savedThemeId)) }
            var isDarkTheme by remember { mutableStateOf(currentTheme.isDark) }
            var appLanguage by remember { mutableStateOf(AppLanguage.ENGLISH) }

            MyWalletTheme(appTheme = currentTheme, darkTheme = isDarkTheme) {
                CompositionLocalProvider(LocalAppLanguage provides appLanguage) {
                    var showSplash by remember { mutableStateOf(true) }

                    val auth = remember {
                        try {
                            Firebase.auth
                        } catch (e: Throwable) {
                            Log.e("MainActivity", "Firebase Auth init error", e)
                            null
                        }
                    }

                    var currentUser by remember { mutableStateOf(auth?.currentUser) }

                    DisposableEffect(auth) {
                        if (auth != null) {
                            val authListener = FirebaseAuth.AuthStateListener { fbAuth ->
                                currentUser = fbAuth.currentUser
                            }
                            auth.addAuthStateListener(authListener)
                            onDispose {
                                auth.removeAuthStateListener(authListener)
                            }
                        } else {
                            onDispose { }
                        }
                    }

                    Crossfade(
                        targetState = showSplash,
                        animationSpec = tween(200, easing = FastOutSlowInEasing),
                        label = "splashCrossfade"
                    ) { isSplash ->
                        if (isSplash) {
                            SplashScreen(onSplashFinished = { showSplash = false })
                        } else if (currentUser == null) {
                            AuthScreen(onAuthSuccess = {
                                val user = Firebase.auth.currentUser ?: auth?.currentUser
                                Log.d("MainActivity", "Google sign-in succeeded, entering Dashboard directly: ${user?.uid}")
                                currentUser = user
                            })
                        } else {
                            // Direct navigation into MainWalletApp with strictly NO PIN verification screen
                            MainWalletApp(
                                currentUserId = currentUser!!.uid,
                                isDarkTheme = isDarkTheme,
                                currentTheme = currentTheme,
                                appLanguage = appLanguage,
                                onToggleDarkTheme = { dark ->
                                    isDarkTheme = dark
                                    currentTheme = if (dark) AppTheme.CYBER_NEON else AppTheme.FROSTED_PEARL
                                    prefs.edit().putString("selected_theme", currentTheme.id).apply()
                                },
                                onSelectTheme = { theme ->
                                    currentTheme = theme
                                    isDarkTheme = theme.isDark
                                    prefs.edit().putString("selected_theme", theme.id).apply()
                                },
                                onToggleLanguage = { appLanguage = it },
                                onSignOut = {
                                    signOut(this@MainActivity) {
                                        currentUser = null
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainWalletApp(
    currentUserId: String,
    isDarkTheme: Boolean,
    currentTheme: AppTheme = AppTheme.CYBER_NEON,
    appLanguage: AppLanguage,
    onToggleDarkTheme: (Boolean) -> Unit,
    onSelectTheme: (AppTheme) -> Unit = {},
    onToggleLanguage: (AppLanguage) -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val repository = remember(currentUserId) { WalletRepository(context) }

    val userProfile by remember(repository) { repository.observeUserProfile() }.collectAsStateWithLifecycle(initialValue = null)
    val accounts by remember(repository) { repository.observeAccounts() }.collectAsStateWithLifecycle(initialValue = emptyList())
    val transactions by remember(repository) { repository.observeTransactions() }.collectAsStateWithLifecycle(initialValue = emptyList())
    val goals by remember(repository) { repository.observeGoals() }.collectAsStateWithLifecycle(initialValue = emptyList())
    val bills by remember(repository) { repository.observeBills() }.collectAsStateWithLifecycle(initialValue = emptyList())

    val prefs = remember { context.getSharedPreferences("wallet_prefs", Context.MODE_PRIVATE) }

    // Robust theme persistence: prioritize saved preference and synchronize to Firestore so reloads never revert
    LaunchedEffect(userProfile) {
        val remoteTheme = userProfile?.themeMode
        val localThemeId = prefs.getString("selected_theme", null)
        if (localThemeId != null) {
            val localTheme = AppTheme.fromId(localThemeId)
            if (currentTheme != localTheme) {
                onSelectTheme(localTheme)
            }
            if (remoteTheme != localTheme.id && userProfile != null) {
                try {
                    repository.saveUserProfile(userProfile!!.copy(themeMode = localTheme.id))
                } catch (_: Exception) {}
            }
        } else if (!remoteTheme.isNullOrBlank()) {
            val remote = AppTheme.fromId(remoteTheme)
            onSelectTheme(remote)
            prefs.edit().putString("selected_theme", remote.id).apply()
        }
    }

    val onSelectThemeAndPersist: (AppTheme) -> Unit = { theme ->
        onSelectTheme(theme)
        prefs.edit().putString("selected_theme", theme.id).apply()
        coroutineScope.launch {
            try {
                val current = userProfile ?: UserProfile()
                repository.saveUserProfile(current.copy(themeMode = theme.id))
            } catch (t: Throwable) {
                android.util.Log.w("MainActivity", "Failed to sync theme to Firestore", t)
            }
        }
    }

    var currentScreen by remember { mutableStateOf(Screen.HOME) }

    // Dialog states
    var showAddAccountDialog by remember { mutableStateOf(false) }
    var showAddTransactionDialog by remember { mutableStateOf(false) }
    var transactionDialogType by remember { mutableStateOf(TransactionType.SPENT) }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showAddBillDialog by remember { mutableStateOf(false) }

    // System BackHandler returns to Home from any tab or sub-screen
    BackHandler(enabled = currentScreen != Screen.HOME) {
        currentScreen = Screen.HOME
    }

    val isSubScreen = currentScreen == Screen.TRANSACTIONS ||
            currentScreen == Screen.GOALS ||
            currentScreen == Screen.BILLS ||
            currentScreen == Screen.CHATBOT

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (currentScreen != Screen.HOME) {
                TopAppBar(
                    title = {
                        Text(
                            text = when (currentScreen) {
                                Screen.HOME -> "My Wallet"
                                Screen.ACCOUNTS -> "Accounts"
                                Screen.REPORTS -> "Reports & Analytics"
                                Screen.SETTINGS -> "Settings & Profile"
                                Screen.TRANSACTIONS -> "Statements"
                                Screen.GOALS -> "Saving Goals"
                                Screen.BILLS -> "Bills & Dues"
                                Screen.CHATBOT -> "AI Financial Advisor"
                            },
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    navigationIcon = {
                        if (isSubScreen) {
                            IconButton(onClick = { currentScreen = Screen.HOME }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    },
                    actions = {
                        if (!isSubScreen && currentScreen != Screen.SETTINGS) {
                            IconButton(
                                onClick = { currentScreen = Screen.SETTINGS },
                                modifier = Modifier.bounceClick(scaleDown = 0.9f)
                            ) {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        },
        bottomBar = {
            if (!isSubScreen) {
                // 4 Core Tabs with modern Pill Capsule Indicator design:
                // Home, Accounts, Reports, Settings
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("bottom_navigation_bar")
                ) {
                    // 1. Home
                    Animated3DBottomNavItem(
                        selected = currentScreen == Screen.HOME,
                        onClick = { currentScreen = Screen.HOME },
                        icon = Icons.Default.AccountBalanceWallet,
                        label = "Home",
                        glowColor = MaterialTheme.colorScheme.primary
                    )

                    // 2. Accounts
                    Animated3DBottomNavItem(
                        selected = currentScreen == Screen.ACCOUNTS,
                        onClick = { currentScreen = Screen.ACCOUNTS },
                        icon = Icons.Default.CreditCard,
                        label = "Accounts",
                        glowColor = MaterialTheme.colorScheme.primary
                    )

                    // 3. Reports
                    Animated3DBottomNavItem(
                        selected = currentScreen == Screen.REPORTS,
                        onClick = { currentScreen = Screen.REPORTS },
                        icon = Icons.Default.BarChart,
                        label = "Reports",
                        glowColor = MaterialTheme.colorScheme.primary
                    )

                    // 4. Settings
                    Animated3DBottomNavItem(
                        selected = currentScreen == Screen.SETTINGS,
                        onClick = { currentScreen = Screen.SETTINGS },
                        icon = Icons.Default.Settings,
                        label = "Settings",
                        glowColor = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(260, easing = FastOutSlowInEasing)) +
                     scaleIn(initialScale = 0.96f, animationSpec = tween(260, easing = FastOutSlowInEasing)))
                        .togetherWith(
                            fadeOut(animationSpec = tween(160, easing = FastOutSlowInEasing)) +
                            scaleOut(targetScale = 1.02f, animationSpec = tween(160, easing = FastOutSlowInEasing))
                        )
                },
                label = "screenTransition"
            ) { screen ->
                when (screen) {
                    Screen.HOME -> HomeScreen(
                        userProfile = userProfile,
                        accounts = accounts,
                        transactions = transactions,
                        bills = bills,
                        onAddAccountClicked = { showAddAccountDialog = true },
                        onUpdateAccount = { updated ->
                            coroutineScope.launch {
                                try {
                                    repository.updateAccount(updated)
                                    snackbarHostState.showSnackbar("Account \"${updated.name}\" balance updated")
                                } catch (e: Exception) {
                                    Log.e("MainActivity", "Error updating account", e)
                                    snackbarHostState.showSnackbar("Failed to update account: ${e.message}")
                                }
                            }
                        },
                        onDeleteAccount = { acc ->
                            coroutineScope.launch {
                                try {
                                    repository.deleteAccount(acc.id)
                                    snackbarHostState.showSnackbar("Account \"${acc.name}\" deleted")
                                } catch (e: Exception) {
                                    Log.e("MainActivity", "Error deleting account", e)
                                    snackbarHostState.showSnackbar("Failed to delete account: ${e.message}")
                                }
                            }
                        },
                        onAddTransactionClicked = { type ->
                            transactionDialogType = type
                            showAddTransactionDialog = true
                        },
                        onViewAllStatementsClicked = { currentScreen = Screen.TRANSACTIONS },
                        onViewBillsClicked = { currentScreen = Screen.BILLS },
                        onViewGoalsClicked = { currentScreen = Screen.GOALS },
                        onOpenAiAdvisor = { currentScreen = Screen.CHATBOT },
                        onOpenSettings = { currentScreen = Screen.SETTINGS }
                    )
                    Screen.ACCOUNTS -> AccountsScreen(
                        accounts = accounts,
                        onAddAccountClicked = { showAddAccountDialog = true },
                        onUpdateAccount = { updated ->
                            coroutineScope.launch {
                                try {
                                    repository.updateAccount(updated)
                                    snackbarHostState.showSnackbar("Account \"${updated.name}\" updated")
                                } catch (e: Exception) {
                                    Log.e("MainActivity", "Error updating account", e)
                                    snackbarHostState.showSnackbar("Failed to update account: ${e.message}")
                                }
                            }
                        },
                        onDeleteAccount = { acc ->
                            coroutineScope.launch {
                                try {
                                    repository.deleteAccount(acc.id)
                                    snackbarHostState.showSnackbar("Account \"${acc.name}\" deleted")
                                } catch (e: Exception) {
                                    Log.e("MainActivity", "Error deleting account", e)
                                    snackbarHostState.showSnackbar("Failed to delete account: ${e.message}")
                                }
                            }
                        }
                    )
                    Screen.REPORTS -> ReportsScreen(
                        transactions = transactions,
                        onOpenAiAdvisor = { currentScreen = Screen.CHATBOT }
                    )
                    Screen.SETTINGS -> SettingsScreen(
                        userProfile = userProfile,
                        currentDarkTheme = isDarkTheme,
                        currentTheme = currentTheme,
                        currentLanguage = appLanguage,
                        onToggleDarkTheme = onToggleDarkTheme,
                        onSelectTheme = onSelectThemeAndPersist,
                        onToggleLanguage = onToggleLanguage,
                        onSaveProfile = { name, avatar ->
                            coroutineScope.launch {
                                repository.saveUserProfile(
                                    UserProfile(
                                        displayName = name,
                                        photoUrl = avatar,
                                        currency = "BDT",
                                        language = appLanguage.code,
                                        themeMode = currentTheme.id
                                    )
                                )
                                snackbarHostState.showSnackbar("Profile & theme saved")
                            }
                        },
                        onSignOut = onSignOut,
                        onOpenGoals = { currentScreen = Screen.GOALS },
                        onOpenBills = { currentScreen = Screen.BILLS }
                    )
                    Screen.TRANSACTIONS -> StatementsScreen(
                        transactions = transactions,
                        onAddTransactionClicked = {
                            transactionDialogType = TransactionType.SPENT
                            showAddTransactionDialog = true
                        },
                        onDeleteTransactionClicked = { txn ->
                            coroutineScope.launch {
                                try {
                                    repository.deleteTransaction(txn)
                                    snackbarHostState.showSnackbar("Transaction deleted")
                                } catch (t: Throwable) {
                                    android.util.Log.e("MainActivity", "Error deleting transaction", t)
                                    snackbarHostState.showSnackbar("Could not delete: ${t.localizedMessage ?: "Unknown error"}")
                                }
                            }
                        }
                    )
                    Screen.GOALS -> GoalsScreen(
                        goals = goals,
                        accounts = accounts,
                        onAddGoalClicked = { showAddGoalDialog = true },
                        onAdjustSavings = { goal, delta, accId ->
                            coroutineScope.launch {
                                repository.adjustGoalSavings(goal, delta, accId)
                                snackbarHostState.showSnackbar("Goal updated")
                            }
                        },
                        onDeleteGoal = { goal ->
                            coroutineScope.launch {
                                repository.deleteGoal(goal.id)
                                snackbarHostState.showSnackbar("Goal removed")
                            }
                        }
                    )
                    Screen.BILLS -> BillsScreen(
                        bills = bills,
                        accounts = accounts,
                        onAddBillClicked = { showAddBillDialog = true },
                        onToggleBillPaid = { bill, accId ->
                            coroutineScope.launch {
                                repository.toggleBillPaid(bill, accId)
                                snackbarHostState.showSnackbar(if (!bill.isPaid) "Bill marked as paid" else "Bill reopened")
                            }
                        },
                        onDeleteBill = { bill ->
                            coroutineScope.launch {
                                repository.deleteBill(bill.id)
                                snackbarHostState.showSnackbar("Bill removed")
                            }
                        }
                    )
                    Screen.CHATBOT -> ChatBotScreen(
                        accounts = accounts,
                        transactions = transactions,
                        goals = goals,
                        bills = bills
                    )
                }
            }
        }
    }

    // Modal Dialogs with clear feedback
    if (showAddAccountDialog) {
        AddAccountDialog(
            onDismiss = { showAddAccountDialog = false },
            onConfirm = { account ->
                showAddAccountDialog = false
                coroutineScope.launch {
                    try {
                        repository.addAccount(account)
                        snackbarHostState.showSnackbar("Account \"${account.name}\" created")
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Failed to add account", e)
                        snackbarHostState.showSnackbar("Could not add account: ${e.message}")
                    }
                }
            }
        )
    }

    if (showAddTransactionDialog) {
        AddTransactionDialog(
            accounts = accounts,
            initialType = transactionDialogType,
            onDismiss = { showAddTransactionDialog = false },
            onConfirm = { txn ->
                coroutineScope.launch {
                    repository.addTransaction(txn)
                    showAddTransactionDialog = false
                    snackbarHostState.showSnackbar("Transaction \"${txn.title}\" recorded")
                }
            },
            onNeedAddAccount = {
                showAddTransactionDialog = false
                showAddAccountDialog = true
            }
        )
    }

    if (showAddGoalDialog) {
        AddGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onConfirm = { goal ->
                coroutineScope.launch {
                    repository.addGoal(goal)
                    showAddGoalDialog = false
                    snackbarHostState.showSnackbar("Goal \"${goal.title}\" created")
                }
            }
        )
    }

    if (showAddBillDialog) {
        AddBillDialog(
            onDismiss = { showAddBillDialog = false },
            onConfirm = { bill ->
                coroutineScope.launch {
                    repository.addBill(bill)
                    showAddBillDialog = false
                    snackbarHostState.showSnackbar("Bill \"${bill.title}\" added")
                }
            }
        )
    }
}

/**
 * Bottom Navigation Item with pill capsule indicator matching design.
 */
@Composable
private fun androidx.compose.foundation.layout.RowScope.Animated3DBottomNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    glowColor: Color
) {
    val isDark = LocalAppTheme.current.isDark
    val activePillColor = if (isDark) MaterialTheme.colorScheme.primary.copy(alpha = 0.22f) else Color(0xFFDBEAFE)
    val activeIconTint = if (isDark) Color(0xFF60A5FA) else Color(0xFF1D4ED8)
    val inactiveTint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            if (selected) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .width(64.dp)
                        .height(32.dp)
                        .background(activePillColor, shape = RoundedCornerShape(16.dp))
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = activeIconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .width(64.dp)
                        .height(32.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = inactiveTint,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        },
        label = {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) (if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A)) else inactiveTint,
                maxLines = 2,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 13.sp
            )
        },
        colors = NavigationBarItemDefaults.colors(
            indicatorColor = Color.Transparent
        )
    )
}

private fun signOut(context: Context, onComplete: () -> Unit) {
    val credentialManager = try {
        CredentialManager.create(context)
    } catch (t: Throwable) {
        Log.e("Auth", "Failed to get CredentialManager for sign out", t)
        null
    }
    try {
        Firebase.auth.signOut()
    } catch (t: Throwable) {
        Log.e("Auth", "Failed to sign out from Firebase", t)
    }
    CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
        try {
            credentialManager?.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.e("Auth", "Failed to clear credential state", e)
        } finally {
            onComplete()
        }
    }
}
