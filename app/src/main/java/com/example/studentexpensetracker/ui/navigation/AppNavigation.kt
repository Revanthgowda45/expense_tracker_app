package com.example.studentexpensetracker.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.studentexpensetracker.ui.home.HomeScreen
import com.example.studentexpensetracker.ui.transactions.TransactionsScreen
import com.example.studentexpensetracker.ui.reports.ReportsScreen
import com.example.studentexpensetracker.ui.split.SplitScreen
import com.example.studentexpensetracker.ui.split.GroupDetailScreen
import com.example.studentexpensetracker.ui.settings.SettingsScreen
import com.example.studentexpensetracker.ui.transactions.AddTransactionScreen
import com.example.studentexpensetracker.ui.transactions.EditTransactionScreen
import com.example.studentexpensetracker.ui.scanner.ReceiptScannerScreen
import androidx.navigation.navArgument

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    val bottomNavItems = listOf(
        Screen.Home,
        Screen.Transactions,
        Screen.Reports,
        Screen.Split,
        Screen.Settings
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    val showBottomBarAndFab = bottomNavItems.any { it.route == currentDestination?.route }

    Scaffold(
        bottomBar = {
            if (showBottomBarAndFab) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { screen.icon?.let { Icon(it, contentDescription = null) } },
                            label = { 
                                Text(
                                    text = stringResource(screen.titleResId),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                ) 
                            },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (showBottomBarAndFab) {
                Column(horizontalAlignment = Alignment.End) {
                    FloatingActionButton(
                        onClick = { 
                            navController.navigate(Screen.ReceiptScanner.route)
                        },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        shape = CircleShape,
                        modifier = Modifier.padding(bottom = 16.dp).size(48.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Scan Receipt")
                    }
                    
                    FloatingActionButton(
                        onClick = { 
                            navController.navigate(Screen.AddTransaction.createRoute())
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = CircleShape
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Transaction")
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { 
                HomeScreen(
                    onNavigateToEdit = { transactionId ->
                        navController.navigate(Screen.EditTransaction.createRoute(transactionId))
                    }
                ) 
            }
            composable(Screen.Transactions.route) { 
                TransactionsScreen(
                    onNavigateToEdit = { transactionId ->
                        navController.navigate(Screen.EditTransaction.createRoute(transactionId))
                    }
                ) 
            }
            composable(Screen.Reports.route) { ReportsScreen() }
            composable(Screen.Split.route) { 
                SplitScreen(
                    onNavigateToGroup = { groupId ->
                        navController.navigate(Screen.GroupDetail.createRoute(groupId))
                    }
                ) 
            }
            composable(Screen.Settings.route) { SettingsScreen() }
            
            composable(
                route = Screen.AddTransaction.route,
                arguments = listOf(
                    navArgument("amount") { type = NavType.StringType; defaultValue = "" },
                    navArgument("merchant") { type = NavType.StringType; defaultValue = "" },
                    navArgument("date") { type = NavType.StringType; defaultValue = "" }
                )
            ) { backStackEntry ->
                val prefillAmount = backStackEntry.arguments?.getString("amount") ?: ""
                val prefillMerchant = backStackEntry.arguments?.getString("merchant") ?: ""
                val prefillDateStr = backStackEntry.arguments?.getString("date") ?: ""
                val prefillDateMillis = prefillDateStr.toLongOrNull()
                
                AddTransactionScreen(
                    onNavigateBack = { navController.popBackStack() },
                    prefillAmount = prefillAmount,
                    prefillMerchant = prefillMerchant,
                    prefillDateMillis = prefillDateMillis
                )
            }
            
            composable(Screen.ReceiptScanner.route) {
                ReceiptScannerScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onReceiptScanned = { receipt ->
                        val amountStr = receipt.amount?.toString() ?: ""
                        val merchantStr = receipt.merchantName ?: ""
                        val dateStr = receipt.dateMillis?.toString() ?: ""
                        
                        navController.navigate(Screen.AddTransaction.createRoute(
                            amount = amountStr,
                            merchant = merchantStr,
                            date = dateStr
                        )) {
                            popUpTo(Screen.ReceiptScanner.route) { inclusive = true }
                        }
                    }
                )
            }
            
            composable(
                route = Screen.EditTransaction.route,
                arguments = listOf(navArgument("transactionId") { type = NavType.LongType })
            ) { backStackEntry ->
                val transactionId = backStackEntry.arguments?.getLong("transactionId") ?: -1L
                EditTransactionScreen(
                    transactionId = transactionId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable(
                route = Screen.GroupDetail.route,
                arguments = listOf(navArgument("groupId") { type = NavType.LongType })
            ) { backStackEntry ->
                val groupId = backStackEntry.arguments?.getLong("groupId") ?: -1L
                GroupDetailScreen(
                    groupId = groupId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
