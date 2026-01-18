package com.finly.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Nguồn giao dịch - các ứng dụng ngân hàng/ví điện tử được hỗ trợ
 */
enum class TransactionSource(val displayName: String, val packageName: String) {
    TECHCOMBANK("Techcombank", "com.techcombank.bb.app"),
    VIETINBANK("VietinBank", "com.vietinbank.ipay"),
    TIMO("Timo", "vn.timo"),
    CAKE("Cake", "com.vp.cake"),
    MOMO("MoMo", "com.mservice.momotransfer"),
    ZALOPAY("ZaloPay", "vn.com.vng.zalopay"),
    MANUAL("Thủ công", "manual"),
    UNKNOWN("Không xác định", "");

    companion object {
        /**
         * Tìm nguồn giao dịch từ package name
         */
        fun fromPackageName(packageName: String): TransactionSource {
            return entries.find { it.packageName == packageName } ?: UNKNOWN
        }
    }
}

/**
 * Loại giao dịch
 */
enum class TransactionType {
    INCOME,   // Thu nhập (tiền vào)
    EXPENSE   // Chi tiêu (tiền ra)
}

/**
 * Entity đại diện cho một giao dịch tài chính
 * Lưu trữ trong Room database
 */
@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["source"]),
        Index(value = ["rawTextHash"], unique = true) // Ngăn chặn giao dịch trùng lặp
    ]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /** Nguồn giao dịch (Techcombank, MoMo, etc.) */
    val source: TransactionSource,
    
    /** Loại giao dịch (thu/chi) */
    val type: TransactionType,
    
    /** Số tiền giao dịch (VND), luôn dương */
    val amount: Long,
    
    /** Số dư sau giao dịch (nếu có trong thông báo) */
    val balance: Long? = null,
    
    /** Thời điểm giao dịch được ghi nhận */
    val timestamp: Long = System.currentTimeMillis(),
    
    /** Nội dung thông báo gốc (để debug) */
    val rawText: String,
    
    /** Hash của rawText để kiểm tra trùng lặp */
    val rawTextHash: String,
    
    /** Mô tả giao dịch (nếu parse được) */
    val description: String? = null,
    
    /** Danh mục giao dịch (chỉ dùng cho giao dịch thủ công) */
    val category: TransactionCategory? = null,
    
    /** ID danh mục tùy chỉnh (nếu người dùng tạo danh mục riêng) */
    val customCategoryId: Long? = null
)
