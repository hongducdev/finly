package com.finly.data.repository

import com.finly.data.local.BudgetDao
import com.finly.data.local.entity.Budget
import com.finly.data.local.entity.TransactionCategory
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository cho Budget
 */
@Singleton
class BudgetRepository @Inject constructor(
    private val budgetDao: BudgetDao
) {
    
    fun getBudgetsForMonth(month: Int, year: Int): Flow<List<Budget>> =
        budgetDao.getBudgetsForMonth(month, year)
    
    fun getAllBudgets(): Flow<List<Budget>> =
        budgetDao.getAllBudgets()
    
    suspend fun getBudgetForCategory(
        category: TransactionCategory, 
        month: Int, 
        year: Int
    ): Budget? = budgetDao.getBudgetForCategory(category, month, year)
    
    suspend fun getBudgetById(id: Long): Budget? =
        budgetDao.getBudgetById(id)
    
    suspend fun insertBudget(budget: Budget): Long =
        budgetDao.insert(budget)
    
    suspend fun updateBudget(budget: Budget) =
        budgetDao.update(budget)
    
    suspend fun deleteBudget(budget: Budget) =
        budgetDao.delete(budget)
    
    suspend fun deleteBudgetById(id: Long) =
        budgetDao.deleteById(id)
}
