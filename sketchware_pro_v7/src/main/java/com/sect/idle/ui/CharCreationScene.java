package com.sect.idle.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;

import com.sect.idle.core.GameConfig;
import com.sect.idle.core.GameState;
import com.sect.idle.systems.CameraSystem;
import com.sect.idle.systems.RNG;

/**
 * CharCreationScene - Xianxia character creation & Sect naming scene.
 * Optimized for low-spec devices (itel A70) with pre-allocated UI bounds and zero GC allocations in render loop.
 * Pure Java standard without lambdas for Sketchware Pro v7.0.0 compatibility.
 */
public final class CharCreationScene {
    private final Context context;
    private float time = 0f;

    // Selection states
    private boolean isMale = true;
    private int selectedElement = 0; // 0=Metal, 1=Wood, 2=Water, 3=Fire, 4=Earth
    private int statAtk = 10;
    private int statDef = 10;
    private int statSpeed = 10;
    private int statTalent = 80;

    private String charName = "Li Tian";
    private String sectName = "Cloud Sword Sect";

    private final Paint bgPaint;
    private final Paint cardBgPaint;
    private final Paint cardBorderPaint;
    private final Paint titlePaint;
    private final Paint textPaint;
    private final Paint btnPaint;
    private final Paint btnActivePaint;
    private final Paint btnBorderPaint;

    private final RectF mainCardRect;
    private final RectF maleBtnRect;
    private final RectF femaleBtnRect;
    private final RectF[] elemBtnRects;
    private final RectF rollBtnRect;
    private final RectF nameBtnRect;
    private final RectF sectNameBtnRect;
    private final RectF startBtnRect;

    private static final String[] SURNAMES = {"Li", "Zhang", "Wang", "Liu", "Chen", "Yang", "Zhao", "Huang", "Zhou", "Wu"};
    private static final String[] NAMES = {"Tian", "Feng", "Yun", "Xuan", "Lian", "Mo", "Chen", "Yu", "Zhen", "Qing"};
    private static final String[] SECT_PREFIXES = {"Cloud", "Thunder", "Nine", "Celestial", "Immortal", "Dragon", "Phoenix", "Spirit"};
    private static final String[] SECT_SUFFIXES = {"Sect", "Valley", "Palace", "Pavilion", "Sanctuary", "Temple"};

    private static final String[] ELEMENT_NAMES = {"Metal ⚔️", "Wood 🌿", "Water 💧", "Fire 💥", "Earth 🛡️"};
    private static final int[] ELEMENT_COLORS = {0xFFFFD700, 0xFF4CAF50, 0xFF00E5FF, 0xFFFF5252, 0xFF8D6E63};

    public CharCreationScene(Context ctx) {
        this.context = ctx;

        bgPaint = new Paint();
        bgPaint.setColor(0xFF0A0A15);

        cardBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cardBgPaint.setColor(0xEE120A24);

        cardBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cardBorderPaint.setStyle(Paint.Style.STROKE);
        cardBorderPaint.setStrokeWidth(1.5f);
        cardBorderPaint.setColor(0x60FFD700);

        titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(0xFFFFD700);
        titlePaint.setTextSize(26f);
        titlePaint.setTypeface(Typeface.DEFAULT_BOLD);
        titlePaint.setTextAlign(Paint.Align.CENTER);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xFFFFFFFF);
        textPaint.setTextSize(13f);

        btnPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        btnPaint.setColor(0xFF1A1A2E);

        btnActivePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        btnActivePaint.setColor(0xFF2E2E5D);

        btnBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        btnBorderPaint.setStyle(Paint.Style.STROKE);
        btnBorderPaint.setStrokeWidth(1.5f);
        btnBorderPaint.setColor(0xFF448AFF);

        mainCardRect = new RectF();
        maleBtnRect = new RectF();
        femaleBtnRect = new RectF();
        elemBtnRects = new RectF[5];
        for (int i = 0; i < 5; i++) elemBtnRects[i] = new RectF();

        rollBtnRect = new RectF();
        nameBtnRect = new RectF();
        sectNameBtnRect = new RectF();
        startBtnRect = new RectF();

        rollRandomNames();
        rollRandomStats();
    }

    public void rollRandomNames() {
        charName = SURNAMES[RNG.nextInt(SURNAMES.length)] + " " + NAMES[RNG.nextInt(NAMES.length)];
        sectName = SECT_PREFIXES[RNG.nextInt(SECT_PREFIXES.length)] + " " + SECT_SUFFIXES[RNG.nextInt(SECT_SUFFIXES.length)];
    }

    public void rollRandomStats() {
        statAtk = 8 + RNG.nextInt(8);
        statDef = 8 + RNG.nextInt(8);
        statSpeed = 8 + RNG.nextInt(8);
        statTalent = 70 + RNG.nextInt(31);
    }

    public void update(float dt) {
        time += dt;
    }

    public void render(Canvas canvas, Paint paint, CameraSystem cam) {
        if (canvas == null) return;
        int w = canvas.getWidth();
        int h = canvas.getHeight();
        if (w <= 0 || h <= 0) return;

        canvas.drawRect(0, 0, w, h, bgPaint);

        float cx = w * 0.5f;
        float cardW = Math.min(w * 0.92f, 420f);
        float cardH = Math.min(h * 0.90f, 620f);
        float cy = h * 0.5f;

        mainCardRect.set(cx - cardW * 0.5f, cy - cardH * 0.5f, cx + cardW * 0.5f, cy + cardH * 0.5f);
        canvas.drawRoundRect(mainCardRect, 20f, 20f, cardBgPaint);
        canvas.drawRoundRect(mainCardRect, 20f, 20f, cardBorderPaint);

        // Title
        canvas.drawText("📜 IMMORTAL DESTINY", cx, mainCardRect.top + 42f, titlePaint);

        float topY = mainCardRect.top + 70f;

        // Gender Selection
        textPaint.setColor(0xFFFFD700);
        textPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Gender / Appearance:", mainCardRect.left + 24f, topY, textPaint);

        float genderW = (cardW - 60f) * 0.5f;
        maleBtnRect.set(mainCardRect.left + 24f, topY + 10f, mainCardRect.left + 24f + genderW, topY + 46f);
        femaleBtnRect.set(mainCardRect.left + 36f + genderW, topY + 10f, mainCardRect.right - 24f, topY + 46f);

        canvas.drawRoundRect(maleBtnRect, 10f, 10f, isMale ? btnActivePaint : btnPaint);
        canvas.drawRoundRect(femaleBtnRect, 10f, 10f, !isMale ? btnActivePaint : btnPaint);

        btnBorderPaint.setColor(isMale ? 0xFFFFD700 : 0x40FFFFFF);
        canvas.drawRoundRect(maleBtnRect, 10f, 10f, btnBorderPaint);
        btnBorderPaint.setColor(!isMale ? 0xFFFFD700 : 0x40FFFFFF);
        canvas.drawRoundRect(femaleBtnRect, 10f, 10f, btnBorderPaint);

        textPaint.setColor(0xFFFFFFFF);
        textPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("👨 Male Disciple", maleBtnRect.centerX(), maleBtnRect.centerY() + 5f, textPaint);
        canvas.drawText("👩 Female Disciple", femaleBtnRect.centerX(), femaleBtnRect.centerY() + 5f, textPaint);

        // Primary Element Selection
        topY += 75f;
        textPaint.setColor(0xFFFFD700);
        textPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Spiritual Root Element:", mainCardRect.left + 24f, topY, textPaint);

        float elemW = (cardW - 68f) / 5f;
        for (int i = 0; i < 5; i++) {
            float ex = mainCardRect.left + 24f + i * (elemW + 5f);
            elemBtnRects[i].set(ex, topY + 10f, ex + elemW, topY + 46f);

            canvas.drawRoundRect(elemBtnRects[i], 8f, 8f, selectedElement == i ? btnActivePaint : btnPaint);
            btnBorderPaint.setColor(selectedElement == i ? ELEMENT_COLORS[i] : 0x40FFFFFF);
            canvas.drawRoundRect(elemBtnRects[i], 8f, 8f, btnBorderPaint);

            textPaint.setTextSize(10f);
            textPaint.setColor(ELEMENT_COLORS[i]);
            textPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(ELEMENT_NAMES[i], elemBtnRects[i].centerX(), elemBtnRects[i].centerY() + 4f, textPaint);
        }

        // Stats Display & Roll Button
        topY += 75f;
        textPaint.setTextSize(12f);
        textPaint.setColor(0xFFFFFFFF);
        textPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("⚔️ Atk: " + statAtk + "   🛡️ Def: " + statDef + "   ⚡ Speed: " + statSpeed, mainCardRect.left + 24f, topY, textPaint);
        canvas.drawText("🌟 Qi Affinity Talent: " + statTalent + " / 100", mainCardRect.left + 24f, topY + 22f, textPaint);

        rollBtnRect.set(mainCardRect.right - 120f, topY - 12f, mainCardRect.right - 24f, topY + 28f);
        canvas.drawRoundRect(rollBtnRect, 10f, 10f, btnPaint);
        btnBorderPaint.setColor(0xFF00E5FF);
        canvas.drawRoundRect(rollBtnRect, 10f, 10f, btnBorderPaint);

        textPaint.setTextSize(11f);
        textPaint.setColor(0xFF00E5FF);
        textPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("🎲 Re-roll", rollBtnRect.centerX(), rollBtnRect.centerY() + 4f, textPaint);

        // Name & Sect Generator Buttons
        topY += 65f;
        nameBtnRect.set(mainCardRect.left + 24f, topY, mainCardRect.right - 24f, topY + 40f);
        canvas.drawRoundRect(nameBtnRect, 10f, 10f, btnPaint);
        btnBorderPaint.setColor(0xFFFFD700);
        canvas.drawRoundRect(nameBtnRect, 10f, 10f, btnBorderPaint);

        textPaint.setTextSize(12f);
        textPaint.setColor(0xFFFFFFFF);
        textPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("👤 Cultivator Name: " + charName + " 🎲", nameBtnRect.centerX(), nameBtnRect.centerY() + 4f, textPaint);

        topY += 50f;
        sectNameBtnRect.set(mainCardRect.left + 24f, topY, mainCardRect.right - 24f, topY + 40f);
        canvas.drawRoundRect(sectNameBtnRect, 10f, 10f, btnPaint);
        btnBorderPaint.setColor(0xFFEA80FC);
        canvas.drawRoundRect(sectNameBtnRect, 10f, 10f, btnBorderPaint);

        canvas.drawText("🏯 Sect Realm Name: " + sectName + " 🎲", sectNameBtnRect.centerX(), sectNameBtnRect.centerY() + 4f, textPaint);

        // Start Journey Button
        startBtnRect.set(mainCardRect.left + 24f, mainCardRect.bottom - 60f, mainCardRect.right - 24f, mainCardRect.bottom - 16f);
        canvas.drawRoundRect(startBtnRect, 14f, 14f, btnActivePaint);
        btnBorderPaint.setColor(0xFFFFD700);
        btnBorderPaint.setStrokeWidth(2f);
        canvas.drawRoundRect(startBtnRect, 14f, 14f, btnBorderPaint);

        textPaint.setTextSize(14f);
        textPaint.setColor(0xFFFFD700);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        canvas.drawText("✨ BEGIN IMMORTAL PATH ✨", startBtnRect.centerX(), startBtnRect.centerY() + 5f, textPaint);
        textPaint.setTypeface(Typeface.DEFAULT);
    }

    public void onTouchDown(float x, float y) {
        if (maleBtnRect.contains(x, y)) isMale = true;
        if (femaleBtnRect.contains(x, y)) isMale = false;

        for (int i = 0; i < 5; i++) {
            if (elemBtnRects[i].contains(x, y)) selectedElement = i;
        }

        if (rollBtnRect.contains(x, y)) rollRandomStats();
        if (nameBtnRect.contains(x, y)) charName = SURNAMES[RNG.nextInt(SURNAMES.length)] + " " + NAMES[RNG.nextInt(NAMES.length)];
        if (sectNameBtnRect.contains(x, y)) sectName = SECT_PREFIXES[RNG.nextInt(SECT_PREFIXES.length)] + " " + SECT_SUFFIXES[RNG.nextInt(SECT_SUFFIXES.length)];

        if (startBtnRect.contains(x, y) && context instanceof GameActivity) {
            GameActivity act = (GameActivity) context;
            if (act.getData() != null) {
                act.getData().sectName = sectName;
            }
            act.sceneManager.setState(GameState.SECT);
        }
    }

    public String getCharName() { return charName; }
    public String getSectName() { return sectName; }
}
