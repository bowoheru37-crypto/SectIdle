package com.sect.idle.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sect.idle.data.db.dao.BuildingDao
import com.sect.idle.data.db.dao.CultivationDao
import com.sect.idle.data.db.dao.DiscipleDao
import com.sect.idle.data.db.dao.SectProfileDao
import com.sect.idle.data.db.dao.TaskDao
import com.sect.idle.data.db.entities.BuildingEntity
import com.sect.idle.data.db.entities.CultivationRecordEntity
import com.sect.idle.data.db.entities.DiscipleEntity
import com.sect.idle.data.db.entities.SectProfileEntity
import com.sect.idle.data.db.entities.TaskEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * AppDatabase definition extending RoomDatabase.
 * Defines the database configuration including Disciple, Building, and Task entities.
 */
@Database(
    entities = [
        DiscipleEntity::class,
        BuildingEntity::class,
        TaskEntity::class,
        CultivationRecordEntity::class,
        SectProfileEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun discipleDao(): DiscipleDao
    abstract fun buildingDao(): BuildingDao
    abstract fun taskDao(): TaskDao
    abstract fun cultivationDao(): CultivationDao
    abstract fun sectProfileDao(): SectProfileDao

    /**
     * Seeds initial game state for disciples, buildings, and tasks if database is empty.
     */
    suspend fun seedInitialGameStateIfEmpty() {
        if (sectProfileDao().getProfile() == null) {
            sectProfileDao().saveProfile(SectProfileEntity())
        }
        if (discipleDao().getAllDisciples().isEmpty()) {
            discipleDao().insertAll(DiscipleEntity.createInitialDisciples())
        }
        if (buildingDao().getAllBuildings().isEmpty()) {
            buildingDao().insertAll(BuildingEntity.createInitialBuildings())
        }
        if (taskDao().getAllTasks().isEmpty()) {
            taskDao().insertAll(TaskEntity.createInitialTasks())
        }
    }

    companion object {
        private const val DATABASE_NAME = "sect_immortal_realm.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.seedInitialGameStateIfEmpty()
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
