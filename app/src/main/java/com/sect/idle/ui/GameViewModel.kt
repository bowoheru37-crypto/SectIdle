package com.sect.idle.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.viewModelScope
import com.sect.idle.ai.CultivationAiEngine
import com.sect.idle.ai.CultivationArenaEngine
import com.sect.idle.ai.DynamicSectQuest
import com.sect.idle.ai.EventChoice
import com.sect.idle.ai.GrandmasterAiService
import com.sect.idle.ai.GrandmasterWisdom
import com.sect.idle.ai.SectAiRepository
import com.sect.idle.ai.SectRandomEvent
import com.sect.idle.core.GameConfig
import com.sect.idle.models.Building
import com.sect.idle.models.Disciple
import com.sect.idle.models.Item
import com.sect.idle.models.Quest
import com.sect.idle.systems.AudioManager
import com.sect.idle.systems.CompressionUtil
import com.sect.idle.systems.RNG
import com.sect.idle.systems.SoundManager
import com.sect.idle.ui.base.AiMessage
import com.sect.idle.ui.base.BaseSectViewModel
import com.sect.idle.ui.base.BattleState
import com.sect.idle.ui.base.QiOrb
import com.sect.idle.ui.base.SectUiEvent
import com.sect.idle.ui.base.SectUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

typealias QiOrb = com.sect.idle.ui.base.QiOrb
typealias BattleState = com.sect.idle.ui.base.BattleState
typealias AiMessage = com.sect.idle.ui.base.AiMessage
typealias SectUiState = com.sect.idle.ui.base.SectUiState
typealias SectUiEvent = com.sect.idle.ui.base.SectUiEvent

class GameViewModel(application: Application) : BaseSectViewModel(application) {
    private val prefs = application.getSharedPreferences("IdleSectSaveState", Context.MODE_PRIVATE)

    private var loopJob: Job? = null
    private var saveTimer = 0
    private var eventTimer = 0
    private var currentBossState: CultivationAiEngine.BossCombatState? = null

    init {
        loadGame()
        startGameloop()
    }

    private fun startGameloop() {
        loopJob?.cancel()
        loopJob = viewModelScope.launch {
            while (isActive) {
                delay(200) // 5 ticks per second
                tickSimulation(0.2f)
            }
        }
    }

    private fun tickSimulation(dt: Float) {
        _uiState.update { current ->
            val qiGatheringRate = current.buildings.find { it.type == 1 }?.level ?: 1
            val discipleQiBonus = current.disciples.filter { it.currentTask == 0 }.sumOf { it.realm * 2L + 1 }
            val qiGain = (qiGatheringRate * 3 + discipleQiBonus)

            val newQi = (current.qi + qiGain).coerceAtMost(current.maxQi)

            // Tick Autonomous Cultivation AI Engine for all disciples
            CultivationAiEngine.tickNpcAi(current.disciples, 400f, 600f, dt)

            // Update disciples cultivation & tasks
            val updatedDisciples = current.disciples.map { d ->
                if (d.currentTask == 0) { // Cultivating
                    val speed = (1.0f + d.talentGrade * 0.1f) * (1.0f + (current.buildings.find { it.type == 4 }?.level ?: 1) * 0.15f)
                    d.realmExp += (15 * speed).toInt()
                }
                d
            }

            // Spawn Qi orbs randomly on the mountain
            val newOrbs = current.qiOrbs.toMutableList()
            if (newOrbs.size < 6 && RNG.nextFloat() < 0.08f) {
                val element = RNG.nextInt(5)
                newOrbs.add(
                    QiOrb(
                        x = RNG.nextFloat(60f, 320f),
                        y = RNG.nextFloat(100f, 500f),
                        amount = 20L * current.sectLevel + RNG.nextInt(30),
                        color = GameConfig.getElementColor(element)
                    )
                )
            }

            // Calendar progression
            val newDay = if (saveTimer % 150 == 0) current.day + 1 else current.day
            val seasonNames = listOf("Spring", "Summer", "Autumn", "Winter")
            val newSeason = seasonNames[(newDay / 30) % 4]
            val newYear = 1 + newDay / 120

            current.copy(
                qi = newQi,
                disciples = updatedDisciples,
                qiOrbs = newOrbs,
                day = newDay,
                season = newSeason,
                year = newYear
            )
        }

        saveTimer++
        if (saveTimer >= 50) { // Auto-save every 10 seconds
            saveTimer = 0
            saveGame()
        }

        // Random Celestial Event occurrence check (~every 100 seconds)
        eventTimer++
        if (eventTimer >= 500) {
            eventTimer = 0
            if (_uiState.value.activeRandomEvent == null && RNG.nextFloat() < 0.6f) {
                triggerRandomHeavenlyEvent()
            }
        }
    }

    override fun collectQiOrb(orbId: String) {
        _uiState.update { current ->
            val orb = current.qiOrbs.find { it.id == orbId } ?: return@update current
            audioManager.playSfx("gather")
            val updatedOrbs = current.qiOrbs.filterNot { it.id == orbId }
            val addedQi = (current.qi + orb.amount).coerceAtMost(current.maxQi)
            val addedStones = current.spiritStones + (orb.amount / 10).coerceAtLeast(1)
            current.copy(
                qi = addedQi,
                spiritStones = addedStones,
                qiOrbs = updatedOrbs,
                notificationMessage = "+${orb.amount} Spiritual Qi, +${(orb.amount / 10).coerceAtLeast(1)} Spirit Stones!"
            )
        }
    }

    override fun tapMountainQiGather() {
        _uiState.update { current ->
            audioManager.playSfx("gather")
            val gain = 5L * current.sectLevel
            val newQi = (current.qi + gain).coerceAtMost(current.maxQi)
            current.copy(qi = newQi)
        }
    }

    override fun selectTab(tabIndex: Int) {
        _uiState.update { it.copy(activeTab = tabIndex) }
    }

    override fun clearNotification() {
        _uiState.update { it.copy(notificationMessage = null) }
    }

    override fun recruitDisciple(candidate: Disciple) {
        _uiState.update { current ->
            val maxDisciples = current.buildings.find { it.type == 0 }?.level?.times(5) ?: 10
            if (current.disciples.size >= maxDisciples) {
                soundManager.playSectSound(SoundManager.SectSound.ACTION_FAIL)
                return@update current.copy(notificationMessage = "Expand Mountain Main Hall to accommodate more disciples!")
            }
            val cost = 50L
            if (current.spiritStones < cost) {
                soundManager.playSectSound(SoundManager.SectSound.ACTION_FAIL)
                return@update current.copy(notificationMessage = "Need $cost Spirit Stones to recruit!")
            }
            soundManager.playDiscipleSound(SoundManager.DiscipleSound.RECRUIT)
            val updated = current.disciples + candidate
            val updatedCandidates = current.candidateDisciples.filterNot { it.id == candidate.id }
            current.copy(
                disciples = updated,
                candidateDisciples = updatedCandidates,
                spiritStones = current.spiritStones - cost,
                reputation = current.reputation + 10,
                notificationMessage = "Welcomed ${candidate.name} to the Sect!"
            )
        }
    }

    fun refreshCandidates() {
        soundManager.playSectSound(SoundManager.SectSound.UI_CLICK)
        val names = listOf("Lin Feng", "Su Qingxue", "Xiao Yan", "Han Li", "Bai Xiaochun", "Ye Chen", "Meng Hao", "Chu Feng")
        val candidates = (1..3).map {
            val d = Disciple()
            d.name = names[RNG.nextInt(names.size)]
            d.isMale = RNG.nextInt(2) == 0
            d.element = RNG.nextInt(5)
            d.realm = RNG.nextInt(2)
            d.talentGrade = RNG.nextInt(5) + 3
            d.initStats(
                10 + RNG.nextInt(15),
                10 + RNG.nextInt(15),
                10 + RNG.nextInt(15),
                10 + RNG.nextInt(15),
                10 + RNG.nextInt(15),
                10 + RNG.nextInt(15),
                10 + RNG.nextInt(15)
            )
            d
        }
        _uiState.update { it.copy(candidateDisciples = candidates) }
    }

    fun interactWithDisciple(discipleId: String) {
        val disciple = _uiState.value.disciples.find { it.id == discipleId } ?: return
        soundManager.playDiscipleSound(SoundManager.DiscipleSound.GREETING)
        val agent = CultivationAiEngine.getOrRegisterAgent(disciple)
        _uiState.update {
            it.copy(
                notificationMessage = "${disciple.name} (${GameConfig.getRealmName(disciple.realm)}): \"${agent.currentThought}\""
            )
        }
    }

    fun assignTask(discipleId: String, taskIndex: Int) {
        soundManager.playDiscipleSound(SoundManager.DiscipleSound.ASSIGN_TASK)
        _uiState.update { current ->
            val updated = current.disciples.map {
                if (it.id == discipleId) {
                    it.currentTask = taskIndex
                }
                it
            }
            val taskName = when (taskIndex) {
                0 -> "Cultivating Qi"
                1 -> "Gathering Herbs"
                2 -> "Refining Pills"
                3 -> "Guarding Mountain Gate"
                4 -> "Beast Hunting"
                else -> "Studying Dao"
            }
            current.copy(
                disciples = updated,
                notificationMessage = "Assigned disciple to $taskName"
            )
        }
    }

    override fun assignAllTasks(taskIndex: Int) {
        soundManager.playDiscipleSound(SoundManager.DiscipleSound.ASSIGN_TASK)
        _uiState.update { current ->
            val updated = current.disciples.map {
                it.currentTask = taskIndex
                it
            }
            val taskName = when (taskIndex) {
                0 -> "Cultivating Qi"
                1 -> "Gathering Herbs"
                2 -> "Refining Pills"
                3 -> "Guarding Mountain Gate"
                4 -> "Beast Hunting"
                else -> "Studying Dao"
            }
            current.copy(
                disciples = updated,
                notificationMessage = "Assigned all ${current.disciples.size} disciples to $taskName!"
            )
        }
    }

    override fun collectAllQi() {
        soundManager.playSectSound(SoundManager.SectSound.GATHER_QI)
        _uiState.update { current ->
            if (current.qiOrbs.isEmpty()) {
                val gain = 15L * current.sectLevel
                val newQi = (current.qi + gain).coerceAtMost(current.maxQi)
                return@update current.copy(
                    qi = newQi,
                    notificationMessage = "Harvested mountain spiritual veins (+${gain} Qi)!"
                )
            }
            val totalOrbQi = current.qiOrbs.sumOf { it.amount }
            val addedQi = (current.qi + totalOrbQi).coerceAtMost(current.maxQi)
            val addedStones = current.spiritStones + (totalOrbQi / 10).coerceAtLeast(1)
            current.copy(
                qi = addedQi,
                spiritStones = addedStones,
                qiOrbs = emptyList(),
                notificationMessage = "Gathered all Spiritual Orbs! +$totalOrbQi Qi, +${(totalOrbQi / 10).coerceAtLeast(1)} Spirit Stones!"
            )
        }
    }

    override fun attemptBreakthrough(discipleId: String) {
        _uiState.update { current ->
            val disciple = current.disciples.find { it.id == discipleId } ?: return@update current
            val reqExp = (disciple.realm + 1) * 300
            if (disciple.realmExp < reqExp) {
                soundManager.playSectSound(SoundManager.SectSound.ACTION_FAIL)
                return@update current.copy(notificationMessage = "${disciple.name} has not accumulated enough Qi for breakthrough!")
            }
            val successRate = (0.75f - disciple.realm * 0.04f).coerceIn(0.2f, 0.95f)
            val isSuccess = RNG.nextFloat() < successRate

            if (isSuccess) {
                soundManager.playDiscipleSound(SoundManager.DiscipleSound.BREAKTHROUGH_SUCCESS)
                disciple.realm += 1
                disciple.realmExp = 0
                disciple.maxHp += 50 * disciple.realm
                disciple.hp = disciple.maxHp
                disciple.atk += 15 * disciple.realm
                disciple.def += 10 * disciple.realm
                val realmName = GameConfig.getRealmName(disciple.realm)
                current.copy(
                    reputation = current.reputation + 25,
                    karma = current.karma + 10,
                    notificationMessage = "⚡ HEAVENLY TRIBULATION PASSED! ${disciple.name} reached $realmName!"
                )
            } else {
                soundManager.playDiscipleSound(SoundManager.DiscipleSound.BREAKTHROUGH_FAIL)
                disciple.realmExp = (disciple.realmExp * 0.5f).toInt()
                disciple.hp = (disciple.maxHp * 0.4f).toInt()
                current.copy(
                    notificationMessage = "Tribulation failed! ${disciple.name} suffered Qi deviation, lost half cultivation."
                )
            }
        }
    }

    override fun dismissDisciple(discipleId: String) {
        _uiState.update { current ->
            val target = current.disciples.find { it.id == discipleId }
            val updated = current.disciples.filterNot { it.id == discipleId }
            soundManager.playDiscipleSound(SoundManager.DiscipleSound.DISMISS)
            current.copy(
                disciples = updated,
                notificationMessage = "Severed ties with disciple ${target?.name ?: ""}."
            )
        }
    }

    override fun demolishBuilding(buildingType: Int) {
        _uiState.update { current ->
            val b = current.buildings.find { it.type == buildingType } ?: return@update current
            if (b.level <= 1) {
                soundManager.playSectSound(SoundManager.SectSound.ACTION_FAIL)
                return@update current.copy(notificationMessage = "Cannot dismantle foundational Level 1 facility!")
            }
            soundManager.playSectSound(SoundManager.SectSound.BUILDING_DEMOLISH)
            b.level -= 1
            b.workers = 0
            b.efficiency = (b.efficiency - 0.2f).coerceAtLeast(1.0f)
            val newMaxQi = if (buildingType == 1) (current.maxQi - 1000).coerceAtLeast(1000) else current.maxQi
            current.copy(
                maxQi = newMaxQi,
                notificationMessage = "Dismantled ${b.name} to Level ${b.level}."
            )
        }
    }

    override fun upgradeBuilding(buildingType: Int) {
        _uiState.update { current ->
            val b = current.buildings.find { it.type == buildingType } ?: return@update current
            val costQi = b.level * 200L
            val costStones = b.level * 100L
            if (current.qi < costQi || current.spiritStones < costStones) {
                soundManager.playSectSound(SoundManager.SectSound.ACTION_FAIL)
                return@update current.copy(notificationMessage = "Need $costQi Qi & $costStones Spirit Stones to upgrade ${b.name}!")
            }
            soundManager.playSectSound(SoundManager.SectSound.BUILDING_UPGRADE)
            b.upgrade()
            val newMaxQi = if (buildingType == 1) current.maxQi + 1000 else current.maxQi
            current.copy(
                qi = current.qi - costQi,
                spiritStones = current.spiritStones - costStones,
                maxQi = newMaxQi,
                reputation = current.reputation + 15,
                notificationMessage = "Upgraded ${b.name} to Level ${b.level}!"
            )
        }
    }

    fun promoteSectRank() {
        _uiState.update { current ->
            val nextLevel = current.sectLevel + 1
            val reqRep = current.sectLevel * 100L
            val reqStones = current.sectLevel * 300L
            if (current.reputation < reqRep || current.spiritStones < reqStones) {
                soundManager.playSectSound(SoundManager.SectSound.ACTION_FAIL)
                return@update current.copy(
                    notificationMessage = "Need $reqRep Fame & $reqStones Spirit Stones to advance Sect Rank!"
                )
            }
            soundManager.playDiscipleSound(SoundManager.DiscipleSound.DAO_EPIPHANY)
            val rankName = when (nextLevel) {
                2 -> "Mystic Mountain Sect"
                3 -> "Earth Domain Sect"
                4 -> "Heaven Gate Sect"
                5 -> "Immortal Ascension Sect"
                else -> "Supreme Celestial Dao Sect"
            }
            current.copy(
                sectLevel = nextLevel,
                sectRankName = rankName,
                spiritStones = current.spiritStones - reqStones,
                maxQi = current.maxQi + 2000,
                karma = current.karma + 50,
                notificationMessage = "🌟 SECT ASCENDED! Now recognized as a $rankName!"
            )
        }
    }

    override fun craftPill(pillType: Int) {
        _uiState.update { current ->
            val costQi = 150L
            val costStones = 50L
            if (current.qi < costQi || current.spiritStones < costStones) {
                soundManager.playSectSound(SoundManager.SectSound.ACTION_FAIL)
                return@update current.copy(notificationMessage = "Not enough Qi or Spirit Stones for pill alchemy!")
            }
            soundManager.playSectSound(SoundManager.SectSound.ALCHEMY_REFINE)
            val pillName = when (pillType) {
                0 -> "Qi Condensation Pill"
                1 -> "Foundation Breakthrough Pill"
                2 -> "Nine Revolutions Golden Core Elixir"
                else -> "Tribulation Shield Pill"
            }
            val newItem = Item(
                "pill_$pillType",
                pillName,
                Item.TYPE_CONSUMABLE,
                (pillType + 1).coerceAtMost(5),
                99,
                100 * (pillType + 1)
            )
            current.copy(
                qi = current.qi - costQi,
                spiritStones = current.spiritStones - costStones,
                inventory = current.inventory + newItem,
                notificationMessage = "Successfully refined $pillName!"
            )
        }
    }

    fun bestowPill(discipleId: String, itemId: String) {
        _uiState.update { current ->
            val disciple = current.disciples.find { it.id == discipleId } ?: return@update current
            val item = current.inventory.find { it.id == itemId } ?: return@update current

            soundManager.playDiscipleSound(SoundManager.DiscipleSound.BESTOW_PILL)
            disciple.realmExp += (500 * (item.getRarity() + 1))
            disciple.hp = disciple.maxHp
            val updatedInventory = current.inventory.filterNot { it.id == itemId }

            current.copy(
                inventory = updatedInventory,
                notificationMessage = "Bestowed ${item.getName()} to ${disciple.name}! Gained ${500 * (item.getRarity() + 1)} Qi Exp."
            )
        }
    }

    fun setCombatStance(stance: CultivationAiEngine.CombatStance) {
        soundManager.playCombatSound(SoundManager.CombatSound.SWORD_SPAR)
        _uiState.update { it.copy(combatStance = stance) }
    }

    fun startExpedition(location: String, difficulty: Int) {
        startArenaMatch(
            stipulation = CultivationArenaEngine.MatchStipulation.GRAND_DAO_CHAMPIONSHIP,
            difficulty = difficulty,
            customLocation = location
        )
    }

    fun startArenaMatch(
        stipulation: CultivationArenaEngine.MatchStipulation,
        difficulty: Int,
        customLocation: String? = null,
        customOpponentName: String? = null
    ) {
        val disciple = _uiState.value.disciples.firstOrNull() ?: return
        val boss = CultivationAiEngine.createBossForRealm(difficulty)
        currentBossState = boss

        val discipleProf = CultivationArenaEngine.createDiscipleProfile(disciple)
        val opponentProf = CultivationArenaEngine.createOpponentProfile(difficulty, customOpponentName ?: boss.name)

        soundManager.playArenaSound(SoundManager.ArenaSound.RING_BELL)
        soundManager.playArenaSound(SoundManager.ArenaSound.CROWD_CHEER)
        soundManager.setBgmTheme(SoundManager.BgmTheme.COMBAT_INTENSE)

        val loc = customLocation ?: "${stipulation.displayName} @ Celestial Octagon"

        val initialStructuredLogs = listOf(
            com.sect.idle.ui.components.CombatLogEntry(
                id = System.currentTimeMillis(),
                text = "🔔 DING DING DING! The Elder Referee rings the Celestial Octagon Bell!",
                category = com.sect.idle.ui.components.LogCategory.REFEREE_COUNT
            ),
            com.sect.idle.ui.components.CombatLogEntry(
                id = System.currentTimeMillis() + 1,
                text = "🥋 ${discipleProf.name} vs ${opponentProf.name} has officially begun under [${stipulation.displayName}]!",
                category = com.sect.idle.ui.components.LogCategory.GENERAL_COMMENTARY
            )
        )

        _uiState.update { current ->
            current.copy(
                battleState = BattleState(
                    inBattle = true,
                    areaName = loc,
                    enemyName = opponentProf.name,
                    enemyHp = boss.maxHp,
                    enemyMaxHp = boss.maxHp,
                    enemyElement = boss.element,
                    enemyPhase = 1,
                    enemyShield = boss.shieldHp,
                    discipleHp = disciple.hp,
                    discipleMaxHp = disciple.maxHp,
                    battleLog = listOf(
                        "🔔 DING DING DING! The Elder Referee calls for the bell!",
                        "🥋 ${discipleProf.name} vs ${opponentProf.name} has officially begun!",
                        "⚡ Stipulation: ${stipulation.displayName}"
                    ),
                    structuredLogs = initialStructuredLogs,
                    isVictory = null,
                    stipulation = stipulation,
                    discipleProfile = discipleProf,
                    opponentProfile = opponentProf,
                    crowdHype = 25,
                    opponentHype = 15,
                    momentum = 10,
                    refereeCall = "Elder Referee: \"TOUCH GLOVES & COMMENCE DAO BATTLE!\"",
                    playByPlayCommentary = "🎙️ Master Tianji: \"The bell sounds and the crowd is erupting! Both immortals circle the Bagua Octagon!\"",
                    colorCommentary = "💥 Elder Kuang: \"Look at the intensity in their eyes! This is for supreme sect dominance!\"",
                    crowdChant = CultivationArenaEngine.getCrowdChant(25, discipleProf.name),
                    lastMoveVisual = "IDLE",
                    roundTimeSeconds = 180,
                    currentRound = 1,
                    showTaleOfTheTape = true,
                    discipleLevel = disciple.level,
                    discipleCurrentExp = disciple.exp,
                    discipleMaxExp = disciple.maxExp,
                    peakCrowdHype = 25
                )
            )
        }
    }

    fun toggleTaleOfTheTape() {
        _uiState.update { current ->
            current.copy(
                battleState = current.battleState.copy(
                    showTaleOfTheTape = !current.battleState.showTaleOfTheTape
                )
            )
        }
    }

    fun executeArenaAction(action: CultivationArenaEngine.ArenaActionType) {
        val bState = _uiState.value.battleState
        if (!bState.inBattle || bState.isVictory != null) return

        val discipleProf = bState.discipleProfile ?: return
        val opponentProf = bState.opponentProfile ?: return
        val boss = currentBossState ?: return

        // Map action to VFX type & Log Category
        val (vfxType, logCategory) = when (action) {
            CultivationArenaEngine.ArenaActionType.MARTIAL_STRIKE -> Pair(com.sect.idle.render.CombatSkillVfxType.FLYING_SWORD_SLASH, com.sect.idle.ui.components.LogCategory.SWORD_TECHNIQUE)
            CultivationArenaEngine.ArenaActionType.HEAVENLY_GRAPPLE -> Pair(com.sect.idle.render.CombatSkillVfxType.HEAVENLY_SUPLEX_CRUSH, com.sect.idle.ui.components.LogCategory.SUPLEX_GRAPPLE)
            CultivationArenaEngine.ArenaActionType.BAGUA_SUBMISSION -> Pair(com.sect.idle.render.CombatSkillVfxType.BAGUA_PALM_SHOCKWAVE, com.sect.idle.ui.components.LogCategory.MERIDIAN_SUBMISSION)
            CultivationArenaEngine.ArenaActionType.ROPE_REBOUND -> Pair(com.sect.idle.render.CombatSkillVfxType.SPIRIT_ENERGY_BLAST, com.sect.idle.ui.components.LogCategory.ROPE_REBOUND)
            CultivationArenaEngine.ArenaActionType.TAUNT_CROWD -> Pair(com.sect.idle.render.CombatSkillVfxType.NONE, com.sect.idle.ui.components.LogCategory.GENERAL_COMMENTARY)
            CultivationArenaEngine.ArenaActionType.CELESTIAL_FINISHER -> Pair(com.sect.idle.render.CombatSkillVfxType.SPIRIT_ENERGY_BLAST, com.sect.idle.ui.components.LogCategory.CELESTIAL_FINISHER)
        }

        // 1. Resolve Disciple's Arena Action
        val playerResult = CultivationArenaEngine.executePlayerAction(
            action = action,
            attacker = discipleProf,
            defender = opponentProf,
            attackerHp = bState.discipleHp,
            defenderHp = bState.enemyHp,
            currentHype = bState.crowdHype,
            currentMomentum = bState.momentum,
            stipulation = bState.stipulation
        )

        // Play SFX corresponding to action
        when (action) {
            CultivationArenaEngine.ArenaActionType.MARTIAL_STRIKE -> {
                if (playerResult.isCritical) soundManager.playCombatSound(SoundManager.CombatSound.CRITICAL)
                else soundManager.playCombatSound(SoundManager.CombatSound.STRIKE)
            }
            CultivationArenaEngine.ArenaActionType.HEAVENLY_GRAPPLE -> {
                soundManager.playArenaSound(SoundManager.ArenaSound.GRAPPLE_SLAM)
                soundManager.playArenaSound(SoundManager.ArenaSound.CROWD_GASP)
            }
            CultivationArenaEngine.ArenaActionType.BAGUA_SUBMISSION -> {
                soundManager.playCombatSound(SoundManager.CombatSound.ICE_BURST)
                soundManager.playArenaSound(SoundManager.ArenaSound.REFEREE_COUNT)
            }
            CultivationArenaEngine.ArenaActionType.ROPE_REBOUND -> {
                soundManager.playArenaSound(SoundManager.ArenaSound.ROPE_BOUNCE)
                soundManager.playCombatSound(SoundManager.CombatSound.LIGHTNING_BURST)
            }
            CultivationArenaEngine.ArenaActionType.TAUNT_CROWD -> {
                soundManager.playArenaSound(SoundManager.ArenaSound.CROWD_CHEER)
                soundManager.playDiscipleSound(SoundManager.DiscipleSound.DAO_EPIPHANY)
            }
            CultivationArenaEngine.ArenaActionType.CELESTIAL_FINISHER -> {
                soundManager.playArenaSound(SoundManager.ArenaSound.FINISHER_HIT)
                soundManager.playArenaSound(SoundManager.ArenaSound.CROWD_CHEER)
            }
        }

        val newEnemyHp = (bState.enemyHp - playerResult.damage).coerceAtLeast(0)
        boss.currentHp = newEnemyHp

        var newHype = if (action == CultivationArenaEngine.ArenaActionType.CELESTIAL_FINISHER) 15
        else (bState.crowdHype + playerResult.hypeGain).coerceIn(0, 100)

        val newMomentum = (bState.momentum + playerResult.momentumDelta).coerceIn(-100, 100)
        val newLogs = (bState.battleLog + "⚔️ ${playerResult.attackerName}: ${playerResult.moveName} (-${playerResult.damage} HP)").takeLast(8)

        val newStructuredLog = com.sect.idle.ui.components.CombatLogEntry(
            id = System.currentTimeMillis(),
            text = "🥋 ${playerResult.attackerName} unleashes [${playerResult.moveName}] dealing ${playerResult.damage} DMG!",
            category = logCategory
        )
        val updatedStructuredLogs = (bState.structuredLogs + newStructuredLog).takeLast(15)

        val peakHype = maxOf(bState.peakCrowdHype, newHype)

        // Trigger VFX animation coroutine
        viewModelScope.launch {
            _uiState.update { s -> s.copy(battleState = s.battleState.copy(activeVfx = vfxType, vfxProgress = 0.2f)) }
            delay(120)
            _uiState.update { s -> s.copy(battleState = s.battleState.copy(vfxProgress = 0.7f)) }
            delay(120)
            _uiState.update { s -> s.copy(battleState = s.battleState.copy(vfxProgress = 1.0f)) }
            delay(80)
            _uiState.update { s -> s.copy(battleState = s.battleState.copy(activeVfx = com.sect.idle.render.CombatSkillVfxType.NONE, vfxProgress = 0f)) }
        }

        // Check if opponent is KO'd
        if (newEnemyHp <= 0) {
            handleMatchConclusion(isWin = true, bState.copy(structuredLogs = updatedStructuredLogs, peakCrowdHype = peakHype), newLogs, playerResult)
            return
        }

        _uiState.update { current ->
            current.copy(
                battleState = current.battleState.copy(
                    enemyHp = newEnemyHp,
                    crowdHype = newHype,
                    peakCrowdHype = peakHype,
                    momentum = newMomentum,
                    battleLog = newLogs,
                    structuredLogs = updatedStructuredLogs,
                    playByPlayCommentary = playerResult.playByPlayCommentary,
                    colorCommentary = playerResult.colorCommentary,
                    refereeCall = playerResult.refereeCall,
                    lastMoveVisual = playerResult.visualAnimation,
                    crowdChant = CultivationArenaEngine.getCrowdChant(newHype, discipleProf.name)
                )
            )
        }

        // 2. Opponent counter-attack turn with slight dramatic delay
        viewModelScope.launch {
            delay(750)
            if (!_uiState.value.battleState.inBattle || _uiState.value.battleState.isVictory != null) return@launch

            val curr = _uiState.value.battleState
            val oppResult = CultivationArenaEngine.executeOpponentTurn(
                opponent = opponentProf,
                disciple = discipleProf,
                opponentHp = curr.enemyHp,
                discipleHp = curr.discipleHp,
                opponentHype = curr.opponentHype,
                stipulation = curr.stipulation
            )

            // Play opponent SFX
            when (oppResult.moveType) {
                CultivationArenaEngine.ArenaActionType.HEAVENLY_GRAPPLE -> soundManager.playArenaSound(SoundManager.ArenaSound.GRAPPLE_SLAM)
                CultivationArenaEngine.ArenaActionType.ROPE_REBOUND -> soundManager.playArenaSound(SoundManager.ArenaSound.ROPE_BOUNCE)
                CultivationArenaEngine.ArenaActionType.CELESTIAL_FINISHER -> soundManager.playArenaSound(SoundManager.ArenaSound.FINISHER_HIT)
                else -> soundManager.playCombatSound(SoundManager.CombatSound.SWORD_SPAR)
            }

            val newDiscipleHp = (curr.discipleHp - oppResult.damage).coerceAtLeast(0)
            val oppHype = if (oppResult.moveType == CultivationArenaEngine.ArenaActionType.CELESTIAL_FINISHER) 10
            else (curr.opponentHype + oppResult.hypeGain).coerceIn(0, 100)

            val oppMomentum = (curr.momentum + oppResult.momentumDelta).coerceIn(-100, 100)
            val updatedLogs = (curr.battleLog + "💥 ${oppResult.attackerName}: ${oppResult.moveName} (-${oppResult.damage} HP)").takeLast(8)

            val oppStructuredLog = com.sect.idle.ui.components.CombatLogEntry(
                id = System.currentTimeMillis(),
                text = "💥 ${oppResult.attackerName} counters with [${oppResult.moveName}] dealing ${oppResult.damage} DMG!",
                category = com.sect.idle.ui.components.LogCategory.DAMAGE_TAKEN
            )
            val allStructuredLogs = (curr.structuredLogs + oppStructuredLog).takeLast(15)

            if (newDiscipleHp <= 0) {
                handleMatchConclusion(isWin = false, curr.copy(structuredLogs = allStructuredLogs), updatedLogs, oppResult)
                return@launch
            }

            _uiState.update { state ->
                state.copy(
                    battleState = state.battleState.copy(
                        discipleHp = newDiscipleHp,
                        opponentHype = oppHype,
                        momentum = oppMomentum,
                        battleLog = updatedLogs,
                        structuredLogs = allStructuredLogs,
                        playByPlayCommentary = oppResult.playByPlayCommentary,
                        colorCommentary = oppResult.colorCommentary,
                        refereeCall = oppResult.refereeCall,
                        lastMoveVisual = oppResult.visualAnimation
                    )
                )
            }
        }
    }

    private fun handleMatchConclusion(
        isWin: Boolean,
        bState: BattleState,
        logs: List<String>,
        lastAction: CultivationArenaEngine.ArenaActionResult
    ) {
        soundManager.playArenaSound(SoundManager.ArenaSound.RING_BELL)
        if (isWin) {
            soundManager.playArenaSound(SoundManager.ArenaSound.CROWD_CHEER)
            soundManager.playCombatSound(SoundManager.CombatSound.VICTORY)
        } else {
            soundManager.playCombatSound(SoundManager.CombatSound.DEFEAT)
        }

        val rewardStones = if (isWin) 350L else 50L
        val rewardQi = if (isWin) 600L else 100L
        val rewardFame = if (isWin) 45L else 5L
        val expGain = if (isWin) 85L else 25L

        val disciple = _uiState.value.disciples.firstOrNull()
        if (disciple != null) {
            viewModelScope.launch {
                discipleRepository.gainCombatExperience(disciple.id, expGain, isWin)
            }
        }

        val rewardsList = if (isWin) {
            listOf(
                "👑 ${bState.championshipBelt} Acquired!",
                "+$rewardStones Spirit Stones (Prize Purse)",
                "+$rewardQi Heavenly Qi Epiphany",
                "+$rewardFame Sect Prestige & Acclaim",
                "+$expGain Combat Experience Points"
            )
        } else {
            listOf(
                "🥈 Match Concluded (Ref Stop / Tapout)",
                "+$rewardStones Spirit Stones (Participant Purse)",
                "+$rewardQi Qi Consolation",
                "+$expGain Combat Experience Points"
            )
        }

        val finalPlayByPlay = if (isWin) {
            "🎙️ Master Tianji: \"THE REFEREE REACHES THREE! IT IS ALL OVER! ${bState.discipleProfile?.name} HAS CONQUERED THE ARENA!\""
        } else {
            "🎙️ Master Tianji: \"${bState.enemyName} scores the decisive pinfall! What a thrilling celestial showdown!\""
        }

        val finalColor = if (isWin) {
            "🏆 Elder Kuang: \"NEW CHAMPION! WHAT AN ABSOLUTE MASTERPIECE OF MARTIAL DONGHUA GLORY! Raise that championship belt!\""
        } else {
            "🎙️ Elder Kuang: \"A valiant effort! They will surely return for a rematch in the next realm tournament!\""
        }

        _uiState.update { current ->
            current.copy(
                spiritStones = current.spiritStones + rewardStones,
                qi = (current.qi + rewardQi).coerceAtMost(current.maxQi),
                reputation = current.reputation + rewardFame,
                battleState = current.battleState.copy(
                    isVictory = isWin,
                    rewards = rewardsList,
                    battleLog = logs + (if (isWin) "🏆 1... 2... 3! KNOCKOUT VICTORY! DING DING DING!" else "❌ Pinfall / Tapout loss. Return to mountain pavilion to train."),
                    playByPlayCommentary = finalPlayByPlay,
                    colorCommentary = finalColor,
                    discipleExpGain = expGain,
                    discipleLevel = disciple?.level ?: 1,
                    discipleCurrentExp = (disciple?.exp ?: 0L) + expGain,
                    discipleMaxExp = disciple?.maxExp ?: 100L,
                    refereeCall = if (isWin) "Elder Referee: \"1... 2... 3! WINNER BY KNOCKOUT!\"" else "Elder Referee: \"1... 2... 3! WINNER!\"",
                    lastMoveVisual = if (isWin) "VICTORY_BELT" else "DEFEAT"
                )
            )
        }
    }

    fun exitBattle() {
        currentBossState = null
        soundManager.setBgmTheme(SoundManager.BgmTheme.SECT_PEACE)
        _uiState.update { it.copy(battleState = BattleState()) }
    }

    // ====================================================
    // GEMINI AI EVENTS, PROPHESIES & DYNAMIC QUESTS
    // ====================================================

    override fun triggerRandomHeavenlyEvent() {
        val currentState = _uiState.value
        val discipleSummary = currentState.disciples.take(4).joinToString(", ") {
            "${it.name} (${GameConfig.getRealmName(it.realm)})"
        }
        val sectContext = "Sect: ${currentState.sectName} (${currentState.sectRankName}, Lv ${currentState.sectLevel}), Year ${currentState.year} ${currentState.season}, Qi: ${currentState.qi}/${currentState.maxQi}, Disciples: $discipleSummary"

        _uiState.update { it.copy(isAiThinking = true) }
        viewModelScope.launch {
            val event = sectAiRepository.generateRandomSectEvent(sectContext)
            _uiState.update {
                it.copy(
                    activeRandomEvent = event,
                    isAiThinking = false
                )
            }
        }
    }

    fun resolveEventChoice(choice: EventChoice) {
        val current = _uiState.value
        if (current.qi < choice.requiredQi || current.spiritStones < choice.requiredStones) {
            _uiState.update {
                it.copy(notificationMessage = "Insufficient spiritual resources to execute this Dao decision!")
            }
            return
        }

        val success = RNG.nextFloat() <= choice.successRate
        val qiCost = choice.requiredQi
        val stoneCost = choice.requiredStones

        val addedQi = if (success) 200L * current.sectLevel else 0L
        val addedStones = if (success) 100L * current.sectLevel else 0L
        val finalQi = (current.qi - qiCost + addedQi).coerceIn(0L, current.maxQi)
        val finalStones = (current.spiritStones - stoneCost + addedStones).coerceAtLeast(0L)
        val finalKarma = (current.karma + choice.karmaImpact).coerceAtLeast(0L)

        audioManager.playSfx(if (success) "breakthrough" else "gather")
        val outcomeMsg = if (success) choice.outcomeSuccess else choice.outcomeFailure

        _uiState.update {
            it.copy(
                qi = finalQi,
                spiritStones = finalStones,
                karma = finalKarma,
                activeRandomEvent = null,
                notificationMessage = outcomeMsg
            )
        }
    }

    override fun dismissRandomEvent() {
        _uiState.update { it.copy(activeRandomEvent = null) }
    }

    fun requestGrandmasterProphecy() {
        val currentState = _uiState.value
        val discipleSummary = currentState.disciples.take(5).joinToString(", ") {
            "${it.name} (${GameConfig.getRealmName(it.realm)})"
        }
        val sectContext = "Sect: ${currentState.sectName} (${currentState.sectRankName}, Lv ${currentState.sectLevel}), Year ${currentState.year} ${currentState.season}, Qi: ${currentState.qi}/${currentState.maxQi}, Disciples: $discipleSummary"

        _uiState.update { it.copy(isAiThinking = true) }
        viewModelScope.launch {
            val wisdom = sectAiRepository.getGrandmasterWisdom(sectContext)
            _uiState.update {
                it.copy(
                    latestWisdom = wisdom,
                    isAiThinking = false,
                    notificationMessage = "Grandmaster Ancestor bestowed a Heavenly Prophecy!"
                )
            }
        }
    }

    fun generateDynamicSectQuest(difficulty: Int = 1) {
        val currentState = _uiState.value
        val sectContext = "Sect: ${currentState.sectName}, Rank: ${currentState.sectRankName}, Disciples: ${currentState.disciples.size}"

        _uiState.update { it.copy(isAiThinking = true) }
        viewModelScope.launch {
            val quest = sectAiRepository.generateDynamicQuest(sectContext, difficulty)
            _uiState.update {
                it.copy(
                    activeDynamicQuest = quest,
                    isAiThinking = false,
                    notificationMessage = "New Celestial Quest generated: ${quest.title}"
                )
            }
        }
    }

    fun claimDynamicQuestReward() {
        val quest = _uiState.value.activeDynamicQuest ?: return
        audioManager.playSfx("breakthrough")
        _uiState.update { current ->
            current.copy(
                qi = (current.qi + quest.rewardQi).coerceAtMost(current.maxQi),
                spiritStones = current.spiritStones + quest.rewardStones,
                karma = current.karma + quest.rewardKarma,
                reputation = current.reputation + quest.rewardReputation,
                activeDynamicQuest = null,
                notificationMessage = "Completed [${quest.title}]! Reaped +${quest.rewardQi} Qi, +${quest.rewardStones} Stones, +${quest.rewardKarma} Karma!"
            )
        }
    }

    fun askGrandmaster(prompt: String) {
        if (prompt.isBlank()) return
        val currentState = _uiState.value
        val userMsg = AiMessage(sender = "Sect Master", text = prompt)
        _uiState.update {
            it.copy(
                aiMessages = it.aiMessages + userMsg,
                isAiThinking = true
            )
        }

        val discipleSummary = currentState.disciples.take(5).joinToString(", ") {
            "${it.name} (${GameConfig.getRealmName(it.realm)})"
        }
        val sectContext = "Sect: ${currentState.sectName} (${currentState.sectRankName}, Lv ${currentState.sectLevel}), Year ${currentState.year} ${currentState.season}, Qi: ${currentState.qi}/${currentState.maxQi}, Spirit Stones: ${currentState.spiritStones}, Disciples (${currentState.disciples.size}): $discipleSummary"

        viewModelScope.launch {
            val responseText = GrandmasterAiService.askGrandmaster(prompt, sectContext)
            val aiMsg = AiMessage(sender = "Ancient Ancestor", text = responseText)
            _uiState.update {
                it.copy(
                    aiMessages = it.aiMessages + aiMsg,
                    isAiThinking = false
                )
            }
        }
    }

    override fun toggleSound() {
        _uiState.update {
            val nextState = !it.soundEnabled
            audioManager.setEnabled(nextState)
            it.copy(soundEnabled = nextState)
        }
    }

    private fun loadGame() {
        val savedData = prefs.getString("save_json", null)
        if (savedData != null) {
            try {
                val jsonStr = CompressionUtil.decompressFromBase64(savedData)
                val json = JSONObject(if (jsonStr.isNotBlank()) jsonStr else savedData)
                val sectName = json.optString("sectName", "Cloud Mist Sect")
                val sectRankName = json.optString("sectRankName", "Mortal Sect")
                val sectLevel = json.optInt("sectLevel", 1)
                var qi = json.optLong("qi", 500)
                val maxQi = json.optLong("maxQi", 2000L + (sectLevel - 1) * 2000L)
                var spiritStones = json.optLong("spiritStones", 300)
                val karma = json.optLong("karma", 100)
                val reputation = json.optLong("reputation", 50)
                val day = json.optInt("day", 1)
                val season = json.optString("season", "Spring")
                val year = json.optInt("year", 1)
                val savedTimestamp = json.optLong("savedTimestamp", 0L)

                val buildingsList = mutableListOf<Building>()
                val bArray = json.optJSONArray("buildings")
                if (bArray != null) {
                    for (i in 0 until bArray.length()) {
                        val bObj = bArray.getJSONObject(i)
                        val b = Building(
                            bObj.optInt("type", 0),
                            bObj.optString("name"),
                            10,
                            100L,
                            5
                        )
                        b.level = bObj.optInt("level", 1)
                        b.posX = bObj.optInt("posX", 100)
                        b.posY = bObj.optInt("posY", 100)
                        b.build()
                        buildingsList.add(b)
                    }
                }

                val disciplesList = mutableListOf<Disciple>()
                val dArray = json.optJSONArray("disciples")
                if (dArray != null) {
                    for (i in 0 until dArray.length()) {
                        val dObj = dArray.getJSONObject(i)
                        val d = Disciple()
                        d.name = dObj.optString("name")
                        d.isMale = dObj.optBoolean("isMale", true)
                        d.element = dObj.optInt("element", 0)
                        d.realm = dObj.optInt("realm", 1)
                        d.realmExp = dObj.optInt("realmExp", 0)
                        d.hp = dObj.optInt("hp", 100)
                        d.maxHp = dObj.optInt("maxHp", 100)
                        d.atk = dObj.optInt("atk", 20)
                        d.def = dObj.optInt("def", 10)
                        d.talentGrade = dObj.optInt("talentGrade", 3)
                        d.currentTask = dObj.optInt("currentTask", 0)
                        disciplesList.add(d)
                    }
                }

                var offlineNotice: String? = null
                if (savedTimestamp > 0) {
                    val now = System.currentTimeMillis()
                    val elapsedSeconds = ((now - savedTimestamp) / 1000L).coerceIn(0L, 86400L) // up to 24 hours
                    if (elapsedSeconds > 10) {
                        val arrayLevel = buildingsList.find { it.type == 1 }?.level ?: 1
                        val offlineQiRate = arrayLevel * 2L
                        val offlineQiGained = (elapsedSeconds * offlineQiRate).coerceAtMost(maxQi - qi)
                        val offlineStones = (elapsedSeconds / 30L).coerceAtLeast(0L)

                        qi = (qi + offlineQiGained).coerceAtMost(maxQi)
                        spiritStones += offlineStones

                        // Offline disciples cultivation
                        disciplesList.forEach { d ->
                            if (d.currentTask == 0) {
                                d.realmExp += (elapsedSeconds * 2).toInt()
                            }
                        }

                        val mins = elapsedSeconds / 60
                        offlineNotice = "🌙 Welcome back! While away (${mins}m), your disciples gathered +$offlineQiGained Qi and +$offlineStones Spirit Stones!"
                    }
                }

                _uiState.value = SectUiState(
                    sectName = sectName,
                    sectRankName = sectRankName,
                    sectLevel = sectLevel,
                    qi = qi,
                    maxQi = maxQi,
                    spiritStones = spiritStones,
                    karma = karma,
                    reputation = reputation,
                    day = day,
                    season = season,
                    year = year,
                    notificationMessage = offlineNotice,
                    buildings = if (buildingsList.isNotEmpty()) buildingsList else createDefaultBuildings(),
                    disciples = if (disciplesList.isNotEmpty()) disciplesList else createDefaultDisciples(),
                    inventory = createDefaultInventory()
                )
                refreshCandidates()
                return
            } catch (ignored: Exception) {}
        }

        // Fresh Start Defaults
        _uiState.value = SectUiState(
            sectName = "Cloud Mist Sect",
            buildings = createDefaultBuildings(),
            disciples = createDefaultDisciples(),
            inventory = createDefaultInventory()
        )
        refreshCandidates()
    }

    private fun createDefaultBuildings(): List<Building> {
        val b0 = Building(0, "Mountain Main Hall", 10, 200L, 5).apply { level = 1; posX = 200; posY = 150; build() }
        val b1 = Building(1, "Spirit Gathering Array", 10, 200L, 5).apply { level = 1; posX = 100; posY = 280; build() }
        val b2 = Building(2, "Alchemy Chamber", 10, 200L, 5).apply { level = 1; posX = 300; posY = 260; build() }
        val b3 = Building(3, "Spirit Herb Garden", 10, 200L, 5).apply { level = 1; posX = 90; posY = 420; build() }
        val b4 = Building(4, "Scripture Pavilion", 10, 200L, 5).apply { level = 1; posX = 290; posY = 400; build() }
        val b5 = Building(5, "Demon Subduing Pagoda", 10, 200L, 5).apply { level = 1; posX = 200; posY = 520; build() }
        return listOf(b0, b1, b2, b3, b4, b5)
    }

    private fun createDefaultDisciples(): List<Disciple> {
        val d1 = Disciple().apply {
            name = "Lin Feng"
            isMale = true
            element = 0
            realm = 1
            realmExp = 50
            atk = 25
            def = 15
            talentGrade = 4
        }
        val d2 = Disciple().apply {
            name = "Su Qingxue"
            isMale = false
            element = 1
            realm = 2
            realmExp = 120
            atk = 38
            def = 22
            talentGrade = 5
        }
        return listOf(d1, d2)
    }

    private fun createDefaultInventory(): List<Item> {
        return listOf(
            Item("pill_0", "Qi Condensation Pill", Item.TYPE_CONSUMABLE, 1, 99, 100),
            Item("herb_1", "Spirit Gathering Grass", Item.TYPE_MATERIAL, 1, 99, 50),
            Item("herb_2", "Thousand-Year Fire Ginseng", Item.TYPE_MATERIAL, 2, 99, 120)
        )
    }

    fun saveGame() {
        val state = _uiState.value
        try {
            val json = JSONObject().apply {
                put("sectName", state.sectName)
                put("sectLevel", state.sectLevel)
                put("sectRankName", state.sectRankName)
                put("qi", state.qi)
                put("maxQi", state.maxQi)
                put("spiritStones", state.spiritStones)
                put("karma", state.karma)
                put("reputation", state.reputation)
                put("day", state.day)
                put("season", state.season)
                put("year", state.year)
                put("savedTimestamp", System.currentTimeMillis())

                val bArray = JSONArray()
                state.buildings.forEach { b ->
                    bArray.put(JSONObject().apply {
                        put("name", b.name)
                        put("type", b.type)
                        put("level", b.level)
                        put("posX", b.posX)
                        put("posY", b.posY)
                    })
                }
                put("buildings", bArray)

                val dArray = JSONArray()
                state.disciples.forEach { d ->
                    dArray.put(JSONObject().apply {
                        put("name", d.name)
                        put("isMale", d.isMale)
                        put("element", d.element)
                        put("realm", d.realm)
                        put("realmExp", d.realmExp)
                        put("hp", d.hp)
                        put("maxHp", d.maxHp)
                        put("atk", d.atk)
                        put("def", d.def)
                        put("talentGrade", d.talentGrade)
                        put("currentTask", d.currentTask)
                    })
                }
                put("disciples", dArray)
            }
            val compressed = CompressionUtil.compressToBase64(json.toString())
            prefs.edit().putString("save_json", compressed).apply()

            // Asynchronously synchronize with Room Database
            viewModelScope.launch(ioDispatcher) {
                syncRoomDatabaseEntities(state)
            }
        } catch (ignored: Exception) {}
    }

    override fun onCleared() {
        super.onCleared()
        loopJob?.cancel()
        saveGame()
    }
}
