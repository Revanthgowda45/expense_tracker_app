package com.example.studentexpensetracker.ui.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.studentexpensetracker.ui.reports.ChartColors
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val totalExpense by viewModel.totalExpense.collectAsState()
    val expensesByCategory by viewModel.expensesByCategory.collectAsState()
    
    // Hardcoded total budgets for phase 2. Phase 3 would let users create these dynamically in the DB.
    var overallBudgetAmount by remember { mutableStateOf(10000.0) }
    val defaultCategoryBudget = 3000.0 

    // Dialog state
    var showEditBudgetDialog by remember { mutableStateOf(false) }
    var newBudgetAmount by remember { mutableStateOf(overallBudgetAmount.toString()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Budget", 
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    ) 
                },
                actions = {
                    IconButton(onClick = { 
                        newBudgetAmount = overallBudgetAmount.toString()
                        showEditBudgetDialog = true 
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Edit Budget")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Overall Monthly Budget",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            item {
                BudgetCategoryCard(
                    categoryName = "Overall Budget",
                    spent = totalExpense,
                    total = overallBudgetAmount,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            item {
                Text(
                    text = "Category Budgets",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            if (expensesByCategory.isEmpty()) {
                item {
                    Text(
                        "Add expenses to track your category budgets.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            } else {
                items(expensesByCategory.entries.toList().sortedByDescending { it.value }.withIndex().toList()) { (index, entry) ->
                    BudgetCategoryCard(
                        categoryName = entry.key,
                        spent = entry.value,
                        total = defaultCategoryBudget,
                        color = ChartColors[index % ChartColors.size]
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        if (showEditBudgetDialog) {
            AlertDialog(
                onDismissRequest = { showEditBudgetDialog = false },
                title = { Text("Set Overall Budget") },
                text = {
                    Column {
                        Text("Enter your target monthly budget amount:")
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = newBudgetAmount,
                            onValueChange = { newBudgetAmount = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            prefix = { Text("₹") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            newBudgetAmount.toDoubleOrNull()?.let {
                                if (it > 0) {
                                    overallBudgetAmount = it
                                }
                            }
                            showEditBudgetDialog = false
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditBudgetDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun BudgetCategoryCard(
    categoryName: String,
    spent: Double,
    total: Double,
    color: Color
) {
    val progress = (spent / total).toFloat().coerceIn(0f, 1f)
    val isOverBudget = spent >= total
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = categoryName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = if (isOverBudget) MaterialTheme.colorScheme.error else color,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${formatter.format(spent)} / ${formatter.format(total)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (isOverBudget) "Over budget" else "${formatter.format(total - spent)} left",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isOverBudget) MaterialTheme.colorScheme.error else Color(0xFF2E7D32)
                )
            }
        }
    }
}
