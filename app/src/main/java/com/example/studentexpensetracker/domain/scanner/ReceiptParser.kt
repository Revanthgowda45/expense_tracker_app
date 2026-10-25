package com.example.studentexpensetracker.domain.scanner

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.regex.Pattern

data class ParsedReceipt(
    val amount: Double?,
    val merchantName: String?,
    val dateMillis: Long?
)

object ReceiptParser {
    
    fun parse(text: String): ParsedReceipt {
        return ParsedReceipt(
            amount = extractAmount(text),
            merchantName = extractMerchant(text),
            dateMillis = extractDate(text)
        )
    }

    private fun extractAmount(text: String): Double? {
        // Look for keywords like total, amount, rs, inr, etc.
        val lines = text.split("\n")
        
        // 1. Try finding explicit "Total" lines
        val totalPatterns = listOf(
            "grand total", "total amount", "net amount", "amount payable", "total"
        )
        
        for (line in lines) {
            val lowerLine = line.lowercase(Locale.getDefault())
            if (totalPatterns.any { lowerLine.contains(it) }) {
                // Try to find the number in this line
                val amount = findLargestCurrency(line)
                if (amount != null) return amount
            }
        }
        
        // 2. If no explicit total found, just find the largest currency-looking number
        return findLargestCurrency(text)
    }
    
    private fun findLargestCurrency(text: String): Double? {
        // Match numbers that look like currency (e.g. 1,200.50 or 1200 or 12.0)
        val regex = Regex("""\b(?:rs\.?|inr|₹|\$)?\s*(\d{1,3}(?:,\d{3})*(?:\.\d{1,2})?|\d+(?:\.\d{1,2})?)\b""", RegexOption.IGNORE_CASE)
        
        val matches = regex.findAll(text)
        var maxAmount = 0.0
        var found = false
        
        for (match in matches) {
            val numStr = match.groupValues[1].replace(",", "")
            val num = numStr.toDoubleOrNull()
            if (num != null) {
                found = true
                if (num > maxAmount) {
                    // Try to avoid extremely large parsing errors like phone numbers, pins (assume < 500000)
                    if (num < 500000) {
                        maxAmount = num
                    }
                }
            }
        }
        
        return if (found) maxAmount else null
    }

    private fun extractMerchant(text: String): String? {
        val lines = text.split("\n").map { it.trim() }.filter { it.isNotBlank() }
        
        if (lines.isEmpty()) return null
        
        // Usually the first or second line of a receipt is the merchant name
        // Filter out lines that look like addresses or dates
        for (line in lines.take(3)) {
            // Ignore if it's purely numbers, or contains date/time characters or address keywords
            val lowerLine = line.lowercase()
            if (lowerLine.contains("date") || lowerLine.contains("time") || 
                lowerLine.contains("address") || lowerLine.contains("ph:") ||
                line.matches(Regex("^[0-9\\-/:\\s]+$"))) {
                continue
            }
            
            // Clean up common OCR noise
            var merchant = line
            if (merchant.length > 3) {
                return merchant
            }
        }
        
        return null
    }

    private fun extractDate(text: String): Long? {
        val lines = text.split("\n")
        
        val datePatterns = listOf(
            Regex("""\b(\d{1,2})[/-](\d{1,2})[/-](\d{2,4})\b"""), // dd/MM/yyyy or dd-MM-yyyy
            Regex("""\b(\d{1,2})\s+(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z]*\s+(\d{2,4})\b""", RegexOption.IGNORE_CASE) // 12 Jan 2023
        )
        
        for (line in lines) {
            for (pattern in datePatterns) {
                val match = pattern.find(line)
                if (match != null) {
                    val dateStr = match.value
                    try {
                        // Try parsing with common formats
                        val formats = listOf("dd/MM/yyyy", "dd-MM-yyyy", "dd/MM/yy", "dd-MM-yy", "dd MMM yyyy", "dd MMMM yyyy")
                        for (format in formats) {
                            try {
                                val sdf = SimpleDateFormat(format, Locale.getDefault())
                                sdf.isLenient = false
                                val date = sdf.parse(dateStr)
                                if (date != null) return date.time
                            } catch (e: Exception) {
                                // Ignore and try next format
                            }
                        }
                    } catch (e: Exception) {
                        // ignore
                    }
                }
            }
        }
        
        return null
    }
}
