package com.finly.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity Ngân sách - đặt hạn mức chi tiêu theo danh mục/tháng
 */
@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /** Danh mục áp dụng ngân sách */
    val category: TransactionCategory,
    
    /** Số tiền hạn mức (VND) */
    val amount: Long,
    
    /** Tháng (1-12) */
    val month: Int,
    
    /** Năm */
    val year: Int,
    
    /** Thời gian tạo */
    val createdAt: Long = System.currentTimeMillis()
)
