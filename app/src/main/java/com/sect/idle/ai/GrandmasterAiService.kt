package com.sect.idle.ai

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * GrandmasterAiService - Connects to Gemini API for deep immortal wisdom,
 * cultivation guidance, scripture generation, and disciple divination.
 */
object GrandmasterAiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private const val MODEL_NAME = "gemini-2.5-flash"
    private const val SYSTEM_PROMPT = """
You are the Ancient Grandmaster Ancestor of a Daoist cultivation sect in an Eastern fantasy Xianxia world.
Speak in an enigmatic, profound, wise, and encouraging tone of an ancient Daoist immortal.
Use cultivation terms like Dao, Qi, Dantian, Tribulation, Karma, Spiritual Roots, and Heavenly Fortune.
Keep your answers engaging, immersive, and concise (under 120 words unless requested).
Provide practical cultivation insights or mystical prophecies when asked.
"""

    suspend fun askGrandmaster(userPrompt: String, sectContext: String = ""): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isNullOrBlank() || apiKey == "YOUR_API_KEY") {
            return@withContext generateOfflineGrandmasterWisdom(userPrompt)
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent?key=$apiKey"

            val fullUserContent = if (sectContext.isNotBlank()) {
                "[Current Sect Status: $sectContext]\n\nSect Master asks: $userPrompt"
            } else {
                userPrompt
            }

            val jsonBody = JSONObject().apply {
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", SYSTEM_PROMPT))
                    })
                })
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", fullUserContent))
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("maxOutputTokens", 300)
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful || responseBody.isBlank()) {
                return@withContext generateOfflineGrandmasterWisdom(userPrompt)
            }

            val json = JSONObject(responseBody)
            val candidates = json.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    return@withContext parts.getJSONObject(0).optString("text", "The Dao that can be spoken is not the eternal Dao.")
                }
            }
            generateOfflineGrandmasterWisdom(userPrompt)
        } catch (e: Exception) {
            generateOfflineGrandmasterWisdom(userPrompt)
        }
    }

    private fun generateOfflineGrandmasterWisdom(prompt: String): String {
        val p = prompt.lowercase()
        return when {
            "breakthrough" in p || "tribulation" in p ->
                "Junior, the heavenly tribulation tests not just the body, but the Dao heart. Fortify your disciples' Dantian with Spirit Gathering Elixirs, and let them face the lightning without fear of death. Only through destruction can rebirth occur."
            "disciple" in p || "recruit" in p ->
                "A pure spiritual root is a blessing, yet unyielding perseverance is what turns mortal clay into immortal jade. Watch closely their loyalty and Dao heart; even a dual-root cultivator can shake the heavens with the right discipline."
            "pill" in p || "alchemy" in p ->
                "The furnace mirrors the universe. Yin herbs temper the raging Yang fire. Ensure your Alchemy Chamber reaches Tier 3 to extract ninety percent purity, preventing pill poison from corrupting their meridians."
            "scripture" in p || "technique" in p ->
                "The Nine Heavens Celestial Scripture requires understanding the harmony of the Five Elements. Wood feeds Fire, Fire creates Earth. Direct your disciples to meditate in the Scripture Pavilion under the full moon."
            "battle" in p || "demon" in p || "beast" in p ->
                "Ancient demon beasts are driven by raw primordial instinct. Form a Five Elements Array: place your Metal disciples on the vanguard for supreme penetration, supported by Water healers from behind."
            else ->
                "The Grand Dao is boundless. Cultivate the inner self, gather the earth's spiritual veins, and protect the sect from worldly chaos. When the stars align, your disciples shall ascend to the Immortal Realm!"
        }
    }
}
