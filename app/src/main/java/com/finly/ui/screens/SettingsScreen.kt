package com.finly.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finly.ui.theme.ExpenseRed
import com.finly.ui.theme.IncomeGreen
import com.finly.ui.viewmodel.SettingsViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Màn hình cài đặt
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToBudget: () -> Unit = {},
    onNavigateToSavingsGoal: () -> Unit = {},
    onNavigateToDebts: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val formatter = NumberFormat.getInstance(Locale("vi", "VN"))

    // Launcher để xin quyền POST_NOTIFICATIONS
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleQuickAddNotification(true)
        }
    }

    // Launcher để tạo file export
    val exportFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.exportData(it) }
    }

    // Launcher để chọn file import
    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importData(it) }
    }

    // Xử lý sau khi xóa thành công
    LaunchedEffect(uiState.deleteSuccess) {
        if (uiState.deleteSuccess) {
            Toast.makeText(context, "Đã xóa tất cả giao dịch", Toast.LENGTH_SHORT).show()
            viewModel.resetDeleteSuccess()
        }
    }

    // Xử lý sau khi export thành công
    LaunchedEffect(uiState.exportSuccess) {
        if (uiState.exportSuccess) {
            Toast.makeText(context, "Đã xuất dữ liệu thành công!", Toast.LENGTH_SHORT).show()
            viewModel.resetExportImportState()
        }
    }

    // Xử lý sau khi import thành công
    LaunchedEffect(uiState.importSuccess) {
        if (uiState.importSuccess) {
            Toast.makeText(context, "Đã nhập ${uiState.importedCount} giao dịch!", Toast.LENGTH_SHORT).show()
            viewModel.resetExportImportState()
        }
    }

    // Xử lý lỗi
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            viewModel.resetExportImportState()
        }
    }

    // Dialog xác nhận xóa
    if (uiState.showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteConfirmDialog() },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = ExpenseRed) },
            title = { Text("Xóa tất cả dữ liệu?") },
            text = { 
                Text("Bạn có chắc muốn xóa tất cả ${formatter.format(uiState.transactionCount)} giao dịch? Hành động này không thể hoàn tác.")
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteAllTransactions() },
                    colors = ButtonDefaults.textButtonColors(contentColor = ExpenseRed)
                ) {
                    Text("Xóa tất cả", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDeleteConfirmDialog() }) {
                    Text("Hủy")
                }
            }
        )
    }

    // Loading overlay
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    // PIN Creation Dialog
    var showPinDialog by remember { mutableStateOf(false) }
    var pinStep by remember { mutableIntStateOf(0) } // 0: Enter, 1: Confirm
    var firstPin by remember { mutableStateOf("") }
    var currentPinInput by remember { mutableStateOf("") }
    
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { 
                showPinDialog = false 
                pinStep = 0
                firstPin = ""
                currentPinInput = ""
                // If cancelled and lock not enabled, ensure toggle is off
                if (!viewModel.isPinSet()) {
                    viewModel.toggleAppLock(false)
                }
            },
            title = { Text(if (pinStep == 0) "Tạo mã PIN mới" else "Xác nhận mã PIN") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (pinStep == 0) "Nhập 4 số PIN của bạn" else "Nhập lại mã PIN để xác nhận")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = currentPinInput,
                        onValueChange = { 
                            if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                currentPinInput = it
                            }
                        },
                        singleLine = true,
                        // VisualTransformation for PIN dots can be added here
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (currentPinInput.length == 4) {
                            if (pinStep == 0) {
                                firstPin = currentPinInput
                                currentPinInput = ""
                                pinStep = 1
                            } else {
                                if (currentPinInput == firstPin) {
                                    // Success
                                    val hash = java.security.MessageDigest.getInstance("SHA-256")
                                        .digest(currentPinInput.toByteArray())
                                        .joinToString("") { "%02x".format(it) }
                                    
                                    viewModel.setPin(hash)
                                    showPinDialog = false
                                    pinStep = 0
                                    firstPin = ""
                                    currentPinInput = ""
                                } else {
                                    Toast.makeText(context, "Mã PIN không khớp", Toast.LENGTH_SHORT).show()
                                    currentPinInput = ""
                                }
                            }
                        }
                    }
                ) {
                    Text(if (pinStep == 0) "Tiếp tục" else "Xác nhận")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showPinDialog = false
                    pinStep = 0
                    firstPin = ""
                    currentPinInput = ""
                    if (!viewModel.isPinSet()) {
                        viewModel.toggleAppLock(false)
                    }
                }) {
                    Text("Hủy")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cài đặt", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Quản lý tài chính Section
            SettingsSection(title = "Quản lý tài chính") {
                SettingsItem(
                    icon = Icons.Default.AccountBalanceWallet,
                    title = "Ngân sách",
                    subtitle = "Đặt hạn mức chi tiêu theo danh mục",
                    onClick = onNavigateToBudget
                )
                
                HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                
                SettingsItem(
                    icon = Icons.Default.Savings,
                    title = "Mục tiêu tiết kiệm",
                    subtitle = "Theo dõi tiến trình tiết kiệm",
                    onClick = onNavigateToSavingsGoal
                )
                
                HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                
                SettingsItem(
                    icon = Icons.Default.MoneyOff,
                    title = "Quản lý nợ",
                    subtitle = "Theo dõi các khoản cho vay và đi vay",
                    onClick = onNavigateToDebts
                )
            }

            // Tiện ích Section
            SettingsSection(title = "Tiện ích") {
                SettingsToggleItem(
                    icon = Icons.Default.NotificationsActive,
                    title = "Thêm nhanh từ thông báo",
                    subtitle = "Hiển thị thông báo với nút Chi tiêu và Thu nhập",
                    checked = uiState.quickAddNotificationEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            // Kiểm tra và xin quyền POST_NOTIFICATIONS cho Android 13+
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED
                                
                                if (hasPermission) {
                                    viewModel.toggleQuickAddNotification(true)
                                } else {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            } else {
                                viewModel.toggleQuickAddNotification(true)
                            }
                        } else {
                            viewModel.toggleQuickAddNotification(false)
                        }
                    }
                )
            }
            
            // Bảo mật Section
            SettingsSection(title = "Bảo mật") {
                SettingsToggleItem(
                    icon = Icons.Default.Lock,
                    title = "Khóa ứng dụng",
                    subtitle = "Sử dụng PIN hoặc sinh trắc học",
                    checked = uiState.isAppLockEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled && !viewModel.isPinSet()) {
                            showPinDialog = true
                        } else {
                            viewModel.toggleAppLock(enabled)
                        }
                    }
                )
                
                if (uiState.isAppLockEnabled) {
                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                    
                    SettingsToggleItem(
                        icon = Icons.Default.Fingerprint,
                        title = "Sử dụng sinh trắc học",
                        subtitle = "Vân tay hoặc khuôn mặt",
                        checked = uiState.isBiometricEnabled,
                        onCheckedChange = { viewModel.toggleBiometric(it) }
                    )
                }
            }

            // Dữ liệu Section
            SettingsSection(title = "Dữ liệu") {
                SettingsInfoItem(
                    icon = Icons.Default.Receipt,
                    title = "Tổng giao dịch",
                    value = formatter.format(uiState.transactionCount)
                )
                
                HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                
                SettingsItem(
                    icon = Icons.Default.Upload,
                    title = "Xuất dữ liệu",
                    subtitle = "Xuất giao dịch ra file JSON",
                    titleColor = IncomeGreen,
                    onClick = {
                        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                        val fileName = "finly_backup_${dateFormat.format(Date())}.json"
                        exportFileLauncher.launch(fileName)
                    }
                )
                
                HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                
                SettingsItem(
                    icon = Icons.Default.Download,
                    title = "Nhập dữ liệu",
                    subtitle = "Nhập giao dịch từ file JSON",
                    titleColor = MaterialTheme.colorScheme.primary,
                    onClick = {
                        importFileLauncher.launch(arrayOf("application/json", "*/*"))
                    }
                )
                
                HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                
                SettingsItem(
                    icon = Icons.Default.DeleteForever,
                    title = "Xóa tất cả giao dịch",
                    subtitle = "Xóa toàn bộ dữ liệu giao dịch đã lưu",
                    titleColor = ExpenseRed,
                    onClick = { viewModel.showDeleteConfirmDialog() }
                )
            }

            // Quyền ứng dụng Section
            SettingsSection(title = "Quyền ứng dụng") {
                SettingsItem(
                    icon = Icons.Default.Notifications,
                    title = "Quyền đọc thông báo",
                    subtitle = "Cho phép đọc thông báo từ ứng dụng ngân hàng",
                    onClick = {
                        context.startActivity(viewModel.openNotificationSettings())
                    }
                )
                
                HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                
                SettingsItem(
                    icon = Icons.Default.Settings,
                    title = "Cài đặt ứng dụng",
                    subtitle = "Mở cài đặt hệ thống cho Finly",
                    onClick = {
                        context.startActivity(viewModel.openAppSettings())
                    }
                )
            }

            // Thông tin Section
            SettingsSection(title = "Thông tin") {
                SettingsInfoItem(
                    icon = Icons.Default.Info,
                    title = "Phiên bản",
                    value = "1.0.0"
                )
                
                HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                
                SettingsInfoItem(
                    icon = Icons.Default.Code,
                    title = "Được phát triển bởi",
                    value = "Finly Team"
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * Section header
 */
@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
        )
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(content = content)
        }
    }
}

/**
 * Settings item với click action
 */
@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    titleColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = titleColor.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = titleColor
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Settings item hiển thị thông tin (không click)
 */
@Composable
private fun SettingsInfoItem(
    icon: ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Settings item với Switch toggle
 */
@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (checked) MaterialTheme.colorScheme.primary 
                   else MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
