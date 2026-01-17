package com.finly.ui

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
import com.finly.ui.screens.AddTransactionScreen
import com.finly.ui.screens.BudgetScreen
import com.finly.ui.screens.CalendarScreen
import com.finly.ui.screens.DashboardScreen
import com.finly.ui.screens.LockScreen
import com.finly.ui.screens.OnboardingScreen
import com.finly.ui.screens.SavingsGoalScreen
import com.finly.ui.screens.SettingsScreen
import com.finly.ui.screens.StatisticsScreen
import com.finly.ui.theme.FinlyTheme
import com.finly.util.AppLockManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * MainActivity - Entry point của ứng dụng
 */
@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var appLockManager: AppLockManager
    
    @Inject
    lateinit var securityPreferences: SecurityPreferences

    // State để xử lý deep link từ notification
    private var pendingTransactionType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Lock State
        appLockManager.initOnStartup()
        
        // Xử lý intent từ notification
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
                                    .zIndex(999f) // Ensure it's on top
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
        // Check lock when returning to foreground
        appLockManager.checkAndLock()
        // Update last active timestamp
        appLockManager.updateLastActive()
    }
    
    override fun onPause() {
        super.onPause()
        // Record background time
        appLockManager.onAppBackgrounded()
    }
    
    override fun onUserInteraction() {
        super.onUserInteraction()
        appLockManager.updateLastActive()
    }
    
    /**
     * Kiểm tra quyền Notification Listener
     */
    private fun isNotificationListenerEnabled(): Boolean {
        val componentName = ComponentName(this, TransactionNotificationService::class.java)
        val enabledListeners = Settings.Secure.getString(
            contentResolver,
            "enabled_notification_listeners"
        )
        return enabledListeners?.contains(componentName.flattenToString()) == true
    }
}

/**
 * App Navigation
 */
@Composable
fun FinlyApp(
    hasNotificationPermission: () -> Boolean,
    pendingTransactionType: String? = null,
    onTransactionTypeHandled: () -> Unit = {}
) {
    val navController = rememberNavController()
    
    // Xử lý deep link từ notification
    LaunchedEffect(pendingTransactionType) {
        if (pendingTransactionType != null) {
            val timestamp = System.currentTimeMillis()
            navController.navigate("add_transaction/$timestamp?type=$pendingTransactionType") {
                launchSingleTop = true
            }
            onTransactionTypeHandled()
        }
    }
    
    // Kiểm tra quyền để quyết định màn hình bắt đầu
    val startDestination = remember {
        if (hasNotificationPermission()) "calendar" else "onboarding"
    }
    
    NavHost(
        navController = navController,
        startDestination = startDestination
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
                    navController.navigate("add_transaction/$timestamp")
                },
                onNavigateToEditTransaction = { transactionId ->
                    val timestamp = System.currentTimeMillis()
                    navController.navigate("add_transaction/$timestamp?transactionId=$transactionId")
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
            route = "add_transaction/{timestamp}?type={type}&transactionId={transactionId}",
            arguments = listOf(
                navArgument("timestamp") { 
                    type = NavType.LongType
                    defaultValue = System.currentTimeMillis()
                },
                navArgument("type") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("transactionId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) {
            AddTransactionScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
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
                }
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
