package com.finly.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finly.data.local.entity.SavingsGoal
import com.finly.ui.theme.IncomeGreen
import com.finly.ui.theme.ExpenseRed
import com.finly.ui.viewmodel.SavingsGoalViewModel
import java.text.NumberFormat
import java.util.*

/**
 * Màn hình Mục tiêu tiết kiệm
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsGoalScreen(
    viewModel: SavingsGoalViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val formatter = NumberFormat.getInstance(Locale("vi", "VN"))

    // Dialog thêm mục tiêu
    if (uiState.showAddDialog) {
        AddGoalDialog(
            name = uiState.inputName,
            targetAmount = uiState.inputTargetAmount,
            selectedIcon = uiState.inputIcon,
            availableIcons = viewModel.getAvailableIcons(),
            onNameChange = { viewModel.updateInputName(it) },
            onTargetAmountChange = { viewModel.updateInputTargetAmount(it) },
            onIconSelected = { viewModel.updateInputIcon(it) },
            onSave = { viewModel.saveGoal() },
            onDismiss = { viewModel.hideAddDialog() }
        )
    }

    // Dialog thêm tiền
    if (uiState.showAddMoneyDialog) {
        AddMoneyDialog(
            amount = uiState.inputAddAmount,
            onAmountChange = { viewModel.updateInputAddAmount(it) },
            onSave = { viewModel.addMoney() },
            onDismiss = { viewModel.hideAddMoneyDialog() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mục tiêu tiết kiệm", fontWeight = FontWeight.Bold) },
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
            FloatingActionButton(
                onClick = { viewModel.showAddDialog() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm mục tiêu")
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
            // Active Goals
            if (uiState.activeGoals.isNotEmpty()) {
                item {
                    Text(
                        text = "Đang thực hiện",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                items(uiState.activeGoals) { goal ->
                    SavingsGoalItem(
                        goal = goal,
                        formatter = formatter,
                        onAddMoney = { viewModel.showAddMoneyDialog(goal.id) },
                        onDelete = { viewModel.deleteGoal(goal.id) }
                    )
                }
            }

            // Completed Goals
            if (uiState.completedGoals.isNotEmpty()) {
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        text = "Đã hoàn thành 🎉",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = IncomeGreen
                    )
                }
                
                items(uiState.completedGoals) { goal ->
                    CompletedGoalItem(
                        goal = goal,
                        formatter = formatter,
                        onDelete = { viewModel.deleteGoal(goal.id) }
                    )
                }
            }

            // Empty state
            if (uiState.activeGoals.isEmpty() && uiState.completedGoals.isEmpty() && !uiState.isLoading) {
                item {
                    EmptyGoalsCard()
                }
            }
        }
    }
}

/**
 * Item mục tiêu đang thực hiện
 */
@Composable
private fun SavingsGoalItem(
    goal: SavingsGoal,
    formatter: NumberFormat,
    onAddMoney: () -> Unit,
    onDelete: () -> Unit
) {
    val percentage = if (goal.targetAmount > 0) 
        (goal.currentAmount.toFloat() / goal.targetAmount * 100).coerceAtMost(100f) else 0f
    val remaining = goal.targetAmount - goal.currentAmount
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = goal.icon,
                        fontSize = 24.sp
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${formatter.format(goal.currentAmount)}đ / ${formatter.format(goal.targetAmount)}đ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Percentage
                Text(
                    text = "${String.format("%.0f", percentage)}%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Progress bar
            LinearProgressIndicator(
                progress = { percentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = IncomeGreen,
                trackColor = IncomeGreen.copy(alpha = 0.2f)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Còn thiếu ${formatter.format(remaining)}đ",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, "Xóa", tint = ExpenseRed.copy(alpha = 0.7f))
                    }
                    Button(
                        onClick = onAddMoney,
                        colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen)
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Thêm tiền")
                    }
                }
            }
        }
    }
}

/**
 * Item mục tiêu đã hoàn thành
 */
@Composable
private fun CompletedGoalItem(
    goal: SavingsGoal,
    formatter: NumberFormat,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = IncomeGreen.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(IncomeGreen.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = goal.icon, fontSize = 20.sp)
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = goal.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${formatter.format(goal.targetAmount)}đ",
                    style = MaterialTheme.typography.labelSmall,
                    color = IncomeGreen
                )
            }
            
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Hoàn thành",
                tint = IncomeGreen,
                modifier = Modifier.size(24.dp)
            )
            
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Xóa", tint = ExpenseRed.copy(alpha = 0.5f))
            }
        }
    }
}

/**
 * Card khi chưa có mục tiêu
 */
@Composable
private fun EmptyGoalsCard() {
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
            Text("🎯", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Chưa có mục tiêu tiết kiệm",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Nhấn + để thêm mục tiêu mới",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Dialog thêm mục tiêu
 */
@Composable
private fun AddGoalDialog(
    name: String,
    targetAmount: String,
    selectedIcon: String,
    availableIcons: List<String>,
    onNameChange: (String) -> Unit,
    onTargetAmountChange: (String) -> Unit,
    onIconSelected: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm mục tiêu", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Icon selection
                Text("Chọn icon", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(availableIcons) { icon ->
                        val isSelected = icon == selectedIcon
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { onIconSelected(icon) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = icon, fontSize = 20.sp)
                        }
                    }
                }
                
                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Tên mục tiêu") },
                    placeholder = { Text("VD: Mua iPhone, Du lịch...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Target amount
                OutlinedTextField(
                    value = targetAmount,
                    onValueChange = onTargetAmountChange,
                    label = { Text("Số tiền mục tiêu (nghìn đồng)") },
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
                enabled = name.isNotBlank() && targetAmount.isNotBlank()
            ) {
                Text("Lưu")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        }
    )
}

/**
 * Dialog thêm tiền
 */
@Composable
private fun AddMoneyDialog(
    amount: String,
    onAmountChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Savings, null, tint = IncomeGreen) },
        title = { Text("Thêm tiền tiết kiệm", fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = amount,
                onValueChange = onAmountChange,
                label = { Text("Số tiền (nghìn đồng)") },
                suffix = { Text(",000 đ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = amount.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen)
            ) {
                Text("Thêm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        }
    )
}
