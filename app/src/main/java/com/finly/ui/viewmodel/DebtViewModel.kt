package com.finly.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finly.data.local.entity.Debt
import com.finly.data.local.entity.DebtType
import com.finly.data.repository.DebtRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class DebtFilter {
    ALL, LEND, BORROW, UNPAID
}

data class DebtListUiState(
    val debts: List<Debt> = emptyList(),
    val filter: DebtFilter = DebtFilter.ALL,
    val isLoading: Boolean = false,
    val totalLent: Long = 0,
    val totalBorrowed: Long = 0,
    val unpaidLent: Long = 0,
    val unpaidBorrowed: Long = 0
)

/**
 * ViewModel cho Debt List Screen
 */
@HiltViewModel
class DebtViewModel @Inject constructor(
    private val debtRepository: DebtRepository
) : ViewModel() {
    
    private val _filter = MutableStateFlow(DebtFilter.ALL)
    
    private val _uiState = MutableStateFlow(DebtListUiState())
    val uiState: StateFlow<DebtListUiState> = _uiState.asStateFlow()
    
    init {
        loadDebts()
    }
    
    private fun loadDebts() {
        viewModelScope.launch {
            combine(
                debtRepository.getAllDebts(),
                _filter
            ) { allDebts, filter ->
                val filteredDebts = when (filter) {
                    DebtFilter.ALL -> allDebts
                    DebtFilter.LEND -> allDebts.filter { it.type == DebtType.LEND }
                    DebtFilter.BORROW -> allDebts.filter { it.type == DebtType.BORROW }
                    DebtFilter.UNPAID -> allDebts.filter { !it.isPaid }
                }
                
                val totalLent = allDebts.filter { it.type == DebtType.LEND }.sumOf { it.amount }
                val totalBorrowed = allDebts.filter { it.type == DebtType.BORROW }.sumOf { it.amount }
                val unpaidLent = allDebts.filter { it.type == DebtType.LEND && !it.isPaid }.sumOf { it.amount }
                val unpaidBorrowed = allDebts.filter { it.type == DebtType.BORROW && !it.isPaid }.sumOf { it.amount }
                
                DebtListUiState(
                    debts = filteredDebts,
                    filter = filter,
                    totalLent = totalLent,
                    totalBorrowed = totalBorrowed,
                    unpaidLent = unpaidLent,
                    unpaidBorrowed = unpaidBorrowed
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }
    
    fun setFilter(filter: DebtFilter) {
        _filter.value = filter
    }
    
    fun deleteDebt(debt: Debt) {
        viewModelScope.launch {
            debtRepository.deleteDebt(debt)
        }
    }
    
    fun togglePaidStatus(debt: Debt) {
        viewModelScope.launch {
            if (debt.isPaid) {
                debtRepository.markAsUnpaid(debt.id)
            } else {
                debtRepository.markAsPaid(debt.id)
            }
        }
    }
}
