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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
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
import com.example.ui.theme.midnightAppBackground
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
import androidx.compose.ui.draw.shadow
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
import com.example.data.model.Debt
import com.example.data.model.Goal
import com.example.data.model.HabitRoutine
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.data.model.UserProfile
import com.example.data.repository.WalletRepository
import com.example.ui.accounts.AccountsScreen
import com.example.ui.auth.AuthScreen
import com.example.ui.bills.BillsScreen
import com.example.ui.chat.ChatBotScreen
import com.example.ui.debts.DebtsScreen
import com.example.ui.dialogs.AddAccountDialog
import com.example.ui.dialogs.AddBillDialog
import com.example.ui.dialogs.AddGoalDialog
import com.example.ui.dialogs.AddTransactionDialog
import com.example.ui.goals.GoalsScreen
import com.example.ui.habits.HabitsScreen
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
import com.example.util.AppCurrency
import com.example.util.AppLanguage
import com.example.util.LocalAppCurrency
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
    DEBTS,
    HABITS,
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

        // App Check intent wiring before setContent
        try {
            configureAppCheck(this, intent)
        } catch (e: Throwable) {
            Log.e("MainActivity", "Error during configureAppCheck", e)
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
            val savedThemeId = remember {
                val existing = prefs.getString("selected_theme", null)
                if (existing == null) {
                    prefs.edit().putString("selected_theme", AppTheme.CYBER_NEON.id).apply()
                    AppTheme.CYBER_NEON.id
                } else {
                    existing
                }
            }
            var currentTheme by remember { mutableStateOf(AppTheme.fromId(savedThemeId)) }
            var isDarkTheme by remember { mutableStateOf(currentTheme.isDark) }
            val savedLangCode = remember { prefs.getString("selected_language", AppLanguage.BANGLADESH.code) ?: AppLanguage.BANGLADESH.code }
            var appLanguage by remember { mutableStateOf(AppLanguage.fromCode(savedLangCode)) }
            val savedCurrencyCode = remember { prefs.getString("selected_currency", AppCurrency.BDT.code) ?: AppCurrency.BDT.code }
            var appCurrency by remember { mutableStateOf(AppCurrency.fromCode(savedCurrencyCode)) }

            var isGuestMode by remember { mutableStateOf(prefs.getBoolean("guest_mode_enabled", false)) }

            MyWalletTheme(appTheme = currentTheme, darkTheme = isDarkTheme) {
                CompositionLocalProvider(
                    LocalAppLanguage provides appLanguage,
                    LocalAppCurrency provides appCurrency
                ) {
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
                        } else if (currentUser == null && !isGuestMode) {
                            AuthScreen(
                                currentLanguage = appLanguage,
                                onAuthSuccess = {
                                    val user = Firebase.auth.currentUser ?: auth?.currentUser
                                    Log.d("MainActivity", "Google sign-in succeeded, entering Dashboard directly: ${user?.uid}")
                                    isGuestMode = false
                                    prefs.edit().putBoolean("guest_mode_enabled", false).apply()
                                    prefs.edit().putString("selected_theme", currentTheme.id).apply()
                                    currentUser = user
                                },
                                onExploreAsGuest = {
                                    isGuestMode = true
                                    prefs.edit().putBoolean("guest_mode_enabled", true).apply()
                                    prefs.edit().putString("selected_theme", currentTheme.id).apply()
                                },
                                onToggleDarkTheme = { dark ->
                                    isDarkTheme = dark
                                    currentTheme = if (dark) AppTheme.CYBER_NEON else AppTheme.FROSTED_PEARL
                                    prefs.edit().putString("selected_theme", currentTheme.id).apply()
                                },
                                onToggleLanguage = { newLang ->
                                    appLanguage = newLang
                                    prefs.edit().putString("selected_language", newLang.code).apply()
                                }
                            )
                        } else {
                            // Direct navigation into MainWalletApp with strictly NO PIN verification screen
                            MainWalletApp(
                                currentUserId = currentUser?.uid ?: "guest_local",
                                isDarkTheme = isDarkTheme,
                                currentTheme = currentTheme,
                                appLanguage = appLanguage,
                                currentCurrency = appCurrency,
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
                                onToggleLanguage = { newLang ->
                                    appLanguage = newLang
                                    prefs.edit().putString("selected_language", newLang.code).apply()
                                },
                                onSelectCurrency = { newCurr ->
                                    appCurrency = newCurr
                                    prefs.edit().putString("selected_currency", newCurr.code).apply()
                                },
                                onSignOut = {
                                    signOut(this@MainActivity) {
                                        isGuestMode = false
                                        prefs.edit().putBoolean("guest_mode_enabled", false).apply()
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
    currentCurrency: AppCurrency = LocalAppCurrency.current,
    onToggleDarkTheme: (Boolean) -> Unit,
    onSelectTheme: (AppTheme) -> Unit = {},
    onToggleLanguage: (AppLanguage) -> Unit,
    onSelectCurrency: (AppCurrency) -> Unit = {},
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
    val debts by remember(repository) { repository.observeDebts() }.collectAsStateWithLifecycle(initialValue = emptyList())
    val habits by remember(repository) { repository.observeHabits() }.collectAsStateWithLifecycle(initialValue = emptyList())

    val prefs = remember { context.getSharedPreferences("wallet_prefs", Context.MODE_PRIVATE) }

    // Robust theme persistence: prioritize active/saved preference and synchronize to Firestore
    LaunchedEffect(userProfile) {
        val remoteTheme = userProfile?.themeMode
        val localThemeId = prefs.getString("selected_theme", currentTheme.id) ?: currentTheme.id
        val localTheme = AppTheme.fromId(localThemeId)
        if (currentTheme != localTheme) {
            onSelectTheme(localTheme)
        }
        if (userProfile != null && remoteTheme != localTheme.id) {
            try {
                repository.saveUserProfile(userProfile!!.copy(themeMode = localTheme.id))
            } catch (_: Exception) {}
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
            currentScreen == Screen.DEBTS ||
            currentScreen == Screen.HABITS ||
            currentScreen == Screen.CHATBOT

    Scaffold(
        containerColor = if (isDarkTheme) Color.Transparent else MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (currentScreen != Screen.HOME) {
                TopAppBar(
                    title = {
                        Text(
                            text = when (currentScreen) {
                                Screen.HOME -> appString(StringKeys.APP_NAME)
                                Screen.ACCOUNTS -> appString(StringKeys.ACCOUNTS)
                                Screen.REPORTS -> appString(StringKeys.REPORTS)
                                Screen.SETTINGS -> appString(StringKeys.SETTINGS)
                                Screen.TRANSACTIONS -> appString(StringKeys.STATEMENTS)
                                Screen.GOALS -> appString(StringKeys.GOALS)
                                Screen.BILLS -> appString(StringKeys.BILLS)
                                Screen.DEBTS -> appString(StringKeys.DEBTS)
                                Screen.HABITS -> appString(StringKeys.HABITS)
                                Screen.CHATBOT -> appString(StringKeys.CHATBOT)
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
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        },
        bottomBar = {
            if (!isSubScreen) {
                // Ultra-Premium Floating Glassmorphic Pill Island Navigation
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = if (isDarkTheme) 20.dp else 14.dp,
                                shape = RoundedCornerShape(28.dp),
                                spotColor = if (isDarkTheme) Color(0xFF0070F3).copy(alpha = 0.45f) else Color(0x330038A8)
                            )
                            .border(
                                width = 1.2.dp,
                                brush = Brush.linearGradient(
                                    if (isDarkTheme) listOf(
                                        Color(0xFF38BDF8).copy(alpha = 0.5f),
                                        Color(0xFF1E293B).copy(alpha = 0.3f),
                                        Color(0xFF0052FE).copy(alpha = 0.5f)
                                    ) else listOf(
                                        Color.White,
                                        Color(0xFFBFDBFE),
                                        Color(0xFF93C5FD)
                                    )
                                ),
                                shape = RoundedCornerShape(28.dp)
                            )
                            .testTag("bottom_navigation_bar"),
                        shape = RoundedCornerShape(28.dp),
                        color = if (isDarkTheme) Color(0xF209142E) else Color(0xF7FFFFFF),
                        tonalElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 1. Home
                            PremiumBottomNavItem(
                                selected = currentScreen == Screen.HOME,
                                onClick = { currentScreen = Screen.HOME },
                                icon = Icons.Default.AccountBalanceWallet,
                                label = appString(StringKeys.HOME)
                            )

                            // 2. Accounts
                            PremiumBottomNavItem(
                                selected = currentScreen == Screen.ACCOUNTS,
                                onClick = { currentScreen = Screen.ACCOUNTS },
                                icon = Icons.Default.CreditCard,
                                label = appString(StringKeys.ACCOUNTS)
                            )

                            // 3. Reports
                            PremiumBottomNavItem(
                                selected = currentScreen == Screen.REPORTS,
                                onClick = { currentScreen = Screen.REPORTS },
                                icon = Icons.Default.BarChart,
                                label = appString(StringKeys.REPORTS)
                            )

                            // 4. Settings
                            PremiumBottomNavItem(
                                selected = currentScreen == Screen.SETTINGS,
                                onClick = { currentScreen = Screen.SETTINGS },
                                icon = Icons.Default.Settings,
                                label = appString(StringKeys.SETTINGS)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .midnightAppBackground(isDark = isDarkTheme)
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
                        debts = debts,
                        habits = habits,
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
                        onViewDebtsClicked = { currentScreen = Screen.DEBTS },
                        onViewHabitsClicked = { currentScreen = Screen.HABITS },
                        onOpenAiAdvisor = { currentScreen = Screen.CHATBOT },
                        onOpenSettings = { currentScreen = Screen.SETTINGS },
                        onToggleLanguage = onToggleLanguage,
                        currentCurrency = currentCurrency,
                        onSelectCurrency = onSelectCurrency
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
                        currentCurrency = currentCurrency,
                        onToggleDarkTheme = onToggleDarkTheme,
                        onSelectTheme = onSelectThemeAndPersist,
                        onToggleLanguage = onToggleLanguage,
                        onSelectCurrency = onSelectCurrency,
                        onSaveProfile = { name, avatar ->
                            coroutineScope.launch {
                                repository.saveUserProfile(
                                    UserProfile(
                                        displayName = name,
                                        photoUrl = avatar,
                                        currency = currentCurrency.code,
                                        language = appLanguage.code,
                                        themeMode = currentTheme.id
                                    )
                                )
                                snackbarHostState.showSnackbar("Profile & theme saved")
                            }
                        },
                        onSignOut = onSignOut,
                        onOpenGoals = { currentScreen = Screen.GOALS },
                        onOpenBills = { currentScreen = Screen.BILLS },
                        onOpenDebts = { currentScreen = Screen.DEBTS },
                        onOpenHabits = { currentScreen = Screen.HABITS }
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
                    Screen.DEBTS -> DebtsScreen(
                        debts = debts,
                        accounts = accounts,
                        onAddDebt = { newDebt, accId ->
                            coroutineScope.launch {
                                try {
                                    repository.addDebt(newDebt, accId)
                                    snackbarHostState.showSnackbar("হিসাব সফলভাবে সংরক্ষিত হয়েছে")
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("ব্যর্থ হয়েছে: ${e.message}")
                                }
                            }
                        },
                        onRecordRepayment = { debt, repayAmt, accId ->
                            coroutineScope.launch {
                                try {
                                    repository.recordDebtRepayment(debt, repayAmt, accId)
                                    snackbarHostState.showSnackbar("পরিশোধ রেকর্ড সফল হয়েছে")
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("ব্যর্থ হয়েছে: ${e.message}")
                                }
                            }
                        },
                        onDeleteDebt = { debt ->
                            coroutineScope.launch {
                                try {
                                    repository.deleteDebt(debt.id)
                                    snackbarHostState.showSnackbar("হিসাব মুছে ফেলা হয়েছে")
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("ব্যর্থ হয়েছে: ${e.message}")
                                }
                            }
                        }
                    )
                    Screen.HABITS -> HabitsScreen(
                        habits = habits,
                        accounts = accounts,
                        onAddHabit = { newHabit ->
                            coroutineScope.launch {
                                try {
                                    repository.addHabit(newHabit)
                                    snackbarHostState.showSnackbar("নতুন রুটিন সংরক্ষিত হয়েছে")
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("ব্যর্থ হয়েছে: ${e.message}")
                                }
                            }
                        },
                        onMarkCompleted = { habit, compDate, costAccId ->
                            coroutineScope.launch {
                                try {
                                    repository.markHabitCompleted(habit, compDate, costAccId)
                                    snackbarHostState.showSnackbar("রুটিন সম্পন্ন হয়েছে এবং পরবর্তী তারিখ সেট করা হয়েছে!")
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("ব্যর্থ হয়েছে: ${e.message}")
                                }
                            }
                        },
                        onDeleteHabit = { habit ->
                            coroutineScope.launch {
                                try {
                                    repository.deleteHabit(habit.id)
                                    snackbarHostState.showSnackbar("রুটিন মুছে ফেলা হয়েছে")
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("ব্যর্থ হয়েছে: ${e.message}")
                                }
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
 * Ultra-Premium Bottom Navigation Item with animated glowing capsule,
 * spring bounce interaction, and neon micro-dot indicator.
 */
@Composable
private fun androidx.compose.foundation.layout.RowScope.PremiumBottomNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String
) {
    val isDark = LocalAppTheme.current.isDark
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.05f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "navScale"
    )

    val activeGradient = if (isDark) {
        Brush.horizontalGradient(listOf(Color(0xFF0052FE), Color(0xFF0284C7), Color(0xFF00E5FF)))
    } else {
        Brush.horizontalGradient(listOf(Color(0xFF0052FE), Color(0xFF0284C7)))
    }

    Box(
        modifier = Modifier
            .weight(1f)
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .bounceClick(scaleDown = 0.94f) { onClick() }
            .padding(vertical = 4.dp, horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (selected) {
                // Active Glowing Capsule Pill
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .width(58.dp)
                        .height(34.dp)
                        .shadow(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(17.dp),
                            spotColor = if (isDark) Color(0xFF00D2FF).copy(alpha = 0.65f) else Color(0xFF0052FE).copy(alpha = 0.45f)
                        )
                        .background(activeGradient, shape = RoundedCornerShape(17.dp))
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                // Inactive Muted Icon
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .width(58.dp)
                        .height(34.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            // Text Label
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) {
                    if (isDark) Color(0xFF38BDF8) else Color(0xFF0052FE)
                } else {
                    if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                },
                maxLines = 1,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            // Active Glowing Micro-dot indicator
            if (selected) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF38BDF8) else Color(0xFF0052FE))
                )
            } else {
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
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
