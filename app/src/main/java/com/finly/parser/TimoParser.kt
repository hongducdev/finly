package com.finly.parser

import com.finly.data.local.entity.TransactionSource
import com.finly.data.local.entity.TransactionType

/**
 * Parser cho Timo
 * Package: vn.timo
 * 
 * Ví dụ thông báo:
 * - "Bạn vừa chi tiêu 500.000đ tại Shopee. Số dư còn lại: 1.234.567đ"
 * - "Bạn vừa nhận 1.000.000đ từ Nguyễn Văn A. Số dư: 2.234.567đ"
 */
class TimoParser : BaseTransactionParser() {
    
    override val source = TransactionSource.TIMO
    
    // Pattern cho chi tiêu
    private val timoExpensePattern = Regex(
        """(?:chi tiêu|thanh toán|chuyển)\s*([\d.,]+)\s*(?:VND|VNĐ|đ|d)?""",
        RegexOption.IGNORE_CASE
    )
    
    // Pattern cho thu nhập
    private val timoIncomePattern = Regex(
        """(?:nhận|nhận được|được)\s*([\d.,]+)\s*(?:VND|VNĐ|đ|d)?""",
        RegexOption.IGNORE_CASE
    )
    
    // Pattern cho số dư
    private val timoBalancePattern = Regex(
        """[Ss]ố dư[^:]*[:\s]*([\d.,]+)\s*(?:VND|VNĐ|đ|d)?""",
        RegexOption.IGNORE_CASE
    )
    
    override fun isTransactionNotification(title: String?, content: String?): Boolean {
        if (content.isNullOrBlank()) return false
        
        if (containsIgnoreKeyword(content)) return false
        
        return timoExpensePattern.containsMatchIn(content) || 
               timoIncomePattern.containsMatchIn(content)
    }
    
    override fun parse(title: String?, content: String?): ParsedTransaction? {
        if (content.isNullOrBlank()) return null
        
        // Thử match chi tiêu trước
        val expenseMatch = timoExpensePattern.find(content)
        val incomeMatch = timoIncomePattern.find(content)
        
        val (type, amountStr) = when {
            expenseMatch != null -> TransactionType.EXPENSE to expenseMatch.groupValues[1]
            incomeMatch != null -> TransactionType.INCOME to incomeMatch.groupValues[1]
            else -> return null
        }
        
        val amount = normalizeAmount(amountStr) ?: return null
        
        val balanceMatch = timoBalancePattern.find(content)
        val balance = balanceMatch?.groupValues?.get(1)?.let { normalizeAmount(it) }
        
        return ParsedTransaction(
            type = type,
            amount = amount,
            balance = balance,
            description = null
        )
    }
}
