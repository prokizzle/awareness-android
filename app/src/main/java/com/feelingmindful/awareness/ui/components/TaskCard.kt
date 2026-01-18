package com.feelingmindful.awareness.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.feelingmindful.awareness.domain.entities.TaskStatus
import com.feelingmindful.awareness.ui.theme.AwarenessByChrisShanksTheme

@Composable
fun TaskCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    state: TaskStatus,
    showsCheckbox: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEnabled = when {
        showsCheckbox -> state == TaskStatus.AVAILABLE || state == TaskStatus.COMPLETED
        else -> state.isActionable
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (state == TaskStatus.LOCKED) 0.5f else 1f)
            .clickable(enabled = isEnabled, onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (state == TaskStatus.COMPLETED) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = when {
                    state == TaskStatus.LOCKED -> Icons.Default.Lock
                    showsCheckbox && state == TaskStatus.COMPLETED -> Icons.Default.CheckCircle
                    showsCheckbox -> Icons.Outlined.CheckCircle
                    else -> Icons.AutoMirrored.Filled.KeyboardArrowRight
                },
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = when {
                    state == TaskStatus.COMPLETED -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Preview
@Composable
private fun TaskCardPreview() {
    AwarenessByChrisShanksTheme {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TaskCard(
                title = "Meditation",
                subtitle = "10 min guided",
                icon = Icons.Default.CheckCircle,
                state = TaskStatus.AVAILABLE,
                onClick = {}
            )
            TaskCard(
                title = "Cardio",
                subtitle = "30 min movement",
                icon = Icons.Default.CheckCircle,
                state = TaskStatus.COMPLETED,
                showsCheckbox = true,
                onClick = {}
            )
            TaskCard(
                title = "Meals",
                subtitle = "Mindful eating",
                icon = Icons.Default.CheckCircle,
                state = TaskStatus.LOCKED,
                onClick = {}
            )
        }
    }
}
