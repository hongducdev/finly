package com.finly.data.local

import androidx.room.*
import com.finly.data.local.entity.Transaction
import com.finly.data.local.entity.TransactionSource
import com.finly.data.local.entity.TransactionType
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object cho Transaction
 * Cung cấp các phương thức CRUD và truy vấn thống kê
 */
@Dao
interface TransactionDao {
    
    // ==================== INSERT ====================
    
    /**
     * Thêm giao dịch mới
     * Nếu rawTextHash đã tồn tại thì bỏ qua (tránh trùng lặp)
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(transaction: Transaction): Long
    
    /**
     * Thêm nhiều giao dịch
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(transactions: List<Transaction>): List<Long>
    
    // ==================== QUERY ====================
    
    /**
     * Lấy tất cả giao dịch, sắp xếp theo thời gian mới nhất
     */
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<Transaction>>
    
    /**
     * Lấy tất cả giao dịch (sync - cho export)
     */
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    suspend fun getAllTransactionsSync(): List<Transaction>
    
    /**
     * Lấy giao dịch theo ID
     */
    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): Transaction?
    
    /**
     * Lấy giao dịch trong khoảng thời gian
     */
    @Query("SELECT * FROM transactions WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getTransactionsBetween(startTime: Long, endTime: Long): Flow<List<Transaction>>

    /**
     * Lấy giao dịch trong khoảng thời gian (Sync)
     */
    @Query("SELECT * FROM transactions WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    suspend fun getTransactionsBetweenSync(startTime: Long, endTime: Long): List<Transaction>
    
    /**
     * Lấy giao dịch theo nguồn
     */
    @Query("SELECT * FROM transactions WHERE source = :source ORDER BY timestamp DESC")
    fun getTransactionsBySource(source: TransactionSource): Flow<List<Transaction>>
    
    /**
     * Lấy N giao dịch gần đây nhất
     */
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int): Flow<List<Transaction>>
    
    // ==================== STATISTICS ====================
    
    /**
     * Tổng chi tiêu trong khoảng thời gian
     */
    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE type = :type AND timestamp BETWEEN :startTime AND :endTime
    """)
    fun getTotalAmountBetween(type: TransactionType, startTime: Long, endTime: Long): Flow<Long>
    
    /**
     * Tổng chi tiêu hôm nay
     */
    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE type = 'EXPENSE' AND timestamp >= :startOfDay
    """)
    fun getTodayExpense(startOfDay: Long): Flow<Long>
    
    /**
     * Tổng thu nhập hôm nay
     */
    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE type = 'INCOME' AND timestamp >= :startOfDay
    """)
    fun getTodayIncome(startOfDay: Long): Flow<Long>
    
    /**
     * Tổng chi tiêu tháng này
     */
    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE type = 'EXPENSE' AND timestamp >= :startOfMonth
    """)
    fun getMonthExpense(startOfMonth: Long): Flow<Long>
    
    /**
     * Tổng thu nhập tháng này
     */
    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE type = 'INCOME' AND timestamp >= :startOfMonth
    """)
    fun getMonthIncome(startOfMonth: Long): Flow<Long>
    
    /**
     * Đếm số giao dịch
     */
    @Query("SELECT COUNT(*) FROM transactions")
    fun getTransactionCount(): Flow<Int>
    
    /**
     * Chi tiêu theo ngày trong tháng (cho biểu đồ)
     */
    @Query("""
        SELECT SUM(amount) as total, 
               strftime('%Y-%m-%d', timestamp/1000, 'unixepoch', 'localtime') as date
        FROM transactions 
        WHERE type = 'EXPENSE' AND timestamp >= :startOfMonth
        GROUP BY date
        ORDER BY date ASC
    """)
    fun getDailyExpenseForMonth(startOfMonth: Long): Flow<List<DailyExpense>>
    
    // ==================== DELETE ====================
    
    /**
     * Xóa giao dịch theo ID
     */
    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    /**
     * Xóa tất cả giao dịch
     */
    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
    
    /**
     * Kiểm tra giao dịch đã tồn tại chưa (theo hash)
     */
    @Query("SELECT EXISTS(SELECT 1 FROM transactions WHERE rawTextHash = :hash)")
    suspend fun existsByHash(hash: String): Boolean
}

/**
 * Data class cho kết quả truy vấn chi tiêu theo ngày
 */
data class DailyExpense(
    val total: Long,
    val date: String
)
