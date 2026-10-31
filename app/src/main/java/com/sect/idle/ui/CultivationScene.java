package com.sect.idle.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;

import com.sect.idle.core.GameState;
import com.sect.idle.systems.CameraSystem;
import com.sect.idle.systems.RNG;

/**
 * CultivationScene - Real-time Qi Gathering & Breakthrough Scene.
 * Pure Java standard without lambdas for Sketchware Pro v7.0.0 compatibility.
 */
public final class CultivationScene {
    private final Context context;
    private float time = 0f;
    private float qiProgress = 0.2f;

    private final Paint bgPaint;
    private final Paint cardBgPaint;
    private final Paint cardBorderPaint;
    private final Paint titlePaint;
    private final Paint textPaint;
    private final Paint arrayRingPaint;
    private final Paint particlePaint;
    private final Paint btnPaint;

    private final RectF mainCardRect;
    private final RectF gatherBtnRect;
    private final RectF breakthroughBtnRect;

    private static final int PARTICLE_COUNT = 20;
    private final float[] pX = new float[PARTICLE_COUNT];
    private final float[] pY = new float[PARTICLE_COUNT];
    private final float[] pSpeed = new float[PARTICLE_COUNT];

    public CultivationScene(Context ctx) {
        this.context = ctx;

        bgPaint = new Paint();
        bgPaint.setColor(0xFF0A0A15);

        cardBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cardBgPaint.setColor(0xEE120A24);

        cardBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cardBorderPaint.setStyle(Paint.Style.STROKE);
        cardBorderPaint.setStrokeWidth(1.5f);
        cardBorderPaint.setColor(0x6000E5FF);

        titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(0xFF00E5FF);
        titlePaint.setTextSize(24f);
        titlePaint.setTypeface(Typeface.DEFAULT_BOLD);
        titlePaint.setTextAlign(Paint.Align.CENTER);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xFFFFFFFF);
        textPaint.setTextSize(13f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        arrayRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arrayRingPaint.setStyle(Paint.Style.STROKE);
        arrayRingPaint.setStrokeWidth(2f);
        arrayRingPaint.setColor(0x8000E5FF);

        particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        particlePaint.setColor(0xFFFFD700);

        btnPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        btnPaint.setColor(0xFF1A1A2E);

        mainCardRect = new RectF();
        gatherBtnRect = new RectF();
        breakthroughBtnRect = new RectF();

        for (int i = 0; i < PARTICLE_COUNT; i++) {
            pX[i] = RNG.nextFloat(0f, 1080f);
            pY[i] = RNG.nextFloat(0f, 1920f);
            pSpeed[i] = RNG.nextFloat(1f, 3f);
        }
    }

    public void update(float dt) {
        time += dt;
        qiProgress = Math.min(1.0f, qiProgress + dt * 0.05f);

        for (int i = 0; i < PARTICLE_COUNT; i++) {
            pY[i] -= pSpeed[i];
            if (pY[i] < -10f) {
                pX[i] = RNG.nextFloat(0f, 1080f);
                pY[i] = 1920f + 10f;
            }
        }
    }

    public void render(Canvas canvas, Paint paint, CameraSystem cam) {
        if (canvas == null) return;
        int w = canvas.getWidth();
        int h = canvas.getHeight();
        if (w <= 0 || h <= 0) return;

        canvas.drawRect(0, 0, w, h, bgPaint);

        float cx = w * 0.5f;
        float cy = h * 0.4f;

        // Render Rotating Spirit Array Rings
        for (int r = 1; r <= 3; r++) {
            float radius = 60f * r + (float) Math.sin(time * 2f + r) * 6f;
            canvas.drawCircle(cx, cy, radius, arrayRingPaint);
        }

        // Center Cultivator Dot
        canvas.drawCircle(cx, cy, 12f, particlePaint);

        // Card Overlay
        float cardW = Math.min(w * 0.88f, 380f);
        float cardH = 220f;
        mainCardRect.set(cx - cardW * 0.5f, h - cardH - 40f, cx + cardW * 0.5f, h - 40f);

        canvas.drawRoundRect(mainCardRect, 18f, 18f, cardBgPaint);
        canvas.drawRoundRect(mainCardRect, 18f, 18f, cardBorderPaint);

        canvas.drawText("🧘 MEDITATION & QI ABSORPTION", cx, mainCardRect.top + 36f, titlePaint);

        int pct = (int) (qiProgress * 100f);
        canvas.drawText("Dantian Qi Density: " + pct + "% / 100%", cx, mainCardRect.top + 70f, textPaint);

        // Buttons
        float btnW = (cardW - 40f) * 0.48f;
        gatherBtnRect.set(mainCardRect.left + 16f, mainCardRect.bottom - 60f, mainCardRect.left + 16f + btnW, mainCardRect.bottom - 16f);
        breakthroughBtnRect.set(mainCardRect.right - 16f - btnW, mainCardRect.bottom - 60f, mainCardRect.right - 16f, mainCardRect.bottom - 16f);

        canvas.drawRoundRect(gatherBtnRect, 10f, 10f, btnPaint);
        canvas.drawRoundRect(breakthroughBtnRect, 10f, 10f, btnPaint);

        canvas.drawText("✨ Gather Qi", gatherBtnRect.centerX(), gatherBtnRect.centerY() + 5f, textPaint);

        textPaint.setColor(qiProgress >= 1.0f ? 0xFFFFD700 : 0x80FFFFFF);
        canvas.drawText("⚡ Breakthrough", breakthroughBtnRect.centerX(), breakthroughBtnRect.centerY() + 5f, textPaint);
        textPaint.setColor(0xFFFFFFFF);
    }

    public void onTouchDown(float x, float y) {
        if (gatherBtnRect.contains(x, y)) {
            qiProgress = Math.min(1.0f, qiProgress + 0.15f);
        }
        if (breakthroughBtnRect.contains(x, y) && qiProgress >= 1.0f) {
            if (context instanceof GameActivity) {
                ((GameActivity) context).sceneManager.setState(GameState.TRIBULATION);
            }
            qiProgress = 0.2f;
        }
    }
}
