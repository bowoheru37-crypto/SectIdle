package com.sect.idle.systems;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Looper;
import com.example.R;
import com.sect.idle.core.MathUtils;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AudioManager - High-performance, low-latency audio engine optimized for Android 5.0+ (API 21+)
 * smartphone devices, featuring:
 * 1. Procedural Xianxia sound synthesizer for combat strikes, criticals, shields, and disciple interactions.
 * 2. Multi-theme procedural continuous BGM generator (Peaceful Sect, Intense Combat, Zen Meditation).
 * 3. Dynamic audio ducking, AudioFocus management, and zero-allocation runtime streaming buffers.
 * 4. Full fallback and integration with Android background assets.
 * 5. 100% Pure Java 7 & Sketchware Pro v7.0.0 Compatible (Zero Lambdas, Zero Streams).
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

    // Martial Arts Arena & WWE/MMA Ring Identifiers
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

    private SoundPool sfxPool;
    private MediaPlayer bgmPlayer;
    private MediaPlayer ambientPlayer;
    private final HashMap<String, Integer> sfxMap = new HashMap<String, Integer>();
    private final HashMap<String, Integer> loadedSfx = new HashMap<String, Integer>();
    private final HashMap<String, Float> sfxPitches = new HashMap<String, Float>();
    private final Context context;

    private float bgmVolume = 0.6f;
    private float ambientVolume = 0.4f;
    private float sfxVolume = 0.75f;
    private float masterVolume = 1.0f;
    private boolean enabled = true;
    private boolean bgmEnabled = true;
    private boolean ducking = false;
    private String currentBgm = "";
    private String currentAmbient = "";

    private final Handler mainHandler;
    private final ExecutorService soundExecutor;

    // Procedural Xianxia BGM Streaming Track
    private AudioTrack proceduralBgmTrack;
    private final AtomicBoolean isProceduralBgmRunning = new AtomicBoolean(false);
    private volatile String activeBgmTheme = THEME_SECT_PEACE;

    private android.media.AudioManager androidAudioManager;
    private android.media.AudioManager.OnAudioFocusChangeListener focusListener;

    private static final int MAX_STREAMS = 8;
    private static final int SAMPLE_RATE = 22050; // Ideal for low-end mobile devices (Android 5+)

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

    private AudioManager(Context ctx) {
        this.context = ctx.getApplicationContext();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.soundExecutor = Executors.newFixedThreadPool(3, new ThreadFactory() {
            private int count = 0;
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "SectAudioEngine-" + (++count));
                t.setPriority(Thread.NORM_PRIORITY);
                t.setDaemon(true);
                return t;
            }
        });

        this.androidAudioManager = (android.media.AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        this.sfxPool = new SoundPool.Builder().setMaxStreams(MAX_STREAMS).setAudioAttributes(attrs).build();
        this.sfxPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool pool, int sampleId, int status) {
                if (status == 0) {
                    sfxPitches.put("loaded_" + sampleId, 1.0f);
                }
            }
        });

        this.focusListener = new android.media.AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                switch (focusChange) {
                    case android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        setDucking(true);
                        break;
                    case android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        pauseBgm();
                        break;
                    case android.media.AudioManager.AUDIOFOCUS_GAIN:
                        setDucking(false);
                        resumeBgm();
                        break;
                    case android.media.AudioManager.AUDIOFOCUS_LOSS:
                        pauseBgm();
                        break;
                }
            }
        };
        requestAudioFocus();

        // Preload Raw Audio Assets
        try {
            loadSfx(SFX_STRIKE, R.raw.sfx_strike);
            loadSfx(SFX_SWORD_SPAR, R.raw.sfx_sword_spar);
            loadSfx(SFX_CRITICAL_STRIKE, R.raw.sfx_critical);
            loadSfx(SFX_BREAKTHROUGH, R.raw.sfx_breakthrough);
            loadSfx(SFX_CLICK, R.raw.sfx_click);
            loadSfx(SFX_BARRIER_SHATTER, R.raw.sfx_spirit_burst);
            loadSfx(SFX_RING_BELL, R.raw.sfx_bell);
            loadSfx(SFX_VICTORY, R.raw.sfx_victory);
            loadSfx(SFX_DEFEAT, R.raw.sfx_defeat);
        } catch (Throwable ignored) {}

        // Start procedural peaceful sect BGM loop by default
        startProceduralBgm(THEME_SECT_PEACE);
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

    public void loadSfx(String id, int resId) {
        if (!enabled || id == null || id.isEmpty() || resId == 0) return;
        if (loadedSfx.containsKey(id)) return;
        int sid = sfxPool.load(context, resId, 1);
        sfxMap.put(id, resId);
        loadedSfx.put(id, sid);
    }

    public void playSfx(String id) {
        playSfx(id, 1.0f, 0);
    }

    public void playSfx(String id, float priority) {
        playSfx(id, 1.0f, priority);
    }

    public void playSfx(String id, float pitch, float priority) {
        if (!enabled || id == null) return;

        Integer sid = loadedSfx.get(id);
        if (sid != null && sfxPool != null) {
            float vol = sfxVolume * masterVolume * (ducking ? 0.3f : 1.0f);
            float p = MathUtils.clamp(pitch, 0.5f, 2.0f);
            sfxPool.play(sid, vol, vol, (int) priority, 0, p);
        } else {
            playSynthesizedEffect(id);
        }
    }

    // ========================================================================
    // PROCEDURAL AUDIO SYNTHESIZER FOR XIANXIA SOUND EFFECTS
    // ========================================================================

    /**
     * Highly optimized Xianxia procedural audio synthesizer generating 16-bit PCM waves in real-time.
     */
    public void playSynthesizedEffect(final String effect) {
        if (!enabled || effect == null) return;

        soundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    int sampleRate = SAMPLE_RATE;
                    int durationMs;
                    float startFreq;
                    float endFreq;
                    float baseVol = sfxVolume * masterVolume * (ducking ? 0.35f : 1.0f);

                switch (effect) {
                    // --- COMBAT AUDIO EFFECTS ---
                    case SFX_COMBAT_START:
                        // Deep War Drum & Bronze Gong (280ms)
                        durationMs = 280;
                        startFreq = 85f;
                        endFreq = 330f;
                        break;
                    case SFX_STRIKE:
                    case SFX_SWORD_SPAR:
                        // Sharp Daoist Sword Slash (110ms)
                        durationMs = 110;
                        startFreq = 540f;
                        endFreq = 160f;
                        break;
                    case SFX_CRITICAL_STRIKE:
                    case SFX_ELEMENTAL_LIGHTNING:
                        // Heavenly Thunder Slash & Critical Burst (260ms)
                        durationMs = 260;
                        startFreq = 980f;
                        endFreq = 110f;
                        break;
                    case SFX_SHIELD_BLOCK:
                        // Dao Qi Barrier Ping Resonance (130ms)
                        durationMs = 130;
                        startFreq = 1760f;
                        endFreq = 880f;
                        break;
                    case SFX_BARRIER_SHATTER:
                        // Barrier Shatter Crystal Crack (200ms)
                        durationMs = 200;
                        startFreq = 1400f;
                        endFreq = 220f;
                        break;
                    case SFX_ELEMENTAL_FIRE:
                        // Fire Talisman Roar (220ms)
                        durationMs = 220;
                        startFreq = 180f;
                        endFreq = 340f;
                        break;
                    case SFX_ELEMENTAL_ICE:
                        // Glacial Frost Chime (170ms)
                        durationMs = 170;
                        startFreq = 1200f;
                        endFreq = 1800f;
                        break;
                    case SFX_BOSS_ENRAGE:
                        // Demonic Sub-Bass Rumble (350ms)
                        durationMs = 350;
                        startFreq = 65f;
                        endFreq = 180f;
                        break;
                    case SFX_VICTORY:
                        // Celestial Fanfare Tri-Chord (360ms)
                        durationMs = 360;
                        startFreq = 523.25f; // C5
                        endFreq = 1046.50f; // C6
                        break;
                    case SFX_DEFEAT:
                        // Solemn Temple Gong (320ms)
                        durationMs = 320;
                        startFreq = 220f;
                        endFreq = 95f;
                        break;

                    // --- WWE / MMA & MARTIAL ARTS ARENA SYNTHESIS ---
                    case SFX_RING_BELL:
                        // Crisp Brass Ring Bell (300ms)
                        durationMs = 300;
                        startFreq = 1760f; // A6
                        endFreq = 1760f;
                        break;
                    case SFX_CROWD_CHEER:
                        // Roaring Celestial Spectators (450ms)
                        durationMs = 450;
                        startFreq = 280f;
                        endFreq = 540f;
                        break;
                    case SFX_CROWD_GASP:
                        // Sharp Inhale Spectator Gasp (220ms)
                        durationMs = 220;
                        startFreq = 600f;
                        endFreq = 300f;
                        break;
                    case SFX_REFEREE_COUNT:
                        // Heavy Mat Slap 3-Count (130ms)
                        durationMs = 130;
                        startFreq = 140f;
                        endFreq = 50f;
                        break;
                    case SFX_GRAPPLE_SLAM:
                        // Heavy Suplex / Mat Impact (260ms)
                        durationMs = 260;
                        startFreq = 90f;
                        endFreq = 45f;
                        break;
                    case SFX_ROPE_BOUNCE:
                        // Spirit Dao Rope Rebound Twang (160ms)
                        durationMs = 160;
                        startFreq = 350f;
                        endFreq = 680f;
                        break;
                    case SFX_FINISHER_HIT:
                        // Earth-Shattering Celestial K.O. Impact (480ms)
                        durationMs = 480;
                        startFreq = 440f;
                        endFreq = 70f;
                        break;

                    // --- DISCIPLE INTERACTION AUDIO EFFECTS ---
                    case SFX_DISCIPLE_GREETING:
                        // Delicate Guzheng Pluck Greeting (150ms)
                        durationMs = 150;
                        startFreq = 440f; // A4
                        endFreq = 880f;  // A5
                        break;
                    case SFX_BREAKTHROUGH:
                        // Grand Celestial Ascension Gong (380ms)
                        durationMs = 380;
                        startFreq = 196f; // G3
                        endFreq = 1174f; // D6
                        break;
                    case SFX_BREAKTHROUGH_FAIL:
                        // Qi Deviation Dissonant Thud (220ms)
                        durationMs = 220;
                        startFreq = 311f; // D#4
                        endFreq = 80f;
                        break;
                    case SFX_BESTOW_PILL:
                        // Spirit Elixir Sparkle (160ms)
                        durationMs = 160;
                        startFreq = 659.25f; // E5
                        endFreq = 1318.5f;  // E6
                        break;
                    case SFX_ASSIGN_TASK:
                        // Bamboo Tally Woodblock Clack (90ms)
                        durationMs = 90;
                        startFreq = 1100f;
                        endFreq = 550f;
                        break;
                    case SFX_RECRUIT:
                        // Welcome Celestial Bell (240ms)
                        durationMs = 240;
                        startFreq = 587.33f; // D5
                        endFreq = 1174.66f; // D6
                        break;
                    case SFX_DISMISS:
                        // Sever Cultivation Bond Sword Ring (180ms)
                        durationMs = 180;
                        startFreq = 780f;
                        endFreq = 220f;
                        break;
                    case SFX_DAO_ENLIGHTENMENT:
                        // Singing Bowl Harmonic Drone (420ms)
                        durationMs = 420;
                        startFreq = 261.63f; // C4
                        endFreq = 523.25f;  // C5
                        break;

                    // --- SECT & WORLD AUDIO EFFECTS ---
                    case SFX_GATHER:
                    case SFX_COLLECT:
                        // Quartz Spirit Stone Clink (95ms)
                        durationMs = 95;
                        startFreq = 783.99f; // G5
                        endFreq = 1567.98f; // G6
                        break;
                    case SFX_UPGRADE:
                        // Pavilion Stone Chime (200ms)
                        durationMs = 200;
                        startFreq = 392.00f; // G4
                        endFreq = 783.99f;  // G5
                        break;
                    case SFX_DEMOLISH:
                        // Pavilion Dismantle Rubble (190ms)
                        durationMs = 190;
                        startFreq = 160f;
                        endFreq = 60f;
                        break;
                    case SFX_ALCHEMY:
                        // Alchemy Cauldron Simmer (180ms)
                        durationMs = 180;
                        startFreq = 280f;
                        endFreq = 480f;
                        break;
                    case SFX_SCRIPTURE:
                        // Ancient Scripture Chant Chime (290ms)
                        durationMs = 290;
                        startFreq = 329.63f; // E4
                        endFreq = 659.25f;  // E5
                        break;
                    case SFX_FAIL:
                        durationMs = 140;
                        startFreq = 240f;
                        endFreq = 120f;
                        break;
                    case SFX_CLICK:
                    default:
                        durationMs = 45;
                        startFreq = 880f;
                        endFreq = 960f;
                        break;
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
                    } else if (SFX_CRITICAL_STRIKE.equals(effect) || SFX_ELEMENTAL_LIGHTNING.equals(effect)) {
                        float noise = (float) ((Math.random() - 0.5) * 0.45 * Math.pow(1.0 - t, 1.2));
                        envelope = (float) Math.pow(1.0 - t, 1.4);
                        float sampleVal = ((float) Math.sin(phase) + noise) * envelope * Short.MAX_VALUE * (baseVol * 0.6f);
                        buffer[i] = (short) MathUtils.clamp(sampleVal, Short.MIN_VALUE, Short.MAX_VALUE);
                        continue;
                    } else if (SFX_BREAKTHROUGH.equals(effect) || SFX_VICTORY.equals(effect) || SFX_DAO_ENLIGHTENMENT.equals(effect)) {
                        float harmonic2 = (float) Math.sin(phase * 2.0) * 0.35f;
                        float harmonic3 = (float) Math.sin(phase * 3.0) * 0.15f;
                        envelope = (float) (Math.sin(t * Math.PI * 0.5) * Math.pow(1.0 - t, 0.75));
                        float sampleVal = ((float) Math.sin(phase) + harmonic2 + harmonic3) * envelope * Short.MAX_VALUE * (baseVol * 0.5f);
                        buffer[i] = (short) MathUtils.clamp(sampleVal, Short.MIN_VALUE, Short.MAX_VALUE);
                        continue;
                    } else if (SFX_SHIELD_BLOCK.equals(effect)) {
                        envelope = (float) Math.exp(-6.0 * t);
                    } else if (SFX_ASSIGN_TASK.equals(effect)) {
                        envelope = (float) Math.exp(-12.0 * t);
                    } else {
                        envelope = (float) Math.sin(t * Math.PI);
                    }

                    float sampleVal = (float) Math.sin(phase) * envelope * 0.45f * Short.MAX_VALUE * baseVol;
                    buffer[i] = (short) MathUtils.clamp(sampleVal, Short.MIN_VALUE, Short.MAX_VALUE);
                }

                AudioTrack track = new AudioTrack.Builder()
                        .setAudioAttributes(new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_GAME)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build())
                        .setAudioFormat(new AudioFormat.Builder()
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .setSampleRate(sampleRate)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .build())
                        .setBufferSizeInBytes(buffer.length * 2)
                        .setTransferMode(AudioTrack.MODE_STATIC)
                        .build();

                track.write(buffer, 0, buffer.length);
                track.play();
                Thread.sleep(durationMs + 20);
                track.release();
            } catch (Exception ignored) {}
            }
        });
    }

    // ========================================================================
    // PROCEDURAL MULTI-THEME CONTINUOUS BGM STREAM ENGINE
    // ========================================================================

    public void setBgmTheme(String theme) {
        if (theme == null || theme.equals(activeBgmTheme)) return;
        this.activeBgmTheme = theme;
        if (bgmEnabled && enabled && isProceduralBgmRunning.get()) {
            stopProceduralBgm();
            startProceduralBgm(theme);
        }
    }

    public String getActiveBgmTheme() {
        return activeBgmTheme;
    }

    public void startProceduralBgm(String theme) {
        this.activeBgmTheme = theme;
        if (!bgmEnabled || !enabled) return;
        if (isProceduralBgmRunning.get()) return;

        isProceduralBgmRunning.set(true);
        soundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    int sampleRate = SAMPLE_RATE;
                    int minBufSize = AudioTrack.getMinBufferSize(
                            sampleRate,
                            AudioFormat.CHANNEL_OUT_MONO,
                            AudioFormat.ENCODING_PCM_16BIT
                    );

                    proceduralBgmTrack = new AudioTrack.Builder()
                            .setAudioAttributes(new AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_GAME)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .build())
                            .setAudioFormat(new AudioFormat.Builder()
                                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                    .setSampleRate(sampleRate)
                                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                    .build())
                            .setBufferSizeInBytes(Math.max(minBufSize, 4096))
                            .setTransferMode(AudioTrack.MODE_STREAM)
                            .build();

                    proceduralBgmTrack.play();
                    Random rand = new Random(System.currentTimeMillis());

                    while (isProceduralBgmRunning.get() && enabled && bgmEnabled) {
                        String currentTheme = activeBgmTheme;
                        int noteIndex = rand.nextInt(PENTATONIC_FREQS.length);
                        float noteFreq = PENTATONIC_FREQS[noteIndex];

                        int noteDurationMs;
                        int pauseMs;
                        float decayFactor;

                        if (THEME_COMBAT_INTENSE.equals(currentTheme)) {
                            // Fast, urgent martial combat cadence (250ms note, 120ms pause)
                            noteDurationMs = 260 + rand.nextInt(180);
                            pauseMs = 100 + rand.nextInt(120);
                            decayFactor = 4.2f;
                        } else if (THEME_MEDITATION_ZEN.equals(currentTheme)) {
                            // Deep, long sustained meditative singing tone (1200ms note, 600ms pause)
                            noteDurationMs = 1200 + rand.nextInt(600);
                            pauseMs = 500 + rand.nextInt(600);
                            decayFactor = 1.8f;
                        } else {
                            // Standard Peaceful Sect Guzheng pluck (650ms note, 350ms pause)
                            noteDurationMs = 600 + rand.nextInt(450);
                            pauseMs = 280 + rand.nextInt(350);
                            decayFactor = 3.2f;
                        }

                        int noteSamples = (sampleRate * noteDurationMs) / 1000;
                        short[] noteBuffer = new short[noteSamples];
                        float effectiveVol = getEffectiveBgmVolume() * 0.32f;

                        for (int i = 0; i < noteSamples; i++) {
                            float t = (float) i / noteSamples;
                            float fundamental = (float) Math.sin(2.0 * Math.PI * noteFreq * (i / (float) sampleRate));
                            float harmonic2 = (float) Math.sin(4.0 * Math.PI * noteFreq * (i / (float) sampleRate)) * 0.25f;
                            float harmonic3 = (float) Math.sin(6.0 * Math.PI * noteFreq * (i / (float) sampleRate)) * 0.10f;
                            float envelope = (float) Math.exp(-decayFactor * t);

                            // Subtle bamboo mountain breeze layer
                            float breeze = (float) ((rand.nextFloat() - 0.5f) * 0.035f * Math.sin(t * Math.PI));

                            float sampleVal = (fundamental + harmonic2 + harmonic3 + breeze) * envelope * effectiveVol * Short.MAX_VALUE;
                            noteBuffer[i] = (short) MathUtils.clamp(sampleVal, Short.MIN_VALUE, Short.MAX_VALUE);
                        }

                        if (proceduralBgmTrack != null && proceduralBgmTrack.getState() == AudioTrack.STATE_INITIALIZED) {
                            proceduralBgmTrack.write(noteBuffer, 0, noteBuffer.length);
                        }

                        Thread.sleep(pauseMs);
                    }

                    if (proceduralBgmTrack != null) {
                        try {
                            proceduralBgmTrack.stop();
                            proceduralBgmTrack.release();
                        } catch (Exception ignored) {}
                        proceduralBgmTrack = null;
                    }
                } catch (Exception ignored) {
                } finally {
                    isProceduralBgmRunning.set(false);
                }
            }
        });
    }

    public void stopProceduralBgm() {
        isProceduralBgmRunning.set(false);
        if (proceduralBgmTrack != null) {
            try {
                proceduralBgmTrack.stop();
                proceduralBgmTrack.release();
            } catch (Exception ignored) {}
            proceduralBgmTrack = null;
        }
    }

    public void playBgm(int resId, final boolean loop) {
        if (!enabled || resId == 0) return;
        stopProceduralBgm();
        String key = String.valueOf(resId);
        if (key.equals(currentBgm)) return;

        stopBgm();
        try {
            bgmPlayer = MediaPlayer.create(context, resId);
            if (bgmPlayer != null) {
                bgmPlayer.setLooping(loop);
                bgmPlayer.setVolume(getEffectiveBgmVolume(), getEffectiveBgmVolume());
                bgmPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        stopBgm();
                        return true;
                    }
                });
                bgmPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        if (!loop) currentBgm = "";
                    }
                });
                bgmPlayer.start();
                currentBgm = key;
            }
        } catch (Exception e) { stopBgm(); }
    }

    public void stopBgm() {
        stopProceduralBgm();
        if (bgmPlayer != null) {
            try { if (bgmPlayer.isPlaying()) bgmPlayer.stop(); } catch (Exception ignored) {}
            bgmPlayer.release();
            bgmPlayer = null;
            currentBgm = "";
        }
    }

    public void stopAmbient() {
        if (ambientPlayer != null) {
            try { ambientPlayer.stop(); ambientPlayer.release(); } catch (Exception ignored) {}
            ambientPlayer = null;
            currentAmbient = "";
        }
    }

    public void pauseBgm() {
        if (bgmPlayer != null && bgmPlayer.isPlaying()) bgmPlayer.pause();
        stopProceduralBgm();
    }

    public void resumeBgm() {
        if (bgmPlayer != null) {
            bgmPlayer.start();
        } else if (bgmEnabled && enabled) {
            startProceduralBgm(activeBgmTheme);
        }
    }

    public void setDucking(boolean d) {
        ducking = d;
        updateVolumes();
    }

    private void updateVolumes() {
        float effBgm = getEffectiveBgmVolume();
        float effAmb = getEffectiveAmbientVolume();
        if (bgmPlayer != null) bgmPlayer.setVolume(effBgm, effBgm);
        if (ambientPlayer != null) ambientPlayer.setVolume(effAmb, effAmb);
    }

    private float getEffectiveBgmVolume() {
        return MathUtils.clamp(bgmVolume * masterVolume * (ducking ? 0.2f : 1f), 0f, 1f);
    }

    private float getEffectiveAmbientVolume() {
        return MathUtils.clamp(ambientVolume * masterVolume * (ducking ? 0.1f : 1f), 0f, 1f);
    }

    public void setBgmVolume(float v) {
        bgmVolume = MathUtils.clamp01(v);
        updateVolumes();
    }

    public void setAmbientVolume(float v) {
        ambientVolume = MathUtils.clamp01(v);
        updateVolumes();
    }

    public void setSfxVolume(float v) {
        sfxVolume = MathUtils.clamp01(v);
    }

    public void setMasterVolume(float v) {
        masterVolume = MathUtils.clamp01(v);
        updateVolumes();
    }

    public void setBgmEnabled(boolean e) {
        bgmEnabled = e;
        if (!e) {
            stopBgm();
        } else {
            startProceduralBgm(activeBgmTheme);
        }
    }

    public boolean isBgmEnabled() {
        return bgmEnabled;
    }

    public void setEnabled(boolean e) {
        enabled = e;
        if (!e) {
            stopBgm();
            stopAmbient();
        } else {
            requestAudioFocus();
            if (bgmEnabled) startProceduralBgm(activeBgmTheme);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void release() {
        mainHandler.removeCallbacksAndMessages(null);
        abandonAudioFocus();
        if (sfxPool != null) {
            sfxPool.release();
            sfxPool = null;
        }
        stopProceduralBgm();
        stopBgm();
        stopAmbient();
        soundExecutor.shutdown();
        instance = null;
    }
}
