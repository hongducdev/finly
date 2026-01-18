package com.finly.ui.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finly.data.local.entity.Transaction
import com.finly.data.local.entity.TransactionCategory
import com.finly.data.local.entity.TransactionSource
import com.finly.data.local.entity.TransactionType
import com.finly.data.local.entity.getColor
import com.finly.data.local.entity.getIcon
import com.finly.data.repository.CustomCategoryRepository
import com.finly.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class AmountDescriptionUiState(
    val amount: String = "",
    val description: String = "",
    val selectedCategoryName: String? = null,
    val categoryColor: Color = Color.Gray,
    val categoryIcon: ImageVector? = null,
    val customIconName: String? = null,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val isEditMode: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AmountDescriptionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val customCategoryRepository: CustomCategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AmountDescriptionUiState())
    val uiState: StateFlow<AmountDescriptionUiState> = _uiState.asStateFlow()

    private var isCustomCategory: Boolean = false
    private var categoryId: String = ""
    private var transactionType: TransactionType = TransactionType.EXPENSE
    private var timestamp: Long = System.currentTimeMillis()
    private var transactionId: Long = 0L

    fun initialize(
        isCustom: Boolean,
        catId: String,
        type: TransactionType,
        ts: Long,
        txId: Long
    ) {
        isCustomCategory = isCustom
        categoryId = catId
        transactionType = type
        timestamp = ts
        transactionId = txId

        viewModelScope.launch {
            // Load category info
            if (isCustomCategory) {
                val customCat = customCategoryRepository.getCustomCategoryById(catId.toLongOrNull() ?: 0)
                customCat?.let {
                    _uiState.update { state ->
                        state.copy(
                            selectedCategoryName = it.name,
                            categoryColor = Color(android.graphics.Color.parseColor(it.color)),
                            customIconName = it.iconName
                        )
                    }
                }
            } else {
                val defaultCat = try {
                    TransactionCategory.valueOf(catId)
                } catch (e: Exception) {
                    null
                }
                defaultCat?.let {
                    _uiState.update { state ->
                        state.copy(
                            selectedCategoryName = it.displayName,
                            categoryColor = it.getColor(),
                            categoryIcon = it.getIcon()
                        )
                    }
                }
            }

            // Load transaction if editing
            if (transactionId > 0) {
                val transaction = transactionRepository.getTransactionById(transactionId)
                transaction?.let {
                    _uiState.update { state ->
                        state.copy(
                            amount = (it.amount / 1000).toString(),
                            description = it.description ?: "",
                            isEditMode = true
                        )
                    }
                }
            }
        }
    }

    fun updateAmount(value: String) {
        if (value.isEmpty() || value.all { it.isDigit() }) {
            _uiState.update { it.copy(amount = value, errorMessage = null) }
        }
    }

    fun updateDescription(value: String) {
        _uiState.update { it.copy(description = value) }
    }

    fun getFormattedAmount(): String {
        val amount = _uiState.value.amount.toLongOrNull() ?: return ""
        val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        return "${formatter.format(amount * 1000)} đ"
    }

    fun getDateDisplay(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }

    fun saveTransaction() {
        val amount = _uiState.value.amount.toLongOrNull()
        if (amount == null || amount <= 0) {
            _uiState.update { it.copy(errorMessage = "Vui lòng nhập số tiền hợp lệ") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val transaction = Transaction(
                    id = if (transactionId > 0) transactionId else 0,
                    source = TransactionSource.MANUAL,
                    type = transactionType,
                    amount = amount * 1000,
                    timestamp = timestamp,
                    rawText = "",
                    rawTextHash = "",
                    description = _uiState.value.description.ifBlank { null },
                    category = if (!isCustomCategory) {
                        try {
                            TransactionCategory.valueOf(categoryId)
                        } catch (e: Exception) {
                            null
                        }
                    } else null,
                    customCategoryId = if (isCustomCategory) categoryId.toLongOrNull() else null
                )

                if (transactionId > 0) {
                    transactionRepository.updateTransaction(transaction)
                } else {
                    transactionRepository.insertTransaction(transaction)
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
