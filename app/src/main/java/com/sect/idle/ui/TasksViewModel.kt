package com.sect.idle.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sect.idle.data.db.AppDatabase
import com.sect.idle.data.db.entities.TaskEntity
import com.sect.idle.data.repository.ITaskRepository
import com.sect.idle.data.repository.TaskRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TasksUiState(
    val tasks: List<TaskEntity> = emptyList(),
    val isLoading: Boolean = false,
    val statusMessage: String? = null
)

/**
 * TasksViewModel injecting ITaskRepository
 * Exposing reactive StateFlow for UI to observe sect duties and cultivation assignments.
 */
class TasksViewModel(
    application: Application,
    private val taskRepository: ITaskRepository = TaskRepository(AppDatabase.getInstance(application).taskDao()),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(TasksUiState())
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    val tasksFlow: StateFlow<List<TaskEntity>> = taskRepository.getAllTasksFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        loadTasks()
    }

    fun loadTasks() {
        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(isLoading = true) }
            val list = taskRepository.getAllTasks()
            _uiState.update { it.copy(tasks = list, isLoading = false) }
        }
    }

    fun updateWorkerCount(taskId: Int, count: Int) {
        viewModelScope.launch(ioDispatcher) {
            taskRepository.updateWorkerCount(taskId, count)
            val updated = taskRepository.getAllTasks()
            _uiState.update { it.copy(tasks = updated) }
        }
    }

    fun unlockTask(taskId: Int) {
        viewModelScope.launch(ioDispatcher) {
            taskRepository.unlockTask(taskId)
            val updated = taskRepository.getAllTasks()
            _uiState.update { it.copy(tasks = updated) }
        }
    }
}
