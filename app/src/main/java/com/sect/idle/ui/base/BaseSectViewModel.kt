package com.sect.idle.ui.base

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sect.idle.ai.CultivationAiEngine
import com.sect.idle.ai.CultivationArenaEngine
import com.sect.idle.ai.DynamicSectQuest
import com.sect.idle.ai.EventChoice
import com.sect.idle.ai.GrandmasterWisdom
import com.sect.idle.ai.ISectAiRepository
import com.sect.idle.ai.SectAiRepository
import com.sect.idle.ai.SectRandomEvent
import com.sect.idle.core.GameConfig
import com.sect.idle.data.db.SectDatabase
import com.sect.idle.data.db.entities.SectProfileEntity
import com.sect.idle.data.repository.BuildingRepository
import com.sect.idle.data.repository.CultivationRepository
import com.sect.idle.data.repository.DiscipleRepository
import com.sect.idle.data.repository.IBuildingRepository
import com.sect.idle.data.repository.ICultivationRepository
import com.sect.idle.data.repository.IDiscipleRepository
import com.sect.idle.data.repository.ITaskRepository
import com.sect.idle.data.repository.TaskRepository
import com.sect.idle.models.Building
import com.sect.idle.models.Disciple
import com.sect.idle.models.Item
import com.sect.idle.models.Quest
import com.sect.idle.systems.AudioManager
import com.sect.idle.systems.CompressionUtil
import com.sect.idle.systems.RNG
import com.sect.idle.systems.SoundManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

// ============================================================================
// UI STATE MODELS
// ============================================================================

data class QiOrb(
    val id: String = UUID.randomUUID().toString(),
    val x: Float,
    val y: Float,
    val amount: Long,
    val color: Int
)

data class BattleState(
    val inBattle: Boolean = false,
    val areaName: String = "",
    val enemyName: String = "",
    val enemyHp: Int = 100,
    val enemyMaxHp: Int = 100,
    val enemyElement: Int = 0,
    val enemyPhase: Int = 1,
    val enemyShield: Int = 0,
    val discipleHp: Int = 100,
    val discipleMaxHp: Int = 100,
    val battleLog: List<String> = emptyList(),
    val isVictory: Boolean? = null,
    val rewards: List<String> = emptyList(),
    // Xianxia Martial Arts & WWE / MMA Arena Expansion
    val stipulation: CultivationArenaEngine.MatchStipulation = CultivationArenaEngine.MatchStipulation.GRAND_DAO_CHAMPIONSHIP,
    val discipleProfile: CultivationArenaEngine.FighterProfile? = null,
    val opponentProfile: CultivationArenaEngine.FighterProfile? = null,
    val crowdHype: Int = 15,
    val opponentHype: Int = 10,
    val momentum: Int = 0,
    val refereeCall: String = "Elder Referee: \"TOUCH GLOVES & COMMENCE DAO BATTLE!\"",
    val playByPlayCommentary: String = "🎙️ Master Tianji: \"Welcome to the Grand Dao Martial Arena! The atmosphere is electric!\"",
    val colorCommentary: String = "💥 Elder Kuang: \"Two titanic cultivators in the ring tonight! Let's get it on!\"",
    val crowdChant: String = "📣 [AUDIENCE] \"FIGHT! FIGHT! FOR THE SECT!\"",
    val lastMoveVisual: String = "IDLE",
    val roundTimeSeconds: Int = 180,
    val currentRound: Int = 1,
    val showTaleOfTheTape: Boolean = false,
    val championshipBelt: String = "Grand Dao Celestial World Belt",
    val structuredLogs: List<com.sect.idle.ui.components.CombatLogEntry> = emptyList(),
    val activeVfx: com.sect.idle.render.CombatSkillVfxType = com.sect.idle.render.CombatSkillVfxType.NONE,
    val vfxProgress: Float = 0f,
    val discipleLevel: Int = 1,
    val discipleExpGain: Long = 0L,
    val discipleCurrentExp: Long = 0L,
    val discipleMaxExp: Long = 100L,
    val peakCrowdHype: Int = 15
)

data class AiMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class SectUiState(
    val sectName: String = "Cloud Mist Sect",
    val sectLevel: Int = 1,
    val sectRankName: String = "Mortal Sect",
    val qi: Long = 500L,
    val maxQi: Long = 2000L,
    val spiritStones: Long = 300L,
    val karma: Long = 100L,
    val reputation: Long = 50L,
    val day: Int = 1,
    val season: String = "Spring",
    val year: Int = 1,
    val disciples: List<Disciple> = emptyList(),
    val buildings: List<Building> = emptyList(),
    val inventory: List<Item> = emptyList(),
    val activeQuests: List<Quest> = emptyList(),
    val qiOrbs: List<QiOrb> = emptyList(),
    val battleState: BattleState = BattleState(),
    val aiMessages: List<AiMessage> = emptyList(),
    val isAiThinking: Boolean = false,
    val soundEnabled: Boolean = true,
    val activeTab: Int = 0, // 0: Mountain Canvas, 1: Disciples, 2: Sect Facilities, 3: Cultivation/Alchemy, 4: Expeditions, 5: Grandmaster AI
    val notificationMessage: String? = null,
    val candidateDisciples: List<Disciple> = emptyList(),
    val activeRandomEvent: SectRandomEvent? = null,
    val latestWisdom: GrandmasterWisdom? = null,
    val activeDynamicQuest: DynamicSectQuest? = null,
    val combatStance: CultivationAiEngine.CombatStance = CultivationAiEngine.CombatStance.BALANCED
)

sealed interface SectUiEvent {
    data class ShowToast(val message: String) : SectUiEvent
    data class BreakthroughCelebration(val discipleName: String, val newRealm: Int) : SectUiEvent
    data class RandomEventTriggered(val event: SectRandomEvent) : SectUiEvent
}

// ============================================================================
// BASE SECT VIEWMODEL
// ============================================================================

abstract class BaseSectViewModel(
    application: Application,
    val discipleRepository: IDiscipleRepository = DiscipleRepository(SectDatabase.getInstance(application).discipleDao()),
    val buildingRepository: IBuildingRepository = BuildingRepository(SectDatabase.getInstance(application).buildingDao()),
    val taskRepository: ITaskRepository = TaskRepository(SectDatabase.getInstance(application).taskDao()),
    val cultivationRepository: ICultivationRepository = CultivationRepository(SectDatabase.getInstance(application).cultivationDao()),
    val sectAiRepository: ISectAiRepository = SectAiRepository(),
    val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AndroidViewModel(application) {

    protected val context: Context = application.applicationContext
    val soundManager: SoundManager = SoundManager.get(application)
    val audioManager: AudioManager = AudioManager.get(application)
    private val prefs = context.getSharedPreferences("IdleSectSaveState", Context.MODE_PRIVATE)

    protected val _uiState = MutableStateFlow(SectUiState())
    val uiState: StateFlow<SectUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<SectUiEvent>()
    val uiEvent: SharedFlow<SectUiEvent> = _uiEvent.asSharedFlow()

    // ========================================================================
    // 1. DISCIPLE MANAGEMENT STATE HANDLERS
// ========================================================================

    open fun recruitDisciple(disciple: Disciple) {
        viewModelScope.launch(ioDispatcher) {
            val current = _uiState.value
            val cost = 50L + current.disciples.size * 20L
            if (current.spiritStones < cost) {
                postNotification("Insufficient Spirit Stones ($cost required) to recruit ${disciple.name}.")
                return@launch
            }

            val updatedDisciples = current.disciples.toMutableList().apply { add(disciple) }
            val updatedCandidates = current.candidateDisciples.filter { it.id != disciple.id }

            _uiState.update {
                it.copy(
                    spiritStones = it.spiritStones - cost,
                    disciples = updatedDisciples,
                    candidateDisciples = updatedCandidates,
                    reputation = it.reputation + 5,
                    notificationMessage = "🎉 Recruited disciple ${disciple.name} to the Sect!"
                )
            }

            // Sync with Room Database
            discipleRepository.saveDisciple(disciple)
            audioManager.playSfx("breakthrough")
        }
    }

    open fun dismissDisciple(discipleId: String) {
        viewModelScope.launch(ioDispatcher) {
            val d = _uiState.value.disciples.find { it.id == discipleId } ?: return@launch
            _uiState.update {
                it.copy(
                    disciples = it.disciples.filter { disc -> disc.id != discipleId },
                    notificationMessage = "Disciple ${d.name} has departed from the sect."
                )
            }
            discipleRepository.deleteDisciple(discipleId)
            audioManager.playSfx("strike")
        }
    }

    open fun assignDiscipleTask(discipleId: String, taskId: Int) {
        viewModelScope.launch(ioDispatcher) {
            val list = _uiState.value.disciples.map { d ->
                if (d.id == discipleId) {
                    d.currentTask = taskId
                    d
                } else d
            }
            _uiState.update { it.copy(disciples = list) }
            discipleRepository.assignTask(discipleId, taskId)
        }
    }

    open fun assignAllTasks(taskId: Int) {
        viewModelScope.launch(ioDispatcher) {
            val list = _uiState.value.disciples.map { d ->
                d.currentTask = taskId
                d
            }
            _uiState.update { it.copy(disciples = list) }
            discipleRepository.saveAllDisciples(list)
            val taskName = GameConfig.getTaskName(taskId)
            postNotification("All disciples assigned to: $taskName")
        }
    }

    open fun attemptBreakthrough(discipleId: String) {
        viewModelScope.launch(ioDispatcher) {
            val (success, message) = discipleRepository.attemptBreakthrough(discipleId)
            val updatedList = discipleRepository.getAllDisciples()

            _uiState.update {
                it.copy(
                    disciples = if (updatedList.isNotEmpty()) updatedList else it.disciples,
                    notificationMessage = message
                )
            }

            if (success) {
                audioManager.playSfx("breakthrough")
                val d = updatedList.find { it.id == discipleId }
                if (d != null) {
                    _uiEvent.emit(SectUiEvent.BreakthroughCelebration(d.name, d.realm))
                }
            } else {
                audioManager.playSfx("fail")
            }
        }
    }

    open fun refreshCandidateRecruits() {
        viewModelScope.launch(ioDispatcher) {
            val current = _uiState.value
            val cost = 20L
            if (current.spiritStones < cost) {
                postNotification("Need 20 Spirit Stones to post recruitment notices.")
                return@launch
            }

            val candidates = discipleRepository.generateRecruitmentCandidates(4, current.sectLevel)
            _uiState.update {
                it.copy(
                    spiritStones = it.spiritStones - cost,
                    candidateDisciples = candidates,
                    notificationMessage = "New prospective disciples have arrived at the mountain gate!"
                )
            }
        }
    }

    // ========================================================================
    // 2. SECT BUILDING MANAGEMENT STATE HANDLERS
    // ========================================================================

    open fun upgradeBuilding(type: Int) {
        viewModelScope.launch(ioDispatcher) {
            val current = _uiState.value
            val (success, message) = buildingRepository.upgradeBuilding(type, current.spiritStones)

            if (success) {
                val building = buildingRepository.getBuildingByType(type)
                val cost = building?.getUpgradeCost() ?: 100L
                val updatedBuildings = buildingRepository.getAllBuildings()

                _uiState.update {
                    it.copy(
                        spiritStones = (it.spiritStones - cost).coerceAtLeast(0L),
                        buildings = if (updatedBuildings.isNotEmpty()) updatedBuildings else it.buildings,
                        reputation = it.reputation + 10,
                        notificationMessage = message
                    )
                }
                audioManager.playSfx("build")
            } else {
                postNotification(message)
                audioManager.playSfx("fail")
            }
        }
    }

    open fun demolishBuilding(type: Int) {
        viewModelScope.launch(ioDispatcher) {
            val (success, message) = buildingRepository.demolishBuilding(type)
            if (success) {
                val updatedBuildings = buildingRepository.getAllBuildings()
                _uiState.update {
                    it.copy(
                        buildings = if (updatedBuildings.isNotEmpty()) updatedBuildings else it.buildings,
                        notificationMessage = message
                    )
                }
                audioManager.playSfx("strike")
            } else {
                postNotification(message)
                audioManager.playSfx("fail")
            }
        }
    }

    open fun assignBuildingWorker(type: Int) {
        viewModelScope.launch(ioDispatcher) {
            val success = buildingRepository.assignWorker(type)
            if (success) {
                val list = buildingRepository.getAllBuildings()
                _uiState.update { it.copy(buildings = list) }
            }
        }
    }

    open fun removeBuildingWorker(type: Int) {
        viewModelScope.launch(ioDispatcher) {
            val success = buildingRepository.removeWorker(type)
            if (success) {
                val list = buildingRepository.getAllBuildings()
                _uiState.update { it.copy(buildings = list) }
            }
        }
    }

    // ========================================================================
    // 3. CULTIVATION & DAO TASKS STATE HANDLERS
    // ========================================================================

    open fun tapMountainQiGather() {
        _uiState.update {
            val gatherAmount = (5L * it.sectLevel).coerceAtLeast(5L)
            val newQi = (it.qi + gatherAmount).coerceAtMost(it.maxQi)
            it.copy(qi = newQi)
        }
        audioManager.playSfx("gather")
    }

    open fun collectQiOrb(orbId: String) {
        val orb = _uiState.value.qiOrbs.find { it.id == orbId } ?: return
        _uiState.update {
            val newQi = (it.qi + orb.amount).coerceAtMost(it.maxQi)
            it.copy(
                qi = newQi,
                qiOrbs = it.qiOrbs.filter { o -> o.id != orbId }
            )
        }
        audioManager.playSfx("gather")
    }

    open fun collectAllQi() {
        val totalAmount = _uiState.value.qiOrbs.sumOf { it.amount }
        _uiState.update {
            val bonus = 25L * it.sectLevel
            val newQi = (it.qi + totalAmount + bonus).coerceAtMost(it.maxQi)
            it.copy(
                qi = newQi,
                qiOrbs = emptyList(),
                notificationMessage = "Harvested spiritual essence (+${totalAmount + bonus} Qi)"
            )
        }
        audioManager.playSfx("breakthrough")
    }

    open fun craftPill(pillType: Int) {
        viewModelScope.launch(ioDispatcher) {
            val state = _uiState.value
            val result = cultivationRepository.craftPill(pillType, state.qi, state.spiritStones)

            if (result.success) {
                val costQi = when (pillType) { 0 -> 150L; 1 -> 350L; 2 -> 800L; else -> 1200L }
                val costStones = when (pillType) { 0 -> 50L; 1 -> 120L; 2 -> 300L; else -> 500L }

                val updatedDisciples = state.disciples.map { d ->
                    d.realmExp += result.expGranted
                    d
                }
                discipleRepository.saveAllDisciples(updatedDisciples)

                _uiState.update {
                    it.copy(
                        qi = it.qi - costQi,
                        spiritStones = it.spiritStones - costStones,
                        disciples = updatedDisciples,
                        notificationMessage = result.message
                    )
                }
                audioManager.playSfx("breakthrough")
            } else {
                postNotification(result.message)
                audioManager.playSfx("fail")
            }
        }
    }

    open fun comprehendScripture(scriptureId: String, name: String, costQi: Long, costStones: Long) {
        viewModelScope.launch(ioDispatcher) {
            val state = _uiState.value
            if (state.qi < costQi || state.spiritStones < costStones) {
                postNotification("Insufficient Qi or Spirit Stones to comprehend $name.")
                return@launch
            }

            cultivationRepository.unlockScripture(scriptureId, name, costQi, costStones)
            _uiState.update {
                it.copy(
                    qi = it.qi - costQi,
                    spiritStones = it.spiritStones - costStones,
                    reputation = it.reputation + 25,
                    notificationMessage = "📖 Comprehended $name! Sect aura permanently enhanced."
                )
            }
            audioManager.playSfx("breakthrough")
        }
    }

    // ========================================================================
    // 4. GEMINI API AI REPOSITORY INTEGRATION HANDLERS
    // ========================================================================

    open fun askGrandmasterWisdom(customQuery: String? = null) {
        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(isAiThinking = true) }
            val context = buildSectContextSummary(_uiState.value)
            val wisdom = sectAiRepository.getGrandmasterWisdom(context, customQuery)

            _uiState.update {
                val newMessages = it.aiMessages.toMutableList().apply {
                    if (!customQuery.isNullOrBlank()) {
                        add(AiMessage(sender = "Sect Master", text = customQuery))
                    }
                    add(AiMessage(sender = "Ancestral Spirit", text = wisdom.text))
                }
                it.copy(
                    isAiThinking = false,
                    latestWisdom = wisdom,
                    aiMessages = newMessages,
                    notificationMessage = "⚡ Grandmaster Ancestor shared Dao insight!"
                )
            }
        }
    }

    open fun triggerRandomHeavenlyEvent() {
        viewModelScope.launch(ioDispatcher) {
            val context = buildSectContextSummary(_uiState.value)
            val event = sectAiRepository.generateRandomSectEvent(context)
            _uiState.update { it.copy(activeRandomEvent = event) }
            _uiEvent.emit(SectUiEvent.RandomEventTriggered(event))
        }
    }

    open fun resolveRandomEvent(choice: EventChoice) {
        val current = _uiState.value
        if (current.qi < choice.requiredQi || current.spiritStones < choice.requiredStones) {
            postNotification("Insufficient Qi or Spirit Stones for this choice.")
            return
        }

        val success = RNG.nextFloat() <= choice.successRate
        val outcome = if (success) choice.outcomeSuccess else choice.outcomeFailure

        _uiState.update {
            val newKarma = it.karma + (if (success) choice.karmaImpact else (choice.karmaImpact / 2))
            val newRep = it.reputation + (if (success) 15 else 5)
            it.copy(
                qi = (it.qi - choice.requiredQi + (if (success) 100L else 0L)).coerceAtLeast(0L).coerceAtMost(it.maxQi),
                spiritStones = (it.spiritStones - choice.requiredStones + (if (success) 50L else 0L)).coerceAtLeast(0L),
                karma = newKarma,
                reputation = newRep,
                activeRandomEvent = null,
                notificationMessage = "${if (success) "✨ Outcome:" else "⚠️ Tribulation:"} $outcome"
            )
        }
        audioManager.playSfx(if (success) "breakthrough" else "fail")
    }

    open fun dismissRandomEvent() {
        _uiState.update { it.copy(activeRandomEvent = null) }
    }

    open fun generateDynamicQuestWithAi() {
        viewModelScope.launch(ioDispatcher) {
            val context = buildSectContextSummary(_uiState.value)
            val quest = sectAiRepository.generateDynamicQuest(context, _uiState.value.sectLevel)
            _uiState.update {
                it.copy(
                    activeDynamicQuest = quest,
                    notificationMessage = "📜 Heavenly Quest Manifested: ${quest.title}"
                )
            }
        }
    }

    // ========================================================================
    // 5. HELPER METHODS & PERSISTENCE
    // ========================================================================

    protected fun postNotification(message: String) {
        _uiState.update { it.copy(notificationMessage = message) }
    }

    open fun clearNotification() {
        _uiState.update { it.copy(notificationMessage = null) }
    }

    open fun selectTab(tabIndex: Int) {
        soundManager.playSectSound(SoundManager.SectSound.UI_CLICK)
        _uiState.update { it.copy(activeTab = tabIndex) }
    }

    open fun toggleSound() {
        _uiState.update {
            val newSound = !it.soundEnabled
            soundManager.setSoundEnabled(newSound)
            it.copy(soundEnabled = newSound)
        }
    }

    open fun pauseAudio() {
        soundManager.pauseAllAudio()
    }

    open fun resumeAudio() {
        if (_uiState.value.soundEnabled) {
            soundManager.resumeAllAudio()
        }
    }

    open fun playSfx(sfxId: String) {
        if (_uiState.value.soundEnabled) {
            audioManager.playSfx(sfxId)
        }
    }

    private fun buildSectContextSummary(state: SectUiState): String {
        return "Sect: ${state.sectName} (Rank: ${state.sectRankName}, Lv.${state.sectLevel}). Qi: ${state.qi}/${state.maxQi}, Stones: ${state.spiritStones}, Karma: ${state.karma}, Fame: ${state.reputation}, Disciples: ${state.disciples.size}, Season: ${state.season} Day ${state.day % 30 + 1}, Year ${state.year}."
    }

    protected suspend fun syncRoomDatabaseEntities(state: SectUiState) {
        try {
            discipleRepository.saveAllDisciples(state.disciples)
            buildingRepository.saveAllBuildings(state.buildings)
            val db = SectDatabase.getInstance(context)
            db.sectProfileDao().saveProfile(
                SectProfileEntity(
                    id = 1,
                    sectName = state.sectName,
                    sectRankName = state.sectRankName,
                    sectLevel = state.sectLevel,
                    qi = state.qi,
                    maxQi = state.maxQi,
                    spiritStones = state.spiritStones,
                    karma = state.karma,
                    reputation = state.reputation,
                    day = state.day,
                    season = state.season,
                    year = state.year,
                    soundEnabled = state.soundEnabled,
                    lastSaveTimestamp = System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            // Graceful non-blocking catch
        }
    }
}
