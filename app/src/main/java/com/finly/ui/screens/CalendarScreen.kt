package com.finly.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finly.data.local.entity.Transaction
import com.finly.data.local.entity.TransactionCategory
import com.finly.data.local.entity.TransactionType
import com.finly.data.local.entity.getColor
import com.finly.data.local.entity.getIcon
import com.finly.ui.theme.ExpenseRed
import com.finly.ui.theme.IncomeGreen
import com.finly.ui.viewmodel.CalendarViewModel
import com.finly.ui.viewmodel.DaySummary
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Màn hình chính dạng lịch
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = hiltViewModel(),
    onNavigateToAddTransaction: (Long) -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToStatistics: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Finly", fontWeight = FontWeight.Bold) },
                actions = {
                    // Nút thống kê
                    IconButton(onClick = onNavigateToStatistics) {
                        Icon(Icons.Default.PieChart, contentDescription = "Thống kê")
                    }
                    // Nút cài đặt
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Cài đặt")
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
                onClick = { 
                    // Truyền timestamp của ngày đã chọn (hoặc ngày hiện tại nếu chưa chọn)
                    val selectedTimestamp = uiState.selectedDate?.timeInMillis ?: System.currentTimeMillis()
                    onNavigateToAddTransaction(selectedTimestamp)
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm giao dịch")
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
            // Month Summary Card
            item {
                MonthSummaryCard(
                    income = uiState.monthlyIncome,
                    expense = uiState.monthlyExpense
                )
            }
            
            // Calendar Card
            item {
                CalendarCard(
                    currentMonth = uiState.currentMonth,
                    selectedDate = uiState.selectedDate,
                    daySummaries = uiState.daySummaries,
                    onPreviousMonth = { viewModel.previousMonth() },
                    onNextMonth = { viewModel.nextMonth() },
                    onDateSelected = { viewModel.selectDate(it) },
                    daysInMonth = viewModel.getDaysInMonth(),
                    firstDayOfWeek = viewModel.getFirstDayOfWeek()
                )
            }
            
            // Selected Day Transactions
            if (uiState.selectedDate != null && uiState.selectedDayTransactions.isNotEmpty()) {
                item {
                    Text(
                        text = "Giao dịch ngày ${uiState.selectedDate?.get(Calendar.DAY_OF_MONTH)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                items(uiState.selectedDayTransactions) { transaction ->
                    TransactionItem(transaction = transaction)
                }
            } else if (uiState.selectedDate != null) {
                item {
                    EmptyDayCard()
                }
            }
        }
    }
}

/**
 * Card tổng hợp thu chi tháng
 */
@Composable
private fun MonthSummaryCard(
    income: Long,
    expense: Long
) {
    val balance = income - expense
    val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Tổng quan tháng này",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Thu nhập
                Column {
                    Text(
                        text = "Thu nhập",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "+${formatter.format(income)}đ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = IncomeGreen
                    )
                }
                
                // Chi tiêu
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Chi tiêu",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "-${formatter.format(expense)}đ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ExpenseRed
                    )
                }
            }
            
            HorizontalDivider()
            
            // Số dư
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Chênh lệch",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "${if (balance >= 0) "+" else ""}${formatter.format(balance)}đ",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (balance >= 0) IncomeGreen else ExpenseRed
                )
            }
        }
    }
}

/**
 * Card lịch tháng
 */
@Composable
private fun CalendarCard(
    currentMonth: Calendar,
    selectedDate: Calendar?,
    daySummaries: Map<Int, DaySummary>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDateSelected: (Int) -> Unit,
    daysInMonth: Int,
    firstDayOfWeek: Int
) {
    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("vi", "VN"))
    val weekDays = listOf("CN", "T2", "T3", "T4", "T5", "T6", "T7")
    val today = Calendar.getInstance()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Month navigation header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Tháng trước")
                }
                
                Text(
                    text = monthFormat.format(currentMonth.time).replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(onClick = onNextMonth) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Tháng sau")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Week day headers
            Row(modifier = Modifier.fillMaxWidth()) {
                weekDays.forEach { day ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Calendar grid
            val totalCells = firstDayOfWeek + daysInMonth
            val rows = (totalCells + 6) / 7
            
            for (row in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0..6) {
                        val cellIndex = row * 7 + col
                        val day = cellIndex - firstDayOfWeek + 1
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            if (day in 1..daysInMonth) {
                                val isToday = today.get(Calendar.YEAR) == currentMonth.get(Calendar.YEAR) &&
                                        today.get(Calendar.MONTH) == currentMonth.get(Calendar.MONTH) &&
                                        today.get(Calendar.DAY_OF_MONTH) == day
                                val isSelected = selectedDate?.get(Calendar.DAY_OF_MONTH) == day
                                val dayData = daySummaries[day]
                                
                                DayCell(
                                    day = day,
                                    isToday = isToday,
                                    isSelected = isSelected,
                                    hasIncome = (dayData?.totalIncome ?: 0) > 0,
                                    hasExpense = (dayData?.totalExpense ?: 0) > 0,
                                    onClick = { onDateSelected(day) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Ô ngày trong lịch
 */
@Composable
private fun DayCell(
    day: Int,
    isToday: Boolean,
    isSelected: Boolean,
    hasIncome: Boolean,
    hasExpense: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isToday -> MaterialTheme.colorScheme.primaryContainer
                    else -> Color.Transparent
                }
            )
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
            color = when {
                isSelected -> MaterialTheme.colorScheme.onPrimary
                isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
        
        // Indicators for income/expense
        if (hasIncome || hasExpense) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (hasIncome) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) Color.White else IncomeGreen)
                    )
                }
                if (hasExpense) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) Color.White else ExpenseRed)
                    )
                }
            }
        }
    }
}

/**
 * Item giao dịch
 */
@Composable
private fun TransactionItem(transaction: Transaction) {
    val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
    val timeFormat = SimpleDateFormat("HH:mm", Locale("vi", "VN"))
    val isExpense = transaction.type == TransactionType.EXPENSE
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category icon
            val category = transaction.category
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        category?.getColor()?.copy(alpha = 0.2f)
                            ?: if (isExpense) ExpenseRed.copy(alpha = 0.2f) else IncomeGreen.copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category?.getIcon() ?: if (isExpense) Icons.Default.TrendingDown else Icons.Default.TrendingUp,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = category?.getColor() ?: if (isExpense) ExpenseRed else IncomeGreen
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Description
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.description ?: transaction.category?.displayName ?: transaction.source.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = timeFormat.format(Date(transaction.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Amount
            Text(
                text = "${if (isExpense) "-" else "+"}${formatter.format(transaction.amount)}đ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isExpense) ExpenseRed else IncomeGreen
            )
        }
    }
}

/**
 * Card khi không có giao dịch trong ngày được chọn
 */
@Composable
private fun EmptyDayCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.EventBusy,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Không có giao dịch",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
