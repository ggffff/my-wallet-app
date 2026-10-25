package com.example

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.ui.auth.AuthScreen
import com.example.ui.bills.BillsScreen
import com.example.ui.chat.ChatBotScreen
import com.example.ui.dialogs.AddAccountDialog
import com.example.ui.dialogs.AddBillDialog
import com.example.ui.dialogs.AddGoalDialog
import com.example.ui.dialogs.AddTransactionDialog
import com.example.ui.goals.GoalsScreen
import com.example.ui.home.HomeScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.splash.SplashScreen
import com.example.ui.statements.StatementsScreen
import com.example.ui.theme.BrandGlow
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.MyWalletTheme
import com.example.util.AppLanguage
import com.example.util.LocalAppLanguage
import com.example.util.StringKeys
import com.example.util.appString
import com.example.util.configureAppCheck
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

enum class Screen {
    HOME,
    STATEMENTS,
    GOALS,
    BILLS,
    CHATBOT,
    SETTINGS
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Mandatory App Check intent wiring
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        setContent {
            val systemDark = isSystemInDarkTheme()
            var isDarkTheme by remember { mutableStateOf(true) }
            var appLanguage by remember { mutableStateOf(AppLanguage.ENGLISH) }

            MyWalletTheme(darkTheme = isDarkTheme) {
                CompositionLocalProvider(LocalAppLanguage provides appLanguage) {
                    var showSplash by remember { mutableStateOf(true) }
                    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

                    DisposableEffect(Unit) {
                        val authListener = FirebaseAuth.AuthStateListener { auth ->
                            currentUser = auth.currentUser
                        }
                        Firebase.auth.addAuthStateListener(authListener)
                        onDispose {
                            Firebase.auth.removeAuthStateListener(authListener)
                        }
                    }

                    if (showSplash) {
                        SplashScreen(onSplashFinished = { showSplash = false })
                    } else if (currentUser == null) {
                        AuthScreen(onAuthSuccess = { currentUser = Firebase.auth.currentUser })
                    } else {
                        MainWalletApp(
                            currentUserId = currentUser!!.uid,
                            isDarkTheme = isDarkTheme,
                            appLanguage = appLanguage,
                            onToggleDarkTheme = { isDarkTheme = it },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainWalletApp(
    currentUserId: String,
    isDarkTheme: Boolean,
    appLanguage: AppLanguage,
    onToggleDarkTheme: (Boolean) -> Unit,
    onToggleLanguage: (AppLanguage) -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember(currentUserId) { WalletRepository(context) }

    val userProfile by repository.observeUserProfile().collectAsStateWithLifecycle(initialValue = null)
    val accounts by repository.observeAccounts().collectAsStateWithLifecycle(initialValue = emptyList())
    val transactions by repository.observeTransactions().collectAsStateWithLifecycle(initialValue = emptyList())
    val goals by repository.observeGoals().collectAsStateWithLifecycle(initialValue = emptyList())
    val bills by repository.observeBills().collectAsStateWithLifecycle(initialValue = emptyList())

    var currentScreen by remember { mutableStateOf(Screen.HOME) }

    // Dialog states
    var showAddAccountDialog by remember { mutableStateOf(false) }
    var showAddTransactionDialog by remember { mutableStateOf(false) }
    var transactionDialogType by remember { mutableStateOf(TransactionType.SPENT) }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showAddBillDialog by remember { mutableStateOf(false) }

    // Handle system back navigation
    BackHandler(enabled = currentScreen != Screen.HOME) {
        currentScreen = Screen.HOME
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentScreen) {
                            Screen.HOME -> appString(StringKeys.APP_NAME)
                            Screen.STATEMENTS -> appString(StringKeys.STATEMENTS)
                            Screen.GOALS -> appString(StringKeys.GOALS)
                            Screen.BILLS -> appString(StringKeys.BILLS)
                            Screen.CHATBOT -> appString(StringKeys.CHATBOT)
                            Screen.SETTINGS -> appString(StringKeys.SETTINGS)
                        },
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    if (currentScreen == Screen.SETTINGS) {
                        IconButton(onClick = { currentScreen = Screen.HOME }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (currentScreen != Screen.SETTINGS) {
                        IconButton(onClick = { currentScreen = Screen.SETTINGS }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            if (currentScreen != Screen.SETTINGS) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    NavigationBarItem(
                        selected = currentScreen == Screen.HOME,
                        onClick = { currentScreen = Screen.HOME },
                        icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Home") },
                        label = { Text("Home", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrandGlow,
                            indicatorColor = BrandPrimary.copy(alpha = 0.2f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentScreen == Screen.STATEMENTS,
                        onClick = { currentScreen = Screen.STATEMENTS },
                        icon = { Icon(Icons.Default.ViewList, contentDescription = "Statements") },
                        label = { Text(appString(StringKeys.STATEMENTS), fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrandGlow,
                            indicatorColor = BrandPrimary.copy(alpha = 0.2f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentScreen == Screen.GOALS,
                        onClick = { currentScreen = Screen.GOALS },
                        icon = { Icon(Icons.Default.Savings, contentDescription = "Goals") },
                        label = { Text(appString(StringKeys.GOALS), fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrandGlow,
                            indicatorColor = BrandPrimary.copy(alpha = 0.2f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentScreen == Screen.BILLS,
                        onClick = { currentScreen = Screen.BILLS },
                        icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Bills") },
                        label = { Text(appString(StringKeys.BILLS), fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrandGlow,
                            indicatorColor = BrandPrimary.copy(alpha = 0.2f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentScreen == Screen.CHATBOT,
                        onClick = { currentScreen = Screen.CHATBOT },
                        icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI") },
                        label = { Text("AI Advisor", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrandGlow,
                            indicatorColor = BrandPrimary.copy(alpha = 0.2f)
                        )
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
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screenTransition"
            ) { screen ->
                when (screen) {
                    Screen.HOME -> HomeScreen(
                        userProfile = userProfile,
                        accounts = accounts,
                        transactions = transactions,
                        bills = bills,
                        onAddAccountClicked = { showAddAccountDialog = true },
                        onAddTransactionClicked = { type ->
                            transactionDialogType = type
                            showAddTransactionDialog = true
                        },
                        onViewAllStatementsClicked = { currentScreen = Screen.STATEMENTS },
                        onViewBillsClicked = { currentScreen = Screen.BILLS },
                        onViewGoalsClicked = { currentScreen = Screen.GOALS }
                    )
                    Screen.STATEMENTS -> StatementsScreen(
                        transactions = transactions,
                        onAddTransactionClicked = {
                            transactionDialogType = TransactionType.SPENT
                            showAddTransactionDialog = true
                        },
                        onDeleteTransactionClicked = { txn ->
                            coroutineScope.launch { repository.deleteTransaction(txn) }
                        }
                    )
                    Screen.GOALS -> GoalsScreen(
                        goals = goals,
                        accounts = accounts,
                        onAddGoalClicked = { showAddGoalDialog = true },
                        onAdjustSavings = { goal, delta, accId ->
                            coroutineScope.launch { repository.adjustGoalSavings(goal, delta, accId) }
                        },
                        onDeleteGoal = { goal ->
                            coroutineScope.launch { repository.deleteGoal(goal.id) }
                        }
                    )
                    Screen.BILLS -> BillsScreen(
                        bills = bills,
                        accounts = accounts,
                        onAddBillClicked = { showAddBillDialog = true },
                        onToggleBillPaid = { bill, accId ->
                            coroutineScope.launch { repository.toggleBillPaid(bill, accId) }
                        },
                        onDeleteBill = { bill ->
                            coroutineScope.launch { repository.deleteBill(bill.id) }
                        }
                    )
                    Screen.CHATBOT -> ChatBotScreen(
                        accounts = accounts,
                        transactions = transactions,
                        goals = goals,
                        bills = bills
                    )
                    Screen.SETTINGS -> SettingsScreen(
                        userProfile = userProfile,
                        currentDarkTheme = isDarkTheme,
                        currentLanguage = appLanguage,
                        onToggleDarkTheme = onToggleDarkTheme,
                        onToggleLanguage = onToggleLanguage,
                        onSaveProfile = { name, avatar ->
                            coroutineScope.launch {
                                repository.saveUserProfile(
                                    UserProfile(
                                        displayName = name,
                                        photoUrl = avatar,
                                        currency = "BDT",
                                        language = appLanguage.code,
                                        themeMode = if (isDarkTheme) "dark" else "light"
                                    )
                                )
                            }
                        },
                        onSignOut = onSignOut
                    )
                }
            }
        }
    }

    // Dialogs
    if (showAddAccountDialog) {
        AddAccountDialog(
            onDismiss = { showAddAccountDialog = false },
            onConfirm = { account ->
                coroutineScope.launch {
                    repository.addAccount(account)
                    showAddAccountDialog = false
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
                }
            }
        )
    }
}

private fun signOut(context: Context, onComplete: () -> Unit) {
    val credentialManager = CredentialManager.create(context)
    Firebase.auth.signOut()
    CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.e("Auth", "Failed to clear credential state", e)
        } finally {
            onComplete()
        }
    }
}
