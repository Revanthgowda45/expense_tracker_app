package com.example.studentexpensetracker.domain.scanner;

import org.junit.Test;
import java.util.Calendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ReceiptParserTest {

    @Test
    public void parse_extractsTotalAmountFromExplicitTotalLine() {
        String text = "Starbucks Coffee\n" +
                "1 Caffe Latte        150.00\n" +
                "1 Croissant          200.00\n" +
                "CGST                  18.50\n" +
                "Total Amount         368.50\n" +
                "Thanks for visiting!";

        ParsedReceipt parsed = ReceiptParser.parse(text);
        assertNotNull(parsed.getAmount());
        assertEquals(368.50, parsed.getAmount(), 0.001);
        assertEquals("Starbucks", parsed.getMerchantName());
        assertEquals("Food & Dining", parsed.getSuggestedCategory());
    }

    @Test
    public void parse_handlesIndianRupeeSymbolWithCommasAndPaise() {
        String text = "WELCOME TO CAFE COFFEE DAY\n" +
                "Connaught Place, New Delhi\n" +
                "GSTIN: 07AAAAA0000A1Z5\n" +
                "Date: 24/10/2024\n\n" +
                "2x Cappuccino      ₹ 480.00\n" +
                "1x Brownie Fudge   ₹ 220.00\n" +
                "Subtotal:          ₹ 700.00\n" +
                "CGST @ 2.5%:        ₹ 17.50\n" +
                "SGST @ 2.5%:        ₹ 17.50\n" +
                "Net Payable:     ₹ 1,250.50\n\n" +
                "Thank You Visit Again";

        ParsedReceipt parsed = ReceiptParser.parse(text);
        assertNotNull(parsed.getAmount());
        assertEquals(1250.50, parsed.getAmount(), 0.001);
        assertEquals("Cafe Coffee Day", parsed.getMerchantName());
        assertEquals("Food & Dining", parsed.getSuggestedCategory());

        assertNotNull(parsed.getDateMillis());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(parsed.getDateMillis());
        assertEquals(24, calendar.get(Calendar.DAY_OF_MONTH));
        assertEquals(9, calendar.get(Calendar.MONTH)); // 9 is October (0-indexed)
        assertEquals(2024, calendar.get(Calendar.YEAR));
    }

    @Test
    public void parse_correctlyIgnoresCashTenderedAndChangeReturned() {
        String text = "TAX INVOICE\n" +
                "McDonald's India\n" +
                "Burger Meal        Rs. 249.00\n" +
                "Fries Extra        Rs.  99.00\n" +
                "Grand Total:       Rs. 348.00\n" +
                "Cash Tendered:     Rs. 500.00\n" +
                "Change Returned:   Rs. 152.00";

        ParsedReceipt parsed = ReceiptParser.parse(text);
        assertNotNull(parsed.getAmount());
        assertEquals(348.00, parsed.getAmount(), 0.001);
        assertEquals("McDonald's", parsed.getMerchantName());
        assertEquals("Food & Dining", parsed.getSuggestedCategory());
    }

    @Test
    public void parse_categorizesFuelStationReceiptAsTransport() {
        String text = "SHELL INDIA MARKETS PVT LTD\n" +
                "OUTLET #412 BANGALORE\n" +
                "DATE: 12-OCT-2024\n" +
                "UNLEADED PETROL\n" +
                "RATE/LTR: 102.50\n" +
                "VOLUME: 10.00 LTR\n" +
                "AMOUNT PAYABLE: 1025.00\n" +
                "MODE: UPI";

        ParsedReceipt parsed = ReceiptParser.parse(text);
        assertNotNull(parsed.getAmount());
        assertEquals(1025.0, parsed.getAmount(), 0.001);
        assertEquals("Shell", parsed.getMerchantName());
        assertEquals("Transport", parsed.getSuggestedCategory());

        assertNotNull(parsed.getDateMillis());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(parsed.getDateMillis());
        assertEquals(12, calendar.get(Calendar.DAY_OF_MONTH));
        assertEquals(9, calendar.get(Calendar.MONTH));
        assertEquals(2024, calendar.get(Calendar.YEAR));
    }

    @Test
    public void parse_categorizesStationeryShopReceiptAsBooksAndSupplies() {
        String text = "CAMPUS BOOKSTORE & STATIONERY\n" +
                "Opposite University Gate\n" +
                "Ph: 9876543210\n" +
                "Date: 15.08.2024\n\n" +
                "1 Notebook 200pgs     Rs. 80\n" +
                "2 Pilot Pens          Rs. 120\n" +
                "1 Engineering Chart   Rs. 45\n" +
                "Total:                Rs. 245.00";

        ParsedReceipt parsed = ReceiptParser.parse(text);
        assertNotNull(parsed.getAmount());
        assertEquals(245.0, parsed.getAmount(), 0.001);
        assertEquals("CAMPUS BOOKSTORE & STATIONERY", parsed.getMerchantName());
        assertEquals("Books & Supplies", parsed.getSuggestedCategory());

        assertNotNull(parsed.getDateMillis());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(parsed.getDateMillis());
        assertEquals(15, calendar.get(Calendar.DAY_OF_MONTH));
        assertEquals(7, calendar.get(Calendar.MONTH)); // 7 is August
        assertEquals(2024, calendar.get(Calendar.YEAR));
    }

    @Test
    public void parse_categorizesSupermarketReceiptAsShopping() {
        String text = "RETAIL INVOICE\n" +
                "DMART - AVENUE SUPERMARTS\n" +
                "Whitefield, Bangalore\n" +
                "Date: 2024-09-18\n" +
                "Item Count: 4\n" +
                "Total Amount: Rs. 899.00";

        ParsedReceipt parsed = ReceiptParser.parse(text);
        assertNotNull(parsed.getAmount());
        assertEquals(899.0, parsed.getAmount(), 0.001);
        assertEquals("DMart", parsed.getMerchantName());
        assertEquals("Shopping", parsed.getSuggestedCategory());

        assertNotNull(parsed.getDateMillis());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(parsed.getDateMillis());
        assertEquals(18, calendar.get(Calendar.DAY_OF_MONTH));
        assertEquals(8, calendar.get(Calendar.MONTH)); // 8 is September
        assertEquals(2024, calendar.get(Calendar.YEAR));
    }

    @Test
    public void parse_extractsTaxAmount() {
        String text = "Dominos Pizza\n" +
                "Total Before Tax: 400.00\n" +
                "GST: 20.00\n" +
                "Total: 420.00";

        ParsedReceipt parsed = ReceiptParser.parse(text);
        assertNotNull(parsed.getAmount());
        assertEquals(420.0, parsed.getAmount(), 0.001);
        assertNotNull(parsed.getTaxAmount());
        assertEquals(20.0, parsed.getTaxAmount(), 0.001);
    }

    @Test
    public void parse_handlesNoisyInputGracefully() {
        String text = "Random noise from camera\n" +
                "Blurry line ?#@!\n" +
                "No numbers here";

        ParsedReceipt parsed = ReceiptParser.parse(text);
        assertNull(parsed.getAmount());
        assertNull(parsed.getDateMillis());
    }
}
