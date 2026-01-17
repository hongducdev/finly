package com.finly.parser

import com.finly.data.local.entity.TransactionSource
import com.finly.data.local.entity.TransactionType

/**
 * Parser cho VietinBank iPay
 * Package: com.vietinbank.ipay
 * 
 * Ví dụ thông báo:
 * - "TK 1020xxxxxx52 - 500.000đ lúc 15:30 25/01. SD: 1.234.567đ"
 * - "TK 1020xxxxxx52 + 1.000.000đ lúc 10:00 25/01. SD: 2.234.567đ"
 */
class VietinBankParser : BaseTransactionParser() {
    
    override val source = TransactionSource.VIETINBANK
    
    // Pattern cho giao dịch VietinBank
    private val vtbAmountPattern = Regex(
        """([+\-])\s*([\d.,]+)\s*(?:VND|VNĐ|đ|d)""",
        RegexOption.IGNORE_CASE
    )
    
    // Pattern cho số dư (SD:)
    private val vtbBalancePattern = Regex(
        """SD[:\s]*([\d.,]+)\s*(?:VND|VNĐ|đ|d)?""",
        RegexOption.IGNORE_CASE
    )
    
    override fun isTransactionNotification(title: String?, content: String?): Boolean {
        if (content.isNullOrBlank()) return false
        
        if (containsIgnoreKeyword(content)) return false
        
        val hasAmount = vtbAmountPattern.containsMatchIn(content)
        val hasAccount = content.contains("TK", ignoreCase = true) || 
                         content.contains("tài khoản", ignoreCase = true)
        
        return hasAmount && hasAccount
    }
    
    override fun parse(title: String?, content: String?): ParsedTransaction? {
        if (content.isNullOrBlank()) return null
        
        val amountMatch = vtbAmountPattern.find(content) ?: return null
        val sign = amountMatch.groupValues[1]
        val amountStr = amountMatch.groupValues[2]
        val amount = normalizeAmount(amountStr) ?: return null
        
        val type = if (sign == "-") TransactionType.EXPENSE else TransactionType.INCOME
        
        val balanceMatch = vtbBalancePattern.find(content)
        val balance = balanceMatch?.groupValues?.get(1)?.let { normalizeAmount(it) }
        
        return ParsedTransaction(
            type = type,
            amount = amount,
            balance = balance,
            description = null
        )
    }
}
