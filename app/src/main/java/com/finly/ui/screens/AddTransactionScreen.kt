package com.finly.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finly.data.local.entity.TransactionCategory
import com.finly.data.local.entity.TransactionType
import com.finly.data.local.entity.getColor
import com.finly.data.local.entity.getIcon
import com.finly.ui.theme.ExpenseRed
import com.finly.ui.theme.IncomeGreen
import com.finly.ui.viewmodel.AddTransactionViewModel
import java.text.NumberFormat
import java.util.Locale

/**
 * Màn hình thêm giao dịch thủ công
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: AddTransactionViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Tự động quay lại khi lưu thành công
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thêm giao dịch", fontWeight = FontWeight.Bold) },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Transaction Type Selector
            TransactionTypeSelector(
                selectedType = uiState.type,
                onTypeSelected = { viewModel.updateType(it) }
            )

            // 📅 Hiển thị ngày đã chọn
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Ngày giao dịch: ",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = viewModel.getSelectedDateDisplay(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Category Grid - Chọn danh mục nhanh
            CategoryGrid(
                categories = viewModel.getCategoriesForCurrentType(),
                selectedCategory = uiState.category,
                onCategorySelected = { viewModel.updateCategory(it) }
            )

            // Amount Input - hiển thị với ,000
            AmountInputWithThousands(
                amount = uiState.amount,
                onAmountChange = { viewModel.updateAmount(it) },
                isExpense = uiState.type == TransactionType.EXPENSE,
                errorMessage = uiState.errorMessage
            )

            // Description Input
            DescriptionInput(
                description = uiState.description,
                onDescriptionChange = { viewModel.updateDescription(it) }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Save Button
            Button(
                onClick = { viewModel.saveTransaction() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !uiState.isLoading && uiState.amount.isNotBlank() && uiState.category != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.type == TransactionType.EXPENSE) ExpenseRed else IncomeGreen
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Lưu giao dịch",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Selector chọn loại giao dịch (Thu/Chi)
 */
@Composable
private fun TransactionTypeSelector(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit
) {
    Column {
        Text(
            text = "Loại giao dịch",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Expense Button
            OutlinedButton(
                onClick = { onTypeSelected(TransactionType.EXPENSE) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (selectedType == TransactionType.EXPENSE) 
                        ExpenseRed.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                    contentColor = if (selectedType == TransactionType.EXPENSE) 
                        ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = BorderStroke(
                    width = if (selectedType == TransactionType.EXPENSE) 2.dp else 1.dp,
                    color = if (selectedType == TransactionType.EXPENSE) 
                        ExpenseRed else MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingDown,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Chi tiêu", fontWeight = FontWeight.SemiBold)
            }

            // Income Button
            OutlinedButton(
                onClick = { onTypeSelected(TransactionType.INCOME) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (selectedType == TransactionType.INCOME) 
                        IncomeGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                    contentColor = if (selectedType == TransactionType.INCOME) 
                        IncomeGreen else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = BorderStroke(
                    width = if (selectedType == TransactionType.INCOME) 2.dp else 1.dp,
                    color = if (selectedType == TransactionType.INCOME) 
                        IncomeGreen else MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Thu nhập", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

/**
 * Grid chọn danh mục nhanh
 */
@Composable
private fun CategoryGrid(
    categories: List<TransactionCategory>,
    selectedCategory: TransactionCategory?,
    onCategorySelected: (TransactionCategory) -> Unit
) {
    Column {
        Text(
            text = "Chọn danh mục",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        // Grid 4 cột
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.height(180.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(categories) { category ->
                CategoryItem(
                    category = category,
                    isSelected = category == selectedCategory,
                    onClick = { onCategorySelected(category) }
                )
            }
        }
    }
}

/**
 * Item danh mục trong grid - màu pastel
 */
@Composable
private fun CategoryItem(
    category: TransactionCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val categoryColor = category.getColor()
    
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(
                if (isSelected) categoryColor.copy(alpha = 0.2f) else Color.Transparent
            )
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon container - màu pastel
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) categoryColor.copy(alpha = 0.7f) 
                    else categoryColor.copy(alpha = 0.2f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = category.getIcon(),
                contentDescription = category.displayName,
                modifier = Modifier.size(22.dp),
                tint = if (isSelected) Color.White else categoryColor
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Label
        Text(
            text = category.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) categoryColor else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1
        )
    }
}

/**
 * Input nhập số tiền - tự động thêm ,000 đ
 * User nhập: 50 -> hiển thị và lưu: 50,000 đ
 */
@Composable
private fun AmountInputWithThousands(
    amount: String,
    onAmountChange: (String) -> Unit,
    isExpense: Boolean,
    errorMessage: String?
) {
    val accentColor = if (isExpense) ExpenseRed else IncomeGreen
    val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
    
    // Hiển thị số với format: amount,000
    val displayValue = if (amount.isNotBlank()) {
        val numValue = amount.toLongOrNull() ?: 0
        formatter.format(numValue) + ",000"
    } else ""
    
    // Số tiền thực tế (x1000)
    val actualAmount = (amount.toLongOrNull() ?: 0) * 1000
    
    Column {
        Text(
            text = "Số tiền",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        // Hiển thị số tiền lớn
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = accentColor.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Số tiền hiển thị
                Text(
                    text = if (amount.isNotBlank()) "${formatter.format(actualAmount)} ₫" else "0 ₫",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Nhập số nghìn (VD: 50 = 50,000đ)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Input field
        OutlinedTextField(
            value = amount,
            onValueChange = { newValue ->
                // Chỉ cho phép số
                val filtered = newValue.filter { it.isDigit() }
                onAmountChange(filtered)
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Nhập số nghìn đồng...") },
            suffix = { Text(",000 ₫", fontWeight = FontWeight.Bold, color = accentColor) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Payments,
                    contentDescription = null,
                    tint = accentColor
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            isError = errorMessage != null,
            supportingText = errorMessage?.let { { Text(it) } },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentColor,
                cursorColor = accentColor
            ),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

/**
 * Input mô tả
 */
@Composable
private fun DescriptionInput(
    description: String,
    onDescriptionChange: (String) -> Unit
) {
    Column {
        Text(
            text = "Mô tả (tùy chọn)",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Thêm ghi chú...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            maxLines = 2,
            shape = RoundedCornerShape(12.dp)
        )
    }
}
