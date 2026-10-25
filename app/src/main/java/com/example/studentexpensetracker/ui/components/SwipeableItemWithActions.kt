package com.example.studentexpensetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableItemWithActions(
    isDeleteEnabled: Boolean = true,
    isEditEnabled: Boolean = false,
    onDelete: () -> Unit = {},
    onEdit: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    
    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = { totalDistance -> totalDistance * 0.3f },
        confirmValueChange = { newValue ->
            newValue == SwipeToDismissBoxValue.EndToStart || newValue == SwipeToDismissBoxValue.Settled
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = isDeleteEnabled || isEditEnabled,
        backgroundContent = {
            val isSwipingLeft = dismissState.targetValue == SwipeToDismissBoxValue.EndToStart || dismissState.currentValue == SwipeToDismissBoxValue.EndToStart

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 4.dp), // Match Card's vertical padding exactly
                contentAlignment = Alignment.CenterEnd
            ) {
                if (isSwipingLeft) {
                    // Create an absolutely positioned row that fills the space.
                    // But we push everything inside it to the End (right side).
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            // Clip ONLY the right side of the row to match the card, leave the left raw.
                            .clip(RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp)),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        
                        // THE FIX: This Box takes up ALL remaining space to the left of the buttons.
                        // We color it to match the background color of the app so it acts as an "eraser"
                        // over any artifacts that might get exposed on the far left.
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.background)
                        )

                        // Edit Action
                        if (isEditEnabled) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(80.dp) // Fixed width button
                                    .background(Color(0xFF2B3648))
                                    .clickable { 
                                        coroutineScope.launch {
                                            dismissState.reset()
                                        }
                                        onEdit()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = "Edit",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }

                        // Delete Action
                        if (isDeleteEnabled) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(80.dp) // Fixed width button
                                    .background(Color(0xFF5A2525))
                                    .clickable { 
                                        coroutineScope.launch {
                                            dismissState.reset()
                                        }
                                        onDelete()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color(0xFFEF5350), 
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = "Delete",
                                        color = Color(0xFFEF5350),
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        content = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
                    .pointerInput(dismissState.currentValue) {
                        detectTapGestures(
                            onTap = {
                                if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
                                    coroutineScope.launch {
                                        dismissState.reset() 
                                    }
                                }
                            }
                        )
                    }
            ) {
                content()
            }
        }
    )
}

@Composable
private fun Column(
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = horizontalAlignment,
        content = content
    )
}
