package com.finly.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finly.data.local.entity.Debt
import com.finly.data.local.entity.DebtType
import com.finly.data.repository.DebtRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditDebtUiState(
   val debtType: DebtType = DebtType.LEND,
    val personName: String = "",
    val amount: String = "",
    val description: String = "",
    val dueDate: Long = System.currentTimeMillis(),
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AddEditDebtViewModel @Inject constructor(
    private val debtRepository: DebtRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val debtId: Long? = savedStateHandle.get<String>("debtId")?.toLongOrNull()
    
    private val _uiState = MutableStateFlow(AddEditDebtUiState())
    val uiState: StateFlow<AddEditDebtUiState> = _uiState.asStateFlow()
    
    init {
        debtId?.let { loadDebt(it) }
    }
    
    private fun loadDebt(id: Long) {
        viewModelScope.launch {
            debtRepository.getDebtById(id)?.let { debt ->
                _uiState.update {
                    it.copy(
                        debtType = debt.type,
                        personName = debt.personName,
                        amount = (debt.amount / 1000).toString(),
                        description = debt.description ?: "",
                        dueDate = debt.dueDate
                    )
                }
            }
        }
    }
    
    fun setDebtType(type: DebtType) {
        _uiState.update { it.copy(debtType = type) }
    }
    
    fun setPersonName(name: String) {
        _uiState.update { it.copy(personName = name, errorMessage = null) }
    }
    
    fun setAmount(amount: String) {
        if (amount.isEmpty() || amount.all { it.isDigit() }) {
            _uiState.update { it.copy(amount = amount, errorMessage = null) }
        }
    }
    
    fun setDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }
    
    fun setDueDate(date: Long) {
        _uiState.update { it.copy(dueDate = date) }
    }
    
    fun saveDebt() {
        val state = _uiState.value
        
        if (state.personName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Vui lòng nhập tên người") }
            return
        }
        
        val amountValue = state.amount.toLongOrNull()
        if (amountValue == null || amountValue <= 0) {
            _uiState.update { it.copy(errorMessage = "Vui lòng nhập số tiền hợp lệ") }
            return
        }
        
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        
        viewModelScope.launch {
            try {
                val debt = Debt(
                    id = debtId ?: 0,
                    type = state.debtType,
                    personName = state.personName,
                    amount = amountValue * 1000,
                    description = state.description.ifBlank { null },
                    dueDate = state.dueDate
                )
                
                if (debtId != null) {
                    debtRepository.updateDebt(debt)
                } else {
                    debtRepository.insertDebt(debt)
                }
                
                _uiState.update { it.copy(isLoading = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Lỗi: ${e.message}"
                    )
                }
            }
        }
    }
}
