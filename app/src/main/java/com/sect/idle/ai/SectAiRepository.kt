package com.sect.idle.ai

import com.example.BuildConfig
import com.sect.idle.systems.RNG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Data structures for Gemini AI-generated Xianxia events, wisdom, and dynamic quests.
 */
data class GrandmasterWisdom(
    val title: String,
    val text: String,
    val fortuneProphecy: String,
    val recommendedAction: String,
    val sectBlessing: String
)

data class EventChoice(
    val text: String,
    val description: String,
    val requiredQi: Long = 0,
    val requiredStones: Long = 0,
    val karmaImpact: Long = 0,
    val successRate: Float = 0.8f,
    val rewardSummary: String = "",
    val outcomeSuccess: String = "",
    val outcomeFailure: String = ""
)

enum class EventCategory {
    CELESTIAL_PHENOMENON,
    DEMONIC_INCURSION,
    DAO_EPIPHANY,
    TRAVELING_IMMORTAL,
    ANCIENT_RUIN,
    SECT_DILEMMA
}

data class SectRandomEvent(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val category: EventCategory,
    val description: String,
    val choices: List<EventChoice>
)

data class DynamicSectQuest(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val targetName: String,
    val difficulty: Int,
    val rewardQi: Long,
    val rewardStones: Long,
    val rewardKarma: Long,
    val rewardReputation: Long
)

data class TribulationTrial(
    val title: String,
    val narrative: String,
    val trialType: String, // Heart Demon, Heavenly Lightning, Five Element Tribulation
    val advice: String,
    val bonusSuccessChance: Float
)

/**
 * SectAiRepository - Repository providing Gemini AI generation for random heavenly events,
 * grandmaster wisdom, disciple tribulation trials, and dynamic celestial quests.
 */
class SectAiRepository(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
) : ISectAiRepository {
    companion object {
        private const val MODEL_NAME = "gemini-3.5-flash"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

        private const val SYSTEM_INSTRUCTION = """
You are the Grandmaster Ancient Ancestor of an Eastern Xianxia Cultivation Sect.
Generate immersive Eastern fantasy cultivation lore, mystical prophecies, sect choices, and heavenly tribulations.
Output strictly formatted JSON as requested, with rich Daoist atmosphere and vivid descriptions.
"""
    }

    private fun getApiKey(): String {
        return try {
            val key = BuildConfig.GEMINI_API_KEY
            if (key.isNullOrBlank() || key == "YOUR_API_KEY" || key == "MY_GEMINI_API_KEY") "" else key
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Request Grandmaster Ancestor wisdom with full sect state context.
     */
    override suspend fun getGrandmasterWisdom(
        sectContext: String,
        userQuery: String?
    ): GrandmasterWisdom = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            return@withContext generateFallbackWisdom(sectContext, userQuery)
        }

        val prompt = buildString {
            append("Sect Status: $sectContext\n")
            if (!userQuery.isNullOrBlank()) {
                append("Sect Master asks: \"$userQuery\"\n")
            } else {
                append("The Sect Master meditates at the Ancestral Altar seeking guidance on how to guide the disciples and ascend the sect.\n")
            }
            append("\nProvide grandmaster wisdom and return JSON with keys: title, text, fortuneProphecy, recommendedAction, sectBlessing.")
        }

        try {
            val json = callGeminiJson(apiKey, prompt)
            val title = json.optString("title", "Heavenly Dao Revelation")
            val text = json.optString("text", "The Dao flows like water; when the Dantian is clear, celestial energy condensates effortlessly.")
            val fortune = json.optString("fortuneProphecy", " Auspicious Purple Qi gathers from the East; great fortune approaches.")
            val action = json.optString("recommendedAction", "Focus disciples on spirit vein cultivation and herbal alchemy.")
            val blessing = json.optString("sectBlessing", "All disciples gain +10% cultivation speed for this season.")

            GrandmasterWisdom(title, text, fortune, action, blessing)
        } catch (e: Exception) {
            generateFallbackWisdom(sectContext, userQuery)
        }
    }

    /**
     * Generate a dynamic Random Heavenly Event with interactive choices.
     */
    override suspend fun generateRandomSectEvent(sectContext: String): SectRandomEvent = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            return@withContext generateFallbackEvent(sectContext)
        }

        val prompt = """
Current Sect Context: $sectContext

Generate a random dramatic Xianxia sect event (e.g., celestial star fall, beast tide near the gate, wandering rogue immortal offering forbidden manual, disciple uncovering ancient cavern, or moral tribulation).
Return JSON:
{
  "title": "Title of Event",
  "category": "CELESTIAL_PHENOMENON" | "DEMONIC_INCURSION" | "DAO_EPIPHANY" | "TRAVELING_IMMORTAL" | "ANCIENT_RUIN" | "SECT_DILEMMA",
  "description": "2-3 sentences describing the happening with high Xianxia flavor",
  "choices": [
    {
      "text": "Short choice 1",
      "description": "What this choice entails",
      "requiredQi": 100,
      "requiredStones": 50,
      "karmaImpact": 10,
      "successRate": 0.85,
      "rewardSummary": "+250 Qi, +100 Spirit Stones",
      "outcomeSuccess": "Vivid success outcome description",
      "outcomeFailure": "Vivid failure outcome description"
    },
    {
      "text": "Short choice 2",
      "description": "What this choice entails",
      "requiredQi": 0,
      "requiredStones": 0,
      "karmaImpact": -5,
      "successRate": 0.95,
      "rewardSummary": "+50 Karma, +1 Low Grade Pill",
      "outcomeSuccess": "Vivid success outcome description",
      "outcomeFailure": "Vivid failure outcome description"
    }
  ]
}
"""

        try {
            val json = callGeminiJson(apiKey, prompt)
            val title = json.optString("title", "Mystic Cloud Phenomenon")
            val catStr = json.optString("category", "CELESTIAL_PHENOMENON")
            val category = try {
                EventCategory.valueOf(catStr)
            } catch (e: Exception) {
                EventCategory.CELESTIAL_PHENOMENON
            }
            val desc = json.optString("description", "A vibrant pillar of seven-colored light pierces the clouds above the sect's mountain peak.")
            val choicesArray = json.optJSONArray("choices")
            val choices = mutableListOf<EventChoice>()

            if (choicesArray != null && choicesArray.length() > 0) {
                for (i in 0 until choicesArray.length()) {
                    val c = choicesArray.getJSONObject(i)
                    choices.add(
                        EventChoice(
                            text = c.optString("text", "Investigate with Reverence"),
                            description = c.optString("description", "Dispatch disciples to channel the spiritual energy."),
                            requiredQi = c.optLong("requiredQi", 50L),
                            requiredStones = c.optLong("requiredStones", 20L),
                            karmaImpact = c.optLong("karmaImpact", 5L),
                            successRate = c.optDouble("successRate", 0.85).toFloat(),
                            rewardSummary = c.optString("rewardSummary", "+150 Qi, +50 Stones"),
                            outcomeSuccess = c.optString("outcomeSuccess", "The disciples successfully harnessed the celestial aura!"),
                            outcomeFailure = c.optString("outcomeFailure", "The unstable Qi burst dissipated into the winds.")
                        )
                    )
                }
            } else {
                choices.addAll(getDefaultChoices())
            }

            SectRandomEvent(
                title = title,
                category = category,
                description = desc,
                choices = choices
            )
        } catch (e: Exception) {
            generateFallbackEvent(sectContext)
        }
    }

    /**
     * Generate dynamic celestial quests and sect bounties.
     */
    override suspend fun generateDynamicQuest(sectContext: String, difficulty: Int): DynamicSectQuest = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            return@withContext generateFallbackQuest(difficulty)
        }

        val prompt = """
Current Sect Context: $sectContext
Difficulty Level: $difficulty (1=Qi Condensation, 2=Foundation, 3=Golden Core, 4=Nascent Soul)

Generate a dynamic Xianxia sect expedition or bounty quest.
Return JSON:
{
  "title": "Quest Title",
  "description": "Engaging lore quest summary",
  "targetName": "Name of the target beast or rogue cultivator",
  "difficulty": $difficulty,
  "rewardQi": ${difficulty * 300},
  "rewardStones": ${difficulty * 150},
  "rewardKarma": ${difficulty * 10},
  "rewardReputation": ${difficulty * 25}
}
"""
        try {
            val json = callGeminiJson(apiKey, prompt)
            DynamicSectQuest(
                title = json.optString("title", "Subdue the Venomous Frost Python"),
                description = json.optString("description", "A mutated beast terrorizes nearby mortal villages. Cleanse the evil to uphold sect justice."),
                targetName = json.optString("targetName", "Venomous Frost Python"),
                difficulty = json.optInt("difficulty", difficulty),
                rewardQi = json.optLong("rewardQi", difficulty * 300L),
                rewardStones = json.optLong("rewardStones", difficulty * 150L),
                rewardKarma = json.optLong("rewardKarma", difficulty * 10L),
                rewardReputation = json.optLong("rewardReputation", difficulty * 25L)
            )
        } catch (e: Exception) {
            generateFallbackQuest(difficulty)
        }
    }

    override suspend fun consultTribulationTrial(discipleName: String, realm: Int, element: String): TribulationTrial = withContext(Dispatchers.IO) {
        val realmNames = arrayOf("Qi Gathering", "Foundation Establishment", "Golden Core", "Nascent Soul", "Soul Formation")
        val currentRealm = realmNames.getOrElse(realm) { "Immortal Realm" }
        TribulationTrial(
            title = "Tribulation of $currentRealm",
            narrative = "$discipleName senses the celestial heavens shaking as $element Qi surges through the meridian pathways.",
            trialType = "Nine Heavens Lightning Tribulation",
            advice = "Stabilize the Dantian with Foundation Pills and align Yin-Yang breaths.",
            bonusSuccessChance = 0.15f
        )
    }

    private fun callGeminiJson(apiKey: String, userPrompt: String): JSONObject {
        val url = "$BASE_URL/$MODEL_NAME:generateContent?key=$apiKey"

        val body = JSONObject().apply {
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().put("text", SYSTEM_INSTRUCTION))
                })
            })
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", userPrompt))
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.7)
                put("maxOutputTokens", 600)
                put("responseMimeType", "application/json")
            })
        }

        val request = Request.Builder()
            .url(url)
            .post(body.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        val resStr = response.body?.string() ?: throw IllegalStateException("Empty response")
        if (!response.isSuccessful) {
            throw IllegalStateException("API error: ${response.code} $resStr")
        }

        val resJson = JSONObject(resStr)
        val candidate = resJson.getJSONArray("candidates").getJSONObject(0)
        val content = candidate.getJSONObject("content")
        val part = content.getJSONArray("parts").getJSONObject(0)
        val rawText = part.getString("text")

        return JSONObject(rawText.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim())
    }

    // ================= FALLBACK SYSTEM =================

    private fun generateFallbackWisdom(sectContext: String, userQuery: String?): GrandmasterWisdom {
        val wisdomPool = listOf(
            GrandmasterWisdom(
                title = "Harmony of Yin and Yang",
                text = "The path of immortality lies not in brute strength, but in comprehending the cyclical nature of Heaven and Earth. Let your disciples balance strenuous cultivation with quiet contemplation.",
                fortuneProphecy = "The stars of the North Big Dipper shine brightly; a disciple will soon encounter a breakthrough.",
                recommendedAction = "Assign senior disciples to guide junior disciples in meditation.",
                sectBlessing = "Sect Spiritual Vein output increases by 15%."
            ),
            GrandmasterWisdom(
                title = "Tempering the Dao Heart",
                text = "Tribulations are not curses from Heaven, but supreme whetstones meant to sharpen the immortal sword. Do not fear failure in breakthrough; forge unyielding willpower.",
                fortuneProphecy = "A fortunate wind blows from the southern valleys; herb harvesting yields double.",
                recommendedAction = "Refine breakthrough pills in the Alchemy Chamber before attempting higher realms.",
                sectBlessing = "All disciples receive +10% tribulation success rate."
            ),
            GrandmasterWisdom(
                title = "Sect Renown and Karma",
                text = "A sect without virtue is like a tree without roots. Protecting nearby mortals and eradicating wandering demonic fiends accumulates positive Heavenly Karma.",
                fortuneProphecy = "The heavenly scales tilt in your favor; righteous deeds bring celestial rewards.",
                recommendedAction = "Send combat disciples on secret realm expeditions.",
                sectBlessing = "Gain +30 Heavenly Karma."
            )
        )
        return wisdomPool[RNG.nextInt(wisdomPool.size)]
    }

    private fun generateFallbackEvent(sectContext: String): SectRandomEvent {
        val events = listOf(
            SectRandomEvent(
                title = "Ancient Spirit Beast at Sect Gate",
                category = EventCategory.TRAVELING_IMMORTAL,
                description = "A majestic Qilin enveloped in gentle blue flames descends peacefully before your sect's mountain gate, sniffing the herbal aroma.",
                choices = listOf(
                    EventChoice(
                        text = "Offer Refined Spirit Pills",
                        description = "Feed the mythical beast with high-grade alchemical elixirs.",
                        requiredQi = 80L,
                        requiredStones = 100L,
                        karmaImpact = 25L,
                        successRate = 0.90f,
                        rewardSummary = "+400 Qi, +30 Sect Fame, Beast Blessing",
                        outcomeSuccess = "The Qilin accepts your offering with a resonant roar, leaving behind a pool of pure spiritual dew!",
                        outcomeFailure = "The beast took the pills and quietly soared back to the clouds."
                    ),
                    EventChoice(
                        text = "Bow with Reverence and Offer Incense",
                        description = "Show humility to the ancient guardian without disturbing its rest.",
                        requiredQi = 0L,
                        requiredStones = 0L,
                        karmaImpact = 10L,
                        successRate = 1.0f,
                        rewardSummary = "+150 Qi, +15 Karma",
                        outcomeSuccess = "The Qilin nods gently and grants the sect an aura of tranquility.",
                        outcomeFailure = "The beast departed into the mist."
                    )
                )
            ),
            SectRandomEvent(
                title = "Mystic Cavern in Back Mountain",
                category = EventCategory.ANCIENT_RUIN,
                description = "While gathering herbs, your disciples discovered an ancient stone cave sealed with a Five Elements Dao formation.",
                choices = listOf(
                    EventChoice(
                        text = "Channel Qi to Dispel the Formation",
                        description = "Unravel the ancient seals through deep spiritual resonance.",
                        requiredQi = 120L,
                        requiredStones = 50L,
                        karmaImpact = 5L,
                        successRate = 0.80f,
                        rewardSummary = "+350 Qi, +200 Spirit Stones, Ancient Scripture",
                        outcomeSuccess = "The formation dispels smoothly! Inside lies ancient Dao scripture remnants and glowing spirit crystals!",
                        outcomeFailure = "The array backfired slightly, consuming the channeled Qi before collapsing safely."
                    ),
                    EventChoice(
                        text = "Establish a Guard Post",
                        description = "Carefully preserve the site for future study.",
                        requiredQi = 30L,
                        requiredStones = 0L,
                        karmaImpact = 10L,
                        successRate = 0.95f,
                        rewardSummary = "+100 Qi, +20 Sect Fame",
                        outcomeSuccess = "The cavern becomes a sacred meditation ground for the sect disciples.",
                        outcomeFailure = "The cavern energy slowly faded."
                    )
                )
            ),
            SectRandomEvent(
                title = "Rogue Demonic Cultivator Sighted",
                category = EventCategory.DEMONIC_INCURSION,
                description = "A blood-robed rogue cultivator is attempting to siphon spiritual herbs from the outer valley perimeter.",
                choices = listOf(
                    EventChoice(
                        text = "Dispatch Sword Disciples to Subdue",
                        description = "Enforce sect justice with martial might.",
                        requiredQi = 100L,
                        requiredStones = 0L,
                        karmaImpact = 20L,
                        successRate = 0.85f,
                        rewardSummary = "+250 Spirit Stones, +30 Fame, Rogue Pill Stash",
                        outcomeSuccess = "Your disciples captured the rogue and confiscated his spatial ring filled with spirit stones!",
                        outcomeFailure = "The rogue used a blood escape talisman, leaving behind only minor scrap materials."
                    ),
                    EventChoice(
                        text = "Activate Sect Defense Barrier",
                        description = "Defend within the mountain gate array safely.",
                        requiredQi = 50L,
                        requiredStones = 30L,
                        karmaImpact = 5L,
                        successRate = 1.0f,
                        rewardSummary = "+50 Fame, Mountain Gate Reinforced",
                        outcomeSuccess = "The golden mountain barrier repels the intruder instantly.",
                        outcomeFailure = "The barrier held firm."
                    )
                )
            )
        )
        return events[RNG.nextInt(events.size)]
    }

    private fun generateFallbackQuest(difficulty: Int): DynamicSectQuest {
        val titles = listOf(
            "Purge the Thunder Wyrm Nest",
            "Harvest 1000-Year Frost Lotus",
            "Defend Mortal Border from Corpse Fiends",
            "Explore the Sunken Abyssal Pagoda"
        )
        val targets = listOf("Thunder Wyrm", "Frost Drake", "Corpse Fiend Lord", "Abyssal Shadow Golem")
        val idx = RNG.nextInt(titles.size)

        return DynamicSectQuest(
            title = titles[idx],
            description = "Travel to the perilous outer borders to eliminate demonic threats and retrieve precious cultivation treasures for the sect.",
            targetName = targets[idx],
            difficulty = difficulty,
            rewardQi = difficulty * 300L,
            rewardStones = difficulty * 150L,
            rewardKarma = difficulty * 15L,
            rewardReputation = difficulty * 35L
        )
    }

    private fun getDefaultChoices(): List<EventChoice> {
        return listOf(
            EventChoice(
                text = "Embrace the Opportunity",
                description = "Invest spiritual resources to maximize benefits.",
                requiredQi = 50L,
                requiredStones = 30L,
                karmaImpact = 10L,
                successRate = 0.85f,
                rewardSummary = "+200 Qi, +100 Spirit Stones",
                outcomeSuccess = "Great success! The sect reaped abundant heavenly rewards.",
                outcomeFailure = "The event passed with modest gains."
            )
        )
    }
}
