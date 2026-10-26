package com.example.studentexpensetracker.domain.scanner

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ReceiptParserTest {

    @Test
    fun `parse extracts total amount from explicit total line`() {
        val text = """
            Starbucks Coffee
            1 Caffe Latte        150.00
            1 Croissant          200.00
            CGST                  18.50
            Total Amount         368.50
            Thanks for visiting!
        """.trimIndent()

        val parsed = ReceiptParser.parse(text)
        assertEquals(368.50, parsed.amount)
        assertEquals("Starbucks", parsed.merchantName)
        assertEquals("Food & Dining", parsed.suggestedCategory)
    }

    @Test
    fun `parse handles Indian Rupee symbol with commas and paise`() {
        val text = """
            WELCOME TO CAFE COFFEE DAY
            Connaught Place, New Delhi
            GSTIN: 07AAAAA0000A1Z5
            Date: 24/10/2024
            
            2x Cappuccino      ₹ 480.00
            1x Brownie Fudge   ₹ 220.00
            Subtotal:          ₹ 700.00
            CGST @ 2.5%:        ₹ 17.50
            SGST @ 2.5%:        ₹ 17.50
            Net Payable:     ₹ 1,250.50
            
            Thank You Visit Again
        """.trimIndent()

        val parsed = ReceiptParser.parse(text)
        assertEquals(1250.50, parsed.amount)
        assertEquals("Cafe Coffee Day", parsed.merchantName)
        assertEquals("Food & Dining", parsed.suggestedCategory)
        
        val calendar = Calendar.getInstance().apply { timeInMillis = parsed.dateMillis ?: 0 }
        assertEquals(24, calendar.get(Calendar.DAY_OF_MONTH))
        assertEquals(9, calendar.get(Calendar.MONTH)) // 9 is October (0-indexed)
        assertEquals(2024, calendar.get(Calendar.YEAR))
    }

    @Test
    fun `parse correctly ignores cash tendered and change returned`() {
        val text = """
            TAX INVOICE
            McDonald's India
            Burger Meal        Rs. 249.00
            Fries Extra        Rs.  99.00
            Grand Total:       Rs. 348.00
            Cash Tendered:     Rs. 500.00
            Change Returned:   Rs. 152.00
        """.trimIndent()

        val parsed = ReceiptParser.parse(text)
        assertEquals(348.00, parsed.amount)
        assertEquals("McDonald's", parsed.merchantName)
        assertEquals("Food & Dining", parsed.suggestedCategory)
    }

    @Test
    fun `parse categorizes fuel station receipt as Transport`() {
        val text = """
            SHELL INDIA MARKETS PVT LTD
            OUTLET #412 BANGALORE
            DATE: 12-OCT-2024
            UNLEADED PETROL
            RATE/LTR: 102.50
            VOLUME: 10.00 LTR
            AMOUNT PAYABLE: 1025.00
            MODE: UPI
        """.trimIndent()

        val parsed = ReceiptParser.parse(text)
        assertEquals(1025.0, parsed.amount)
        assertEquals("Shell", parsed.merchantName)
        assertEquals("Transport", parsed.suggestedCategory)

        val calendar = Calendar.getInstance().apply { timeInMillis = parsed.dateMillis ?: 0 }
        assertEquals(12, calendar.get(Calendar.DAY_OF_MONTH))
        assertEquals(9, calendar.get(Calendar.MONTH)) // 9 is October
        assertEquals(2024, calendar.get(Calendar.YEAR))
    }

    @Test
    fun `parse categorizes stationery shop receipt as Books and Supplies`() {
        val text = """
            CAMPUS BOOKSTORE & STATIONERY
            Opposite University Gate
            Ph: 9876543210
            Date: 15.08.2024
            
            1 Notebook 200pgs     Rs. 80
            2 Pilot Pens          Rs. 120
            1 Engineering Chart   Rs. 45
            Total:                Rs. 245.00
        """.trimIndent()

        val parsed = ReceiptParser.parse(text)
        assertEquals(245.0, parsed.amount)
        assertEquals("CAMPUS BOOKSTORE & STATIONERY", parsed.merchantName)
        assertEquals("Books & Supplies", parsed.suggestedCategory)

        val calendar = Calendar.getInstance().apply { timeInMillis = parsed.dateMillis ?: 0 }
        assertEquals(15, calendar.get(Calendar.DAY_OF_MONTH))
        assertEquals(7, calendar.get(Calendar.MONTH)) // 7 is August
        assertEquals(2024, calendar.get(Calendar.YEAR))
    }

    @Test
    fun `parse categorizes supermarket receipt as Shopping`() {
        val text = """
            RETAIL INVOICE
            DMART - AVENUE SUPERMARTS
            Whitefield, Bangalore
            Date: 2024-09-18
            Item Count: 4
            Total Amount: Rs. 899.00
        """.trimIndent()

        val parsed = ReceiptParser.parse(text)
        assertEquals(899.0, parsed.amount)
        assertEquals("DMart", parsed.merchantName)
        assertEquals("Shopping", parsed.suggestedCategory)

        val calendar = Calendar.getInstance().apply { timeInMillis = parsed.dateMillis ?: 0 }
        assertEquals(18, calendar.get(Calendar.DAY_OF_MONTH))
        assertEquals(8, calendar.get(Calendar.MONTH)) // 8 is September
        assertEquals(2024, calendar.get(Calendar.YEAR))
    }

    @Test
    fun `parse extracts tax amount`() {
        val text = """
            Dominos Pizza
            Total Before Tax: 400.00
            GST: 20.00
            Total: 420.00
        """.trimIndent()

        val parsed = ReceiptParser.parse(text)
        assertEquals(420.0, parsed.amount)
        assertEquals(20.0, parsed.taxAmount)
    }

    @Test
    fun `parse handles noisy input gracefully`() {
        val text = """
            Random noise from camera
            Blurry line ?#@!
            No numbers here
        """.trimIndent()

        val parsed = ReceiptParser.parse(text)
        assertNull(parsed.amount)
        assertNull(parsed.dateMillis)
    }
}
