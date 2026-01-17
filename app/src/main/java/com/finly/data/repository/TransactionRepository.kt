package com.finly.data.repository

import com.finly.data.local.DailyExpense
import com.finly.data.local.TransactionDao
import com.finly.data.local.entity.Transaction
import com.finly.data.local.entity.TransactionSource
import com.finly.data.local.entity.TransactionType
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository cho Transaction
 * Cung cấp abstraction layer giữa ViewModel và DAO
 */
@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao
) {
    
    // ==================== Lấy dữ liệu ====================
    
    /**
     * Lấy tất cả giao dịch
     */
    fun getAllTransactions(): Flow<List<Transaction>> = transactionDao.getAllTransactions()
    
    /**
     * Lấy tất cả giao dịch (sync - cho export)
     */
    suspend fun getAllTransactionsSync(): List<Transaction> = transactionDao.getAllTransactionsSync()
    
    /**
     * Lấy N giao dịch gần đây
     */
    fun getRecentTransactions(limit: Int = 20): Flow<List<Transaction>> = 
        transactionDao.getRecentTransactions(limit)
    
    /**
     * Lấy giao dịch theo nguồn
     */
    fun getTransactionsBySource(source: TransactionSource): Flow<List<Transaction>> =
        transactionDao.getTransactionsBySource(source)
    
    /**
     * Lấy giao dịch theo ID
     */
    suspend fun getTransactionById(id: Long): Transaction? = 
        transactionDao.getTransactionById(id)
    
    /**
     * Lấy giao dịch trong khoảng thời gian
     */
    fun getTransactionsBetween(startTime: Long, endTime: Long): Flow<List<Transaction>> =
        transactionDao.getTransactionsBetween(startTime, endTime)

    /**
     * Lấy giao dịch trong khoảng thời gian (Sync)
     */
    suspend fun getTransactionsBetweenSync(startTime: Long, endTime: Long): List<Transaction> =
        transactionDao.getTransactionsBetweenSync(startTime, endTime)
    
    // ==================== Thống kê ====================
    
    /**
     * Tổng chi tiêu hôm nay
     */
    fun getTodayExpense(): Flow<Long> = transactionDao.getTodayExpense(getStartOfDay())
    
    /**
     * Tổng thu nhập hôm nay
     */
    fun getTodayIncome(): Flow<Long> = transactionDao.getTodayIncome(getStartOfDay())
    
    /**
     * Tổng chi tiêu tháng này
     */
    fun getMonthExpense(): Flow<Long> = transactionDao.getMonthExpense(getStartOfMonth())
    
    /**
     * Tổng thu nhập tháng này
     */
    fun getMonthIncome(): Flow<Long> = transactionDao.getMonthIncome(getStartOfMonth())
    
    /**
     * Chi tiêu theo ngày trong tháng (cho biểu đồ)
     */
    fun getDailyExpenseForMonth(): Flow<List<DailyExpense>> = 
        transactionDao.getDailyExpenseForMonth(getStartOfMonth())
    
    /**
     * Đếm số giao dịch
     */
    fun getTransactionCount(): Flow<Int> = transactionDao.getTransactionCount()
    
    // ==================== Thêm giao dịch ====================
    
    /**
     * Thêm giao dịch mới
     * @return ID của giao dịch nếu thêm thành công, -1 nếu đã tồn tại
     */
    suspend fun insertTransaction(transaction: Transaction): Long {
        return transactionDao.insert(transaction)
    }

    /**
     * Cập nhật giao dịch
     */
    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.update(transaction)
    }
    
    /**
     * Kiểm tra giao dịch đã tồn tại chưa
     */
    suspend fun existsByHash(hash: String): Boolean = transactionDao.existsByHash(hash)
    
    // ==================== Xóa ====================
    
    /**
     * Xóa giao dịch theo ID
     */
    suspend fun deleteTransaction(id: Long) = transactionDao.deleteById(id)
    
    /**
     * Xóa tất cả giao dịch
     */
    suspend fun deleteAllTransactions() = transactionDao.deleteAll()
    
    // ==================== Helper functions ====================
    
    /**
     * Lấy timestamp đầu ngày hôm nay
     */
    private fun getStartOfDay(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    
    /**
     * Lấy timestamp đầu tháng này
     */
    private fun getStartOfMonth(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
