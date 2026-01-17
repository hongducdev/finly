package com.finly.parser

import com.finly.data.local.entity.TransactionSource
import com.finly.data.local.entity.TransactionType

/**
 * Parser cho Techcombank
 * Package: com.techcombank.bb.app
 * 
 * Ví dụ thông báo:
 * - "Tài khoản 19xxxxxx78 -500,000 VND. Số dư: 1,234,567 VND. Nội dung: Thanh toan hoa don"
 * - "Tài khoản 19xxxxxx78 +1,000,000 VND. Số dư: 2,234,567 VND. Chuyển khoản từ..."
 */
class TechcombankParser : BaseTransactionParser() {
    
    override val source = TransactionSource.TECHCOMBANK
    
    // Pattern cho giao dịch Techcombank
    // Ví dụ: "-500,000 VND" hoặc "+1,000,000 VND"
    private val tcbAmountPattern = Regex(
        """([+\-])[\s]*([\d.,]+)\s*(?:VND|VNĐ|đ)""",
        RegexOption.IGNORE_CASE
    )
    
    // Pattern cho số dư
    private val tcbBalancePattern = Regex(
        """[Ss]ố dư[:\s]*([\d.,]+)\s*(?:VND|VNĐ|đ)?""",
        RegexOption.IGNORE_CASE
    )
    
    // Pattern cho nội dung
    private val tcbDescPattern = Regex(
        """[Nn]ội dung[:\s]*(.+?)(?:\.|$)""",
        RegexOption.IGNORE_CASE
    )
    
    override fun isTransactionNotification(title: String?, content: String?): Boolean {
        if (content.isNullOrBlank()) return false
        
        // Bỏ qua thông báo OTP, quảng cáo
        if (containsIgnoreKeyword(content)) return false
        
        // Kiểm tra có chứa pattern số tiền không
        val hasAmount = tcbAmountPattern.containsMatchIn(content)
        val hasAccount = content.contains("tài khoản", ignoreCase = true)
        
        return hasAmount && hasAccount
    }
    
    override fun parse(title: String?, content: String?): ParsedTransaction? {
        if (content.isNullOrBlank()) return null
        
        // Trích xuất số tiền
        val amountMatch = tcbAmountPattern.find(content) ?: return null
        val sign = amountMatch.groupValues[1]
        val amountStr = amountMatch.groupValues[2]
        val amount = normalizeAmount(amountStr) ?: return null
        
        // Xác định loại giao dịch
        val type = if (sign == "-") TransactionType.EXPENSE else TransactionType.INCOME
        
        // Trích xuất số dư
        val balanceMatch = tcbBalancePattern.find(content)
        val balance = balanceMatch?.groupValues?.get(1)?.let { normalizeAmount(it) }
        
        // Trích xuất nội dung
        val descMatch = tcbDescPattern.find(content)
        val description = descMatch?.groupValues?.get(1)?.trim()
        
        return ParsedTransaction(
            type = type,
            amount = amount,
            balance = balance,
            description = description
        )
    }
}
