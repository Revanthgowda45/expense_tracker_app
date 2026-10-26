package com.example.studentexpensetracker.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.studentexpensetracker.ui.theme.ExpenseRed
import com.example.studentexpensetracker.ui.theme.IncomeGreen

/**
 * Maps categories and transaction notes to representative Material icons and accent colors.
 */
object CategoryUiHelper {

    data class CategoryVisual(
        val icon: ImageVector,
        val color: Color
    )

    fun getVisual(categoryOrNote: String?, isIncome: Boolean): CategoryVisual {
        val query = categoryOrNote?.trim()?.lowercase() ?: ""
        return when {
            // Food & Dining
            query.contains("food") || query.contains("dining") || query.contains("restaurant") ||
            query.contains("cafe") || query.contains("snack") || query.contains("burger") ||
            query.contains("pizza") || query.contains("meal") || query.contains("lunch") ||
            query.contains("dinner") || query.contains("breakfast") || query.contains("canteen") ||
            query.contains("coffee") || query.contains("tea") -> {
                CategoryVisual(Icons.Default.Restaurant, Color(0xFFFB923C)) // Luminous Warm Orange
            }

            // Transport & Travel
            query.contains("transport") || query.contains("travel") || query.contains("bus") ||
            query.contains("metro") || query.contains("train") || query.contains("cab") ||
            query.contains("taxi") || query.contains("auto") || query.contains("uber") ||
            query.contains("ola") || query.contains("petrol") || query.contains("fuel") ||
            query.contains("diesel") || query.contains("flight") -> {
                CategoryVisual(Icons.Default.DirectionsBus, Color(0xFF38BDF8)) // Luminous Sky Blue
            }

            // Books & Supplies / Stationery / College Study
            query.contains("book") || query.contains("supplies") || query.contains("stationery") ||
            query.contains("notebook") || query.contains("pen") || query.contains("pencil") ||
            query.contains("library") || query.contains("xerox") || query.contains("print") ||
            query.contains("textbook") -> {
                CategoryVisual(Icons.AutoMirrored.Filled.MenuBook, Color(0xFFA855F7)) // Luminous Purple
            }

            // Tuition Fee / College Fee / Academics
            query.contains("tuition") || query.contains("college fee") || query.contains("fees") ||
            query.contains("course") || query.contains("exam") || query.contains("semester") ||
            query.contains("admission") -> {
                CategoryVisual(Icons.Default.School, Color(0xFF818CF8)) // Luminous Indigo
            }

            // Rent & Accommodation / Hostel
            query.contains("rent") || query.contains("hostel") || query.contains("room") ||
            query.contains("flat") || query.contains("pg") || query.contains("apartment") ||
            query.contains("housing") -> {
                CategoryVisual(Icons.Default.Home, Color(0xFF2DD4BF)) // Luminous Teal
            }

            // Entertainment / Media / Leisure
            query.contains("entertainment") || query.contains("movie") || query.contains("cinema") ||
            query.contains("netflix") || query.contains("spotify") || query.contains("game") ||
            query.contains("gaming") || query.contains("party") || query.contains("outing") ||
            query.contains("concert") || query.contains("theatre") -> {
                CategoryVisual(Icons.Default.Movie, Color(0xFFF472B6)) // Luminous Pink
            }

            // Shopping & Retail / Groceries
            query.contains("shopping") || query.contains("cloth") || query.contains("dress") ||
            query.contains("shoes") || query.contains("amazon") || query.contains("flipkart") ||
            query.contains("myntra") || query.contains("electronics") || query.contains("grocery") ||
            query.contains("mart") || query.contains("store") -> {
                CategoryVisual(Icons.Default.ShoppingBag, Color(0xFFC084FC)) // Luminous Violet
            }

            // Health & Medical / Pharmacy
            query.contains("health") || query.contains("medical") || query.contains("doctor") ||
            query.contains("pharmacy") || query.contains("medicine") || query.contains("hospital") ||
            query.contains("clinic") -> {
                CategoryVisual(Icons.Default.LocalHospital, Color(0xFFF87171)) // Luminous Red
            }

            // Part-time Job / Salary / Work / Stipend
            query.contains("part-time") || query.contains("job") || query.contains("salary") ||
            query.contains("stipend") || query.contains("wage") || query.contains("internship") ||
            query.contains("work") -> {
                CategoryVisual(Icons.Default.Work, Color(0xFF34D399)) // Luminous Emerald
            }

            // Allowance / Pocket Money
            query.contains("allowance") || query.contains("pocket money") || query.contains("parents") ||
            query.contains("family") || query.contains("home money") -> {
                CategoryVisual(Icons.Default.AccountBalanceWallet, Color(0xFF22D3EE)) // Luminous Cyan
            }

            // Scholarship / Grants / Awards
            query.contains("scholarship") || query.contains("grant") || query.contains("award") ||
            query.contains("fellowship") -> {
                CategoryVisual(Icons.Default.WorkspacePremium, Color(0xFFFBBF24)) // Luminous Gold
            }

            // Gift / Presents
            query.contains("gift") || query.contains("birthday") || query.contains("present") -> {
                CategoryVisual(Icons.Default.CardGiftcard, Color(0xFFFB7185)) // Luminous Rose
            }

            // Freelance / Remote / Client
            query.contains("freelance") || query.contains("client") || query.contains("gig") ||
            query.contains("project") || query.contains("contract") -> {
                CategoryVisual(Icons.Default.Laptop, Color(0xFF2DD4BF)) // Luminous Teal
            }

            // Utilities / Bills / Recharges
            query.contains("bill") || query.contains("utility") || query.contains("recharge") ||
            query.contains("electricity") || query.contains("water") || query.contains("wifi") ||
            query.contains("internet") -> {
                CategoryVisual(Icons.Default.AccountBalance, Color(0xFFF59E0B)) // Luminous Amber
            }

            // Default Fallbacks
            isIncome -> CategoryVisual(Icons.AutoMirrored.Filled.TrendingUp, Color(0xFF34D399))
            else -> CategoryVisual(Icons.Default.Payments, Color(0xFFF87171))
        }
    }
}
