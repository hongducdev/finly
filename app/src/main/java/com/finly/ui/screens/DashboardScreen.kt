package com.finly.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.TrendingDown
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finly.data.local.entity.Transaction
import com.finly.data.local.entity.TransactionSource
import com.finly.data.local.entity.TransactionType
import com.finly.ui.theme.ExpenseRed
import com.finly.ui.theme.IncomeGreen
import com.finly.ui.viewmodel.DashboardUiState
import com.finly.ui.viewmodel.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Dashboard Screen - Màn hình chính của ứng dụng
 * Hiển thị thống kê chi tiêu và danh sách giao dịch
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit = {},
    onNavigateToAddTransaction: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Finly",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Cài đặt"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddTransaction,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Thêm giao dịch"
                )
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Summary Cards
                item {
                    SummarySection(uiState = uiState)
                }

                // Monthly Stats
                item {
                    MonthlyStatsCard(uiState = uiState)
                }

                // Recent Transactions Header
                item {
                    Text(
                        text = "Giao dịch gần đây",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Transaction List
                if (uiState.recentTransactions.isEmpty()) {
                    item {
                        EmptyTransactionCard()
                    }
                } else {
                    items(
                        items = uiState.recentTransactions,
                        key = { it.id }
                    ) { transaction ->
                        TransactionItem(
                            transaction = transaction,
                            onDelete = { viewModel.deleteTransaction(transaction.id) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Phần tổng quan chi tiêu hôm nay
 */
@Composable
private fun SummarySection(uiState: DashboardUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Chi tiêu hôm nay
        SummaryCard(
            modifier = Modifier.weight(1f),
            title = "Chi hôm nay",
            amount = uiState.todayExpense,
            icon = Icons.Outlined.TrendingDown,
            isExpense = true
        )

        // Thu nhập hôm nay
        SummaryCard(
            modifier = Modifier.weight(1f),
            title = "Thu hôm nay",
            amount = uiState.todayIncome,
            icon = Icons.Outlined.TrendingUp,
            isExpense = false
        )
    }
}

/**
 * Card hiển thị số tiền tổng
 */
@Composable
private fun SummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    amount: Long,
    icon: ImageVector,
    isExpense: Boolean
) {
    val backgroundColor = if (isExpense) {
        ExpenseRed.copy(alpha = 0.1f)
    } else {
        IncomeGreen.copy(alpha = 0.1f)
    }
    
    val contentColor = if (isExpense) ExpenseRed else IncomeGreen

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(backgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = DashboardViewModel.formatCurrency(amount),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

/**
 * Card thống kê tháng
 */
@Composable
private fun MonthlyStatsCard(uiState: DashboardUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Tháng này",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Tổng chi",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = DashboardViewModel.formatCurrency(uiState.monthExpense),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = ExpenseRed
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Tổng thu",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = DashboardViewModel.formatCurrency(uiState.monthIncome),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = IncomeGreen
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Balance
            val balance = uiState.monthIncome - uiState.monthExpense
            val balanceColor = if (balance >= 0) IncomeGreen else ExpenseRed
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cân đối",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "${if (balance >= 0) "+" else ""}${DashboardViewModel.formatCurrency(balance)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = balanceColor
                )
            }
        }
    }
}

/**
 * Item giao dịch trong danh sách
 */
@Composable
private fun TransactionItem(
    transaction: Transaction,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Xóa giao dịch") },
            text = { Text("Bạn có chắc muốn xóa giao dịch này?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Xóa", color = ExpenseRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Source Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(getSourceColor(transaction.source).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getSourceIcon(transaction.source),
                    contentDescription = null,
                    tint = getSourceColor(transaction.source),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Transaction Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.source.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatTimestamp(transaction.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                transaction.description?.let { desc ->
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // Amount
            Column(horizontalAlignment = Alignment.End) {
                val isExpense = transaction.type == TransactionType.EXPENSE
                val amountColor = if (isExpense) ExpenseRed else IncomeGreen
                val prefix = if (isExpense) "-" else "+"
                
                Text(
                    text = "$prefix${DashboardViewModel.formatCurrency(transaction.amount)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )
                
                transaction.balance?.let { balance ->
                    Text(
                        text = "SD: ${DashboardViewModel.formatCurrency(balance)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Card hiển thị khi chưa có giao dịch
 */
@Composable
private fun EmptyTransactionCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.AccountBalance,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Chưa có giao dịch nào",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Giao dịch sẽ tự động được ghi nhận khi bạn nhận thông báo từ ngân hàng",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

// ==================== Helper Functions ====================

private fun getSourceIcon(source: TransactionSource): ImageVector {
    return when (source) {
        TransactionSource.TECHCOMBANK -> Icons.Default.AccountBalance
        TransactionSource.VIETINBANK -> Icons.Default.AccountBalance
        TransactionSource.TIMO -> Icons.Default.CreditCard
        TransactionSource.CAKE -> Icons.Default.CreditCard
        TransactionSource.MOMO -> Icons.Default.Wallet
        TransactionSource.ZALOPAY -> Icons.Default.Wallet
        TransactionSource.MANUAL -> Icons.Default.Edit
        TransactionSource.UNKNOWN -> Icons.Default.QuestionMark
    }
}

private fun getSourceColor(source: TransactionSource): androidx.compose.ui.graphics.Color {
    return when (source) {
        TransactionSource.TECHCOMBANK -> androidx.compose.ui.graphics.Color(0xFFE31837) // Red
        TransactionSource.VIETINBANK -> androidx.compose.ui.graphics.Color(0xFF1E3A8A) // Blue
        TransactionSource.TIMO -> androidx.compose.ui.graphics.Color(0xFF00BFA5) // Teal
        TransactionSource.CAKE -> androidx.compose.ui.graphics.Color(0xFFFFD700) // Yellow
        TransactionSource.MOMO -> androidx.compose.ui.graphics.Color(0xFFD82D8B) // Pink
        TransactionSource.ZALOPAY -> androidx.compose.ui.graphics.Color(0xFF0068FF) // Blue
        TransactionSource.MANUAL -> androidx.compose.ui.graphics.Color(0xFF6366F1) // Indigo
        TransactionSource.UNKNOWN -> androidx.compose.ui.graphics.Color.Gray
    }
}

private val dateFormatter = SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale("vi", "VN"))

private fun formatTimestamp(timestamp: Long): String {
    return dateFormatter.format(Date(timestamp))
}
