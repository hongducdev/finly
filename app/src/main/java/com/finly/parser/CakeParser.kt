package com.finly.parser

import com.finly.data.local.entity.TransactionSource
import com.finly.data.local.entity.TransactionType

/**
 * Parser cho Cake by VPBank
 * Package: com.vp.cake
 * 
 * Ví dụ thông báo:
 * - "Bạn đã chi tiêu 500.000đ. Số dư khả dụng: 1.234.567đ"
 * - "Bạn vừa nhận 1.000.000đ từ NGUYEN VAN A"
 */
class CakeParser : BaseTransactionParser() {
    
    override val source = TransactionSource.CAKE
    
    // Pattern cho chi tiêu
    private val cakeExpensePattern = Regex(
        """(?:chi tiêu|thanh toán|chuyển tiền)\s*([\d.,]+)\s*(?:VND|VNĐ|đ|d)?""",
        RegexOption.IGNORE_CASE
    )
    
    // Pattern cho thu nhập
    private val cakeIncomePattern = Regex(
        """(?:nhận|được)\s*([\d.,]+)\s*(?:VND|VNĐ|đ|d)?""",
        RegexOption.IGNORE_CASE
    )
    
    // Pattern cho số dư
    private val cakeBalancePattern = Regex(
        """[Ss]ố dư[^:]*[:\s]*([\d.,]+)\s*(?:VND|VNĐ|đ|d)?""",
        RegexOption.IGNORE_CASE
    )
    
    override fun isTransactionNotification(title: String?, content: String?): Boolean {
        if (content.isNullOrBlank()) return false
        
        if (containsIgnoreKeyword(content)) return false
        
        return cakeExpensePattern.containsMatchIn(content) || 
               cakeIncomePattern.containsMatchIn(content)
    }
    
    override fun parse(title: String?, content: String?): ParsedTransaction? {
        if (content.isNullOrBlank()) return null
        
        val expenseMatch = cakeExpensePattern.find(content)
        val incomeMatch = cakeIncomePattern.find(content)
        
        val (type, amountStr) = when {
            expenseMatch != null -> TransactionType.EXPENSE to expenseMatch.groupValues[1]
            incomeMatch != null -> TransactionType.INCOME to incomeMatch.groupValues[1]
            else -> return null
        }
        
        val amount = normalizeAmount(amountStr) ?: return null
        
        val balanceMatch = cakeBalancePattern.find(content)
        val balance = balanceMatch?.groupValues?.get(1)?.let { normalizeAmount(it) }
        
        return ParsedTransaction(
            type = type,
            amount = amount,
            balance = balance,
            description = null
        )
    }
}
