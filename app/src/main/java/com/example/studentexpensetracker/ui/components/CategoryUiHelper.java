package com.example.studentexpensetracker.ui.components;

import com.example.studentexpensetracker.R;

public class CategoryUiHelper {

    public static class CategoryVisual {
        private final int iconResId;
        private final int color;

        public CategoryVisual(int iconResId, int color) {
            this.iconResId = iconResId;
            this.color = color;
        }

        public int getIconResId() {
            return iconResId;
        }

        public int getColor() {
            return color;
        }
    }

    public static CategoryVisual getVisual(String categoryOrNote, boolean isIncome) {
        String query = categoryOrNote != null ? categoryOrNote.trim().toLowerCase() : "";

        // Food & Dining
        if (query.contains("food") || query.contains("dining") || query.contains("restaurant") ||
            query.contains("cafe") || query.contains("snack") || query.contains("burger") ||
            query.contains("pizza") || query.contains("meal") || query.contains("lunch") ||
            query.contains("dinner") || query.contains("breakfast") || query.contains("canteen") ||
            query.contains("coffee") || query.contains("tea")) {
            return new CategoryVisual(R.drawable.ic_restaurant, 0xFFFB923C); // Warm Orange
        }

        // Transport
        if (query.contains("transport") || query.contains("travel") || query.contains("bus") ||
            query.contains("metro") || query.contains("train") || query.contains("cab") ||
            query.contains("taxi") || query.contains("auto") || query.contains("uber") ||
            query.contains("ola") || query.contains("petrol") || query.contains("fuel") ||
            query.contains("diesel") || query.contains("flight")) {
            return new CategoryVisual(R.drawable.ic_directions_bus, 0xFF38BDF8); // Sky Blue
        }

        // Books & Stationery
        if (query.contains("book") || query.contains("supplies") || query.contains("stationery") ||
            query.contains("notebook") || query.contains("pen") || query.contains("pencil") ||
            query.contains("library") || query.contains("xerox") || query.contains("print") ||
            query.contains("textbook")) {
            return new CategoryVisual(R.drawable.ic_menu_book, 0xFFA855F7); // Purple
        }

        // Academics / Fees
        if (query.contains("tuition") || query.contains("college fee") || query.contains("fees") ||
            query.contains("course") || query.contains("exam") || query.contains("semester") ||
            query.contains("admission")) {
            return new CategoryVisual(R.drawable.ic_school, 0xFF818CF8); // Indigo
        }

        // Shopping
        if (query.contains("shopping") || query.contains("cloth") || query.contains("dress") ||
            query.contains("shoes") || query.contains("amazon") || query.contains("flipkart") ||
            query.contains("myntra") || query.contains("electronics") || query.contains("grocery") ||
            query.contains("mart") || query.contains("store")) {
            return new CategoryVisual(R.drawable.ic_shopping_bag, 0xFFC084FC); // Violet
        }

        // Medical
        if (query.contains("health") || query.contains("medical") || query.contains("doctor") ||
            query.contains("pharmacy") || query.contains("medicine") || query.contains("hospital") ||
            query.contains("clinic")) {
            return new CategoryVisual(R.drawable.ic_local_hospital, 0xFFF87171); // Red
        }

        // Allowance / Pocket Money
        if (query.contains("allowance") || query.contains("pocket money") || query.contains("parents") ||
            query.contains("family") || query.contains("home money")) {
            return new CategoryVisual(R.drawable.ic_account_balance_wallet, 0xFF22D3EE); // Cyan
        }

        if (isIncome) {
            return new CategoryVisual(R.drawable.ic_arrow_downward, 0xFF34D399); // Green
        } else {
            return new CategoryVisual(R.drawable.ic_payments, 0xFFF87171); // Red
        }
    }
}
