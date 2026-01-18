package com.finly.data.repository

import com.finly.data.local.DebtDao
import com.finly.data.local.entity.Debt
import com.finly.data.local.entity.DebtType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository cho Debt
 */
@Singleton
class DebtRepository @Inject constructor(
    private val debtDao: DebtDao
) {
    
    fun getAllDebts(): Flow<List<Debt>> = debtDao.getAllDebts()
    
    fun getDebtsByType(type: DebtType): Flow<List<Debt>> = debtDao.getDebtsByType(type)
    
    fun getUnpaidDebts(): Flow<List<Debt>> = debtDao.getUnpaidDebts()
    
    suspend fun getDebtById(id: Long): Debt? = debtDao.getDebtById(id)
    
    suspend fun insertDebt(debt: Debt): Long = debtDao.insertDebt(debt)
    
    suspend fun updateDebt(debt: Debt) = debtDao.updateDebt(debt)
    
    suspend fun deleteDebt(debt: Debt) = debtDao.deleteDebt(debt)
    
    suspend fun markAsPaid(debtId: Long, paidDate: Long = System.currentTimeMillis()) {
        debtDao.markAsPaid(debtId, paidDate)
    }
    
    suspend fun markAsUnpaid(debtId: Long) {
        debtDao.markAsUnpaid(debtId)
    }
}
