package com.finly.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finly.data.local.entity.SavingsGoal
import com.finly.data.repository.SavingsGoalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State cho SavingsGoal Screen
 */
data class SavingsGoalUiState(
    val activeGoals: List<SavingsGoal> = emptyList(),
    val completedGoals: List<SavingsGoal> = emptyList(),
    val isLoading: Boolean = true,
    val showAddDialog: Boolean = false,
    val showAddMoneyDialog: Boolean = false,
    val selectedGoalId: Long? = null,
    val inputName: String = "",
    val inputTargetAmount: String = "",
    val inputIcon: String = "🎯",
    val inputAddAmount: String = ""
)

/**
 * ViewModel cho màn hình Mục tiêu tiết kiệm
 */
@HiltViewModel
class SavingsGoalViewModel @Inject constructor(
    private val savingsGoalRepository: SavingsGoalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SavingsGoalUiState())
    val uiState: StateFlow<SavingsGoalUiState> = _uiState.asStateFlow()

    private val availableIcons = listOf("🎯", "🏠", "🚗", "✈️", "📱", "💻", "🎮", "👗", "💍", "🎓", "💰", "🎁")
    
    init {
        loadGoals()
    }

    fun getAvailableIcons(): List<String> = availableIcons

    /**
     * Load mục tiêu tiết kiệm
     */
    private fun loadGoals() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            savingsGoalRepository.getActiveGoals().combine(
                savingsGoalRepository.getCompletedGoals()
            ) { active, completed ->
                Pair(active, completed)
            }.collect { (active, completed) ->
                _uiState.update {
                    it.copy(
                        activeGoals = active,
                        completedGoals = completed,
                        isLoading = false
                    )
                }
            }
        }
    }

    // Dialog thêm mục tiêu
    fun showAddDialog() {
        _uiState.update { 
            it.copy(
                showAddDialog = true, 
                inputName = "", 
                inputTargetAmount = "",
                inputIcon = "🎯"
            ) 
        }
    }

    fun hideAddDialog() {
        _uiState.update { it.copy(showAddDialog = false) }
    }

    fun updateInputName(name: String) {
        _uiState.update { it.copy(inputName = name) }
    }

    fun updateInputTargetAmount(amount: String) {
        _uiState.update { it.copy(inputTargetAmount = amount.filter { c -> c.isDigit() }) }
    }

    fun updateInputIcon(icon: String) {
        _uiState.update { it.copy(inputIcon = icon) }
    }

    fun saveGoal() {
        val name = _uiState.value.inputName.trim()
        val targetAmount = _uiState.value.inputTargetAmount.toLongOrNull() ?: return
        if (name.isBlank() || targetAmount <= 0) return
        
        val goal = SavingsGoal(
            name = name,
            targetAmount = targetAmount * 1000, // nhân 1000
            icon = _uiState.value.inputIcon
        )
        
        viewModelScope.launch {
            savingsGoalRepository.insertGoal(goal)
            _uiState.update { it.copy(showAddDialog = false) }
        }
    }

    // Dialog thêm tiền
    fun showAddMoneyDialog(goalId: Long) {
        _uiState.update { 
            it.copy(
                showAddMoneyDialog = true, 
                selectedGoalId = goalId,
                inputAddAmount = ""
            ) 
        }
    }

    fun hideAddMoneyDialog() {
        _uiState.update { it.copy(showAddMoneyDialog = false, selectedGoalId = null) }
    }

    fun updateInputAddAmount(amount: String) {
        _uiState.update { it.copy(inputAddAmount = amount.filter { c -> c.isDigit() }) }
    }

    fun addMoney() {
        val goalId = _uiState.value.selectedGoalId ?: return
        val addAmount = _uiState.value.inputAddAmount.toLongOrNull() ?: return
        if (addAmount <= 0) return
        
        viewModelScope.launch {
            val goal = savingsGoalRepository.getGoalById(goalId) ?: return@launch
            val newAmount = goal.currentAmount + (addAmount * 1000)
            savingsGoalRepository.updateCurrentAmount(goalId, newAmount)
            
            // Check if completed
            if (newAmount >= goal.targetAmount) {
                savingsGoalRepository.markAsCompleted(goalId)
            }
            
            _uiState.update { it.copy(showAddMoneyDialog = false, selectedGoalId = null) }
        }
    }

    /**
     * Xóa mục tiêu
     */
    fun deleteGoal(goalId: Long) {
        viewModelScope.launch {
            savingsGoalRepository.deleteGoalById(goalId)
        }
    }
}
