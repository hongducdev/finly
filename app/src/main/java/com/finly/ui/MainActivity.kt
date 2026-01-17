package com.finly.ui

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.finly.service.QuickAddNotificationService
import com.finly.service.TransactionNotificationService
import com.finly.ui.screens.AddTransactionScreen
import com.finly.ui.screens.BudgetScreen
import com.finly.ui.screens.CalendarScreen
import com.finly.ui.screens.DashboardScreen
import com.finly.ui.screens.OnboardingScreen
import com.finly.ui.screens.SavingsGoalScreen
import com.finly.ui.screens.SettingsScreen
import com.finly.ui.screens.StatisticsScreen
import com.finly.ui.theme.FinlyTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * MainActivity - Entry point của ứng dụng
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // State để xử lý deep link từ notification
    private var pendingTransactionType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Xử lý intent từ notification
        handleIntent(intent)
        
        setContent {
            FinlyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FinlyApp(
                        hasNotificationPermission = { isNotificationListenerEnabled() },
                        pendingTransactionType = pendingTransactionType,
                        onTransactionTypeHandled = { pendingTransactionType = null }
                    )
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
        // Trigger recomposition khi quay lại app
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
            route = "add_transaction/{timestamp}?type={type}",
            arguments = listOf(
                navArgument("timestamp") { 
                    type = NavType.LongType
                    defaultValue = System.currentTimeMillis()
                },
                navArgument("type") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
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
