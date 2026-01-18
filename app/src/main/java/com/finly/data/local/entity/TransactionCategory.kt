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
    HOUSING("Nhà cửa", TransactionType.EXPENSE),
    SUBSCRIPTIONS("Đăng ký", TransactionType.EXPENSE),
    PETS("Thú cưng", TransactionType.EXPENSE),
    BEAUTY("Làm đẹp", TransactionType.EXPENSE),
    TRAVEL("Du lịch", TransactionType.EXPENSE),
    GIFTS_GIVEN("Quà tặng", TransactionType.EXPENSE),
    
    // Thu nhập
    SALARY("Lương", TransactionType.INCOME),
    BONUS("Thưởng", TransactionType.INCOME),
    INVESTMENT("Đầu tư", TransactionType.INCOME),
    SIDEJOB("Làm thêm", TransactionType.INCOME),
    FREELANCE("Freelance", TransactionType.INCOME),
    GIFT("Quà tặng", TransactionType.INCOME),
    REFUND("Hoàn tiền", TransactionType.INCOME),
    RENTAL("Cho thuê", TransactionType.INCOME);
    
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
        TransactionCategory.HOUSING -> Icons.Default.Home
        TransactionCategory.SUBSCRIPTIONS -> Icons.Default.Subscriptions
        TransactionCategory.PETS -> Icons.Default.Pets
        TransactionCategory.BEAUTY -> Icons.Default.Face
        TransactionCategory.TRAVEL -> Icons.Default.Flight
        TransactionCategory.GIFTS_GIVEN -> Icons.Default.CardGiftcard
        // Thu nhập
        TransactionCategory.SALARY -> Icons.Default.Work
        TransactionCategory.BONUS -> Icons.Default.MonetizationOn
        TransactionCategory.INVESTMENT -> Icons.Default.TrendingUp
        TransactionCategory.SIDEJOB -> Icons.Default.Build
        TransactionCategory.FREELANCE -> Icons.Default.Laptop
        TransactionCategory.GIFT -> Icons.Default.Star
        TransactionCategory.REFUND -> Icons.Default.Refresh
        TransactionCategory.RENTAL -> Icons.Default.Key
    }
}

/**
 * Extension function để lấy màu pastel cho mỗi danh mục
 */
fun TransactionCategory.getColor(): Color {
    return when (this) {
        // Chi tiêu - màu pastel đỏ/cam/tím
        TransactionCategory.FOOD -> Color(0xFFFFAB91)        // Pastel coral
        TransactionCategory.SHOPPING -> Color(0xFFF48FB1)    // Pastel pink
        TransactionCategory.TRANSPORT -> Color(0xFFFFCC80)   // Pastel orange
        TransactionCategory.ENTERTAINMENT -> Color(0xFFCE93D8) // Pastel purple
        TransactionCategory.BILLS -> Color(0xFFBCAAA4)       // Pastel brown
        TransactionCategory.HEALTH -> Color(0xFFEF9A9A)      // Pastel red
        TransactionCategory.EDUCATION -> Color(0xFF9FA8DA)   // Pastel indigo
        TransactionCategory.HOUSING -> Color(0xFFFFCC80)     // Pastel deep orange
        TransactionCategory.SUBSCRIPTIONS -> Color(0xFFB39DDB) // Pastel deep purple
        TransactionCategory.PETS -> Color(0xFFFFF59D)        // Pastel yellow
        TransactionCategory.BEAUTY -> Color(0xFFF8BBD0)      // Pastel light pink
        TransactionCategory.TRAVEL -> Color(0xFF81D4FA)      // Pastel light blue
        TransactionCategory.GIFTS_GIVEN -> Color(0xFFFFCCBC) // Pastel deep orange light
        // Thu nhập - màu pastel xanh/vàng
        TransactionCategory.SALARY -> Color(0xFFA5D6A7)      // Pastel green
        TransactionCategory.BONUS -> Color(0xFFFFE082)       // Pastel amber
        TransactionCategory.INVESTMENT -> Color(0xFF80DEEA)  // Pastel cyan
        TransactionCategory.SIDEJOB -> Color(0xFFC5E1A5)     // Pastel lime
        TransactionCategory.FREELANCE -> Color(0xFFB2DFDB)   // Pastel teal light
        TransactionCategory.GIFT -> Color(0xFFFFF59D)        // Pastel yellow
        TransactionCategory.REFUND -> Color(0xFF90CAF9)      // Pastel blue
        TransactionCategory.RENTAL -> Color(0xFFA5D6A7)      // Pastel green light
    }
}
