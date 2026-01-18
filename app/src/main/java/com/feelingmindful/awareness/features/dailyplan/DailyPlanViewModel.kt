package com.feelingmindful.awareness.features.dailyplan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.feelingmindful.awareness.domain.entities.DailyPlan
import com.feelingmindful.awareness.domain.entities.Mantra
import com.feelingmindful.awareness.domain.entities.Meditation
import com.feelingmindful.awareness.domain.entities.User
import com.feelingmindful.awareness.domain.entities.WindowType
import com.feelingmindful.awareness.domain.repositories.AuthRepository
import com.feelingmindful.awareness.domain.repositories.MantraRepository
import com.feelingmindful.awareness.domain.repositories.MeditationRepository
import com.feelingmindful.awareness.domain.services.DailyPlanService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

data class DailyPlanUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val dailyPlan: DailyPlan? = null,
    val meditation: Meditation? = null,
    val mantra: Mantra? = null,
    val isMantraLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DailyPlanViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val dailyPlanService: DailyPlanService,
    private val meditationRepository: MeditationRepository,
    private val mantraRepository: MantraRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DailyPlanUiState())
    val uiState: StateFlow<DailyPlanUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.value = _uiState.value.copy(user = user)
                if (user != null) {
                    loadDailyPlan(user.id)
                }
            }
        }
    }

    private suspend fun loadDailyPlan(userId: String) {
        try {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val plan = dailyPlanService.getTodaysPlan(userId)
            val meditation = plan.meditationId?.let { meditationRepository.getMeditation(it) }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                dailyPlan = plan,
                meditation = meditation
            )

            loadMantra()
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = e.message ?: "Failed to load daily plan"
            )
        }
    }

    fun loadMantra() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isMantraLoading = true)
                val hour = Clock.System.now()
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .hour
                val windowType = WindowType.fromHour(hour)
                val mantra = mantraRepository.getRandomMantra(windowType)
                _uiState.value = _uiState.value.copy(
                    mantra = mantra,
                    isMantraLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isMantraLoading = false)
            }
        }
    }

    fun completeMeditation() {
        viewModelScope.launch {
            val user = _uiState.value.user ?: return@launch
            val plan = _uiState.value.dailyPlan ?: return@launch

            try {
                val updated = dailyPlanService.completeMeditation(user.id, plan)
                _uiState.value = _uiState.value.copy(dailyPlan = updated)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun completeCardio(minutes: Int, autoCompleted: Boolean = false) {
        viewModelScope.launch {
            val user = _uiState.value.user ?: return@launch
            val plan = _uiState.value.dailyPlan ?: return@launch

            try {
                val updated = dailyPlanService.completeCardio(user.id, plan, minutes, autoCompleted)
                _uiState.value = _uiState.value.copy(dailyPlan = updated)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun uncompleteCardio() {
        viewModelScope.launch {
            val user = _uiState.value.user ?: return@launch
            val plan = _uiState.value.dailyPlan ?: return@launch

            try {
                val updated = dailyPlanService.uncompleteCardio(user.id, plan)
                _uiState.value = _uiState.value.copy(dailyPlan = updated)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun completeMeals() {
        viewModelScope.launch {
            val user = _uiState.value.user ?: return@launch
            val plan = _uiState.value.dailyPlan ?: return@launch

            try {
                val updated = dailyPlanService.completeMeals(user.id, plan)
                _uiState.value = _uiState.value.copy(dailyPlan = updated)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun uncompleteMeals() {
        viewModelScope.launch {
            val user = _uiState.value.user ?: return@launch
            val plan = _uiState.value.dailyPlan ?: return@launch

            try {
                val updated = dailyPlanService.uncompleteMeals(user.id, plan)
                _uiState.value = _uiState.value.copy(dailyPlan = updated)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun getGreeting(): String {
        val hour = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .hour
        return when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
    }
}
