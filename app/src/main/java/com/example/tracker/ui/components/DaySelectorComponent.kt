package com.example.tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Helper Composable for Day Selector
@Composable
fun DaySelector(day: String, isSelected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val backgroundColor =
        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
    val contentColor = MaterialTheme.colorScheme.onTertiary

    Box(
        modifier = Modifier
            .size(40.dp)
            .background(
                // Adjust alpha if disabled
                color = if (enabled) backgroundColor else backgroundColor.copy(alpha = 0.5f),
                shape = RoundedCornerShape(30),
            )
            .clickable(
                enabled = enabled,
                indication = null,
                interactionSource = interactionSource, // Prevents click animations
                onClick = onClick
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = day,
            // Adjust alpha if disabled
            color = if (enabled) contentColor else contentColor.copy(alpha = 0.5f),
            fontWeight = FontWeight.Normal
        )
    }
}