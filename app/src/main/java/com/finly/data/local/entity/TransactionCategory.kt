package com.finly.data.local.entity

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Danh mục giao dịch - cho phép chọn nhanh khi nhập thủ công
 */
enum class TransactionCategory(
    val displayName: String,
    val type: TransactionType
) {
    // Chi tiêu
    FOOD("Ăn uống", TransactionType.EXPENSE),
    SHOPPING("Mua sắm", TransactionType.EXPENSE),
    TRANSPORT("Di chuyển", TransactionType.EXPENSE),
    ENTERTAINMENT("Giải trí", TransactionType.EXPENSE),
    BILLS("Hóa đơn", TransactionType.EXPENSE),
    HEALTH("Sức khỏe", TransactionType.EXPENSE),
    EDUCATION("Học tập", TransactionType.EXPENSE),
    OTHER_EXPENSE("Khác", TransactionType.EXPENSE),
    
    // Thu nhập
    SALARY("Lương", TransactionType.INCOME),
    INVESTMENT("Đầu tư", TransactionType.INCOME),
    SIDEJOB("Làm thêm", TransactionType.INCOME),
    GIFT("Quà tặng", TransactionType.INCOME),
    OTHER_INCOME("Khác", TransactionType.INCOME);
    
    companion object {
        /**
         * Lấy danh sách danh mục theo loại giao dịch
         */
        fun getByType(type: TransactionType): List<TransactionCategory> {
            return entries.filter { it.type == type }
        }
    }
}

/**
 * Extension function để lấy icon cho mỗi danh mục
 */
fun TransactionCategory.getIcon(): ImageVector {
    return when (this) {
        // Chi tiêu
        TransactionCategory.FOOD -> Icons.Default.Fastfood
        TransactionCategory.SHOPPING -> Icons.Default.ShoppingCart
        TransactionCategory.TRANSPORT -> Icons.Default.DirectionsCar
        TransactionCategory.ENTERTAINMENT -> Icons.Default.Movie
        TransactionCategory.BILLS -> Icons.Default.Receipt
        TransactionCategory.HEALTH -> Icons.Default.Favorite
        TransactionCategory.EDUCATION -> Icons.Default.School
        TransactionCategory.OTHER_EXPENSE -> Icons.Default.MoreHoriz
        // Thu nhập
        TransactionCategory.SALARY -> Icons.Default.Work
        TransactionCategory.INVESTMENT -> Icons.Default.TrendingUp
        TransactionCategory.SIDEJOB -> Icons.Default.Build
        TransactionCategory.GIFT -> Icons.Default.Star
        TransactionCategory.OTHER_INCOME -> Icons.Default.MoreHoriz
    }
}

/**
 * Extension function để lấy màu pastel cho mỗi danh mục
 */
fun TransactionCategory.getColor(): Color {
    return when (this) {
        // Chi tiêu - màu pastel đỏ/cam
        TransactionCategory.FOOD -> Color(0xFFFFAB91)        // Pastel coral
        TransactionCategory.SHOPPING -> Color(0xFFF48FB1)    // Pastel pink
        TransactionCategory.TRANSPORT -> Color(0xFFFFCC80)   // Pastel orange
        TransactionCategory.ENTERTAINMENT -> Color(0xFFCE93D8) // Pastel purple
        TransactionCategory.BILLS -> Color(0xFFBCAAA4)       // Pastel brown
        TransactionCategory.HEALTH -> Color(0xFFEF9A9A)      // Pastel red
        TransactionCategory.EDUCATION -> Color(0xFF9FA8DA)   // Pastel indigo
        TransactionCategory.OTHER_EXPENSE -> Color(0xFFB0BEC5) // Pastel grey
        // Thu nhập - màu pastel xanh
        TransactionCategory.SALARY -> Color(0xFFA5D6A7)      // Pastel green
        TransactionCategory.INVESTMENT -> Color(0xFF80DEEA)  // Pastel cyan
        TransactionCategory.SIDEJOB -> Color(0xFFC5E1A5)     // Pastel lime
        TransactionCategory.GIFT -> Color(0xFFFFF59D)        // Pastel yellow
        TransactionCategory.OTHER_INCOME -> Color(0xFF80CBC4) // Pastel teal
    }
}
