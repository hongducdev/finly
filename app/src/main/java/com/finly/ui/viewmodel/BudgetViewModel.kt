package com.finly.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finly.data.local.entity.Budget
import com.finly.data.local.entity.TransactionCategory
import com.finly.data.local.entity.TransactionType
import com.finly.data.repository.BudgetRepository
import com.finly.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

/**
 * Thông tin ngân sách với số tiền đã chi
 */
data class BudgetWithSpent(
    val budget: Budget,
    val spent: Long,
    val percentage: Float
)

/**
 * UI State cho Budget Screen
 */
data class BudgetUiState(
    val currentMonth: Calendar = Calendar.getInstance(),
    val budgets: List<BudgetWithSpent> = emptyList(),
    val totalBudget: Long = 0,
    val totalSpent: Long = 0,
    val isLoading: Boolean = true,
    val showAddDialog: Boolean = false,
    val selectedCategory: TransactionCategory? = null,
    val inputAmount: String = ""
)

/**
 * ViewModel cho màn hình Ngân sách
 */
@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    init {
        loadBudgets()
    }

    /**
     * Load ngân sách và tính số tiền đã chi
     */
    private fun loadBudgets() {
        val month = _uiState.value.currentMonth
        val monthValue = month.get(Calendar.MONTH) + 1
        val yearValue = month.get(Calendar.YEAR)
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Lấy ngân sách của tháng
            budgetRepository.getBudgetsForMonth(monthValue, yearValue)
                .combine(getMonthlyTransactions()) { budgets, transactions ->
                    // Tính số tiền đã chi cho mỗi danh mục
                    budgets.map { budget ->
                        val spent = transactions
                            .filter { it.category == budget.category && it.type == TransactionType.EXPENSE }
                            .sumOf { it.amount }
                        BudgetWithSpent(
                            budget = budget,
                            spent = spent,
                            percentage = if (budget.amount > 0) (spent.toFloat() / budget.amount * 100).coerceAtMost(100f) else 0f
                        )
                    }
                }
                .collect { budgetsWithSpent ->
                    _uiState.update {
                        it.copy(
                            budgets = budgetsWithSpent,
                            totalBudget = budgetsWithSpent.sumOf { b -> b.budget.amount },
                            totalSpent = budgetsWithSpent.sumOf { b -> b.spent },
                            isLoading = false
                        )
                    }
                }
        }
    }

    /**
     * Lấy giao dịch trong tháng hiện tại
     */
    private fun getMonthlyTransactions(): Flow<List<com.finly.data.local.entity.Transaction>> {
        val month = _uiState.value.currentMonth
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
        
        return transactionRepository.getTransactionsBetween(
            startOfMonth.timeInMillis,
            endOfMonth.timeInMillis
        )
    }

    /**
     * Chuyển tháng trước
     */
    fun previousMonth() {
        val newMonth = _uiState.value.currentMonth.clone() as Calendar
        newMonth.add(Calendar.MONTH, -1)
        _uiState.update { it.copy(currentMonth = newMonth) }
        loadBudgets()
    }

    /**
     * Chuyển tháng sau
     */
    fun nextMonth() {
        val newMonth = _uiState.value.currentMonth.clone() as Calendar
        newMonth.add(Calendar.MONTH, 1)
        _uiState.update { it.copy(currentMonth = newMonth) }
        loadBudgets()
    }

    /**
     * Hiển thị dialog thêm ngân sách
     */
    fun showAddDialog() {
        _uiState.update { it.copy(showAddDialog = true, selectedCategory = null, inputAmount = "") }
    }

    /**
     * Ẩn dialog thêm ngân sách
     */
    fun hideAddDialog() {
        _uiState.update { it.copy(showAddDialog = false) }
    }

    /**
     * Chọn danh mục
     */
    fun selectCategory(category: TransactionCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    /**
     * Cập nhật số tiền
     */
    fun updateAmount(amount: String) {
        _uiState.update { it.copy(inputAmount = amount.filter { c -> c.isDigit() }) }
    }

    /**
     * Lưu ngân sách
     */
    fun saveBudget() {
        val category = _uiState.value.selectedCategory ?: return
        val amount = _uiState.value.inputAmount.toLongOrNull() ?: return
        if (amount <= 0) return
        
        val month = _uiState.value.currentMonth
        val budget = Budget(
            category = category,
            amount = amount * 1000, // nhân 1000 như số tiền
            month = month.get(Calendar.MONTH) + 1,
            year = month.get(Calendar.YEAR)
        )
        
        viewModelScope.launch {
            budgetRepository.insertBudget(budget)
            _uiState.update { it.copy(showAddDialog = false) }
            loadBudgets()
        }
    }

    /**
     * Xóa ngân sách
     */
    fun deleteBudget(budgetId: Long) {
        viewModelScope.launch {
            budgetRepository.deleteBudgetById(budgetId)
            loadBudgets()
        }
    }

    /**
     * Lấy danh sách danh mục chi tiêu chưa có ngân sách
     */
    fun getAvailableCategories(): List<TransactionCategory> {
        val existingCategories = _uiState.value.budgets.map { it.budget.category }
        return TransactionCategory.getByType(TransactionType.EXPENSE)
            .filter { it !in existingCategories }
    }
}
