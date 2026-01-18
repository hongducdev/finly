package com.finly.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.finly.data.local.entity.Budget
import com.finly.data.local.entity.SavingsGoal
import com.finly.data.local.entity.Transaction
import com.finly.data.local.entity.CustomCategory
import com.finly.data.local.entity.Debt

/**
 * Room Database cho Finly
 * Lưu trữ giao dịch, ngân sách và mục tiêu tiết kiệm
 */
@Database(
    entities = [
        Transaction::class,
        Budget::class,
        SavingsGoal::class,
        CustomCategory::class,
        Debt::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun customCategoryDao(): CustomCategoryDao
    
    abstract fun debtDao(): DebtDao
    
    companion object {
        const val DATABASE_NAME = "finly_database"
    }
}
