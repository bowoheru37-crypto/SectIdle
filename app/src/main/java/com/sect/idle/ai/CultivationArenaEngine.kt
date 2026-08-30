package com.sect.idle.ai

import com.sect.idle.core.GameConfig
import com.sect.idle.models.Disciple
import com.sect.idle.systems.RNG
import kotlin.math.max
import kotlin.math.min

/**
 * CultivationArenaEngine - Comprehensive Martial Arts Tournament, WWE / MMA Octagon,
 * and Donghua Xianxia Championship Combat Engine.
 *
 * Implements:
 * 1. Tale of the Tape (Fighter profiles, archetypes, signature moves, records).
 * 2. Play-by-Play & Color Announcer commentary engine (Dao Master Tianji & Elder Kuang).
 * 3. Ring Mechanics (Crowd Hype Meter, Momentum, Spirit Ropes, Bagua Cage, Grapple Slams).
 * 4. Move Resolution (Strikes, Suplexes, Qi Submissions, Rope Dives, Taunts, and Celestial Finishers).
 * 5. Elder Referee system (Pin counts, Submission checks, Rope breaks, KO declarations).
 */
object CultivationArenaEngine {

    enum class MatchStipulation(val displayName: String, val description: String, val icon: String) {
        GRAND_DAO_CHAMPIONSHIP(
            "Grand Dao Championship Bout",
            "1-on-1 Classical Celestial Ring. Standard rules, pinfall or knockout to win the Sect Belt.",
            "🏆"
        ),
        BAGUA_STEEL_CAGE(
            "Eight Trigrams Dao Cage",
            "Enclosed in 10,000-volt Spirit Thunder Cage. Wall bounces and heightened slam damage!",
            "⚡"
        ),
        DEMON_ROYAL_RUMBLE(
            "Demonic Gauntlet Rumble",
            "Continuous waves of demonic martial artists entering the ring over the top ropes.",
            "🔥"
        ),
        TRIBULATION_LADDER(
            "Celestial Ladder Match",
            "Climb the Nine Heavens Pillar to unseal and retrieve the Supreme Immortality Pill.",
            "🪜"
        ),
        NO_HOLDS_BARRED(
            "Forbidden Sect Grudge Match",
            "No referee count-outs. Flying spiritual talismans, spirit chairs, and demonic pills allowed!",
            "💀"
        )
    }

    enum class FighterArchetype(val title: String, val perkDesc: String) {
        STRIKER("Martial Striker (Pendekar Pukulan)", "High crit rate & rapid palm/kick strike combos"),
        DAO_GRAPPLER("Dao Grappler (Ahli Bantingan)", "Devastating suplex slams & high defense"),
        SWORD_VIRTUOSO("Sword Virtuoso (Master Pedang)", "High agility & aerial rope rebounds"),
        SUBMISSION_MASTER("Submission Master (Pakar Kuncian)", "Continuous Qi joint-locks & stamina drain")
    }

    data class FighterProfile(
        val name: String,
        val realmName: String,
        val nickname: String,
        val archetype: FighterArchetype,
        val wins: Int,
        val losses: Int,
        val knockouts: Int,
        val signatureMove: String,
        val finisherMove: String,
        val entranceAura: String,
        val titleBelt: String? = null,
        val atk: Int = 45,
        val def: Int = 25,
        val spd: Int = 18
    )

    enum class ArenaActionType {
        MARTIAL_STRIKE,     // Palm / Punch / Kick combo
        HEAVENLY_GRAPPLE,   // Suplex / Mountain Slam
        BAGUA_SUBMISSION,   // Joint-lock / Qi choke
        ROPE_REBOUND,       // Bounce off Dao ropes / Flying dropkick
        TAUNT_CROWD,        // Show off aura to pump up crowd hype
        CELESTIAL_FINISHER  // 100% Hype Ultimate Knockout
    }

    data class ArenaActionResult(
        val moveName: String,
        val moveType: ArenaActionType,
        val attackerName: String,
        val defenderName: String,
        val damage: Int,
        val isCritical: Boolean,
        val hypeGain: Int,
        val momentumDelta: Int,
        val playByPlayCommentary: String,
        val colorCommentary: String,
        val refereeCall: String,
        val visualAnimation: String, // STRIKE, SUPLEX, SUBMISSION, ROPE_BOUNCE, FINISHER, TAUNT
        val hitSparks: List<Pair<Float, Float>> = emptyList()
    )

    // ========================================================================
    // FIGHTER PROFILE GENERATION
    // ========================================================================

    fun createDiscipleProfile(disciple: Disciple?): FighterProfile {
        if (disciple == null) {
            return FighterProfile(
                name = "Unnamed Disciple",
                realmName = "Qi Condensation",
                nickname = "The Unknown Cultivator",
                archetype = FighterArchetype.STRIKER,
                wins = 5,
                losses = 1,
                knockouts = 4,
                signatureMove = "Eight Trigrams Palm",
                finisherMove = "Nine Heavens Dragon Slam",
                entranceAura = "Ascends amidst emerald lotus petals",
                titleBelt = "Sect Contender"
            )
        }

        val archetype = when (disciple.personality) {
            GameConfig.PER_FIGHTER -> FighterArchetype.DAO_GRAPPLER
            GameConfig.PER_ARROGANT -> FighterArchetype.STRIKER
            GameConfig.PER_MYSTERIOUS -> FighterArchetype.SUBMISSION_MASTER
            else -> FighterArchetype.SWORD_VIRTUOSO
        }

        val nicknames = listOf(
            "The Jade Dragon", "The Iron Fist Daoist", "The Azure Gale",
            "The Unbroken Lotus", "The Sword Sovereign", "The Mountain Crusher"
        )
        val nickname = nicknames[disciple.name.hashCode().mod(nicknames.size)]

        val signatures = listOf(
            "Spiritual Palm Cyclone", "Northern Fist Impact", "Mountain-Tossing Suplex",
            "Void Guillotine Choke", "Azure Lotus Spinning Kick"
        )
        val finishers = listOf(
            "Nine-Dragon Heavenly Piledriver", "Grand Sun Phoenix Dropkick",
            "Reincarnation Knockout (RKO)", "Sect-Shattering Celestial Slam",
            "Sublime Dantian Shatter Tombstone"
        )

        return FighterProfile(
            name = disciple.name,
            realmName = GameConfig.getRealmName(disciple.realm),
            nickname = nickname,
            archetype = archetype,
            wins = (disciple.realm * 4) + RNG.nextInt(3, 8),
            losses = RNG.nextInt(0, 3),
            knockouts = (disciple.realm * 3) + RNG.nextInt(2, 6),
            signatureMove = signatures[disciple.name.hashCode().mod(signatures.size)],
            finisherMove = finishers[disciple.name.hashCode().mod(finishers.size)],
            entranceAura = "Descends with billowing Dao robes and crackling spirit lightning!",
            titleBelt = if (disciple.realm >= 2) "Undisputed Cloud-Mist Champion" else "Rising Sect Diamond",
            atk = disciple.atk.coerceAtLeast(35),
            def = disciple.def.coerceAtLeast(20),
            spd = disciple.spd.coerceAtLeast(15)
        )
    }

    fun createOpponentProfile(difficulty: Int, customName: String? = null): FighterProfile {
        return when (difficulty) {
            1 -> FighterProfile(
                name = customName ?: "Mist Valley Viper Fang",
                realmName = "Qi Condensation Peak",
                nickname = "The Green Venom Striker",
                archetype = FighterArchetype.STRIKER,
                wins = 12,
                losses = 4,
                knockouts = 9,
                signatureMove = "Toxic Cobra Jab Combo",
                finisherMove = "Venomous Spiral Neckbreaker",
                entranceAura = "Steps into the ring shrouded in eerie emerald mist and serpent whispers.",
                titleBelt = "Outer Mountain Belt",
                atk = 45,
                def = 25,
                spd = 22
            )
            2 -> FighterProfile(
                name = customName ?: "Blood Asura Berserker",
                realmName = "Foundation Establishment",
                nickname = "The Demonic Powerhouse",
                archetype = FighterArchetype.DAO_GRAPPLER,
                wins = 24,
                losses = 2,
                knockouts = 21,
                signatureMove = "Hellfire Powerbomb",
                finisherMove = "Blood Soul Chokeslam from Mount Asura",
                entranceAura = "Explodes with crimson demonic flames as war drums shake the arena!",
                titleBelt = "Inter-Sect Heavyweight Championship",
                atk = 75,
                def = 50,
                spd = 35
            )
            3 -> FighterProfile(
                name = customName ?: "Sword Sovereign Xie Tian",
                realmName = "Golden Core Realm",
                nickname = "The Apex Swordsman",
                archetype = FighterArchetype.SWORD_VIRTUOSO,
                wins = 38,
                losses = 1,
                knockouts = 34,
                signatureMove = "Thousand Blade Springboard Dive",
                finisherMove = "Sword-Intent Tombstone Piledriver",
                entranceAura = "Rides a barrage of a thousand flying celestial golden swords into the octagon ring!",
                titleBelt = "Grand Celestial World Belt",
                atk = 120,
                def = 85,
                spd = 65
            )
            else -> FighterProfile(
                name = customName ?: "Primordial Void Dragon God",
                realmName = "Nascent Soul / Tribulation",
                nickname = "The Ancient World Destroyer",
                archetype = FighterArchetype.SUBMISSION_MASTER,
                wins = 99,
                losses = 0,
                knockouts = 95,
                signatureMove = "Black Hole Spinebuster",
                finisherMove = "Nine Heavens Void Lock of Extinction",
                entranceAura = "Tears open space and time; dark lightning illuminates the thunderous crowd!",
                titleBelt = "Undisputed Universal Dao Emperor Title",
                atk = 220,
                def = 160,
                spd = 110
            )
        }
    }

    // ========================================================================
    // MOVE EXECUTION & RESOLUTION LOGIC
    // ========================================================================

    fun executePlayerAction(
        action: ArenaActionType,
        attacker: FighterProfile,
        defender: FighterProfile,
        attackerHp: Int,
        defenderHp: Int,
        currentHype: Int,
        currentMomentum: Int,
        stipulation: MatchStipulation
    ): ArenaActionResult {
        val isCrit = (action == ArenaActionType.CELESTIAL_FINISHER) || (RNG.nextFloat() < 0.25f)
        val critMod = if (isCrit) 1.6f else 1.0f

        var damage: Int
        var hypeGain: Int
        var momentumDelta: Int
        var moveName: String
        var playByPlay: String
        var colorComm: String
        var refCall: String
        var visualAnim: String

        when (action) {
            ArenaActionType.MARTIAL_STRIKE -> {
                moveName = when (attacker.archetype) {
                    FighterArchetype.STRIKER -> "Dragon Flurry Palm Strike Combo"
                    FighterArchetype.DAO_GRAPPLER -> "Heavy Bicep Lariat & Chest Slap"
                    FighterArchetype.SWORD_VIRTUOSO -> "Piercing Sword-Hand Thrust"
                    FighterArchetype.SUBMISSION_MASTER -> "Precision Meridian Elbow Jab"
                }
                damage = (RNG.nextInt(25, 45) * critMod).toInt()
                hypeGain = if (isCrit) 18 else 10
                momentumDelta = if (isCrit) 25 else 12
                visualAnim = "STRIKE"

                playByPlay = "🎙️ Master Tianji: \"${attacker.name} charges in! Delivers a blistering $moveName that rocks ${defender.name}!\""
                colorComm = if (isCrit) {
                    "💥 Elder Kuang: \"SWEET HEAVENS! THAT JAB CRACKED THE SOUND BARRIER! Listen to the audience roaring!\""
                } else {
                    "🎙️ Elder Kuang: \"Crisp martial form! Keeping the pressure on the perimeter!\""
                }
                refCall = if (isCrit) "Elder Referee: \"WATCH THE CLEAN CONTACT!\"" else "Elder Referee: \"FIGHT ON!\""
            }

            ArenaActionType.HEAVENLY_GRAPPLE -> {
                moveName = when (attacker.archetype) {
                    FighterArchetype.DAO_GRAPPLER -> "Mount Tai German Suplex over the Dao Ring!"
                    FighterArchetype.STRIKER -> "Spiritual Belly-to-Belly Overhead Toss"
                    FighterArchetype.SWORD_VIRTUOSO -> "Hurricanrana Sword Spin Throw"
                    FighterArchetype.SUBMISSION_MASTER -> "Arm-Drag Spinebuster onto the Bagua Mat"
                }
                damage = (RNG.nextInt(40, 70) * critMod).toInt()
                if (stipulation == MatchStipulation.BAGUA_STEEL_CAGE) damage += 15 // Cage bonus!
                hypeGain = 20
                momentumDelta = 30
                visualAnim = "SUPLEX"

                playByPlay = "🎙️ Master Tianji: \"OH MY DAO! ${attacker.name} lifts ${defender.name} high in the air... AND SLAMS THEM DOWN with $moveName!\""
                colorComm = "💥 Elder Kuang: \"BAH GAWD ALMIGHTY! HE BROKE HIM IN HALF! The spiritual ring mat shook to its core!\""
                refCall = "Elder Referee: \"1... 2... NO! ${defender.name} kicks out at two!\""
            }

            ArenaActionType.BAGUA_SUBMISSION -> {
                moveName = when (attacker.archetype) {
                    FighterArchetype.SUBMISSION_MASTER -> "Five-Elements Dantian Lock & Guillotine Choke"
                    FighterArchetype.STRIKER -> "Iron-Grip Dragon Sleeper"
                    FighterArchetype.DAO_GRAPPLER -> "Boston Crab of the Tortoise Domain"
                    FighterArchetype.SWORD_VIRTUOSO -> "Cross-Armbreaker of the Flying Phoenix"
                }
                damage = (RNG.nextInt(30, 55) * critMod).toInt()
                hypeGain = 15
                momentumDelta = 20
                visualAnim = "SUBMISSION"

                playByPlay = "🎙️ Master Tianji: \"${attacker.name} transitions smoothly into $moveName! The pressure on the meridians is unbearable!\""
                colorComm = "💥 Elder Kuang: \"Look at the agony on ${defender.name}'s face! WILL HE TAP?! WILL HE SUBMIT TO THE IMMORTAL SECT?!\""
                refCall = "Elder Referee: \"DO YOU SUBMIT?! TAP OR PASS OUT!\""
            }

            ArenaActionType.ROPE_REBOUND -> {
                moveName = "Dao Spirit Rope Springboard Flying Dropkick!"
                damage = (RNG.nextInt(35, 60) * critMod).toInt()
                hypeGain = 22
                momentumDelta = 25
                visualAnim = "ROPE_BOUNCE"

                playByPlay = "🎙️ Master Tianji: \"${attacker.name} sprints, rebounds off the Dao Spirit Ropes with explosive velocity... AIRBORNE ATTACK!\""
                colorComm = "💥 Elder Kuang: \"HIGH FLYING DONGHUA ACTION! What athletic cultivation mastery! The crowd is on their feet!\""
                refCall = "Elder Referee: \"BEAUTIFUL REBOUND! KEEP IT IN THE RING!\""
            }

            ArenaActionType.TAUNT_CROWD -> {
                moveName = "Grand Dao Flex & Crowd Aura Flare 🌟"
                damage = 0
                hypeGain = 35
                momentumDelta = 20
                visualAnim = "TAUNT"

                playByPlay = "🎙️ Master Tianji: \"${attacker.name} climbs the turnbuckle and unleashes a blinding burst of celestial Qi to hypes up the entire stadium!\""
                colorComm = "💥 Elder Kuang: \"LISTEN TO THAT DEAFENING ROAR! The entire sect is chanting their name! HYPE IS AT MAXIMUM!\""
                refCall = "Elder Referee: \"RESPECT THE CLOCK! ENGAGE YOUR OPPONENT!\""
            }

            ArenaActionType.CELESTIAL_FINISHER -> {
                moveName = attacker.finisherMove
                damage = RNG.nextInt(110, 180)
                hypeGain = 0 // Expended
                momentumDelta = 60
                visualAnim = "FINISHER"

                playByPlay = "🎙️ Master Tianji: \"THIS IS IT! ${attacker.name} sets up ${defender.name}... EXECUTES THE ${attacker.finisherMove.uppercase()}!\""
                colorComm = "⚡ Elder Kuang: \"GOOD HEAVENS AND EARTH! UNBELIEVABLE! THAT HIT WITH THE FORCE OF A THOUSAND CELESTIAL TRIBULATIONS! IT'S OVER!\""
                refCall = "Elder Referee: \"1... 2... 3! DING DING DING! WE HAVE A WINNER!\""
            }
        }

        return ArenaActionResult(
            moveName = moveName,
            moveType = action,
            attackerName = attacker.name,
            defenderName = defender.name,
            damage = damage,
            isCritical = isCrit,
            hypeGain = hypeGain,
            momentumDelta = momentumDelta,
            playByPlayCommentary = playByPlay,
            colorCommentary = colorComm,
            refereeCall = refCall,
            visualAnimation = visualAnim
        )
    }

    fun executeOpponentTurn(
        opponent: FighterProfile,
        disciple: FighterProfile,
        opponentHp: Int,
        discipleHp: Int,
        opponentHype: Int,
        stipulation: MatchStipulation
    ): ArenaActionResult {
        // AI selects action based on archetype and situation
        val action = when {
            opponentHype >= 100 -> ArenaActionType.CELESTIAL_FINISHER
            RNG.nextFloat() < 0.35f -> ArenaActionType.HEAVENLY_GRAPPLE
            RNG.nextFloat() < 0.30f -> ArenaActionType.BAGUA_SUBMISSION
            RNG.nextFloat() < 0.20f -> ArenaActionType.ROPE_REBOUND
            else -> ArenaActionType.MARTIAL_STRIKE
        }

        val isCrit = (action == ArenaActionType.CELESTIAL_FINISHER) || (RNG.nextFloat() < 0.20f)
        val critMod = if (isCrit) 1.5f else 1.0f

        val (moveName, damage, visualAnim) = when (action) {
            ArenaActionType.CELESTIAL_FINISHER -> Triple(opponent.finisherMove, RNG.nextInt(90, 150), "FINISHER")
            ArenaActionType.HEAVENLY_GRAPPLE -> Triple("Thunderous Belly-to-Back Mat Suplex", (RNG.nextInt(35, 60) * critMod).toInt(), "SUPLEX")
            ArenaActionType.BAGUA_SUBMISSION -> Triple("Cruel Bone-Cracking Dantian Hold", (RNG.nextInt(25, 45) * critMod).toInt(), "SUBMISSION")
            ArenaActionType.ROPE_REBOUND -> Triple("Demonic Rebound Clothesline", (RNG.nextInt(30, 50) * critMod).toInt(), "ROPE_BOUNCE")
            else -> Triple(opponent.signatureMove, (RNG.nextInt(20, 40) * critMod).toInt(), "STRIKE")
        }

        val playByPlay = "🎙️ Master Tianji: \"${opponent.name} retaliates with vicious momentum! Hits ${disciple.name} with $moveName!\""
        val colorComm = if (isCrit) {
            "💥 Elder Kuang: \"Ouch! That looked like it cracked ribs and shattered spiritual shields! ${disciple.name} is in deep trouble!\""
        } else {
            "🎙️ Elder Kuang: \"Relentless offense from the challenger! He knows what is at stake!\""
        }
        val refCall = if (action == ArenaActionType.BAGUA_SUBMISSION) "Elder Referee: \"CHECK THE ROPES! BREAK AT FIVE!\"" else "Elder Referee: \"CLEAN HIT! CONTINUE!\""

        return ArenaActionResult(
            moveName = moveName,
            moveType = action,
            attackerName = opponent.name,
            defenderName = disciple.name,
            damage = damage,
            isCritical = isCrit,
            hypeGain = if (action == ArenaActionType.CELESTIAL_FINISHER) 0 else 15,
            momentumDelta = -20,
            playByPlayCommentary = playByPlay,
            colorCommentary = colorComm,
            refereeCall = refCall,
            visualAnimation = visualAnim
        )
    }

    // Dynamic Crowd Chants
    fun getCrowdChant(hype: Int, discipleName: String): String {
        return when {
            hype >= 85 -> "📣 [AUDIENCE CHANT] \"${discipleName.uppercase()}! ${discipleName.uppercase()}! FINISH HIM! BREAK THROUGH!\" ⚡"
            hype >= 60 -> "📣 [AUDIENCE CHANT] \"LET'S GO CLOUD-MIST! *CLAP CLAP CLAP*!\" 🔥"
            hype >= 30 -> "📣 [AUDIENCE CHANT] \"SLAP THE MAT! SHOW NO MERCY!\" 🥋"
            else -> "📣 [AUDIENCE CHANT] \"DEFENSE! DEFENSE! FIGHT FOR THE SECT!\" 🛡️"
        }
    }
}
