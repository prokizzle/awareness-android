package com.feelingmindful.awareness.features.reflection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.feelingmindful.awareness.domain.entities.ReflectionAnswer
import com.feelingmindful.awareness.domain.entities.ReflectionPrompt
import com.feelingmindful.awareness.domain.repositories.AuthRepository
import com.feelingmindful.awareness.domain.repositories.MeditationRepository
import com.feelingmindful.awareness.domain.services.DailyPlanService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReflectionUiState(
    val isLoading: Boolean = true,
    val prompts: List<ReflectionPrompt> = emptyList(),
    val answers: Map<String, String> = emptyMap(),
    val currentPromptIndex: Int = 0,
    val isSaving: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ReflectionViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val meditationRepository: MeditationRepository,
    private val dailyPlanService: DailyPlanService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReflectionUiState())
    val uiState: StateFlow<ReflectionUiState> = _uiState.asStateFlow()

    init {
        loadPrompts()
    }

    private fun loadPrompts() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val prompts = meditationRepository.getSystemPrompts(emptyList())
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    prompts = prompts
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load prompts"
                )
            }
        }
    }

    fun updateAnswer(promptId: String, answer: String) {
        _uiState.value = _uiState.value.copy(
            answers = _uiState.value.answers + (promptId to answer)
        )
    }

    fun nextPrompt() {
        val currentIndex = _uiState.value.currentPromptIndex
        if (currentIndex < _uiState.value.prompts.size - 1) {
            _uiState.value = _uiState.value.copy(currentPromptIndex = currentIndex + 1)
        }
    }

    fun previousPrompt() {
        val currentIndex = _uiState.value.currentPromptIndex
        if (currentIndex > 0) {
            _uiState.value = _uiState.value.copy(currentPromptIndex = currentIndex - 1)
        }
    }

    fun saveReflections(onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isSaving = true, error = null)

                val user = authRepository.currentUser.first() ?: return@launch
                val plan = dailyPlanService.getTodaysPlan(user.id)

                val answers = _uiState.value.prompts.mapNotNull { prompt ->
                    val answer = _uiState.value.answers[prompt.id]
                    if (answer.isNullOrBlank()) null
                    else ReflectionAnswer(
                        promptId = prompt.id,
                        question = prompt.question,
                        answer = answer
                    )
                }

                dailyPlanService.completeReflection(user.id, plan, answers)
                _uiState.value = _uiState.value.copy(isSaving = false)
                onComplete()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message ?: "Failed to save reflections"
                )
            }
        }
    }

    val canGoNext: Boolean
        get() = _uiState.value.currentPromptIndex < _uiState.value.prompts.size - 1

    val canGoPrevious: Boolean
        get() = _uiState.value.currentPromptIndex > 0

    val isLastPrompt: Boolean
        get() = _uiState.value.currentPromptIndex == _uiState.value.prompts.size - 1
}
