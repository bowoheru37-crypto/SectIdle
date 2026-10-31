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
 * TribulationScene - Heavenly Lightning Deflection Mini-game Scene.
 * Pure Java standard without lambdas for Sketchware Pro v7.0.0 compatibility.
 */
public final class TribulationScene {
    private final Context context;
    private float time = 0f;
    private int strikesDeflected = 0;
    private static final int TOTAL_STRIKES = 9;
    private float shieldAngle = 0f;

    private final Paint bgPaint;
    private final Paint titlePaint;
    private final Paint lightningPaint;
    private final Paint shieldPaint;
    private final Paint textPaint;
    private final Paint btnPaint;

    private final RectF leftShieldBtn;
    private final RectF rightShieldBtn;

    public TribulationScene(Context ctx) {
        this.context = ctx;

        bgPaint = new Paint();
        bgPaint.setColor(0xFF0D0015);

        titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(0xFFFF5252);
        titlePaint.setTextSize(26f);
        titlePaint.setTypeface(Typeface.DEFAULT_BOLD);
        titlePaint.setTextAlign(Paint.Align.CENTER);

        lightningPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        lightningPaint.setColor(0xFF00E5FF);
        lightningPaint.setStrokeWidth(4f);
        lightningPaint.setStyle(Paint.Style.STROKE);

        shieldPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shieldPaint.setColor(0xFFFFD700);
        shieldPaint.setStrokeWidth(6f);
        shieldPaint.setStyle(Paint.Style.STROKE);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xFFFFFFFF);
        textPaint.setTextSize(14f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        btnPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        btnPaint.setColor(0xFF1E1435);

        leftShieldBtn = new RectF();
        rightShieldBtn = new RectF();
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
        float cy = h * 0.45f;

        canvas.drawText("⚡ NINE HEAVENS TRIBULATION ⚡", cx, 80f, titlePaint);
        canvas.drawText("Deflect Heavenly Lightning Strikes: " + strikesDeflected + " / " + TOTAL_STRIKES, cx, 120f, textPaint);

        // Draw Cultivator Center
        paint.setColor(0xFFFFD700);
        canvas.drawCircle(cx, cy, 20f, paint);

        // Draw Shield Aegis Barrier
        float shieldR = 70f;
        canvas.drawCircle(cx + (float) Math.cos(shieldAngle) * shieldR, cy + (float) Math.sin(shieldAngle) * shieldR, 18f, shieldPaint);

        // Render Animated Lightning Bolt
        float strikeX = cx + (RNG.nextFloat() * 40f - 20f);
        canvas.drawLine(strikeX, 160f, cx + (float) Math.cos(shieldAngle) * shieldR, cy + (float) Math.sin(shieldAngle) * shieldR, lightningPaint);

        // Touch Control Pill Buttons
        float btnW = 140f;
        float btnH = 50f;
        leftShieldBtn.set(cx - btnW - 20f, h - 100f, cx - 20f, h - 50f);
        rightShieldBtn.set(cx + 20f, h - 100f, cx + btnW + 20f, h - 50f);

        canvas.drawRoundRect(leftShieldBtn, 12f, 12f, btnPaint);
        canvas.drawRoundRect(rightShieldBtn, 12f, 12f, btnPaint);

        canvas.drawText("🛡️ Shift Left", leftShieldBtn.centerX(), leftShieldBtn.centerY() + 5f, textPaint);
        canvas.drawText("🛡️ Shift Right", rightShieldBtn.centerX(), rightShieldBtn.centerY() + 5f, textPaint);
    }

    public void onTouchDown(float x, float y) {
        if (leftShieldBtn.contains(x, y)) {
            shieldAngle -= 0.6f;
            strikesDeflected++;
        } else if (rightShieldBtn.contains(x, y)) {
            shieldAngle += 0.6f;
            strikesDeflected++;
        }

        if (strikesDeflected >= TOTAL_STRIKES) {
            strikesDeflected = 0;
            if (context instanceof GameActivity) {
                ((GameActivity) context).sceneManager.setState(GameState.ASCENSION);
            }
        }
    }
}
