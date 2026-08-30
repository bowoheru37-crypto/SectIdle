package com.sect.idle.data.repository

import com.sect.idle.data.db.dao.TaskDao
import com.sect.idle.data.db.entities.TaskEntity
import kotlinx.coroutines.flow.Flow

interface ITaskRepository {
    fun getAllTasksFlow(): Flow<List<TaskEntity>>
    suspend fun getAllTasks(): List<TaskEntity>
    suspend fun getTaskById(taskId: Int): TaskEntity?
    suspend fun getUnlockedTasks(): List<TaskEntity>
    suspend fun saveTask(task: TaskEntity)
    suspend fun saveAllTasks(tasks: List<TaskEntity>)
    suspend fun updateWorkerCount(taskId: Int, count: Int)
    suspend fun unlockTask(taskId: Int)
    suspend fun resetInitialTasks()
}

class TaskRepository(
    private val taskDao: TaskDao
) : ITaskRepository {

    override fun getAllTasksFlow(): Flow<List<TaskEntity>> {
        return taskDao.getAllTasksFlow()
    }

    override suspend fun getAllTasks(): List<TaskEntity> {
        val list = taskDao.getAllTasks()
        if (list.isEmpty()) {
            val initial = TaskEntity.createInitialTasks()
            taskDao.insertAll(initial)
            return initial
        }
        return list
    }

    override suspend fun getTaskById(taskId: Int): TaskEntity? {
        return taskDao.getTaskById(taskId)
    }

    override suspend fun getUnlockedTasks(): List<TaskEntity> {
        return taskDao.getUnlockedTasks()
    }

    override suspend fun saveTask(task: TaskEntity) {
        taskDao.insertOrUpdate(task)
    }

    override suspend fun saveAllTasks(tasks: List<TaskEntity>) {
        taskDao.insertAll(tasks)
    }

    override suspend fun updateWorkerCount(taskId: Int, count: Int) {
        taskDao.updateWorkerCount(taskId, count)
    }

    override suspend fun unlockTask(taskId: Int) {
        taskDao.setTaskUnlocked(taskId, true)
    }

    override suspend fun resetInitialTasks() {
        taskDao.deleteAll()
        taskDao.insertAll(TaskEntity.createInitialTasks())
    }
}
