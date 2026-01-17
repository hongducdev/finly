package com.finly.data.local

import androidx.room.*
import com.finly.data.local.entity.Budget
import com.finly.data.local.entity.TransactionCategory
import kotlinx.coroutines.flow.Flow

/**
 * DAO cho Budget
 */
@Dao
interface BudgetDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: Budget): Long
    
    @Update
    suspend fun update(budget: Budget)
    
    @Delete
    suspend fun delete(budget: Budget)
    
    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year ORDER BY category")
    fun getBudgetsForMonth(month: Int, year: Int): Flow<List<Budget>>
    
    @Query("SELECT * FROM budgets WHERE category = :category AND month = :month AND year = :year LIMIT 1")
    suspend fun getBudgetForCategory(category: TransactionCategory, month: Int, year: Int): Budget?
    
    @Query("SELECT * FROM budgets WHERE id = :id")
    suspend fun getBudgetById(id: Long): Budget?
    
    @Query("DELETE FROM budgets WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    @Query("SELECT * FROM budgets ORDER BY year DESC, month DESC")
    fun getAllBudgets(): Flow<List<Budget>>
}
