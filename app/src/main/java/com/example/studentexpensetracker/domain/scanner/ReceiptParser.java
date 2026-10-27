package com.example.studentexpensetracker.domain.scanner;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReceiptParser {

    private static final Map<String, List<String>> KNOWN_MERCHANTS = new HashMap<>();

    static {
        KNOWN_MERCHANTS.put("Starbucks", Arrays.asList("starbucks"));
        KNOWN_MERCHANTS.put("McDonald's", Arrays.asList("mcdonald", "mcdonalds", "mc donald"));
        KNOWN_MERCHANTS.put("Domino's Pizza", Arrays.asList("domino", "dominos"));
        KNOWN_MERCHANTS.put("Subway", Arrays.asList("subway"));
        KNOWN_MERCHANTS.put("Burger King", Arrays.asList("burger king"));
        KNOWN_MERCHANTS.put("KFC", Arrays.asList("kfc", "kentucky"));
        KNOWN_MERCHANTS.put("Pizza Hut", Arrays.asList("pizza hut"));
        KNOWN_MERCHANTS.put("Cafe Coffee Day", Arrays.asList("coffee day", "ccd"));
        KNOWN_MERCHANTS.put("Chaayos", Arrays.asList("chaayos"));
        KNOWN_MERCHANTS.put("Chai Point", Arrays.asList("chai point"));
        KNOWN_MERCHANTS.put("Haldiram's", Arrays.asList("haldiram"));
        KNOWN_MERCHANTS.put("Zomato", Arrays.asList("zomato"));
        KNOWN_MERCHANTS.put("Swiggy", Arrays.asList("swiggy"));
        KNOWN_MERCHANTS.put("Zepto", Arrays.asList("zepto"));
        KNOWN_MERCHANTS.put("Blinkit", Arrays.asList("blinkit"));
        KNOWN_MERCHANTS.put("Instamart", Arrays.asList("instamart"));
        KNOWN_MERCHANTS.put("DMart", Arrays.asList("dmart", "d-mart", "avenue supermarts"));
        KNOWN_MERCHANTS.put("Reliance Smart", Arrays.asList("reliance smart", "reliance fresh", "reliance retail"));
        KNOWN_MERCHANTS.put("More Supermarket", Arrays.asList("more supermarket", "more retail"));
        KNOWN_MERCHANTS.put("Apollo Pharmacy", Arrays.asList("apollo pharmacy", "apollo"));
        KNOWN_MERCHANTS.put("MedPlus", Arrays.asList("medplus"));
        KNOWN_MERCHANTS.put("Shell", Arrays.asList("shell"));
        KNOWN_MERCHANTS.put("Indian Oil", Arrays.asList("indian oil", "iocl"));
        KNOWN_MERCHANTS.put("Bharat Petroleum", Arrays.asList("bharat petroleum", "bpcl"));
        KNOWN_MERCHANTS.put("HP Petrol", Arrays.asList("hindustan petroleum", "hpcl"));
        KNOWN_MERCHANTS.put("PVR Cinemas", Arrays.asList("pvr", "pvr cinemas"));
        KNOWN_MERCHANTS.put("INOX", Arrays.asList("inox"));
        KNOWN_MERCHANTS.put("Cinepolis", Arrays.asList("cinepolis"));
        KNOWN_MERCHANTS.put("Decathlon", Arrays.asList("decathlon"));
        KNOWN_MERCHANTS.put("Uniqlo", Arrays.asList("uniqlo"));
        KNOWN_MERCHANTS.put("Zara", Arrays.asList("zara"));
        KNOWN_MERCHANTS.put("H&M", Arrays.asList("h&m", "h & m"));
    }

    public static ParsedReceipt parse(String text, String receiptImagePath) {
        if (text == null) text = "";
        String cleanText = text.trim();
        Double amount = extractAmount(cleanText);
        String merchant = extractMerchant(cleanText);
        Long date = extractDate(cleanText);
        String category = suggestCategory(cleanText, merchant);
        Double tax = extractTax(cleanText);

        return new ParsedReceipt(
                amount,
                merchant,
                date,
                category,
                tax,
                cleanText,
                receiptImagePath
        );
    }

    public static ParsedReceipt parse(String text) {
        return parse(text, null);
    }

    private static Double extractAmount(String text) {
        String[] rawLines = text.split("\n");
        List<String> lines = Arrays.asList(rawLines);

        List<String> explicitGrandTotalPatterns = Arrays.asList(
                "grand total", "net payable", "amount payable", "total payable",
                "net amount", "final total", "total due", "bill total", "bal due", "balance due"
        );
        List<String> regularTotalPatterns = Arrays.asList(
                "total amount", "total:", "total"
        );

        List<String> excludeLineKeywords = Arrays.asList(
                "total items", "total qty", "item count", "total savings",
                "you saved", "cash tendered", "cash received", "cash paid",
                "change returned", "change due", "customer paid", "tendered:",
                "subtotal", "sub-total", "sub total", "before tax", "pre tax"
        );

        // 1. Highest priority: Explicit grand total (bottom up)
        for (int i = lines.size() - 1; i >= 0; i--) {
            String line = lines.get(i);
            String lower = line.toLowerCase(Locale.ROOT).trim();
            if (containsAny(lower, excludeLineKeywords)) continue;

            if (containsAny(lower, explicitGrandTotalPatterns)) {
                Double amount = findLargestCurrencyInLine(line);
                if (amount != null && amount > 0) return amount;
            }
        }

        // 2. High priority: Regular "Total" lines (bottom up)
        for (int i = lines.size() - 1; i >= 0; i--) {
            String line = lines.get(i);
            String lower = line.toLowerCase(Locale.ROOT).trim();
            if (containsAny(lower, excludeLineKeywords)) continue;

            if (containsAny(lower, regularTotalPatterns)) {
                Double amount = findLargestCurrencyInLine(line);
                if (amount != null && amount > 0) return amount;
            }
        }

        // 3. Fallback: "net" or "payable"
        for (int i = lines.size() - 1; i >= 0; i--) {
            String line = lines.get(i);
            String lower = line.toLowerCase(Locale.ROOT).trim();
            if (containsAny(lower, excludeLineKeywords)) continue;

            if (lower.contains("net") || lower.contains("payable")) {
                Double amount = findLargestCurrencyInLine(line);
                if (amount != null && amount > 0) return amount;
            }
        }

        // 4. Fallback: search candidate amounts
        List<Double> candidateAmounts = new ArrayList<>();
        for (String line : lines) {
            String lower = line.toLowerCase(Locale.ROOT).trim();
            if (containsAny(lower, excludeLineKeywords)) continue;
            if (lower.contains("date") || lower.contains("time") || lower.contains("gstin") ||
                    lower.contains("fssai") || lower.contains("cin") || lower.contains("ph:") ||
                    lower.contains("tel:") || lower.contains("card")) {
                continue;
            }
            Double amount = findLargestCurrencyInLine(line);
            if (amount != null && amount > 0) {
                candidateAmounts.add(amount);
            }
        }

        if (candidateAmounts.isEmpty()) return null;
        return Collections.max(candidateAmounts);
    }

    private static boolean containsAny(String text, List<String> keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) return true;
        }
        return false;
    }

    private static Double findLargestCurrencyInLine(String line) {
        Pattern regex = Pattern.compile(
                "\\b(?:rs\\.?|inr|₹|\\$)?\\s*(\\d{1,3}(?:,\\d{3})*(?:\\.\\d{1,2})?|\\d+(?:\\.\\d{1,2})?)\\s*(?:/=|/-)?\\b",
                Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = regex.matcher(line);
        Double maxAmount = null;

        while (matcher.find()) {
            String numStr = matcher.group(1);
            if (numStr == null) continue;
            numStr = numStr.replace(",", "");
            try {
                double num = Double.parseDouble(numStr);
                if (num >= 0.5 && num <= 500000.0 && num != 2023.0 && num != 2024.0 && num != 2025.0 && num != 2026.0 && num != 2027.0) {
                    if (maxAmount == null || num > maxAmount) {
                        maxAmount = num;
                    }
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return maxAmount;
    }

    private static String extractMerchant(String text) {
        String lowerText = text.toLowerCase(Locale.ROOT);

        for (Map.Entry<String, List<String>> entry : KNOWN_MERCHANTS.entrySet()) {
            for (String pattern : entry.getValue()) {
                if (lowerText.contains(pattern)) {
                    return entry.getKey();
                }
            }
        }

        String[] rawLines = text.split("\n");
        List<String> genericHeaders = Arrays.asList(
                "tax invoice", "retail invoice", "invoice", "bill of supply",
                "cash memo", "receipt", "estimate", "order receipt",
                "original", "duplicate", "customer copy", "welcome to",
                "welcome", "thank you", "visit again", "merchant copy",
                "pos sale", "sale receipt"
        );

        int checkCount = Math.min(rawLines.length, 5);
        for (int i = 0; i < checkCount; i++) {
            String line = rawLines[i].trim();
            if (line.isEmpty()) continue;
            String lowerLine = line.toLowerCase(Locale.ROOT);

            if (lowerLine.startsWith("welcome to ")) {
                line = line.substring(11).trim();
            } else if (lowerLine.startsWith("welcome to")) {
                line = line.substring(10).trim();
            }

            String cleanedLower = line.toLowerCase(Locale.ROOT);
            if (genericHeaders.contains(cleanedLower)) continue;
            if (cleanedLower.contains("gstin") || cleanedLower.contains("fssai") ||
                    cleanedLower.contains("cin:") || cleanedLower.contains("date:") ||
                    cleanedLower.contains("tel:") || cleanedLower.contains("ph:") ||
                    cleanedLower.contains("phone") || cleanedLower.contains("address") ||
                    cleanedLower.contains("road") || cleanedLower.contains("street") ||
                    cleanedLower.contains("floor") || cleanedLower.contains("www.") ||
                    cleanedLower.contains(".com") || line.matches("^[0-9\\-/:\\s,.]+$")) {
                continue;
            }

            String merchant = line.replaceAll("[#*_=~]+", "").trim();
            if (merchant.length() >= 3 && merchant.length() <= 40) {
                return merchant;
            }
        }
        return null;
    }

    private static Long extractDate(String text) {
        String[] lines = text.split("\n");

        List<Pattern> dateRegexes = Arrays.asList(
                Pattern.compile("\\b(\\d{1,2})[/-](\\d{1,2})[/-](\\d{2,4})\\b"),
                Pattern.compile("\\b(\\d{1,2})\\.(\\d{1,2})\\.(\\d{2,4})\\b"),
                Pattern.compile("\\b(\\d{4})[/-](\\d{1,2})[/-](\\d{1,2})\\b"),
                Pattern.compile("\\b(\\d{1,2})[- ](jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z]*[- ](\\d{2,4})\\b", Pattern.CASE_INSENSITIVE),
                Pattern.compile("\\b(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z]*\\s+(\\d{1,2}),?\\s+(\\d{2,4})\\b", Pattern.CASE_INSENSITIVE)
        );

        List<String> formats = Arrays.asList(
                "dd/MM/yyyy", "dd-MM-yyyy", "dd.MM.yyyy",
                "dd/MM/yy", "dd-MM-yy", "dd.MM.yy",
                "yyyy-MM-dd", "yyyy/MM/dd",
                "dd MMM yyyy", "dd-MMM-yyyy", "dd MMMM yyyy",
                "MMM dd yyyy", "MMM dd, yyyy", "MMMM dd yyyy"
        );

        long now = System.currentTimeMillis();
        long twoDaysFuture = now + (2L * 24 * 60 * 60 * 1000);
        Calendar cal2000 = Calendar.getInstance();
        cal2000.set(2000, Calendar.JANUARY, 1);
        long minYear2000 = cal2000.getTimeInMillis();

        for (String line : lines) {
            for (Pattern pattern : dateRegexes) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    String dateStr = matcher.group(0).replace(",", "");
                    for (String format : formats) {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.ENGLISH);
                            sdf.setLenient(false);
                            Date parsedDate = sdf.parse(dateStr);
                            if (parsedDate != null) {
                                long time = parsedDate.getTime();
                                if (time >= minYear2000 && time <= twoDaysFuture) {
                                    return time;
                                }
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }
        return null;
    }

    private static String suggestCategory(String text, String merchant) {
        String combined = ((merchant != null ? merchant : "") + " " + text).toLowerCase(Locale.ROOT);

        List<String> foodKeywords = Arrays.asList(
                "cafe", "coffee", "tea", "chai", "burger", "pizza", "restaurant",
                "food", "kitchen", "diner", "bakery", "canteen", "mess", "biryani",
                "sandwich", "starbucks", "mcdonald", "domino", "subway", "kfc",
                "swiggy", "zomato", "eat", "meals", "bar", "bistro", "roll", "grill"
        );
        List<String> transportKeywords = Arrays.asList(
                "petrol", "fuel", "diesel", "gas", "cng", "shell", "indian oil",
                "bpcl", "hpcl", "uber", "ola", "rapido", "taxi", "cab", "auto",
                "metro", "train", "irctc", "bus", "toll", "parking"
        );
        List<String> booksKeywords = Arrays.asList(
                "book", "stationery", "stationary", "xerox", "print", "printing",
                "copy", "bind", "paper", "pen", "notebook", "lab", "academic"
        );
        List<String> shoppingKeywords = Arrays.asList(
                "mart", "supermarket", "grocery", "dmart", "reliance", "spencer",
                "retail", "fashion", "apparel", "clothing", "shoes", "zara", "h&m",
                "uniqlo", "decathlon", "lifestyle", "mall", "store"
        );
        List<String> entertainmentKeywords = Arrays.asList(
                "cinema", "movie", "pvr", "inox", "cinepolis", "theatre", "ticket",
                "gaming", "bowling", "concert"
        );
        List<String> healthKeywords = Arrays.asList(
                "pharmacy", "medical", "chemist", "medicine", "apollo", "medplus",
                "clinic", "hospital", "doctor", "tablet"
        );

        if (containsAny(combined, foodKeywords)) return "Food & Dining";
        if (containsAny(combined, transportKeywords)) return "Transport";
        if (containsAny(combined, booksKeywords)) return "Books & Supplies";
        if (containsAny(combined, shoppingKeywords)) return "Shopping";
        if (containsAny(combined, entertainmentKeywords)) return "Entertainment";
        if (containsAny(combined, healthKeywords)) return "Other Expense";

        return "Food & Dining";
    }

    private static Double extractTax(String text) {
        String[] lines = text.split("\n");
        List<String> taxKeywords = Arrays.asList("cgst", "sgst", "vat", "gst", "tax");
        List<String> excludeKeywords = Arrays.asList("before tax", "pre tax", "excl", "without tax", "tax invoice", "gstin");

        for (String line : lines) {
            String lower = line.toLowerCase(Locale.ROOT);
            if (containsAny(lower, excludeKeywords)) continue;
            if (containsAny(lower, taxKeywords)) {
                Double amount = findLargestCurrencyInLine(line);
                if (amount != null && amount > 0) return amount;
            }
        }
        return null;
    }
}
