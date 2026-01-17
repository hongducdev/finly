package com.finly.data.repository

import com.finly.data.local.SavingsGoalDao
import com.finly.data.local.entity.SavingsGoal
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository cho SavingsGoal
 */
@Singleton
class SavingsGoalRepository @Inject constructor(
    private val savingsGoalDao: SavingsGoalDao
) {
    
    fun getActiveGoals(): Flow<List<SavingsGoal>> =
        savingsGoalDao.getActiveGoals()
    
    fun getCompletedGoals(): Flow<List<SavingsGoal>> =
        savingsGoalDao.getCompletedGoals()
    
    fun getAllGoals(): Flow<List<SavingsGoal>> =
        savingsGoalDao.getAllGoals()
    
    suspend fun getGoalById(id: Long): SavingsGoal? =
        savingsGoalDao.getGoalById(id)
    
    suspend fun insertGoal(goal: SavingsGoal): Long =
        savingsGoalDao.insert(goal)
    
    suspend fun updateGoal(goal: SavingsGoal) =
        savingsGoalDao.update(goal)
    
    suspend fun deleteGoal(goal: SavingsGoal) =
        savingsGoalDao.delete(goal)
    
    suspend fun deleteGoalById(id: Long) =
        savingsGoalDao.deleteById(id)
    
    suspend fun updateCurrentAmount(id: Long, amount: Long) =
        savingsGoalDao.updateCurrentAmount(id, amount)
    
    suspend fun markAsCompleted(id: Long) =
        savingsGoalDao.markAsCompleted(id)
}
