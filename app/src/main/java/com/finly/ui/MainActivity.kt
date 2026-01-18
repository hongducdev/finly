package com.finly.ui

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.finly.data.local.SecurityPreferences
import com.finly.service.QuickAddNotificationService
import com.finly.service.TransactionNotificationService
import com.finly.ui.components.FinlyBottomBar
import com.finly.ui.screens.AddTransactionScreen
import com.finly.ui.screens.AmountDescriptionScreen
import com.finly.ui.screens.BudgetScreen
import com.finly.ui.screens.CalendarScreen
import com.finly.ui.screens.CategorySelectionScreen
import com.finly.ui.screens.CustomCategoryCreatorScreen
import com.finly.ui.screens.DashboardScreen
import com.finly.ui.screens.DebtListScreen
import com.finly.ui.screens.AddEditDebtScreen
import com.finly.ui.screens.LockScreen
import com.finly.ui.screens.OnboardingScreen
import com.finly.ui.screens.SavingsGoalScreen
import com.finly.ui.screens.SettingsScreen
import com.finly.ui.screens.StatisticsScreen
import com.finly.ui.theme.FinlyTheme
import com.finly.util.AppLockManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var appLockManager: AppLockManager
    
    @Inject
    lateinit var securityPreferences: SecurityPreferences

    private var pendingTransactionType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        appLockManager.initOnStartup()
        handleIntent(intent)
        
        setContent {
            FinlyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val isLocked by appLockManager.isLocked.collectAsStateWithLifecycle()
                    
                    Box(modifier = Modifier.fillMaxSize()) {
                        FinlyApp(
                            hasNotificationPermission = { isNotificationListenerEnabled() },
                            pendingTransactionType = pendingTransactionType,
                            onTransactionTypeHandled = { pendingTransactionType = null }
                        )
                        
                        if (isLocked) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .zIndex(999f)
                            ) {
                                LockScreen(
                                    appLockManager = appLockManager,
                                    securityPreferences = securityPreferences
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }
    
    private fun handleIntent(intent: Intent?) {
        when (intent?.action) {
            QuickAddNotificationService.ACTION_ADD_EXPENSE -> {
                pendingTransactionType = "EXPENSE"
            }
            QuickAddNotificationService.ACTION_ADD_INCOME -> {
                pendingTransactionType = "INCOME"
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        appLockManager.checkAndLock()
        appLockManager.updateLastActive()
    }
    
    override fun onPause() {
        super.onPause()
        appLockManager.onAppBackgrounded()
    }
    
    override fun onUserInteraction() {
        super.onUserInteraction()
        appLockManager.updateLastActive()
    }
    
    private fun isNotificationListenerEnabled(): Boolean {
        val componentName = ComponentName(this, TransactionNotificationService::class.java)
        val enabledListeners = Settings.Secure.getString(
            contentResolver,
            "enabled_notification_listeners"
        )
        return enabledListeners?.contains(componentName.flattenToString()) == true
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinlyApp(
    hasNotificationPermission: () -> Boolean,
    pendingTransactionType: String? = null,
    onTransactionTypeHandled: () -> Unit = {}
) {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryFlow
        .collectAsState(initial = navController.currentBackStackEntry)
        .value?.destination?.route
    
    LaunchedEffect(pendingTransactionType) {
        if (pendingTransactionType != null) {
            val timestamp = System.currentTimeMillis()
            navController.navigate("category_selection/$timestamp/$pendingTransactionType") {
                launchSingleTop = true
            }
            onTransactionTypeHandled()
        }
    }
    
    val startDestination = remember {
        if (hasNotificationPermission()) "calendar" else "onboarding"
    }
    
    val bottomNavRoutes = setOf("calendar", "statistics", "debt_list", "settings")
    val showBottomBar = currentRoute in bottomNavRoutes
    
    Scaffold(
        topBar = {
            if (showBottomBar) {
                TopAppBar(
                    title = { Text("Finly", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                FinlyBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onAddTransaction = {
                        val timestamp = System.currentTimeMillis()
                        navController.navigate("category_selection/$timestamp/EXPENSE")
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("onboarding") {
                OnboardingScreen(
                    onComplete = {
                        navController.navigate("calendar") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    }
                )
            }
            
            composable("calendar") {
                CalendarScreen(
                    onNavigateToAddTransaction = { timestamp ->
                        navController.navigate("category_selection/$timestamp/EXPENSE")
                    },
                    onNavigateToEditTransaction = { transactionId ->
                        val timestamp = System.currentTimeMillis()
                        navController.navigate("category_selection/$timestamp/EXPENSE?transactionId=$transactionId")
                    },
                    onNavigateToSettings = {
                        navController.navigate("settings")
                    },
                    onNavigateToStatistics = {
                        navController.navigate("statistics")
                    }
                )
            }
            
            composable("dashboard") {
                DashboardScreen(
                    onNavigateToSettings = {
                        navController.navigate("settings")
                    },
                    onNavigateToAddTransaction = {
                        val timestamp = System.currentTimeMillis()
                        navController.navigate("add_transaction/$timestamp")
                    }
                )
            }

        composable(
            route = "category_selection/{timestamp}/{type}?transactionId={transactionId}",
            arguments = listOf(
                navArgument("timestamp") { type = NavType.LongType },
                navArgument("type") { type = NavType.StringType },
                navArgument("transactionId") { type = NavType.LongType; defaultValue = 0L }
            )
        ) { backStackEntry ->
            val timestamp = backStackEntry.arguments?.getLong("timestamp") ?: System.currentTimeMillis()
            val typeString = backStackEntry.arguments?.getString("type") ?: "EXPENSE"
            val transactionId = backStackEntry.arguments?.getLong("transactionId") ?: 0L
            val transactionType = com.finly.data.local.entity.TransactionType.valueOf(typeString)
            
            CategorySelectionScreen(
                transactionType = transactionType,
                onNavigateBack = { navController.popBackStack() },
                onCategorySelected = { isCustom, categoryId ->
                    navController.navigate("amount_description/$timestamp/$isCustom/$categoryId/$typeString?transactionId=$transactionId")
                },
                onCreateCustomCategory = {
                    navController.navigate("custom_category_creator/$timestamp/$typeString?transactionId=$transactionId")
                }
            )
        }
        
        composable(
            route = "custom_category_creator/{timestamp}/{type}?transactionId={transactionId}",
            arguments = listOf(
                navArgument("timestamp") { type = NavType.LongType },
                navArgument("type") { type = NavType.StringType },
                navArgument("transactionId") { type = NavType.LongType; defaultValue = 0L }
            )
        ) { backStackEntry ->
            val timestamp = backStackEntry.arguments?.getLong("timestamp") ?: System.currentTimeMillis()
            val typeString = backStackEntry.arguments?.getString("type") ?: "EXPENSE"
            val transactionId = backStackEntry.arguments?.getLong("transactionId") ?: 0L
            val transactionType = com.finly.data.local.entity.TransactionType.valueOf(typeString)
            
            CustomCategoryCreatorScreen(
                transactionType = transactionType,
                onNavigateBack = { navController.popBackStack() },
                onCategoryCreated = { categoryId ->
                    navController.popBackStack()
                    navController.navigate("amount_description/$timestamp/true/$categoryId/$typeString?transactionId=$transactionId")
                }
            )
        }
        
        composable(
            route = "amount_description/{timestamp}/{isCustom}/{categoryId}/{type}?transactionId={transactionId}",
            arguments = listOf(
                navArgument("timestamp") { type = NavType.LongType },
                navArgument("isCustom") { type = NavType.BoolType },
                navArgument("categoryId") { type = NavType.StringType },
                navArgument("type") { type = NavType.StringType },
                navArgument("transactionId") { type = NavType.LongType; defaultValue = 0L }
            )
        ) { backStackEntry ->
            val timestamp = backStackEntry.arguments?.getLong("timestamp") ?: System.currentTimeMillis()
            val isCustom = backStackEntry.arguments?.getBoolean("isCustom") ?: false
            val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
            val typeString = backStackEntry.arguments?.getString("type") ?: "EXPENSE"
            val transactionId = backStackEntry.arguments?.getLong("transactionId") ?: 0L
            val transactionType = com.finly.data.local.entity.TransactionType.valueOf(typeString)
            
            AmountDescriptionScreen(
                isCustomCategory = isCustom,
                categoryId = categoryId,
                transactionType = transactionType,
                timestamp = timestamp,
                transactionId = transactionId,
                onNavigateBack = { navController.popBackStack("calendar", inclusive = false) }
            )
        }
        
        composable("statistics") {
            StatisticsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("settings") {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToBudget = {
                    navController.navigate("budget")
                },
                onNavigateToSavingsGoal = {
                    navController.navigate("savings_goal")
                },
                onNavigateToDebts = {
                    navController.navigate("debt_list")
                }
            )
        }
        
        // Debt Management
        composable("debt_list") {
            DebtListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddDebt = { navController.navigate("add_debt") }
            )
        }
        
        composable("add_debt") {
            AddEditDebtScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable("budget") {
            BudgetScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("savings_goal") {
            SavingsGoalScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
    }
}
