package com.example.studentexpensetracker.ui.split

import android.content.Context
import android.content.Intent
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
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
import com.example.studentexpensetracker.data.local.entity.split.ExpenseShareEntity
import com.example.studentexpensetracker.data.local.entity.split.FriendEntity
import com.example.studentexpensetracker.data.local.entity.split.SharedExpenseEntity
import com.example.studentexpensetracker.domain.split.Debt
import com.example.studentexpensetracker.domain.split.SplitCalculator
import com.example.studentexpensetracker.domain.split.SplitType
import com.example.studentexpensetracker.ui.components.SwipeableItemWithActions
import com.example.studentexpensetracker.ui.theme.ExpenseRed
import com.example.studentexpensetracker.ui.theme.IncomeGreen
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    groupId: Long,
    onNavigateBack: () -> Unit,
    viewModel: GroupDetailViewModel = hiltViewModel()
) {
    val group by viewModel.group.collectAsState()
    val members by viewModel.groupMembers.collectAsState()
    val expenses by viewModel.groupExpenses.collectAsState()
    val debts by viewModel.simplifiedDebts.collectAsState()
    val totalSpending by viewModel.totalGroupSpending.collectAsState()
    val memberSummaries by viewModel.memberSummaries.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(groupId) {
        // Active group context
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Expenses", "Balances", "Members")

    var showAddFriendDialog by remember { mutableStateOf(false) }
    var newFriendName by remember { mutableStateOf("") }
    var newFriendPhone by remember { mutableStateOf("") }

    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var expenseToEdit by remember { mutableStateOf<SharedExpenseEntity?>(null) }
    var expenseToDelete by remember { mutableStateOf<SharedExpenseEntity?>(null) }
    var debtToSettle by remember { mutableStateOf<Debt?>(null) }

    // Contact picker for adding member
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri ->
        if (uri != null) {
            val contactInfo = getContactDetailsFromUri(context, uri)
            if (contactInfo != null) {
                newFriendName = contactInfo.first
                newFriendPhone = contactInfo.second ?: ""
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = group?.name ?: "Group Details",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${members.size} members • ${NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(totalSpending)} total",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddFriendDialog = true }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Add Member")
                    }
                    IconButton(onClick = {
                        val shareText = formatDebtsForSharing(group?.name ?: "Group", debts, members)
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Share Balances on WhatsApp")
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
                    Icon(Icons.Default.Add, contentDescription = "Add Shared Expense")
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

            when (selectedTabIndex) {
                0 -> ExpensesTab(
                    expenses = expenses,
                    members = members,
                    totalSpending = totalSpending,
                    onEditExpense = { expenseToEdit = it },
                    onDeleteExpense = { expenseToDelete = it }
                )
                1 -> BalancesTab(
                    groupName = group?.name ?: "Group",
                    debts = debts,
                    members = members,
                    onSettle = { debtToSettle = it },
                    onShare = {
                        val shareText = formatDebtsForSharing(group?.name ?: "Group", debts, members)
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Balances"))
                    }
                )
                2 -> MembersTab(
                    summaries = memberSummaries,
                    onAddMemberClick = { showAddFriendDialog = true }
                )
            }
        }

        // Add Member Bottom Sheet
        if (showAddFriendDialog) {
            ModalBottomSheet(
                onDismissRequest = {
                    showAddFriendDialog = false
                    newFriendName = ""
                    newFriendPhone = ""
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
                            text = "Add Member to Group",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = {
                            showAddFriendDialog = false
                            newFriendName = ""
                            newFriendPhone = ""
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newFriendName,
                            onValueChange = { newFriendName = it },
                            label = { Text("Friend's Name *") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = { contactPickerLauncher.launch(null) }) {
                            Icon(Icons.Default.ContactPhone, contentDescription = "Pick Contact", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    OutlinedTextField(
                        value = newFriendPhone,
                        onValueChange = { newFriendPhone = it },
                        label = { Text("Phone Number (Optional)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (newFriendName.isNotBlank()) {
                                viewModel.addFriendToGroup(newFriendName, newFriendPhone.takeIf { it.isNotBlank() })
                                showAddFriendDialog = false
                                newFriendName = ""
                                newFriendPhone = ""
                            }
                        },
                        enabled = newFriendName.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Add Member", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Add Shared Expense Dialog (Supports All 4 Split Types)
        if (showAddExpenseDialog) {
            AddSharedExpenseDialog(
                members = members,
                onDismiss = { showAddExpenseDialog = false },
                onSaveExpense = { desc, amount, paidById, shares, addPersonal ->
                    viewModel.addSharedExpense(desc, amount, paidById, shares, addPersonal)
                    showAddExpenseDialog = false
                }
            )
        }

        // Edit Shared Expense Dialog
        if (expenseToEdit != null) {
            AddSharedExpenseDialog(
                members = members,
                expenseToEdit = expenseToEdit,
                onDismiss = { expenseToEdit = null },
                onSaveExpense = { desc, amount, paidById, shares, _ ->
                    viewModel.updateSharedExpense(
                        expenseId = expenseToEdit!!.sharedExpenseId,
                        description = desc,
                        totalAmount = amount,
                        paidByFriendId = paidById,
                        shares = shares
                    )
                    expenseToEdit = null
                }
            )
        }

        // Settle Up Dialog (Supports Full and Partial Settlements)
        if (debtToSettle != null) {
            SettleUpDialog(
                debt = debtToSettle!!,
                members = members,
                onDismiss = { debtToSettle = null },
                onConfirmSettlement = { amount ->
                    viewModel.settleDebt(debtToSettle!!.fromId, debtToSettle!!.toId, amount)
                    debtToSettle = null
                }
            )
        }

        // Delete Expense Confirmation Bottom Sheet
        if (expenseToDelete != null) {
            ModalBottomSheet(
                onDismissRequest = { expenseToDelete = null },
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
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Delete Shared Expense?",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Delete '${expenseToDelete?.description}'? Debts and member balances will be recalculated automatically.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { expenseToDelete = null },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                expenseToDelete?.let { viewModel.deleteSharedExpense(it) }
                                expenseToDelete = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExpensesTab(
    expenses: List<SharedExpenseEntity>,
    members: List<FriendEntity>,
    totalSpending: Double,
    onEditExpense: (SharedExpenseEntity) -> Unit = {},
    onDeleteExpense: (SharedExpenseEntity) -> Unit
) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(modifier = Modifier.height(12.dp)) }

        // Hero Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Total Group Spending",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatter.format(totalSpending),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${expenses.size} expenses recorded across ${members.size} members",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        if (expenses.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No expenses added yet.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Tap the + button below to add your first bill.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(expenses, key = { it.sharedExpenseId }) { expense ->
                val payer = members.find { it.friendId == expense.paidByFriendId }?.name ?: "Unknown"

                SwipeableItemWithActions(
                    cornerRadius = 16.dp,
                    isEditEnabled = true,
                    onEdit = { onEditExpense(expense) },
                    onDelete = { onDeleteExpense(expense) }
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { onEditExpense(expense) },
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
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Payments,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = expense.description,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Paid by $payer • ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(expense.dateTime))}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                text = formatter.format(expense.amount),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun BalancesTab(
    groupName: String,
    debts: List<Debt>,
    members: List<FriendEntity>,
    onSettle: (Debt) -> Unit,
    onShare: () -> Unit
) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(modifier = Modifier.height(12.dp)) }

        if (debts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(IncomeGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(44.dp),
                                tint = IncomeGreen
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "You are all settled up! 🎉",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "No pending debts in $groupName.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Simplified Debts (${debts.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(onClick = onShare) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share Balances")
                    }
                }
            }

            items(debts) { debt ->
                val fromName = members.find { it.friendId == debt.fromId }?.name ?: "Unknown"
                val toName = members.find { it.friendId == debt.toId }?.name ?: "Unknown"
                val isUserOwing = fromName.equals("You", ignoreCase = true)
                val isUserReceiving = toName.equals("You", ignoreCase = true)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = fromName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isUserOwing) ExpenseRed else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = toName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isUserReceiving) IncomeGreen else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formatter.format(debt.amount),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
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
                            Text("Settle Up", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun MembersTab(
    summaries: List<MemberBalanceSummary>,
    onAddMemberClick: () -> Unit
) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(modifier = Modifier.height(12.dp)) }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Member Breakdown (${summaries.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = onAddMemberClick) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Member")
                }
            }
        }

        items(summaries, key = { it.friend.friendId }) { summary ->
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = summary.friend.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (!summary.friend.phoneNumber.isNullOrBlank()) {
                                    Text(
                                        text = summary.friend.phoneNumber,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Net status badge
                        val (statusText, statusColor) = when {
                            summary.netBalance > 0.01 -> Pair("+${formatter.format(summary.netBalance)}", IncomeGreen)
                            summary.netBalance < -0.01 -> Pair(formatter.format(summary.netBalance), ExpenseRed)
                            else -> Pair("Settled", MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(statusColor.copy(alpha = 0.12f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Paid: ${formatter.format(summary.totalPaid)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Share: ${formatter.format(summary.totalShare)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSharedExpenseDialog(
    members: List<FriendEntity>,
    expenseToEdit: SharedExpenseEntity? = null,
    onDismiss: () -> Unit,
    onSaveExpense: (
        description: String,
        amount: Double,
        paidByFriendId: Long,
        shares: List<ExpenseShareEntity>,
        addUserShareToPersonal: Boolean
    ) -> Unit
) {
    var description by remember(expenseToEdit) { mutableStateOf(expenseToEdit?.description ?: "") }
    var amountText by remember(expenseToEdit) {
        mutableStateOf(
            expenseToEdit?.let {
                if (it.amount % 1.0 == 0.0) it.amount.toLong().toString() else it.amount.toString()
            } ?: ""
        )
    }
    var selectedPayer by remember(expenseToEdit, members) {
        mutableStateOf(
            if (expenseToEdit != null) {
                members.firstOrNull { it.friendId == expenseToEdit.paidByFriendId } ?: members.firstOrNull()
            } else {
                members.firstOrNull { it.name.equals("You", ignoreCase = true) } ?: members.firstOrNull()
            }
        )
    }
    var payerExpanded by remember { mutableStateOf(false) }

    var selectedSplitType by remember { mutableStateOf(SplitType.EQUALLY) }

    // Equal split: participating members
    val participatingMembers = remember { mutableStateMapOf<Long, Boolean>().apply { members.forEach { put(it.friendId, true) } } }

    // Exact split amounts
    val exactAmounts = remember { mutableStateMapOf<Long, String>() }

    // Percentage split
    val percentageAmounts = remember { mutableStateMapOf<Long, String>() }

    // Shares split
    val sharesRatio = remember { mutableStateMapOf<Long, String>().apply { members.forEach { put(it.friendId, "1") } } }

    var addPersonalShare by remember(expenseToEdit) { mutableStateOf(expenseToEdit == null) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val userMember = members.firstOrNull { it.name.equals("You", ignoreCase = true) }
    val totalAmount = amountText.toDoubleOrNull() ?: 0.0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (expenseToEdit == null) "Add Shared Expense" else "Edit Shared Expense",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it; validationError = null },
                    label = { Text("Expense Description *") },
                    placeholder = { Text("e.g. Dinner, Hotel, Petrol") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it; validationError = null },
                    label = { Text("Total Amount (₹) *") },
                    prefix = { Text("₹") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Paid By Dropdown
                ExposedDropdownMenuBox(
                    expanded = payerExpanded,
                    onExpandedChange = { payerExpanded = !payerExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedPayer?.name ?: "Select Payer",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Paid By") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = payerExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = payerExpanded,
                        onDismissRequest = { payerExpanded = false }
                    ) {
                        members.forEach { member ->
                            DropdownMenuItem(
                                text = { Text(member.name) },
                                onClick = {
                                    selectedPayer = member
                                    payerExpanded = false
                                }
                            )
                        }
                    }
                }

                // Split Type Selection Tabs
                Text(
                    text = "Split Method",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SplitType.values().forEach { type ->
                        FilterChip(
                            selected = selectedSplitType == type,
                            onClick = { selectedSplitType = type; validationError = null },
                            label = {
                                Text(
                                    when (type) {
                                        SplitType.EQUALLY -> "Equally"
                                        SplitType.EXACT_AMOUNTS -> "Exact"
                                        SplitType.PERCENTAGES -> "%"
                                        SplitType.SHARES -> "Shares"
                                    },
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        )
                    }
                }

                // Split inputs based on selected split type
                when (selectedSplitType) {
                    SplitType.EQUALLY -> {
                        val activeCount = participatingMembers.values.count { it }
                        val perPerson = if (activeCount > 0 && totalAmount > 0) totalAmount / activeCount else 0.0
                        Text(
                            text = "Split equally: ₹${"%.2f".format(perPerson)} per person",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )

                        members.forEach { member ->
                            val isChecked = participatingMembers[member.friendId] ?: true
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(member.name, style = MaterialTheme.typography.bodyMedium)
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { participatingMembers[member.friendId] = it }
                                )
                            }
                        }
                    }

                    SplitType.EXACT_AMOUNTS -> {
                        val enteredSum = members.sumOf { exactAmounts[it.friendId]?.toDoubleOrNull() ?: 0.0 }
                        val diff = totalAmount - enteredSum

                        Text(
                            text = "Entered: ₹${"%.2f".format(enteredSum)} / ₹${"%.2f".format(totalAmount)} (Diff: ₹${"%.2f".format(diff)})",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (abs(diff) < 0.01) IncomeGreen else ExpenseRed,
                            fontWeight = FontWeight.SemiBold
                        )

                        members.forEach { member ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(member.name, modifier = Modifier.weight(1f))
                                OutlinedTextField(
                                    value = exactAmounts[member.friendId] ?: "",
                                    onValueChange = { exactAmounts[member.friendId] = it },
                                    prefix = { Text("₹") },
                                    placeholder = { Text("0") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.width(130.dp),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                    }

                    SplitType.PERCENTAGES -> {
                        val enteredPercent = members.sumOf { percentageAmounts[it.friendId]?.toDoubleOrNull() ?: 0.0 }
                        val remainingPercent = 100.0 - enteredPercent

                        Text(
                            text = "Total %: ${"%.1f".format(enteredPercent)}% / 100% (Left: ${"%.1f".format(remainingPercent)}%)",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (abs(remainingPercent) < 0.05) IncomeGreen else ExpenseRed,
                            fontWeight = FontWeight.SemiBold
                        )

                        members.forEach { member ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(member.name, modifier = Modifier.weight(1f))
                                OutlinedTextField(
                                    value = percentageAmounts[member.friendId] ?: "",
                                    onValueChange = { percentageAmounts[member.friendId] = it },
                                    suffix = { Text("%") },
                                    placeholder = { Text("0") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.width(120.dp),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                    }

                    SplitType.SHARES -> {
                        val totalShares = members.sumOf { sharesRatio[it.friendId]?.toDoubleOrNull() ?: 0.0 }
                        Text(
                            text = "Total shares: ${"%.1f".format(totalShares)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )

                        members.forEach { member ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(member.name, modifier = Modifier.weight(1f))
                                OutlinedTextField(
                                    value = sharesRatio[member.friendId] ?: "1",
                                    onValueChange = { sharesRatio[member.friendId] = it },
                                    placeholder = { Text("1") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.width(110.dp),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                    }
                }

                // Add to personal expenses checkbox (if user is part of group)
                if (userMember != null) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = addPersonalShare,
                            onCheckedChange = { addPersonalShare = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add only my own share to my personal expense list",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                if (validationError != null) {
                    Text(
                        text = validationError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                onClick = {
                    if (description.isBlank()) {
                        validationError = "Please enter an expense description"
                        return@Button
                    }
                    if (totalAmount <= 0) {
                        validationError = "Please enter a valid amount greater than zero"
                        return@Button
                    }
                    if (selectedPayer == null) {
                        validationError = "Please select who paid the bill"
                        return@Button
                    }

                    val sharesResult: Result<List<ExpenseShareEntity>> = when (selectedSplitType) {
                        SplitType.EQUALLY -> {
                            val activeMemberIds = members.filter { participatingMembers[it.friendId] == true }.map { it.friendId }
                            if (activeMemberIds.isEmpty()) {
                                Result.failure(IllegalArgumentException("Please select at least one participating member"))
                            } else {
                                Result.success(SplitCalculator.splitEqually(totalAmount, activeMemberIds))
                            }
                        }
                        SplitType.EXACT_AMOUNTS -> {
                            val amountsMap = members.associate { it.friendId to (exactAmounts[it.friendId]?.toDoubleOrNull() ?: 0.0) }
                            SplitCalculator.splitByExactAmounts(totalAmount, amountsMap)
                        }
                        SplitType.PERCENTAGES -> {
                            val percentMap = members.associate { it.friendId to (percentageAmounts[it.friendId]?.toDoubleOrNull() ?: 0.0) }
                            SplitCalculator.splitByPercentages(totalAmount, percentMap)
                        }
                        SplitType.SHARES -> {
                            val ratioMap = members.associate { it.friendId to (sharesRatio[it.friendId]?.toDoubleOrNull() ?: 0.0) }
                            SplitCalculator.splitByShares(totalAmount, ratioMap)
                        }
                    }

                    sharesResult.fold(
                        onSuccess = { calculatedShares ->
                            onSaveExpense(
                                description,
                                totalAmount,
                                selectedPayer!!.friendId,
                                calculatedShares,
                                addPersonalShare
                            )
                        },
                        onFailure = { error ->
                            validationError = error.message ?: "Invalid split configuration"
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (expenseToEdit == null) "Save Expense" else "Update Expense",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettleUpDialog(
    debt: Debt,
    members: List<FriendEntity>,
    onDismiss: () -> Unit,
    onConfirmSettlement: (amount: Double) -> Unit
) {
    val fromName = members.find { it.friendId == debt.fromId }?.name ?: "Unknown"
    val toName = members.find { it.friendId == debt.toId }?.name ?: "Unknown"

    var settleAmountText by remember { mutableStateOf(debt.amount.toString()) }
    var isFullSettlement by remember { mutableStateOf(true) }
    var settleError by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
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
                    text = "Record Settlement",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Text(
                text = "$fromName is paying $toName",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Total outstanding balance: ₹${"%.2f".format(debt.amount)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = isFullSettlement,
                    onClick = {
                        isFullSettlement = true
                        settleAmountText = debt.amount.toString()
                        settleError = null
                    },
                    label = { Text("Full: ₹${"%.2f".format(debt.amount)}") }
                )

                FilterChip(
                    selected = !isFullSettlement,
                    onClick = {
                        isFullSettlement = false
                        settleError = null
                    },
                    label = { Text("Partial Settlement") }
                )
            }

            OutlinedTextField(
                value = settleAmountText,
                onValueChange = {
                    settleAmountText = it
                    isFullSettlement = false
                    settleError = null
                },
                label = { Text("Settlement Amount (₹)") },
                prefix = { Text("₹") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            if (settleError != null) {
                Text(
                    text = settleError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val amount = settleAmountText.toDoubleOrNull() ?: 0.0
                    if (amount <= 0) {
                        settleError = "Amount must be greater than zero"
                    } else if (amount > debt.amount + 0.01) {
                        settleError = "Amount cannot exceed total debt of ₹${"%.2f".format(debt.amount)}"
                    } else {
                        onConfirmSettlement(amount)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Confirm Payment", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun formatDebtsForSharing(groupName: String, debts: List<Debt>, members: List<FriendEntity>): String {
    if (debts.isEmpty()) return "$groupName balance: All settled up! 🎉"

    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    val sb = StringBuilder("$groupName balance:\n")

    val lines = debts.map { debt ->
        val fromName = members.find { it.friendId == debt.fromId }?.name ?: "Unknown"
        val toName = members.find { it.friendId == debt.toId }?.name ?: "Unknown"
        "$fromName owes $toName ${formatter.format(debt.amount)}"
    }

    sb.append(lines.joinToString(" • "))
    return sb.toString()
}

private fun getContactDetailsFromUri(context: Context, uri: android.net.Uri): Pair<String, String?>? {
    return try {
        val cursor = context.contentResolver.query(
            uri,
            arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.HAS_PHONE_NUMBER),
            null,
            null,
            null
        )
        cursor?.use {
            if (it.moveToFirst()) {
                val id = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                val name = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
                val hasPhone = it.getInt(it.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                var phone: String? = null

                if (hasPhone > 0) {
                    val pCursor = context.contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                        "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                        arrayOf(id),
                        null
                    )
                    pCursor?.use { pc ->
                        if (pc.moveToFirst()) {
                            phone = pc.getString(pc.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        }
                    }
                }
                if (!name.isNullOrBlank()) Pair(name, phone) else null
            } else null
        }
    } catch (e: Exception) {
        null
    }
}
