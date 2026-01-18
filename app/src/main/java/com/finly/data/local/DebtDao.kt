package com.finly.data.local

import androidx.room.*
import com.finly.data.local.entity.Debt
import com.finly.data.local.entity.DebtType
import kotlinx.coroutines.flow.Flow

/**
 * DAO cho Debt
 */
@Dao
interface DebtDao {
    
    @Query("SELECT * FROM debts ORDER BY dueDate ASC")
    fun getAllDebts(): Flow<List<Debt>>
    
    @Query("SELECT * FROM debts WHERE type = :type ORDER BY dueDate ASC")
    fun getDebtsByType(type: DebtType): Flow<List<Debt>>
    
    @Query("SELECT * FROM debts WHERE isPaid = 0 ORDER BY dueDate ASC")
    fun getUnpaidDebts(): Flow<List<Debt>>
    
    @Query("SELECT * FROM debts WHERE id = :id")
    suspend fun getDebtById(id: Long): Debt?
    
    @Insert
    suspend fun insertDebt(debt: Debt): Long
    
    @Update
    suspend fun updateDebt(debt: Debt)
    
    @Delete
    suspend fun deleteDebt(debt: Debt)
    
    @Query("UPDATE debts SET isPaid = 1, paidDate = :paidDate WHERE id = :debtId")
    suspend fun markAsPaid(debtId: Long, paidDate: Long)
    
    @Query("UPDATE debts SET isPaid = 0, paidDate = NULL WHERE id = :debtId")
    suspend fun markAsUnpaid(debtId: Long)
}
