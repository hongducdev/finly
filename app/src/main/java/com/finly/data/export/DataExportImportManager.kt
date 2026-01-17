package com.finly.data.export

import android.content.Context
import android.net.Uri
import com.finly.data.local.entity.Transaction
import com.finly.data.local.entity.TransactionCategory
import com.finly.data.local.entity.TransactionSource
import com.finly.data.local.entity.TransactionType
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

/**
 * Manager để xuất/nhập dữ liệu giao dịch dưới dạng JSON
 */
class DataExportImportManager(private val context: Context) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    /**
     * Xuất danh sách giao dịch thành JSON string
     */
    fun exportToJson(transactions: List<Transaction>): String {
        val jsonArray = JSONArray()
        
        transactions.forEach { transaction ->
            val jsonObject = JSONObject().apply {
                put("id", transaction.id)
                put("source", transaction.source.name)
                put("type", transaction.type.name)
                put("amount", transaction.amount)
                put("balance", transaction.balance ?: JSONObject.NULL)
                put("timestamp", transaction.timestamp)
                put("date", dateFormat.format(Date(transaction.timestamp)))
                put("rawText", transaction.rawText)
                put("rawTextHash", transaction.rawTextHash)
                put("description", transaction.description ?: JSONObject.NULL)
                put("category", transaction.category?.name ?: JSONObject.NULL)
            }
            jsonArray.put(jsonObject)
        }
        
        val rootObject = JSONObject().apply {
            put("exportDate", dateFormat.format(Date()))
            put("version", "1.0")
            put("totalTransactions", transactions.size)
            put("transactions", jsonArray)
        }
        
        return rootObject.toString(2) // Pretty print với indent 2
    }

    /**
     * Ghi JSON ra file
     */
    fun writeToUri(uri: Uri, content: String): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(content.toByteArray(Charsets.UTF_8))
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Đọc JSON từ file và parse thành danh sách Transaction
     */
    fun importFromUri(uri: Uri): ImportResult {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return ImportResult.Error("Không thể mở file")
            
            val reader = BufferedReader(InputStreamReader(inputStream))
            val content = reader.readText()
            reader.close()
            
            parseJsonToTransactions(content)
        } catch (e: Exception) {
            e.printStackTrace()
            ImportResult.Error("Lỗi đọc file: ${e.message}")
        }
    }

    /**
     * Parse JSON string thành danh sách Transaction
     */
    private fun parseJsonToTransactions(jsonString: String): ImportResult {
        return try {
            val rootObject = JSONObject(jsonString)
            val jsonArray = rootObject.getJSONArray("transactions")
            val transactions = mutableListOf<Transaction>()
            
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                
                val source = try {
                    TransactionSource.valueOf(obj.getString("source"))
                } catch (e: Exception) {
                    TransactionSource.MANUAL
                }
                
                val type = try {
                    TransactionType.valueOf(obj.getString("type"))
                } catch (e: Exception) {
                    TransactionType.EXPENSE
                }
                
                val category = try {
                    val categoryName = obj.optString("category", null)
                    if (categoryName != null && categoryName != "null") {
                        TransactionCategory.valueOf(categoryName)
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    null
                }
                
                val transaction = Transaction(
                    id = 0, // Tạo ID mới
                    source = source,
                    type = type,
                    amount = obj.getLong("amount"),
                    balance = if (obj.isNull("balance")) null else obj.getLong("balance"),
                    timestamp = obj.getLong("timestamp"),
                    rawText = obj.getString("rawText"),
                    rawTextHash = obj.getString("rawTextHash") + "_imported_${System.currentTimeMillis()}", // Tạo hash mới tránh trùng
                    description = if (obj.isNull("description")) null else obj.getString("description"),
                    category = category
                )
                
                transactions.add(transaction)
            }
            
            ImportResult.Success(transactions)
        } catch (e: Exception) {
            e.printStackTrace()
            ImportResult.Error("Lỗi parse JSON: ${e.message}")
        }
    }

    /**
     * Kết quả import
     */
    sealed class ImportResult {
        data class Success(val transactions: List<Transaction>) : ImportResult()
        data class Error(val message: String) : ImportResult()
    }
}
