package com.example.studentexpensetracker.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun SwipeableItemWithActions(
    modifier: Modifier = Modifier,
    isDeleteEnabled: Boolean = true,
    isEditEnabled: Boolean = false,
    cornerRadius: Dp = 20.dp,
    onDelete: () -> Unit = {},
    onEdit: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val swipeOffset = remember { Animatable(0f) }
    val density = LocalDensity.current

    val buttonCount = (if (isEditEnabled) 1 else 0) + (if (isDeleteEnabled) 1 else 0)
    val buttonWidth = 76.dp
    val totalActionsWidth = buttonWidth * buttonCount
    val totalActionsWidthPx = with(density) { totalActionsWidth.toPx() }

    val isSwipeEnabled = buttonCount > 0

    Box(
        modifier = modifier
            .fillMaxWidth()
            .draggable(
                state = rememberDraggableState { delta ->
                    coroutineScope.launch {
                        val current = swipeOffset.value
                        val newTarget = current + delta
                        val clamped = when {
                            newTarget < -totalActionsWidthPx -> {
                                -totalActionsWidthPx + (newTarget + totalActionsWidthPx) * 0.15f
                            }
                            newTarget > 0f -> {
                                newTarget * 0.15f
                            }
                            else -> newTarget
                        }
                        swipeOffset.snapTo(clamped)
                    }
                },
                orientation = Orientation.Horizontal,
                enabled = isSwipeEnabled,
                onDragStopped = { velocity ->
                    val flingThreshold = 500f
                    val target = when {
                        velocity < -flingThreshold -> -totalActionsWidthPx
                        velocity > flingThreshold -> 0f
                        swipeOffset.value < -totalActionsWidthPx * 0.4f -> -totalActionsWidthPx
                        else -> 0f
                    }
                    coroutineScope.launch {
                        swipeOffset.animateTo(
                            targetValue = target,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        )
                    }
                }
            )
    ) {
        // 1. Content Card (stays stationary within the screen margins, never pushed off-screen)
        content()

        if (isSwipeEnabled) {
            val isOpen = swipeOffset.value < -5f

            // 2. Transparent tap overlay to close when tapped on card content
            if (isOpen) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(end = totalActionsWidth)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = {
                                    coroutineScope.launch {
                                        swipeOffset.animateTo(
                                            0f,
                                            spring(
                                                dampingRatio = Spring.DampingRatioLowBouncy,
                                                stiffness = Spring.StiffnessMediumLow
                                            )
                                        )
                                    }
                                }
                            )
                        }
                )
            }

            // 3. Action Buttons Drawer (revealed from the right inside card's shape)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(cornerRadius)),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(
                    modifier = Modifier
                        .width(totalActionsWidth)
                        .fillMaxHeight()
                        .graphicsLayer {
                            translationX = (totalActionsWidthPx + swipeOffset.value).coerceAtLeast(0f)
                        },
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isEditEnabled) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(Color(0xFF263238))
                                .clickable {
                                    coroutineScope.launch {
                                        swipeOffset.animateTo(0f)
                                    }
                                    onEdit()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Edit",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    if (isEditEnabled && isDeleteEnabled) {
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .fillMaxHeight()
                                .background(Color.White.copy(alpha = 0.08f))
                        )
                    }

                    if (isDeleteEnabled) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(Color(0xFF541C1C))
                                .clickable {
                                    coroutineScope.launch {
                                        swipeOffset.animateTo(0f)
                                    }
                                    onDelete()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color(0xFFFF6B6B),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Delete",
                                    color = Color(0xFFFF6B6B),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
