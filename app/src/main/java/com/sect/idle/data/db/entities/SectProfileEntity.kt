package com.sect.idle.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cultivation_records")
data class CultivationRecordEntity(
    @PrimaryKey
    val recordKey: String, // e.g. "active_task_alchemy", "scripture_unlocked_1"
    val category: String, // "BREATH", "CHAMBER", "SCRIPTURE", "PILL"
    val valueInt: Int = 0,
    val valueLong: Long = 0L,
    val valueString: String = "",
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "sect_profile")
data class SectProfileEntity(
    @PrimaryKey
    val id: Int = 1, // Single profile singleton row
    val sectName: String = "Cloud Mist Sect",
    val sectRankName: String = "Mortal Sect",
    val sectLevel: Int = 1,
    val qi: Long = 500L,
    val maxQi: Long = 2000L,
    val spiritStones: Long = 300L,
    val karma: Long = 100L,
    val reputation: Long = 50L,
    val day: Int = 1,
    val season: String = "Spring",
    val year: Int = 1,
    val soundEnabled: Boolean = true,
    val lastSaveTimestamp: Long = System.currentTimeMillis()
)
