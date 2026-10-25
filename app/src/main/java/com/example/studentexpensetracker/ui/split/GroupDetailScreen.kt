package com.example.studentexpensetracker.ui.split

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.studentexpensetracker.data.local.entity.split.FriendEntity
import com.example.studentexpensetracker.data.local.entity.split.SharedExpenseEntity
import com.example.studentexpensetracker.domain.split.Debt
import com.example.studentexpensetracker.ui.components.SwipeableItemWithActions
import com.example.studentexpensetracker.ui.theme.ExpenseRed
import com.example.studentexpensetracker.ui.theme.IncomeGreen
import java.lang.StringBuilder
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    groupId: Long,
    onNavigateBack: () -> Unit,
    viewModel: GroupDetailViewModel = hiltViewModel()
) {
    val members by viewModel.groupMembers.collectAsState()
    val expenses by viewModel.groupExpenses.collectAsState()
    val debts by viewModel.simplifiedDebts.collectAsState()
    val context = LocalContext.current
    
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Expenses", "Balances")

    var showAddFriendDialog by remember { mutableStateOf(false) }
    var newFriendName by remember { mutableStateOf("") }

    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var expenseDesc by remember { mutableStateOf("") }
    var expenseAmount by remember { mutableStateOf("") }
    var paidByExpanded by remember { mutableStateOf(false) }
    var paidByFriend by remember { mutableStateOf<FriendEntity?>(null) }
    
    var expenseToDelete by remember { mutableStateOf<SharedExpenseEntity?>(null) }
    
    if (paidByFriend == null && members.isNotEmpty()) {
        paidByFriend = members.firstOrNull { it.name == "You" } ?: members.first()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Group Details", 
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddFriendDialog = true }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Add Friend")
                    }
                    IconButton(onClick = { 
                        val shareText = formatDebtsForSharing(debts, members)
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Share Balances")
                        context.startActivity(shareIntent)
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            if (selectedTabIndex == 0) {
                FloatingActionButton(
                    onClick = { showAddExpenseDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Expense")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            if (selectedTabIndex == 0) {
                ExpensesTab(
                    expenses = expenses, 
                    members = members,
                    onDeleteExpense = { expenseToDelete = it }
                )
            } else {
                BalancesTab(debts, members, onSettle = { debt ->
                    viewModel.settleDebt(debt.fromId, debt.toId, debt.amount)
                })
            }
        }

        if (showAddFriendDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showAddFriendDialog = false
                    newFriendName = "" 
                },
                title = { Text("Add Friend to Group") },
                text = {
                    OutlinedTextField(
                        value = newFriendName,
                        onValueChange = { newFriendName = it },
                        label = { Text("Friend's Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newFriendName.isNotBlank()) {
                                viewModel.addFriendToGroup(newFriendName)
                                showAddFriendDialog = false
                                newFriendName = ""
                            }
                        }
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        showAddFriendDialog = false
                        newFriendName = ""
                    }) {
                        Text("Cancel")
                    }
                },
                shape = RoundedCornerShape(24.dp),
                containerColor = MaterialTheme.colorScheme.surface
            )
        }

        if (showAddExpenseDialog) {
            AlertDialog(
                onDismissRequest = { showAddExpenseDialog = false },
                title = { Text("Add Shared Expense") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = expenseDesc,
                            onValueChange = { expenseDesc = it },
                            label = { Text("Description") },
                            placeholder = { Text("e.g. Dinner at Cafe") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = expenseAmount,
                            onValueChange = { expenseAmount = it },
                            label = { Text("Total Amount") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            prefix = { Text("₹") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        ExposedDropdownMenuBox(
                            expanded = paidByExpanded,
                            onExpandedChange = { paidByExpanded = !paidByExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = paidByFriend?.name ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Paid By") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paidByExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = paidByExpanded,
                                onDismissRequest = { paidByExpanded = false }
                            ) {
                                members.forEach { member ->
                                    DropdownMenuItem(
                                        text = { Text(member.name) },
                                        onClick = {
                                            paidByFriend = member
                                            paidByExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        Text(
                            text = "Split equally among all ${members.size} members.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val amount = expenseAmount.toDoubleOrNull() ?: 0.0
                            if (amount > 0 && expenseDesc.isNotBlank() && paidByFriend != null) {
                                viewModel.addSharedExpense(
                                    description = expenseDesc,
                                    totalAmount = amount,
                                    paidByFriendId = paidByFriend!!.friendId,
                                    memberIds = members.map { it.friendId } 
                                )
                                showAddExpenseDialog = false
                                expenseDesc = ""
                                expenseAmount = ""
                            }
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddExpenseDialog = false }) {
                        Text("Cancel")
                    }
                },
                shape = RoundedCornerShape(24.dp),
                containerColor = MaterialTheme.colorScheme.surface
            )
        }
        
        // Delete Expense Confirmation Dialog
        if (expenseToDelete != null) {
            AlertDialog(
                onDismissRequest = { expenseToDelete = null },
                icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                title = { Text("Delete Shared Expense") },
                text = { Text("Are you sure you want to delete '${expenseToDelete?.description}'? This will recalculate everyone's balances.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            expenseToDelete?.let { viewModel.deleteSharedExpense(it) }
                            expenseToDelete = null
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { expenseToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExpensesTab(
    expenses: List<SharedExpenseEntity>, 
    members: List<FriendEntity>,
    onDeleteExpense: (SharedExpenseEntity) -> Unit
) {
    if (expenses.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No shared expenses yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }
            
            items(expenses, key = { it.sharedExpenseId }) { expense ->
                val payerName = members.find { it.friendId == expense.paidByFriendId }?.name ?: "Unknown"
                
                SwipeableItemWithActions(
                    onDelete = { onDeleteExpense(expense) }
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { },
                                onLongClick = { onDeleteExpense(expense) }
                            ),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = expense.description,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Paid by $payerName on ${SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(expense.dateTime))}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = formatter.format(expense.amount),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun BalancesTab(debts: List<Debt>, members: List<FriendEntity>, onSettle: (Debt) -> Unit) {
    if (debts.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp).padding(bottom = 16.dp),
                    tint = IncomeGreen
                )
                Text(
                    text = "You are all settled up!",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    } else {
        val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }
            
            items(debts) { debt ->
                val fromName = members.find { it.friendId == debt.fromId }?.name ?: "Unknown"
                val toName = members.find { it.friendId == debt.toId }?.name ?: "Unknown"
                val isUserOwing = fromName == "You"
                val isUserReceiving = toName == "You"
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "$fromName owes $toName",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = formatter.format(debt.amount),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        isUserOwing -> ExpenseRed
                                        isUserReceiving -> IncomeGreen
                                        else -> MaterialTheme.colorScheme.primary
                                    }
                                )
                            }
                            
                            Button(
                                onClick = { onSettle(debt) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Settle", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatDebtsForSharing(debts: List<Debt>, members: List<FriendEntity>): String {
    if (debts.isEmpty()) return "We are all settled up! 🎉"
    
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    val sb = StringBuilder("Group Balances:\n\n")
    
    debts.forEach { debt ->
        val fromName = members.find { it.friendId == debt.fromId }?.name ?: "Unknown"
        val toName = members.find { it.friendId == debt.toId }?.name ?: "Unknown"
        sb.append("• $fromName owes $toName ${formatter.format(debt.amount)}\n")
    }
    
    return sb.toString()
}
