package com.sect.idle.data.repository

import com.sect.idle.data.db.dao.CultivationDao
import com.sect.idle.data.db.entities.CultivationRecordEntity
import com.sect.idle.models.Item
import kotlinx.coroutines.flow.Flow

data class PillCraftResult(
    val success: Boolean,
    val pillName: String,
    val expGranted: Int,
    val message: String
)

interface ICultivationRepository {
    fun getScripturesFlow(): Flow<List<CultivationRecordEntity>>
    suspend fun unlockScripture(scriptureId: String, name: String, costQi: Long, costStones: Long): Boolean
    suspend fun isScriptureUnlocked(scriptureId: String): Boolean
    suspend fun craftPill(pillType: Int, currentQi: Long, currentStones: Long): PillCraftResult
    suspend fun calculateQiYield(gatheringArrayLevel: Int, activeMeditatingDisciples: Int): Long
}

class CultivationRepository(
    private val cultivationDao: CultivationDao
) : ICultivationRepository {

    override fun getScripturesFlow(): Flow<List<CultivationRecordEntity>> {
        return cultivationDao.getRecordsByCategoryFlow("SCRIPTURE")
    }

    override suspend fun unlockScripture(scriptureId: String, name: String, costQi: Long, costStones: Long): Boolean {
        val record = CultivationRecordEntity(
            recordKey = "scripture_$scriptureId",
            category = "SCRIPTURE",
            valueString = name,
            valueLong = costQi,
            isCompleted = true,
            timestamp = System.currentTimeMillis()
        )
        cultivationDao.saveRecord(record)
        return true
    }

    override suspend fun isScriptureUnlocked(scriptureId: String): Boolean {
        val record = cultivationDao.getRecordByKey("scripture_$scriptureId")
        return record?.isCompleted == true
    }

    override suspend fun craftPill(pillType: Int, currentQi: Long, currentStones: Long): PillCraftResult {
        return when (pillType) {
            0 -> {
                if (currentQi < 150L || currentStones < 50L) {
                    PillCraftResult(false, "Qi Condensation Pill", 0, "Insufficient Qi or Stones for Qi Condensation Pill")
                } else {
                    PillCraftResult(true, "Qi Condensation Pill", 500, "Refined Qi Condensation Pill (+500 Exp)")
                }
            }
            1 -> {
                if (currentQi < 350L || currentStones < 120L) {
                    PillCraftResult(false, "Foundation Breakthrough Pill", 0, "Insufficient Qi or Stones for Foundation Pill")
                } else {
                    PillCraftResult(true, "Foundation Breakthrough Pill", 1500, "Refined Foundation Breakthrough Pill (+1,500 Exp)")
                }
            }
            2 -> {
                if (currentQi < 800L || currentStones < 300L) {
                    PillCraftResult(false, "Nine Revolutions Golden Core Elixir", 0, "Insufficient Qi or Stones for Core Elixir")
                } else {
                    PillCraftResult(true, "Nine Revolutions Golden Core Elixir", 4000, "Refined Nine Revolutions Golden Core Elixir (+4,000 Exp)")
                }
            }
            3 -> {
                if (currentQi < 1200L || currentStones < 500L) {
                    PillCraftResult(false, "Tribulation Shield Pill", 0, "Insufficient Qi or Stones for Tribulation Shield")
                } else {
                    PillCraftResult(true, "Tribulation Shield Pill", 8000, "Refined Tribulation Shield Pill (+8,000 Exp)")
                }
            }
            else -> PillCraftResult(false, "Unknown Pill", 0, "Unknown recipe")
        }
    }

    override suspend fun calculateQiYield(gatheringArrayLevel: Int, activeMeditatingDisciples: Int): Long {
        val baseArrayQi = (gatheringArrayLevel * 5L).coerceAtLeast(5L)
        val discipleQi = activeMeditatingDisciples * 3L
        return baseArrayQi + discipleQi
    }
}
