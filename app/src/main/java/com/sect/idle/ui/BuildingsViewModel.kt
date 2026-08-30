package com.sect.idle.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sect.idle.data.db.AppDatabase
import com.sect.idle.data.repository.BuildingRepository
import com.sect.idle.data.repository.IBuildingRepository
import com.sect.idle.models.Building
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

data class BuildingsUiState(
    val buildings: List<Building> = emptyList(),
    val isLoading: Boolean = false,
    val selectedBuilding: Building? = null,
    val statusMessage: String? = null
)

/**
 * BuildingsViewModel injecting IBuildingRepository
 * Exposing reactive StateFlow for UI to observe building data changes and facility upgrades.
 */
class BuildingsViewModel(
    application: Application,
    private val buildingRepository: IBuildingRepository = BuildingRepository(AppDatabase.getInstance(application).buildingDao()),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AndroidViewModel(application) {

    private val audioManager = AudioManager.get(application)

    private val _uiState = MutableStateFlow(BuildingsUiState())
    val uiState: StateFlow<BuildingsUiState> = _uiState.asStateFlow()

    // Dedicated reactive StateFlow directly from repository
    val buildingsFlow: StateFlow<List<Building>> = buildingRepository.getBuildingsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        loadBuildings()
    }

    fun loadBuildings() {
        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(isLoading = true) }
            val list = buildingRepository.getAllBuildings()
            _uiState.update { it.copy(buildings = list, isLoading = false) }
        }
    }

    fun upgradeBuilding(type: Int, currentSpiritStones: Long, onCostDeducted: ((Long) -> Unit)? = null) {
        viewModelScope.launch(ioDispatcher) {
            val (success, message) = buildingRepository.upgradeBuilding(type, currentSpiritStones)
            val updated = buildingRepository.getAllBuildings()
            if (success) {
                audioManager.playSfx("breakthrough")
                val building = updated.find { it.type == type }
                if (building != null) {
                    onCostDeducted?.invoke(building.getUpgradeCost())
                }
            } else {
                audioManager.playSfx("fail")
            }
            _uiState.update {
                it.copy(
                    buildings = updated,
                    statusMessage = message
                )
            }
        }
    }

    fun assignWorker(type: Int) {
        viewModelScope.launch(ioDispatcher) {
            val success = buildingRepository.assignWorker(type)
            if (success) {
                audioManager.playSfx("gather")
            }
            val updated = buildingRepository.getAllBuildings()
            _uiState.update { it.copy(buildings = updated) }
        }
    }

    fun removeWorker(type: Int) {
        viewModelScope.launch(ioDispatcher) {
            val success = buildingRepository.removeWorker(type)
            val updated = buildingRepository.getAllBuildings()
            _uiState.update { it.copy(buildings = updated) }
        }
    }

    fun selectBuilding(building: Building?) {
        _uiState.update { it.copy(selectedBuilding = building) }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }
}
