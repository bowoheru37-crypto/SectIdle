package com.sect.idle.data.repository

import com.sect.idle.data.db.dao.BuildingDao
import com.sect.idle.data.db.entities.BuildingEntity
import com.sect.idle.models.Building
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface IBuildingRepository {
    fun getBuildingsFlow(): Flow<List<Building>>
    suspend fun getAllBuildings(): List<Building>
    suspend fun getBuildingByType(type: Int): Building?
    suspend fun saveBuilding(building: Building)
    suspend fun saveAllBuildings(buildings: List<Building>)
    suspend fun upgradeBuilding(type: Int, currentSpiritStones: Long): Pair<Boolean, String>
    suspend fun demolishBuilding(type: Int): Pair<Boolean, String>
    suspend fun assignWorker(type: Int): Boolean
    suspend fun removeWorker(type: Int): Boolean
}

class BuildingRepository(
    private val buildingDao: BuildingDao
) : IBuildingRepository {

    override fun getBuildingsFlow(): Flow<List<Building>> {
        return buildingDao.getAllBuildingsFlow().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getAllBuildings(): List<Building> {
        return buildingDao.getAllBuildings().map { it.toDomainModel() }
    }

    override suspend fun getBuildingByType(type: Int): Building? {
        return buildingDao.getBuildingByType(type)?.toDomainModel()
    }

    override suspend fun saveBuilding(building: Building) {
        buildingDao.insertOrUpdate(BuildingEntity.fromDomainModel(building))
    }

    override suspend fun saveAllBuildings(buildings: List<Building>) {
        buildingDao.insertAll(buildings.map { BuildingEntity.fromDomainModel(it) })
    }

    override suspend fun upgradeBuilding(type: Int, currentSpiritStones: Long): Pair<Boolean, String> {
        val entity = buildingDao.getBuildingByType(type) ?: return Pair(false, "Pavilion not found")
        val b = entity.toDomainModel()

        val cost = b.getUpgradeCost()
        if (currentSpiritStones < cost) {
            return Pair(false, "Insufficient Spirit Stones. Required: $cost")
        }

        if (b.level >= b.maxLevel) {
            return Pair(false, "${b.name} is already at maximum level!")
        }

        b.isBuilt = true
        b.upgrade()
        saveBuilding(b)
        return Pair(true, "🌟 ${b.name} upgraded to Level ${b.level}!")
    }

    override suspend fun demolishBuilding(type: Int): Pair<Boolean, String> {
        val entity = buildingDao.getBuildingByType(type) ?: return Pair(false, "Pavilion not found")
        val b = entity.toDomainModel()
        if (b.level <= 1) {
            return Pair(false, "Foundational level 1 pavilion cannot be dismantled further!")
        }
        b.level -= 1
        b.workers = 0
        b.efficiency = (b.efficiency - 0.2f).coerceAtLeast(1.0f)
        saveBuilding(b)
        return Pair(true, "⚠️ ${b.name} dismantled to Level ${b.level}.")
    }

    override suspend fun assignWorker(type: Int): Boolean {
        val entity = buildingDao.getBuildingByType(type) ?: return false
        val b = entity.toDomainModel()
        if (b.assignWorker()) {
            saveBuilding(b)
            return true
        }
        return false
    }

    override suspend fun removeWorker(type: Int): Boolean {
        val entity = buildingDao.getBuildingByType(type) ?: return false
        val b = entity.toDomainModel()
        if (b.removeWorker()) {
            saveBuilding(b)
            return true
        }
        return false
    }
}
