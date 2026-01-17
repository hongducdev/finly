package com.finly.parser

import com.finly.data.local.entity.TransactionSource
import com.finly.data.local.entity.TransactionType

/**
 * Kết quả parse thông báo
 */
data class ParsedTransaction(
    /** Loại giao dịch (thu/chi) */
    val type: TransactionType,
    
    /** Số tiền giao dịch (VND), luôn dương */
    val amount: Long,
    
    /** Số dư sau giao dịch (nếu có) */
    val balance: Long? = null,
    
    /** Mô tả giao dịch */
    val description: String? = null
)

/**
 * Interface cho các parser thông báo giao dịch
 * Sử dụng Strategy Pattern - mỗi ngân hàng/ví có parser riêng
 */
interface TransactionParser {
    
    /** Nguồn giao dịch mà parser này xử lý */
    val source: TransactionSource
    
    /**
     * Kiểm tra xem thông báo có phải là giao dịch không
     * Lọc bỏ OTP, quảng cáo, khuyến mãi
     * 
     * @param title Tiêu đề thông báo
     * @param content Nội dung thông báo
     * @return true nếu là thông báo giao dịch
     */
    fun isTransactionNotification(title: String?, content: String?): Boolean
    
    /**
     * Parse thông báo để trích xuất thông tin giao dịch
     * 
     * @param title Tiêu đề thông báo
     * @param content Nội dung thông báo
     * @return ParsedTransaction nếu parse thành công, null nếu không
     */
    fun parse(title: String?, content: String?): ParsedTransaction?
}

/**
 * Base class cho các parser
 * Cung cấp các utility functions chung
 */
abstract class BaseTransactionParser : TransactionParser {
    
    companion object {
        // Các từ khóa để lọc thông báo không phải giao dịch
        private val IGNORE_KEYWORDS = listOf(
            "otp", "mã xác thực", "mã xác nhận", "verification",
            "khuyến mãi", "ưu đãi", "giảm giá", "voucher", "coupon",
            "quảng cáo", "marketing", "promotion",
            "đăng nhập", "login", "password", "mật khẩu",
            "cập nhật", "update", "nâng cấp"
        )
        
        // Pattern để trích xuất số tiền (VND)
        // Ví dụ: "1.000.000 VND", "1,000,000đ", "-500.000", "+1.500.000"
        val AMOUNT_PATTERN = Regex(
            """([+\-]?)[\s]*(\d{1,3}(?:[.,]\d{3})*)\s*(?:VND|VNĐ|đ|d)?""",
            RegexOption.IGNORE_CASE
        )
        
        // Pattern để trích xuất số dư
        val BALANCE_PATTERN = Regex(
            """(?:số dư|sd|balance)[:\s]*(\d{1,3}(?:[.,]\d{3})*)\s*(?:VND|VNĐ|đ|d)?""",
            RegexOption.IGNORE_CASE
        )
    }
    
    /**
     * Kiểm tra thông báo có chứa từ khóa cần bỏ qua không
     */
    protected fun containsIgnoreKeyword(text: String): Boolean {
        val lowerText = text.lowercase()
        return IGNORE_KEYWORDS.any { lowerText.contains(it) }
    }
    
    /**
     * Trích xuất số tiền từ chuỗi
     * Chuyển đổi sang Long (VND)
     */
    protected fun extractAmount(text: String): Pair<Long, Boolean>? {
        val match = AMOUNT_PATTERN.find(text) ?: return null
        val sign = match.groupValues[1]
        val amountStr = match.groupValues[2]
            .replace(".", "")
            .replace(",", "")
        
        val amount = amountStr.toLongOrNull() ?: return null
        val isNegative = sign == "-"
        
        return Pair(amount, isNegative)
    }
    
    /**
     * Trích xuất số dư từ chuỗi
     */
    protected fun extractBalance(text: String): Long? {
        val match = BALANCE_PATTERN.find(text) ?: return null
        val balanceStr = match.groupValues[1]
            .replace(".", "")
            .replace(",", "")
        
        return balanceStr.toLongOrNull()
    }
    
    /**
     * Chuẩn hóa số tiền - loại bỏ dấu phân cách
     */
    protected fun normalizeAmount(amountStr: String): Long? {
        val cleaned = amountStr
            .replace(".", "")
            .replace(",", "")
            .replace(" ", "")
            .replace("VND", "", ignoreCase = true)
            .replace("VNĐ", "", ignoreCase = true)
            .replace("đ", "", ignoreCase = true)
            .replace("d", "", ignoreCase = true)
            .trim()
        
        return cleaned.toLongOrNull()
    }
}
