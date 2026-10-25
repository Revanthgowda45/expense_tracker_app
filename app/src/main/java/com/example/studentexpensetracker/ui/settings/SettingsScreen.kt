package com.example.studentexpensetracker.ui.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    var notificationsEnabled by remember { mutableStateOf(true) }

    var showClearDataDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var editNameText by remember { mutableStateOf("") }

    var showAllowanceDialog by remember { mutableStateOf(false) }
    var editAllowanceDayText by remember { mutableStateOf("") }

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

            SettingsItem(
                icon = Icons.Default.AccountCircle,
                title = "User Name",
                subtitle = userName ?: "Student",
                onClick = {
                    editNameText = userName ?: "Student"
                    showEditNameDialog = true
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SettingsSectionTitle("Preferences")

            SettingsSwitchItem(
                icon = Icons.Default.DarkMode,
                title = "Dark Theme",
                subtitle = "Toggle dark mode",
                checked = darkThemeEnabled ?: false,
                onCheckedChange = { viewModel.setDarkMode(it) }
            )

            SettingsItem(
                icon = Icons.Default.DateRange,
                title = "Allowance Day",
                subtitle = "Day $allowanceDay of every month",
                onClick = {
                    editAllowanceDayText = allowanceDay.toString()
                    showAllowanceDialog = true
                }
            )

            SettingsSwitchItem(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                subtitle = "Daily reminders and alerts",
                checked = notificationsEnabled,
                onCheckedChange = {
                    notificationsEnabled = it
                    Toast.makeText(context, "Notifications preference updated", Toast.LENGTH_SHORT).show()
                }
            )

            SettingsItem(
                icon = Icons.Default.Paid,
                title = "Currency",
                subtitle = "Indian Rupee (₹)",
                onClick = { Toast.makeText(context, "Multiple currencies coming in Phase 3!", Toast.LENGTH_SHORT).show() }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SettingsSectionTitle("Data & Categories")

            SettingsItem(
                icon = Icons.Default.Category,
                title = "Manage Categories",
                subtitle = "Add, edit, or remove categories",
                onClick = { Toast.makeText(context, "Category management coming in Phase 3!", Toast.LENGTH_SHORT).show() }
            )

            SettingsItem(
                icon = Icons.Default.Backup,
                title = "Backup & Restore",
                subtitle = "Export to JSON / Import from file",
                onClick = { Toast.makeText(context, "Cloud sync coming in Phase 3!", Toast.LENGTH_SHORT).show() }
            )

            SettingsItem(
                icon = Icons.Default.DeleteForever,
                title = "Clear All Data",
                subtitle = "Permanently delete all records",
                onClick = { showClearDataDialog = true },
                isDestructive = true
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SettingsSectionTitle("Security")

            SettingsItem(
                icon = Icons.Default.Lock,
                title = "App Lock",
                subtitle = "Require PIN / Biometric to open",
                onClick = { Toast.makeText(context, "Biometric lock coming in Phase 3!", Toast.LENGTH_SHORT).show() }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SettingsSectionTitle("About")
            
            SettingsItem(
                icon = Icons.Default.Info,
                title = "About App",
                subtitle = "Version 1.0.0",
                onClick = { Toast.makeText(context, "Student Expense Tracker by Pair Programmer AI", Toast.LENGTH_SHORT).show() }
            )
            
            Spacer(modifier = Modifier.height(100.dp))
        }

        // Edit Name Dialog
        if (showEditNameDialog) {
            AlertDialog(
                onDismissRequest = { showEditNameDialog = false },
                title = { Text("Update Name") },
                text = {
                    Column {
                        Text("Enter your name to personalize the app:")
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = editNameText,
                            onValueChange = { editNameText = it },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val newName = editNameText.trim()
                            if (newName.isNotEmpty()) {
                                viewModel.setUserName(newName)
                            } else {
                                viewModel.setUserName("Student")
                            }
                            showEditNameDialog = false
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditNameDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Edit Allowance Day Dialog
        if (showAllowanceDialog) {
            AlertDialog(
                onDismissRequest = { showAllowanceDialog = false },
                title = { Text("Set Allowance Day") },
                text = {
                    Column {
                        Text("On what day of the month do you receive your allowance? (1-31)")
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = editAllowanceDayText,
                            onValueChange = { editAllowanceDayText = it },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val day = editAllowanceDayText.toIntOrNull()
                            if (day != null && day in 1..31) {
                                viewModel.setAllowanceDay(day)
                            }
                            showAllowanceDialog = false
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAllowanceDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Clear Data Confirmation Dialog
        if (showClearDataDialog) {
            AlertDialog(
                onDismissRequest = { showClearDataDialog = false },
                icon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                title = { Text("Clear All Data") },
                text = { Text("Are you sure you want to delete all your transactions? This action cannot be undone and will reset your balances to zero.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.clearAllData()
                            showClearDataDialog = false
                            Toast.makeText(context, "All data successfully cleared", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Delete Everything", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearDataDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    val color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.padding(end = 16.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = color
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDestructive) color.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(end = 16.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
