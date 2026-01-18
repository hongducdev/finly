package com.finly.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.finly.ui.viewmodel.CategoryStat
import com.finly.ui.viewmodel.StatisticsViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("vi", "VN"))
    val formatter = NumberFormat.getInstance(Locale("vi", "VN"))

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            MonthNavigation(
                currentMonth = uiState.currentMonth,
                monthFormat = monthFormat,
                onPreviousMonth = { viewModel.previousMonth() },
                onNextMonth = { viewModel.nextMonth() }
            )
        }

        item {
            TypeSelector(
                selectedType = uiState.selectedType,
                onTypeSelected = { viewModel.switchType(it) }
            )
        }

        item {
            if (uiState.categoryStats.isNotEmpty()) {
                PieChartCard(
                    stats = uiState.categoryStats,
                    totalAmount = uiState.totalAmount,
                    isExpense = uiState.selectedType == TransactionType.EXPENSE,
                    formatter = formatter
                )
            } else if (!uiState.isLoading) {
                EmptyStatsCard(isExpense = uiState.selectedType == TransactionType.EXPENSE)
            }
        }

        if (uiState.categoryStats.isNotEmpty()) {
            item {
                Text(
                    text = "Chi tiết theo danh mục",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(uiState.categoryStats) { stat ->
                CategoryStatItem(
                    stat = stat,
                    formatter = formatter
                )
            }
        }
    }
}


@Composable
private fun MonthNavigation(
    currentMonth: Calendar,
    monthFormat: SimpleDateFormat,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
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
    }
}


@Composable
private fun TypeSelector(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FilterChip(
            selected = selectedType == TransactionType.EXPENSE,
            onClick = { onTypeSelected(TransactionType.EXPENSE) },
            label = { Text("Chi tiêu") },
            leadingIcon = {
                Icon(
                    Icons.Default.TrendingDown,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = ExpenseRed.copy(alpha = 0.2f),
                selectedLabelColor = ExpenseRed,
                selectedLeadingIconColor = ExpenseRed
            ),
            modifier = Modifier.weight(1f)
        )
        
        FilterChip(
            selected = selectedType == TransactionType.INCOME,
            onClick = { onTypeSelected(TransactionType.INCOME) },
            label = { Text("Thu nhập") },
            leadingIcon = {
                Icon(
                    Icons.Default.TrendingUp,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = IncomeGreen.copy(alpha = 0.2f),
                selectedLabelColor = IncomeGreen,
                selectedLeadingIconColor = IncomeGreen
            ),
            modifier = Modifier.weight(1f)
        )
    }
}


@Composable
private fun PieChartCard(
    stats: List<CategoryStat>,
    totalAmount: Long,
    isExpense: Boolean,
    formatter: NumberFormat
) {
    val accentColor = if (isExpense) ExpenseRed else IncomeGreen
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                PieChart(
                    stats = stats,
                    modifier = Modifier.fillMaxSize()
                )
                

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Tổng",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${formatter.format(totalAmount)}đ",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            

            stats.take(5).chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    row.forEach { stat ->
                        LegendItem(
                            category = stat.category,
                            percentage = stat.percentage
                        )
                    }

                    if (row.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}


@Composable
private fun PieChart(
    stats: List<CategoryStat>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.padding(16.dp)) {
        val total = stats.sumOf { it.amount }
        if (total == 0L) return@Canvas
        
        var startAngle = -90f
        val strokeWidth = 40f
        val radius = (size.minDimension - strokeWidth) / 2
        val center = Offset(size.width / 2, size.height / 2)
        
        stats.forEach { stat ->
            val sweepAngle = (stat.amount.toFloat() / total) * 360f
            
            drawArc(
                color = stat.category.getColor(),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth)
            )
            
            startAngle += sweepAngle
        }
    }
}


@Composable
private fun LegendItem(
    category: TransactionCategory,
    percentage: Float
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(category.getColor())
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "${category.displayName} (${String.format("%.0f", percentage)}%)",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Composable
private fun CategoryStatItem(
    stat: CategoryStat,
    formatter: NumberFormat
) {
    val categoryColor = stat.category.getColor()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = categoryColor.copy(alpha = 0.1f)
        )
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
                    .background(categoryColor.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = stat.category.getIcon(),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = categoryColor
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stat.category.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${stat.count} giao dịch",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${formatter.format(stat.amount)}đ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = categoryColor
                )
                Text(
                    text = "${String.format("%.1f", stat.percentage)}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        

        LinearProgressIndicator(
            progress = { stat.percentage / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = categoryColor,
            trackColor = categoryColor.copy(alpha = 0.2f)
        )
    }
}


@Composable
private fun EmptyStatsCard(isExpense: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (isExpense) Icons.Default.TrendingDown else Icons.Default.TrendingUp,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isExpense) "Chưa có chi tiêu nào" else "Chưa có thu nhập nào",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "trong tháng này",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
