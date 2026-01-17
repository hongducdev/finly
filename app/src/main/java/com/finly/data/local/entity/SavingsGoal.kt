package com.finly.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity Mục tiêu tiết kiệm
 */
@Entity(tableName = "savings_goals")
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /** Tên mục tiêu (VD: "Mua iPhone", "Du lịch Đà Nẵng") */
    val name: String,
    
    /** Số tiền mục tiêu (VND) */
    val targetAmount: Long,
    
    /** Số tiền đã tiết kiệm (VND) */
    val currentAmount: Long = 0,
    
    /** Icon (emoji) */
    val icon: String = "🎯",
    
    /** Màu sắc (hex) */
    val color: Long = 0xFF5C9EAD,
    
    /** Ngày dự kiến đạt mục tiêu (timestamp, nullable) */
    val targetDate: Long? = null,
    
    /** Thời gian tạo */
    val createdAt: Long = System.currentTimeMillis(),
    
    /** Đã hoàn thành chưa */
    val isCompleted: Boolean = false
)
