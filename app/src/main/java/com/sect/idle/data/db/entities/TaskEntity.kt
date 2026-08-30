package com.sect.idle.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sect.idle.core.GameConfig

@Entity(tableName = "sect_tasks")
data class TaskEntity(
    @PrimaryKey
    val taskId: Int,
    val name: String,
    val category: String = "SECT_DUTY",
    val description: String = "",
    val baseYieldQi: Long = 0L,
    val baseYieldStones: Long = 0L,
    val assignedDisciplesCount: Int = 0,
    val efficiencyMultiplier: Float = 1.0f,
    val requiredRealm: Int = 0,
    val isUnlocked: Boolean = true,
    val iconResName: String = "ic_task_default",
    val updatedTimestamp: Long = System.currentTimeMillis()
) {
    companion object {
        fun createInitialTasks(): List<TaskEntity> {
            return listOf(
                TaskEntity(
                    taskId = GameConfig.TASK_NONE,
                    name = "Rest & Meditate",
                    category = "REST",
                    description = "Recover vital energy and stabilize cultivation dao heart.",
                    baseYieldQi = 2L,
                    baseYieldStones = 0L,
                    assignedDisciplesCount = 0,
                    efficiencyMultiplier = 1.0f,
                    requiredRealm = 0,
                    isUnlocked = true,
                    iconResName = "ic_meditate"
                ),
                TaskEntity(
                    taskId = GameConfig.TASK_FARMING,
                    name = "Spirit Herb Farming",
                    category = "RESOURCE",
                    description = "Tend the sect's spiritual herb garden to harvest rare alchemy ingredients.",
                    baseYieldQi = 15L,
                    baseYieldStones = 25L,
                    assignedDisciplesCount = 0,
                    efficiencyMultiplier = 1.0f,
                    requiredRealm = 0,
                    isUnlocked = true,
                    iconResName = "ic_herb_farm"
                ),
                TaskEntity(
                    taskId = GameConfig.TASK_CRAFTING,
                    name = "Talisman Crafting",
                    category = "CRAFTING",
                    description = "Inscribe defensive and offensive spiritual talismans for sect reserves.",
                    baseYieldQi = 10L,
                    baseYieldStones = 40L,
                    assignedDisciplesCount = 0,
                    efficiencyMultiplier = 1.0f,
                    requiredRealm = 0,
                    isUnlocked = true,
                    iconResName = "ic_talisman"
                ),
                TaskEntity(
                    taskId = GameConfig.TASK_ALCHEMY,
                    name = "Pill Refining",
                    category = "ALCHEMY",
                    description = "Refine Qi-Condensing and Foundation Pills in the sect alchemy furnaces.",
                    baseYieldQi = 35L,
                    baseYieldStones = 60L,
                    assignedDisciplesCount = 0,
                    efficiencyMultiplier = 1.0f,
                    requiredRealm = 1,
                    isUnlocked = true,
                    iconResName = "ic_pill"
                ),
                TaskEntity(
                    taskId = GameConfig.TASK_CULTIVATION,
                    name = "Deep Qi Cultivation",
                    category = "CULTIVATION",
                    description = "Absorb natural spiritual mountain veins to accelerate personal realm breakthroughs.",
                    baseYieldQi = 50L,
                    baseYieldStones = 0L,
                    assignedDisciplesCount = 0,
                    efficiencyMultiplier = 1.2f,
                    requiredRealm = 0,
                    isUnlocked = true,
                    iconResName = "ic_cultivation"
                ),
                TaskEntity(
                    taskId = GameConfig.TASK_MINING,
                    name = "Spirit Stone Mining",
                    category = "RESOURCE",
                    description = "Excavate raw spirit veins deep beneath the sect's ancestral peaks.",
                    baseYieldQi = 8L,
                    baseYieldStones = 30L,
                    assignedDisciplesCount = 0,
                    efficiencyMultiplier = 1.0f,
                    requiredRealm = 0,
                    isUnlocked = true,
                    iconResName = "ic_mine"
                ),
                TaskEntity(
                    taskId = GameConfig.TASK_TRAINING,
                    name = "Martial Dao Sparring",
                    category = "MARTIAL",
                    description = "Hone celestial sword arts and combat techniques with fellow martial brethren.",
                    baseYieldQi = 12L,
                    baseYieldStones = 10L,
                    assignedDisciplesCount = 0,
                    efficiencyMultiplier = 1.0f,
                    requiredRealm = 0,
                    isUnlocked = true,
                    iconResName = "ic_sword_spar"
                ),
                TaskEntity(
                    taskId = GameConfig.TASK_GUARD,
                    name = "Mountain Gate Patrol",
                    category = "DEFENSE",
                    description = "Guard the mountain boundaries against demonic beasts and rival rogue cultivators.",
                    baseYieldQi = 5L,
                    baseYieldStones = 15L,
                    assignedDisciplesCount = 0,
                    efficiencyMultiplier = 1.0f,
                    requiredRealm = 0,
                    isUnlocked = true,
                    iconResName = "ic_gate_guard"
                ),
                TaskEntity(
                    taskId = GameConfig.TASK_RESEARCH,
                    name = "Scripture Comprehension",
                    category = "RESEARCH",
                    description = "Decipher ancient ancestral scrolls in the Grand Library Pavilion.",
                    baseYieldQi = 25L,
                    baseYieldStones = 20L,
                    assignedDisciplesCount = 0,
                    efficiencyMultiplier = 1.1f,
                    requiredRealm = 1,
                    isUnlocked = true,
                    iconResName = "ic_scroll"
                ),
                TaskEntity(
                    taskId = GameConfig.TASK_TRADING,
                    name = "Mortal Realm Commerce",
                    category = "COMMERCE",
                    description = "Trade refined elixirs and spiritual trinkets across mortal merchant markets.",
                    baseYieldQi = 0L,
                    baseYieldStones = 80L,
                    assignedDisciplesCount = 0,
                    efficiencyMultiplier = 1.0f,
                    requiredRealm = 0,
                    isUnlocked = true,
                    iconResName = "ic_market_trade"
                ),
                TaskEntity(
                    taskId = GameConfig.TASK_EXPLORING,
                    name = "Secret Realm Exploration",
                    category = "EXPEDITION",
                    description = "Venture into uncharted primordial valleys to unearth ancient treasures.",
                    baseYieldQi = 40L,
                    baseYieldStones = 75L,
                    assignedDisciplesCount = 0,
                    efficiencyMultiplier = 1.3f,
                    requiredRealm = 2,
                    isUnlocked = true,
                    iconResName = "ic_exploration"
                )
            )
        }
    }
}
