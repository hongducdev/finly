package com.finly.parser

import com.finly.data.local.entity.TransactionSource
import com.finly.data.local.entity.TransactionType

/**
 * Parser cho ZaloPay
 * Package: vn.com.vng.zalopay
 * 
 * Ví dụ thông báo:
 * - "Bạn đã thanh toán 500.000đ cho đơn hàng tại Shopee"
 * - "Bạn đã nhận 1.000.000đ từ 0912xxx345"
 * - "Chuyển tiền -500.000đ thành công"
 */
class ZaloPayParser : BaseTransactionParser() {
    
    override val source = TransactionSource.ZALOPAY
    
    // Pattern cho thanh toán
    private val zpExpensePattern = Regex(
        """(?:thanh toán|chuyển tiền|trả|chi)\s*[-]?\s*([\d.,]+)\s*(?:VND|VNĐ|đ|d)?""",
        RegexOption.IGNORE_CASE
    )
    
    // Pattern cho nhận tiền
    private val zpIncomePattern = Regex(
        """(?:nhận|nhận được|được)\s*[+]?\s*([\d.,]+)\s*(?:VND|VNĐ|đ|d)?""",
        RegexOption.IGNORE_CASE
    )
    
    // Pattern phát hiện qua dấu +/-
    private val zpSignPattern = Regex(
        """([+\-])\s*([\d.,]+)\s*(?:VND|VNĐ|đ|d)?""",
        RegexOption.IGNORE_CASE
    )
    
    override fun isTransactionNotification(title: String?, content: String?): Boolean {
        if (content.isNullOrBlank()) return false
        
        if (containsIgnoreKeyword(content)) return false
        
        // Bỏ qua voucher
        if (content.contains("voucher", ignoreCase = true) ||
            content.contains("giảm ngay", ignoreCase = true)) {
            return false
        }
        
        return zpExpensePattern.containsMatchIn(content) || 
               zpIncomePattern.containsMatchIn(content) ||
               zpSignPattern.containsMatchIn(content)
    }
    
    override fun parse(title: String?, content: String?): ParsedTransaction? {
        if (content.isNullOrBlank()) return null
        
        // Ưu tiên pattern có dấu
        val signMatch = zpSignPattern.find(content)
        if (signMatch != null) {
            val sign = signMatch.groupValues[1]
            val amountStr = signMatch.groupValues[2]
            val amount = normalizeAmount(amountStr) ?: return null
            val type = if (sign == "-") TransactionType.EXPENSE else TransactionType.INCOME
            
            return ParsedTransaction(
                type = type,
                amount = amount,
                balance = null,
                description = null
            )
        }
        
        val expenseMatch = zpExpensePattern.find(content)
        val incomeMatch = zpIncomePattern.find(content)
        
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
            description = null
        )
    }
}
