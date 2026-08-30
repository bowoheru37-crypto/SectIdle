package com.sect.idle.ai

import com.sect.idle.core.GameConfig
import com.sect.idle.core.Vector2
import com.sect.idle.models.Disciple
import com.sect.idle.systems.RNG
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Advanced AI Behavior, Movement, Personality, and Combat Decision Engine
 * for NPCs, Disciples, Demonic Enemies, and World Bosses.
 */
object CultivationAiEngine {

    // ==========================================
    // 1. NPC DISCIPLE BEHAVIOR & MOVEMENT AI
    // ==========================================

    enum class NpcActionState {
        IDLE,
        WANDERING,
        CULTIVATING,
        GATHERING,
        ALCHEMIZING,
        SPARRING,
        DISCUSSING_DAO,
        MEDITATING,
        RESTING,
        EPIPHANY
    }

    data class NpcAiAgent(
        val discipleId: String,
        var state: NpcActionState = NpcActionState.WANDERING,
        val currentPos: Vector2 = Vector2(),
        val targetPos: Vector2 = Vector2(),
        val velocity: Vector2 = Vector2(),
        var stateTimer: Float = 0f,
        var stateDuration: Float = 4f,
        var partnerId: String? = null,
        var currentThought: String = "Observing the sky...",
        var emoteIcon: String = "☯️",
        var wanderAngle: Float = 0f
    )

    private val activeNpcAgents = mutableMapOf<String, NpcAiAgent>()

    fun getOrRegisterAgent(disciple: Disciple): NpcAiAgent {
        return activeNpcAgents.getOrPut(disciple.id) {
            val agent = NpcAiAgent(
                discipleId = disciple.id,
                currentPos = Vector2(disciple.position.x, disciple.position.y),
                targetPos = Vector2(disciple.position.x, disciple.position.y)
            )
            assignNewAction(agent, disciple)
            agent
        }
    }

    fun getAllAgents(): Map<String, NpcAiAgent> = activeNpcAgents

    /**
     * Process autonomous AI updates for all disciples on the sect mountain grounds.
     */
    fun tickNpcAi(disciples: List<Disciple>, boundsWidth: Float = 400f, boundsHeight: Float = 600f, dt: Float = 0.2f) {
        // Cleanup removed disciples
        val activeIds = disciples.map { it.id }.toSet()
        activeNpcAgents.keys.retainAll(activeIds)

        for (disciple in disciples) {
            val agent = getOrRegisterAgent(disciple)
            agent.stateTimer += dt

            // Check if state duration expired -> transition to next behavior
            if (agent.stateTimer >= agent.stateDuration) {
                assignNewAction(agent, disciple)
            }

            // Execute movement & steering behavior
            updateNpcMovement(agent, disciple, boundsWidth, boundsHeight, dt)

            // Execute social AI & nearby disciple interactions
            checkSocialInteractions(agent, disciple, disciples)

            // Sync back to disciple model
            disciple.position.set(agent.currentPos.x, agent.currentPos.y)
        }
    }

    private fun assignNewAction(agent: NpcAiAgent, disciple: Disciple) {
        agent.stateTimer = 0f
        agent.stateDuration = RNG.nextFloat(3f, 7f)
        agent.partnerId = null

        // Task override prioritization
        when (disciple.currentTask) {
            0 -> { // Assigned Cultivation
                agent.state = if (RNG.nextFloat() < 0.7f) NpcActionState.CULTIVATING else NpcActionState.MEDITATING
                agent.currentThought = "Channelling spiritual Qi into Dantian."
                agent.emoteIcon = "🧘"
                agent.targetPos.set(RNG.nextFloat(140f, 260f), RNG.nextFloat(200f, 320f))
                return
            }
            1 -> { // Gathering Herbs
                agent.state = NpcActionState.GATHERING
                agent.currentThought = "Searching for 100-year spirit ginseng."
                agent.emoteIcon = "🌿"
                agent.targetPos.set(RNG.nextFloat(50f, 350f), RNG.nextFloat(350f, 500f))
                return
            }
            2 -> { // Pill Alchemy
                agent.state = NpcActionState.ALCHEMIZING
                agent.currentThought = "Controlling furnace flame temperature."
                agent.emoteIcon = "🔥"
                agent.targetPos.set(RNG.nextFloat(70f, 150f), RNG.nextFloat(220f, 300f))
                return
            }
            4 -> { // Beast Hunting
                agent.state = NpcActionState.SPARRING
                agent.currentThought = "Honing sword intent for the hunt!"
                agent.emoteIcon = "⚔️"
                agent.targetPos.set(RNG.nextFloat(250f, 360f), RNG.nextFloat(380f, 520f))
                return
            }
        }

        // Personality-Driven Autonomous Selection
        val roll = RNG.nextFloat()
        when (disciple.personality) {
            GameConfig.PER_ARROGANT -> {
                if (roll < 0.45f) {
                    agent.state = NpcActionState.CULTIVATING
                    agent.currentThought = "I shall surpass all peers under heaven!"
                    agent.emoteIcon = "⚡"
                    agent.targetPos.set(200f, 180f) // Peak
                } else if (roll < 0.8f) {
                    agent.state = NpcActionState.SPARRING
                    agent.currentThought = "None can withstand my supreme strike!"
                    agent.emoteIcon = "🗡️"
                    agent.targetPos.set(RNG.nextFloat(100f, 300f), RNG.nextFloat(280f, 400f))
                } else {
                    agent.state = NpcActionState.WANDERING
                    agent.currentThought = "Walking with solitary pride."
                    agent.emoteIcon = "✨"
                }
            }
            GameConfig.PER_DILIGENT -> {
                if (roll < 0.6f) {
                    agent.state = NpcActionState.CULTIVATING
                    agent.currentThought = "Repetition builds the foundation of immortality."
                    agent.emoteIcon = "🧘"
                } else {
                    agent.state = NpcActionState.GATHERING
                    agent.currentThought = "Every grain of spiritual sand counts."
                    agent.emoteIcon = "🌱"
                }
                agent.targetPos.set(RNG.nextFloat(80f, 320f), RNG.nextFloat(250f, 450f))
            }
            GameConfig.PER_MYSTERIOUS -> {
                if (roll < 0.4f) {
                    agent.state = NpcActionState.EPIPHANY
                    agent.currentThought = "Gazing upon the celestial Dao matrix..."
                    agent.emoteIcon = "💡"
                    disciple.realmExp += 10
                } else {
                    agent.state = NpcActionState.MEDITATING
                    agent.currentThought = "Merging spirit with misty clouds."
                    agent.emoteIcon = "🌫️"
                }
                agent.targetPos.set(RNG.nextFloat(160f, 240f), RNG.nextFloat(140f, 220f))
            }
            GameConfig.PER_FIGHTER -> {
                agent.state = NpcActionState.SPARRING
                agent.currentThought = "My sword thirsts for true combat!"
                agent.emoteIcon = "⚔️"
                agent.targetPos.set(RNG.nextFloat(120f, 280f), RNG.nextFloat(300f, 420f))
            }
            GameConfig.PER_SCHOLAR, GameConfig.PER_WISE -> {
                if (roll < 0.5f) {
                    agent.state = NpcActionState.MEDITATING
                    agent.currentThought = "Deciphering the Nine Heavens Scripture."
                    agent.emoteIcon = "📜"
                } else {
                    agent.state = NpcActionState.DISCUSSING_DAO
                    agent.currentThought = "The Dao that can be named is not eternal."
                    agent.emoteIcon = "☯️"
                }
                agent.targetPos.set(RNG.nextFloat(100f, 300f), RNG.nextFloat(200f, 350f))
            }
            GameConfig.PER_LAZY -> {
                agent.state = NpcActionState.RESTING
                agent.currentThought = "Napping under the spiritual peach tree..."
                agent.emoteIcon = "💤"
                agent.targetPos.set(RNG.nextFloat(60f, 150f), RNG.nextFloat(400f, 500f))
            }
            else -> {
                agent.state = NpcActionState.WANDERING
                agent.currentThought = "Taking a peaceful stroll in the sect grounds."
                agent.emoteIcon = "🍃"
                agent.targetPos.set(RNG.nextFloat(60f, 340f), RNG.nextFloat(200f, 480f))
            }
        }
    }

    private fun updateNpcMovement(agent: NpcAiAgent, disciple: Disciple, width: Float, height: Float, dt: Float) {
        val dx = agent.targetPos.x - agent.currentPos.x
        val dy = agent.targetPos.y - agent.currentPos.y
        val dist = sqrt(dx * dx + dy * dy)

        if (dist > 3f) {
            val speed = disciple.moveSpeed * 25f
            val nx = (dx / dist) * speed
            val ny = (dy / dist) * speed

            // Smooth velocity steering
            agent.velocity.x += (nx - agent.velocity.x) * 0.2f
            agent.velocity.y += (ny - agent.velocity.y) * 0.2f

            agent.currentPos.x += agent.velocity.x * dt
            agent.currentPos.y += agent.velocity.y * dt
            disciple.isMoving = true
            disciple.facing = if (agent.velocity.x >= 0) 1 else -1
        } else {
            agent.velocity.set(0f, 0f)
            disciple.isMoving = false

            // Add slight wander drift while idle
            agent.wanderAngle += RNG.nextFloat(-0.2f, 0.2f)
            agent.currentPos.x += cos(agent.wanderAngle) * 0.3f
            agent.currentPos.y += sin(agent.wanderAngle) * 0.3f
        }

        // Keep within mountain bounds
        agent.currentPos.x = agent.currentPos.x.coerceIn(40f, width - 40f)
        agent.currentPos.y = agent.currentPos.y.coerceIn(120f, height - 80f)
    }

    private fun checkSocialInteractions(agent: NpcAiAgent, disciple: Disciple, allDisciples: List<Disciple>) {
        if (agent.state == NpcActionState.SPARRING || agent.state == NpcActionState.DISCUSSING_DAO) return

        for (other in allDisciples) {
            if (other.id == disciple.id) continue
            val otherAgent = activeNpcAgents[other.id] ?: continue

            val distSq = (agent.currentPos.x - otherAgent.currentPos.x) * (agent.currentPos.x - otherAgent.currentPos.x) +
                    (agent.currentPos.y - otherAgent.currentPos.y) * (agent.currentPos.y - otherAgent.currentPos.y)

            if (distSq < 30f * 30f && RNG.nextFloat() < 0.05f) {
                // Social interaction triggers!
                if (disciple.personality == GameConfig.PER_FIGHTER || other.personality == GameConfig.PER_FIGHTER) {
                    agent.state = NpcActionState.SPARRING
                    agent.partnerId = other.id
                    agent.stateDuration = 4f
                    agent.currentThought = "Sparring sword techniques with ${other.name}!"
                    agent.emoteIcon = "⚔️"

                    otherAgent.state = NpcActionState.SPARRING
                    otherAgent.partnerId = disciple.id
                    otherAgent.stateDuration = 4f
                    otherAgent.currentThought = "Trading martial blows with ${disciple.name}!"
                    otherAgent.emoteIcon = "⚔️"

                    disciple.realmExp += 5
                    other.realmExp += 5
                } else if (disciple.personality == GameConfig.PER_KIND) {
                    agent.currentThought = "Shared herbal spiritual tea with ${other.name}."
                    agent.emoteIcon = "🍵"
                    other.stress = (other.stress - 5).coerceAtLeast(0)
                    other.mood = (other.mood + 5).coerceAtMost(100)
                }
                break
            }
        }
    }

    // ==========================================
    // 2. ENEMY & BOSS COMBAT AI SYSTEM
    // ==========================================

    enum class BossArchetype {
        PRIMORDIAL_BEAST_KING,
        CORRUPTED_DEMON_LORD,
        ANCIENT_IMMORTAL_REMNANT,
        CELESTIAL_TRIBULATION_DRAGON
    }

    enum class CombatStance {
        AGGRESSIVE,
        BALANCED,
        DEFENSIVE
    }

    data class BossCombatState(
        val name: String,
        val archetype: BossArchetype,
        val maxHp: Int,
        var currentHp: Int,
        val element: Int, // 0=None, 1=Metal, 2=Wood, 3=Water, 4=Fire, 5=Earth
        var phase: Int = 1, // 1=Normal, 2=Enraged/Domain, 3=Desperation/Nirvana
        var shieldHp: Int = 0,
        var enrageMultiplier: Float = 1.0f,
        var turnsElapsed: Int = 0,
        val skillsCooldown: MutableMap<String, Int> = mutableMapOf()
    )

    data class TurnDecision(
        val skillName: String,
        val damage: Int,
        val isCritical: Boolean,
        val logMessage: String,
        val appliedEffect: String? = null,
        val shieldGained: Int = 0
    )

    fun createBossForRealm(difficulty: Int): BossCombatState {
        return when (difficulty) {
            1 -> BossCombatState(
                name = "Ancient Venom Mist Python",
                archetype = BossArchetype.PRIMORDIAL_BEAST_KING,
                maxHp = 220,
                currentHp = 220,
                element = GameConfig.ELEM_WOOD
            )
            2 -> BossCombatState(
                name = "Blood Asura Patriarch",
                archetype = BossArchetype.CORRUPTED_DEMON_LORD,
                maxHp = 450,
                currentHp = 450,
                element = GameConfig.ELEM_FIRE
            )
            3 -> BossCombatState(
                name = "Corrupted Celestial Sword Sovereign",
                archetype = BossArchetype.ANCIENT_IMMORTAL_REMNANT,
                maxHp = 800,
                currentHp = 800,
                element = GameConfig.ELEM_METAL
            )
            else -> BossCombatState(
                name = "Nine-Headed Heavenly Void Dragon",
                archetype = BossArchetype.CELESTIAL_TRIBULATION_DRAGON,
                maxHp = 1350,
                currentHp = 1350,
                element = GameConfig.ELEM_EARTH
            )
        }
    }

    /**
     * Advanced tactical AI evaluation for Enemy/Boss turns.
     */
    fun evaluateEnemyTurn(
        boss: BossCombatState,
        discipleHp: Int,
        discipleMaxHp: Int,
        discipleElement: Int,
        discipleStance: CombatStance = CombatStance.BALANCED
    ): TurnDecision {
        boss.turnsElapsed++

        // Decrement cooldowns
        boss.skillsCooldown.keys.forEach { skill ->
            val cd = boss.skillsCooldown[skill] ?: 0
            if (cd > 0) boss.skillsCooldown[skill] = cd - 1
        }

        // Update Boss Phase based on HP percentage
        val hpPct = (boss.currentHp.toFloat() / boss.maxHp.toFloat())
        if (hpPct <= 0.25f && boss.phase < 3) {
            boss.phase = 3
            boss.enrageMultiplier = 1.6f
            boss.shieldHp += (boss.maxHp * 0.15f).toInt()
        } else if (hpPct <= 0.60f && boss.phase < 2) {
            boss.phase = 2
            boss.enrageMultiplier = 1.25f
        }

        // Calculate elemental counter advantage
        val elementAdvantage = calculateElementMultiplier(boss.element, discipleElement)

        // Stance defense reduction
        val stanceDmgMod = when (discipleStance) {
            CombatStance.DEFENSIVE -> 0.7f
            CombatStance.AGGRESSIVE -> 1.25f
            CombatStance.BALANCED -> 1.0f
        }

        val baseAtk = (boss.maxHp / 10f) * boss.enrageMultiplier * elementAdvantage * stanceDmgMod
        val isCrit = RNG.nextFloat() < (if (boss.phase == 3) 0.35f else 0.15f)
        val critMod = if (isCrit) 1.5f else 1.0f

        // Skill Decision Tree
        val decision = when (boss.archetype) {
            BossArchetype.PRIMORDIAL_BEAST_KING -> {
                if (boss.phase == 3 && (boss.skillsCooldown["PrimordialRavage"] ?: 0) == 0) {
                    boss.skillsCooldown["PrimordialRavage"] = 3
                    val dmg = (baseAtk * 1.8f * critMod).toInt()
                    TurnDecision(
                        skillName = "Primordial Beast King Ravage",
                        damage = dmg,
                        isCritical = isCrit,
                        logMessage = "🐉 ${boss.name} unleashed [Primordial Beast Ravage]! Shattering earth and flesh for $dmg damage!",
                        appliedEffect = "Bleed"
                    )
                } else if (boss.phase >= 2 && (boss.skillsCooldown["ToxicBreath"] ?: 0) == 0) {
                    boss.skillsCooldown["ToxicBreath"] = 2
                    val dmg = (baseAtk * 1.3f * critMod).toInt()
                    TurnDecision(
                        skillName = "Venom Cloud Breath",
                        damage = dmg,
                        isCritical = isCrit,
                        logMessage = "💨 ${boss.name} spewed corrosive [Venom Cloud Breath] for $dmg damage!",
                        appliedEffect = "Poison"
                    )
                } else {
                    val dmg = (baseAtk * RNG.nextFloat(0.85f, 1.15f) * critMod).toInt()
                    TurnDecision(
                        skillName = "Feral Claw Sweep",
                        damage = dmg,
                        isCritical = isCrit,
                        logMessage = "🐾 ${boss.name} lashed out with [Feral Claw Sweep] dealing $dmg damage!"
                    )
                }
            }

            BossArchetype.CORRUPTED_DEMON_LORD -> {
                if (boss.phase == 3 && (boss.skillsCooldown["BloodSacrifice"] ?: 0) == 0) {
                    boss.skillsCooldown["BloodSacrifice"] = 4
                    val dmg = (baseAtk * 2.0f * critMod).toInt()
                    val healAmt = (dmg * 0.4f).toInt()
                    boss.currentHp = (boss.currentHp + healAmt).coerceAtMost(boss.maxHp)
                    TurnDecision(
                        skillName = "Asura Blood Soul Sacrifice",
                        damage = dmg,
                        isCritical = isCrit,
                        logMessage = "🩸 ${boss.name} invoked [Asura Blood Soul Sacrifice]! Inflicted $dmg damage and absorbed $healAmt HP!",
                        appliedEffect = "Lifesteal"
                    )
                } else if ((boss.skillsCooldown["QiDrain"] ?: 0) == 0) {
                    boss.skillsCooldown["QiDrain"] = 2
                    val dmg = (baseAtk * 1.25f * critMod).toInt()
                    TurnDecision(
                        skillName = "Corrupted Qi Siphon",
                        damage = dmg,
                        isCritical = isCrit,
                        logMessage = "🌑 ${boss.name} used [Corrupted Qi Siphon], draining meridians for $dmg damage!",
                        appliedEffect = "Qi Drain"
                    )
                } else {
                    val dmg = (baseAtk * RNG.nextFloat(0.9f, 1.2f) * critMod).toInt()
                    TurnDecision(
                        skillName = "Demon Blade Thrust",
                        damage = dmg,
                        isCritical = isCrit,
                        logMessage = "🗡️ ${boss.name} cleaved with [Demon Blade Thrust] for $dmg damage!"
                    )
                }
            }

            BossArchetype.ANCIENT_IMMORTAL_REMNANT -> {
                if (boss.phase == 3 && (boss.skillsCooldown["TribulationLightning"] ?: 0) == 0) {
                    boss.skillsCooldown["TribulationLightning"] = 3
                    val dmg = (baseAtk * 2.2f * critMod).toInt()
                    TurnDecision(
                        skillName = "Nine Heavens Tribulation Lightning",
                        damage = dmg,
                        isCritical = isCrit,
                        logMessage = "⚡ ${boss.name} summoned [Nine Heavens Tribulation Lightning]! Celestial thunder struck for $dmg damage!",
                        appliedEffect = "Paralyze"
                    )
                } else if (boss.shieldHp <= 0 && (boss.skillsCooldown["ArrayBarrier"] ?: 0) == 0) {
                    boss.skillsCooldown["ArrayBarrier"] = 4
                    val shield = (boss.maxHp * 0.20f).toInt()
                    boss.shieldHp = shield
                    TurnDecision(
                        skillName = "Five Elements Dao Barrier",
                        damage = 0,
                        isCritical = false,
                        logMessage = "🛡️ ${boss.name} formed [Five Elements Dao Barrier], manifesting a $shield HP celestial shield!",
                        shieldGained = shield
                    )
                } else {
                    val dmg = (baseAtk * RNG.nextFloat(1.0f, 1.3f) * critMod).toInt()
                    TurnDecision(
                        skillName = "Immortal Flying Sword Array",
                        damage = dmg,
                        isCritical = isCrit,
                        logMessage = "⚔️ ${boss.name} commanded [Immortal Flying Sword Array], piercing for $dmg damage!"
                    )
                }
            }

            BossArchetype.CELESTIAL_TRIBULATION_DRAGON -> {
                if (boss.phase >= 2 && (boss.skillsCooldown["CosmicRoar"] ?: 0) == 0) {
                    boss.skillsCooldown["CosmicRoar"] = 3
                    val dmg = (baseAtk * 1.9f * critMod).toInt()
                    TurnDecision(
                        skillName = "Heaven Sundering Void Roar",
                        damage = dmg,
                        isCritical = isCrit,
                        logMessage = "🌌 ${boss.name} roared with [Heaven Sundering Void Roar], blasting reality for $dmg damage!",
                        appliedEffect = "Stun"
                    )
                } else {
                    val dmg = (baseAtk * RNG.nextFloat(0.95f, 1.25f) * critMod).toInt()
                    TurnDecision(
                        skillName = "Draconic Void Breath",
                        damage = dmg,
                        isCritical = isCrit,
                        logMessage = "🔥 ${boss.name} engulfed the battlefield in [Draconic Void Breath] for $dmg damage!"
                    )
                }
            }
        }

        return decision
    }

    /**
     * Five Elements Generation & Overcoming Cycle:
     * Metal overcomes Wood, Wood overcomes Earth, Earth overcomes Water, Water overcomes Fire, Fire overcomes Metal.
     */
    private fun calculateElementMultiplier(attackerElem: Int, defenderElem: Int): Float {
        if (attackerElem == GameConfig.ELEM_NONE || defenderElem == GameConfig.ELEM_NONE) return 1.0f

        val overcomes = when (attackerElem) {
            GameConfig.ELEM_METAL -> defenderElem == GameConfig.ELEM_WOOD
            GameConfig.ELEM_WOOD -> defenderElem == GameConfig.ELEM_EARTH
            GameConfig.ELEM_EARTH -> defenderElem == GameConfig.ELEM_WATER
            GameConfig.ELEM_WATER -> defenderElem == GameConfig.ELEM_FIRE
            GameConfig.ELEM_FIRE -> defenderElem == GameConfig.ELEM_METAL
            else -> false
        }

        val countered = when (attackerElem) {
            GameConfig.ELEM_WOOD -> defenderElem == GameConfig.ELEM_METAL
            GameConfig.ELEM_EARTH -> defenderElem == GameConfig.ELEM_WOOD
            GameConfig.ELEM_WATER -> defenderElem == GameConfig.ELEM_EARTH
            GameConfig.ELEM_FIRE -> defenderElem == GameConfig.ELEM_WATER
            GameConfig.ELEM_METAL -> defenderElem == GameConfig.ELEM_FIRE
            else -> false
        }

        return when {
            overcomes -> 1.30f // Strong against
            countered -> 0.75f // Weak against
            else -> 1.0f
        }
    }
}
