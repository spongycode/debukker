package com.spongycode.debukker.ui

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

/**
 * A draggable floating debug button that can be placed anywhere on the screen.
 * When clicked, it opens the Debug Menu bottom sheet.
 *
 * Usage:
 * ```
 * DraggableDebugButton()
 * ```
 *
 * This composable should be placed at the root level of your app's UI hierarchy,
 * typically inside a Box that fills the entire screen.
 */
@Composable
fun DraggableDebugButton(
    modifier: Modifier = Modifier
) {
    var isDebugMenuVisible by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val fabSize = 55.dp
        val fabSizePx = with(density) { fabSize.toPx() }
        val maxWidthPx = with(density) { maxWidth.toPx() }
        val maxHeightPx = with(density) { maxHeight.toPx() }

        // Initial position at bottom-end with padding
        val initialOffsetX = maxWidthPx - fabSizePx - with(density) { 20.dp.toPx() }
        val initialOffsetY = maxHeightPx - fabSizePx - with(density) { 100.dp.toPx() }

        var offsetX by remember { mutableStateOf(initialOffsetX) }
        var offsetY by remember { mutableStateOf(initialOffsetY) }

        FloatingActionButton(
            modifier = Modifier
                .size(fabSize)
                .offset { IntOffset(offsetX.toInt(), offsetY.toInt()) }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val newX = (offsetX + dragAmount.x).coerceIn(0f, maxWidthPx - fabSizePx)
                        val newY = (offsetY + dragAmount.y).coerceIn(0f, maxHeightPx - fabSizePx)
                        offsetX = newX
                        offsetY = newY
                    }
                },
            onClick = { isDebugMenuVisible = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.BugReport,
                contentDescription = "Debug Menu"
            )
        }
    }

    DebugMenu(
        isVisible = isDebugMenuVisible,
        onDismiss = { isDebugMenuVisible = false }
    )
}
