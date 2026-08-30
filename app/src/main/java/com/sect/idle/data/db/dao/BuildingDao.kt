package com.sect.idle.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sect.idle.data.db.entities.BuildingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BuildingDao {
    @Query("SELECT * FROM buildings ORDER BY type ASC")
    fun getAllBuildingsFlow(): Flow<List<BuildingEntity>>

    @Query("SELECT * FROM buildings ORDER BY type ASC")
    suspend fun getAllBuildings(): List<BuildingEntity>

    @Query("SELECT * FROM buildings WHERE type = :type LIMIT 1")
    suspend fun getBuildingByType(type: Int): BuildingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(building: BuildingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(buildings: List<BuildingEntity>)

    @Update
    suspend fun update(building: BuildingEntity)

    @Query("DELETE FROM buildings")
    suspend fun deleteAll()
}
