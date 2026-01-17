package com.finly.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.finly.R
import com.finly.data.local.entity.TransactionType
import com.finly.data.repository.TransactionRepository
import com.finly.service.QuickAddNotificationService
import com.finly.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*
import javax.inject.Inject

/**
 * Widget màn hình chính hiển thị thống kê chi tiêu
 */
@AndroidEntryPoint
class FinlyWidgetProvider : AppWidgetProvider() {

    @Inject
    lateinit var transactionRepository: TransactionRepository

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // Cập nhật tất cả các widget instance
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        // Nếu nhận được action update manually (ví dụ từ app sau khi thêm giao dịch)
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE || 
            intent.action == ACTION_REFRESH_WIDGET) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, FinlyWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        // Hủy job nếu cần (AppWidgetProvider là broadcast receiver nên lifecycle ngắn, 
        // nhưng scope vẫn có thể chạy cho đến khi process bị kill)
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        // Launch coroutine để lấy dữ liệu
        scope.launch {
            try {
                // Tính toán thời gian
                val calendar = Calendar.getInstance()
                
                // Hôm nay
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfDay = calendar.timeInMillis
                
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                val endOfDay = calendar.timeInMillis
                
                // Tháng này
                calendar.set(Calendar.DAY_OF_MONTH, 1) // Ngày 1 của tháng hiện tại
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                val startOfMonth = calendar.timeInMillis
                
                // Data fetching
                val todayTransactions = transactionRepository.getTransactionsBetweenSync(startOfDay, endOfDay)
                val monthTransactions = transactionRepository.getTransactionsBetweenSync(startOfMonth, System.currentTimeMillis())
                
                // Tính tổng chi tiêu (EXPENSE)
                val todayExpense = todayTransactions
                    .filter { it.type == TransactionType.EXPENSE }
                    .sumOf { it.amount }
                    
                val monthExpense = monthTransactions
                    .filter { it.type == TransactionType.EXPENSE }
                    .sumOf { it.amount }
                
                // Format tiền tệ
                val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
                
                // Update UI trên Main Thread
                val views = RemoteViews(context.packageName, R.layout.widget_finly)
                views.setTextViewText(R.id.tv_today_expense, "${formatter.format(todayExpense)} đ")
                views.setTextViewText(R.id.tv_month_expense, "${formatter.format(monthExpense)} đ")
                
                // Setup Actions
                setupWidgetIntents(context, views)
                
                appWidgetManager.updateAppWidget(appWidgetId, views)
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun setupWidgetIntents(context: Context, views: RemoteViews) {
        // Refresh Button
        val refreshIntent = Intent(context, FinlyWidgetProvider::class.java).apply {
            action = ACTION_REFRESH_WIDGET
        }
        val refreshPendingIntent = PendingIntent.getBroadcast(
            context, 0, refreshIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.btn_refresh, refreshPendingIntent)
        
        // Open App (Logo)
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context, 100, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.iv_app_logo, openAppPendingIntent)
        
        // Add Expense Button
        val expenseIntent = Intent(context, MainActivity::class.java).apply {
            action = QuickAddNotificationService.ACTION_ADD_EXPENSE
            putExtra(QuickAddNotificationService.EXTRA_TRANSACTION_TYPE, "EXPENSE")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val expensePendingIntent = PendingIntent.getActivity(
            context, 101, expenseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.btn_add_expense, expensePendingIntent)
        
        // Add Income Button
        val incomeIntent = Intent(context, MainActivity::class.java).apply {
            action = QuickAddNotificationService.ACTION_ADD_INCOME
            putExtra(QuickAddNotificationService.EXTRA_TRANSACTION_TYPE, "INCOME")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val incomePendingIntent = PendingIntent.getActivity(
            context, 102, incomeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.btn_add_income, incomePendingIntent)
    }

    companion object {
        const val ACTION_REFRESH_WIDGET = "com.finly.ACTION_REFRESH_WIDGET"
        
        fun sendRefreshBroadcast(context: Context) {
            val intent = Intent(context, FinlyWidgetProvider::class.java).apply {
                action = ACTION_REFRESH_WIDGET
            }
            context.sendBroadcast(intent)
        }
    }
}
