package com.sect.idle.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import com.sect.idle.gameplay.BattleEngine;
import com.sect.idle.models.BattleUnit;
import android.graphics.Path;
import com.sect.idle.systems.AudioManager;
import com.sect.idle.systems.CameraSystem;
import com.sect.idle.systems.RNG;
import java.util.ArrayList;

/**
 * BattleScene v6.0.1 - Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class BattleScene {
    private final Context context;
    private BattleEngine battle;

    // Pre-allocated paints
    private final Paint bgPaint;
    private final Paint arenaPaint;
    private final Paint unitPaint;
    private final Paint hpPaint;
    private final Paint hpBgPaint;
    private final Paint mpPaint;
    private final Paint mpBgPaint;
    private final Paint actionPaint;
    private final Paint logBgPaint;
    private final Paint textPaint;
    private final Paint shadowPaint;
    private final Paint selectionPaint;
    private final Paint damagePaint;
    private final Paint glowPaint;
    private final Paint particlePaint;
    private final Paint flashPaint;

    // 4 Action Buttons (80px wide) - Idle Sect v2.0
    private final RectF btnAttackRect = new RectF();
    private final RectF btnSkillRect = new RectF();
    private final RectF btnItemRect = new RectF();
    private final RectF btnFleeRect = new RectF();

    // Reusable rect
    private final RectF rectPool;
    private final Rect srcRect;
    private final Rect dstRect;

    // Constants
    private static final float UNIT_W = 70f;
    private static final float UNIT_H = 70f;
    private static final float HP_H = 8f;
    private static final float MP_H = 4f;
    private static final float ACTION_H = 4f;
    private static final float PADDING = 16f;
    private static final int LOG_LINES = 10;
    private static final float LOG_LINE_H = 18f;
    private static final float LOG_BG_HEIGHT = 200f;
    private static final int TEAM_PLAYER_COLOR = 0xFF448AFF;
    private static final int TEAM_ENEMY_COLOR = 0xFFFF5252;
    private static final int DEAD_COLOR = 0xFF555555;
    private static final int HP_COLOR = 0xFF4CAF50;
    private static final int HP_LOW_COLOR = 0xFFFF5722;
    private static final int MP_COLOR = 0xFF2196F3;
    private static final int ACTION_COLOR = 0xFFFFD700;
    private static final int BG_COLOR = 0xFF0A0A12;
    private static final int LOG_BG_COLOR = 0xDD000000; 
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int SHADOW_COLOR = 0x80000000;
    private static final float NAME_OFFSET = 12f;
    private static final float BAR_GAP = 3f;

    // Animation
    private float animTime = 0f;
    private int selectedUnit = -1;

    // Assets
    private Bitmap atlas;
    private Bitmap battleSheet;
    private Bitmap effectSheet;

    // Screen shake & flash
    private float shakeIntensity = 0f;
    private float shakeDecay = 0.85f;
    private float flashIntensity = 0f;
    private int flashColor = 0xFFFFFFFF;

    // Particle system (pooled)
    private static final int MAX_PARTICLES = 60;
    private final float[] ptX, ptY, ptVX, ptVY, ptLife, ptMaxLife, ptSize;
    private final int[] ptColor;
    private int activeParticles = 0;

    // Floating damage texts
    private static final int MAX_FLOATING = 12;
    private final String[] ftText;
    private final float[] ftX, ftY, ftLife, ftMaxLife;
    private final int[] ftColor;
    private int activeFloating = 0;


    // Combat Simulation Controls
    private float simulationSpeed = 1.0f;
    private boolean autoBattle = true;
    private boolean isPaused = false;
    private final RectF btnAutoRect = new RectF();
    private final RectF btnSpeedRect = new RectF();
    private final RectF btnPauseRect = new RectF();
    private final Paint buttonBgPaint;
    private final Paint buttonTextPaint;
    private final StringBuilder stringBuilder = new StringBuilder(32);

    // Sword Slash Ribbon & Spark System
    private static final int MAX_SLASH_POINTS = 32;
    private final float[] slashX = new float[MAX_SLASH_POINTS];
    private final float[] slashY = new float[MAX_SLASH_POINTS];
    private final float[] slashLife = new float[MAX_SLASH_POINTS];
    private final float[] slashWidth = new float[MAX_SLASH_POINTS];
    private int slashHead = 0;
    private int slashCount = 0;
    private final Path slashPathOuter = new Path();
    private final Path slashPathCore = new Path();
    private final Paint slashGlowPaint;
    private final Paint slashCorePaint;
    private final Paint slashSparkPaint;
    // Spirit Aura Buffer Renderer
    private final com.sect.idle.render.SpiritAuraBufferRenderer auraRenderer;
    private final java.util.HashMap<String, Bitmap> unitSpriteCache = new java.util.HashMap<String, Bitmap>();

    private Bitmap getUnitSprite(BattleUnit u) {
        if (u == null) return null;
        String key = (u.source != null && u.source.id != null ? u.source.id : u.name) + "_" + u.team;
        Bitmap cached = unitSpriteCache.get(key);
        if (cached == null || cached.isRecycled()) {
            try {
                if (u.team == 0) {
                    if (u.name != null && u.name.contains("Zhao Kai")) {
                        cached = com.sect.idle.procedural.IdleSectAssetGenerator.createZhaoKaiChibi(80);
                    } else {
                        cached = com.sect.idle.procedural.IdleSectAssetGenerator.createSectGuardianChibi(80);
                    }
                } else {
                    cached = com.sect.idle.procedural.IdleSectAssetGenerator.createDemonicWolf(80);
                }
                if (cached != null) {
                    unitSpriteCache.put(key, cached);
                }
            } catch (OutOfMemoryError oom) {
                return null;
            }
        }
        return cached;
    }

    public interface BattleListener {
        void onBattleEnded(boolean victory);
    }
    private BattleListener battleListener;
    public void setBattleListener(BattleListener l) { this.battleListener = l; }

    private final Paint logBgGradPaint;
    private final Paint resultButtonBgPaint;
    private final Paint resultButtonTextPaint;
    private final RectF resultButtonRect;

    public BattleScene(Context ctx) {
        this.context = ctx;
        this.rectPool = new RectF();
        this.srcRect = new Rect();
        this.dstRect = new Rect();
        this.resultButtonRect = new RectF();
        this.auraRenderer = new com.sect.idle.render.SpiritAuraBufferRenderer();

        bgPaint = new Paint();
        bgPaint.setColor(BG_COLOR);

        arenaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arenaPaint.setShader(new LinearGradient(0, 0, 0, 1280,
            new int[]{0xFF1A0F2E, 0xFF0A0A12, 0xFF1A0F2E},
            new float[]{0f, 0.5f, 1f}, Shader.TileMode.CLAMP));

        unitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        hpPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hpBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hpBgPaint.setColor(0xFF333333);
        mpPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mpPaint.setColor(MP_COLOR);
        mpBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mpBgPaint.setColor(0xFF333333);

        actionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        actionPaint.setColor(ACTION_COLOR);

        logBgPaint = new Paint();
        logBgPaint.setColor(LOG_BG_COLOR);
        logBgPaint.setStyle(Paint.Style.FILL);

        logBgGradPaint = new Paint();
        logBgGradPaint.setColor(0xDD0A0A18);

        resultButtonBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        resultButtonBgPaint.setColor(0xEE1E1435);

        resultButtonTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        resultButtonTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        resultButtonTextPaint.setTextAlign(Paint.Align.CENTER);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(TEXT_COLOR);
        textPaint.setTextSize(14f);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);

        shadowPaint = new Paint();
        shadowPaint.setColor(SHADOW_COLOR);
        shadowPaint.setStyle(Paint.Style.FILL);

        selectionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectionPaint.setColor(0xFFFFFF00);
        selectionPaint.setStyle(Paint.Style.STROKE);
        selectionPaint.setStrokeWidth(3f);

        damagePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        damagePaint.setTypeface(Typeface.DEFAULT_BOLD);
        damagePaint.setTextAlign(Paint.Align.CENTER);
        damagePaint.setShadowLayer(2f, 1f, 1f, 0xFF000000);

        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setStyle(Paint.Style.STROKE);

        particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        flashPaint = new Paint();
        flashPaint.setColor(0xFFFFFFFF);

        ptX = new float[MAX_PARTICLES]; ptY = new float[MAX_PARTICLES];
        ptVX = new float[MAX_PARTICLES]; ptVY = new float[MAX_PARTICLES];
        ptLife = new float[MAX_PARTICLES]; ptMaxLife = new float[MAX_PARTICLES];
        ptSize = new float[MAX_PARTICLES]; ptColor = new int[MAX_PARTICLES];

        ftText = new String[MAX_FLOATING];
        ftX = new float[MAX_FLOATING]; ftY = new float[MAX_FLOATING];
        ftLife = new float[MAX_FLOATING]; ftMaxLife = new float[MAX_FLOATING];
        ftColor = new int[MAX_FLOATING];

        buttonBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        buttonBgPaint.setColor(0xFF263238);

        buttonTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        buttonTextPaint.setColor(0xFFFFFFFF);
        buttonTextPaint.setTextSize(12f);
        buttonTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        buttonTextPaint.setTextAlign(Paint.Align.CENTER);

        slashGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        slashGlowPaint.setStyle(Paint.Style.STROKE);
        slashGlowPaint.setStrokeCap(Paint.Cap.ROUND);
        slashGlowPaint.setStrokeJoin(Paint.Join.ROUND);

        slashCorePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        slashCorePaint.setStyle(Paint.Style.STROKE);
        slashCorePaint.setStrokeCap(Paint.Cap.ROUND);
        slashCorePaint.setStrokeJoin(Paint.Join.ROUND);
        slashCorePaint.setColor(0xFFFFFFFF);

        slashSparkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        slashSparkPaint.setStyle(Paint.Style.FILL);

    }

    public void setAtlas(Bitmap atlas, Bitmap battleSheet, Bitmap effectSheet) {
        this.atlas = atlas;
        this.battleSheet = battleSheet;
        this.effectSheet = effectSheet;
    }

    public void setBattle(BattleEngine b) { this.battle = b; }

    public void update(float dt) {
        if (isPaused) return;

        float simDt = dt * simulationSpeed;
        if (battle != null && battle.isRunning) {
            if (autoBattle) {
                int steps = Math.max(1, (int) simulationSpeed);
                for (int s = 0; s < steps; s++) {
                    battle.tick();
                    if (!battle.isRunning) break;
                }
            }
        }
        animTime += simDt;

        if (slashCount > 0) {
            for (int i = 0; i < slashCount; i++) {
                int idx = (slashHead - slashCount + i + MAX_SLASH_POINTS) % MAX_SLASH_POINTS;
                slashLife[idx] -= dt * 3.5f;
            }
            while (slashCount > 0) {
                int tailIdx = (slashHead - slashCount + MAX_SLASH_POINTS) % MAX_SLASH_POINTS;
                if (slashLife[tailIdx] <= 0f) {
                    slashCount--;
                } else {
                    break;
                }
            }
        }

        shakeIntensity *= shakeDecay;
        if (shakeIntensity < 0.1f) shakeIntensity = 0f;
        flashIntensity *= 0.9f;
        if (flashIntensity < 0.01f) flashIntensity = 0f;

        updateParticles(simDt);
        updateFloatingTexts(simDt);
    }

    public void addSlashPoint(float x, float y) {
        int idx = slashHead;
        slashX[idx] = x;
        slashY[idx] = y;
        slashLife[idx] = 1.0f;
        slashWidth[idx] = 12f;
        slashHead = (slashHead + 1) % MAX_SLASH_POINTS;
        if (slashCount < MAX_SLASH_POINTS) slashCount++;

        spawnHitSparks(x, y, 0xFF00E5FF);
        try {
            AudioManager.getInstance(context).playSfx(AudioManager.SFX_SWORD_SPAR);
        } catch (Throwable ignored) {}
    }

    public void spawnHitSparks(float x, float y, int color) {
        spawnHitEffect(x, y, color);
    }

    public void spawnFloatingDamage(float x, float y, int amount, boolean isCrit) {
        spawnDamageText(x, y, isCrit ? ("CRIT -" + amount) : ("-" + amount), isCrit ? 0xFFFFD700 : 0xFFFF5252);
    }

    private void updateParticles(float dt) {
        for (int i = activeParticles - 1; i >= 0; i--) {
            ptLife[i] -= dt;
            if (ptLife[i] <= 0) {
                if (i < activeParticles - 1) {
                    ptX[i] = ptX[activeParticles-1]; ptY[i] = ptY[activeParticles-1];
                    ptVX[i] = ptVX[activeParticles-1]; ptVY[i] = ptVY[activeParticles-1];
                    ptLife[i] = ptLife[activeParticles-1]; ptMaxLife[i] = ptMaxLife[activeParticles-1];
                    ptSize[i] = ptSize[activeParticles-1]; ptColor[i] = ptColor[activeParticles-1];
                }
                activeParticles--;
                continue;
            }
            ptX[i] += ptVX[i] * dt;
            ptY[i] += ptVY[i] * dt;
            ptVY[i] += 15f * dt;
        }
    }

    private void updateFloatingTexts(float dt) {
        for (int i = activeFloating - 1; i >= 0; i--) {
            ftLife[i] -= dt;
            ftY[i] -= 40f * dt;
            if (ftLife[i] <= 0) {
                if (i < activeFloating - 1) {
                    ftText[i] = ftText[activeFloating-1];
                    ftX[i] = ftX[activeFloating-1]; ftY[i] = ftY[activeFloating-1];
                    ftLife[i] = ftLife[activeFloating-1]; ftMaxLife[i] = ftMaxLife[activeFloating-1];
                    ftColor[i] = ftColor[activeFloating-1];
                }
                activeFloating--;
            }
        }
    }

    public void spawnDamageText(float x, float y, String text, int color) {
        if (activeFloating >= MAX_FLOATING) return;
        int i = activeFloating++;
        ftText[i] = text; ftX[i] = x; ftY[i] = y;
        ftColor[i] = color; ftLife[i] = 1.5f; ftMaxLife[i] = 1.5f;
    }

    public void spawnHitEffect(float x, float y, int color) {
        if (activeParticles >= MAX_PARTICLES) return;
        for (int j = 0; j < 8 && activeParticles < MAX_PARTICLES; j++) {
            int i = activeParticles++;
            float angle = RNG.nextFloat() * 6.283f;
            float speed = 30f + RNG.nextFloat() * 70f;
            ptX[i] = x; ptY[i] = y;
            ptVX[i] = (float)Math.cos(angle) * speed;
            ptVY[i] = (float)Math.sin(angle) * speed;
            ptSize[i] = 2f + RNG.nextFloat() * 3f;
            ptColor[i] = color;
            ptLife[i] = 0.3f + RNG.nextFloat() * 0.5f;
            ptMaxLife[i] = ptLife[i];
        }
        shakeIntensity = 5f;
    }

    public void triggerFlash(int color) {
        flashColor = color;
        flashIntensity = 0.6f;
    }

    public void render(Canvas canvas, Paint paint, CameraSystem cam) {
        int w = canvas.getWidth();
        int h = canvas.getHeight();

        canvas.save();
        if (shakeIntensity > 0) {
            canvas.translate(
                (RNG.nextFloat() * 2f - 1f) * shakeIntensity,
                (RNG.nextFloat() * 2f - 1f) * shakeIntensity
            );
        }

        canvas.drawRect(-10, -10, w+10, h+10, bgPaint);
        canvas.drawRect(0, 0, w, h, arenaPaint);

        if (battle == null) { canvas.restore(); return; }

        ArrayList<BattleUnit> units = battle.getUnits();
        int size = units.size();
        if (size == 0) { canvas.restore(); return; }

        if (auraRenderer != null) {
            auraRenderer.updateBufferSize(w, h);
            auraRenderer.renderSpiritAuras(canvas, units, animTime, w, h);
        }

        float cy = h * 0.45f;
        float spacing = UNIT_H + 30f;

        for (int i = 0; i < size; i++) {
            BattleUnit u = units.get(i);
            boolean isLeft = u.team == 0;
            float x = isLeft ? PADDING * 2f : w - PADDING * 2f - UNIT_W;
            float baseY = cy + (i - size * 0.5f) * spacing;
            float y = baseY + (u.isAlive ? (float)Math.sin(animTime * 3f + i) * 3f : 0f);
            renderUnit(canvas, u, x, y, i == selectedUnit, isLeft);
        }

        renderBattleInfo(canvas, w, h);
        renderLog(canvas, w, h);
        renderSwordSlashes(canvas);
        renderParticles(canvas);
        renderFloatingTexts(canvas);
        renderControlButtons(canvas, w, h);

        if (flashIntensity > 0) {
            flashPaint.setColor(flashColor);
            flashPaint.setAlpha((int)(flashIntensity * 180));
            canvas.drawRect(-20, -20, w+20, h+20, flashPaint);
            flashPaint.setAlpha(255);
        }

        canvas.restore();
    }

    private void renderUnit(Canvas canvas, BattleUnit u, float x, float y, boolean selected, boolean facingRight) {
        rectPool.set(x + 5f, y + 5f, x + UNIT_W + 5f, y + UNIT_H + 5f);
        canvas.drawRect(rectPool, shadowPaint);

        if (battleSheet != null && !battleSheet.isRecycled()) {
            int frame = (int)(animTime * 4f) % 4;
            int srcX = frame * 120;
            srcRect.set(srcX, 0, srcX + 120, 120);

            float spriteW = UNIT_W * 1.2f;
            float spriteH = UNIT_H * 1.2f;
            float sx = x + UNIT_W * 0.5f - spriteW * 0.5f;
            float sy = y + UNIT_H - spriteH;

            if (!facingRight) {
                canvas.save();
                canvas.scale(-1f, 1f, x + UNIT_W * 0.5f, y + UNIT_H * 0.5f);
                dstRect.set((int)sx, (int)sy, (int)(sx + spriteW), (int)(sy + spriteH));
                canvas.drawBitmap(battleSheet, srcRect, dstRect, unitPaint);
                canvas.restore();
            } else {
                dstRect.set((int)sx, (int)sy, (int)(sx + spriteW), (int)(sy + spriteH));
                canvas.drawBitmap(battleSheet, srcRect, dstRect, unitPaint);
            }
        } else {
            Bitmap uSprite = getUnitSprite(u);
            if (uSprite != null && !uSprite.isRecycled() && u.isAlive) {
                if (!facingRight) {
                    canvas.save();
                    canvas.scale(-1f, 1f, x + UNIT_W * 0.5f, y + UNIT_H * 0.5f);
                    dstRect.set((int)x, (int)y, (int)(x + UNIT_W), (int)(y + UNIT_H));
                    canvas.drawBitmap(uSprite, null, dstRect, unitPaint);
                    canvas.restore();
                } else {
                    dstRect.set((int)x, (int)y, (int)(x + UNIT_W), (int)(y + UNIT_H));
                    canvas.drawBitmap(uSprite, null, dstRect, unitPaint);
                }
            } else {
                int bodyColor = !u.isAlive ? DEAD_COLOR : (u.team == 0 ? TEAM_PLAYER_COLOR : TEAM_ENEMY_COLOR);
                unitPaint.setColor(bodyColor);
                rectPool.set(x, y, x + UNIT_W, y + UNIT_H);
                canvas.drawRoundRect(rectPool, 8f, 8f, unitPaint);
            }
        }

        if (selected) {
            rectPool.set(x - 3f, y - 3f, x + UNIT_W + 3f, y + UNIT_H + 3f);
            canvas.drawRoundRect(rectPool, 10f, 10f, selectionPaint);
        }

        textPaint.setTextSize(12f);
        textPaint.setColor(TEXT_COLOR);
        textPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(u.name, x + UNIT_W * 0.5f, y - NAME_OFFSET, textPaint);

        if (u.source != null) {
            textPaint.setTextSize(10f);
            textPaint.setColor(0xFFAAAAAA);
            canvas.drawText("Lv." + u.source.realm, x + UNIT_W * 0.5f, y + UNIT_H + 12f, textPaint);
        }

        float barY = y + UNIT_H + 18f;

        // Rounded HP Bar
        rectPool.set(x, barY, x + UNIT_W, barY + HP_H);
        canvas.drawRoundRect(rectPool, 4f, 4f, hpBgPaint);

        float hpPercent = u.maxHp > 0 ? Math.max(0f, Math.min(1f, u.hp / (float)u.maxHp)) : 0f;
        float barW = UNIT_W * hpPercent;
        if (barW > 0) {
            hpPaint.setColor(hpPercent < 0.3f ? HP_LOW_COLOR : HP_COLOR);
            rectPool.set(x, barY, x + barW, barY + HP_H);
            canvas.drawRoundRect(rectPool, 4f, 4f, hpPaint);
        }

        textPaint.setTextSize(9f);
        textPaint.setColor(0xFFFFFFFF);
        canvas.drawText(u.hp + "/" + u.maxHp, x + UNIT_W * 0.5f, barY + HP_H - 1f, textPaint);

        barY += HP_H + BAR_GAP;

        // Rounded MP Bar
        rectPool.set(x, barY, x + UNIT_W, barY + MP_H);
        canvas.drawRoundRect(rectPool, 2f, 2f, mpBgPaint);

        float mpPercent = u.maxMp > 0 ? Math.max(0f, Math.min(1f, u.mp / (float)u.maxMp)) : 0f;
        float mpBarW = UNIT_W * mpPercent;
        if (mpBarW > 0) {
            rectPool.set(x, barY, x + mpBarW, barY + MP_H);
            canvas.drawRoundRect(rectPool, 2f, 2f, mpPaint);
        }

        barY += MP_H + BAR_GAP;

        // Rounded Action Gauge Bar
        float actionPercent = u.maxActionBar > 0 ? Math.max(0f, Math.min(1f, u.actionBar / (float)u.maxActionBar)) : 0f;
        float actionW = UNIT_W * actionPercent;
        actionPaint.setColor(ACTION_COLOR);
        if (actionW > 0) {
            rectPool.set(x, barY, x + actionW, barY + ACTION_H);
            canvas.drawRoundRect(rectPool, 2f, 2f, actionPaint);
        }

        if (actionPercent >= 1f) {
            glowPaint.setColor(ACTION_COLOR);
            glowPaint.setAlpha(100 + (int)(Math.sin(animTime * 8f) * 50));
            glowPaint.setStrokeWidth(2f);
            rectPool.set(x - 2f, barY - 2f, x + UNIT_W + 2f, barY + ACTION_H + 2f);
            canvas.drawRoundRect(rectPool, 2f, 2f, glowPaint);
            glowPaint.setAlpha(255);
        }
        textPaint.setTextAlign(Paint.Align.LEFT);
    }

    private void renderBattleInfo(Canvas canvas, int w, int h) {
        if (battle == null) return;
        textPaint.setTextSize(18f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        if (!battle.isRunning) {
            textPaint.setColor(battle.playerWon ? 0xFF4CAF50 : 0xFFFF5252);
            textPaint.setShadowLayer(12f, 0f, 0f, battle.playerWon ? 0x804CAF50 : 0x80FF5252);
            canvas.drawText(battle.playerWon ? "VICTORY!" : "DEFEAT!", w * 0.5f, 45f, textPaint);
            textPaint.setShadowLayer(0f, 0f, 0f, 0);

            // Interactive Return Button
            resultButtonRect.set(w * 0.5f - 130f, 65f, w * 0.5f + 130f, 115f);
            resultButtonBgPaint.setColor(0xEE1A0F2E);
            resultButtonBgPaint.setStyle(Paint.Style.FILL);
            canvas.drawRoundRect(resultButtonRect, 8f, 8f, resultButtonBgPaint);

            resultButtonBgPaint.setColor(battle.playerWon ? 0xFFFFD700 : 0xFFFF5252);
            resultButtonBgPaint.setStyle(Paint.Style.STROKE);
            resultButtonBgPaint.setStrokeWidth(2f);
            canvas.drawRoundRect(resultButtonRect, 8f, 8f, resultButtonBgPaint);

            resultButtonTextPaint.setColor(0xFFFFFFFF);
            resultButtonTextPaint.setTextSize(14f);
            canvas.drawText(battle.playerWon ? "⚔️ Claim Victory & Return" : "☠️ Retreat to Sect",
                w * 0.5f, 96f, resultButtonTextPaint);
        } else {
            textPaint.setColor(0xFFFFD700);
            textPaint.setShadowLayer(8f, 0f, 0f, 0x60FFD700);
            canvas.drawText("Round " + battle.round + " | Turn " + battle.turn, w * 0.5f, 40f, textPaint);
            textPaint.setShadowLayer(0f, 0f, 0f, 0);
        }
        textPaint.setTextAlign(Paint.Align.LEFT);
    }

    private void renderLog(Canvas canvas, int w, int h) {
        ArrayList<String> logs = battle.getLog();
        if (logs.isEmpty()) return;

        float logTop = h - LOG_BG_HEIGHT;
        rectPool.set(PADDING, logTop, w - PADDING, h - PADDING);
        canvas.drawRoundRect(rectPool, 12f, 12f, logBgPaint);

        int start = Math.max(0, logs.size() - LOG_LINES);
        textPaint.setTextSize(12f);

        textPaint.setColor(0xFFFFD700);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        canvas.drawText("≡ Battle Log ≡", PADDING + 14f, logTop + 22f, textPaint);

        for (int i = start; i < logs.size(); i++) {
            float textY = logTop + PADDING + 18f + (i - start + 1) * LOG_LINE_H;
            String msg = logs.get(i);

            if (msg.contains("VICTORY")) textPaint.setColor(0xFF4CAF50);
            else if (msg.contains("DEFEAT") || msg.contains("fallen")) textPaint.setColor(0xFFFF5252);
            else if (msg.contains("uses")) textPaint.setColor(0xFFFFD700);
            else if (msg.contains("crit")) textPaint.setColor(0xFFFF1744);
            else if (msg.contains("heal")) textPaint.setColor(0xFF00E676);
            else textPaint.setColor(TEXT_COLOR);

            canvas.drawText(msg, PADDING + 14f, textY, textPaint);
        }
        textPaint.setTypeface(Typeface.DEFAULT);
    }

    private void renderParticles(Canvas canvas) {
        for (int i = 0; i < activeParticles; i++) {
            float lifeRatio = ptLife[i] / ptMaxLife[i];
            particlePaint.setColor(ptColor[i]);
            particlePaint.setAlpha((int)(255 * lifeRatio));
            canvas.drawCircle(ptX[i], ptY[i], ptSize[i] * lifeRatio, particlePaint);
        }
        particlePaint.setAlpha(255);
    }

    private void renderFloatingTexts(Canvas canvas) {
        for (int i = 0; i < activeFloating; i++) {
            float lifeRatio = ftLife[i] / ftMaxLife[i];
            damagePaint.setColor(ftColor[i]);
            damagePaint.setAlpha((int)(255 * lifeRatio));
            damagePaint.setTextSize(20f + (1f - lifeRatio) * 10f);
            canvas.drawText(ftText[i], ftX[i], ftY[i], damagePaint);
        }
        damagePaint.setAlpha(255);
    }


    private void renderSwordSlashes(Canvas canvas) {
        if (slashCount < 2) return;

        slashPathOuter.rewind();
        slashPathCore.rewind();

        int startIdx = (slashHead - slashCount + MAX_SLASH_POINTS) % MAX_SLASH_POINTS;
        slashPathOuter.moveTo(slashX[startIdx], slashY[startIdx]);
        slashPathCore.moveTo(slashX[startIdx], slashY[startIdx]);

        for (int i = 1; i < slashCount; i++) {
            int prev = (slashHead - slashCount + i - 1 + MAX_SLASH_POINTS) % MAX_SLASH_POINTS;
            int curr = (slashHead - slashCount + i + MAX_SLASH_POINTS) % MAX_SLASH_POINTS;

            float midX = (slashX[prev] + slashX[curr]) * 0.5f;
            float midY = (slashY[prev] + slashY[curr]) * 0.5f;

            slashPathOuter.quadTo(slashX[prev], slashY[prev], midX, midY);
            slashPathCore.quadTo(slashX[prev], slashY[prev], midX, midY);
        }

        slashGlowPaint.setColor(0xFF00E5FF);
        slashGlowPaint.setStrokeWidth(14f);
        slashGlowPaint.setAlpha(180);
        canvas.drawPath(slashPathOuter, slashGlowPaint);

        slashCorePaint.setStrokeWidth(4f);
        slashCorePaint.setAlpha(255);
        canvas.drawPath(slashPathCore, slashCorePaint);
    }

    private void renderControlButtons(Canvas canvas, int w, int h) {
        // Top right simulation controls (Auto, Speed, Pause)
        float btnW = 85f;
        float btnH = 32f;
        float margin = 10f;
        float topY = 16f;

        btnAutoRect.set(w - (btnW * 3f + margin * 3f), topY, w - (btnW * 2f + margin * 3f), topY + btnH);
        btnSpeedRect.set(w - (btnW * 2f + margin * 2f), topY, w - (btnW + margin * 2f), topY + btnH);
        btnPauseRect.set(w - (btnW + margin), topY, w - margin, topY + btnH);

        buttonBgPaint.setColor(autoBattle ? 0xFF2E7D32 : 0xFF455A64);
        canvas.drawRoundRect(btnAutoRect, 6f, 6f, buttonBgPaint);
        canvas.drawText(autoBattle ? "AUTO: ON" : "AUTO: OFF", btnAutoRect.centerX(), btnAutoRect.centerY() + 5f, buttonTextPaint);

        buttonBgPaint.setColor(0xFF1565C0);
        canvas.drawRoundRect(btnSpeedRect, 6f, 6f, buttonBgPaint);
        stringBuilder.setLength(0);
        stringBuilder.append((int) simulationSpeed).append("X SPEED");
        canvas.drawText(stringBuilder.toString(), btnSpeedRect.centerX(), btnSpeedRect.centerY() + 5f, buttonTextPaint);

        buttonBgPaint.setColor(isPaused ? 0xFFC62828 : 0xFF37474F);
        canvas.drawRoundRect(btnPauseRect, 6f, 6f, buttonBgPaint);
        canvas.drawText(isPaused ? "PLAY" : "PAUSE", btnPauseRect.centerX(), btnPauseRect.centerY() + 5f, buttonTextPaint);

        // 4 Action Buttons (Attack, Skill, Item, Flee) - 80px style
        float actW = 80f;
        float actH = 38f;
        float actGap = 8f;
        float actStartY = h - actH - 12f;
        float totalActW = actW * 4f + actGap * 3f;
        float actStartX = (w - totalActW) * 0.5f;

        btnAttackRect.set(actStartX, actStartY, actStartX + actW, actStartY + actH);
        btnSkillRect.set(actStartX + (actW + actGap), actStartY, actStartX + (actW + actGap) + actW, actStartY + actH);
        btnItemRect.set(actStartX + (actW + actGap) * 2, actStartY, actStartX + (actW + actGap) * 2 + actW, actStartY + actH);
        btnFleeRect.set(actStartX + (actW + actGap) * 3, actStartY, actStartX + (actW + actGap) * 3 + actW, actStartY + actH);

        // Render Attack Button (Batik Gold Border)
        buttonBgPaint.setColor(0xFF3E2723);
        canvas.drawRoundRect(btnAttackRect, 8f, 8f, buttonBgPaint);
        glowPaint.setColor(com.sect.idle.procedural.IdleSectAssetGenerator.COLOR_BATIK_GOLD);
        glowPaint.setStrokeWidth(2f);
        canvas.drawRoundRect(btnAttackRect, 8f, 8f, glowPaint);
        canvas.drawText("⚔️ ATTACK", btnAttackRect.centerX(), btnAttackRect.centerY() + 5f, buttonTextPaint);

        // Render Skill Button (Cyan Accent)
        buttonBgPaint.setColor(0xFF006064);
        canvas.drawRoundRect(btnSkillRect, 8f, 8f, buttonBgPaint);
        glowPaint.setColor(com.sect.idle.procedural.IdleSectAssetGenerator.COLOR_CYAN);
        canvas.drawRoundRect(btnSkillRect, 8f, 8f, glowPaint);
        canvas.drawText("✨ SKILL", btnSkillRect.centerX(), btnSkillRect.centerY() + 5f, buttonTextPaint);

        // Render Item Button (Jade Green)
        buttonBgPaint.setColor(0xFF1B5E20);
        canvas.drawRoundRect(btnItemRect, 8f, 8f, buttonBgPaint);
        glowPaint.setColor(com.sect.idle.procedural.IdleSectAssetGenerator.COLOR_JADE_GREEN);
        canvas.drawRoundRect(btnItemRect, 8f, 8f, glowPaint);
        canvas.drawText("🧪 ITEM", btnItemRect.centerX(), btnItemRect.centerY() + 5f, buttonTextPaint);

        // Render Flee Button (Dark Red)
        buttonBgPaint.setColor(0xFFB71C1C);
        canvas.drawRoundRect(btnFleeRect, 8f, 8f, buttonBgPaint);
        glowPaint.setColor(0xFFFF5252);
        canvas.drawRoundRect(btnFleeRect, 8f, 8f, glowPaint);
        canvas.drawText("🏃 FLEE", btnFleeRect.centerX(), btnFleeRect.centerY() + 5f, buttonTextPaint);
    }

    public void onTouchDown(float x, float y) {
        if (btnAutoRect.contains(x, y)) {
            autoBattle = !autoBattle;
            try { AudioManager.getInstance(context).playSfx(AudioManager.SFX_CLICK); } catch (Throwable ignored) {}
            return;
        }
        if (btnSpeedRect.contains(x, y)) {
            if (simulationSpeed == 1.0f) simulationSpeed = 2.0f;
            else if (simulationSpeed == 2.0f) simulationSpeed = 4.0f;
            else simulationSpeed = 1.0f;
            try { AudioManager.getInstance(context).playSfx(AudioManager.SFX_CLICK); } catch (Throwable ignored) {}
            return;
        }
        if (btnPauseRect.contains(x, y)) {
            isPaused = !isPaused;
            try { AudioManager.getInstance(context).playSfx(AudioManager.SFX_CLICK); } catch (Throwable ignored) {}
            return;
        }

        // Action Buttons
        if (btnAttackRect.contains(x, y)) {
            try { AudioManager.getInstance(context).playSfx(AudioManager.SFX_STRIKE); } catch (Throwable ignored) {}
            if (battle != null && battle.isRunning) {
                battle.tick();
            }
            return;
        }
        if (btnSkillRect.contains(x, y)) {
            try { AudioManager.getInstance(context).playSfx(AudioManager.SFX_CRITICAL_STRIKE); } catch (Throwable ignored) {}
            if (battle != null && battle.isRunning) {
                battle.tick();
                battle.tick();
            }
            return;
        }
        if (btnItemRect.contains(x, y)) {
            try { AudioManager.getInstance(context).playSfx(AudioManager.SFX_PILL_CAULDRON_DING); } catch (Throwable ignored) {}
            return;
        }
        if (btnFleeRect.contains(x, y)) {
            try { AudioManager.getInstance(context).playSfx(AudioManager.SFX_DEFEAT); } catch (Throwable ignored) {}
            if (battleListener != null) {
                battleListener.onBattleEnded(false);
            }
            return;
        }

        addSlashPoint(x, y);

        if (battle == null) return;

        if (!battle.isRunning) {
            if (resultButtonRect.contains(x, y)) {
                if (battleListener != null) {
                    battleListener.onBattleEnded(battle.playerWon);
                }
                return;
            }
        }

        ArrayList<BattleUnit> units = battle.getUnits();
        int size = units.size();
        if (size == 0) return;

        android.util.DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float screenW = metrics.widthPixels;
        float screenH = metrics.heightPixels;
        float cy = screenH * 0.45f;
        float spacing = UNIT_H + 30f;

        selectedUnit = -1;
        for (int i = 0; i < size; i++) {
            BattleUnit u = units.get(i);
            boolean isLeft = u.team == 0;
            float ux = isLeft ? PADDING * 2f : screenW - PADDING * 2f - UNIT_W;
            float uy = cy + (i - size * 0.5f) * spacing;
            if (x >= ux && x <= ux + UNIT_W && y >= uy && y <= uy + UNIT_H + 40f) {
                selectedUnit = i;
                break;
            }
        }
    }

    public void onTouchUp(float x, float y) {}


    public void destroy() {
        for (Bitmap b : unitSpriteCache.values()) {
            if (b != null && !b.isRecycled()) {
                b.recycle();
            }
        }
        unitSpriteCache.clear();
        if (auraRenderer != null) {
            auraRenderer.releaseBuffer();
        }
        slashCount = 0;
        activeParticles = 0;
        activeFloating = 0;
    }

    public void reset() {
        selectedUnit = -1;
        animTime = 0f;
        activeParticles = 0;
        activeFloating = 0;
        shakeIntensity = 0f;
        flashIntensity = 0f;
    }
}
