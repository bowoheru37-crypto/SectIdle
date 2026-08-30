package com.sect.idle.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sect.idle.data.db.AppDatabase
import com.sect.idle.data.repository.DiscipleRepository
import com.sect.idle.data.repository.IDiscipleRepository
import com.sect.idle.data.repository.ITaskRepository
import com.sect.idle.data.repository.TaskRepository
import com.sect.idle.models.Disciple
import com.sect.idle.systems.AudioManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DisciplesUiState(
    val disciples: List<Disciple> = emptyList(),
    val candidates: List<Disciple> = emptyList(),
    val isLoading: Boolean = false,
    val selectedDisciple: Disciple? = null,
    val statusMessage: String? = null
)

/**
 * DisciplesViewModel injecting IDiscipleRepository and ITaskRepository
 * Exposing reactive StateFlow for UI to observe disciple data changes.
 */
class DisciplesViewModel(
    application: Application,
    private val discipleRepository: IDiscipleRepository = DiscipleRepository(AppDatabase.getInstance(application).discipleDao()),
    private val taskRepository: ITaskRepository = TaskRepository(AppDatabase.getInstance(application).taskDao()),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AndroidViewModel(application) {

    private val audioManager = AudioManager.get(application)

    private val _uiState = MutableStateFlow(DisciplesUiState())
    val uiState: StateFlow<DisciplesUiState> = _uiState.asStateFlow()

    // Dedicated reactive StateFlow directly from repository
    val disciplesFlow: StateFlow<List<Disciple>> = discipleRepository.getDisciplesFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        loadDisciples()
        refreshCandidateRecruits(sectLevel = 1)
    }

    fun loadDisciples() {
        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(isLoading = true) }
            val list = discipleRepository.getAllDisciples()
            _uiState.update { it.copy(disciples = list, isLoading = false) }
        }
    }

    fun refreshCandidateRecruits(sectLevel: Int = 1) {
        viewModelScope.launch(ioDispatcher) {
            val candidates = discipleRepository.generateRecruitmentCandidates(4, sectLevel)
            _uiState.update { it.copy(candidates = candidates) }
        }
    }

    fun recruitDisciple(candidate: Disciple) {
        viewModelScope.launch(ioDispatcher) {
            discipleRepository.saveDisciple(candidate)
            val updated = discipleRepository.getAllDisciples()
            val remainingCandidates = _uiState.value.candidates.filterNot { it.id == candidate.id }
            audioManager.playSfx("breakthrough")
            _uiState.update {
                it.copy(
                    disciples = updated,
                    candidates = remainingCandidates,
                    statusMessage = "🎉 ${candidate.name} joined the sect!"
                )
            }
        }
    }

    fun dismissDisciple(discipleId: String) {
        viewModelScope.launch(ioDispatcher) {
            discipleRepository.deleteDisciple(discipleId)
            val updated = discipleRepository.getAllDisciples()
            _uiState.update {
                it.copy(
                    disciples = updated,
                    statusMessage = "Disciple departed from the mountain."
                )
            }
        }
    }

    fun assignTask(discipleId: String, taskId: Int) {
        viewModelScope.launch(ioDispatcher) {
            discipleRepository.assignTask(discipleId, taskId)
            val updated = discipleRepository.getAllDisciples()
            _uiState.update { it.copy(disciples = updated) }
        }
    }

    fun attemptBreakthrough(discipleId: String) {
        viewModelScope.launch(ioDispatcher) {
            val (success, message) = discipleRepository.attemptBreakthrough(discipleId)
            val updated = discipleRepository.getAllDisciples()
            audioManager.playSfx(if (success) "breakthrough" else "fail")
            _uiState.update {
                it.copy(
                    disciples = updated,
                    statusMessage = message
                )
            }
        }
    }

    fun selectDisciple(disciple: Disciple?) {
        _uiState.update { it.copy(selectedDisciple = disciple) }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }
}
