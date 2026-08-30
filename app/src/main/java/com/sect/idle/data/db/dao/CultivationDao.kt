package com.sect.idle.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sect.idle.data.db.entities.CultivationRecordEntity
import com.sect.idle.data.db.entities.SectProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CultivationDao {
    @Query("SELECT * FROM cultivation_records WHERE category = :category")
    fun getRecordsByCategoryFlow(category: String): Flow<List<CultivationRecordEntity>>

    @Query("SELECT * FROM cultivation_records WHERE recordKey = :key LIMIT 1")
    suspend fun getRecordByKey(key: String): CultivationRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveRecord(record: CultivationRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAllRecords(records: List<CultivationRecordEntity>)

    @Query("DELETE FROM cultivation_records WHERE recordKey = :key")
    suspend fun deleteRecord(key: String)
}

@Dao
interface SectProfileDao {
    @Query("SELECT * FROM sect_profile WHERE id = 1 LIMIT 1")
    fun getProfileFlow(): Flow<SectProfileEntity?>

    @Query("SELECT * FROM sect_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfile(): SectProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: SectProfileEntity)
}
