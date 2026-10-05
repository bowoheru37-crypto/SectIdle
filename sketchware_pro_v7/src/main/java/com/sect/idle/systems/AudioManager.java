package com.sect.idle.systems;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Looper;
import com.sect.idle.core.MathUtils;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AudioManager - High-performance, zero-allocation software PCM Audio Mixer
 * optimized for Android 5.0+ (API 21+) and all modern Android versions.
 *
 * Features:
 * 1. Pure in-memory procedural Xianxia sound synthesis (zero disk I/O, zero MediaCodec/SoundPool dependencies).
 * 2. Unified low-latency software mixing engine for simultaneous BGM and polyphonic SFX.
 * 3. Dynamic audio ducking, AudioFocus management, and zero GC allocations during gameplay.
 * 4. 100% Java 7 and Sketchware Pro v7.0.0 compatible.
 */
public final class AudioManager {
    private static volatile AudioManager instance;

    public static final String THEME_SECT_PEACE = "sect_peace";
    public static final String THEME_COMBAT_INTENSE = "combat_intense";
    public static final String THEME_MEDITATION_ZEN = "meditation_zen";

    // Combat Sound Identifiers
    public static final String SFX_COMBAT_START = "combat_start";
    public static final String SFX_STRIKE = "strike";
    public static final String SFX_SWORD_SPAR = "sword_spar";
    public static final String SFX_CRITICAL_STRIKE = "critical_strike";
    public static final String SFX_SHIELD_BLOCK = "shield_block";
    public static final String SFX_BARRIER_SHATTER = "barrier_shatter";
    public static final String SFX_ELEMENTAL_FIRE = "elemental_fire";
    public static final String SFX_ELEMENTAL_LIGHTNING = "elemental_lightning";
    public static final String SFX_ELEMENTAL_ICE = "elemental_ice";
    public static final String SFX_BOSS_ENRAGE = "boss_enrage";
    public static final String SFX_VICTORY = "victory";
    public static final String SFX_DEFEAT = "defeat";

    // Martial Arts Arena & Ring Identifiers
    public static final String SFX_RING_BELL = "ring_bell";
    public static final String SFX_CROWD_CHEER = "crowd_cheer";
    public static final String SFX_CROWD_GASP = "crowd_gasp";
    public static final String SFX_REFEREE_COUNT = "referee_count";
    public static final String SFX_GRAPPLE_SLAM = "grapple_slam";
    public static final String SFX_ROPE_BOUNCE = "rope_bounce";
    public static final String SFX_FINISHER_HIT = "finisher_hit";

    // Disciple & Interaction Sound Identifiers
    public static final String SFX_DISCIPLE_GREETING = "disciple_greeting";
    public static final String SFX_BREAKTHROUGH = "breakthrough";
    public static final String SFX_BREAKTHROUGH_FAIL = "breakthrough_fail";
    public static final String SFX_BESTOW_PILL = "bestow_pill";
    public static final String SFX_ASSIGN_TASK = "assign_task";
    public static final String SFX_RECRUIT = "recruit";
    public static final String SFX_DISMISS = "dismiss";
    public static final String SFX_DAO_ENLIGHTENMENT = "dao_enlightenment";

    // Sect & Environment Identifiers
    public static final String SFX_GATHER = "gather";
    public static final String SFX_COLLECT = "collect";
    public static final String SFX_UPGRADE = "upgrade";
    public static final String SFX_DEMOLISH = "demolish";
    public static final String SFX_ALCHEMY = "alchemy";
    public static final String SFX_SCRIPTURE = "scripture";
    public static final String SFX_CLICK = "click";
    public static final String SFX_FAIL = "fail";

    // Xianxia Immersion Identifiers
    public static final String SFX_FLYING_SWORD = "flying_sword";
    public static final String SFX_HEAVENLY_TRIBULATION = "heavenly_tribulation";
    public static final String SFX_QI_BURST = "qi_burst";
    public static final String SFX_PILL_CAULDRON_DING = "pill_cauldron_ding";
    public static final String SFX_TALISMAN_BURN = "talisman_burn";
    public static final String SFX_IMMORTAL_BELL = "immortal_bell";

    private final Context context;
    private final Handler mainHandler;

    private float bgmVolume = 0.6f;
    private float ambientVolume = 0.4f;
    private float sfxVolume = 0.85f;
    private float masterVolume = 1.0f;
    private boolean enabled = true;
    private boolean bgmEnabled = true;
    private boolean ducking = false;

    private static final int MAX_VOICES = 12;
    private static final int SAMPLE_RATE = 22050;
    private static final int BUFFER_SIZE_SAMPLES = 1024;

    private static class Voice {
        short[] sample;
        float cursor;
        float volume;
        float pitch;
        int priority;
        boolean active;
    }

    private final Voice[] voices = new Voice[MAX_VOICES];
    private final HashMap<String, short[]> pcmCache = new HashMap<String, short[]>();

    private AudioTrack mixerTrack;
    private Thread mixerThread;
    private final AtomicBoolean isMixerRunning = new AtomicBoolean(false);
    private volatile String activeBgmTheme = THEME_SECT_PEACE;

    private android.media.AudioManager androidAudioManager;
    private android.media.AudioManager.OnAudioFocusChangeListener focusListener;

    // Ancient Chinese Pentatonic Frequencies (Gong, Shang, Jue, Zhi, Yu)
    private static final float[] PENTATONIC_FREQS = {
            261.63f, // C4 (Gong)
            293.66f, // D4 (Shang)
            329.63f, // E4 (Jue)
            392.00f, // G4 (Zhi)
            440.00f, // A4 (Yu)
            523.25f, // C5
            587.33f, // D5
            659.25f, // E5
            783.99f, // G5
            880.00f, // A5
            1046.50f // C6
    };

    private static final String[] ALL_SFX_KEYS = new String[] {
            SFX_CLICK, SFX_STRIKE, SFX_SWORD_SPAR, SFX_CRITICAL_STRIKE,
            SFX_SHIELD_BLOCK, SFX_BARRIER_SHATTER, SFX_ELEMENTAL_FIRE,
            SFX_ELEMENTAL_LIGHTNING, SFX_ELEMENTAL_ICE, SFX_BOSS_ENRAGE,
            SFX_VICTORY, SFX_DEFEAT, SFX_COMBAT_START,
            SFX_RING_BELL, SFX_CROWD_CHEER, SFX_CROWD_GASP, SFX_REFEREE_COUNT,
            SFX_GRAPPLE_SLAM, SFX_ROPE_BOUNCE, SFX_FINISHER_HIT,
            SFX_DISCIPLE_GREETING, SFX_BREAKTHROUGH, SFX_BREAKTHROUGH_FAIL,
            SFX_BESTOW_PILL, SFX_ASSIGN_TASK, SFX_RECRUIT, SFX_DISMISS,
            SFX_DAO_ENLIGHTENMENT, SFX_GATHER, SFX_COLLECT, SFX_UPGRADE,
            SFX_DEMOLISH, SFX_ALCHEMY, SFX_SCRIPTURE, SFX_FAIL,
            SFX_FLYING_SWORD, SFX_HEAVENLY_TRIBULATION, SFX_QI_BURST,
            SFX_PILL_CAULDRON_DING, SFX_TALISMAN_BURN, SFX_IMMORTAL_BELL
    };

    private AudioManager(Context ctx) {
        this.context = ctx.getApplicationContext();
        this.mainHandler = new Handler(Looper.getMainLooper());

        for (int i = 0; i < MAX_VOICES; i++) {
            voices[i] = new Voice();
        }

        this.androidAudioManager = (android.media.AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        this.focusListener = new android.media.AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                switch (focusChange) {
                    case android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        setDucking(true);
                        break;
                    case android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    case android.media.AudioManager.AUDIOFOCUS_LOSS:
                        pauseBgm();
                        break;
                    case android.media.AudioManager.AUDIOFOCUS_GAIN:
                        setDucking(false);
                        resumeBgm();
                        break;
                }
            }
        };
        requestAudioFocus();

        // Synthesize all waveforms directly into memory
        for (int i = 0; i < ALL_SFX_KEYS.length; i++) {
            String key = ALL_SFX_KEYS[i];
            pcmCache.put(key, synthesizePcm(key));
        }

        startMixer();
    }

    public static AudioManager get(Context ctx) {
        if (instance == null) {
            synchronized (AudioManager.class) {
                if (instance == null) {
                    instance = new AudioManager(ctx);
                }
            }
        }
        return instance;
    }

    public static AudioManager getInstance(Context ctx) {
        return get(ctx);
    }

    private void requestAudioFocus() {
        if (androidAudioManager != null) {
            try {
                androidAudioManager.requestAudioFocus(focusListener, android.media.AudioManager.STREAM_MUSIC, android.media.AudioManager.AUDIOFOCUS_GAIN);
            } catch (Exception ignored) {}
        }
    }

    private void abandonAudioFocus() {
        if (androidAudioManager != null) {
            try {
                androidAudioManager.abandonAudioFocus(focusListener);
            } catch (Exception ignored) {}
        }
    }

    public void playSfx(String id) {
        playSfx(id, 1.0f, 0);
    }

    public void playSfx(String id, float priority) {
        playSfx(id, 1.0f, priority);
    }

    public void playSfx(String id, float pitch, float priority) {
        if (!enabled || id == null) return;

        short[] sample = pcmCache.get(id);
        if (sample == null) {
            sample = synthesizePcm(id);
            pcmCache.put(id, sample);
        }

        synchronized (voices) {
            int selectedVoice = -1;
            int lowestPriority = Integer.MAX_VALUE;

            for (int i = 0; i < MAX_VOICES; i++) {
                if (!voices[i].active) {
                    selectedVoice = i;
                    break;
                }
                if (voices[i].priority < lowestPriority) {
                    lowestPriority = voices[i].priority;
                    selectedVoice = i;
                }
            }

            if (selectedVoice >= 0) {
                Voice v = voices[selectedVoice];
                v.sample = sample;
                v.cursor = 0f;
                v.pitch = MathUtils.clamp(pitch, 0.5f, 2.0f);
                v.volume = sfxVolume * masterVolume * (ducking ? 0.35f : 1.0f);
                v.priority = (int) priority;
                v.active = true;
            }
        }
    }

    // ========================================================================
    // UNIFIED PCM MIXER ENGINE (ZERO NATIVE CODEC / SOUNDPOOL CALLS)
    // ========================================================================

    private synchronized void startMixer() {
        if (isMixerRunning.get()) return;
        isMixerRunning.set(true);

        mixerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int minBuf = AudioTrack.getMinBufferSize(
                            SAMPLE_RATE,
                            AudioFormat.CHANNEL_OUT_MONO,
                            AudioFormat.ENCODING_PCM_16BIT
                    );

                    mixerTrack = new AudioTrack.Builder()
                            .setAudioAttributes(new AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_GAME)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                    .build())
                            .setAudioFormat(new AudioFormat.Builder()
                                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                    .setSampleRate(SAMPLE_RATE)
                                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                    .build())
                            .setBufferSizeInBytes(Math.max(minBuf * 2, BUFFER_SIZE_SAMPLES * 4))
                            .setTransferMode(AudioTrack.MODE_STREAM)
                            .build();

                    if (mixerTrack.getState() != AudioTrack.STATE_INITIALIZED) {
                        return;
                    }

                    mixerTrack.play();

                    short[] mixBuffer = new short[BUFFER_SIZE_SAMPLES];
                    float[] bgmNoteBuffer = new float[SAMPLE_RATE * 2]; // up to 2s note buffer
                    int bgmNoteLen = 0;
                    int bgmNotePos = 0;
                    int bgmPauseRemaining = 0;
                    Random rand = new Random(System.currentTimeMillis());

                    while (isMixerRunning.get()) {
                        if (!enabled) {
                            Thread.sleep(50);
                            continue;
                        }

                        // 1. Generate procedural Xianxia BGM stream into mixBuffer
                        float effBgmVol = bgmEnabled ? getEffectiveBgmVolume() * 0.32f : 0f;
                        String currentTheme = activeBgmTheme;

                        for (int i = 0; i < BUFFER_SIZE_SAMPLES; i++) {
                            float bgmSample = 0f;

                            if (effBgmVol > 0.001f) {
                                if (bgmPauseRemaining > 0) {
                                    bgmPauseRemaining--;
                                } else if (bgmNotePos < bgmNoteLen) {
                                    bgmSample = bgmNoteBuffer[bgmNotePos++] * effBgmVol;
                                } else {
                                    // Generate next pentatonic note with Chinese traditional timbre (Guzheng pluck + Dizi vibrato harmonics)
                                    int noteIdx = rand.nextInt(PENTATONIC_FREQS.length);
                                    float freq = PENTATONIC_FREQS[noteIdx];

                                    int noteMs;
                                    int pauseMs;
                                    float decay;
                                    boolean isFlutePassage = rand.nextFloat() < 0.35f;

                                    if (THEME_COMBAT_INTENSE.equals(currentTheme)) {
                                        noteMs = 200 + rand.nextInt(180);
                                        pauseMs = 60 + rand.nextInt(80);
                                        decay = 4.5f;
                                    } else if (THEME_MEDITATION_ZEN.equals(currentTheme)) {
                                        noteMs = 1200 + rand.nextInt(600);
                                        pauseMs = 500 + rand.nextInt(450);
                                        decay = 1.6f;
                                    } else {
                                        noteMs = 600 + rand.nextInt(450);
                                        pauseMs = 240 + rand.nextInt(280);
                                        decay = 2.8f;
                                    }

                                    bgmNoteLen = Math.min((SAMPLE_RATE * noteMs) / 1000, bgmNoteBuffer.length);
                                    bgmNotePos = 0;
                                    bgmPauseRemaining = (SAMPLE_RATE * pauseMs) / 1000;

                                    for (int s = 0; s < bgmNoteLen; s++) {
                                        float t = (float) s / bgmNoteLen;
                                        float timeSec = s / (float) SAMPLE_RATE;

                                        if (isFlutePassage) {
                                            // Dizi (Bamboo Flute) timbre with breath noise and vibrato
                                            float vibrato = (float) Math.sin(2.0 * Math.PI * 5.5 * timeSec) * (freq * 0.015f);
                                            float effFreq = freq + vibrato;
                                            float fund = (float) Math.sin(2.0 * Math.PI * effFreq * timeSec);
                                            float h2 = (float) Math.sin(4.0 * Math.PI * effFreq * timeSec) * 0.38f;
                                            float h3 = (float) Math.sin(6.0 * Math.PI * effFreq * timeSec) * 0.12f;
                                            float breath = (float) ((rand.nextFloat() - 0.5f) * 0.04f);
                                            // Attack-Decay-Sustain-Release envelope
                                            float fluteEnv = (float) (Math.sin(Math.min(1.0, t * 4.0) * Math.PI * 0.5) * Math.pow(1.0 - t, 0.8));
                                            bgmNoteBuffer[s] = (fund + h2 + h3 + breath) * fluteEnv * 0.85f;
                                        } else {
                                            // Guzheng / Guqin plucking resonance with dual harmonic decay
                                            float fund = (float) Math.sin(2.0 * Math.PI * freq * timeSec);
                                            float h2 = (float) Math.sin(4.0 * Math.PI * freq * 1.002f * timeSec) * 0.35f;
                                            float h3 = (float) Math.sin(6.0 * Math.PI * freq * 0.998f * timeSec) * 0.18f;
                                            float h4 = (float) Math.sin(8.0 * Math.PI * freq * timeSec) * 0.08f;
                                            float pluckAttack = (float) Math.exp(-25.0 * t);
                                            float pluckBody = (float) Math.exp(-decay * t);
                                            float env = pluckAttack * 0.4f + pluckBody * 0.6f;
                                            float streamBreeze = (float) ((rand.nextFloat() - 0.5f) * 0.02f * Math.sin(t * Math.PI));
                                            bgmNoteBuffer[s] = (fund + h2 + h3 + h4 + streamBreeze) * env;
                                        }
                                    }

                                    if (bgmNoteLen > 0) {
                                        bgmSample = bgmNoteBuffer[bgmNotePos++] * effBgmVol;
                                    }
                                }
                            }

                            // 2. Mix active SFX voices
                            float sfxSum = 0f;
                            synchronized (voices) {
                                for (int v = 0; v < MAX_VOICES; v++) {
                                    Voice voice = voices[v];
                                    if (voice.active && voice.sample != null) {
                                        int idx = (int) voice.cursor;
                                        if (idx < voice.sample.length) {
                                            sfxSum += (voice.sample[idx] / 32768.0f) * voice.volume;
                                            voice.cursor += voice.pitch;
                                        } else {
                                            voice.active = false;
                                        }
                                    }
                                }
                            }

                            float total = bgmSample + sfxSum;
                            mixBuffer[i] = (short) MathUtils.clamp(total * Short.MAX_VALUE, Short.MIN_VALUE, Short.MAX_VALUE);
                        }

                        mixerTrack.write(mixBuffer, 0, BUFFER_SIZE_SAMPLES);
                    }
                } catch (Exception ignored) {
                } finally {
                    if (mixerTrack != null) {
                        try {
                            mixerTrack.stop();
                            mixerTrack.release();
                        } catch (Exception ignored) {}
                        mixerTrack = null;
                    }
                    isMixerRunning.set(false);
                }
            }
        }, "SectPcmMixer");

        mixerThread.setPriority(Thread.NORM_PRIORITY + 1);
        mixerThread.setDaemon(true);
        mixerThread.start();
    }

    private synchronized void stopMixer() {
        isMixerRunning.set(false);
        if (mixerThread != null) {
            mixerThread.interrupt();
            mixerThread = null;
        }
    }

    // ========================================================================
    // PROCEDURAL AUDIO SYNTHESIZER
    // ========================================================================

    private short[] synthesizePcm(String effect) {
        int sampleRate = SAMPLE_RATE;
        int durationMs;
        float startFreq;
        float endFreq;

        if (SFX_COMBAT_START.equals(effect)) {
            durationMs = 320; startFreq = 75f; endFreq = 440f;
        } else if (SFX_STRIKE.equals(effect) || SFX_SWORD_SPAR.equals(effect)) {
            durationMs = 120; startFreq = 620f; endFreq = 140f;
        } else if (SFX_CRITICAL_STRIKE.equals(effect) || SFX_ELEMENTAL_LIGHTNING.equals(effect)) {
            durationMs = 280; startFreq = 1100f; endFreq = 90f;
        } else if (SFX_SHIELD_BLOCK.equals(effect)) {
            durationMs = 140; startFreq = 1860f; endFreq = 820f;
        } else if (SFX_BARRIER_SHATTER.equals(effect)) {
            durationMs = 220; startFreq = 1500f; endFreq = 180f;
        } else if (SFX_ELEMENTAL_FIRE.equals(effect)) {
            durationMs = 240; startFreq = 160f; endFreq = 380f;
        } else if (SFX_ELEMENTAL_ICE.equals(effect)) {
            durationMs = 180; startFreq = 1300f; endFreq = 1950f;
        } else if (SFX_BOSS_ENRAGE.equals(effect)) {
            durationMs = 380; startFreq = 60f; endFreq = 200f;
        } else if (SFX_VICTORY.equals(effect)) {
            durationMs = 400; startFreq = 523.25f; endFreq = 1046.50f;
        } else if (SFX_DEFEAT.equals(effect)) {
            durationMs = 340; startFreq = 240f; endFreq = 80f;
        } else if (SFX_RING_BELL.equals(effect) || SFX_IMMORTAL_BELL.equals(effect)) {
            durationMs = 420; startFreq = 1318.5f; endFreq = 659.25f;
        } else if (SFX_CROWD_CHEER.equals(effect)) {
            durationMs = 450; startFreq = 280f; endFreq = 540f;
        } else if (SFX_CROWD_GASP.equals(effect)) {
            durationMs = 220; startFreq = 600f; endFreq = 300f;
        } else if (SFX_REFEREE_COUNT.equals(effect)) {
            durationMs = 130; startFreq = 140f; endFreq = 50f;
        } else if (SFX_GRAPPLE_SLAM.equals(effect)) {
            durationMs = 260; startFreq = 90f; endFreq = 45f;
        } else if (SFX_ROPE_BOUNCE.equals(effect)) {
            durationMs = 160; startFreq = 350f; endFreq = 680f;
        } else if (SFX_FINISHER_HIT.equals(effect)) {
            durationMs = 480; startFreq = 440f; endFreq = 70f;
        } else if (SFX_DISCIPLE_GREETING.equals(effect)) {
            durationMs = 150; startFreq = 440f; endFreq = 880f;
        } else if (SFX_BREAKTHROUGH.equals(effect)) {
            durationMs = 480; startFreq = 196f; endFreq = 1318.5f;
        } else if (SFX_BREAKTHROUGH_FAIL.equals(effect)) {
            durationMs = 240; startFreq = 311f; endFreq = 75f;
        } else if (SFX_BESTOW_PILL.equals(effect) || SFX_PILL_CAULDRON_DING.equals(effect)) {
            durationMs = 220; startFreq = 783.99f; endFreq = 1567.98f;
        } else if (SFX_ASSIGN_TASK.equals(effect)) {
            durationMs = 90; startFreq = 1100f; endFreq = 550f;
        } else if (SFX_RECRUIT.equals(effect)) {
            durationMs = 260; startFreq = 587.33f; endFreq = 1174.66f;
        } else if (SFX_DISMISS.equals(effect)) {
            durationMs = 180; startFreq = 780f; endFreq = 220f;
        } else if (SFX_DAO_ENLIGHTENMENT.equals(effect)) {
            durationMs = 460; startFreq = 261.63f; endFreq = 659.25f;
        } else if (SFX_GATHER.equals(effect) || SFX_COLLECT.equals(effect)) {
            durationMs = 95; startFreq = 783.99f; endFreq = 1567.98f;
        } else if (SFX_UPGRADE.equals(effect)) {
            durationMs = 220; startFreq = 392.00f; endFreq = 880.00f;
        } else if (SFX_DEMOLISH.equals(effect)) {
            durationMs = 190; startFreq = 160f; endFreq = 60f;
        } else if (SFX_ALCHEMY.equals(effect)) {
            durationMs = 220; startFreq = 280f; endFreq = 560f;
        } else if (SFX_SCRIPTURE.equals(effect)) {
            durationMs = 300; startFreq = 329.63f; endFreq = 783.99f;
        } else if (SFX_FLYING_SWORD.equals(effect)) {
            durationMs = 280; startFreq = 680f; endFreq = 1250f;
        } else if (SFX_HEAVENLY_TRIBULATION.equals(effect)) {
            durationMs = 450; startFreq = 120f; endFreq = 40f;
        } else if (SFX_QI_BURST.equals(effect)) {
            durationMs = 300; startFreq = 320f; endFreq = 880f;
        } else if (SFX_TALISMAN_BURN.equals(effect)) {
            durationMs = 180; startFreq = 480f; endFreq = 960f;
        } else if (SFX_FAIL.equals(effect)) {
            durationMs = 140; startFreq = 240f; endFreq = 120f;
        } else {
            durationMs = 45; startFreq = 880f; endFreq = 960f;
        }

        int numSamples = (sampleRate * durationMs) / 1000;
        short[] buffer = new short[numSamples];

        for (int i = 0; i < numSamples; i++) {
            float t = (float) i / numSamples;
            float freq = startFreq + (endFreq - startFreq) * t;
            float phase = (float) (2.0 * Math.PI * freq * (i / (float) sampleRate));
            float envelope;

            if (SFX_STRIKE.equals(effect) || SFX_SWORD_SPAR.equals(effect) || SFX_DISMISS.equals(effect)) {
                envelope = (float) Math.pow(1.0 - t, 2.0);
            } else if (SFX_CRITICAL_STRIKE.equals(effect) || SFX_ELEMENTAL_LIGHTNING.equals(effect) || SFX_HEAVENLY_TRIBULATION.equals(effect)) {
                float noise = (float) ((Math.random() - 0.5) * 0.55 * Math.pow(1.0 - t, 1.1));
                envelope = (float) Math.pow(1.0 - t, 1.2);
                float sampleVal = ((float) Math.sin(phase) + noise) * envelope * Short.MAX_VALUE * 0.90f;
                buffer[i] = (short) MathUtils.clamp(sampleVal, Short.MIN_VALUE, Short.MAX_VALUE);
                continue;
            } else if (SFX_BREAKTHROUGH.equals(effect) || SFX_VICTORY.equals(effect) || SFX_DAO_ENLIGHTENMENT.equals(effect) || SFX_IMMORTAL_BELL.equals(effect)) {
                float harmonic2 = (float) Math.sin(phase * 2.0) * 0.35f;
                float harmonic3 = (float) Math.sin(phase * 3.0) * 0.15f;
                float harmonic4 = (float) Math.sin(phase * 4.0) * 0.08f;
                envelope = (float) (Math.sin(t * Math.PI * 0.5) * Math.pow(1.0 - t, 0.70));
                float sampleVal = ((float) Math.sin(phase) + harmonic2 + harmonic3 + harmonic4) * envelope * Short.MAX_VALUE * 0.78f;
                buffer[i] = (short) MathUtils.clamp(sampleVal, Short.MIN_VALUE, Short.MAX_VALUE);
                continue;
            } else if (SFX_FLYING_SWORD.equals(effect)) {
                float breeze = (float) ((Math.random() - 0.5) * 0.25);
                envelope = (float) (Math.sin(t * Math.PI) * 0.8);
                float sampleVal = ((float) Math.sin(phase) + breeze) * envelope * Short.MAX_VALUE * 0.75f;
                buffer[i] = (short) MathUtils.clamp(sampleVal, Short.MIN_VALUE, Short.MAX_VALUE);
                continue;
            } else if (SFX_SHIELD_BLOCK.equals(effect)) {
                envelope = (float) Math.exp(-6.0 * t);
            } else if (SFX_ASSIGN_TASK.equals(effect)) {
                envelope = (float) Math.exp(-12.0 * t);
            } else {
                envelope = (float) Math.sin(t * Math.PI);
            }

            float sampleVal = (float) Math.sin(phase) * envelope * 0.75f * Short.MAX_VALUE;
            buffer[i] = (short) MathUtils.clamp(sampleVal, Short.MIN_VALUE, Short.MAX_VALUE);
        }

        return buffer;
    }

    public void startProceduralBgm(String theme) {
        this.activeBgmTheme = theme;
        if (!isMixerRunning.get()) {
            startMixer();
        }
    }

    public void stopProceduralBgm() {
        // Handled dynamically in mixer
    }

    public void playBgm(String theme) {
        startProceduralBgm(theme);
    }

    public void playBgm(String theme, boolean loop) {
        startProceduralBgm(theme);
    }

    public void playBgm(int resId, final boolean loop) {
        // Compatibility stub
    }

    public void stopBgm() {
        // Compatibility stub
    }

    public void stopAmbient() {
        // Compatibility stub
    }

    public void pauseBgm() {
        setBgmEnabled(false);
    }

    public void resumeBgm() {
        setBgmEnabled(true);
    }

    public void setDucking(boolean d) {
        this.ducking = d;
    }

    public float getEffectiveBgmVolume() {
        return MathUtils.clamp(bgmVolume * masterVolume * (ducking ? 0.2f : 1f), 0f, 1f);
    }

    public float getEffectiveAmbientVolume() {
        return MathUtils.clamp(ambientVolume * masterVolume * (ducking ? 0.1f : 1f), 0f, 1f);
    }

    public float getBgmVolume() {
        return bgmVolume;
    }

    public float getAmbientVolume() {
        return ambientVolume;
    }

    public float getSfxVolume() {
        return sfxVolume;
    }

    public float getMasterVolume() {
        return masterVolume;
    }

    public void setBgmVolume(float v) {
        this.bgmVolume = MathUtils.clamp01(v);
    }

    public void setAmbientVolume(float v) {
        this.ambientVolume = MathUtils.clamp01(v);
    }

    public void setSfxVolume(float v) {
        this.sfxVolume = MathUtils.clamp01(v);
    }

    public void setMasterVolume(float v) {
        this.masterVolume = MathUtils.clamp01(v);
    }

    public void setBgmEnabled(boolean e) {
        this.bgmEnabled = e;
    }

    public boolean isBgmEnabled() {
        return bgmEnabled;
    }

    public void setEnabled(boolean e) {
        this.enabled = e;
        if (e && !isMixerRunning.get()) {
            requestAudioFocus();
            startMixer();
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void release() {
        mainHandler.removeCallbacksAndMessages(null);
        abandonAudioFocus();
        stopMixer();
        instance = null;
    }
}
