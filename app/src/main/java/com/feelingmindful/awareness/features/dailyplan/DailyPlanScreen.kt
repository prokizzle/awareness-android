package com.feelingmindful.awareness.features.dailyplan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.feelingmindful.awareness.domain.entities.TaskStatus
import com.feelingmindful.awareness.ui.components.LoadingIndicator
import com.feelingmindful.awareness.ui.components.MantraCard
import com.feelingmindful.awareness.ui.components.TaskCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyPlanScreen(
    onNavigateToPlayer: (meditationId: String) -> Unit,
    onNavigateToReflection: () -> Unit,
    viewModel: DailyPlanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = viewModel.getGreeting(),
                            style = MaterialTheme.typography.titleLarge
                        )
                        uiState.user?.displayName?.let { name ->
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            LoadingIndicator(
                message = "Loading your plan...",
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                MantraCard(
                    mantra = uiState.mantra,
                    isLoading = uiState.isMantraLoading,
                    onRefresh = viewModel::loadMantra,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Today's Plan",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                val plan = uiState.dailyPlan

                // Meditation Task
                TaskCard(
                    title = "Meditation",
                    subtitle = uiState.meditation?.let { "${it.durationText} • ${it.title}" }
                        ?: "Guided session",
                    icon = Icons.Default.PlayArrow,
                    state = plan?.tasks?.meditation?.status ?: TaskStatus.LOCKED,
                    onClick = {
                        uiState.dailyPlan?.meditationId?.let { onNavigateToPlayer(it) }
                    }
                )

                // Reflection Task
                TaskCard(
                    title = "Reflection",
                    subtitle = "Journal your thoughts",
                    icon = Icons.Default.Edit,
                    state = plan?.tasks?.reflection?.status ?: TaskStatus.LOCKED,
                    onClick = onNavigateToReflection
                )

                // Cardio Task
                TaskCard(
                    title = "Cardio",
                    subtitle = plan?.tasks?.cardio?.minutes?.let { "$it min completed" }
                        ?: "30 min movement",
                    icon = Icons.Default.Favorite,
                    state = plan?.tasks?.cardio?.status ?: TaskStatus.LOCKED,
                    showsCheckbox = true,
                    onClick = {
                        if (plan?.tasks?.cardio?.status == TaskStatus.COMPLETED) {
                            viewModel.uncompleteCardio()
                        } else {
                            viewModel.completeCardio(30)
                        }
                    }
                )

                // Meals Task
                TaskCard(
                    title = "Mindful Meals",
                    subtitle = "Eat with intention",
                    icon = Icons.Default.Star,
                    state = plan?.tasks?.meals?.status ?: TaskStatus.LOCKED,
                    showsCheckbox = true,
                    onClick = {
                        if (plan?.tasks?.meals?.status == TaskStatus.COMPLETED) {
                            viewModel.uncompleteMeals()
                        } else {
                            viewModel.completeMeals()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
