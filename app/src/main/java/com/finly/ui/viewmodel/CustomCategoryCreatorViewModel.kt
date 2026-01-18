package com.finly.ui.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finly.data.local.entity.CustomCategory
import com.finly.data.local.entity.TransactionType
import com.finly.data.repository.CustomCategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CustomCategoryCreatorUiState(
    val categoryName: String = "",
    val selectedIcon: ImageVector? = null,
    val selectedIconName: String = "",
    val selectedColor: Color = Color(0xFFFFAB91),
    val isLoading: Boolean = false,
    val createdCategoryId: Long? = null
)

@HiltViewModel
class CustomCategoryCreatorViewModel @Inject constructor(
    private val customCategoryRepository: CustomCategoryRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CustomCategoryCreatorUiState())
    val uiState: StateFlow<CustomCategoryCreatorUiState> = _uiState.asStateFlow()
    
    private var transactionType: TransactionType = TransactionType.EXPENSE
    
    fun setTransactionType(type: TransactionType) {
        transactionType = type
    }
    
    fun updateName(name: String) {
        _uiState.update { it.copy(categoryName = name) }
    }
    
    fun selectIcon(icon: ImageVector, iconName: String) {
        _uiState.update { 
            it.copy(
                selectedIcon = icon,
                selectedIconName = iconName
            ) 
        }
    }
    
    fun selectColor(color: Color) {
        _uiState.update { it.copy(selectedColor = color) }
    }
    
    fun createCategory() {
        val state = _uiState.value
        if (state.categoryName.isBlank() || state.selectedIcon == null) {
            return
        }
        
        _uiState.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            try {
                val colorHex = String.format(
                    "#%02X%02X%02X",
                    (state.selectedColor.red * 255).toInt(),
                    (state.selectedColor.green * 255).toInt(),
                    (state.selectedColor.blue * 255).toInt()
                )
                
                val customCategory = CustomCategory(
                    name = state.categoryName,
                    iconName = state.selectedIconName,
                    type = transactionType,
                    color = colorHex
                )
                
                val id = customCategoryRepository.insertCustomCategory(customCategory)
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        createdCategoryId = id
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
