package com.finly.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.finly.R
import com.finly.ui.MainActivity

/**
 * Foreground Service hiển thị thông báo với nút thêm nhanh Chi tiêu/Thu nhập
 */
class QuickAddNotificationService : Service() {

    companion object {
        const val CHANNEL_ID = "quick_add_channel"
        const val NOTIFICATION_ID = 1001
        
        const val ACTION_ADD_EXPENSE = "com.finly.ACTION_ADD_EXPENSE"
        const val ACTION_ADD_INCOME = "com.finly.ACTION_ADD_INCOME"
        const val ACTION_STOP_SERVICE = "com.finly.ACTION_STOP_SERVICE"
        
        const val EXTRA_TRANSACTION_TYPE = "transaction_type"
        
        fun start(context: Context) {
            val intent = Intent(context, QuickAddNotificationService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        fun stop(context: Context) {
            val intent = Intent(context, QuickAddNotificationService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_SERVICE -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
        }
        
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
        
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Thêm giao dịch nhanh",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Hiển thị nút thêm nhanh chi tiêu và thu nhập"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        // Intent mở app với Chi tiêu
        val expenseIntent = Intent(this, MainActivity::class.java).apply {
            action = ACTION_ADD_EXPENSE
            putExtra(EXTRA_TRANSACTION_TYPE, "EXPENSE")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val expensePendingIntent = PendingIntent.getActivity(
            this, 1, expenseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent mở app với Thu nhập
        val incomeIntent = Intent(this, MainActivity::class.java).apply {
            action = ACTION_ADD_INCOME
            putExtra(EXTRA_TRANSACTION_TYPE, "INCOME")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val incomePendingIntent = PendingIntent.getActivity(
            this, 2, incomeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent mở app bình thường
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Sử dụng DecoratedCustomViewStyle hoặc BigTextStyle để hiển thị mở rộng
        val bigTextStyle = NotificationCompat.BigTextStyle()
            .setBigContentTitle("Finly - Thêm giao dịch nhanh")
            .bigText("Nhấn nút bên dưới để thêm chi tiêu hoặc thu nhập mới")

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Finly - Thêm giao dịch")
            .setContentText("💸 Chi tiêu  |  💰 Thu nhập")
            .setContentIntent(openAppPendingIntent)
            .setStyle(bigTextStyle) // Mở rộng mặc định
            .setPriority(NotificationCompat.PRIORITY_DEFAULT) // Priority cao hơn để hiển thị mở rộng
            .setOngoing(true)
            .setSilent(true)
            .setAutoCancel(false)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(
                android.R.drawable.ic_menu_add,
                "💸 Chi tiêu",
                expensePendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_add,
                "💰 Thu nhập",
                incomePendingIntent
            )
            .build()
        
        // Thêm flags để notification không thể bị clear
        notification.flags = notification.flags or 
            Notification.FLAG_NO_CLEAR or 
            Notification.FLAG_ONGOING_EVENT
        
        return notification
    }
}
