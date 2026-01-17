package com.finly.ui.viewmodel

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finly.data.export.DataExportImportManager
import com.finly.data.repository.TransactionRepository
import com.finly.service.QuickAddNotificationService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * UI State cho Settings Screen
 */
data class SettingsUiState(
    val transactionCount: Int = 0,
    val isLoading: Boolean = false,
    val showDeleteConfirmDialog: Boolean = false,
    val deleteSuccess: Boolean = false,
    val quickAddNotificationEnabled: Boolean = false,
    val exportSuccess: Boolean = false,
    val importSuccess: Boolean = false,
    val importedCount: Int = 0,
    val errorMessage: String? = null
)

/**
 * ViewModel cho màn hình Settings
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    private val exportImportManager = DataExportImportManager(context)

    init {
        loadStats()
        checkQuickAddNotificationStatus()
    }

    /**
     * Load thống kê
     */
    private fun loadStats() {
        viewModelScope.launch {
            transactionRepository.getTransactionCount().collect { count ->
                _uiState.update { it.copy(transactionCount = count) }
            }
        }
    }

    /**
     * Kiểm tra trạng thái Quick Add Notification Service
     */
    private fun checkQuickAddNotificationStatus() {
        val isRunning = isServiceRunning(QuickAddNotificationService::class.java)
        _uiState.update { it.copy(quickAddNotificationEnabled = isRunning) }
    }

    @Suppress("DEPRECATION")
    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    /**
     * Bật/tắt Quick Add Notification
     */
    fun toggleQuickAddNotification(enabled: Boolean) {
        if (enabled) {
            QuickAddNotificationService.start(context)
        } else {
            QuickAddNotificationService.stop(context)
        }
        _uiState.update { it.copy(quickAddNotificationEnabled = enabled) }
    }

    /**
     * Xuất dữ liệu ra file JSON
     */
    fun exportData(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val transactions = withContext(Dispatchers.IO) {
                    transactionRepository.getAllTransactionsSync()
                }
                
                val jsonContent = exportImportManager.exportToJson(transactions)
                val success = withContext(Dispatchers.IO) {
                    exportImportManager.writeToUri(uri, jsonContent)
                }
                
                if (success) {
                    _uiState.update { it.copy(isLoading = false, exportSuccess = true) }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Lỗi ghi file") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Lỗi xuất dữ liệu: ${e.message}") }
            }
        }
    }

    /**
     * Nhập dữ liệu từ file JSON
     */
    fun importData(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val result = withContext(Dispatchers.IO) {
                    exportImportManager.importFromUri(uri)
                }
                
                when (result) {
                    is DataExportImportManager.ImportResult.Success -> {
                        // Insert all transactions
                        withContext(Dispatchers.IO) {
                            result.transactions.forEach { transaction ->
                                try {
                                    transactionRepository.insertTransaction(transaction)
                                } catch (e: Exception) {
                                    // Ignore duplicate errors
                                }
                            }
                        }
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                importSuccess = true,
                                importedCount = result.transactions.size
                            ) 
                        }
                    }
                    is DataExportImportManager.ImportResult.Error -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Lỗi nhập dữ liệu: ${e.message}") }
            }
        }
    }

    /**
     * Reset trạng thái export/import
     */
    fun resetExportImportState() {
        _uiState.update { it.copy(exportSuccess = false, importSuccess = false, errorMessage = null) }
    }

    /**
     * Hiển thị dialog xác nhận xóa
     */
    fun showDeleteConfirmDialog() {
        _uiState.update { it.copy(showDeleteConfirmDialog = true) }
    }

    /**
     * Ẩn dialog xác nhận xóa
     */
    fun hideDeleteConfirmDialog() {
        _uiState.update { it.copy(showDeleteConfirmDialog = false) }
    }

    /**
     * Xóa tất cả giao dịch
     */
    fun deleteAllTransactions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showDeleteConfirmDialog = false) }
            try {
                transactionRepository.deleteAllTransactions()
                _uiState.update { it.copy(isLoading = false, deleteSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * Mở cài đặt Notification Listener
     */
    fun openNotificationSettings(): Intent {
        return Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
    }

    /**
     * Mở cài đặt ứng dụng
     */
    fun openAppSettings(): Intent {
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
    }

    /**
     * Reset trạng thái xóa thành công
     */
    fun resetDeleteSuccess() {
        _uiState.update { it.copy(deleteSuccess = false) }
    }
}
