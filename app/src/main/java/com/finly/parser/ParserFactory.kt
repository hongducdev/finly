package com.finly.parser

import com.finly.data.local.entity.TransactionSource

/**
 * Factory để tạo parser phù hợp dựa trên package name
 */
class ParserFactory {
    
    private val parsers: Map<String, TransactionParser> = mapOf(
        TransactionSource.TECHCOMBANK.packageName to TechcombankParser(),
        TransactionSource.VIETINBANK.packageName to VietinBankParser(),
        TransactionSource.TIMO.packageName to TimoParser(),
        TransactionSource.CAKE.packageName to CakeParser(),
        TransactionSource.MOMO.packageName to MoMoParser(),
        TransactionSource.ZALOPAY.packageName to ZaloPayParser()
    )
    
    /**
     * Lấy parser phù hợp cho package
     * @return Parser nếu hỗ trợ, null nếu không
     */
    fun getParser(packageName: String): TransactionParser? {
        return parsers[packageName]
    }
    
    /**
     * Kiểm tra có hỗ trợ package không
     */
    fun isSupported(packageName: String): Boolean {
        return parsers.containsKey(packageName)
    }
    
    /**
     * Lấy danh sách tất cả package được hỗ trợ
     */
    fun getSupportedPackages(): Set<String> {
        return parsers.keys
    }
}
