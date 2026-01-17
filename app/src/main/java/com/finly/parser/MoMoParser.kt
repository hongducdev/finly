package com.finly.parser

import com.finly.data.local.entity.TransactionSource
import com.finly.data.local.entity.TransactionType

/**
 * Parser cho MoMo
 * Package: com.mservice.momotransfer
 * 
 * Ví dụ thông báo:
 * - "Bạn đã thanh toán 500.000đ cho đơn hàng #12345"
 * - "Bạn đã nhận 1.000.000đ từ số điện thoại 0912xxx345"
 * - "Chuyển tiền thành công -500.000đ cho 0912xxx345"
 */
class MoMoParser : BaseTransactionParser() {
    
    override val source = TransactionSource.MOMO
    
    // Pattern cho thanh toán/chuyển tiền
    private val momoExpensePattern = Regex(
        """(?:thanh toán|chuyển tiền|chuyển|trừ)\s*[-]?\s*([\d.,]+)\s*(?:VND|VNĐ|đ|d)?""",
        RegexOption.IGNORE_CASE
    )
    
    // Pattern cho nhận tiền
    private val momoIncomePattern = Regex(
        """(?:nhận|nhận được|được nhận)\s*[+]?\s*([\d.,]+)\s*(?:VND|VNĐ|đ|d)?""",
        RegexOption.IGNORE_CASE
    )
    
    // Pattern phát hiện giao dịch qua dấu +/-
    private val momoSignPattern = Regex(
        """([+\-])\s*([\d.,]+)\s*(?:VND|VNĐ|đ|d)?""",
        RegexOption.IGNORE_CASE
    )
    
    override fun isTransactionNotification(title: String?, content: String?): Boolean {
        if (content.isNullOrBlank()) return false
        
        if (containsIgnoreKeyword(content)) return false
        
        // Bỏ qua thông báo voucher
        if (content.contains("voucher", ignoreCase = true) ||
            content.contains("giảm giá", ignoreCase = true)) {
            return false
        }
        
        return momoExpensePattern.containsMatchIn(content) || 
               momoIncomePattern.containsMatchIn(content) ||
               momoSignPattern.containsMatchIn(content)
    }
    
    override fun parse(title: String?, content: String?): ParsedTransaction? {
        if (content.isNullOrBlank()) return null
        
        // Ưu tiên pattern có dấu +/-
        val signMatch = momoSignPattern.find(content)
        if (signMatch != null) {
            val sign = signMatch.groupValues[1]
            val amountStr = signMatch.groupValues[2]
            val amount = normalizeAmount(amountStr) ?: return null
            val type = if (sign == "-") TransactionType.EXPENSE else TransactionType.INCOME
            
            return ParsedTransaction(
                type = type,
                amount = amount,
                balance = null,
                description = extractDescription(content)
            )
        }
        
        // Thử các pattern khác
        val expenseMatch = momoExpensePattern.find(content)
        val incomeMatch = momoIncomePattern.find(content)
        
        val (type, amountStr) = when {
            expenseMatch != null -> TransactionType.EXPENSE to expenseMatch.groupValues[1]
            incomeMatch != null -> TransactionType.INCOME to incomeMatch.groupValues[1]
            else -> return null
        }
        
        val amount = normalizeAmount(amountStr) ?: return null
        
        return ParsedTransaction(
            type = type,
            amount = amount,
            balance = null,
            description = extractDescription(content)
        )
    }
    
    private fun extractDescription(content: String): String? {
        // Trích xuất mô tả từ "cho" hoặc "từ"
        val forPattern = Regex("""(?:cho|tới)\s+(.+?)(?:\.|$)""", RegexOption.IGNORE_CASE)
        val fromPattern = Regex("""(?:từ)\s+(.+?)(?:\.|$)""", RegexOption.IGNORE_CASE)
        
        val forMatch = forPattern.find(content)
        val fromMatch = fromPattern.find(content)
        
        return (forMatch ?: fromMatch)?.groupValues?.get(1)?.trim()
    }
}
