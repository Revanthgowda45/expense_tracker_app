package com.example.studentexpensetracker.ui.split

import android.content.Context
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.studentexpensetracker.data.local.entity.split.ExpenseGroupEntity
import com.example.studentexpensetracker.data.local.entity.split.FriendEntity
import com.example.studentexpensetracker.ui.components.SwipeableItemWithActions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitScreen(
    viewModel: SplitViewModel = hiltViewModel(),
    onNavigateToGroup: (Long) -> Unit
) {
    val groups by viewModel.allGroups.collectAsState()
    val context = LocalContext.current
    
    var showCreateDialog by remember { mutableStateOf(false) }
    var newGroupName by remember { mutableStateOf("") }
    var newGroupDesc by remember { mutableStateOf("") }
    val newGroupMembers = remember { mutableStateListOf("You") }
    var memberInputName by remember { mutableStateOf("") }

    var groupToDelete by remember { mutableStateOf<ExpenseGroupEntity?>(null) }
    var groupToEdit by remember { mutableStateOf<ExpenseGroupEntity?>(null) }
    
    var editGroupName by remember { mutableStateOf("") }
    var editGroupDesc by remember { mutableStateOf("") }

    // Contact picker launcher
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri ->
        if (uri != null) {
            val contactName = getContactNameFromUri(context, uri)
            if (!contactName.isNullOrBlank() && !newGroupMembers.contains(contactName)) {
                newGroupMembers.add(contactName)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            "Split Bills with Friends", 
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            "Track shared trips, rent & dining",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        newGroupName = ""
                        newGroupDesc = ""
                        newGroupMembers.clear()
                        newGroupMembers.add("You")
                        showCreateDialog = true 
                    }) {
                        Icon(Icons.Default.GroupAdd, contentDescription = "Create Group")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    newGroupName = ""
                    newGroupDesc = ""
                    newGroupMembers.clear()
                    newGroupMembers.add("You")
                    showCreateDialog = true
                },
                icon = { Icon(Icons.Default.GroupAdd, contentDescription = null) },
                text = { Text("New Group", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            if (groups.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(bottom = 80.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Expense Groups Yet",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Create groups like 'Goa Trip' or 'Hostel 204' to split expenses effortlessly with friends.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 32.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { 
                                newGroupName = ""
                                newGroupDesc = ""
                                newGroupMembers.clear()
                                newGroupMembers.add("You")
                                showCreateDialog = true 
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create First Group")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                    
                    items(groups, key = { it.groupId }) { group ->
                        SwipeableItemWithActions(
                            isEditEnabled = true,
                            onDelete = { groupToDelete = group },
                            onEdit = { 
                                groupToEdit = group
                                editGroupName = group.name
                                editGroupDesc = group.description ?: ""
                            }
                        ) {
                            GroupCard(
                                group = group, 
                                onClick = { onNavigateToGroup(group.groupId) },
                                onLongClick = { groupToDelete = group }
                            )
                        }
                    }
                    
                    item { Spacer(modifier = Modifier.height(100.dp)) }
                }
            }
        }

        // Create Group Bottom Sheet
        if (showCreateDialog) {
            ModalBottomSheet(
                onDismissRequest = { showCreateDialog = false },
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
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Create Expense Group",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(onClick = { showCreateDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    // Category Presets
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        listOf(
                            "🏖️ Trip",
                            "🏠 Hostel",
                            "🍕 Dining",
                            "🚗 Travel",
                            "🎉 Party",
                            "🎓 College"
                        ).forEach { tag ->
                            val keyword = tag.substringAfter(" ")
                            SuggestionChip(
                                onClick = {
                                    if (newGroupName.isBlank()) {
                                        newGroupName = keyword
                                    }
                                },
                                label = { Text(tag, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = newGroupName,
                        onValueChange = { newGroupName = it },
                        label = { Text("Group Name *") },
                        placeholder = { Text("e.g., Goa Trip, Hostel Room 204") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = newGroupDesc,
                        onValueChange = { newGroupDesc = it },
                        label = { Text("Description (Optional)") },
                        placeholder = { Text("e.g., Weekend getaway with college friends") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Text(
                        text = "Group Members (${newGroupMembers.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Member chips row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(newGroupMembers) { member ->
                            InputChip(
                                selected = false,
                                onClick = {},
                                label = { Text(member) },
                                trailingIcon = {
                                    if (member != "You") {
                                        IconButton(
                                            onClick = { newGroupMembers.remove(member) },
                                            modifier = Modifier.size(16.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            )
                        }
                    }

                    // Add friend input with Contact Picker
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = memberInputName,
                            onValueChange = { memberInputName = it },
                            placeholder = { Text("Friend's name") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FilledTonalIconButton(
                            onClick = {
                                val name = memberInputName.trim()
                                if (name.isNotEmpty() && !newGroupMembers.contains(name)) {
                                    newGroupMembers.add(name)
                                    memberInputName = ""
                                }
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Member")
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        FilledTonalIconButton(
                            onClick = { contactPickerLauncher.launch(null) }
                        ) {
                            Icon(Icons.Default.ContactPhone, contentDescription = "Pick Contact")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (newGroupName.isNotBlank()) {
                                val friends = newGroupMembers.map { name ->
                                    FriendEntity(name = name, phoneNumber = null)
                                }
                                viewModel.createGroup(
                                    name = newGroupName, 
                                    description = newGroupDesc.takeIf { it.isNotBlank() },
                                    friends = friends
                                )
                                showCreateDialog = false
                            }
                        },
                        enabled = newGroupName.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Create Group", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        // Edit Group Bottom Sheet
        if (groupToEdit != null) {
            ModalBottomSheet(
                onDismissRequest = { groupToEdit = null },
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
                            text = "Edit Group",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { groupToEdit = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    OutlinedTextField(
                        value = editGroupName,
                        onValueChange = { editGroupName = it },
                        label = { Text("Group Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = editGroupDesc,
                        onValueChange = { editGroupDesc = it },
                        label = { Text("Description (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (editGroupName.isNotBlank()) {
                                viewModel.updateGroup(
                                    groupToEdit!!.copy(
                                        name = editGroupName.trim(),
                                        description = editGroupDesc.trim().takeIf { it.isNotBlank() }
                                    )
                                )
                                groupToEdit = null
                            }
                        },
                        enabled = editGroupName.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Save Changes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        // Delete Group Bottom Sheet
        if (groupToDelete != null) {
            ModalBottomSheet(
                onDismissRequest = { groupToDelete = null },
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
                            text = "Delete Group?",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Are you sure you want to delete '${groupToDelete?.name}'? All shared expenses, member shares, and settlements will be permanently removed.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { groupToDelete = null },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                groupToDelete?.let { viewModel.deleteGroup(it) }
                                groupToDelete = null
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

data class GroupVisual(
    val icon: ImageVector?,
    val initials: String,
    val containerColor: Color,
    val contentColor: Color
)

fun getGroupVisual(name: String): GroupVisual {
    val lower = name.lowercase(Locale.ROOT)
    val words = name.trim().split(Regex("""\s+""")).filter { it.isNotBlank() }
    val initials = when {
        words.size >= 2 -> words.take(2).map { it.first().uppercaseChar() }.joinToString("")
        words.size == 1 && words[0].length >= 2 -> words[0].take(2).uppercase(Locale.ROOT)
        words.size == 1 -> words[0].take(1).uppercase(Locale.ROOT)
        else -> "GR"
    }

    return when {
        lower.contains("trip") || lower.contains("goa") || lower.contains("beach") || lower.contains("tour") || lower.contains("vacation") -> {
            GroupVisual(
                icon = Icons.Default.BeachAccess,
                initials = initials,
                containerColor = Color(0xFF0284C7).copy(alpha = 0.18f),
                contentColor = Color(0xFF0284C7)
            )
        }
        lower.contains("hostel") || lower.contains("room") || lower.contains("flat") || lower.contains("rent") || lower.contains("home") || lower.contains("pg") -> {
            GroupVisual(
                icon = Icons.Default.Home,
                initials = initials,
                containerColor = Color(0xFF7C3AED).copy(alpha = 0.18f),
                contentColor = Color(0xFF7C3AED)
            )
        }
        lower.contains("food") || lower.contains("dinner") || lower.contains("lunch") || lower.contains("cafe") || lower.contains("pizza") || lower.contains("biryani") || lower.contains("party") || lower.contains("drinks") || lower.contains("dining") -> {
            GroupVisual(
                icon = Icons.Default.Restaurant,
                initials = initials,
                containerColor = Color(0xFFEA580C).copy(alpha = 0.18f),
                contentColor = Color(0xFFEA580C)
            )
        }
        lower.contains("car") || lower.contains("drive") || lower.contains("cab") || lower.contains("travel") || lower.contains("road") -> {
            GroupVisual(
                icon = Icons.Default.DirectionsCar,
                initials = initials,
                containerColor = Color(0xFF059669).copy(alpha = 0.18f),
                contentColor = Color(0xFF059669)
            )
        }
        lower.contains("movie") || lower.contains("cinema") || lower.contains("show") -> {
            GroupVisual(
                icon = Icons.Default.Movie,
                initials = initials,
                containerColor = Color(0xFFDB2777).copy(alpha = 0.18f),
                contentColor = Color(0xFFDB2777)
            )
        }
        lower.contains("project") || lower.contains("college") || lower.contains("study") -> {
            GroupVisual(
                icon = Icons.Default.School,
                initials = initials,
                containerColor = Color(0xFF2563EB).copy(alpha = 0.18f),
                contentColor = Color(0xFF2563EB)
            )
        }
        else -> {
            val palette = listOf(
                Color(0xFF6366F1), Color(0xFF0EA5E9), Color(0xFF10B981),
                Color(0xFFF59E0B), Color(0xFFEC4899), Color(0xFF8B5CF6)
            )
            val color = palette[Math.abs(name.hashCode()) % palette.size]
            GroupVisual(
                icon = null,
                initials = initials,
                containerColor = color.copy(alpha = 0.18f),
                contentColor = color
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GroupCard(
    group: ExpenseGroupEntity,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val visual = getGroupVisual(group.name)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(visual.containerColor),
                contentAlignment = Alignment.Center
            ) {
                if (visual.icon != null) {
                    Icon(
                        imageVector = visual.icon,
                        contentDescription = null,
                        tint = visual.contentColor,
                        modifier = Modifier.size(28.dp)
                    )
                } else {
                    Text(
                        text = visual.initials,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = visual.contentColor
                    )
                }
            }
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = group.description?.ifBlank { null }
                        ?: "Created ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(group.createdAt))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun getContactNameFromUri(context: Context, uri: android.net.Uri): String? {
    return try {
        val cursor = context.contentResolver.query(
            uri,
            arrayOf(ContactsContract.Contacts.DISPLAY_NAME),
            null,
            null,
            null
        )
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                if (nameIndex != -1) it.getString(nameIndex) else null
            } else null
        }
    } catch (e: Exception) {
        null
    }
}
