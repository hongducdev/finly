package com.finly.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finly.data.local.DailyExpense
import com.finly.data.local.entity.Transaction
import com.finly.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject

/**
 * UI State cho Dashboard
 */
data class DashboardUiState(
    val todayExpense: Long = 0,
    val todayIncome: Long = 0,
    val monthExpense: Long = 0,
    val monthIncome: Long = 0,
    val recentTransactions: List<Transaction> = emptyList(),
    val dailyExpenses: List<DailyExpense> = emptyList(),
    val transactionCount: Int = 0,
    val isLoading: Boolean = true
)

/**
 * ViewModel cho Dashboard screen
 * Quản lý state và business logic
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    /**
     * Tải dữ liệu dashboard
     */
    private fun loadDashboardData() {
        // Combine tất cả flows để cập nhật UI state
        viewModelScope.launch {
            combine(
                transactionRepository.getTodayExpense(),
                transactionRepository.getTodayIncome(),
                transactionRepository.getMonthExpense(),
                transactionRepository.getMonthIncome(),
                transactionRepository.getRecentTransactions(20),
                transactionRepository.getDailyExpenseForMonth(),
                transactionRepository.getTransactionCount()
            ) { values ->
                DashboardUiState(
                    todayExpense = values[0] as Long,
                    todayIncome = values[1] as Long,
                    monthExpense = values[2] as Long,
                    monthIncome = values[3] as Long,
                    recentTransactions = @Suppress("UNCHECKED_CAST") (values[4] as List<Transaction>),
                    dailyExpenses = @Suppress("UNCHECKED_CAST") (values[5] as List<DailyExpense>),
                    transactionCount = values[6] as Int,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    /**
     * Xóa giao dịch
     */
    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            transactionRepository.deleteTransaction(id)
        }
    }

    /**
     * Xóa tất cả giao dịch
     */
    fun deleteAllTransactions() {
        viewModelScope.launch {
            transactionRepository.deleteAllTransactions()
        }
    }

    companion object {
        private val currencyFormat = NumberFormat.getNumberInstance(Locale("vi", "VN"))

        /**
         * Format số tiền sang định dạng VND
         */
        fun formatCurrency(amount: Long): String {
            return "${currencyFormat.format(amount)} ₫"
        }
    }
}
