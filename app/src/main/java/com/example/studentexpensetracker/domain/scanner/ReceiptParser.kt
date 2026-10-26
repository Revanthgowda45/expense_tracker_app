package com.example.studentexpensetracker.domain.scanner

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class ParsedReceipt(
    val amount: Double?,
    val merchantName: String?,
    val dateMillis: Long?,
    val suggestedCategory: String? = null,
    val taxAmount: Double? = null,
    val rawText: String = "",
    val receiptImagePath: String? = null
)

object ReceiptParser {

    private val KNOWN_MERCHANTS = listOf(
        "Starbucks" to listOf("starbucks"),
        "McDonald's" to listOf("mcdonald", "mcdonalds", "mc donald"),
        "Domino's Pizza" to listOf("domino", "dominos"),
        "Subway" to listOf("subway"),
        "Burger King" to listOf("burger king"),
        "KFC" to listOf("kfc", "kentucky"),
        "Pizza Hut" to listOf("pizza hut"),
        "Cafe Coffee Day" to listOf("coffee day", "ccd"),
        "Chaayos" to listOf("chaayos"),
        "Chai Point" to listOf("chai point"),
        "Haldiram's" to listOf("haldiram"),
        "Zomato" to listOf("zomato"),
        "Swiggy" to listOf("swiggy"),
        "Zepto" to listOf("zepto"),
        "Blinkit" to listOf("blinkit"),
        "Instamart" to listOf("instamart"),
        "DMart" to listOf("dmart", "d-mart", "avenue supermarts"),
        "Reliance Smart" to listOf("reliance smart", "reliance fresh", "reliance retail"),
        "More Supermarket" to listOf("more supermarket", "more retail"),
        "Apollo Pharmacy" to listOf("apollo pharmacy", "apollo"),
        "MedPlus" to listOf("medplus"),
        "Shell" to listOf("shell"),
        "Indian Oil" to listOf("indian oil", "iocl"),
        "Bharat Petroleum" to listOf("bharat petroleum", "bpcl"),
        "HP Petrol" to listOf("hindustan petroleum", "hpcl"),
        "PVR Cinemas" to listOf("pvr", "pvr cinemas"),
        "INOX" to listOf("inox"),
        "Cinepolis" to listOf("cinepolis"),
        "Decathlon" to listOf("decathlon"),
        "Uniqlo" to listOf("uniqlo"),
        "Zara" to listOf("zara"),
        "H&M" to listOf("h&m", "h & m")
    )

    fun parse(text: String, receiptImagePath: String? = null): ParsedReceipt {
        val cleanText = text.trim()
        val amount = extractAmount(cleanText)
        val merchant = extractMerchant(cleanText)
        val date = extractDate(cleanText)
        val category = suggestCategory(cleanText, merchant)
        val tax = extractTax(cleanText)

        return ParsedReceipt(
            amount = amount,
            merchantName = merchant,
            dateMillis = date,
            suggestedCategory = category,
            taxAmount = tax,
            rawText = cleanText,
            receiptImagePath = receiptImagePath
        )
    }

    private fun extractAmount(text: String): Double? {
        val lines = text.split("\n")

        val explicitGrandTotalPatterns = listOf(
            "grand total", "net payable", "amount payable", "total payable",
            "net amount", "final total", "total due", "bill total", "bal due", "balance due"
        )
        val regularTotalPatterns = listOf(
            "total amount", "total:", "total"
        )

        val excludeLineKeywords = listOf(
            "total items", "total qty", "item count", "total savings",
            "you saved", "cash tendered", "cash received", "cash paid",
            "change returned", "change due", "customer paid", "tendered:",
            "subtotal", "sub-total", "sub total", "before tax", "pre tax"
        )

        // 1. Highest priority: Lines containing explicit Grand Total / Net Payable keywords (bottom up)
        for (line in lines.reversed()) {
            val lower = line.lowercase(Locale.ROOT).trim()
            if (excludeLineKeywords.any { lower.contains(it) }) continue

            if (explicitGrandTotalPatterns.any { lower.contains(it) }) {
                val amount = findLargestCurrencyInLine(line)
                if (amount != null && amount > 0) return amount
            }
        }

        // 2. High priority: Regular "Total" lines (bottom up)
        for (line in lines.reversed()) {
            val lower = line.lowercase(Locale.ROOT).trim()
            if (excludeLineKeywords.any { lower.contains(it) }) continue

            if (regularTotalPatterns.any { lower.contains(it) }) {
                val amount = findLargestCurrencyInLine(line)
                if (amount != null && amount > 0) return amount
            }
        }

        // 3. Fallback: Any line containing "payable" or "net"
        for (line in lines.reversed()) {
            val lower = line.lowercase(Locale.ROOT).trim()
            if (excludeLineKeywords.any { lower.contains(it) }) continue

            if (lower.contains("net") || lower.contains("payable")) {
                val amount = findLargestCurrencyInLine(line)
                if (amount != null && amount > 0) return amount
            }
        }

        // 3. Fallback: Search all text excluding noise lines (like cash tendered, dates, phone, gstin)
        val candidateAmounts = mutableListOf<Double>()
        for (line in lines) {
            val lower = line.lowercase(Locale.ROOT).trim()
            if (excludeLineKeywords.any { lower.contains(it) }) continue
            if (lower.contains("date") || lower.contains("time") || lower.contains("gstin") ||
                lower.contains("fssai") || lower.contains("cin") || lower.contains("ph:") ||
                lower.contains("tel:") || lower.contains("card")) {
                continue
            }
            val amount = findLargestCurrencyInLine(line)
            if (amount != null && amount > 0) {
                candidateAmounts.add(amount)
            }
        }

        return candidateAmounts.maxOrNull()
    }

    private fun findLargestCurrencyInLine(line: String): Double? {
        // Regex matches currency numbers with optional prefix (Rs, ₹, INR, $) and optional suffix (/=, /-)
        val regex = Regex("""\b(?:rs\.?|inr|₹|\$)?\s*(\d{1,3}(?:,\d{3})*(?:\.\d{1,2})?|\d+(?:\.\d{1,2})?)\s*(?:/=|/-)?\b""", RegexOption.IGNORE_CASE)
        val matches = regex.findAll(line)
        var maxAmount: Double? = null

        for (match in matches) {
            val numStr = match.groupValues[1].replace(",", "")
            val num = numStr.toDoubleOrNull() ?: continue

            // Filter out common false positives: years, PIN codes, phone numbers
            if (num in 0.5..500000.0 && num != 2023.0 && num != 2024.0 && num != 2025.0 && num != 2026.0 && num != 2027.0) {
                if (maxAmount == null || num > maxAmount) {
                    maxAmount = num
                }
            }
        }
        return maxAmount
    }

    private fun extractMerchant(text: String): String? {
        val lowerText = text.lowercase(Locale.ROOT)

        // 1. Check known brand database
        for ((brandName, patterns) in KNOWN_MERCHANTS) {
            if (patterns.any { lowerText.contains(it) }) {
                return brandName
            }
        }

        // 2. Extract from top lines of receipt
        val lines = text.split("\n")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val genericHeaders = listOf(
            "tax invoice", "retail invoice", "invoice", "bill of supply",
            "cash memo", "receipt", "estimate", "order receipt",
            "original", "duplicate", "customer copy", "welcome to",
            "welcome", "thank you", "visit again", "merchant copy",
            "pos sale", "sale receipt"
        )

        for (rawLine in lines.take(5)) {
            var line = rawLine
            val lowerLine = line.lowercase(Locale.ROOT)

            // Strip "WELCOME TO " prefix if present
            if (lowerLine.startsWith("welcome to ")) {
                line = line.substring(11).trim()
            } else if (lowerLine.startsWith("welcome to")) {
                line = line.substring(10).trim()
            }

            val cleanedLower = line.lowercase(Locale.ROOT)

            // Skip generic header lines
            if (genericHeaders.any { cleanedLower == it }) continue
            if (cleanedLower.contains("gstin") || cleanedLower.contains("fssai") ||
                cleanedLower.contains("cin:") || cleanedLower.contains("date:") ||
                cleanedLower.contains("tel:") || cleanedLower.contains("ph:") ||
                cleanedLower.contains("phone") || cleanedLower.contains("address") ||
                cleanedLower.contains("road") || cleanedLower.contains("street") ||
                cleanedLower.contains("floor") || cleanedLower.contains("www.") ||
                cleanedLower.contains(".com") || line.matches(Regex("^[0-9\\-/:\\s,.]+$"))) {
                continue
            }

            val merchant = line.replace(Regex("""[#*_=~]+"""), "").trim()
            if (merchant.length in 3..40) {
                return merchant
            }
        }

        return null
    }

    private fun extractDate(text: String): Long? {
        val lines = text.split("\n")

        val dateRegexes = listOf(
            Regex("""\b(\d{1,2})[/-](\d{1,2})[/-](\d{2,4})\b"""), // dd/MM/yyyy or dd-MM-yyyy
            Regex("""\b(\d{1,2})\.(\d{1,2})\.(\d{2,4})\b"""),     // dd.MM.yyyy or dd.MM.yy
            Regex("""\b(\d{4})[/-](\d{1,2})[/-](\d{1,2})\b"""), // yyyy-MM-dd or yyyy/MM/dd
            Regex("""\b(\d{1,2})[- ](jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z]*[- ](\d{2,4})\b""", RegexOption.IGNORE_CASE),
            Regex("""\b(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z]*\s+(\d{1,2}),?\s+(\d{2,4})\b""", RegexOption.IGNORE_CASE)
        )

        val formats = listOf(
            "dd/MM/yyyy", "dd-MM-yyyy", "dd.MM.yyyy",
            "dd/MM/yy", "dd-MM-yy", "dd.MM.yy",
            "yyyy-MM-dd", "yyyy/MM/dd",
            "dd MMM yyyy", "dd-MMM-yyyy", "dd MMMM yyyy",
            "MMM dd yyyy", "MMM dd, yyyy", "MMMM dd yyyy"
        )

        val now = System.currentTimeMillis()
        val twoDaysFuture = now + (2L * 24 * 60 * 60 * 1000)
        val minYear2000 = Calendar.getInstance().apply { set(2000, 0, 1) }.timeInMillis

        for (line in lines) {
            for (pattern in dateRegexes) {
                val match = pattern.find(line)
                if (match != null) {
                    val dateStr = match.value.replace(",", "")
                    for (format in formats) {
                        try {
                            val sdf = SimpleDateFormat(format, Locale.ENGLISH)
                            sdf.isLenient = false
                            val parsedDate = sdf.parse(dateStr)
                            if (parsedDate != null) {
                                val time = parsedDate.time
                                if (time in minYear2000..twoDaysFuture) {
                                    return time
                                }
                            }
                        } catch (e: Exception) {
                            // Try next format
                        }
                    }
                }
            }
        }
        return null
    }

    private fun suggestCategory(text: String, merchant: String?): String {
        val combined = "${merchant.orEmpty()} $text".lowercase(Locale.ROOT)

        val foodKeywords = listOf(
            "cafe", "coffee", "tea", "chai", "burger", "pizza", "restaurant",
            "food", "kitchen", "diner", "bakery", "canteen", "mess", "biryani",
            "sandwich", "starbucks", "mcdonald", "domino", "subway", "kfc",
            "swiggy", "zomato", "eat", "meals", "bar", "bistro", "roll", "grill"
        )
        val transportKeywords = listOf(
            "petrol", "fuel", "diesel", "gas", "cng", "shell", "indian oil",
            "bpcl", "hpcl", "uber", "ola", "rapido", "taxi", "cab", "auto",
            "metro", "train", "irctc", "bus", "toll", "parking"
        )
        val booksKeywords = listOf(
            "book", "stationery", "stationary", "xerox", "print", "printing",
            "copy", "bind", "paper", "pen", "notebook", "lab", "academic"
        )
        val shoppingKeywords = listOf(
            "mart", "supermarket", "grocery", "dmart", "reliance", "spencer",
            "retail", "fashion", "apparel", "clothing", "shoes", "zara", "h&m",
            "uniqlo", "decathlon", "lifestyle", "mall", "store"
        )
        val entertainmentKeywords = listOf(
            "cinema", "movie", "pvr", "inox", "cinepolis", "theatre", "ticket",
            "gaming", "bowling", "concert"
        )
        val healthKeywords = listOf(
            "pharmacy", "medical", "chemist", "medicine", "apollo", "medplus",
            "clinic", "hospital", "doctor", "tablet"
        )

        return when {
            foodKeywords.any { combined.contains(it) } -> "Food & Dining"
            transportKeywords.any { combined.contains(it) } -> "Transport"
            booksKeywords.any { combined.contains(it) } -> "Books & Supplies"
            shoppingKeywords.any { combined.contains(it) } -> "Shopping"
            entertainmentKeywords.any { combined.contains(it) } -> "Entertainment"
            healthKeywords.any { combined.contains(it) } -> "Other Expense" // Or Health if present
            else -> "Food & Dining" // Default for students
        }
    }

    private fun extractTax(text: String): Double? {
        val lines = text.split("\n")
        val taxKeywords = listOf("cgst", "sgst", "vat", "gst", "tax")
        val excludeKeywords = listOf("before tax", "pre tax", "excl", "without tax", "tax invoice", "gstin")

        for (line in lines) {
            val lower = line.lowercase(Locale.ROOT)
            if (excludeKeywords.any { lower.contains(it) }) continue
            if (taxKeywords.any { lower.contains(it) }) {
                val amount = findLargestCurrencyInLine(line)
                if (amount != null && amount > 0) return amount
            }
        }
        return null
    }
}
