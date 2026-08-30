package com.sect.idle.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sect.idle.models.Building

@Entity(tableName = "buildings")
data class BuildingEntity(
    @PrimaryKey
    val type: Int,
    val name: String,
    val level: Int = 0,
    val maxLevel: Int = 10,
    val upgradeCost: Long = 100L,
    val workers: Int = 0,
    val maxWorkers: Int = 5,
    val efficiency: Float = 1.0f,
    val isBuilt: Boolean = false,
    val posX: Int = 0,
    val posY: Int = 0,
    val width: Int = 80,
    val height: Int = 80,
    val incomeBonus: Int = 0,
    val storageBonus: Int = 0
) {
    fun toDomainModel(): Building {
        val b = Building(type, name, maxLevel, upgradeCost, maxWorkers)
        b.level = level
        b.upgradeCost = upgradeCost
        b.workers = workers
        b.efficiency = efficiency
        b.isBuilt = isBuilt
        b.posX = posX
        b.posY = posY
        b.width = width
        b.height = height
        b.incomeBonus = incomeBonus
        b.storageBonus = storageBonus
        return b
    }

    companion object {
        fun createInitialBuildings(): List<BuildingEntity> {
            return listOf(
                BuildingEntity(
                    type = 0, // Main Hall
                    name = "Main Sect Hall",
                    level = 1,
                    maxLevel = 10,
                    upgradeCost = 150L,
                    workers = 0,
                    maxWorkers = 10,
                    efficiency = 1.0f,
                    isBuilt = true,
                    posX = 160,
                    posY = 280,
                    width = 120,
                    height = 90,
                    incomeBonus = 10,
                    storageBonus = 2000
                ),
                BuildingEntity(
                    type = 1, // Library / Scripture Pavilion
                    name = "Scripture Pavilion",
                    level = 1,
                    maxLevel = 10,
                    upgradeCost = 200L,
                    workers = 0,
                    maxWorkers = 5,
                    efficiency = 1.0f,
                    isBuilt = true,
                    posX = 60,
                    posY = 220,
                    width = 80,
                    height = 80,
                    incomeBonus = 5,
                    storageBonus = 500
                ),
                BuildingEntity(
                    type = 2, // Alchemy Lab
                    name = "Alchemy Chamber",
                    level = 1,
                    maxLevel = 10,
                    upgradeCost = 250L,
                    workers = 0,
                    maxWorkers = 5,
                    efficiency = 1.0f,
                    isBuilt = true,
                    posX = 260,
                    posY = 220,
                    width = 80,
                    height = 80,
                    incomeBonus = 15,
                    storageBonus = 500
                ),
                BuildingEntity(
                    type = 3, // Spirit Garden / Farm
                    name = "Spirit Herb Garden",
                    level = 1,
                    maxLevel = 10,
                    upgradeCost = 120L,
                    workers = 0,
                    maxWorkers = 8,
                    efficiency = 1.0f,
                    isBuilt = true,
                    posX = 100,
                    posY = 360,
                    width = 90,
                    height = 70,
                    incomeBonus = 20,
                    storageBonus = 1000
                ),
                BuildingEntity(
                    type = 4, // Spirit Pool
                    name = "Spirit Gathering Pool",
                    level = 1,
                    maxLevel = 10,
                    upgradeCost = 300L,
                    workers = 0,
                    maxWorkers = 5,
                    efficiency = 1.2f,
                    isBuilt = true,
                    posX = 220,
                    posY = 360,
                    width = 90,
                    height = 70,
                    incomeBonus = 25,
                    storageBonus = 1500
                )
            )
        }

        fun fromDomainModel(b: Building): BuildingEntity {
            return BuildingEntity(
                type = b.type,
                name = b.name,
                level = b.level,
                maxLevel = b.maxLevel,
                upgradeCost = b.upgradeCost,
                workers = b.workers,
                maxWorkers = b.maxWorkers,
                efficiency = b.efficiency,
                isBuilt = b.isBuilt,
                posX = b.posX,
                posY = b.posY,
                width = b.width,
                height = b.height,
                incomeBonus = b.incomeBonus,
                storageBonus = b.storageBonus
            )
        }
    }
}
