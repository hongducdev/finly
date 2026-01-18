package com.finly.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.finly.data.local.entity.TransactionType

/**
 * Custom Category - Danh mục tùy chỉnh do người dùng tạo
 */
@Entity(tableName = "custom_categories")
data class CustomCategory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val name: String,
    
    val iconName: String, // Tên icon từ Material Icons
    
    val type: TransactionType, // INCOME hoặc EXPENSE
    
    val color: String // Mã màu hex, ví dụ: "#FFB6C1"
)
