package com.feelingmindful.awareness.features.library

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.feelingmindful.awareness.domain.entities.Course
import com.feelingmindful.awareness.domain.entities.FullReflection
import com.feelingmindful.awareness.domain.entities.Meditation
import com.feelingmindful.awareness.domain.entities.MeditationSession
import com.feelingmindful.awareness.domain.entities.UserProgress
import com.feelingmindful.awareness.domain.repositories.AuthRepository
import com.feelingmindful.awareness.domain.repositories.DailyPlanRepository
import com.feelingmindful.awareness.domain.repositories.MeditationRepository
import com.feelingmindful.awareness.domain.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import javax.inject.Inject

/**
 * Represents an item in the recent activity feed.
 */
sealed class RecentActivityItem {
    abstract val id: String
    abstract val date: Instant

    data class Session(val session: MeditationSession) : RecentActivityItem() {
        override val id: String get() = "session-${session.id}"
        override val date: Instant get() = session.completedAt
    }

    data class Reflection(val reflection: FullReflection) : RecentActivityItem() {
        override val id: String get() = "reflection-${reflection.id}"
        override val date: Instant get() = reflection.createdAt
    }
}

@Immutable
data class LibraryUiState(
    val isLoading: Boolean = true,
    val isSearching: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val courses: ImmutableList<Course> = persistentListOf(),
    val standaloneMeditations: ImmutableList<Meditation> = persistentListOf(),
    val recentActivity: ImmutableList<RecentActivityItem> = persistentListOf(),
    val userProgress: UserProgress = UserProgress(),
    val expandedCourseId: String? = null,
    val courseMeditations: ImmutableMap<String, ImmutableList<Meditation>> = persistentMapOf(),
    // Pre-computed set for efficient lookup (avoids .toSet() in composition)
    val completedMeditationIds: ImmutableSet<String> = persistentSetOf(),
    // Search results
    val searchResultCourses: ImmutableList<Course> = persistentListOf(),
    val searchResultMeditations: ImmutableList<Meditation> = persistentListOf(),
    val searchResultSessions: ImmutableList<MeditationSession> = persistentListOf(),
    val searchResultReflections: ImmutableList<FullReflection> = persistentListOf()
) {
    val isSearchQueryEmpty: Boolean
        get() = searchQuery.trim().isEmpty()

    val hasSearchResults: Boolean
        get() = searchResultCourses.isNotEmpty() ||
                searchResultMeditations.isNotEmpty() ||
                searchResultSessions.isNotEmpty() ||
                searchResultReflections.isNotEmpty()
}

@OptIn(FlowPreview::class)
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val meditationRepository: MeditationRepository,
    private val dailyPlanRepository: DailyPlanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    // Cache for all content (used for search)
    private var allMeditations: List<Meditation> = emptyList()
    private var allSessions: List<MeditationSession> = emptyList()
    private var allReflections: List<FullReflection> = emptyList()

    init {
        setupSearchDebounce()
        loadInitialContent()
    }

    private fun setupSearchDebounce() {
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .distinctUntilChanged()
                .collect { query ->
                    performSearch(query)
                }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        _searchQuery.value = query
    }

    fun clearSearch() {
        _uiState.update {
            it.copy(
                searchQuery = "",
                searchResultCourses = persistentListOf(),
                searchResultMeditations = persistentListOf(),
                searchResultSessions = persistentListOf(),
                searchResultReflections = persistentListOf()
            )
        }
        _searchQuery.value = ""
    }

    fun loadInitialContent() {
        viewModelScope.launch {
            val user = authRepository.currentUser.first() ?: return@launch

            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Load all content in parallel
                val progress = userRepository.getProgress(user.id)
                val courses = meditationRepository.getCourses()
                val meditations = meditationRepository.getAllMeditations(1000)
                val sessions = dailyPlanRepository.getSessions(user.id, 50)
                val reflections = dailyPlanRepository.getReflections(user.id, 50)

                // Cache for search
                allMeditations = meditations
                allSessions = sessions
                allReflections = reflections

                // Filter standalone meditations (no courseId)
                val standalone = meditations.filter { it.courseId == null }

                // Build recent activity
                val recentActivity = buildRecentActivity(sessions, reflections)

                // Pre-compute completed meditation IDs as a Set for efficient lookup
                val completedIds = progress.completedMeditationIds.toImmutableSet()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        userProgress = progress,
                        courses = courses.toImmutableList(),
                        standaloneMeditations = standalone.toImmutableList(),
                        recentActivity = recentActivity.toImmutableList(),
                        completedMeditationIds = completedIds
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Failed to load library")
                }
            }
        }
    }

    fun toggleCourseExpansion(courseId: String) {
        val currentExpanded = _uiState.value.expandedCourseId
        if (currentExpanded == courseId) {
            _uiState.update { it.copy(expandedCourseId = null) }
        } else {
            _uiState.update { it.copy(expandedCourseId = courseId) }
            loadCourseMeditations(courseId)
        }
    }

    private fun loadCourseMeditations(courseId: String) {
        // Skip if already loaded
        if (_uiState.value.courseMeditations.containsKey(courseId)) return

        val course = _uiState.value.courses.find { it.id == courseId } ?: return

        viewModelScope.launch {
            try {
                val meditations = meditationRepository.getMeditations(course.meditationIds)
                // Sort by order in course
                val sorted = meditations.sortedBy { it.orderInCourse ?: 0 }.toImmutableList()

                _uiState.update {
                    it.copy(
                        courseMeditations = (it.courseMeditations.toMutableMap().apply {
                            put(courseId, sorted)
                        }).toImmutableMap()
                    )
                }
            } catch (e: Exception) {
                // Silently fail - user can try expanding again
            }
        }
    }

    fun isMeditationPlayable(meditation: Meditation): Boolean {
        // Standalone meditations are always playable
        if (meditation.courseId == null) return true
        // Course meditations are only playable if already played
        return _uiState.value.userProgress.completedMeditationIds.contains(meditation.id)
    }

    fun refresh() {
        allMeditations = emptyList()
        _uiState.update { it.copy(courseMeditations = persistentMapOf()) }
        loadInitialContent()
    }

    private fun performSearch(query: String) {
        val trimmed = query.trim().lowercase()

        if (trimmed.isEmpty()) {
            _uiState.update {
                it.copy(
                    isSearching = false,
                    searchResultCourses = persistentListOf(),
                    searchResultMeditations = persistentListOf(),
                    searchResultSessions = persistentListOf(),
                    searchResultReflections = persistentListOf()
                )
            }
            return
        }

        _uiState.update { it.copy(isSearching = true) }

        // Search courses
        val matchingCourses = _uiState.value.courses.filter {
            it.title.lowercase().contains(trimmed) ||
                    it.description.lowercase().contains(trimmed)
        }.toImmutableList()

        // Search standalone meditations
        val matchingMeditations = _uiState.value.standaloneMeditations.filter {
            it.title.lowercase().contains(trimmed) ||
                    it.description.lowercase().contains(trimmed)
        }.toImmutableList()

        // Search sessions
        val matchingSessions = allSessions.filter {
            it.meditationTitle.lowercase().contains(trimmed)
        }.toImmutableList()

        // Search reflections
        val matchingReflections = allReflections.filter {
            it.meditationTitle.lowercase().contains(trimmed) ||
                    it.answers.any { answer -> answer.answer.lowercase().contains(trimmed) }
        }.toImmutableList()

        _uiState.update {
            it.copy(
                isSearching = false,
                searchResultCourses = matchingCourses,
                searchResultMeditations = matchingMeditations,
                searchResultSessions = matchingSessions,
                searchResultReflections = matchingReflections
            )
        }
    }

    private fun buildRecentActivity(
        sessions: List<MeditationSession>,
        reflections: List<FullReflection>
    ): List<RecentActivityItem> {
        val items = mutableListOf<RecentActivityItem>()

        sessions.forEach { items.add(RecentActivityItem.Session(it)) }
        reflections.forEach { items.add(RecentActivityItem.Reflection(it)) }

        // Sort by date, newest first
        return items.sortedByDescending { it.date }
    }
}
