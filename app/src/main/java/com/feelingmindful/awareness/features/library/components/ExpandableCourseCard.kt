package com.feelingmindful.awareness.features.library.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.feelingmindful.awareness.domain.entities.Course
import com.feelingmindful.awareness.domain.entities.Meditation

/**
 * Expandable card showing a course with inline meditation list.
 * Matches the iOS ExpandableCourseCard design.
 */
@Composable
fun ExpandableCourseCard(
    course: Course,
    isExpanded: Boolean,
    meditations: List<Meditation>,
    playedMeditationIds: Set<String>,
    onToggle: () -> Unit,
    onMeditationTap: (Meditation) -> Unit,
    modifier: Modifier = Modifier
) {
    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 90f else 0f,
        label = "chevronRotation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column {
            // Header
            CourseHeader(
                course = course,
                isExpanded = isExpanded,
                chevronRotation = chevronRotation,
                onClick = onToggle
            )

            // Expanded content
            if (isExpanded) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )

                MeditationsList(
                    meditations = meditations,
                    playedMeditationIds = playedMeditationIds,
                    onMeditationTap = onMeditationTap
                )
            }
        }
    }
}

@Composable
private fun CourseHeader(
    course: Course,
    isExpanded: Boolean,
    chevronRotation: Float,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (course.isIntro) Icons.Default.Star else Icons.Default.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Text content
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = course.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = course.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${course.meditationIds.size} meditations",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )

                if (course.isIntro) {
                    IntroBadge()
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Chevron
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = if (isExpanded) "Collapse" else "Expand",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.rotate(chevronRotation)
        )
    }
}

@Composable
private fun IntroBadge() {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    ) {
        Text(
            text = "Intro",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun MeditationsList(
    meditations: List<Meditation>,
    playedMeditationIds: Set<String>,
    onMeditationTap: (Meditation) -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        if (meditations.isEmpty()) {
            // Loading state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            }
        } else {
            // Remember expensive computation to avoid recalculating on every recomposition
            val hasAnyPlayed = remember(meditations, playedMeditationIds) {
                meditations.any { playedMeditationIds.contains(it.id) }
            }

            if (!hasAnyPlayed) {
                Text(
                    text = "Continue this course from Today",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            meditations.forEachIndexed { index, meditation ->
                val isPlayable = playedMeditationIds.contains(meditation.id)
                // Remember onClick to avoid lambda recreation
                val onClick = remember(isPlayable, meditation) {
                    { if (isPlayable) onMeditationTap(meditation) }
                }
                MeditationRow(
                    meditation = meditation,
                    dayNumber = index + 1,
                    isPlayable = isPlayable,
                    onClick = onClick
                )
            }
        }
    }
}

@Composable
private fun MeditationRow(
    meditation: Meditation,
    dayNumber: Int,
    isPlayable: Boolean,
    onClick: () -> Unit
) {
    // Pre-compute strings to avoid recreation on recomposition
    val titleText = remember(dayNumber, meditation.title) {
        "Day $dayNumber: ${meditation.title}"
    }
    val durationText = remember(meditation.duration) {
        "${meditation.duration / 60} min"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isPlayable, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .alpha(if (isPlayable) 1f else 0.5f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Status icon
        Icon(
            imageVector = if (isPlayable) Icons.Default.CheckCircle else Icons.Default.Lock,
            contentDescription = if (isPlayable) "Completed" else "Locked",
            tint = if (isPlayable) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            },
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Title
        Text(
            text = titleText,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isPlayable) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Duration
        Text(
            text = durationText,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}
