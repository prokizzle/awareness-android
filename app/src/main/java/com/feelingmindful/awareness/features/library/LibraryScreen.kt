package com.feelingmindful.awareness.features.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.feelingmindful.awareness.domain.entities.Meditation
import com.feelingmindful.awareness.features.library.components.ExpandableCourseCard
import com.feelingmindful.awareness.features.library.components.MeditationCard
import com.feelingmindful.awareness.features.library.components.ReflectionCard
import com.feelingmindful.awareness.features.library.components.SessionCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onNavigateToPlayer: (meditationId: String) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Library") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            SearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = uiState.searchQuery,
                        onQueryChange = viewModel::updateSearchQuery,
                        onSearch = {},
                        expanded = false,
                        onExpandedChange = {},
                        placeholder = { Text("Search meditations, courses, history...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = viewModel::clearSearch) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        }
                    )
                },
                expanded = false,
                onExpandedChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {}

            Spacer(modifier = Modifier.height(8.dp))

            // Remember callback to avoid lambda recreation on recomposition
            val onMeditationTap = remember<(Meditation) -> Unit>(viewModel, onNavigateToPlayer) {
                { meditation ->
                    if (viewModel.isMeditationPlayable(meditation)) {
                        onNavigateToPlayer(meditation.id)
                    }
                }
            }

            // Content
            when {
                uiState.isLoading -> LoadingView()
                uiState.error != null -> ErrorView(
                    error = uiState.error!!,
                    onRetry = viewModel::refresh
                )
                uiState.isSearchQueryEmpty -> BrowseContent(
                    uiState = uiState,
                    viewModel = viewModel,
                    onMeditationTap = onMeditationTap
                )
                else -> SearchResultsContent(
                    uiState = uiState,
                    viewModel = viewModel,
                    onMeditationTap = onMeditationTap
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BrowseContent(
    uiState: LibraryUiState,
    viewModel: LibraryViewModel,
    onMeditationTap: (Meditation) -> Unit
) {
    PullToRefreshBox(
        isRefreshing = uiState.isLoading,
        onRefresh = viewModel::refresh,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Courses Section
            item {
                SectionHeader(title = "Courses")
            }

            if (uiState.courses.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = "book",
                        title = "No courses available"
                    )
                }
            } else {
                items(uiState.courses, key = { it.id }) { course ->
                    ExpandableCourseCard(
                        course = course,
                        isExpanded = uiState.expandedCourseId == course.id,
                        meditations = uiState.courseMeditations[course.id] ?: emptyList(),
                        playedMeditationIds = uiState.completedMeditationIds,
                        onToggle = { viewModel.toggleCourseExpansion(course.id) },
                        onMeditationTap = onMeditationTap
                    )
                }
            }

            // Meditations Section
            if (uiState.standaloneMeditations.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    SectionHeader(title = "Meditations")
                }

                items(uiState.standaloneMeditations, key = { it.id }) { meditation ->
                    MeditationCard(
                        meditation = meditation,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Recent Section
            item {
                Spacer(modifier = Modifier.height(12.dp))
                SectionHeader(title = "Recent")
            }

            if (uiState.recentActivity.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = "clock",
                        title = "No recent activity",
                        subtitle = "Start your first meditation from the Today tab"
                    )
                }
            } else {
                items(uiState.recentActivity, key = { it.id }) { item ->
                    when (item) {
                        is RecentActivityItem.Session -> SessionCard(session = item.session)
                        is RecentActivityItem.Reflection -> ReflectionCard(reflection = item.reflection)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SearchResultsContent(
    uiState: LibraryUiState,
    viewModel: LibraryViewModel,
    onMeditationTap: (Meditation) -> Unit
) {
    if (uiState.isSearching) {
        LoadingView()
        return
    }

    if (!uiState.hasSearchResults) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No results for \"${uiState.searchQuery}\"",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Try a different search term",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Courses
        if (uiState.searchResultCourses.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Courses",
                    count = uiState.searchResultCourses.size
                )
            }

            items(uiState.searchResultCourses, key = { it.id }) { course ->
                ExpandableCourseCard(
                    course = course,
                    isExpanded = uiState.expandedCourseId == course.id,
                    meditations = uiState.courseMeditations[course.id] ?: emptyList(),
                    playedMeditationIds = uiState.completedMeditationIds,
                    onToggle = { viewModel.toggleCourseExpansion(course.id) },
                    onMeditationTap = onMeditationTap
                )
            }
        }

        // Meditations
        if (uiState.searchResultMeditations.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Meditations",
                    count = uiState.searchResultMeditations.size
                )
            }

            items(uiState.searchResultMeditations, key = { it.id }) { meditation ->
                MeditationCard(meditation = meditation)
            }
        }

        // Sessions
        if (uiState.searchResultSessions.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Your Sessions",
                    count = uiState.searchResultSessions.size
                )
            }

            items(uiState.searchResultSessions, key = { it.id }) { session ->
                SessionCard(session = session)
            }
        }

        // Reflections
        if (uiState.searchResultReflections.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Reflections",
                    count = uiState.searchResultReflections.size
                )
            }

            items(uiState.searchResultReflections, key = { it.id }) { reflection ->
                ReflectionCard(reflection = reflection)
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    count: Int? = null
) {
    Text(
        text = if (count != null) "$title ($count)" else title,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading library...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorView(
    error: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Something went wrong",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onRetry) {
                Text("Try Again")
            }
        }
    }
}

@Composable
private fun EmptyStateView(
    icon: String,
    title: String,
    subtitle: String? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Using a simple text placeholder for the icon
        Text(
            text = when (icon) {
                "book" -> "\uD83D\uDCDA"
                "clock" -> "\u23F0"
                else -> "\u2139\uFE0F"
            },
            style = MaterialTheme.typography.displaySmall
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        subtitle?.let {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
