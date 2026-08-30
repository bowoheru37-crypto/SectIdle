package com.sect.idle.systems

import android.content.Context

/**
 * SoundManager - Kotlin Idiomatic Component facade for the Immortal Sect Audio Engine.
 * Provides high-level typed triggers for:
 * 1. Combat Sound Effects (Strikes, Crits, Shields, Elementals, Enrage, Fanfares).
 * 2. Disciple Interactions (Greetings, Breakthroughs, Pill Ingestion, Task Assignment, Ascension).
 * 3. Sect & Mountain Events (Spiritual Harvest, Pavilion Construction, Alchemy, Dao Comprehension).
 * 4. Procedural & Background Music Themes.
 */
class SoundManager private constructor(private val context: Context) {

    private val audioEngine: AudioManager = AudioManager.get(context)

    // ========================================================================
    // TYPED AUDIO EVENT DEFINITIONS
    // ========================================================================

    enum class CombatSound(val effectKey: String) {
        START(AudioManager.SFX_COMBAT_START),
        STRIKE(AudioManager.SFX_STRIKE),
        SWORD_SPAR(AudioManager.SFX_SWORD_SPAR),
        CRITICAL(AudioManager.SFX_CRITICAL_STRIKE),
        SHIELD_BLOCK(AudioManager.SFX_SHIELD_BLOCK),
        BARRIER_SHATTER(AudioManager.SFX_BARRIER_SHATTER),
        FIRE_BURST(AudioManager.SFX_ELEMENTAL_FIRE),
        LIGHTNING_BURST(AudioManager.SFX_ELEMENTAL_LIGHTNING),
        ICE_BURST(AudioManager.SFX_ELEMENTAL_ICE),
        BOSS_ENRAGE(AudioManager.SFX_BOSS_ENRAGE),
        VICTORY(AudioManager.SFX_VICTORY),
        DEFEAT(AudioManager.SFX_DEFEAT)
    }

    enum class ArenaSound(val effectKey: String) {
        RING_BELL(AudioManager.SFX_RING_BELL),
        CROWD_CHEER(AudioManager.SFX_CROWD_CHEER),
        CROWD_GASP(AudioManager.SFX_CROWD_GASP),
        REFEREE_COUNT(AudioManager.SFX_REFEREE_COUNT),
        GRAPPLE_SLAM(AudioManager.SFX_GRAPPLE_SLAM),
        ROPE_BOUNCE(AudioManager.SFX_ROPE_BOUNCE),
        FINISHER_HIT(AudioManager.SFX_FINISHER_HIT)
    }

    enum class DiscipleSound(val effectKey: String) {
        GREETING(AudioManager.SFX_DISCIPLE_GREETING),
        BREAKTHROUGH_SUCCESS(AudioManager.SFX_BREAKTHROUGH),
        BREAKTHROUGH_FAIL(AudioManager.SFX_BREAKTHROUGH_FAIL),
        BESTOW_PILL(AudioManager.SFX_BESTOW_PILL),
        ASSIGN_TASK(AudioManager.SFX_ASSIGN_TASK),
        RECRUIT(AudioManager.SFX_RECRUIT),
        DISMISS(AudioManager.SFX_DISMISS),
        DAO_EPIPHANY(AudioManager.SFX_DAO_ENLIGHTENMENT)
    }

    enum class SectSound(val effectKey: String) {
        GATHER_QI(AudioManager.SFX_GATHER),
        COLLECT_STONES(AudioManager.SFX_COLLECT),
        BUILDING_UPGRADE(AudioManager.SFX_UPGRADE),
        BUILDING_DEMOLISH(AudioManager.SFX_DEMOLISH),
        ALCHEMY_REFINE(AudioManager.SFX_ALCHEMY),
        SCRIPTURE_COMPREHEND(AudioManager.SFX_SCRIPTURE),
        UI_CLICK(AudioManager.SFX_CLICK),
        ACTION_FAIL(AudioManager.SFX_FAIL)
    }

    enum class BgmTheme(val themeKey: String) {
        SECT_PEACE(AudioManager.THEME_SECT_PEACE),
        COMBAT_INTENSE(AudioManager.THEME_COMBAT_INTENSE),
        MEDITATION_ZEN(AudioManager.THEME_MEDITATION_ZEN)
    }

    // ========================================================================
    // TRIGGER METHODS
    // ========================================================================

    fun playCombatSound(sound: CombatSound, pitch: Float = 1.0f) {
        audioEngine.playSfx(sound.effectKey, pitch, 1.0f)
    }

    fun playArenaSound(sound: ArenaSound, pitch: Float = 1.0f) {
        audioEngine.playSfx(sound.effectKey, pitch, 1.0f)
    }

    fun playDiscipleSound(sound: DiscipleSound, pitch: Float = 1.0f) {
        audioEngine.playSfx(sound.effectKey, pitch, 0.8f)
    }

    fun playSectSound(sound: SectSound, pitch: Float = 1.0f) {
        audioEngine.playSfx(sound.effectKey, pitch, 0.5f)
    }

    fun setBgmTheme(theme: BgmTheme) {
        audioEngine.setBgmTheme(theme.themeKey)
    }

    fun setSoundEnabled(enabled: Boolean) {
        audioEngine.setEnabled(enabled)
    }

    fun isSoundEnabled(): Boolean = audioEngine.isEnabled

    fun setMasterVolume(vol: Float) {
        audioEngine.setMasterVolume(vol)
    }

    fun setSfxVolume(vol: Float) {
        audioEngine.setSfxVolume(vol)
    }

    fun setBgmVolume(vol: Float) {
        audioEngine.setBgmVolume(vol)
    }

    fun pauseAllAudio() {
        audioEngine.pauseBgm()
    }

    fun resumeAllAudio() {
        audioEngine.resumeBgm()
    }

    companion object {
        @Volatile
        private var instance: SoundManager? = null

        fun get(context: Context): SoundManager {
            return instance ?: synchronized(this) {
                instance ?: SoundManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
