package com.finly.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finly.data.local.entity.Transaction
import com.finly.data.local.entity.TransactionCategory
import com.finly.data.local.entity.TransactionSource
import com.finly.data.local.entity.TransactionType
import com.finly.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * UI State cho màn hình thêm giao dịch
 */
data class AddTransactionUiState(
    val amount: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val source: TransactionSource = TransactionSource.MANUAL,
    val category: TransactionCategory? = null,
    val description: String = "",
    val selectedTimestamp: Long = System.currentTimeMillis(),
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null,
    val transactionId: Long? = null
) {
    val isEditMode: Boolean get() = transactionId != null
}

/**
 * ViewModel cho màn hình thêm giao dịch thủ công
 */
@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    savedStateHandle: SavedStateHandle,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    init {
        // Lấy timestamp từ navigation argument nếu có
        val timestampArg = savedStateHandle.get<Long>("timestamp")
        if (timestampArg != null && timestampArg > 0) {
            _uiState.update { it.copy(selectedTimestamp = timestampArg) }
        }
        
        // Lấy transactionId để edit
        val transactionIdArg = savedStateHandle.get<Long>("transactionId")
        if (transactionIdArg != null && transactionIdArg > 0) {
            loadTransaction(transactionIdArg)
        } else {
            // Lấy type từ navigation argument (chỉ khi tạo mới)
            val typeArg = savedStateHandle.get<String>("type")
            if (typeArg != null) {
                val transactionType = when (typeArg) {
                    "INCOME" -> TransactionType.INCOME
                    else -> TransactionType.EXPENSE
                }
                _uiState.update { it.copy(type = transactionType) }
            }
        }
    }

    private fun loadTransaction(id: Long) {
        viewModelScope.launch {
            val transaction = transactionRepository.getTransactionById(id)
            if (transaction != null) {
                _uiState.update { 
                    it.copy(
                        transactionId = transaction.id,
                        amount = (transaction.amount / 1000).toString(), // Chuyển về đơn vị nghìn
                        type = transaction.type,
                        source = transaction.source,
                        category = transaction.category,
                        description = transaction.description ?: "",
                        selectedTimestamp = transaction.timestamp
                    ) 
                }
            }
        }
    }

    /**
     * Lấy ngày đã chọn dạng hiển thị
     */
    fun getSelectedDateDisplay(): String {
        val timestamp = _uiState.value.selectedTimestamp
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("vi", "VN"))
        val today = Calendar.getInstance()
        val selectedDate = Calendar.getInstance().apply { timeInMillis = timestamp }
        
        return when {
            isSameDay(today, selectedDate) -> "Hôm nay"
            isYesterday(today, selectedDate) -> "Hôm qua"
            else -> dateFormat.format(Date(timestamp))
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isYesterday(today: Calendar, date: Calendar): Boolean {
        val yesterday = today.clone() as Calendar
        yesterday.add(Calendar.DAY_OF_YEAR, -1)
        return isSameDay(yesterday, date)
    }

    /**
     * Cập nhật số tiền
     */
    fun updateAmount(amount: String) {
        // Chỉ cho phép nhập số
        val filteredAmount = amount.filter { it.isDigit() }
        _uiState.update { it.copy(amount = filteredAmount, errorMessage = null) }
    }

    /**
     * Cập nhật loại giao dịch và reset category nếu thay đổi
     */
    fun updateType(type: TransactionType) {
        _uiState.update { 
            it.copy(
                type = type, 
                category = null // Reset category khi đổi loại
            ) 
        }
    }

    /**
     * Cập nhật danh mục đã chọn
     */
    fun updateCategory(category: TransactionCategory) {
        _uiState.update { 
            it.copy(
                category = category,
                // Tự động điền description bằng tên danh mục nếu chưa có
                description = if (it.description.isBlank()) category.displayName else it.description
            ) 
        }
    }

    /**
     * Cập nhật mô tả
     */
    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    /**
     * Lấy danh sách danh mục theo loại giao dịch hiện tại
     */
    fun getCategoriesForCurrentType(): List<TransactionCategory> {
        return TransactionCategory.getByType(_uiState.value.type)
    }

    /**
     * Lưu giao dịch
     * Số tiền được nhân x1000 (user nhập 50 = 50,000 đ)
     */
    fun saveTransaction() {
        val currentState = _uiState.value
        
        // Validate
        val inputValue = currentState.amount.toLongOrNull()
        if (inputValue == null || inputValue <= 0) {
            _uiState.update { it.copy(errorMessage = "Vui lòng nhập số tiền hợp lệ") }
            return
        }
        
        // Nhân x1000 để chuyển sang đơn vị đồng
        val amountValue = inputValue * 1000

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Sử dụng timestamp từ ngày đã chọn
                val timestamp = currentState.selectedTimestamp
                val rawText = "MANUAL|${currentState.type}|$amountValue|${currentState.source}|${currentState.category?.name ?: ""}|$timestamp"
                val hash = generateHash(rawText)
                
                val transaction = Transaction(
                    id = currentState.transactionId ?: 0, // 0 means insert, otherwise update
                    source = currentState.source,
                    type = currentState.type,
                    amount = amountValue,
                    balance = null,
                    timestamp = timestamp,
                    rawText = rawText,
                    rawTextHash = hash,
                    description = currentState.description.ifBlank { currentState.category?.displayName },
                    category = currentState.category
                )
                
                if (currentState.isEditMode) {
                    transactionRepository.updateTransaction(transaction)
                } else {
                    transactionRepository.insertTransaction(transaction)
                }
                _uiState.update { it.copy(isLoading = false, isSaved = true) }
                
                // Update widget
                com.finly.widget.FinlyWidgetProvider.sendRefreshBroadcast(context)
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        errorMessage = "Lỗi lưu giao dịch: ${e.message}"
                    ) 
                }
            }
        }
    }

    /**
     * Tạo hash từ raw text để tránh duplicate
     */
    private fun generateHash(text: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(text.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
