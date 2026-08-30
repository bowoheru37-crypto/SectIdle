package com.sect.idle.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sect.idle.data.db.entities.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM sect_tasks ORDER BY taskId ASC")
    fun getAllTasksFlow(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM sect_tasks ORDER BY taskId ASC")
    suspend fun getAllTasks(): List<TaskEntity>

    @Query("SELECT * FROM sect_tasks WHERE taskId = :taskId LIMIT 1")
    suspend fun getTaskById(taskId: Int): TaskEntity?

    @Query("SELECT * FROM sect_tasks WHERE isUnlocked = 1 ORDER BY taskId ASC")
    suspend fun getUnlockedTasks(): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<TaskEntity>)

    @Update
    suspend fun update(task: TaskEntity)

    @Query("UPDATE sect_tasks SET assignedDisciplesCount = :count, updatedTimestamp = :timestamp WHERE taskId = :taskId")
    suspend fun updateWorkerCount(taskId: Int, count: Int, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE sect_tasks SET isUnlocked = :unlocked WHERE taskId = :taskId")
    suspend fun setTaskUnlocked(taskId: Int, unlocked: Boolean)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("DELETE FROM sect_tasks")
    suspend fun deleteAll()
}
