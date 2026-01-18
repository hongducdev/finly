package com.finly.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finly.data.local.entity.TransactionType
import com.finly.ui.viewmodel.CustomCategoryCreatorViewModel

/**
 * Màn hình tạo danh mục tùy chỉnh
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomCategoryCreatorScreen(
    transactionType: TransactionType,
    viewModel: CustomCategoryCreatorViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onCategoryCreated: (categoryId: Long) -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(Unit) {
        viewModel.setTransactionType(transactionType)
    }
    
    LaunchedEffect(uiState.createdCategoryId) {
        uiState.createdCategoryId?.let {
            Toast.makeText(context, "Đã tạo danh mục mới", Toast.LENGTH_SHORT).show()
            onCategoryCreated(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Tạo danh mục mới", 
                        fontWeight = FontWeight.Bold
                    ) 
                },
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Preview Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = uiState.selectedColor.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Xem trước",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(uiState.selectedColor),
                        contentAlignment = Alignment.Center
                    ) {
                        uiState.selectedIcon?.let {
                            Icon(
                                imageVector = it,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = uiState.categoryName.ifBlank { "Tên danh mục" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Category Name Input
            Column {
                Text(
                    text = "Tên danh mục",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = uiState.categoryName,
                    onValueChange = { viewModel.updateName(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ví dụ: Cafe, Xăng xe...") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Color Picker
            Column {
                Text(
                    text = "Chọn màu",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                val colors = listOf(
                    Color(0xFFFFAB91), Color(0xFFF48FB1), Color(0xFFFFCC80),
                    Color(0xFFCE93D8), Color(0xFFBCAAA4), Color(0xFFEF9A9A),
                    Color(0xFF9FA8DA), Color(0xFFFFF59D), Color(0xFFF8BBD0),
                    Color(0xFF81D4FA), Color(0xFFA5D6A7), Color(0xFFFFE082),
                    Color(0xFF80DEEA), Color(0xFFC5E1A5), Color(0xFFB39DDB),
                    Color(0xFF90CAF9), Color(0xFFB2DFDB), Color(0xFFFFCCBC)
                )
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    modifier = Modifier.height(200.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    userScrollEnabled = false
                ) {
                    items(colors) { color ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { viewModel.selectColor(color) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.selectedColor == color) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Icon Picker
            Column {
                Text(
                    text = "Chọn biểu tượng",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                val icons = listOf(
                    Icons.Default.Fastfood to "Đồ ăn",
                    Icons.Default.ShoppingCart to "Mua sắm",
                    Icons.Default.DirectionsCar to "Xe cộ",
                    Icons.Default.Movie to "Giải trí",
                    Icons.Default.Receipt to "Hóa đơn",
                    Icons.Default.Favorite to "Sức khỏe",
                    Icons.Default.School to "Học tập",
                    Icons.Default.Home to "Nhà",
                    Icons.Default.Subscriptions to "Đăng ký",
                    Icons.Default.Pets to "Thú cưng",
                    Icons.Default.Face to "Làm đẹp",
                    Icons.Default.Flight to "Du lịch",
                    Icons.Default.CardGiftcard to "Quà",
                    Icons.Default.Coffee to "Cafe",
                    Icons.Default.Restaurant to "Nhà hàng",
                    Icons.Default.LocalBar to "Bar",
                    Icons.Default.SportsSoccer to "Thể thao",
                    Icons.Default.MusicNote to "Âm nhạc",
                    Icons.Default.Book to "Sách",
                    Icons.Default.Laptop to "Công nghệ",
                    Icons.Default.Phone to "Điện thoại",
                    Icons.Default.Wifi to "Internet",
                )
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    modifier = Modifier.height(300.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(icons) { (icon, name) ->
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (uiState.selectedIcon == icon) 
                                        MaterialTheme.colorScheme.primaryContainer
                                    else 
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .clickable { viewModel.selectIcon(icon, name) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = name,
                                tint = if (uiState.selectedIcon == icon)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Create Button
            Button(
                onClick = { viewModel.createCategory() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !uiState.isLoading && 
                         uiState.categoryName.isNotBlank() && 
                         uiState.selectedIcon != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = "Tạo danh mục",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
