package com.finly.service

import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.finly.data.local.entity.Transaction
import com.finly.data.local.entity.TransactionSource
import com.finly.data.repository.TransactionRepository
import com.finly.parser.ParserFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.security.MessageDigest
import javax.inject.Inject

/**
 * Service lắng nghe thông báo từ các ứng dụng ngân hàng/ví điện tử
 * 
 * Tuân thủ Google Play Policy:
 * - KHÔNG đọc SMS
 * - KHÔNG sử dụng Accessibility Service
 * - CHỈ đọc thông báo từ các app được hỗ trợ
 * - Dữ liệu chỉ lưu local, không gửi lên server
 */
@AndroidEntryPoint
class TransactionNotificationService : NotificationListenerService() {

    companion object {
        private const val TAG = "FinlyNotification"
    }

    @Inject
    lateinit var parserFactory: ParserFactory

    @Inject
    lateinit var transactionRepository: TransactionRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "NotificationListener connected")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(TAG, "NotificationListener disconnected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return

        val packageName = sbn.packageName ?: return

        // Chỉ xử lý các app được hỗ trợ
        if (!parserFactory.isSupported(packageName)) {
            return
        }

        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        // Lấy nội dung thông báo
        val title = extras.getCharSequence("android.title")?.toString()
        val content = extras.getCharSequence("android.text")?.toString()

        if (content.isNullOrBlank()) {
            return
        }

        Log.d(TAG, "Received notification from $packageName: $title - $content")

        // Lấy parser phù hợp
        val parser = parserFactory.getParser(packageName) ?: return

        // Kiểm tra có phải thông báo giao dịch không
        if (!parser.isTransactionNotification(title, content)) {
            Log.d(TAG, "Skipped non-transaction notification")
            return
        }

        // Parse thông báo
        val parsedTransaction = parser.parse(title, content)
        if (parsedTransaction == null) {
            Log.w(TAG, "Failed to parse transaction: $content")
            return
        }

        // Tạo hash để tránh trùng lặp
        val rawText = "$title - $content"
        val hash = generateHash(rawText)

        // Lưu giao dịch
        serviceScope.launch {
            try {
                // Kiểm tra đã tồn tại chưa
                if (transactionRepository.existsByHash(hash)) {
                    Log.d(TAG, "Transaction already exists, skipping")
                    return@launch
                }

                val transaction = Transaction(
                    source = parser.source,
                    type = parsedTransaction.type,
                    amount = parsedTransaction.amount,
                    balance = parsedTransaction.balance,
                    timestamp = sbn.postTime,
                    rawText = rawText,
                    rawTextHash = hash,
                    description = parsedTransaction.description
                )

                val id = transactionRepository.insertTransaction(transaction)
                if (id > 0) {
                    Log.d(TAG, "Transaction saved: ${parsedTransaction.type} ${parsedTransaction.amount} VND from ${parser.source}")
                    com.finly.widget.FinlyWidgetProvider.sendRefreshBroadcast(this@TransactionNotificationService)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving transaction", e)
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // Không cần xử lý khi thông báo bị xóa
    }

    /**
     * Tạo hash MD5 từ chuỗi
     */
    private fun generateHash(text: String): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(text.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
