package com.sect.idle.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sect.idle.models.Disciple

@Entity(tableName = "disciples")
data class DiscipleEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val title: String = "Disciple",
    val isMale: Boolean = true,
    val age: Int = 18,
    val lifespan: Int = 100,
    val realm: Int = 0,
    val realmExp: Int = 0,
    val element: Int = 0,
    val talentGrade: Int = 3,
    val talentName: String = "Normal",
    val str: Int = 10,
    val agi: Int = 10,
    val intel: Int = 10,
    val vit: Int = 10,
    val wis: Int = 10,
    val hp: Int = 100,
    val maxHp: Int = 100,
    val atk: Int = 20,
    val def: Int = 10,
    val spd: Int = 10,
    val currentTask: Int = 0,
    val loyalty: Int = 80,
    val mood: Int = 90,
    val level: Int = 1,
    val exp: Long = 0L,
    val maxExp: Long = 100L,
    val alchemySkill: Int = 1,
    val bodyRefiningStage: Int = 1,
    val totalBattles: Int = 0,
    val battlesWon: Int = 0,
    val updatedTimestamp: Long = System.currentTimeMillis()
) {
    fun toDomainModel(): Disciple {
        val d = Disciple()
        d.id = id
        d.name = name
        d.title = title
        d.isMale = isMale
        d.age = age
        d.lifespan = lifespan
        d.realm = realm
        d.realmExp = realmExp
        d.element = element
        d.talentGrade = talentGrade
        d.talentName = talentName
        d.str = str
        d.agi = agi
        d.intel = intel
        d.vit = vit
        d.wis = wis
        d.hp = hp
        d.maxHp = maxHp
        d.atk = atk
        d.def = def
        d.spd = spd
        d.currentTask = currentTask
        d.loyalty = loyalty
        d.mood = mood
        d.level = level
        d.exp = exp
        d.maxExp = maxExp
        d.alchemySkill = alchemySkill
        d.bodyRefiningStage = bodyRefiningStage
        d.totalBattles = totalBattles
        d.battlesWon = battlesWon
        return d
    }

    companion object {
        fun createInitialDisciples(): List<DiscipleEntity> {
            return listOf(
                DiscipleEntity(
                    id = "disciple_first_disciple",
                    name = "Lin Chen",
                    title = "Head Disciple",
                    isMale = true,
                    age = 19,
                    lifespan = 120,
                    realm = 1, // Qi Refining
                    realmExp = 80,
                    element = 0, // Metal
                    talentGrade = 2, // Earth Spiritual Root
                    talentName = "Nine-Refined Sword Meridian",
                    str = 24,
                    agi = 20,
                    intel = 18,
                    vit = 22,
                    wis = 19,
                    hp = 220,
                    maxHp = 220,
                    atk = 45,
                    def = 28,
                    spd = 18,
                    currentTask = 4, // Cultivation
                    loyalty = 95,
                    mood = 90,
                    level = 3,
                    exp = 40L,
                    maxExp = 180L,
                    alchemySkill = 2,
                    bodyRefiningStage = 2,
                    totalBattles = 5,
                    battlesWon = 5
                ),
                DiscipleEntity(
                    id = "disciple_second_disciple",
                    name = "Su Qingxue",
                    title = "Senior Sister",
                    isMale = false,
                    age = 18,
                    lifespan = 115,
                    realm = 0, // Mortal / Qi Condensation
                    realmExp = 50,
                    element = 2, // Water
                    talentGrade = 2, // Earth Spiritual Root
                    talentName = "Jade Lotus Heart",
                    str = 16,
                    agi = 22,
                    intel = 25,
                    vit = 18,
                    wis = 26,
                    hp = 180,
                    maxHp = 180,
                    atk = 32,
                    def = 20,
                    spd = 22,
                    currentTask = 1, // Spirit Herb Farming
                    loyalty = 90,
                    mood = 95,
                    level = 2,
                    exp = 20L,
                    maxExp = 120L,
                    alchemySkill = 3,
                    bodyRefiningStage = 1,
                    totalBattles = 2,
                    battlesWon = 2
                )
            )
        }

        fun fromDomainModel(d: Disciple): DiscipleEntity {
            return DiscipleEntity(
                id = d.id ?: java.util.UUID.randomUUID().toString(),
                name = d.name ?: "Unknown Disciple",
                title = d.title ?: "Disciple",
                isMale = d.isMale,
                age = d.age,
                lifespan = d.lifespan,
                realm = d.realm,
                realmExp = d.realmExp,
                element = d.element,
                talentGrade = d.talentGrade,
                talentName = d.talentName ?: "Mortal Root",
                str = d.str,
                agi = d.agi,
                intel = d.intel,
                vit = d.vit,
                wis = d.wis,
                hp = d.hp,
                maxHp = d.maxHp,
                atk = d.atk,
                def = d.def,
                spd = d.spd,
                currentTask = d.currentTask,
                loyalty = d.loyalty,
                mood = d.mood,
                level = d.level,
                exp = d.exp,
                maxExp = d.maxExp,
                alchemySkill = d.alchemySkill,
                bodyRefiningStage = d.bodyRefiningStage,
                totalBattles = d.totalBattles,
                battlesWon = d.battlesWon,
                updatedTimestamp = System.currentTimeMillis()
            )
        }
    }
}
