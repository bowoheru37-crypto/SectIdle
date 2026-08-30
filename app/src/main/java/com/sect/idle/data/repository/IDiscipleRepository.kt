package com.sect.idle.data.repository

import com.sect.idle.data.db.dao.DiscipleDao
import com.sect.idle.data.db.entities.DiscipleEntity
import com.sect.idle.models.Disciple
import com.sect.idle.systems.RNG
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface IDiscipleRepository {
    fun getDisciplesFlow(): Flow<List<Disciple>>
    suspend fun getAllDisciples(): List<Disciple>
    suspend fun getDiscipleById(id: String): Disciple?
    suspend fun saveDisciple(disciple: Disciple)
    suspend fun saveAllDisciples(disciples: List<Disciple>)
    suspend fun deleteDisciple(id: String)
    suspend fun generateRecruitmentCandidates(count: Int, sectLevel: Int): List<Disciple>
    suspend fun assignTask(discipleId: String, taskId: Int): Boolean
    suspend fun attemptBreakthrough(discipleId: String): Pair<Boolean, String>
    suspend fun trainDisciple(discipleId: String, trainingType: String): Pair<Boolean, String>
    suspend fun gainCombatExperience(discipleId: String, expGain: Long, won: Boolean): Pair<Boolean, String>
    suspend fun craftAlchemyPill(discipleId: String, pillName: String): Pair<Boolean, String>
}

class DiscipleRepository(
    private val discipleDao: DiscipleDao
) : IDiscipleRepository {

    override fun getDisciplesFlow(): Flow<List<Disciple>> {
        return discipleDao.getAllDisciplesFlow().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getAllDisciples(): List<Disciple> {
        return discipleDao.getAllDisciples().map { it.toDomainModel() }
    }

    override suspend fun getDiscipleById(id: String): Disciple? {
        return discipleDao.getDiscipleById(id)?.toDomainModel()
    }

    override suspend fun saveDisciple(disciple: Disciple) {
        discipleDao.insertOrUpdate(DiscipleEntity.fromDomainModel(disciple))
    }

    override suspend fun saveAllDisciples(disciples: List<Disciple>) {
        discipleDao.insertAll(disciples.map { DiscipleEntity.fromDomainModel(it) })
    }

    override suspend fun deleteDisciple(id: String) {
        discipleDao.deleteById(id)
    }

    override suspend fun generateRecruitmentCandidates(count: Int, sectLevel: Int): List<Disciple> {
        val list = mutableListOf<Disciple>()
        val firstNames = arrayOf("Ling", "Xiao", "Chen", "Ye", "Han", "Yun", "Feng", "Lin", "Bai", "Gu", "Mu", "Su")
        val lastNames = arrayOf("Feng", "Tian", "Yue", "Xue", "Yan", "Hai", "Cloud", "Sword", "Frost", "Thunder", "Shadow", "Lotus")

        for (i in 0 until count) {
            val isMale = RNG.roll(0.5f)
            val name = "${firstNames[RNG.nextInt(firstNames.size)]} ${lastNames[RNG.nextInt(lastNames.size)]}"
            val element = RNG.nextInt(5)
            val d = Disciple().apply {
                this.id = java.util.UUID.randomUUID().toString()
                this.name = name
                this.isMale = isMale
                this.realm = 0
                this.element = element
            }

            // Dynamic Xianxia Talent grades: 1: Divine, 2: Earth, 3: Mystic, 4: Mortal
            val roll = RNG.nextFloat()
            val talent = when {
                roll < 0.05f + (sectLevel * 0.02f) -> 1 // Divine Spiritual Root
                roll < 0.25f + (sectLevel * 0.03f) -> 2 // Earth Spiritual Root
                roll < 0.65f -> 3 // Mystic Spiritual Root
                else -> 4 // Mortal Root
            }
            d.talentGrade = talent
            d.talentName = when (talent) {
                1 -> "Heavenly Divine Root"
                2 -> "Earth True Spiritual Root"
                3 -> "Mystic Pure Root"
                else -> "Mortal Cleansed Root"
            }
            d.atk = 15 + (5 - talent) * 8 + RNG.nextInt(10)
            d.def = 8 + (5 - talent) * 4 + RNG.nextInt(6)
            d.maxHp = 80 + (5 - talent) * 25 + RNG.nextInt(20)
            d.hp = d.maxHp
            d.loyalty = 70 + RNG.nextInt(30)
            list.add(d)
        }
        return list
    }

    override suspend fun assignTask(discipleId: String, taskId: Int): Boolean {
        val disciple = discipleDao.getDiscipleById(discipleId) ?: return false
        val updated = disciple.copy(
            currentTask = taskId,
            updatedTimestamp = System.currentTimeMillis()
        )
        discipleDao.update(updated)
        return true
    }

    override suspend fun attemptBreakthrough(discipleId: String): Pair<Boolean, String> {
        val entity = discipleDao.getDiscipleById(discipleId) ?: return Pair(false, "Disciple not found")
        val d = entity.toDomainModel()

        val reqExp = (d.realm + 1) * 300
        if (d.realmExp < reqExp) {
            return Pair(false, "${d.name} needs ${reqExp - d.realmExp} more Qi exp to trigger tribulation.")
        }

        val baseSuccess = (0.75f - d.realm * 0.04f).coerceIn(0.25f, 0.95f)
        val success = RNG.nextFloat() < baseSuccess

        if (success) {
            d.realm++
            d.realmExp = 0
            d.maxHp += 50 + d.realm * 20
            d.hp = d.maxHp
            d.atk += 15 + d.realm * 5
            d.def += 8 + d.realm * 3
            saveDisciple(d)
            return Pair(true, "⚡ Heavenly Thunder Conquered! ${d.name} broke through to Realm ${d.realm}!")
        } else {
            // Minor Qi Deviation penalty
            d.realmExp = (d.realmExp * 0.6f).toInt()
            d.hp = (d.maxHp * 0.4f).toInt()
            saveDisciple(d)
            return Pair(false, "⚡ Tribulation Failed! ${d.name} suffered Qi deviation, exp lost.")
        }
    }

    override suspend fun trainDisciple(discipleId: String, trainingType: String): Pair<Boolean, String> {
        val entity = discipleDao.getDiscipleById(discipleId) ?: return Pair(false, "Disciple not found")
        val d = entity.toDomainModel()
        val expGain = when (trainingType) {
            "Sword Mastery" -> {
                d.str += 2
                d.atk += 4
                45L
            }
            "Body Refining" -> {
                d.vit += 2
                d.bodyRefiningStage++
                d.maxHp += 20
                d.hp = d.maxHp
                50L
            }
            "Dao Meditation" -> {
                d.intel += 2
                d.wis += 2
                d.realmExp += 30
                40L
            }
            "Spirit Herb Alchemy" -> {
                d.alchemySkill++
                d.wis += 1
                35L
            }
            else -> 30L
        }

        val leveledUp = d.addExperience(expGain)
        saveDisciple(d)

        val msg = if (leveledUp) {
            "🌟 Level Up! ${d.name} mastered $trainingType and reached Level ${d.level}! Stats increased!"
        } else {
            "🥋 ${d.name} completed $trainingType (+${expGain} EXP: ${d.exp}/${d.maxExp})"
        }
        return Pair(true, msg)
    }

    override suspend fun gainCombatExperience(discipleId: String, expGain: Long, won: Boolean): Pair<Boolean, String> {
        val entity = discipleDao.getDiscipleById(discipleId) ?: return Pair(false, "Disciple not found")
        val d = entity.toDomainModel()
        d.totalBattles++
        if (won) d.battlesWon++
        val leveledUp = d.addExperience(expGain)
        saveDisciple(d)

        val msg = if (leveledUp) {
            "⚡ Battle Mastery! ${d.name} gained ${expGain} Combat EXP and leveled up to Lv.${d.level}!"
        } else {
            "⚔️ ${d.name} gained ${expGain} Combat EXP (${d.exp}/${d.maxExp})"
        }
        return Pair(true, msg)
    }

    override suspend fun craftAlchemyPill(discipleId: String, pillName: String): Pair<Boolean, String> {
        val entity = discipleDao.getDiscipleById(discipleId) ?: return Pair(false, "Disciple not found")
        val d = entity.toDomainModel()
        d.alchemySkill++
        val leveledUp = d.addExperience(30L)
        saveDisciple(d)
        val msg = "🧪 ${d.name} crafted [$pillName]! Alchemy Skill increased to ${d.alchemySkill}."
        return Pair(true, msg)
    }
}
