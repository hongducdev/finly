package com.finly.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finly.data.local.entity.Transaction
import com.finly.data.local.entity.TransactionCategory
import com.finly.data.local.entity.TransactionType
import com.finly.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

/**
 * Dữ liệu thống kê cho một danh mục
 */
data class CategoryStat(
    val category: TransactionCategory,
    val amount: Long,
    val percentage: Float,
    val count: Int
)

/**
 * UI State cho Statistics Screen
 */
data class StatisticsUiState(
    val currentMonth: Calendar = Calendar.getInstance(),
    val selectedType: TransactionType = TransactionType.EXPENSE,
    val categoryStats: List<CategoryStat> = emptyList(),
    val totalAmount: Long = 0,
    val isLoading: Boolean = true
)

/**
 * ViewModel cho màn hình thống kê
 */
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadStatistics()
    }

    /**
     * Load thống kê theo tháng hiện tại
     */
    private fun loadStatistics() {
        val month = _uiState.value.currentMonth
        val type = _uiState.value.selectedType
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val startOfMonth = month.clone() as Calendar
            startOfMonth.set(Calendar.DAY_OF_MONTH, 1)
            startOfMonth.set(Calendar.HOUR_OF_DAY, 0)
            startOfMonth.set(Calendar.MINUTE, 0)
            startOfMonth.set(Calendar.SECOND, 0)
            startOfMonth.set(Calendar.MILLISECOND, 0)
            
            val endOfMonth = month.clone() as Calendar
            endOfMonth.set(Calendar.DAY_OF_MONTH, endOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH))
            endOfMonth.set(Calendar.HOUR_OF_DAY, 23)
            endOfMonth.set(Calendar.MINUTE, 59)
            endOfMonth.set(Calendar.SECOND, 59)
            endOfMonth.set(Calendar.MILLISECOND, 999)
            
            transactionRepository.getTransactionsBetween(
                startOfMonth.timeInMillis,
                endOfMonth.timeInMillis
            ).collect { transactions ->
                // Lọc theo loại giao dịch
                val filtered = transactions.filter { it.type == type && it.category != null }
                
                // Group theo danh mục
                val grouped = filtered.groupBy { it.category!! }
                
                // Tính tổng
                val total = filtered.sumOf { it.amount }
                
                // Tính thống kê cho từng danh mục
                val stats = grouped.map { (category, txList) ->
                    val amount = txList.sumOf { it.amount }
                    CategoryStat(
                        category = category,
                        amount = amount,
                        percentage = if (total > 0) (amount.toFloat() / total * 100) else 0f,
                        count = txList.size
                    )
                }.sortedByDescending { it.amount }
                
                _uiState.update {
                    it.copy(
                        categoryStats = stats,
                        totalAmount = total,
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Đổi loại giao dịch (Chi tiêu / Thu nhập)
     */
    fun switchType(type: TransactionType) {
        _uiState.update { it.copy(selectedType = type) }
        loadStatistics()
    }

    /**
     * Chuyển sang tháng trước
     */
    fun previousMonth() {
        val newMonth = _uiState.value.currentMonth.clone() as Calendar
        newMonth.add(Calendar.MONTH, -1)
        _uiState.update { it.copy(currentMonth = newMonth) }
        loadStatistics()
    }

    /**
     * Chuyển sang tháng sau
     */
    fun nextMonth() {
        val newMonth = _uiState.value.currentMonth.clone() as Calendar
        newMonth.add(Calendar.MONTH, 1)
        _uiState.update { it.copy(currentMonth = newMonth) }
        loadStatistics()
    }
}
