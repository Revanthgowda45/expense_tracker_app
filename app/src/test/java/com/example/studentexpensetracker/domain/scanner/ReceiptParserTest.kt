package com.example.studentexpensetracker.domain.scanner

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class ReceiptParserTest {

    @Test
    fun `parse extracts total amount from explicit total line`() {
        val text = """
            Starbucks
            1 Coffee        150.00
            1 Sandwich      200.00
            Tax              18.50
            Total Amount    368.50
            Thanks for visiting!
        """.trimIndent()

        val parsed = ReceiptParser.parse(text)
        assertEquals(368.50, parsed.amount)
    }

    @Test
    fun `parse extracts largest amount if no total line found`() {
        val text = """
            McDonalds
            Burger      Rs.120
            Fries       Rs.80
            Rs.200
        """.trimIndent()

        val parsed = ReceiptParser.parse(text)
        assertEquals(200.0, parsed.amount)
    }

    @Test
    fun `parse extracts merchant name from first valid line`() {
        val text = """
            Zomato Foods Pvt Ltd
            Order #12345
            Date: 12/10/2023
            Amount: 500
        """.trimIndent()

        val parsed = ReceiptParser.parse(text)
        assertEquals("Zomato Foods Pvt Ltd", parsed.merchantName)
    }

    @Test
    fun `parse extracts date in ddMMyyyy format`() {
        val text = """
            Dominos Pizza
            Date: 15/08/2023
            Time: 14:30
            Total: 450.0
        """.trimIndent()

        val parsed = ReceiptParser.parse(text)
        
        val calendar = Calendar.getInstance().apply { timeInMillis = parsed.dateMillis ?: 0 }
        assertEquals(15, calendar.get(Calendar.DAY_OF_MONTH))
        assertEquals(7, calendar.get(Calendar.MONTH)) // 0-based, so 7 is August
        assertEquals(2023, calendar.get(Calendar.YEAR))
    }
}
