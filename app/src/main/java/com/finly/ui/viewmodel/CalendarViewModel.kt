package com.finly.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finly.data.local.SecurityPreferences
import com.finly.data.local.entity.Transaction
import com.finly.data.local.entity.TransactionType
import com.finly.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

/**
 * Dữ liệu tổng hợp cho một ngày
 */
data class DaySummary(
    val date: Calendar,
    val totalIncome: Long = 0,
    val totalExpense: Long = 0,
    val transactions: List<Transaction> = emptyList()
)

/**
 * UI State cho Calendar Screen
 */
data class CalendarUiState(
    val currentMonth: Calendar = Calendar.getInstance(),
    val selectedDate: Calendar? = null,
    val daySummaries: Map<Int, DaySummary> = emptyMap(), // key = day of month
    val selectedDayTransactions: List<Transaction> = emptyList(),
    val monthlyIncome: Long = 0,
    val monthlyExpense: Long = 0,
    val isLoading: Boolean = true,
    val isAmountHidden: Boolean = false
)

/**
 * ViewModel cho màn hình Calendar
 */
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val securityPreferences: SecurityPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadCurrentMonth()
        loadHideAmountPreference()
    }
    
    private fun loadHideAmountPreference() {
        _uiState.update {
            it.copy(isAmountHidden = securityPreferences.isAmountHidden())
        }
    }
    
    fun toggleHideAmount() {
        val newValue = !_uiState.value.isAmountHidden
        securityPreferences.setAmountHidden(newValue)
        _uiState.update { it.copy(isAmountHidden = newValue) }
    }

    /**
     * Load dữ liệu tháng hiện tại
     */
    private fun loadCurrentMonth() {
        val currentMonth = _uiState.value.currentMonth
        loadMonthData(currentMonth)
    }

    /**
     * Load dữ liệu cho một tháng
     */
    private fun loadMonthData(month: Calendar) {
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
                // Group theo ngày
                val grouped = transactions.groupBy { tx ->
                    val cal = Calendar.getInstance()
                    cal.timeInMillis = tx.timestamp
                    cal.get(Calendar.DAY_OF_MONTH)
                }
                
                val daySummaries = grouped.mapValues { (day, txList) ->
                    val cal = month.clone() as Calendar
                    cal.set(Calendar.DAY_OF_MONTH, day)
                    DaySummary(
                        date = cal,
                        totalIncome = txList.filter { it.type == TransactionType.INCOME }.sumOf { it.amount },
                        totalExpense = txList.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount },
                        transactions = txList
                    )
                }
                
                val monthlyIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                val monthlyExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                
                _uiState.update { currentState ->
                    // Nếu đang chọn ngày nào đó, cập nhật lại danh sách giao dịch của ngày đó
                    val updatedSelectedTransactions = if (currentState.selectedDate != null) {
                        val selectedDay = currentState.selectedDate.get(Calendar.DAY_OF_MONTH)
                        daySummaries[selectedDay]?.transactions ?: emptyList()
                    } else {
                        currentState.selectedDayTransactions
                    }

                    currentState.copy(
                        daySummaries = daySummaries,
                        selectedDayTransactions = updatedSelectedTransactions,
                        monthlyIncome = monthlyIncome,
                        monthlyExpense = monthlyExpense,
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Chuyển sang tháng trước
     */
    fun previousMonth() {
        val newMonth = _uiState.value.currentMonth.clone() as Calendar
        newMonth.add(Calendar.MONTH, -1)
        _uiState.update { it.copy(currentMonth = newMonth, selectedDate = null, selectedDayTransactions = emptyList()) }
        loadMonthData(newMonth)
    }

    /**
     * Chuyển sang tháng sau
     */
    fun nextMonth() {
        val newMonth = _uiState.value.currentMonth.clone() as Calendar
        newMonth.add(Calendar.MONTH, 1)
        _uiState.update { it.copy(currentMonth = newMonth, selectedDate = null, selectedDayTransactions = emptyList()) }
        loadMonthData(newMonth)
    }

    /**
     * Chọn một ngày
     */
    fun selectDate(day: Int) {
        val selectedCal = _uiState.value.currentMonth.clone() as Calendar
        selectedCal.set(Calendar.DAY_OF_MONTH, day)
        
        val dayData = _uiState.value.daySummaries[day]
        _uiState.update {
            it.copy(
                selectedDate = selectedCal,
                selectedDayTransactions = dayData?.transactions ?: emptyList()
            )
        }
    }

    /**
     * Bỏ chọn ngày
     */
    fun clearSelection() {
        _uiState.update { it.copy(selectedDate = null, selectedDayTransactions = emptyList()) }
    }

    /**
     * Lấy số ngày trong tháng hiện tại
     */
    fun getDaysInMonth(): Int {
        return _uiState.value.currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    /**
     * Lấy ngày đầu tuần của ngày đầu tiên trong tháng (0 = CN, 1 = T2, ...)
     */
    fun getFirstDayOfWeek(): Int {
        val cal = _uiState.value.currentMonth.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        return cal.get(Calendar.DAY_OF_WEEK) - 1 // 0-indexed
    }
}
