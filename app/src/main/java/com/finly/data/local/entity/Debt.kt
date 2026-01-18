package com.finly.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity theo dõi các khoản nợ cá nhân
 */
@Entity(tableName = "debts")
data class Debt(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val type: DebtType,              // LEND (cho vay) hoặc BORROW (đi vay)
    val personName: String,          // Tên người
    val amount: Long,                // Số tiền (VND)
    val description: String? = null, // Mô tả
    val dueDate: Long,               // Ngày hẹn trả (timestamp)
    val createdDate: Long = System.currentTimeMillis(), // Ngày tạo
    val isPaid: Boolean = false,     // Đã trả chưa
    val paidDate: Long? = null       // Ngày trả (nếu đã trả)
)
