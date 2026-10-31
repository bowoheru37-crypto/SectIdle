package com.sect.idle.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;

import com.sect.idle.systems.CameraSystem;
import com.sect.idle.systems.RNG;

/**
 * LoadingScene - Xianxia aesthetic loading screen optimized for low-spec devices (e.g. itel A70).
 * Features animated spirit aura progress bar, rotating lore quotes, and glowing particle effects.
 * Pure Java standard without lambdas for Sketchware Pro v7.0.0 compatibility.
 */
public final class LoadingScene {
    private final Context context;
    private float progress = 0f;
    private float targetProgress = 1.0f;
    private float time = 0f;
    private float quoteTimer = 0f;
    private int currentQuoteIndex = 0;

    private final Paint bgPaint;
    private final Paint cardBgPaint;
    private final Paint cardBorderPaint;
    private final Paint titlePaint;
    private final Paint quotePaint;
    private final Paint barBgPaint;
    private final Paint barFillPaint;
    private final Paint barGlowPaint;
    private final Paint textPaint;
    private final Paint particlePaint;

    private LinearGradient barGradientShader;
    private float cachedBarX = -1f;
    private float cachedBarW = -1f;

    private final RectF cardRect;
    private final RectF barRect;
    private final RectF barFillRect;

    private static final String[] LORE_QUOTES = {
        "\"The Dao that can be spoken is not the eternal Dao.\"",
        "\"Gathering spiritual Qi from the heaven and earth to condense the Golden Core...\"",
        "\"Refining the Soul Fire within the Nine Heavens Cauldron...\"",
        "\"Immortal cultivation requires perseverance through thousands of tribulations.\"",
        "\"Aligning the Five Elements within the Dantian field...\"",
        "\"Observing the movement of stars to decipher celestial destiny...\""
    };

    // Particle pool for low RAM overhead
    private static final int PARTICLE_COUNT = 30;
    private final float[] pX = new float[PARTICLE_COUNT];
    private final float[] pY = new float[PARTICLE_COUNT];
    private final float[] pSpeed = new float[PARTICLE_COUNT];
    private final float[] pSize = new float[PARTICLE_COUNT];
    private final float[] pAlpha = new float[PARTICLE_COUNT];

    private Bitmap parallaxBg;

    public interface LoadingCompleteListener {
        void onLoadingComplete();
    }
    private LoadingCompleteListener completeListener;

    public LoadingScene(Context ctx) {
        this.context = ctx;

        bgPaint = new Paint();
        bgPaint.setColor(0xFF0A0A15);

        cardBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cardBgPaint.setColor(0xCC0D081A);

        cardBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cardBorderPaint.setStyle(Paint.Style.STROKE);
        cardBorderPaint.setStrokeWidth(1.5f);
        cardBorderPaint.setColor(0x40FFD700);

        titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(0xFFFFD700);
        titlePaint.setTextSize(36f);
        titlePaint.setTypeface(Typeface.DEFAULT_BOLD);
        titlePaint.setTextAlign(Paint.Align.CENTER);

        quotePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        quotePaint.setColor(0xFF00E5FF);
        quotePaint.setTextSize(14f);
        quotePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
        quotePaint.setTextAlign(Paint.Align.CENTER);

        barBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barBgPaint.setColor(0xFF1E1435);

        barFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        barGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barGlowPaint.setStyle(Paint.Style.STROKE);
        barGlowPaint.setStrokeWidth(2f);
        barGlowPaint.setColor(0xFF00E5FF);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xFFFFFFFF);
        textPaint.setTextSize(12f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        particlePaint.setColor(0xFF00E5FF);

        cardRect = new RectF();
        barRect = new RectF();
        barFillRect = new RectF();

        for (int i = 0; i < PARTICLE_COUNT; i++) {
            resetParticle(i, true);
        }
    }

    public void setAtlas(Bitmap parallax) {
        this.parallaxBg = parallax;
    }

    public void setCompleteListener(LoadingCompleteListener listener) {
        this.completeListener = listener;
    }

    public void setProgress(float p) {
        this.targetProgress = Math.max(0f, Math.min(1f, p));
    }

    public void resetProgress() {
        this.progress = 0f;
        this.targetProgress = 1.0f;
    }

    public float getProgress() {
        return progress;
    }

    private void resetParticle(int i, boolean randomY) {
        pX[i] = RNG.nextFloat(0f, 1080f);
        pY[i] = randomY ? RNG.nextFloat(0f, 1920f) : 1920f + 10f;
        pSpeed[i] = RNG.nextFloat(0.5f, 2.0f);
        pSize[i] = RNG.nextFloat(1.5f, 3.5f);
        pAlpha[i] = RNG.nextFloat(60f, 200f);
    }

    public void update(float dt) {
        time += dt;
        quoteTimer += dt;

        if (quoteTimer >= 3.5f) {
            quoteTimer = 0f;
            currentQuoteIndex = (currentQuoteIndex + 1) % LORE_QUOTES.length;
        }

        // Smooth progress interpolation
        if (progress < targetProgress) {
            progress = Math.min(targetProgress, progress + dt * 1.5f);
            if (progress >= 1f && completeListener != null) {
                completeListener.onLoadingComplete();
            }
        }

        for (int i = 0; i < PARTICLE_COUNT; i++) {
            pY[i] -= pSpeed[i];
            if (pY[i] < -20f) {
                resetParticle(i, false);
            }
        }
    }

    public void render(Canvas canvas, Paint paint, CameraSystem cam) {
        if (canvas == null) return;
        int w = canvas.getWidth();
        int h = canvas.getHeight();
        if (w <= 0 || h <= 0) return;

        // Background
        canvas.drawRect(0, 0, w, h, bgPaint);

        if (parallaxBg != null && !parallaxBg.isRecycled()) {
            canvas.drawBitmap(parallaxBg, null, new RectF(0, 0, w, h), paint);
        }

        // Render ambient spirit particles
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            particlePaint.setAlpha((int) pAlpha[i]);
            canvas.drawCircle(pX[i] % w, pY[i] % h, pSize[i], particlePaint);
        }

        float cx = w * 0.5f;
        float cy = h * 0.5f;

        // Center Glass Card
        float cardW = Math.min(w * 0.88f, 440f);
        float cardH = 260f;
        cardRect.set(cx - cardW * 0.5f, cy - cardH * 0.5f, cx + cardW * 0.5f, cy + cardH * 0.5f);

        canvas.drawRoundRect(cardRect, 20f, 20f, cardBgPaint);
        canvas.drawRoundRect(cardRect, 20f, 20f, cardBorderPaint);

        // Title
        canvas.drawText("ASCENDING REALM", cx, cy - 60f, titlePaint);

        // Lore Quote
        canvas.drawText(LORE_QUOTES[currentQuoteIndex], cx, cy - 15f, quotePaint);

        // Progress Bar Container
        float barW = cardW - 60f;
        float barH = 16f;
        float barX = cx - barW * 0.5f;
        float barY = cy + 40f;

        barRect.set(barX, barY, barX + barW, barY + barH);
        canvas.drawRoundRect(barRect, 8f, 8f, barBgPaint);

        // Progress Fill with Cached Shader
        float fillW = barW * progress;
        if (fillW > 0) {
            if (barGradientShader == null || cachedBarX != barX || cachedBarW != barW) {
                cachedBarX = barX;
                cachedBarW = barW;
                barGradientShader = new LinearGradient(
                    barX, barY, barX + barW, barY,
                    new int[]{0xFF00E5FF, 0xFFFFD700},
                    null, Shader.TileMode.CLAMP
                );
                barFillPaint.setShader(barGradientShader);
            }
            barFillRect.set(barX, barY, barX + fillW, barY + barH);
            canvas.drawRoundRect(barFillRect, 8f, 8f, barFillPaint);
        }

        canvas.drawRoundRect(barRect, 8f, 8f, barGlowPaint);

        // Progress Text
        int pct = (int) (progress * 100f);
        canvas.drawText("Gathering Qi... " + pct + "%", cx, barY + barH + 25f, textPaint);
    }
}
