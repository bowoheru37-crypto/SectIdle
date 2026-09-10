package com.example;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sect.idle.core.GameConfig;
import com.sect.idle.gameplay.HobbySystem;
import com.sect.idle.gameplay.SaveManager;
import com.sect.idle.gameplay.SectData;
import com.sect.idle.gameplay.TalentSystem;
import com.sect.idle.models.Building;
import com.sect.idle.models.Disciple;
import com.sect.idle.systems.AudioManager;
import com.sect.idle.systems.RNG;
import com.sect.idle.ui.GameActivity;

import java.util.Random;

/**
 * MainActivity - Xianxia Donghua Immortal Cultivation Splash & Launcher Activity.
 * Designed strictly to meet Sketchware Pro v7.0.0 & Pure Java 7 standards:
 * - Optimal, stable, lightweight, zero external library bloat (native android.app.Activity).
 * - Immersive fullscreen with glowing Qi aura animations and artifact levitation.
 * - Dynamic Chinese-English Xianxia breakthrough progress bar (炼气 -> 筑基 -> 金丹 -> 元婴 -> 飞升).
 * - Asynchronous safe state initialization & audio pre-warming.
 */
public class MainActivity extends Activity {

    private View viewAura;
    private View viewAuraInner;
    private ImageView imageview1;
    private ImageView imageviewGlowBg;
    private TextView tvTitle;
    private TextView tvSubtitle;
    private TextView tvRealmStage;
    private ProgressBar progressBar1;
    private TextView textview1;
    private TextView tvTip;

    private boolean isDestroyed = false;
    private Handler mainHandler;
    private Handler auraHandler;
    private Runnable auraRunnable;

    private ValueAnimator floatAnimator;
    private ObjectAnimator rotateAnimator;

    private static final String[] XIANXIA_TIPS = new String[] {
            "Tip: Assign high-Alchemy disciples to the Alchemy Furnace to craft Spirit Pills.",
            "Tip: Higher cultivation realms multiply disciple combat prowess & spirit stone gathering.",
            "Tip: Build and upgrade Dragon Veins to increase sect passive spiritual luck.",
            "Tip: Disciple elemental affinities grant powerful resonance in Martial Arena tournaments.",
            "Tip: Daily breakthrough attempts during nocturnal hours yield doubled comprehension.",
            "Tip: Balanced sect economy ensures disciples maintain high loyalty and low wage unrest."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fullscreen & Immersive Xianxia Window Configuration
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        setContentView(R.layout.main);

        initializeViews();
        initializeAnimations();
        initializeLogic();
    }

    private void initializeViews() {
        viewAura = findViewById(R.id.view_aura);
        viewAuraInner = findViewById(R.id.view_aura_inner);
        imageview1 = findViewById(R.id.imageview1);
        imageviewGlowBg = findViewById(R.id.imageview_glow_bg);
        tvTitle = findViewById(R.id.tv_title);
        tvSubtitle = findViewById(R.id.tv_subtitle);
        tvRealmStage = findViewById(R.id.tv_realm_stage);
        progressBar1 = findViewById(R.id.progressBar1);
        textview1 = findViewById(R.id.textview1);
        tvTip = findViewById(R.id.tv_tip);

        mainHandler = new Handler(Looper.getMainLooper());
        auraHandler = new Handler(Looper.getMainLooper());

        if (progressBar1 != null) {
            progressBar1.setMax(100);
            progressBar1.setProgress(0);
        }

        if (tvTip != null) {
            Random rand = new Random();
            tvTip.setText(XIANXIA_TIPS[rand.nextInt(XIANXIA_TIPS.length)]);
        }
    }

    private void initializeAnimations() {
        if (imageview1 != null) {
            floatAnimator = ValueAnimator.ofFloat(0f, -18f, 0f);
            floatAnimator.setDuration(2400);
            floatAnimator.setRepeatCount(ValueAnimator.INFINITE);
            floatAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            floatAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (isDestroyed || imageview1 == null) return;
                    float val = ((Float) animation.getAnimatedValue()).floatValue();
                    imageview1.setTranslationY(val);
                    if (imageviewGlowBg != null) {
                        imageviewGlowBg.setTranslationY(val);
                    }
                }
            });
            floatAnimator.start();
        }

        if (viewAura != null) {
            rotateAnimator = ObjectAnimator.ofFloat(viewAura, "rotation", 0f, 360f);
            rotateAnimator.setDuration(12000);
            rotateAnimator.setRepeatCount(ValueAnimator.INFINITE);
            rotateAnimator.setInterpolator(new LinearInterpolator());
            rotateAnimator.start();
        }

        auraRunnable = new Runnable() {
            @Override
            public void run() {
                if (isDestroyed) return;

                if (viewAura != null) {
                    viewAura.animate()
                            .scaleX(1.18f)
                            .scaleY(1.18f)
                            .alpha(0.65f)
                            .setDuration(1800)
                            .setInterpolator(new AccelerateDecelerateInterpolator())
                            .withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    if (isDestroyed || viewAura == null) return;
                                    viewAura.animate()
                                            .scaleX(1.0f)
                                            .scaleY(1.0f)
                                            .alpha(0.35f)
                                            .setDuration(1800)
                                            .setInterpolator(new AccelerateDecelerateInterpolator())
                                            .start();
                                }
                            }).start();
                }

                if (viewAuraInner != null) {
                    viewAuraInner.animate()
                            .scaleX(1.25f)
                            .scaleY(1.25f)
                            .alpha(0.5f)
                            .setDuration(1400)
                            .setInterpolator(new AccelerateDecelerateInterpolator())
                            .withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    if (isDestroyed || viewAuraInner == null) return;
                                    viewAuraInner.animate()
                                            .scaleX(0.95f)
                                            .scaleY(0.95f)
                                            .alpha(0.2f)
                                            .setDuration(1400)
                                            .setInterpolator(new AccelerateDecelerateInterpolator())
                                            .start();
                                }
                            }).start();
                }

                if (!isDestroyed && auraHandler != null) {
                    auraHandler.postDelayed(this, 3700);
                }
            }
        };
        auraHandler.post(auraRunnable);
    }

    private void initializeLogic() {
        final int[] progress = {0};
        final Context context = getApplicationContext();

        // Background thread for non-blocking save file loading & sect data initialization
        new Thread(new Runnable() {
            @Override
            public void run() {
                final SectData data = SectData.getInstance();
                final SaveManager sm = new SaveManager(context);
                final boolean loaded = sm.load();

                // Pre-warm AudioManager synthesis in background to ensure zero lag on game start
                try {
                    AudioManager.getInstance(context);
                } catch (Exception ignored) {}

                if (!loaded) {
                    data.reset();
                    String[] names = {"Li Yun", "Zhang Wei", "Wang Fang", "Chen Ming", "Liu Hua", "Zhao Kai", "Sun Mei", "Wu Jian"};
                    for (int i = 0; i < 3; i++) {
                        Disciple d = new Disciple();
                        d.name = RNG.pick(names);
                        d.isMale = RNG.chance(50);
                        d.initStats(
                                RNG.nextInt(15, 45),
                                RNG.nextInt(15, 45),
                                RNG.nextInt(15, 45),
                                RNG.nextInt(10, 35),
                                RNG.nextInt(10, 35),
                                RNG.nextInt(10, 35),
                                RNG.nextInt(10, 30)
                        );
                        d.realm = RNG.nextInt(2);
                        d.realmExp = RNG.nextInt(500);
                        d.element = RNG.nextInt(11);
                        d.personality = RNG.nextInt(15);
                        TalentSystem.generateTalent(d);
                        HobbySystem.assignRandomHobby(d);
                        d.dailyWage = d.getExpectedWage();
                        d.recalcCombat();

                        if (i == 0) d.currentTask = GameConfig.TASK_FARMING;
                        else if (i == 1) d.currentTask = GameConfig.TASK_ALCHEMY;
                        else d.currentTask = GameConfig.TASK_GUARD;

                        d.updateEfficiency();
                        data.addDisciple(d);
                    }

                    if (data.buildings != null && !data.buildings.isEmpty()) {
                        Building mainHall = data.buildings.get(0);
                        if (mainHall != null) {
                            mainHall.isBuilt = true;
                        }
                    }
                    data.recalculateEconomy();
                }

                // Smooth progressive loading on UI thread
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (isDestroyed) return;
                        progress[0] += 5;
                        if (progress[0] > 100) progress[0] = 100;

                        if (progressBar1 != null) {
                            progressBar1.setProgress(progress[0]);
                        }

                        updateProgressText(progress[0]);

                        if (progress[0] < 100) {
                            mainHandler.postDelayed(this, 50);
                        } else {
                            // Loading complete: smooth intent transition to GameActivity
                            launchGameScene();
                        }
                    }
                });
            }
        }, "SectInitializerThread").start();
    }

    private void updateProgressText(int p) {
        if (textview1 != null) {
            textview1.setText("Loading Sect Data... " + p + "%");
        }

        if (tvRealmStage != null) {
            if (p < 25) {
                tvRealmStage.setText("✦ Qi Condensation (炼气期) · Gathering Heaven & Earth Qi ✦");
            } else if (p < 50) {
                tvRealmStage.setText("✦ Foundation Establishment (筑基期) · Opening Meridians ✦");
            } else if (p < 75) {
                tvRealmStage.setText("✦ Core Formation (金丹期) · Refining Sect Dragon Veins ✦");
            } else if (p < 95) {
                tvRealmStage.setText("✦ Nascent Soul (元婴期) · Harmonizing Dao Laws ✦");
            } else {
                tvRealmStage.setText("✦ Immortal Ascension (飞升成仙) · Sect Gates Opening! ✦");
            }
        }
    }

    private void launchGameScene() {
        if (isDestroyed) return;

        try {
            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        } catch (Exception e) {
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (auraHandler != null && auraRunnable != null) {
            auraHandler.removeCallbacks(auraRunnable);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (auraHandler != null && auraRunnable != null) {
            auraHandler.post(auraRunnable);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isDestroyed = true;

        if (floatAnimator != null) {
            floatAnimator.cancel();
            floatAnimator.removeAllUpdateListeners();
            floatAnimator = null;
        }

        if (rotateAnimator != null) {
            rotateAnimator.cancel();
            rotateAnimator = null;
        }

        if (auraHandler != null) {
            auraHandler.removeCallbacksAndMessages(null);
            auraHandler = null;
        }

        if (mainHandler != null) {
            mainHandler.removeCallbacksAndMessages(null);
            mainHandler = null;
        }
    }
}
