package com.feelingmindful.awareness.features.dailyplan

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Notifications
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.hilt.navigation.compose.hiltViewModel
import com.feelingmindful.awareness.domain.entities.TaskStatus
import com.feelingmindful.awareness.ui.components.LoadingIndicator
import com.feelingmindful.awareness.ui.components.MantraCard
import com.feelingmindful.awareness.ui.components.PermissionCard
import com.feelingmindful.awareness.ui.components.TaskCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyPlanScreen(
    onNavigateToPlayer: (meditationId: String) -> Unit,
    onNavigateToReflection: () -> Unit,
    viewModel: DailyPlanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Health Connect permission launcher
    val healthPermissions = setOf(
        HealthPermission.getReadPermission(ExerciseSessionRecord::class)
    )

    val healthPermissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { granted ->
        viewModel.onHealthPermissionResult(granted.containsAll(healthPermissions))
    }

    // Notification permission launcher
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onNotificationPermissionResult(granted)
    }

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

                // Health Permission Card (if needed)
                if (uiState.showHealthPermissionCard) {
                    PermissionCard(
                        icon = Icons.Default.Favorite,
                        title = "Track Your Activity",
                        description = "Connect with Health Connect to automatically track your cardio sessions.",
                        buttonText = "Enable Health Connect",
                        onRequestPermission = {
                            try {
                                val status = HealthConnectClient.getSdkStatus(context)
                                if (status == HealthConnectClient.SDK_AVAILABLE) {
                                    healthPermissionLauncher.launch(healthPermissions)
                                } else {
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setPackage("com.android.vending")
                                        data = android.net.Uri.parse(
                                            "market://details?id=com.google.android.apps.healthdata"
                                        )
                                    }
                                    context.startActivity(intent)
                                }
                            } catch (e: Exception) {
                                viewModel.dismissHealthPermissionCard()
                            }
                        },
                        onSkip = { viewModel.dismissHealthPermissionCard() },
                        isGranted = uiState.healthPermissionGranted,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Notification Permission Card (if needed)
                if (uiState.showNotificationPermissionCard) {
                    PermissionCard(
                        icon = Icons.Default.Notifications,
                        title = "Stay Mindful",
                        description = "Receive gentle reminders to complete your daily practice.",
                        buttonText = "Enable Notifications",
                        onRequestPermission = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                viewModel.onNotificationPermissionResult(true)
                            }
                        },
                        onSkip = { viewModel.dismissNotificationPermissionCard() },
                        isGranted = uiState.notificationPermissionGranted,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

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

                // Remember callbacks to avoid lambda recreation on recomposition
                val meditationId = uiState.dailyPlan?.meditationId
                val onMeditationClick: () -> Unit = remember(meditationId) {
                    { meditationId?.let { onNavigateToPlayer(it) }; Unit }
                }

                val cardioStatus = plan?.tasks?.cardio?.status
                val onCardioClick = remember(cardioStatus) {
                    {
                        if (cardioStatus == TaskStatus.COMPLETED) {
                            viewModel.uncompleteCardio()
                        } else {
                            viewModel.completeCardio(30)
                        }
                    }
                }

                val mealsStatus = plan?.tasks?.meals?.status
                val onMealsClick = remember(mealsStatus) {
                    {
                        if (mealsStatus == TaskStatus.COMPLETED) {
                            viewModel.uncompleteMeals()
                        } else {
                            viewModel.completeMeals()
                        }
                    }
                }

                // Pre-compute subtitle strings to avoid string creation during composition
                val meditationSubtitle = remember(uiState.meditation) {
                    uiState.meditation?.let { "${it.durationText} • ${it.title}" }
                        ?: "Guided session"
                }

                val cardioSubtitle = remember(plan?.tasks?.cardio?.minutes) {
                    plan?.tasks?.cardio?.minutes?.let { "$it min completed" }
                        ?: "30 min movement"
                }

                // Meditation Task
                TaskCard(
                    title = "Meditation",
                    subtitle = meditationSubtitle,
                    icon = Icons.Default.PlayArrow,
                    state = plan?.tasks?.meditation?.status ?: TaskStatus.LOCKED,
                    onClick = onMeditationClick
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
                    subtitle = cardioSubtitle,
                    icon = Icons.Default.Favorite,
                    state = plan?.tasks?.cardio?.status ?: TaskStatus.LOCKED,
                    showsCheckbox = true,
                    onClick = onCardioClick
                )

                // Meals Task
                TaskCard(
                    title = "Mindful Meals",
                    subtitle = "Eat with intention",
                    icon = Icons.Default.Star,
                    state = plan?.tasks?.meals?.status ?: TaskStatus.LOCKED,
                    showsCheckbox = true,
                    onClick = onMealsClick
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
