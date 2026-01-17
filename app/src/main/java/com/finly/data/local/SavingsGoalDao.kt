package com.finly.data.local

import androidx.room.*
import com.finly.data.local.entity.SavingsGoal
import kotlinx.coroutines.flow.Flow

/**
 * DAO cho SavingsGoal
 */
@Dao
interface SavingsGoalDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: SavingsGoal): Long
    
    @Update
    suspend fun update(goal: SavingsGoal)
    
    @Delete
    suspend fun delete(goal: SavingsGoal)
    
    @Query("SELECT * FROM savings_goals WHERE isCompleted = 0 ORDER BY createdAt DESC")
    fun getActiveGoals(): Flow<List<SavingsGoal>>
    
    @Query("SELECT * FROM savings_goals WHERE isCompleted = 1 ORDER BY createdAt DESC")
    fun getCompletedGoals(): Flow<List<SavingsGoal>>
    
    @Query("SELECT * FROM savings_goals ORDER BY createdAt DESC")
    fun getAllGoals(): Flow<List<SavingsGoal>>
    
    @Query("SELECT * FROM savings_goals WHERE id = :id")
    suspend fun getGoalById(id: Long): SavingsGoal?
    
    @Query("DELETE FROM savings_goals WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    @Query("UPDATE savings_goals SET currentAmount = :amount WHERE id = :id")
    suspend fun updateCurrentAmount(id: Long, amount: Long)
    
    @Query("UPDATE savings_goals SET isCompleted = 1 WHERE id = :id")
    suspend fun markAsCompleted(id: Long)
}
