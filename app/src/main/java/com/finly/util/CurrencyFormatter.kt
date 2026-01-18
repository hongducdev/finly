package com.finly.util

import java.text.NumberFormat
import java.util.Locale

/**
 * Utility để format số tiền với khả năng ẩn
 */
object CurrencyFormatter {
    
    private val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    
    /**
     * Format số tiền
     * @param amount Số tiền (VND)
     * @param isHidden Có ẩn số tiền không
     */
    fun format(amount: Long, isHidden: Boolean = false): String {
        return if (isHidden) {
            "***.*** đ"
        } else {
            "${formatter.format(amount)} đ"
        }
    }
    
    /**
     * Format số tiền với dấu +/-
     */
    fun formatWithSign(amount: Long, isHidden: Boolean = false): String {
        return if (isHidden) {
            "***.*** đ"
        } else {
            val sign = if (amount >= 0) "+" else ""
            "$sign${formatter.format(amount)} đ"
        }
    }
}
