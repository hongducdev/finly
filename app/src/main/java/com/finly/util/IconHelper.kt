package com.finly.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Helper để lấy icon từ tên icon
 */
object IconHelper {
    
    private val iconMap = mapOf(
        "Đồ ăn" to Icons.Default.Fastfood,
        "Mua sắm" to Icons.Default.ShoppingCart,
        "Xe cộ" to Icons.Default.DirectionsCar,
        "Giải trí" to Icons.Default.Movie,
        "Hóa đơn" to Icons.Default.Receipt,
        "Sức khỏe" to Icons.Default.Favorite,
        "Học tập" to Icons.Default.School,
        "Nhà" to Icons.Default.Home,
        "Đăng ký" to Icons.Default.Subscriptions,
        "Thú cưng" to Icons.Default.Pets,
        "Làm đẹp" to Icons.Default.Face,
        "Du lịch" to Icons.Default.Flight,
        "Quà" to Icons.Default.CardGiftcard,
        "Cafe" to Icons.Default.Coffee,
        "Nhà hàng" to Icons.Default.Restaurant,
        "Bar" to Icons.Default.LocalBar,
        "Thể thao" to Icons.Default.SportsSoccer,
        "Âm nhạc" to Icons.Default.MusicNote,
        "Sách" to Icons.Default.Book,
        "Công nghệ" to Icons.Default.Laptop,
        "Điện thoại" to Icons.Default.Phone,
        "Internet" to Icons.Default.Wifi,
    )
    
    fun getIconByName(iconName: String): ImageVector? {
        return iconMap[iconName]
    }
}
