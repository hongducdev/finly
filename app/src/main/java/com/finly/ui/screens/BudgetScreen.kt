package com.finly.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finly.data.local.entity.TransactionCategory
import com.finly.data.local.entity.getColor
import com.finly.data.local.entity.getIcon
import com.finly.ui.theme.ExpenseRed
import com.finly.ui.theme.IncomeGreen
import com.finly.ui.viewmodel.BudgetViewModel
import com.finly.ui.viewmodel.BudgetWithSpent
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Màn hình Ngân sách
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("vi", "VN"))
    val formatter = NumberFormat.getInstance(Locale("vi", "VN"))

    // Dialog thêm ngân sách
    if (uiState.showAddDialog) {
        AddBudgetDialog(
            availableCategories = viewModel.getAvailableCategories(),
            selectedCategory = uiState.selectedCategory,
            amount = uiState.inputAmount,
            onCategorySelected = { viewModel.selectCategory(it) },
            onAmountChange = { viewModel.updateAmount(it) },
            onSave = { viewModel.saveBudget() },
            onDismiss = { viewModel.hideAddDialog() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ngân sách", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            if (viewModel.getAvailableCategories().isNotEmpty()) {
                FloatingActionButton(
                    onClick = { viewModel.showAddDialog() },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Thêm ngân sách")
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Month Navigation
            item {
                MonthNavigationCard(
                    currentMonth = uiState.currentMonth,
                    monthFormat = monthFormat,
                    onPrevious = { viewModel.previousMonth() },
                    onNext = { viewModel.nextMonth() }
                )
            }

            // Summary Card
            item {
                BudgetSummaryCard(
                    totalBudget = uiState.totalBudget,
                    totalSpent = uiState.totalSpent,
                    formatter = formatter
                )
            }

            // Budget List
            if (uiState.budgets.isEmpty() && !uiState.isLoading) {
                item {
                    EmptyBudgetCard()
                }
            } else {
                items(uiState.budgets) { budgetWithSpent ->
                    BudgetItem(
                        budgetWithSpent = budgetWithSpent,
                        formatter = formatter,
                        onDelete = { viewModel.deleteBudget(budgetWithSpent.budget.id) }
                    )
                }
            }
        }
    }
}

/**
 * Card navigation tháng
 */
@Composable
private fun MonthNavigationCard(
    currentMonth: Calendar,
    monthFormat: SimpleDateFormat,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevious) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Tháng trước")
            }
            Text(
                text = monthFormat.format(currentMonth.time).replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onNext) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Tháng sau")
            }
        }
    }
}

/**
 * Card tổng quan ngân sách
 */
@Composable
private fun BudgetSummaryCard(
    totalBudget: Long,
    totalSpent: Long,
    formatter: NumberFormat
) {
    val remaining = totalBudget - totalSpent
    val percentage = if (totalBudget > 0) (totalSpent.toFloat() / totalBudget * 100) else 0f
    val isOverBudget = totalSpent > totalBudget
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Tổng quan tháng này",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Đã chi", style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = "${formatter.format(totalSpent)}đ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ExpenseRed
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Ngân sách", style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = "${formatter.format(totalBudget)}đ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Progress bar
            LinearProgressIndicator(
                progress = { (percentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (isOverBudget) ExpenseRed else IncomeGreen,
                trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Remaining
            Text(
                text = if (isOverBudget) "Vượt ngân sách ${formatter.format(-remaining)}đ"
                       else "Còn lại ${formatter.format(remaining)}đ",
                style = MaterialTheme.typography.labelMedium,
                color = if (isOverBudget) ExpenseRed else IncomeGreen,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Item ngân sách
 */
@Composable
private fun BudgetItem(
    budgetWithSpent: BudgetWithSpent,
    formatter: NumberFormat,
    onDelete: () -> Unit
) {
    val budget = budgetWithSpent.budget
    val spent = budgetWithSpent.spent
    val percentage = budgetWithSpent.percentage
    val remaining = budget.amount - spent
    val isOverBudget = spent > budget.amount
    val categoryColor = budget.category.getColor()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = categoryColor.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(categoryColor.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = budget.category.getIcon(),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = categoryColor
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = budget.category.displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${formatter.format(spent)}đ / ${formatter.format(budget.amount)}đ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Delete button
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Xóa",
                        tint = ExpenseRed.copy(alpha = 0.7f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Progress bar
            LinearProgressIndicator(
                progress = { (percentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isOverBudget) ExpenseRed else categoryColor,
                trackColor = categoryColor.copy(alpha = 0.2f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Status
            Text(
                text = if (isOverBudget) "⚠️ Vượt ${formatter.format(-remaining)}đ"
                       else "Còn ${formatter.format(remaining)}đ (${String.format("%.0f", 100 - percentage)}%)",
                style = MaterialTheme.typography.labelSmall,
                color = if (isOverBudget) ExpenseRed else IncomeGreen,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Card khi chưa có ngân sách
 */
@Composable
private fun EmptyBudgetCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.AccountBalanceWallet,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Chưa có ngân sách",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Nhấn + để thêm ngân sách mới",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Dialog thêm ngân sách
 */
@Composable
private fun AddBudgetDialog(
    availableCategories: List<TransactionCategory>,
    selectedCategory: TransactionCategory?,
    amount: String,
    onCategorySelected: (TransactionCategory) -> Unit,
    onAmountChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm ngân sách", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Category selection
                Text("Chọn danh mục", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(availableCategories) { category ->
                        val isSelected = category == selectedCategory
                        val color = category.getColor()
                        
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onCategorySelected(category) }
                                .background(if (isSelected) color.copy(alpha = 0.2f) else Color.Transparent)
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) color else color.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    category.getIcon(),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = if (isSelected) Color.White else color
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = category.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1
                            )
                        }
                    }
                }
                
                // Amount input
                OutlinedTextField(
                    value = amount,
                    onValueChange = onAmountChange,
                    label = { Text("Hạn mức (nghìn đồng)") },
                    suffix = { Text(",000 đ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = selectedCategory != null && amount.isNotBlank()
            ) {
                Text("Lưu")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}
