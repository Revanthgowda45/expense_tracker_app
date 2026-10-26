package com.example.studentexpensetracker.ui.settings

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AccountCircle
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    val darkThemeEnabled by viewModel.isDarkMode.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val allowanceDay by viewModel.allowanceDay.collectAsState()
    val monthlyBudget by viewModel.monthlyBudget.collectAsState()
    val isCustomBudget by viewModel.isCustomBudget.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val survivalAnalysisDays by viewModel.survivalAnalysisDays.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()

    val formatter = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }

    var showClearDataDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var editNameText by remember { mutableStateOf("") }

    var showAllowanceDialog by remember { mutableStateOf(false) }
    var editAllowanceDayText by remember { mutableStateOf("") }
    var allowanceDayError by remember { mutableStateOf<String?>(null) }

    var showBudgetDialog by remember { mutableStateOf(false) }
    var editBudgetText by remember { mutableStateOf("") }
    var budgetError by remember { mutableStateOf<String?>(null) }

    var showSurvivalWindowDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Settings", 
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {

            SettingsSectionTitle("Profile")
            SettingsCard {
                SettingsItem(
                    icon = Icons.Default.AccountCircle,
                    title = "User Name",
                    subtitle = userName ?: "Student",
                    accentColor = Color(0xFF6366F1),
                    onClick = {
                        editNameText = userName ?: "Student"
                        showEditNameDialog = true
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            SettingsSectionTitle("Preferences")
            SettingsCard {
                SettingsSwitchItem(
                    icon = Icons.Default.DarkMode,
                    title = "Dark Theme",
                    subtitle = "Toggle dark mode",
                    checked = darkThemeEnabled ?: false,
                    accentColor = Color(0xFFF59E0B),
                    onCheckedChange = { viewModel.setDarkMode(it) }
                )
                SettingsDivider()
                SettingsItem(
                    icon = Icons.Default.DateRange,
                    title = "Allowance Day",
                    subtitle = "Day $allowanceDay of every month",
                    accentColor = Color(0xFF10B981),
                    onClick = {
                        editAllowanceDayText = allowanceDay.toString()
                        showAllowanceDialog = true
                    }
                )
                SettingsDivider()
                SettingsItem(
                    icon = Icons.Default.AccountBalanceWallet,
                    title = "Monthly Budget",
                    subtitle = if (isCustomBudget) "${formatter.format(monthlyBudget)} (Custom limit)" else "${formatter.format(monthlyBudget)} (Synced with Income)",
                    accentColor = Color(0xFF6366F1),
                    onClick = {
                        editBudgetText = if (monthlyBudget > 0) monthlyBudget.toLong().toString() else ""
                        budgetError = null
                        showBudgetDialog = true
                    }
                )
                SettingsDivider()
                SettingsItem(
                    icon = Icons.Default.DateRange,
                    title = "Survival Calculation Window",
                    subtitle = "Average spend over last $survivalAnalysisDays days",
                    accentColor = Color(0xFF0EA5E9),
                    onClick = {
                        showSurvivalWindowDialog = true
                    }
                )
                SettingsDivider()
                SettingsSwitchItem(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    subtitle = "Daily reminders and alerts",
                    checked = notificationsEnabled,
                    accentColor = Color(0xFFEC4899),
                    onCheckedChange = {
                        viewModel.setNotificationsEnabled(it)
                        Toast.makeText(context, if (it) "Notifications enabled" else "Notifications disabled", Toast.LENGTH_SHORT).show()
                    }
                )
                SettingsDivider()
                SettingsItem(
                    icon = Icons.Default.Paid,
                    title = "Currency",
                    subtitle = "Indian Rupee (₹)",
                    accentColor = Color(0xFF14B8A6),
                    onClick = { Toast.makeText(context, "Multiple currencies coming in Phase 3!", Toast.LENGTH_SHORT).show() }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            SettingsSectionTitle("Data & Categories")
            SettingsCard {
                SettingsItem(
                    icon = Icons.Default.Category,
                    title = "Manage Categories",
                    subtitle = "Add, edit, or remove categories",
                    accentColor = Color(0xFF8B5CF6),
                    onClick = { Toast.makeText(context, "Category management coming in Phase 3!", Toast.LENGTH_SHORT).show() }
                )
                SettingsDivider()
                SettingsItem(
                    icon = Icons.Default.Backup,
                    title = "Backup & Restore",
                    subtitle = "Export to JSON / Import from file",
                    accentColor = Color(0xFF3B82F6),
                    onClick = { Toast.makeText(context, "Cloud sync coming in Phase 3!", Toast.LENGTH_SHORT).show() }
                )
                SettingsDivider()
                SettingsItem(
                    icon = Icons.Default.DeleteForever,
                    title = "Clear All Data",
                    subtitle = "Permanently delete all records",
                    accentColor = Color(0xFFEF4444),
                    onClick = { showClearDataDialog = true },
                    isDestructive = true
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            SettingsSectionTitle("Security")
            SettingsCard {
                SettingsItem(
                    icon = Icons.Default.Lock,
                    title = "App Lock",
                    subtitle = "Require PIN / Biometric to open",
                    accentColor = Color(0xFF6366F1),
                    onClick = { Toast.makeText(context, "Biometric lock coming in Phase 3!", Toast.LENGTH_SHORT).show() }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            SettingsSectionTitle("About")
            SettingsCard {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "About App",
                    subtitle = "Version 1.0.0",
                    accentColor = Color(0xFF64748B),
                    onClick = { Toast.makeText(context, "Student Expense Tracker by Pair Programmer AI", Toast.LENGTH_SHORT).show() }
                )
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }

        // Edit Name Bottom Sheet
        if (showEditNameDialog) {
            ModalBottomSheet(
                onDismissRequest = { showEditNameDialog = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 36.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Update Name",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showEditNameDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Text(
                        text = "Enter your name to personalize the app:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = editNameText,
                        onValueChange = { editNameText = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val newName = editNameText.trim()
                            if (newName.isNotEmpty()) {
                                viewModel.setUserName(newName)
                            } else {
                                viewModel.setUserName("Student")
                            }
                            showEditNameDialog = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Save", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Edit Allowance Day Bottom Sheet
        if (showAllowanceDialog) {
            ModalBottomSheet(
                onDismissRequest = { 
                    showAllowanceDialog = false 
                    allowanceDayError = null
                },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 36.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Set Allowance Day",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { 
                            showAllowanceDialog = false 
                            allowanceDayError = null
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Text(
                        text = "On what day of the month do you receive your allowance? (1-31)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = editAllowanceDayText,
                        onValueChange = { 
                            editAllowanceDayText = it 
                            allowanceDayError = null
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        isError = allowanceDayError != null,
                        supportingText = {
                            allowanceDayError?.let {
                                Text(text = it, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val day = editAllowanceDayText.trim().toIntOrNull()
                            if (day != null && day in 1..31) {
                                viewModel.setAllowanceDay(day)
                                Toast.makeText(context, "Allowance day updated to day $day", Toast.LENGTH_SHORT).show()
                                showAllowanceDialog = false
                                allowanceDayError = null
                            } else {
                                allowanceDayError = "Please enter a valid day between 1 and 31"
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Save", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Edit Monthly Budget Bottom Sheet
        if (showBudgetDialog) {
            ModalBottomSheet(
                onDismissRequest = { 
                    showBudgetDialog = false 
                    budgetError = null
                },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 36.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Monthly Budget",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { 
                            showBudgetDialog = false
                            budgetError = null
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Text(
                        text = "Set your monthly spending target limit. By default, it automatically syncs with your monthly income (${formatter.format(totalIncome)}).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (totalIncome > 0) {
                        OutlinedButton(
                            onClick = {
                                editBudgetText = totalIncome.toLong().toString()
                                budgetError = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Match Monthly Income (${formatter.format(totalIncome)})")
                        }
                    }

                    OutlinedTextField(
                        value = editBudgetText,
                        onValueChange = { 
                            editBudgetText = it 
                            budgetError = null
                        },
                        label = { Text("Budget Amount (₹)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = budgetError != null,
                        supportingText = { budgetError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (isCustomBudget) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.resetMonthlyBudget()
                                    showBudgetDialog = false
                                    Toast.makeText(context, "Budget reset to sync with income", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f).height(52.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Reset")
                            }
                        }

                        Button(
                            onClick = {
                                val amount = editBudgetText.toDoubleOrNull()
                                if (amount == null || amount <= 0) {
                                    budgetError = "Please enter a valid amount > 0"
                                } else {
                                    viewModel.setMonthlyBudget(amount)
                                    showBudgetDialog = false
                                    budgetError = null
                                    Toast.makeText(context, "Monthly budget updated to ${formatter.format(amount)}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(if (isCustomBudget) 1f else 2f).height(52.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Save", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Survival Window Selection Bottom Sheet (7 or 14 days)
        if (showSurvivalWindowDialog) {
            ModalBottomSheet(
                onDismissRequest = { showSurvivalWindowDialog = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 36.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Survival Analysis Window",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showSurvivalWindowDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Text(
                        text = "Choose the spending history window used to project your survival days and daily burn rate:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (survivalAnalysisDays == 7) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else Color.Transparent)
                            .clickable {
                                viewModel.setSurvivalAnalysisDays(7)
                                showSurvivalWindowDialog = false
                                Toast.makeText(context, "Window set to last 7 days", Toast.LENGTH_SHORT).show()
                            }
                            .padding(vertical = 14.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Last 7 Days (Responsive to recent spending)",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (survivalAnalysisDays == 7) FontWeight.Bold else FontWeight.Normal,
                            color = if (survivalAnalysisDays == 7) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (survivalAnalysisDays == 14) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else Color.Transparent)
                            .clickable {
                                viewModel.setSurvivalAnalysisDays(14)
                                showSurvivalWindowDialog = false
                                Toast.makeText(context, "Window set to last 14 days", Toast.LENGTH_SHORT).show()
                            }
                            .padding(vertical = 14.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Last 14 Days (Smoother average)",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (survivalAnalysisDays == 14) FontWeight.Bold else FontWeight.Normal,
                            color = if (survivalAnalysisDays == 14) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Clear Data Confirmation Bottom Sheet
        if (showClearDataDialog) {
            ModalBottomSheet(
                onDismissRequest = { showClearDataDialog = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 36.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.errorContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.DeleteForever,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Clear All Data?",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Are you sure you want to delete all your transactions? This action cannot be undone and will reset your balances to zero.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showClearDataDialog = false },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                viewModel.clearAllData()
                                showClearDataDialog = false
                                Toast.makeText(context, "All data successfully cleared", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Delete Everything")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
    )
}

@Composable
fun SettingsCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        ),
        content = content
    )
}

@Composable
fun SettingsDivider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
        modifier = Modifier.padding(start = 72.dp, end = 16.dp)
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    isDestructive: Boolean = false
) {
    val titleColor = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    val effectiveAccent = if (isDestructive) MaterialTheme.colorScheme.error else accentColor

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(effectiveAccent.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = effectiveAccent,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = titleColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (isDestructive) titleColor.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(accentColor.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
