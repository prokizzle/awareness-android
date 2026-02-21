package com.feelingmindful.awareness.features.playback

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.feelingmindful.awareness.domain.entities.Meditation
import com.feelingmindful.awareness.domain.repositories.MeditationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerUiState(
    val isLoading: Boolean = true,
    val meditation: Meditation? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0,
    val durationMs: Long = 0,
    val error: String? = null
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val meditationRepository: MeditationRepository
) : ViewModel() {

    private val meditationId: String = checkNotNull(savedStateHandle["meditationId"])

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    init {
        loadMeditation()
    }

    private fun loadMeditation() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val meditation = meditationRepository.getMeditation(meditationId)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    meditation = meditation,
                    durationMs = meditation.duration * 1000L
                )
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load meditation"
                )
            }
        }
    }

    fun togglePlayPause() {
        _uiState.value = _uiState.value.copy(isPlaying = !_uiState.value.isPlaying)
    }

    fun seekTo(positionMs: Long) {
        _uiState.value = _uiState.value.copy(currentPositionMs = positionMs)
    }

    fun updatePosition(positionMs: Long) {
        _uiState.value = _uiState.value.copy(currentPositionMs = positionMs)
    }

    fun onPlaybackComplete() {
        _uiState.value = _uiState.value.copy(isPlaying = false)
    }
}
