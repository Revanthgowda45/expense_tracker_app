package com.example.studentexpensetracker.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.studentexpensetracker.R
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Groups

sealed class Screen(val route: String, @StringRes val titleResId: Int = 0, val icon: ImageVector? = null) {
    object Home : Screen("home", R.string.home, Icons.Filled.Home)
    object Transactions : Screen("transactions", R.string.transactions, Icons.Filled.List)
    object Reports : Screen("reports", R.string.reports, Icons.Filled.PieChart)
    object Split : Screen("split", R.string.split, Icons.Filled.Groups) 
    object Settings : Screen("settings", R.string.settings, Icons.Filled.Settings)
    
    // Screens without bottom nav icons
    object AddTransaction : Screen("add_transaction?amount={amount}&merchant={merchant}&date={date}", R.string.add_transaction) {
        fun createRoute(amount: String = "", merchant: String = "", date: String = ""): String {
            return "add_transaction?amount=$amount&merchant=$merchant&date=$date"
        }
    }
    
    object ReceiptScanner : Screen("receipt_scanner")
    
    object EditTransaction : Screen("edit_transaction/{transactionId}") {
        fun createRoute(transactionId: Long) = "edit_transaction/$transactionId"
    }

    // Group Detail Screen - taking an argument
    object GroupDetail : Screen("group_detail/{groupId}") {
        fun createRoute(groupId: Long) = "group_detail/$groupId"
    }
}
