package com.sect.idle.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;

import com.sect.idle.core.GameState;
import com.sect.idle.systems.CameraSystem;

/**
 * AscensionScene - Heavenly Realm Ascension Celebration Scene.
 * Pure Java standard without lambdas for Sketchware Pro v7.0.0 compatibility.
 */
public final class AscensionScene {
    private final Context context;
    private float time = 0f;

    private final Paint bgPaint;
    private final Paint titlePaint;
    private final Paint textPaint;
    private final Paint auraPaint;
    private final Paint btnPaint;

    private final RectF continueBtnRect;

    public AscensionScene(Context ctx) {
        this.context = ctx;

        bgPaint = new Paint();
        bgPaint.setColor(0xFF140028);

        titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(0xFFFFD700);
        titlePaint.setTextSize(30f);
        titlePaint.setTypeface(Typeface.DEFAULT_BOLD);
        titlePaint.setTextAlign(Paint.Align.CENTER);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xFF00E5FF);
        textPaint.setTextSize(14f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        auraPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        auraPaint.setStyle(Paint.Style.STROKE);
        auraPaint.setStrokeWidth(3f);
        auraPaint.setColor(0x80FFD700);

        btnPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        btnPaint.setColor(0xFF2E1A47);

        continueBtnRect = new RectF();
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

        canvas.drawText("✨ ASCENSION TO IMMORTAL REALM ✨", cx, 100f, titlePaint);
        canvas.drawText("Your soul has shed mortal ties and entered Godhood!", cx, 140f, textPaint);

        // Divine Portal Aura Rings
        for (int i = 1; i <= 5; i++) {
            float r = 40f * i + (float) Math.sin(time * 3f + i) * 10f;
            canvas.drawCircle(cx, cy, r, auraPaint);
        }

        // Continue Button
        float btnW = 220f;
        float btnH = 50f;
        continueBtnRect.set(cx - btnW * 0.5f, h - 120f, cx + btnW * 0.5f, h - 70f);

        canvas.drawRoundRect(continueBtnRect, 14f, 14f, btnPaint);
        canvas.drawText("🏯 Return to Sect Realm", continueBtnRect.centerX(), continueBtnRect.centerY() + 5f, titlePaint);
    }

    public void onTouchDown(float x, float y) {
        if (continueBtnRect.contains(x, y) && context instanceof GameActivity) {
            ((GameActivity) context).sceneManager.setState(GameState.SECT);
        }
    }
}
