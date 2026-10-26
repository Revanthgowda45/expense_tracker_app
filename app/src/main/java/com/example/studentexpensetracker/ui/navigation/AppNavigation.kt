package com.example.studentexpensetracker.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
            AnimatedVisibility(
                visible = showBottomBarAndFab,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    bottomNavItems.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            icon = {
                                screen.icon?.let {
                                    Icon(
                                        imageVector = it,
                                        contentDescription = null,
                                        modifier = Modifier.size(if (selected) 26.dp else 24.dp)
                                    )
                                }
                            },
                            label = { 
                                Text(
                                    text = stringResource(screen.titleResId),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                ) 
                            },
                            selected = selected,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                            ),
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
            val showTxFabs = currentDestination?.route == Screen.Home.route || currentDestination?.route == Screen.Transactions.route
            if (showTxFabs) {
                Column(horizontalAlignment = Alignment.End) {
                    SmallFloatingActionButton(
                        onClick = { 
                            navController.navigate(Screen.ReceiptScanner.route)
                        },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.secondary,
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                        shape = CircleShape,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Scan Receipt",
                            modifier = Modifier.size(20.dp)
                        )
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
                    },
                    onNavigateToReports = {
                        navController.navigate(Screen.Reports.route)
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
                    navArgument("date") { type = NavType.StringType; defaultValue = "" },
                    navArgument("category") { type = NavType.StringType; defaultValue = "" },
                    navArgument("receiptPath") { type = NavType.StringType; defaultValue = "" }
                )
            ) { backStackEntry ->
                val rawAmount = backStackEntry.arguments?.getString("amount") ?: ""
                val rawMerchant = backStackEntry.arguments?.getString("merchant") ?: ""
                val rawDateStr = backStackEntry.arguments?.getString("date") ?: ""
                val rawCategory = backStackEntry.arguments?.getString("category") ?: ""
                val rawReceiptPath = backStackEntry.arguments?.getString("receiptPath") ?: ""
                
                val prefillAmount = try { java.net.URLDecoder.decode(rawAmount, "UTF-8") } catch (e: Exception) { rawAmount }
                val prefillMerchant = try { java.net.URLDecoder.decode(rawMerchant, "UTF-8") } catch (e: Exception) { rawMerchant }
                val prefillDateStr = try { java.net.URLDecoder.decode(rawDateStr, "UTF-8") } catch (e: Exception) { rawDateStr }
                val prefillCategory = try { java.net.URLDecoder.decode(rawCategory, "UTF-8") } catch (e: Exception) { rawCategory }
                val prefillReceiptPath = try { java.net.URLDecoder.decode(rawReceiptPath, "UTF-8") } catch (e: Exception) { rawReceiptPath }
                val prefillDateMillis = prefillDateStr.toLongOrNull()
                
                AddTransactionScreen(
                    onNavigateBack = { navController.popBackStack() },
                    prefillAmount = prefillAmount,
                    prefillMerchant = prefillMerchant,
                    prefillDateMillis = prefillDateMillis,
                    prefillCategory = prefillCategory,
                    prefillReceiptPath = prefillReceiptPath
                )
            }

            
            composable(Screen.ReceiptScanner.route) {
                ReceiptScannerScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onReceiptScanned = { receipt ->
                        val amountStr = receipt.amount?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: ""
                        val merchantStr = receipt.merchantName ?: ""
                        val dateStr = receipt.dateMillis?.toString() ?: ""
                        val categoryStr = receipt.suggestedCategory ?: ""
                        val receiptPathStr = receipt.receiptImagePath ?: ""
                        
                        navController.navigate(Screen.AddTransaction.createRoute(
                            amount = amountStr,
                            merchant = merchantStr,
                            date = dateStr,
                            category = categoryStr,
                            receiptPath = receiptPathStr
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
