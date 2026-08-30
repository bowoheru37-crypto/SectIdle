package com.sect.idle.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sect.idle.data.db.entities.DiscipleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DiscipleDao {
    @Query("SELECT * FROM disciples ORDER BY realm DESC, realmExp DESC")
    fun getAllDisciplesFlow(): Flow<List<DiscipleEntity>>

    @Query("SELECT * FROM disciples")
    suspend fun getAllDisciples(): List<DiscipleEntity>

    @Query("SELECT * FROM disciples WHERE id = :id LIMIT 1")
    suspend fun getDiscipleById(id: String): DiscipleEntity?

    @Query("SELECT * FROM disciples WHERE currentTask = :taskId")
    suspend fun getDisciplesByTask(taskId: Int): List<DiscipleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(disciple: DiscipleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(disciples: List<DiscipleEntity>)

    @Update
    suspend fun update(disciple: DiscipleEntity)

    @Delete
    suspend fun delete(disciple: DiscipleEntity)

    @Query("UPDATE disciples SET level = :newLevel, exp = :newExp, maxExp = :newMaxExp, hp = :newHp, maxHp = :newMaxHp, atk = :newAtk, def = :newDef, spd = :newSpd, updatedTimestamp = :timestamp WHERE id = :id")
    suspend fun updateExperienceAndLevel(
        id: String,
        newLevel: Int,
        newExp: Long,
        newMaxExp: Long,
        newHp: Int,
        newMaxHp: Int,
        newAtk: Int,
        newDef: Int,
        newSpd: Int,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("UPDATE disciples SET totalBattles = totalBattles + 1, battlesWon = battlesWon + :wonIncrement, exp = exp + :expGain, updatedTimestamp = :timestamp WHERE id = :id")
    suspend fun recordBattleResult(
        id: String,
        wonIncrement: Int,
        expGain: Long,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("UPDATE disciples SET alchemySkill = alchemySkill + :skillGain, updatedTimestamp = :timestamp WHERE id = :id")
    suspend fun increaseAlchemySkill(
        id: String,
        skillGain: Int = 1,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM disciples WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM disciples")
    suspend fun deleteAll()
}
